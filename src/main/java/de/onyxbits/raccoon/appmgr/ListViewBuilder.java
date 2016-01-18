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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.net.ServerManager;
import de.onyxbits.raccoon.ptools.BridgeManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * A container panel that lists {@link ListItemBuilder} panels.
 * 
 * @author patrick
 * 
 */
class ListViewBuilder extends AbstractPanelBuilder implements ActionListener {

	private JPanel list;
	private JPanel spacer;
	private Vector<ListItemBuilder> appListItems;
	private GridBagConstraints itemConstraints;
	private GridBagConstraints spacerConstraints;
	protected InstallAction installAction;
	protected DeleteAction deleteAction;
	protected ExportAction exportAction;

	public void clear() {
		list.removeAll();
		appListItems.clear();
		list.add(spacer, spacerConstraints);
		list.revalidate();
	}

	/**
	 * Callback for the worker
	 * 
	 * @param apps
	 *          item to add.
	 */
	protected void onAdd(List<ListItemBuilder> apps) {
		for (ListItemBuilder app : apps) {
			appListItems.add(app);
			list.add(app.build(globals), itemConstraints,
					list.getComponentCount() - 1);
			app.checked.addActionListener(this);
		}
		list.revalidate();
	}

	@Override
	protected JPanel assemble() {
		list = new JPanel();
		list.setLayout(new GridBagLayout());
		appListItems = new Vector<ListItemBuilder>();
		spacer = new JPanel();
		itemConstraints = new GridBagConstraints();
		itemConstraints.gridx = GridBagConstraints.REMAINDER;
		itemConstraints.gridy = GridBagConstraints.RELATIVE;
		itemConstraints.anchor = GridBagConstraints.NORTHWEST;
		itemConstraints.fill = GridBagConstraints.NONE;
		itemConstraints.weightx = 0;
		itemConstraints.weighty = 0;
		itemConstraints.ipady = 5;

		spacerConstraints = new GridBagConstraints();
		spacerConstraints.gridx = GridBagConstraints.REMAINDER;
		spacerConstraints.gridy = GridBagConstraints.RELATIVE;
		spacerConstraints.anchor = GridBagConstraints.NORTHWEST;
		spacerConstraints.fill = GridBagConstraints.BOTH;
		spacerConstraints.weightx = 1;
		spacerConstraints.weighty = 1;

		clear();
		installAction = new InstallAction(globals);
		Messages.getLocalizer().localize(installAction, "adbinstall");
		globals.get(BridgeManager.class).addBridgeListener(installAction);

		deleteAction = new DeleteAction(globals);
		Messages.getLocalizer().localize(deleteAction, "deleteapps");

		exportAction = new ExportAction(globals);
		Messages.getLocalizer().localize(exportAction, "export");

		return list;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		List<AndroidApp> apps = new ArrayList<AndroidApp>();
		for (ListItemBuilder alil : appListItems) {
			if (alil.panel.isVisible() && alil.checked.isSelected()) {
				apps.add(alil.getApp());
			}
		}
		deleteAction.setApps(apps);
		installAction.setApps(apps);
		exportAction.setApps(apps);
	}

	/**
	 * Invert the current selection
	 */
	public void invertSelection() {
		List<AndroidApp> apps = new ArrayList<AndroidApp>();
		for (ListItemBuilder alil : appListItems) {
			if (alil.panel.isVisible()) {
				boolean toggle = !alil.checked.isSelected();
				alil.checked.setSelected(toggle);
				if (toggle) {
					apps.add(alil.getApp());
				}
			}
		}
		deleteAction.setApps(apps);
		installAction.setApps(apps);
		exportAction.setApps(apps);
	}

	/**
	 * Filter the list by name and/or group. This will reset the selection. Apps
	 * that don't apply will be hidden.
	 * 
	 * @param name
	 *          part of the apps name or null or empty to not filter by name
	 * @param group
	 *          group or null to not fitler by group
	 * @return the url under which the list can be viewed in the webbrowser.
	 */
	public URI filter(String name, AppGroup group) {
		String lc = null;
		if (name != null) {
			if (name.length() == 0) {
				lc = null;
			}
			else {
				lc = name.toLowerCase();
			}
		}

		List<AndroidApp> apps = new ArrayList<AndroidApp>();
		List<AndroidApp> appsServe = new ArrayList<AndroidApp>();
		for (ListItemBuilder alil : appListItems) {
			alil.filter(lc, group);
			if (alil.panel.isVisible()) {
				appsServe.add(alil.getApp());
				if (alil.checked.isSelected()) {
					apps.add(alil.getApp());
				}
			}
		}
		deleteAction.setApps(apps);
		installAction.setApps(apps);
		exportAction.setApps(apps);
		list.revalidate();
		ServerManager sm = globals.get(ServerManager.class);
		sm.setAtttribute(Traits.class.getName(), globals.get(Traits.class));
		return sm.serve(appsServe);
	}
}
