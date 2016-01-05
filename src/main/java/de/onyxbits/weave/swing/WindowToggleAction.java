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
package de.onyxbits.weave.swing;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;

import de.onyxbits.weave.LifecycleManager;

/**
 * An action for toggling the visibility of an application frame (suitable for
 * being used with a {@link JToggleButton} or a {@link JCheckBoxMenuItem} in the
 * "View" menu). NOTE: This action does not enforce frame creation until it is
 * actually needed. The downside of loading windows lazily is that it is
 * impossible to tell if another part of the system has already brought the
 * window up. Therefore, this action should be used as the exclusive toggle for
 * its window.
 * <p>
 * If a window needs to be toggled from multiple places, subclass this action
 * and put it in the {@link Globals} registry.
 * 
 * @author patrick
 * 
 */
public class WindowToggleAction extends AbstractAction implements
		WindowListener {

	private LifecycleManager lifecycleManager;
	private String id;
	private boolean connected;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param globals
	 *          registry
	 * @param id
	 *          window identifier of the secondary window
	 */
	public WindowToggleAction(LifecycleManager lifecycleManager, String id) {
		this.lifecycleManager = lifecycleManager;
		this.id = id;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		toggleWindow();
	}

	/**
	 * Toggle window visibility.
	 */
	public void toggleWindow() {
		Window frame = lifecycleManager.getWindow(id);
		if (!connected) {
			frame.addWindowListener(this);
			connected = true;
			// Catch up, just in case someone ignored the warning and made another
			// toggle for the window.
			putValue(SELECTED_KEY, frame.isVisible());
		}
		frame.setVisible(!frame.isVisible());
	}

	/**
	 * Show the window
	 */
	public void showWindow() {
		Window frame = lifecycleManager.getWindow(id);
		if (!connected) {
			frame.addWindowListener(this);
			connected = true;
			// Catch up, just in case someone ignored the warning and made another
			// toggle for the window.
			putValue(SELECTED_KEY, true);
		}
		frame.setVisible(true);
	}

	/**
	 * Hide the window
	 */
	public void hideWindow() {
		Window frame = lifecycleManager.getWindow(id);
		if (!connected) {
			frame.addWindowListener(this);
			connected = true;
			// Catch up, just in case someone ignored the warning and made another
			// toggle for the window.
			putValue(SELECTED_KEY, false);
		}
		frame.setVisible(false);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		putValue(SELECTED_KEY, Boolean.TRUE);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		putValue(SELECTED_KEY, Boolean.FALSE);
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

}
