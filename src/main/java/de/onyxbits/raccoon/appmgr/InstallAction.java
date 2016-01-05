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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTextField;

import de.onyxbits.raccoon.db.AndroidApp;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.ptools.BridgeAction;
import de.onyxbits.raccoon.ptools.InstallWorker;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * An action for installing apps via adb.
 * 
 * @author patrick
 * 
 */
public class InstallAction extends BridgeAction {

	private List<AndroidApp> apps;

	public InstallAction(Globals globals) {
		super(globals);
		apps = Collections.emptyList();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Set the list of apps to install when this action is triggered. The action
	 * is enabled/disabled automatically as appropritate.
	 * 
	 * @param apps
	 */
	public void setApps(List<AndroidApp> apps) {
		if (apps == null) {
			this.apps = Collections.emptyList();
		}
		else {
			this.apps = apps;
		}
		setEnabled(true);
	}

	public void setApps(AndroidApp app) {
		List<AndroidApp> apps = new ArrayList<AndroidApp>(1);
		apps.add(app);
		setApps(apps);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (globals.get(Traits.class).isAvailable("4.0.x")) {
			for (AndroidApp app : apps) {
				globals.get(TransferManager.class).schedule(globals,
						new InstallWorker(globals, app),TransferManager.USB);
			}
		}
		else {
			globals.get(LifecycleManager.class).sendBusMessage(
					new JTextField(Messages.getString(getClass().getSimpleName()
							+ ".about")));
		}
	}

	@Override
	public boolean canEnable() {
		return (apps != null && apps.size() > 0);
	}

}
