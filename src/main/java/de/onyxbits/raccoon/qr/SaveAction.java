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

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * An action for saving the QR code to a file
 * 
 * @author patrick
 * 
 */
public class SaveAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private QrPanel panel;

	private int seq;

	private File dir;

	private Globals globals;

	public SaveAction(Globals globals, QrPanel panel, File dir) {
		this.panel = panel;
		this.dir = dir;
		this.globals = globals;
		putValue(NAME, Messages.getString("action.save"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (globals.get(Traits.class).isAvailable("4.0.x")) {
			JFileChooser jfc = new JFileChooser();
			String fn = MessageFormat.format(
					Messages.getString("SaveAction.filename"), seq);
			jfc.setSelectedFile(new File(dir, fn));
			FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
					Messages.getString("SaveAction.types"), "png");
			jfc.setFileFilter(fileFilter);
			jfc.setFileFilter(new FileNameExtensionFilter(Messages
					.getString("SaveAction.filetype"), "png"));
			int ret = jfc.showSaveDialog(panel);
			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					ImageIO.write(panel.getImage(), "png", jfc.getSelectedFile());
					seq++;
				}
				catch (Exception exp) {
					exp.printStackTrace();
				}
			}
		}
		else {
			globals.get(LifecycleManager.class).sendBusMessage(new JTextField());
		}
	}

}
