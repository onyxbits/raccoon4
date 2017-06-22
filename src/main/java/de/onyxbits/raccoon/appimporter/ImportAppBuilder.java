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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.onyxbits.raccoon.appmgr.GroupEditorBuilder;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gplay.PlayProfileDao;
import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.AppGroupDao;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.WindowToggleAction;

public class ImportAppBuilder extends AbstractPanelBuilder implements
		ActionListener, DatasetListener, ListSelectionListener {

	private JButton scan;
	private JButton imprt;
	private JList<Candidate> list;
	private JComboBox<PlayProfile> profile;
	private TitleStrip titleStrip;
	private ScannerWorker scanner;
	private GroupsPanel groupsPanel;

	public static final String ID = ImportAppBuilder.class.getSimpleName();

	private JTextField progress;

	public ImportAppBuilder() {
		scan = new JButton(loadIcon("/icons/famfam/icons/folder_magnify.png"));
		scan.setToolTipText(Messages.getString(ID + ".scan.short_description"));
		imprt = new JButton(loadIcon("/icons/famfam/icons/folder_add.png"));
		imprt.setToolTipText(Messages.getString(ID + ".imprt.short_description"));
		list = new JList<Candidate>();
		profile = new JComboBox<PlayProfile>();
		progress = new JTextField();
		groupsPanel = new GroupsPanel();
		progress.setEditable(false);
		titleStrip = new TitleStrip(Messages.getString(ID
				.concat(".titlestrip.title")), Messages.getString(ID
				.concat(".titlestrip.subtitle")), new ImageIcon(getClass().getResource(
				"/icons/appicon.png")));
		progress.setText(Messages.getString(ID.concat(".howto")));
		imprt.setEnabled(false);
	}

	@Override
	protected JPanel assemble() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel scanPanel = new JPanel();
		JPanel importPanel = new JPanel();
		scanPanel.setBorder(new TitledBorder(
				Messages.getString(ID.concat(".step1"))));
		scanPanel.setLayout(new GridBagLayout());
		importPanel.setBorder(new TitledBorder(Messages.getString(ID
				.concat(".step2"))));
		importPanel.setLayout(new GridBagLayout());

		Action editGroups = Messages.getLocalizer().localize(
				new WindowToggleAction(globals.get(LifecycleManager.class),
						GroupEditorBuilder.ID), "editgroups");

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(2, 2, 2, 2);
		scanPanel.add(progress, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		scanPanel.add(scan, gbc);

		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(400, 200));
		scroll.setMinimumSize(new Dimension(400, 200));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 1;
		gbc.weighty = 1;
		scanPanel.add(scroll, gbc);

		scroll = new JScrollPane(groupsPanel);
		scroll.setPreferredSize(new Dimension(400, 200));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 0;
		gbc.weighty = 0;
		importPanel.add(scroll, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 0;
		gbc.weighty = 0;
		JButton editGroupsButton = new JButton(editGroups);
		editGroupsButton.setToolTipText(Messages.getString(ID
				+ ".editgroupsbutton.short_description"));
		importPanel.add(editGroupsButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 2, 2, 2);
		gbc.weightx = 0;
		gbc.weighty = 0;
		importPanel.add(new JLabel(Messages.getString(ID.concat(".update"))), gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 2, 2, 2);
		importPanel.add(profile, gbc);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 10, 0);
		ret.add(titleStrip, gbc);

		gbc.gridy = 1;
		gbc.weightx = 1.0;
		ret.add(scanPanel, gbc);

		gbc.gridy = 2;
		gbc.weightx = 1.0;
		ret.add(importPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(2, 2, 2, 2);
		ret.add(imprt, gbc);

		imprt.addActionListener(this);
		list.addListSelectionListener(this);
		scan.addActionListener(this);
		globals.get(DatabaseManager.class).get(PlayProfileDao.class)
				.subscribe(this);
		globals.get(DatabaseManager.class).get(AppGroupDao.class)
				.subscribe(groupsPanel);
		return ret;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src == scan) {
			if (scanner != null) {
				scanner.cancel(true);
				scanner = null;
				progress.setText(Messages.getString(ID.concat(".howto")));
			}
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int sel = chooser.showOpenDialog(scan);
			if (sel == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				scanner = new ScannerWorker(f, 6, globals.get(DatabaseManager.class)
						.get(AndroidAppDao.class));
				list.setModel(scanner.getModel());
				progress.setDocument(scanner.getDocument());
				scanner.execute();
			}
		}

		if (src == imprt) {
			List<Candidate> selected = list.getSelectedValuesList();
			TransferManager ts = globals.get(TransferManager.class);
			for (Candidate c : selected) {
				ts.schedule(
						globals,
						new ImportAppWorker(globals, c.src, (PlayProfile) profile
								.getSelectedItem(), groupsPanel.getSelected()),
						TransferManager.LAN);
			}
		}
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {
		if (event.getSource() instanceof PlayProfileDao) {
			PlayProfileDao dao = (PlayProfileDao) event.getSource();
			profile.removeAllItems();
			profile.addItem(null);
			String tmp = dao.get().getAlias();
			List<PlayProfile> all = dao.list();
			for (PlayProfile p : all) {
				profile.addItem(p);
				if (tmp.equals(p.getAlias())) {
					profile.setSelectedItem(p);
				}
			}
		}
		if (event.getSource() instanceof AppGroupDao) {

		}
	}

	private ImageIcon loadIcon(String path) {
		return new ImageIcon(getClass().getResource(path));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			imprt.setEnabled(list.getSelectedValuesList().size() > 0);
		}
	}
}
