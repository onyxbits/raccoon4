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
package de.onyxbits.raccoon.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.onyxbits.weave.Globals;

/**
 * Adds "Escape to close" and "CTRL-Q to quit" actions to secondary windows.
 * 
 * @author patrick
 * 
 */
class CloseTool implements ActionListener {

	private CloseTool() {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Window w = SwingUtilities.windowForComponent((Component) e.getSource());
		w.getToolkit().getSystemEventQueue()
				.postEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Bind an action to a component that will send a
	 * {@link WindowEvent#WINDOW_CLOSING} event to containing window if the Escape
	 * key is pressed.
	 * 
	 * @param rootPane
	 *          the component (should be the root pane of the window).
	 */
	public static void bindTo(Globals globals, Window w) {
		CloseTool dc = new CloseTool();
		JComponent rootPane = null;
		if (w instanceof JFrame) {
			rootPane = (JComponent) ((JFrame) w).getContentPane();
		}
		if (w instanceof JDialog) {
			rootPane = (JComponent) ((JDialog) w).getContentPane();
		}
		rootPane.registerKeyboardAction(dc,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.registerKeyboardAction(new QuitAction(globals), KeyStroke
				.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit()
						.getMenuShortcutKeyMask()), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

}
