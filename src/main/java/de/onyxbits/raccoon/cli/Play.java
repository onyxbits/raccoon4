/*
 * Copyright 2016 Patrick Ahlbrecht
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
package de.onyxbits.raccoon.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.akdeniz.googleplaycrawler.DownloadData;
import com.akdeniz.googleplaycrawler.GooglePlay.AppDetails;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.db.Variables;
import de.onyxbits.raccoon.gplay.PlayAppOwnerDao;
import de.onyxbits.raccoon.gplay.PlayManager;
import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gplay.PlayProfileDao;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.AppExpansionMainNode;
import de.onyxbits.raccoon.repo.AppExpansionPatchNode;
import de.onyxbits.raccoon.repo.AppIconNode;
import de.onyxbits.raccoon.repo.AppInstallerNode;
import de.onyxbits.raccoon.repo.Layout;
import de.onyxbits.weave.Globals;

/**
 * Google Play related functions
 * 
 * @author patrick
 * 
 */
class Play implements Variables {

	/**
	 * System property name
	 */
	public static final String PLAYPROFILESYSPROP = "raccoon.playprofile";

	private static GooglePlayAPI createConnection() {
		return PlayManager.createConnection(getProfile());
	}

	private static DatabaseManager getDatabase() {
		Globals globals = GlobalsProvider.getGlobals();
		return globals.get(DatabaseManager.class);
	}

	private static PlayProfile getProfile() {
		DatabaseManager dbm = getDatabase();
		String alias = System.getProperty(PLAYPROFILESYSPROP,dbm.get(VariableDao.class).getVar(PLAYPROFILE, null));
		PlayProfile ret = dbm.get(PlayProfileDao.class).get(alias);
		if (ret == null) {
			Router.fail("play.profile");
		}
		return ret;
	}

	/**
	 * Perform a details query, print the raw results to stdout
	 * 
	 * @param name
	 *          packagename
	 */
	public static void details(String name) {
		GooglePlayAPI api = createConnection();
		try {
			System.out.println(api.details(name));
		}
		catch (Exception e) {
			Router.fail("play.exception", e.getMessage());
		}
	}

	public static void search(String query) {
		GooglePlayAPI api = createConnection();
		try {
			System.out.println(api.search(query));
		}
		catch (Exception e) {
			Router.fail("play.exception", e.getMessage());
		}
	}

	/**
	 * Perform a details query, print to individual files
	 * 
	 * @param input
	 *          file to read packagenames from
	 */
	public static void details(File input) {
		GooglePlayAPI api = createConnection();
		File parent = input.getParentFile();
		Vector<String> tmp = new Vector<String>();
		readPackages(input, tmp);
		for (String name : tmp) {

			File output = new File(parent, name);
			String res = "";
			try {
				res = api.details(name).toString();
			}
			catch (Exception e) {
				// Not found -> empty file
			}
			try {
				FileWriter fw = new FileWriter(output, false);
				fw.write(res);
				fw.close();
			}
			catch (Exception e) {
				Router.fail("play.exception", e.getMessage());
			}
		}
	}

	/**
	 * Perform a bulk details query, write result to individual files
	 * 
	 * @param input
	 *          file containing a list of packagenames.
	 */
	public static void bulkDetails(File input) {
		Vector<String> tmp = new Vector<String>();
		File parent = input.getParentFile();
		readPackages(input, tmp);

		GooglePlayAPI api = createConnection();
		BulkDetailsResponse res = null;
		try {
			res = api.bulkDetails(tmp);
		}
		catch (IOException e) {
			Router.fail("play.exception", e.getMessage());
		}

		try {
			for (int i = 0; i < tmp.size(); i++) {
				File output = new File(parent, tmp.get(i));
				FileWriter fw = new FileWriter(output, false);
				fw.write(res.getEntry(i).toString());
				fw.close();
			}
		}
		catch (Exception e) {
			Router.fail("play.exception", e.getMessage());
		}
	}

	private static void readPackages(File input, Vector<String> collect) {
		try {
			BufferedReader bi = new BufferedReader(new FileReader(input));
			String line = null;
			while ((line = bi.readLine()) != null) {
				if (line.trim().length() > 0 && !line.startsWith("#")) {
					collect.add(line);
				}
			}
			bi.close();
		}
		catch (Exception e) {
			Router.fail("play.inputfile", input.getAbsolutePath());
		}
	}

