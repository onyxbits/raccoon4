/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.weave;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The life cycle manager. This class must be started on the UI thread
 * (typically by using {@link SwingUtilities#invokeLater(Runnable)}).
 * 
 * @author patrick
 * 
 */
public final class LifecycleManager implements Runnable {

	/**
	 * State: manager has been created, but is not yet running.
	 */
	public static final int INSTANTIATED = 0;

	/**
	 * State: manager is starting up.
	 */
	public static final int BOOTING = 1;

	/**
	 * State: manager is fully operational
	 */
	public static final int RUNNING = 2;

	/**
	 * State: manager is shutting down.
	 */
	public static final int CLOSING = 3;

	/**
	 * State: manager is shut down.
	 */
	public static final int FINISHED = 4;

	private Globals globals;
	private HashMap<String, Window> secondaries;
	private Window primary;
	private Lifecycle lifecycle;
	private int state;
	private Object block;

	/**
	 * Create a manager for a lifecycle.
	 * 
	 * @param lifecycle
	 *          the {@link Lifecycle} to manage.
	 */
	public LifecycleManager(Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		this.secondaries = new HashMap<String, Window>();
		this.state = INSTANTIATED;
		this.block = new Object();
	}

	@Override
	public final void run() {
		if (state != INSTANTIATED) {
			throw new IllegalStateException("Cannot start a lifecycle twice");
		}
		setState(BOOTING);
		globals = new Globals(this);
		lifecycle.onSetup(globals);
		getWindow().setVisible(true);
		setState(RUNNING);
	}

	/**
	 * Get the primary window.
	 * 
	 * @return the main window of this manager's lifecycle.
	 */
	public Window getWindow() {
		if (primary == null) {
			primary = lifecycle.onCreatePrimaryWindow(globals);
			if (primary instanceof JFrame) {
				((JFrame) primary).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			}
			primary.addWindowListener(new LifecycleBackend(this));
		}
		return primary;
	}

	/**
	 * Lookup a secondary window by its identifier.
	 * 
	 * @param id
	 *          window identifier
	 * @return the window instance.
	 */
	public Window getWindow(String id) {
		Window ret = secondaries.get(id);
		if (ret == null) {
			ret = lifecycle.onCreateSecondaryWindow(globals, id);
			if (ret instanceof JFrame) {
				((JFrame) ret).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			}
			if (ret.getName() == null) {
				ret.setName(id); // for unit testing.
			}
			secondaries.put(id, ret);
			ret.addWindowListener(new LifecycleBackend(this));
		}
		return ret;
	}

	/**
	 * Hide, dispose and remove a window from management. NOTE: Only secondary
	 * windows that are no longer needed and either cannot reasonably be recycled
	 * or may exist in multiple instances should be destroyed. In most cases it is
	 * better to hide and reuse them. The primary window cannot be destroyed
	 * except by shutting down the {@link Lifecycle}.
	 * 
	 * @param id
	 *          the window identifier (nothing happens if unknown).
	 */
	public void destroyWindow(String id) {
		Window bye = secondaries.get(id);
		if (bye != null) {
			// Be explicit! There might be listeners that want notification
			bye.getToolkit().getSystemEventQueue()
					.postEvent(new WindowEvent(bye, WindowEvent.WINDOW_CLOSING));
			bye.dispose();
			secondaries.remove(id);
			lifecycle.onDestroySecondaryWindow(id);
		}
	}

	/**
	 * Close a managed window by sending a window event. The window may be
	 * reopened later.
	 * 
	 * @param id
	 *          window identifier.
	 */
	public void closeWindow(String id) {
		Window w = getWindow(id);
		if (w != null) {
			closeWindow(w);
		}
	}

	/**
	 * Send a {@link WindowEvent#WINDOW_CLOSING} event to a given window. The
	 * window is only hidden and may be shown again later.
	 * 
	 * @param w
	 *          the window to close.
	 */
	public static void closeWindow(Window w) {
		w.getToolkit().getSystemEventQueue()
				.postEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Shut the {@link Lifecycle} down. The user may veto this.
	 * <p>
	 * Shutting down does not necessarily imply that the JVM should exit as well.
	 * There are several use cases that call for multiple lifecycles in the same
	 * application/JVM:
	 * <ul>
	 * <li>Long running applications that mostly exist as a background process and
	 * only need to show an UI from time to time.
	 * <li>Multi document applications that open one primary window per file.
	 * <li>Applications with setup wizards.
	 * <li>Applications that allow the user to switch the L&F at runtime.
	 * </ul>
	 */
	public void shutdown() {
		if (!SwingUtilities.isEventDispatchThread()) {
			// Two reasons:
			// 1. Saves a lot of code not having to make this thread safe
			// 2. The lifecycle (window) should never go away without the user
			// explicitly requesting it. The user can only do this on the EDT.
			//
			// If for some strange reason the lifecycle must be forced down
			// externally, use sendBusMessage() to communicate the fact and do it from
			// within.
			throw new RuntimeException("shutdown() may only be called on the EDT");
		}

		if (lifecycle.onRequestShutdown(globals)) {
			setState(CLOSING);

			Set<String> ids = secondaries.keySet();
			for (String id : ids) {
				secondaries.get(id).dispose();
				lifecycle.onDestroySecondaryWindow(id);
			}
			secondaries.clear();
			primary.dispose();
			lifecycle.onTeardown(globals);
			setState(FINISHED);
		}
	}

	/**
	 * Send a message to the application. May be called from any thread, the
	 * message will be processed on the event thread. This method blocks till
	 * {@link Lifecycle#onBusMessage(Globals, Object))} returns.
	 * <p>
	 * This method is primarily designed as a communication channel between
	 * background threads and the {@link Lifecycle}, but it can also be used as
	 * general purpose messaging channel between otherwise independent application
	 * packages (though that should be avoided).
	 * 
	 * @param message
	 *          the message
	 * @throws rethrow
	 *           whatever gets thrown in
	 *           {@link Lifecycle#onBusMessage(Globals, Object)}.
	 */
	public void sendBusMessage(Object message) throws RuntimeException {
		if (state != RUNNING) {
			throw new RuntimeException("Lifecycle is not running!");
		}

		try {
			if (EventQueue.isDispatchThread()) {
				lifecycle.onBusMessage(globals, message);
			}
			else {
				EventQueue.invokeAndWait(new LifecycleBackend(globals, lifecycle,
						message));
			}
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw ((RuntimeException) e.getCause());
		}
	}

	/**
	 * Block till the {@link Lifecycle} enters a given state. Immediately return
	 * if that state has already passed.
	 * 
	 * @param state
	 *          the state to wait for.
	 */
	public void waitForState(int state) {
		if (SwingUtilities.isEventDispatchThread()) {
			// Hello!
			// 1. Never ever wait on the EDT for anything (on principle!)
			// 2. This particular method creates a deadlock on the EDT.
			throw new RuntimeException("Can't wait on the EDT!");
		}

		synchronized (block) {
			while (!(state <= this.state)) {
				try {
					block.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Query the state of the lifecycle.May be called from any {@link Thread}
	 * 
	 * @return CREATED, BOOTING, RUNNING, CLOSING or FINISHED.
	 */
	public synchronized int getState() {
		return state;
	}

	private void setState(int newState) {
		state = newState;
		synchronized (block) {
			block.notifyAll();
		}
	}
}
