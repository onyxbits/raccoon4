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
package de.onyxbits.raccoon.qr;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Copies the content of a {@link QrPanel} to the clipboard
 * 
 * @author patrick
 * 
 */
public class CopyContentAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private QrPanel panel;
	private Globals globals;

	public CopyContentAction(Globals globals, QrPanel panel) {
		this.panel = panel;
		this.globals = globals;
		putValue(NAME, Messages.getString("action.copycontent"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (globals.get(Traits.class).isAvailable("4.0.x")) {
			StringSelection s = new StringSelection(panel.getContentString());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
		}
		else {
			globals.get(LifecycleManager.class).sendBusMessage(new JTextField());
		}
	}

}
