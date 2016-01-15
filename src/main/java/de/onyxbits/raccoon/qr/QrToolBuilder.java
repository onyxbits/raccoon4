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
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

public class QrToolBuilder extends AbstractPanelBuilder implements
		CaretListener {

	public static final String ID = QrToolBuilder.class.getSimpleName();

	private JTextArea input;
	private QrPanel output;
	private String empty;

	public QrToolBuilder() {
		input = new JTextArea(15, 30);
		input.setMargin(new Insets(2, 2, 2, 2));
		output = new QrPanel(400);
	}

	@Override
	protected JPanel assemble() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		empty = Messages.getString(ID + ".empty");

		File tmpDir = new File(System.getProperty("java.io.tmpdir"));

		output.withActions(new CopyCodeAction(globals, output), null,
				new SaveAction(globals, output, tmpDir));
		if (input.getText().length() == 0) {
			output.setContentString(empty);
		}

		TitleStrip titleStrip = new TitleStrip(Messages.getString(ID + ".title"),
				Messages.getString(ID + ".subtitle"), new ImageIcon(getClass()
						.getResource("/icons/appicon.png")));

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setLeftComponent(new JScrollPane(input));
		split.setRightComponent(output);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(titleStrip, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(split, gbc);

		input.addCaretListener(this);
		return ret;
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (input.getText().length() == 0) {
			output.setContentString(empty);
		}
		else {
			output.setContentString(input.getText());
		}
	}

	/**
	 * Set the displayed value
	 * 
	 * @param text
	 *          to show.
	 */
	public void setValue(String content) {
		input.setText(content);
		input.setCaretPosition(0);
		output.setContentString(content);
	}

}
