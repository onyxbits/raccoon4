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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.SwingUtilities;

import de.onyxbits.raccoon.appmgr.AndroidApp;
import de.onyxbits.raccoon.appmgr.AndroidAppDao;
import de.onyxbits.raccoon.appmgr.DetailsViewBuilder;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.transfer.TransferPeerBuilder;
import de.onyxbits.raccoon.transfer.TransferWorker;
import de.onyxbits.raccoon.vfs.AppInstallerNode;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A worker for importing APK files from the local filesystem. Use this one as a
 * reference when implementing a new worker (unfortunately it is not feasible to
 * provide an abstract superclass with common functionality).
 * <p>
 * Implementation note: It is possible (even likely) that a user imports an
 * already existing app for a number of reasons: carelessness, misleading/wrong
 * filenames, replacing an APK with one from another market.
 * <p>
 * We assume the worst case: The user has a hacked version of an app from a
 * shady source in storage and wants to replace it with a clean one.
 * 
 * 
 * @author patrick
 * 
 */
public class ImportAppWorker implements TransferWorker, Runnable, ActionListener {

	private TransferPeerBuilder control;
	private File source;
	private File dest;
	private File iconDest;
	private Globals globals;
	private long totalBytes;
	private long bytesReceived;
	private AndroidApp transferred;
	private InputStream inputStream;
	private OutputStream outputStream;

	/**
	 * Constructor
	 * 
	 * @param globals
	 *          registry
	 * @param source
	 *          file to copy from.
	 */
	public ImportAppWorker(Globals globals, File source) {
		this.source = source;
		this.globals = globals;
		control = new TransferPeerBuilder(source.getName());
		control.withChannel(Messages.getString(getClass().getSimpleName()
				+ ".channel"));
	}

	@Override
	public TransferPeerBuilder getPeer() {
		return control.withViewAction(this);
	}

	@Override
	public InputStream onNextSource() throws Exception {
		if (bytesReceived == 0) {
			return inputStream;
		}
		else {
			return null;
		}
	}

	@Override
	public OutputStream onNextDestination() throws Exception {
		if (bytesReceived == 0) {
			return outputStream;
		}
		else {
			return null;
		}
	}

	@Override
	public float onChunk(int size) {
		bytesReceived += size;
		return (float) bytesReceived / (float) totalBytes;
	}

	@Override
	public void onComplete() throws Exception {
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		dbm.get(AndroidAppDao.class).saveOrUpdate(transferred);
		SwingUtilities.invokeLater(this);
		closeStreams();
	}

	@Override
	public void onIncomplete(Exception e) {
		dest.delete();
		iconDest.delete();
		closeStreams();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Window w = globals.get(LifecycleManager.class).getWindow(
				DetailsViewBuilder.ID);
		globals.get(DetailsViewBuilder.class).setApp(transferred);
		w.setVisible(true);
	}

	@Override
	public void onPrepare() throws Exception {

		Layout layout = globals.get(Layout.class);

		transferred = AndroidAppDao.analyze(source);
		if (transferred == null) {
			throw new IllegalArgumentException("Not an APK!");
		}

		AppInstallerNode ain = new AppInstallerNode(layout,
				transferred.getPackageName(), transferred.getVersionCode());
		dest = ain.resolve();
		iconDest = ain.toIcon().resolve();
		dest.getParentFile().mkdirs();
		ain.toIcon().extractFrom(source);
		inputStream = new FileInputStream(source);
		outputStream = new FileOutputStream(dest);

		globals.get(ImageLoaderService.class).request(control,
				iconDest.toURI().toString());
		totalBytes = Math.max(1, source.length());
	}

	@Override
	public void run() {
		DatabaseManager dbm = globals.get(DatabaseManager.class);
		dbm.fireEntityInvalidated(AndroidApp.class);
	}

	private void closeStreams() {
		try {
			inputStream.close();
			outputStream.close();
		}
		catch (IOException e) {
		}
	}

}