	/**
	 * Download an app.
	 * 
	 * @param name
	 *          packagename
	 * @param versionCode
	 *          versioncode (maybe -1 to get the latest one).
	 * @param offerType
	 *          should always be 1.
	 */
	public static void downloadApp(String name, int versionCode, int offerType) {
		GooglePlayAPI api = createConnection();
		DatabaseManager dbm = GlobalsProvider.getGlobals().get(DatabaseManager.class);
		if (versionCode == -1) {
			try {
				DetailsResponse dr = api.details(name);
				versionCode = dr.getDocV2().getDetails().getAppDetails().getVersionCode();
			} catch (IOException e) {
				Router.fail(e.getMessage());
			}
		}

		DownloadData data = null;
		try {
			data = api.purchaseAndDeliver(name, versionCode, offerType);
		} catch (Exception e) {
			e.printStackTrace();
			Router.fail(e.getMessage());
		}

		List<File> allFiles = new ArrayList<File>();
		try {
			File mainApkFile = new AppInstallerNode(Layout.DEFAULT, name, versionCode).resolve();
			downloadFileHelper(data.getMainApk(), mainApkFile, allFiles);
			
			for (DownloadData.AdditionalFile additionalFile : data.getAdditionalFiles()) {
				File destFile = null;
				switch (additionalFile.getIndex()) {
					case DownloadData.AdditionalFile.MAIN:
						destFile = new AppExpansionMainNode(Layout.DEFAULT, name, additionalFile.getVersionCode()).resolve();
						break;
					case DownloadData.AdditionalFile.PATCH:
						destFile = new AppExpansionPatchNode(Layout.DEFAULT, name, additionalFile.getVersionCode()).resolve();
						break;
					default:
						System.err.println("Unsupported additional file index " + additionalFile.getIndex());
				}
				if (destFile != null) {
					downloadFileHelper(additionalFile, destFile, allFiles);
				}
			}
			for (DownloadData.SplitApkFile splitApkFile : data.getSplitApkFiles()) {
				File destFile = new File(mainApkFile.getParentFile(), splitApkFile.getId() + "-" + versionCode + ".apk");
				downloadFileHelper(splitApkFile, destFile, allFiles);
			}
		} catch (Exception e) {
			for (File file : allFiles) {
				file.delete();
			}
			Router.fail(e.getMessage());
		}
	}
	
	private static void downloadFileHelper(DownloadData.AppFile from, File to, List<File> allFiles)  throws IOException, GeneralSecurityException {
		allFiles.add(to);
		to.getParentFile().mkdirs();
		System.out.println(to);
		try (InputStream in = from.openStream(); OutputStream out = new FileOutputStream(to)) {
			byte[] buffer = new byte[8192];
			while (true) {
				int n = in.read(buffer);
				if (n < 0) break;
				System.out.print('#');
				out.write(buffer, 0, n);
			}
			System.out.println();
		}
	}

	public static void updateApps() {
		DatabaseManager dbm = getDatabase();
		PlayAppOwnerDao pad = dbm.get(PlayAppOwnerDao.class);
		List<AndroidApp> apps = pad.list(getProfile());
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
		GooglePlayAPI api = createConnection();
		try {
			BulkDetailsResponse response = api.bulkDetails(pns);
			List<BulkDetailsEntry> bde = response.getEntryList();
			for (BulkDetailsEntry entry : bde) {
				AppDetails ad = entry.getDoc().getDetails().getAppDetails();
				String pn = ad.getPackageName();
				int rvc = ad.getVersionCode();
				if (map.containsKey(pn)) {
					int lvc = map.get(pn).getVersionCode();
					if (lvc < rvc) {
						System.out.println("^\t" + pn + "\t" + lvc + "\t->\t" + rvc);
						downloadApp(pn, rvc, 1);
					}
					else {
						System.out.println("=\t" + pn + "\t" + lvc + "\t->\t" + rvc);
					}
				}
				else {
					System.out.println("?\t" + pn + "\t0\t->\t" + rvc);
				}
			}
		}
		catch (IOException e) {
			Router.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void auth() {
		PlayProfile pp = getProfile();
		GooglePlayAPI api = createConnection();
		try {
			api.login();
			pp.setToken(api.getToken());
			getDatabase().get(PlayProfileDao.class).update(pp);
		}
		catch (Exception e) {
			e.printStackTrace();
			Router.fail("");
		}
	}
}
