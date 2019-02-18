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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.akdeniz.googleplaycrawler.DownloadData;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.appmgr.DetailsViewBuilder;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.AppExpansionMainNode;
import de.onyxbits.raccoon.repo.AppExpansionPatchNode;
import de.onyxbits.raccoon.repo.AppIconNode;
import de.onyxbits.raccoon.repo.AppInstallerNode;
import de.onyxbits.raccoon.repo.Layout;
import de.onyxbits.raccoon.transfer.TransferPeerBuilder;
import de.onyxbits.raccoon.transfer.TransferWorker;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A worker for downloading apps from Google Play.
 * 
 * @author patrick
 * 
 */
class AppDownloadWorker implements TransferWorker, ActionListener {

	public static final String ID = AppDownloadWorker.class.getSimpleName();

	private Globals globals;
	private InputStream inputStream;
	private OutputStream outputStream;

	private long totalBytes;
	private long bytesReceived;

	private int versionCode, offerType;
	private String packageName;
	private boolean paid;

	private AndroidApp download;
	private DownloadData data;
	private TransferPeerBuilder control;

	private Layout layout;
	private PlayProfile profile;

	private File apkFile;
	private File iconFile;
	private File mainFile;
	private File patchFile;

	private int fileCount;
	private int splitCount;
	private String appName;

	public AppDownloadWorker(Globals globals, DocV2 doc) {
		this.globals = globals;
		this.totalBytes = doc.getDetails().getAppDetails().getInstallationSize();
		layout = globals.get(Layout.class);
		versionCode = doc.getDetails().getAppDetails().getVersionCode();
		offerType = doc.getOffer(0).getOfferType();
		packageName = doc.getBackendDocid();
		control = new TransferPeerBuilder(doc.getTitle());
		control.withChannel(Messages.getString(ID + ".channel"));
		globals.get(ImageLoaderService.class).request(control,
				DocUtil.getAppIconUrl(doc));
		profile = globals.get(DatabaseManager.class).get(PlayProfileDao.class)
				.get();
		paid = doc.getOffer(0).getCheckoutFlowRequired();
		appName = doc.getTitle();
	}

	@Override
	public float onChunk(int size) {
		bytesReceived += size;
		return (float) bytesReceived / (float) totalBytes;
	}

	@Override
	public TransferPeerBuilder getPeer() {
		return control.withViewAction(this);
	}

	@Override
	public InputStream onNextSource() throws Exception {
		closeStreams();
		if (splitCount > 0) {
			splitCount--;
			outputStream = new FileOutputStream(new File(apkFile.getParentFile(),
					data.getSplitId(splitCount) + "-" + versionCode + ".apk"));
			return data.openSplitDelivery(splitCount);
		}
		switch (fileCount) {
			case 0: {
				outputStream = new FileOutputStream(apkFile);
				inputStream = data.openApp();
				break;
			}
			case 1: {
				if (data.hasMainExpansion()) {
					if (mainFile.exists()) {
						// Looks like we are updating the app and the expansion of the
						// previous
						// version is still valid -> skip the download and make sure the
						// file
						// doesn't get deleted if the user cancels.
						mainFile = null;
						inputStream = new DevZeroInputStream(data.getMainSize());
						outputStream = new DevNullOutputStream();
					}
					else {
						inputStream = data.openMainExpansion();
						outputStream = new FileOutputStream(mainFile);
					}
				}
				break;
			}
			case 2: {
				if (data.hasPatchExpansion()) {
					if (patchFile.exists()) {
						// Looks like we are updating the app and the expansion of the
						// previous
						// version is still valid -> skip the download and make sure the
						// file
						// doesn't get deleted if the user cancels.
						patchFile = null;
						inputStream = new DevZeroInputStream(data.getPatchSize());
						outputStream = new DevNullOutputStream();
					}
					else {
						inputStream = data.openPatchExpansion();
						outputStream = new FileOutputStream(patchFile);
					}
				}
				break;
			}
		}
		fileCount++;
		return inputStream;
	}

	@Override
	public OutputStream onNextDestination() throws Exception {
		return outputStream;
	}

	@Override
	public void onPrepare() throws Exception {
		GooglePlayAPI api = globals.get(PlayManager.class).createConnection();

		if (paid) {
			// For apps that must be purchased before download
			data = api.delivery(packageName, versionCode, offerType);
		}
		else {
			// for apps that can be downloaded free of charge.
			data = api.purchaseAndDeliver(packageName, versionCode, offerType);
		}
		data.setCompress(globals.get(Traits.class).isAvailable("4.0.x"));

		this.totalBytes = data.getTotalSize();
		apkFile = new AppInstallerNode(layout, packageName, versionCode).resolve();
		apkFile.getParentFile().mkdirs();
		iconFile = new AppIconNode(layout, packageName, versionCode).resolve();
		mainFile = new AppExpansionMainNode(layout, packageName,
				data.getMainFileVersion()).resolve();
		patchFile = new AppExpansionPatchNode(layout, packageName,
				data.getPatchFileVersion()).resolve();
		splitCount = data.getSplitCount();
		
	}

	@Override
	public void onComplete() throws Exception {
		AppIconNode ain = new AppIconNode(layout, packageName, versionCode);
		iconFile = ain.resolve();
		try {
			ain.extractFrom(apkFile);
		}
		catch (IOException e) {
			// This is (probably) ok. Not all APKs have icons. Lets try to fall back
			// on what we have.
			try {
				Image img = control.iconImage;
				BufferedImage bimage = new BufferedImage(img.getWidth(null),
						img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				Graphics2D bGr = bimage.createGraphics();
				bGr.drawImage(img, 0, 0, null);
				bGr.dispose();
				ImageIO.write(bimage, "png", new FileOutputStream(ain.resolve()));
			}
			catch (Exception e2) {
				// Nope, can't be helped.
			}
		}
		download = AndroidAppDao.analyze(apkFile);
		if (download.getName().startsWith("@string")) {
			// Great, split APK ...
			download.setName(appName);
		}
		if (data.hasMainExpansion()) {
			download.setMainVersion(data.getMainFileVersion());
		}
		if (data.hasPatchExpansion()) {
			download.setPatchVersion(data.getPatchFileVersion());
		}
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		dbm.get(AndroidAppDao.class).saveOrUpdate(download);
		dbm.get(PlayAppOwnerDao.class).own(download, profile);
	}

	@Override
	public void onIncomplete(Exception e) {
		if (apkFile != null) {
			apkFile.delete();
		}
		if (iconFile != null) {
			iconFile.delete();
		}
		if (mainFile != null) {
			mainFile.delete();
		}
		if (patchFile != null) {
			patchFile.delete();
		}
	}

	private void closeStreams() {
		try {
			inputStream.close();
			inputStream = null;
			outputStream.close();
			outputStream = null;
		}
		catch (Exception e) {
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		java.awt.Window w = globals.get(LifecycleManager.class).getWindow(
				DetailsViewBuilder.ID);
		globals.get(DetailsViewBuilder.class).setApp(download);
		w.setVisible(true);
	}
}
