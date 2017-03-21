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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.db.DatasetListenerProxy;
import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.raccoon.net.ServerManager;
import de.onyxbits.raccoon.qr.CopyContentAction;
import de.onyxbits.raccoon.qr.QrPanel;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.AppGroup;
import de.onyxbits.raccoon.repo.AppGroupDao;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.WindowToggleAction;

/**
 * A browser for listing all the apps in local storage.
 * 
 * @author patrick
 * 
 */
public class MyAppsViewBuilder extends AbstractPanelBuilder implements
		CaretListener, ActionListener, DatasetListener {

	public static final String ID = MyAppsViewBuilder.class.getSimpleName();

	private JComboBox<AppGroup> groupFilter;
	private JTextField nameFilter;
	private String lastFilter;
	private QrPanel transfer;
	private ListViewBuilder listView;
	private ListWorker listWorker;
	private JButton install;

	@Override
	protected JPanel assemble() {
		listView = new ListViewBuilder();
		JScrollPane listScroll = new JScrollPane(listView.build(globals));
		listScroll.setPreferredSize(new Dimension(400, 500));
		listScroll.getVerticalScrollBar().setUnitIncrement(20);

		TitleStrip titleStrip = new TitleStrip(Messages.getString(ID + ".title"),
				Messages.getString(ID + ".subTitle"), new ImageIcon(getClass()
						.getResource("/icons/appicon.png")));
		nameFilter = new JTextField(10);
		nameFilter.setMargin(new Insets(2, 2, 2, 2));
		nameFilter.addCaretListener(this);
		nameFilter.requestFocusInWindow();

		groupFilter = new JComboBox<AppGroup>();
		groupFilter.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		filterPanel.add(new JLabel(Messages.getString(ID + ".byname")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		filterPanel.add(nameFilter, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		filterPanel.add(new JLabel(Messages.getString(ID + ".bygroup")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		filterPanel.add(groupFilter, gbc);
		filterPanel.setBorder(BorderFactory.createTitledBorder(Messages
				.getString(ID + ".filter")));

		ActionLocalizer al = Messages.getLocalizer();
		Action toggle = al.localize(new InvertAction(listView), "invertselection");

		Action editGroups = al.localize(
				new WindowToggleAction(globals.get(LifecycleManager.class),
						GroupEditorBuilder.ID), "editgroups");

		install = new JButton(listView.installAction);
		install.addActionListener(this);

		JPanel actionPanel = new ButtonBarBuilder()
				.withVerticalAlignment()
				.addButton(editGroups)
				.addButton(toggle)
				.add(install)
				.addButton(listView.exportAction)
				.addButton(listView.deleteAction)
				.withBorder(
						BorderFactory.createTitledBorder(Messages
								.getString(ID + ".actions"))).build(globals);

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());

		transfer = new QrPanel(200);
		transfer.withActions(new CopyContentAction(globals, transfer));
		String location = globals.get(ServerManager.class)
				.serve(new ArrayList<AndroidApp>()).toString();

		transfer.setContentString(location);
		transfer.setBorder(BorderFactory.createTitledBorder(Messages.getString(ID
				+ ".transfer")));

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.insets.bottom = 10;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(titleStrip, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.insets.right = 10;
		gbc.insets.left = 5;
		ret.add(filterPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		ret.add(actionPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		ret.add(transfer, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 3;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.insets.right = 5;
		gbc.insets.left = 0;
		ret.add(listScroll, gbc);

		reloadGroups();
		reloadList();

		globals.get(DatabaseManager.class).get(AndroidAppDao.class)
				.addDataSetListener(new DatasetListenerProxy(this));
		globals.get(DatabaseManager.class).get(AppGroupDao.class)
				.addDataSetListener(new DatasetListenerProxy(this));
		return ret;
	}

	private void reloadGroups() {
		try {
			Vector<AppGroup> groups = globals.get(DatabaseManager.class)
					.get(AppGroupDao.class).list();

			groupFilter.setModel(new DefaultComboBoxModel<AppGroup>(groups));
			groupFilter.insertItemAt(null, 0);
			groupFilter.setSelectedIndex(0);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void reloadList() {
		if (listWorker != null) {
			listWorker.cancel(true);
		}
		if (listView != null) {
			// Entirely possible that this method gets called without the window being
			// assembled -> Silently ignore the request.
			listView.clear();
			listWorker = new ListWorker(listView, transfer, globals);
			listWorker.execute();
		}
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		String tmp = nameFilter.getText();
		if (!tmp.equals(lastFilter)) {
			lastFilter = tmp;
			URI uri = listView.filter(tmp, (AppGroup) groupFilter.getSelectedItem());
			transfer.setContentString(uri.toString());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == groupFilter) {
			URI uri = listView.filter(lastFilter,
					(AppGroup) groupFilter.getSelectedItem());
			transfer.setContentString(uri.toString());
		}
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {
		reloadList();
		reloadGroups();
	}

}
