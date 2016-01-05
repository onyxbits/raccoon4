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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * Construct an Eclipse style dialog with a title strip, content pane and button
 * bar.
 * 
 * @author patrick
 * 
 */
public class DialogBuilder extends AbstractPanelBuilder {

	private String titleIcon;
	private String titleText;
	private String titleSubText;
	private AbstractPanelBuilder contentBuilder;
	private JComponent content;
	private ButtonBarBuilder buttons;
	private Action help;

	public DialogBuilder(AbstractPanelBuilder contentBuilder) {
		this.contentBuilder = contentBuilder;
		this.titleIcon = "/icons/appicon.png";
		this.titleSubText = "";
		this.titleText = "Unknown Dialog";
	}

	public DialogBuilder(JComponent content) {
		this.content = content;
		this.titleIcon = "/icons/appicon.png";
		this.titleSubText = "";
		this.titleText = "Unknown Dialog";
	}

	@Override
	protected JPanel assemble() {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		ImageIcon ii = new ImageIcon(getClass().getResource(titleIcon));
		ret.add(new TitleStrip(titleText, titleSubText, ii), gbc);


		JComponent contentPanel = content;
		if (contentPanel == null) {
			contentPanel = contentBuilder.build(globals);
		}
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ret.add(contentPanel, gbc);

		if (buttons != null || help != null) {
			ret.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
			gbc.gridx = 0;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.gridwidth = 1;
			if (help != null) {
				gbc.anchor = GridBagConstraints.WEST;
				ret.add(new JButton(help), gbc);
			}
			else {
				ret.add(Box.createHorizontalGlue(), gbc);
			}
			gbc.gridx++;
			gbc.fill = GridBagConstraints.BOTH;
			ret.add(Box.createHorizontalGlue(), gbc);
			gbc.gridx++;
			if (buttons != null) {
				JPanel buttonPanel = buttons.build(globals);
				gbc.fill = GridBagConstraints.NONE;
				gbc.anchor = GridBagConstraints.EAST;
				ret.add(buttonPanel, gbc);
			}
			else {
				ret.add(Box.createHorizontalGlue(), gbc);
			}
		}
		return ret;
	}

	public DialogBuilder withTitleIcon(String icon) {
		this.titleIcon = icon;
		return this;
	}

	public DialogBuilder withTitle(String titleText) {
		this.titleText = titleText;
		return this;
	}

	public DialogBuilder withSubTitle(String titleSubText) {
		this.titleSubText = titleSubText;
		return this;
	}

	public DialogBuilder withButtons(ButtonBarBuilder buttons) {
		this.buttons = buttons;
		return this;
	}

	public DialogBuilder withHelp(Action help) {
		this.help = help;
		return this;
	}

}
