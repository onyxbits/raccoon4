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
package de.onyxbits.raccoon.setup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.gplay.PlayManager;
import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.raccoon.gui.Traits;

/**
 * Upload a device configuration.
 * 
 * @author patrick
 * 
 */
public class UploadLogic extends WizardBuilder {

	private JProgressBar progress;
	private HyperTextPane status;
	private Exception err;

	@Override
	protected JPanel assemble() {

		progress = new JProgressBar();
		status = new HyperTextPane("                             ")
				.withTransparency().withWidth(400);

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 20;
		ret.add(progress, gbc);
		gbc.weighty = 1;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		ret.add(status, gbc);
		return ret;
	}

	@Override
	protected void onActivate() {
		super.onActivate();
		status.setText("");
		progress.setIndeterminate(true);
		doInBackground();
	}

	@Override
	protected void onDoInBackground() {
		PlayProfile pp = globals.get(PlayProfile.class);

		if (pp.getAgent() == null) {
			// TODO: rewrite the GooglePlayAPI. The whole device config stuff is
			// hardcoded and poorly exposed.
			pp.setAgent(new GooglePlayAPI("", "").getUseragent());
		}

		GooglePlayAPI api = PlayManager.createConnection(pp);
		api.setClient(LoginLogic.createLoginClient());
		try {
			if (pp.getGsfId() == null) {
				api.checkin(); // Generates the GSF ID
				api.login();
				api.uploadDeviceConfig();
				pp.setGsfId(api.getAndroidID());
				Thread.sleep(6000);
			}
		}
		catch (Exception e) {
			err = e;
			e.printStackTrace();
		}
	}

	protected void onDone() {
		progress.setIndeterminate(false);
		if (err == null) {
			onNext();
		}
		else {
			status.setText(err.getLocalizedMessage());
			status.withWidth(400);
			previous.setEnabled(true);
		}
	}

	@Override
	protected void onNext() {
		Traits t = new Traits(globals.get(DatabaseManager.class).get(
				VariableDao.class));
		if (t.isAvailable("4.0.x")) {
			show(AliasLogic.class);
		}
		else {
			finish();
		}
	}

	@Override
	protected void onPrevious() {
		show(DeviceLogic.class);
	}

}
