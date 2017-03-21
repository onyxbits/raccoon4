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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.AppGroup;
import de.onyxbits.raccoon.repo.AppGroupDao;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * For assigning apps to groups
 * 
 * @author patrick
 * 
 */
class GroupListBuilder extends AbstractPanelBuilder implements ActionListener {

	private GridBagConstraints gbc;
	private JPanel container;
	private AndroidApp app;
	private JPanel spacer;

	@Override
	protected JPanel assemble() {
		container = new JPanel();
		spacer = new JPanel();
		container.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 2;
		gbc.insets.left = 5;

		reload();
		return container;
	}

	protected void reload() {
		try {
			List<AppGroup> groups = globals.get(DatabaseManager.class)
					.get(AppGroupDao.class).list();
			container.removeAll();
			gbc.gridy = 0;
			String tmp = Messages.getString("GroupListBuilder.nogroups");
			if (groups.size() > 0) {
				tmp = Messages.getString("GroupListBuilder.groups");
			}
			gbc.insets = new Insets(10, 10, 10, 10);
			container.add(new JLabel(tmp), gbc);
			gbc.gridy++;
			gbc.insets = new Insets(2,20,2,2);
			for (AppGroup ag : groups) {
				JCheckBox cb = new JCheckBox(ag.getName());
				cb.setModel(new PayloadButtonModel<AppGroup>(ag));
				if (app != null && app.memberOf(ag)) {
					cb.setSelected(true);
				}
				cb.addActionListener(this);
				container.add(cb, gbc);

				gbc.gridy++;
			}

			gbc.weightx = 1;
			gbc.weighty = 1;
			container.add(spacer, gbc);
			gbc.weightx = 0;
			gbc.weighty = 0;
			container.revalidate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void setApp(AndroidApp app) {
		this.app = app;

		for (int i = 0; i < container.getComponentCount() - 1; i++) {
			Component comp = container.getComponent(i);
			if (comp instanceof JCheckBox) {
				JCheckBox cb = (JCheckBox) comp;
				AppGroup ag = ((PayloadButtonModel<AppGroup>) cb.getModel())
						.getPayload();
				cb.setSelected(app.memberOf(ag));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (app != null) {
			List<AppGroup> ag = new Vector<AppGroup>();
			for (int i = 0; i < container.getComponentCount() - 1; i++) {
				Component comp = container.getComponent(i);
				if (comp instanceof JCheckBox) {
					JCheckBox cb = (JCheckBox) comp;
					if (cb.isSelected()) {
						ag.add(((PayloadButtonModel<AppGroup>) cb.getModel()).getPayload());
					}
				}
			}
			app.setGroups(ag);
			try {
				globals.get(DatabaseManager.class).get(AndroidAppDao.class)
						.saveOrUpdate(app);
			}
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

}
