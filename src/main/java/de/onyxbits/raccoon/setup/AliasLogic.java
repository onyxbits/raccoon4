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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gui.HyperTextPane;

public class AliasLogic extends WizardBuilder implements CaretListener {

	private JTextField alias;

	public AliasLogic() {
		alias = new JTextField(20);
	}

	@Override
	public void onActivate() {
		previous.setEnabled(false);
		next.setEnabled(alias.getText().length() > 0);
		PlayProfile pp = globals.get(PlayProfile.class);
		if (pp.getAlias() == null) {
			alias.setText(pp.getUser().split("@")[0]);
		}
		else {
			alias.setText(pp.getAlias());
		}
	}

	@Override
	protected void onNext() {
		PlayProfile pp = globals.get(PlayProfile.class);
		pp.setAlias(alias.getText());
		finish();
	}

	@Override
	protected void onPrevious() {
	}

	@Override
	protected JPanel assemble() {
		alias.addCaretListener(this);
		JPanel ret = new JPanel();
		JLabel aliasLabel = new JLabel(Messages.getString("AliasLogic.alias"));
		HyperTextPane about = new HyperTextPane(
				Messages.getString("AliasLogic.about")).withWidth(500)
				.withTransparency();
		JPanel tmp = new JPanel();
		tmp.add(aliasLabel);
		tmp.add(alias);

		ret.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		ret.add(about, gbc);

		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		ret.add(tmp, gbc);

		return ret;
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		next.setEnabled(alias.getText().length() > 0);
	}

}
