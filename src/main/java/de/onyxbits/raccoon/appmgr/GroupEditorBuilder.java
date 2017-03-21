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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.DialogBuilder;
import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.repo.AppGroup;
import de.onyxbits.raccoon.repo.AppGroupDao;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;

public class GroupEditorBuilder extends AbstractPanelBuilder implements
		ActionListener, ListSelectionListener {

	/**
	 * ID for referencing this builder.
	 */
	public static final String ID = GroupEditorBuilder.class.getSimpleName();

	private JList<AppGroup> list;
	private JButton add;
	private JButton delete;
	private JButton edit;

	@Override
	protected JPanel assemble() {
		ActionLocalizer al = Messages.getLocalizer();

		add = new JButton(al.localize("add"));
		delete = new JButton(al.localize("deletegroup"));
		edit = new JButton(al.localize("edit"));
		list = new JList<AppGroup>();

		add.addActionListener(this);
		delete.addActionListener(this);
		edit.addActionListener(this);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(10);

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		content.add(new HyperTextPane(Messages.getString(ID + ".about"))
				.withTransparency().withWidth(250), gbc);

		gbc.gridy++;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		content.add(new JScrollPane(list), gbc);

		reload();

		return new DialogBuilder(content)
				.withTitle(Messages.getString(ID + ".title"))
				.withSubTitle(Messages.getString(ID + ".subtitle"))
				.withButtons(new ButtonBarBuilder().add(delete).add(edit).add(add))
				.build(globals);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == delete) {
			deleteGroup();
		}
		if (src == add) {
			if (globals.get(Traits.class).isAvailable("4.0.x")) {
				addGroup();
			}
			else {
				globals.get(LifecycleManager.class).sendBusMessage(new JTextField());
			}
		}
		if (src == edit) {
			editGroup();
		}
	}

	private void editGroup() {
		Window window = SwingUtilities.getWindowAncestor(add);
		Object obj = JOptionPane.showInputDialog(window,
				Messages.getString(ID + ".edit.message"),
				Messages.getString(ID + ".edit.title"), JOptionPane.QUESTION_MESSAGE,
				null, null, list.getSelectedValue().getName());
		String str;
		if (obj != null && (str = obj.toString()).length() > 0) {
			try {
				AppGroup ag = list.getSelectedValue();
				if (!ag.getName().equals(str)) {
					ag.setName(str);
					DatabaseManager dbm = globals.get(DatabaseManager.class);
					dbm.get(AppGroupDao.class).update(ag);
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(window,
						Messages.getString(ID + ".failure"));
				// e.printStackTrace();
			}
			reload();
		}
	}

	private void deleteGroup() {
		Window window = SwingUtilities.getWindowAncestor(add);
		int ret = JOptionPane.showConfirmDialog(window,
				Messages.getString(ID + ".delete.message"),
				Messages.getString(ID + ".delete.title"), JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			try {
				DatabaseManager dbm = globals.get(DatabaseManager.class);
				dbm.get(AppGroupDao.class).delete(list.getSelectedValue());
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			reload();
		}
	}

	private void addGroup() {
		Window window = SwingUtilities.getWindowAncestor(add);

		Object obj = JOptionPane.showInputDialog(window,
				Messages.getString(ID + ".add.message"),
				Messages.getString(ID + ".add.title"), JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		String str;
		if (obj != null && (str = obj.toString()).length() > 0) {
			try {
				AppGroup ag = new AppGroup();
				ag.setName(str);
				DatabaseManager dbm = globals.get(DatabaseManager.class);
				dbm.get(AppGroupDao.class).insert(ag);
			}
			catch (Exception e) {
				// There already is a group by this name. No need to tell the user that
				// we didn't create one.
				// e.printStackTrace();
			}
			reload();
		}
	}

	private void reload() {
		try {
			list.setListData(globals.get(DatabaseManager.class)
					.get(AppGroupDao.class).list());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		buttons();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			buttons();
		}
	}

	private void buttons() {
		edit.setEnabled(list.getSelectedValue() != null);
		delete.setEnabled(list.getSelectedValue() != null);
	}

}
