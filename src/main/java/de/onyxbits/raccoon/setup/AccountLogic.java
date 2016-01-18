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
package de.onyxbits.raccoon.setup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gui.HyperTextPane;

/**
 * Handles credentials
 * 
 * @author patrick
 * 
 */
public class AccountLogic extends WizardBuilder implements ActionListener,
		CaretListener {

	private JTextField username;
	private JPasswordField password;
	private JCheckBox proxy;

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		username = new JTextField(20);
		username.setMargin(new Insets(2, 2, 2, 2));
		password = new JPasswordField(20);
		password.setMargin(new Insets(2, 2, 2, 2));
		proxy = new JCheckBox(Messages.getString("AccountLogic.proxy"));
		proxy.setSelected(false);
		HyperTextPane about = new HyperTextPane(
				Messages.getString("AccountLogic.about")).withWidth(500)
				.withTransparency();
		username.addActionListener(this);
		password.addActionListener(this);
		username.addCaretListener(this);
		password.addCaretListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		gbc.insets.bottom = 3;
		container.add(new JLabel(Messages.getString("AccountLogic.username")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		container.add(username, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		container.add(new JLabel(Messages.getString("AccountLogic.password")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		container.add(password, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		container.add(proxy, gbc);

		panel.add(about);
		panel.add(Box.createVerticalStrut(20));
		panel.add(container);
		panel.add(Box.createVerticalStrut(20));

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == username) {
			password.requestFocusInWindow();
		}
		if (e.getSource() == password && next.isEnabled()) {
			onNext();
		}
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		next.setEnabled(username.getText().length() > 0
				&& password.getPassword().length > 0);
	}

	@Override
	public void onActivate() {
		username.setText(globals.get(PlayProfile.class).getUser());
		previous.setEnabled(false);
		next.setEnabled(username.getText().length() > 0
				&& password.getPassword().length > 0);
	}

	@Override
	protected void onNext() {
		PlayProfile pp = globals.get(PlayProfile.class);
		// Copy&paste tends to include whitespace -> filter away
		pp.setUser(username.getText().trim());
		pp.setAlias(username.getText().trim().split("@")[0]);
		pp.setPassword(new String(password.getPassword()));
		if (proxy.isSelected()) {
			show(ProxyLogic.class);
		}
		else {
			pp.setProxyAddress(null);
			pp.setProxyPassword(null);
			pp.setProxyUser(null);
			show(LoginLogic.class);
		}
	}

	@Override
	protected void onPrevious() {
	}

}
