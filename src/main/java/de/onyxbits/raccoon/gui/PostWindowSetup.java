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
package de.onyxbits.raccoon.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.gplay.PlayProfileDao;
import de.onyxbits.raccoon.setup.WizardLifecycle;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Things that need to be done after the main window shows.
 * 
 * @author patrick
 * 
 */
public class PostWindowSetup extends WindowAdapter {

	private Globals globals;

	public PostWindowSetup(Globals globals) {
		this.globals = globals;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		e.getWindow().removeWindowListener(this);
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		if (dbm.get(PlayProfileDao.class).list().size() == 0) {
			EventQueue.invokeLater(new LifecycleManager(
					new WizardLifecycle(dbm, null)));
		}
	}

}
