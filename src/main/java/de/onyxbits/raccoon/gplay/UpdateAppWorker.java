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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.AppDetails;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.repo.AndroidApp;
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

		int number = 0;
		List<String> pns = new ArrayList<String>(apps.size());
		HashMap<String, AndroidApp> map = new HashMap<String, AndroidApp>();
		for (AndroidApp app : apps) {
			// Note: this check should not exist.
			if (app.getPackageName() != null && !app.getPackageName().equals("")
					&& !pns.contains(app.getPackageName())) {
				pns.add(app.getPackageName());
				map.put(app.getPackageName(), app);
			}
		}
		GooglePlayAPI api = PlayManager.createConnection(profile);

		BulkDetailsResponse response = api.bulkDetails(pns);
		List<BulkDetailsEntry> bde = response.getEntryList();
		for (BulkDetailsEntry entry : bde) {
			DocV2 doc = entry.getDoc();
			AppDetails ad = entry.getDoc().getDetails().getAppDetails();
			String pn = ad.getPackageName();
			if (map.containsKey(pn)) {
				int lvc = map.get(pn).getVersionCode();
				int rvc = ad.getVersionCode();
				if (lvc < rvc) {
					globals.get(TransferManager.class).schedule(globals,
							new AppDownloadWorker(globals, doc), TransferManager.WAN);
					number++;
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
