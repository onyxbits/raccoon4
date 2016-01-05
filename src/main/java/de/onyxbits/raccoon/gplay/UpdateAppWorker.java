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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.db.AndroidApp;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.PlayAppOwnerDao;
import de.onyxbits.raccoon.db.PlayProfile;
import de.onyxbits.raccoon.db.PlayProfileDao;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Pull app updates from Play. Only apps with ownership are updated.
 * 
 * @author patrick
 * 
 */
public class UpdateAppWorker extends SwingWorker<Integer, Object> {

	private Globals globals;

	public UpdateAppWorker(Globals globals) {
		this.globals = globals;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		int count = 0;
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		List<PlayProfile> profiles = dbm.get(PlayProfileDao.class).list();
		for (PlayProfile profile : profiles) {
			List<AndroidApp> apps = dbm.get(PlayAppOwnerDao.class).list(profile);
			count += update(apps, profile);
		}
		return count;
	}

	private Integer update(List<AndroidApp> apps, PlayProfile profile)
			throws IOException {
		// TODO: filtering for the latest version code should be the job of the
		// database.
		HashMap<String, AndroidApp> tmp = new HashMap<String, AndroidApp>();
		for (AndroidApp app : apps) {
			AndroidApp a = tmp.get(app.getPackageName());
			if (a == null) {
				tmp.put(app.getPackageName(), app);
			}
			else {
				if (app.getVersionCode() > a.getVersionCode()) {
					tmp.put(app.getPackageName(), app);
				}
			}
		}
		apps = new Vector<AndroidApp>(tmp.values());
		// End TODO

		Vector<String> packs = new Vector<String>();
		for (AndroidApp app : apps) {
			packs.add(app.getPackageName());
		}
		GooglePlayAPI service = PlayManager.createConnection(profile);
		BulkDetailsResponse response = service.bulkDetails(packs);

		int number = 0;

		for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
			DocV2 doc = bulkDetailsEntry.getDoc();
			String pn = doc.getBackendDocid();
			int vc = doc.getDetails().getAppDetails().getVersionCode();
			for (AndroidApp app : apps) {
				if (pn.equals(app.getPackageName())) {
					if (vc > app.getVersionCode()) {
						globals.get(TransferManager.class).schedule(globals,
								new AppDownloadWorker(globals, doc), TransferManager.WAN);
						number++;
					}
					break;
				}
			}
		}
		return number;
	}

	@Override
	public void done() {
		Window w = globals.get(LifecycleManager.class).getWindow();
		try {
			if (get() == 0) {
				JOptionPane.showMessageDialog(w,
						Messages.getString(getClass().getSimpleName() + ".uptodate"));
			}
		}
		catch (InterruptedException e) {
		}
		catch (ExecutionException e) {
			JOptionPane.showMessageDialog(w, e.getCause().getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
