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
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.db.PlayProfile;
import de.onyxbits.raccoon.gui.HyperTextPane;

/**
 * For entering the parameters of an existing device.
 * 
 * @author patrick
 * 
 */
public class MimicLogic extends WizardBuilder implements CaretListener,
		ActionListener {

	private JTextField gsfId;
	private JTextField userAgent;

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		gsfId = new JTextField(20);
		gsfId.setMargin(new Insets(2, 2, 2, 2));
		userAgent = new JTextField(20);
		userAgent.setMargin(new Insets(2, 2, 2, 2));
		HyperTextPane about = new HyperTextPane(
				Messages.getString("ExistingLogic.about")).withWidth(500)
				.withTransparency();
		gsfId.addActionListener(this);
		userAgent.addActionListener(this);
		gsfId.addCaretListener(this);
		userAgent.addCaretListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		gbc.insets.bottom = 3;
		container.add(new JLabel(Messages.getString("ExistingLogic.gsfid")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		container.add(gsfId, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		container.add(new JLabel(Messages.getString("ExistingLogic.useragent")),
				gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		container.add(userAgent, gbc);

		panel.add(about);
		panel.add(Box.createVerticalStrut(20));
		panel.add(container);
		panel.add(Box.createVerticalStrut(20));

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == gsfId) {
			userAgent.requestFocusInWindow();
		}
		if (e.getSource() == userAgent) {
			onNext();
		}
	}

	@Override
	protected void onActivate() {
		PlayProfile pp = globals.get(PlayProfile.class);
		gsfId.setText(pp.getGsfId());
		userAgent.setText(pp.getAgent());
		previous.setEnabled(true);
		next.setEnabled(gsfId.getText().length() > 0
				&& userAgent.getText().length() > 0);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		next.setEnabled(gsfId.getText().length() > 0
				&& userAgent.getText().length() > 0);
	}

	@Override
	protected void onNext() {
		PlayProfile pp = globals.get(PlayProfile.class);
		pp.setAgent(userAgent.getText());
		pp.setGsfId(gsfId.getText());
		show(UploadLogic.class);
	}

	@Override
	protected void onPrevious() {
		show(DeviceLogic.class);
	}

}
