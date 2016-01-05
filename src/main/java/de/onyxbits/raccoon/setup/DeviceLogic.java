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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.onyxbits.raccoon.gui.HyperTextPane;

public class DeviceLogic extends WizardBuilder {

	private JRadioButton existing;
	private JRadioButton mock;

	@Override
	protected JPanel assemble() {

		HyperTextPane about = new HyperTextPane(
				Messages.getString("DeviceLogic.about")).withWidth(500)
				.withTransparency();
		mock = new JRadioButton(Messages.getString("DeviceLogic.mock"));
		existing = new JRadioButton(Messages.getString("DeviceLogic.existing"));
		ButtonGroup bg = new ButtonGroup();
		bg.add(mock);
		bg.add(existing);
		mock.setSelected(true);

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.insets.bottom = 20;
		JPanel choices = new JPanel();
		choices.setLayout(new BoxLayout(choices, BoxLayout.Y_AXIS));
		choices.add(mock, gbc);
		choices.add(existing, gbc);
		ret.add(about, gbc);
		ret.add(choices, gbc);

		return ret;
	}

	@Override
	protected void onActivate() {
		previous.setEnabled(true);
		next.setEnabled(true);
	}

	@Override
	protected void onNext() {
		if (mock.isSelected()) {
			show(UploadLogic.class);
		}
		else {
			show(MimicLogic.class);
		}
	}

	@Override
	protected void onPrevious() {
		show(AccountLogic.class);
	}

}
