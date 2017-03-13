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
package de.onyxbits.raccoon.gui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import de.onyxbits.raccoon.appmgr.MyAppsViewBuilder;
import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.gplay.ImportBuilder;
import de.onyxbits.raccoon.gplay.ManualDownloadBuilder;
import de.onyxbits.raccoon.gplay.PlayProfileEvent;
import de.onyxbits.raccoon.qr.QrToolBuilder;
import de.onyxbits.raccoon.qr.ShareToolBuilder;
import de.onyxbits.raccoon.transfer.TransferViewBuilder;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.WindowToggleAction;

/**
 * Central place for storing window toggle actions.
 * 
 * @author patrick
 * 
 */
public final class WindowTogglers implements DatasetListener {

	public final WindowToggleAction myApps;
	public final WindowToggleAction transfers;
	public final WindowToggleAction grants;
	public final WindowToggleAction marketimport;
	public final WindowToggleAction manualdownload;
	public final WindowToggleAction qrtool;
	public final WindowToggleAction share;

	public WindowTogglers(LifecycleManager lm) {
		// These sit on the menubar, so let the menubarbuilder handle localization.
		myApps = new WindowToggleAction(lm, MyAppsViewBuilder.ID);
		myApps.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
		transfers = new WindowToggleAction(lm, TransferViewBuilder.ID);
		grants = new WindowToggleAction(lm, GrantBuilder.ID);
		marketimport = new WindowToggleAction(lm, ImportBuilder.ID);
		manualdownload = new WindowToggleAction(lm, ManualDownloadBuilder.ID);
		share = new WindowToggleAction(lm, ShareToolBuilder.ID);
		qrtool = new WindowToggleAction(lm, QrToolBuilder.ID);

		qrtool.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		transfers.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {
		if (event instanceof PlayProfileEvent) {
			PlayProfileEvent ppe = (PlayProfileEvent) event;
			if (ppe.isConnection()) {
				boolean a = ppe.isActivation();
				marketimport.setEnabled(a);
				manualdownload.setEnabled(a);
			}
		}
	}
}
