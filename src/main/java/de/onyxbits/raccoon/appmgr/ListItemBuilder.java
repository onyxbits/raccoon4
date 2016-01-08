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
package de.onyxbits.raccoon.appmgr;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.onyxbits.raccoon.db.AndroidApp;
import de.onyxbits.raccoon.db.AppGroup;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * Represents one {@link AndroidApp}
 * 
 * @author patrick
 * 
 */
class ListItemBuilder extends AbstractPanelBuilder implements ActionListener {

	private AndroidApp androidApp;
	private JButton icon;
	protected JCheckBox checked;
	private BufferedImage image;
	protected JPanel panel;

	public ListItemBuilder(AndroidApp androidApp, BufferedImage image) {
		this.androidApp = androidApp;
		this.image = image;
		if (image == null) {
			this.image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
		}
	}

	/**
	 * Filter by name and/or group. This will reset the selection. Apps that don't
	 * apply will be hidden.
	 * 
	 * @param name
	 *          part of the apps name (must be lowercase) or null to not filter by
	 *          name.
	 * @param group
	 *          group or null to not fitler by group
	 */
	public void filter(String name, AppGroup group) {
		boolean nm = (name == null || androidApp.getName().toLowerCase()
				.contains(name));
		boolean gm = (group == null || androidApp.memberOf(group));
		checked.setSelected(checked.isSelected() && nm && gm);
		panel.setVisible(nm && gm);
	}

	@Override
	protected JPanel assemble() {
		icon = new JButton(new ImageIcon(image.getScaledInstance(48, 48,
				Image.SCALE_SMOOTH)));
		icon.addActionListener(this);
		checked = new JCheckBox();
		JLabel name = new JLabel(androidApp.getName());
		Font font = name.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 2);
		name.setFont(boldFont);
		JLabel version = new JLabel(androidApp.getVersion());
		Font italic = new Font(font.getFontName(), Font.ITALIC, font.getSize());
		version.setFont(italic);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.top = 5;
		gbc.insets.left = 5;
		gbc.insets.bottom = 5;
		gbc.insets.right = 5;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridheight = 3;
		panel.add(icon, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.top = 10;
		gbc.insets.left = 0;
		gbc.insets.bottom = 0;
		gbc.insets.right = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridheight = 1;
		panel.add(name, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.top = 3;
		gbc.insets.left = 0;
		gbc.insets.bottom = 0;
		gbc.insets.right = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridheight = 1;
		panel.add(version, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.top = 3;
		gbc.insets.left = 0;
		gbc.insets.bottom = 0;
		gbc.insets.right = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridheight = 1;
		panel.add(checked, gbc);

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Window w = globals.get(LifecycleManager.class).getWindow(
				DetailsViewBuilder.ID);
		globals.get(DetailsViewBuilder.class).setApp(androidApp);
		w.setVisible(true);
	}

	public AndroidApp getApp() {
		return androidApp;
	}

}
