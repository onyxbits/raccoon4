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
public class ProfilesMenuBuilder implements ActionListener, PlayProfileListener {

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

		globals.get(DatabaseManager.class).get(PlayProfileDao.class)
				.subscribe(this);
		return menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		DatabaseManager dbm = globals.get(DatabaseManager.class);

		if (src == add) {
			int size = globals.get(DatabaseManager.class).get(PlayProfileDao.class)
					.list().size();
			if (globals.get(Traits.class).isAvailable("4.0.x") || size == 0) {
				new LifecycleManager(new WizardLifecycle(dbm, null)).run();
			}
			else {
				globals.get(LifecycleManager.class).sendBusMessage(new JTextField());
			}
		}

		if (src == edit) {
			String edit = dbm.get(PlayProfileDao.class).get().getAlias();
			new LifecycleManager(new WizardLifecycle(dbm, edit)).run();
		}

		if (src == delete) {
			Window w = globals.get(LifecycleManager.class).getWindow();
			int choice = JOptionPane.showConfirmDialog(w,
					Messages.getString(ID + ".delete.message"),
					Messages.getString(ID + ".delete.title"), JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				PlayProfileDao dao = dbm.get(PlayProfileDao.class);
				dao.delete(dao.get().getAlias());
				List<PlayProfile> ppl = dao.list();
				if (ppl.size() == 0) {
					dao.set(null);
				}
				else {
					dao.set(ppl.get(0).getAlias());
				}
			}
		}
	}

	@Override
	public void onPlayProfileChange(PlayProfileEvent event) {
		if (event.isConnection()) {
			boolean a = event.isActivation();
			edit.setEnabled(a);
			delete.setEnabled(a);
		}
		menu.removeAll();
		PlayProfileDao dao = (PlayProfileDao) event.getSource();
		List<PlayProfile> profiles = dao.list();
		PlayProfile def = dao.get();
		ButtonGroup bg = new ButtonGroup();

		for (PlayProfile profile : profiles) {
			ProfileAction pa = new ProfileAction(globals, profile);
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(pa);
			bg.add(item);
			menu.add(item);
			item.setSelected(def != null && def.getAlias().equals(profile.getAlias()));
		}
		menu.add(new JSeparator());
		menu.add(add);
		menu.add(edit);
		menu.add(delete);
	}
}
