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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.AdapterBuilder;

public class GrantBuilder extends AbstractPanelBuilder implements
		ActionListener, CaretListener {

	public static final String ID = GrantBuilder.class.getSimpleName();

	private JTextField input;
	private JButton go;
	private JPanel panel;

	@Override
	protected JPanel assemble() {
		String tmp = Messages.getString(Traits.rev("tuoba.esnecil"));
		tmp = MessageFormat.format(tmp, Bookmarks.ORDER, globals.get(Traits.class)
				.getChallenge().toUpperCase());
		HyperTextPane about = new HyperTextPane(tmp).withTransparency().withWidth(
				300);
		input = new JTextField(20);
		input.setMargin(new Insets(2, 2, 2, 2));
		input.addCaretListener(this);
		input.addActionListener(this);
		go = new JButton(Messages.getString(Traits.rev("ylppa.esnecil")));
		go.addActionListener(this);
		go.setEnabled(false);

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		content.add(about, gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets.top = 15;
		gbc.anchor = GridBagConstraints.WEST;
		content.add(new JLabel(Messages.getString(Traits.rev("yek.esnecil"))), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets.left = 5;
		content.add(input, gbc);

		panel = new DialogBuilder(new AdapterBuilder(content))
				.withTitle(Messages.getString(Traits.rev("eltit.esnecil")))
				.withSubTitle(Messages.getString(Traits.rev("eltitbus.esnecil")))
				.withButtons(new ButtonBarBuilder().add(go)).build(globals);
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == go || src == input) {
			if (globals.get(Traits.class).grant(input.getText().trim())) {
				globals.get(LifecycleManager.class).closeWindow(ID);
				JOptionPane.showMessageDialog(panel,
						Messages.getString(Traits.rev("deilppa.esnecil")));
			}
			else {
				JOptionPane.showMessageDialog(panel,
						Messages.getString(Traits.rev("dilavni.esnecil")));
			}
		}
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		go.setEnabled(input.getText().length() > 0);
	}
}
