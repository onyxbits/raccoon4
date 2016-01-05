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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.net.ServerManager;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * A builder for putting arbitrary files on the webserver and sharing them via
 * QR code.
 * 
 * @author patrick
 * 
 */
public class ShareToolBuilder extends AbstractPanelBuilder implements
		PropertyChangeListener {

	public static final String ID = ShareToolBuilder.class.getSimpleName();

	private QrPanel transfer;
	private JFileChooser chooser;

	@Override
	protected JPanel assemble() {
		JPanel ret = new JPanel();
		TitleStrip titleStrip = new TitleStrip(Messages.getString(ID + ".title"),
				Messages.getString(ID + ".subtitle"), new ImageIcon(getClass()
						.getResource("/icons/appicon.png")));
		transfer = new QrPanel(300);
		transfer.withActions(new CopyContentAction(globals, transfer));
		transfer.setBorder(new TitledBorder(Messages.getString(ID + ".transfer")));

		chooser = new JFileChooser(globals.get(Layout.class).shareDir);
		chooser.setMultiSelectionEnabled(true);
		chooser.setControlButtonsAreShown(false);
		chooser.addPropertyChangeListener(this);

		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(titleStrip, gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		ret.add(chooser, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(7, 7, 7, 7);
		ret.add(transfer, gbc);

		return ret;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == chooser) {
			File[] file = chooser.getSelectedFiles();
			if (file.length > 0) {
				ServerManager sm = globals.get(ServerManager.class);
				sm.setAtttribute(Traits.class.getName(), globals.get(Traits.class));
				transfer.setContentString(sm.serve(file).toString());
			}
			else {
				transfer.setContentString(Messages.getString(ID + ".nofiles"));
			}
		}
	}

}
