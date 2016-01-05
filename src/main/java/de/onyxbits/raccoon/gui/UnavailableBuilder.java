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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.AdapterBuilder;

public class UnavailableBuilder extends AbstractPanelBuilder implements
		ActionListener {

	public static final String ID = UnavailableBuilder.class.getSimpleName();

	private JButton cancel;
	private JButton getit;
	private HyperTextPane about;

	public UnavailableBuilder() {
		about = new HyperTextPane(" ").withWidth(300).withTransparency();
		about.setPreferredSize(new Dimension(320, 150));
	}

	@Override
	protected JPanel assemble() {
		cancel = new JButton(Messages.getString(ID + ".reject"));
		getit = new JButton(Messages.getString(ID + ".obtain"));
		cancel.addActionListener(this);
		getit.addActionListener(this);
		JPanel wrapper = new JPanel();
		wrapper.add(about);
		return new DialogBuilder(new AdapterBuilder(wrapper))
				.withButtons(new ButtonBarBuilder().add(cancel).add(getit))
				.withTitle(Messages.getString(ID + ".title"))
				.withSubTitle(Messages.getString(ID + ".subtitle")).build(globals);
	}

	public void setAbout(String text) {
		about.setText(text);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		LifecycleManager lm = globals.get(LifecycleManager.class);
		if (src == cancel) {

		}
		if (src == getit) {
			lm.getWindow(GrantBuilder.ID).setVisible(true);
		}
		lm.closeWindow(ID);
	}
}
