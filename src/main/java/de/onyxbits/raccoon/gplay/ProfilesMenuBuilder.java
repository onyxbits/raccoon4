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
package de.onyxbits.raccoon.gplay;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.setup.WizardLifecycle;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.NoAction;

/**
 * Creates the Market|Profiles menu. Profiles can be added/removed/changed
 * dynamically.
 * 
 * @author patrick
 * 
 */
public class ProfilesMenuBuilder implements PlayListener, ActionListener {

	public static final String ID = ProfilesMenuBuilder.class.getSimpleName();

	private JMenu menu;
	private Globals globals;
	private JMenuItem add;
	private JMenuItem delete;
	private JMenuItem edit;

	public JMenu assemble(Globals globals) {
		this.globals = globals;
		menu = new JMenu(new NoAction()); // Needs an action to localize
		ActionLocalizer al = Messages.getLocalizer();
		add = new JMenuItem(al.localize("add"));
		edit = new JMenuItem(al.localize("edit"));
		delete = new JMenuItem(al.localize("delete"));
		add.addActionListener(this);
		edit.addActionListener(this);
		delete.addActionListener(this);

		load();
		globals.get(PlayManager.class).addPlayListener(this);
		return menu;
	}

	@Override
	public void onProfileActivated(PlayManager pm) {
		menu.removeAll();
		load();
	}

	private void load() {
		List<PlayProfile> profiles = globals.get(DatabaseManager.class)
				.get(PlayProfileDao.class).list();
		PlayManager pm = globals.get(PlayManager.class);
		ButtonGroup bg = new ButtonGroup();

		for (PlayProfile profile : profiles) {
			ProfileAction pa = new ProfileAction(pm, profile.getAlias());
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(pa);
			bg.add(item);
			menu.add(item);
		}
		menu.add(new JSeparator());
		menu.add(add);
		menu.add(edit);
		menu.add(delete);

		delete.setEnabled(profiles.size() > 1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		PlayManager pm = globals.get(PlayManager.class);

		if (src == add) {
			if (globals.get(Traits.class).isAvailable("4.0.x")) {
				new LifecycleManager(new WizardLifecycle(dbm, pm, null)).run();
			}
			else {
				globals.get(LifecycleManager.class).sendBusMessage(
						new JTextField(Messages.getString(getClass().getSimpleName()
								+ ".about")));
			}
		}

		if (src == edit) {
			new LifecycleManager(new WizardLifecycle(dbm, pm, pm.getActiveProfile()
					.getAlias())).run();
		}

		if (src == delete) {
			Window w = globals.get(LifecycleManager.class).getWindow();
			int choice = JOptionPane.showConfirmDialog(w,
					Messages.getString(ID + ".delete.message"),
					Messages.getString(ID + ".delete.title"), JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				PlayProfileDao dao = dbm.get(PlayProfileDao.class);
				dao.delete(pm.getActiveProfile().getAlias());
				pm.selectProfile(dao.list().get(0).getAlias());
			}
		}
	}

	@Override
	public void onAppSearch() {
	}

	@Override
	public void onAppSearchResult(List<DocV2> apps, boolean append) {
	}

	@Override
	public void onAppView(DocV2 app, boolean brief) {
	}
}
