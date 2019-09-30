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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gui.HyperTextPane;

public class ProxyLogic extends WizardBuilder implements ActionListener,
		CaretListener {

	private JTextField username;
	private JPasswordField password;
	private JTextField server;
	private JSpinner port;

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		username = new JTextField(20);
		username.setMargin(new Insets(2, 2, 2, 2));
		password = new JPasswordField(20);
		password.setMargin(new Insets(2, 2, 2, 2));
		server = new JTextField(20);
		server.setMargin(new Insets(2, 2, 2, 2));
		port = new JSpinner(new SpinnerNumberModel(3218, 1, 65535, 1));
		port.setEditor(new JSpinner.NumberEditor(port, "#"));
		HyperTextPane about = new HyperTextPane(
				Messages.getString("ProxyLogic.about")).withWidth(500)
				.withTransparency();
		username.addActionListener(this);
		password.addActionListener(this);
		username.addCaretListener(this);
		password.addCaretListener(this);
		server.addCaretListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		gbc.insets.bottom = 3;
		container.add(new JLabel(Messages.getString("ProxyLogic.server")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		container.add(server, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		gbc.insets.bottom = 3;
		container.add(new JLabel(Messages.getString("ProxyLogic.port")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		container.add(port, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		gbc.insets.bottom = 3;
		container.add(new JLabel(Messages.getString("ProxyLogic.username")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		container.add(username, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		container.add(new JLabel(Messages.getString("ProxyLogic.password")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 3;
		container.add(password, gbc);

		panel.add(about);
		panel.add(Box.createVerticalStrut(20));
		panel.add(container);
		panel.add(Box.createVerticalStrut(20));

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == server) {
			username.requestFocusInWindow();
		}
		if (e.getSource() == username) {
			password.requestFocusInWindow();
		}
		if (e.getSource() == password && next.isEnabled()) {
			onNext();
		}
	}

	@Override
	protected void onActivate() {
		PlayProfile pp = globals.get(PlayProfile.class);
		server.setText(pp.getProxyAddress());
		username.setText(pp.getProxyUser());
		password.setText(pp.getProxyPassword());
		if (pp.getProxyPort() == 0) {
			port.setValue(3128);
		}
		else {
			port.setValue(pp.getProxyPort());
		}
		previous.setEnabled(true);
		next.setEnabled(server.getText().length() > 0);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		previous.setEnabled(true);
		next.setEnabled(server.getText().length() > 0);
	}

	@Override
	protected void onNext() {
		PlayProfile pp = globals.get(PlayProfile.class);
		if (server.getText().length() > 0) {
			pp.setProxyAddress(server.getText());
			pp.setProxyPort((Integer) port.getValue());
			if (username.getText().length() > 0) {
				pp.setProxyUser(username.getText());
			}
			else {
				pp.setProxyUser(null);
			}
			if (password.getPassword().length > 0) {
				pp.setProxyPassword(new String(password.getPassword()));
			}
			else {
				pp.setProxyPassword(null);
			}
		}
		else {
			pp.setProxyAddress(null);
			pp.setProxyPassword(null);
			pp.setProxyUser(null);
		}
		show(LoginLogic.class);
	}

	@Override
	protected void onPrevious() {
		show(AccountLogic.class);
	}

}
