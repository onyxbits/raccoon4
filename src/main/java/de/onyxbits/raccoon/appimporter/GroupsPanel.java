/*
 * Copyright 2017 Patrick Ahlbrecht
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
package de.onyxbits.raccoon.appimporter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.onyxbits.raccoon.appmgr.PayloadButtonModel;
import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.repo.AppGroup;
import de.onyxbits.raccoon.repo.AppGroupDao;

class GroupsPanel extends JPanel implements DatasetListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<PayloadButtonModel<AppGroup>> models;

	public static final String ID = GroupsPanel.class.getSimpleName();

	public GroupsPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		models = new ArrayList<PayloadButtonModel<AppGroup>>();
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {
		Object src = event.getSource();
		if (src instanceof AppGroupDao) {
			removeAll();
			models.clear();
			AppGroupDao dao = (AppGroupDao) src;
			try {
				List<AppGroup> groups = dao.list();
				if (groups.size() > 0) {
					for (AppGroup ag : groups) {
						PayloadButtonModel<AppGroup> model = new PayloadButtonModel<AppGroup>(
								ag);
						JCheckBox button = new JCheckBox(ag.getName(), false);
						button.setModel(model);
						models.add(model);
						add(button);
					}
				}
				else {
					add(new JLabel(Messages.getString(ID.concat(".nogroups"))));
				}
				revalidate();
			}
			catch (SQLException e) {
			}
		}
	}

	public List<AppGroup> getSelected() {
		ArrayList<AppGroup> ret = new ArrayList<AppGroup>();
		for (PayloadButtonModel<AppGroup> model : models) {
			if (model.isSelected()) {
				ret.add(model.getPayload());
			}
		}
		return ret;
	}

}
