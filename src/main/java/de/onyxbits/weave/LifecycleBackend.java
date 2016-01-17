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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Does the dirty work for the {@link LifecycleManager} so we can have a tidy
 * interface there.
 * 
 * @author patrick
 * 
 */
class LifecycleBackend implements WindowListener, Runnable {

	private LifecycleManager lifecycleManager;
	private Lifecycle lifecycle;
	private Object message;
	private Globals globals;

	protected LifecycleBackend(LifecycleManager lifecycleManager) {
		this.lifecycleManager = lifecycleManager;
	}

	protected LifecycleBackend(Globals globals, Lifecycle lifecycle,
			Object message) {
		this.lifecycle = lifecycle;
		this.message = message;
		this.globals = globals;
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (lifecycleManager.getWindow() == e.getWindow()) {
			// Primary window closed -> Application shutdown
			lifecycleManager.shutdown();
		}
		else {
			// Secondary window closed -> Hide and potentially recycle.
			e.getWindow().setVisible(false);
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void run() {
		lifecycle.onBusMessage(globals, message);
	}

}
