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
package de.onyxbits.raccoon.ptools;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Invoke the screenshot tool on the device, then transfer the file.
 * 
 * @author patrick
 * 
 */
public class ScreenshotAction extends BridgeAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ScreenshotAction(Globals globals) {
		super(globals);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (globals.get(Traits.class).isAvailable("4.0.x")) {
			globals.get(TransferManager.class).schedule(globals,
					new ScreenshotWorker(globals),TransferManager.USB);
		}
		else {
			globals.get(LifecycleManager.class).sendBusMessage(
					new JTextField(Messages.getString(getClass().getSimpleName() + ".about")));
		}
	}
}
