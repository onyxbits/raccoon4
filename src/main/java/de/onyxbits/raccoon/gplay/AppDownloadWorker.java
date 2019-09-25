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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private List<Transfer> transfers;
	private int nextTransferIndex;
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
		if (nextTransferIndex >= transfers.size()) {
			return null;
		}
		Transfer nextTransfer = transfers.get(nextTransferIndex++);
		nextTransfer.to.getParentFile().mkdirs();
		inputStream = nextTransfer.from.openStream();
		outputStream = new FileOutputStream(nextTransfer.to);
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
		} else {
			// for apps that can be downloaded free of charge.
			data = api.purchaseAndDeliver(packageName, versionCode, offerType);
		}
		data.setCompress(globals.get(Traits.class).isAvailable("4.0.x"));

		this.totalBytes = data.getTotalSize();
		
		transfers = new ArrayList<Transfer>();
		apkFile = new AppInstallerNode(layout, packageName, versionCode).resolve();
		transfers.add(new Transfer(data.getMainApk(), apkFile));
		for (DownloadData.AdditionalFile additionalFile : data.getAdditionalFiles()) {
			File destFile = null;
			switch (additionalFile.getIndex()) {
				case DownloadData.AdditionalFile.MAIN:
					destFile = new AppExpansionMainNode(Layout.DEFAULT, packageName, additionalFile.getVersionCode()).resolve();
					break;
				case DownloadData.AdditionalFile.PATCH:
					destFile = new AppExpansionPatchNode(Layout.DEFAULT, packageName, additionalFile.getVersionCode()).resolve();
					break;
				default:
					System.err.println("Unsupported additional file index " + additionalFile.getIndex());
			}
			if (destFile != null) {
				transfers.add(new Transfer(additionalFile, destFile));
			}
		}
		for (DownloadData.SplitApkFile splitApkFile : data.getSplitApkFiles()) {
			File destFile = new File(apkFile.getParentFile(), splitApkFile.getId() + "-" + versionCode + ".apk");
			transfers.add(new Transfer(splitApkFile, destFile));
		}	
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
		for (DownloadData.AdditionalFile additionalFile : data.getAdditionalFiles()) {
			switch (additionalFile.getIndex()) {
				case DownloadData.AdditionalFile.MAIN:
					download.setMainVersion(additionalFile.getVersionCode());
					break;
				case DownloadData.AdditionalFile.PATCH:
					download.setPatchVersion(additionalFile.getVersionCode());
					break;
			}
		}
		
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		dbm.get(AndroidAppDao.class).saveOrUpdate(download);
		dbm.get(PlayAppOwnerDao.class).own(download, profile);
	}

	@Override
	public void onIncomplete(Exception e) {
		if (iconFile != null) {
			iconFile.delete();
		}
		if (transfers != null) {
			for (Transfer transfer : transfers) {
				transfer.to.delete();
			}
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
	
	private static class Transfer {
		public final DownloadData.AppFile<?> from;
		public final File to;
		
		public Transfer(DownloadData.AppFile<?> from, File to) {
			this.from = from;
			this.to = to;
		}
	}
}
