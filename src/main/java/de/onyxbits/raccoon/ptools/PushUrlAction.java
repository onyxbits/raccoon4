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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Send urls from the clipboard to the device.
 * 
 * @author patrick
 * 
 */
public class PushUrlAction extends BridgeAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PushUrlAction(Globals globals) {
		super(globals);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = "am start -a android.intent.action.VIEW -d ";
		try {
			String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor);
			String tmp = new URI(data.trim()).toString();
			cmd += "\"";
			cmd += tmp;
			cmd += "\"";
			Device device = globals.get(BridgeManager.class).getActiveDevice();
			new Thread(new CommandRunner(device, cmd)).start();
		}
		catch (Exception exp) {
			String id = getClass().getSimpleName();
			JOptionPane.showMessageDialog(globals.get(LifecycleManager.class)
					.getWindow(), Messages.getString(id + ".message"), Messages
					.getString(id + ".title"), JOptionPane.ERROR_MESSAGE);
		}
	}

}
