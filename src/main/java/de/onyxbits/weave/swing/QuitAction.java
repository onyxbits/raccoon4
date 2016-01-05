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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * An action for shutting the application down.
 * 
 * @author patrick
 * 
 */
public class QuitAction extends AbstractAction {

	private Globals globals;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new action and bind an accelerator key to it.
	 * 
	 * @param globals
	 *          registry
	 * @param keycode
	 *          a VK_ constant from {@link KeyEvent}.
	 */
	public QuitAction(Globals globals, int keycode, boolean useModifier) {
		this.globals = globals;
		if (useModifier) {
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keycode, Toolkit
					.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		else {
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keycode, 0));
		}
	}

	/**
	 * Create a new action and bind MODIFIER-Q to it.
	 * 
	 * @param globals
	 *          registry
	 */
	public QuitAction(Globals globals) {
		this(globals, KeyEvent.VK_Q, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		globals.get(LifecycleManager.class).shutdown();
	}

}
