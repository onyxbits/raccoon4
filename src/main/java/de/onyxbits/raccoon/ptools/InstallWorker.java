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
package de.onyxbits.raccoon.ptools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.onyxbits.raccoon.db.AndroidApp;
import de.onyxbits.raccoon.transfer.TransferPeerBuilder;
import de.onyxbits.raccoon.transfer.TransferWorker;
import de.onyxbits.raccoon.vfs.AppExpansionMainNode;
import de.onyxbits.raccoon.vfs.AppExpansionPatchNode;
import de.onyxbits.raccoon.vfs.AppIconNode;
import de.onyxbits.raccoon.vfs.AppInstallerNode;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A worker for pushing an app to the device and calling the packagemanager
 * there.
 * 
 * @author patrick
 * 
 */
public class InstallWorker implements TransferWorker, ActionListener {

	private static final String ID = InstallWorker.class.getSimpleName();
	private static final String OBB = "Android/obb/";

	private AndroidApp app;
	private Globals globals;
	private TransferPeerBuilder control;
	private long totalBytes;
	private long bytesReceived;
	private InputStream input;
	private OutputStream output;
	private int fileCounter;
	private Device device;

	private File inst;

	private File main;

	private File patch;

	public InstallWorker(Globals globals, AndroidApp app) {
		this.globals = globals;
		this.device = globals.get(BridgeManager.class).getActiveDevice();
		this.app = app;
		control = new TransferPeerBuilder(app.getName()).withChannel(Messages
				.getString(ID + ".channel"));
		AppIconNode ain = new AppIconNode(globals.get(Layout.class),
				app.getPackageName(), app.getVersionCode());
		globals.get(ImageLoaderService.class).request(control,
				ain.resolve().toURI().toString());
	}

	@Override
	public TransferPeerBuilder getPeer() {
		return control.withViewAction(this);
	}

	@Override
	public InputStream onNextSource() throws Exception {
		if (input != null) {
			input.close();
			output.flush();
			output.close();
			input = null;
			output = null;
		}
		if (fileCounter == 0) {
			input = new FileInputStream(inst);
			String remote = device.getUserTempDir() + inst.getName();
			output = device.createPushStream(remote, 0644);
		}
		if (fileCounter == 1 && main.exists()) {
			String dir = device.getExternalStorageDir() + OBB + app.getPackageName()
					+ "/";
			device.exec("mkdir -p " + dir);
			input = new FileInputStream(main);
			output = device.createPushStream(dir + main.getName(), 0644);
		}
		if (fileCounter == 2 && patch.exists()) {
			String dir = device.getExternalStorageDir() + OBB + app.getPackageName()
					+ "/";
			device.exec("mkdir -p " + dir);
			input = new FileInputStream(patch);
			output = device.createPushStream(dir + patch.getName(), 0644);
		}
		fileCounter++;
		return input;
	}

	@Override
	public OutputStream onNextDestination() throws Exception {
		return output;
	}

	@Override
	public float onChunk(int size) {
		bytesReceived += size;
		return (float) bytesReceived / (float) totalBytes;
	}

	@Override
	public void onPrepare() throws Exception {
		Layout layout = globals.get(Layout.class);
		String pack = app.getPackageName();
		AppInstallerNode ain = new AppInstallerNode(layout, pack,
				app.getVersionCode());
		AppExpansionMainNode amn = new AppExpansionMainNode(layout, pack,
				app.getMainVersion());
		AppExpansionPatchNode apn = new AppExpansionPatchNode(layout, pack,
				app.getPatchVersion());
		inst = ain.resolve();
		main = amn.resolve();
		patch = apn.resolve();
		totalBytes = inst.length();

		if (main.exists()) {
			totalBytes += main.length();
		}
		if (patch.exists()) {
			totalBytes += patch.length();
		}

	}

	@Override
	public void onComplete() throws Exception {
		try {
			// ADB may not have written all the bytes yet -> Wait till the filesizes
			// match before continuing.
			int count = 0;
			while (count < 20) {
				count++;
				Thread.sleep(100);
				RemoteFile rf = device.stat(device.getUserTempDir() + inst.getName());
				if (rf.getSize() == inst.length()) {
					break;
				}
			}
		}
		catch (Exception e) {
		}
		device.exec("pm install -rtd " + device.getUserTempDir() + inst.getName());
		cleanUp();
	}

	@Override
	public void onIncomplete(Exception e) {
		cleanUp();
	}

	private void cleanUp() {
		try {
			device.exec("rm " + device.getUserTempDir() + inst.getName());
		}
		catch (IOException e) {
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {

			device.exec("monkey -p " + app.getPackageName()
					+ " -c android.intent.category.LAUNCHER 1");
		}
		catch (Exception exp) {
			exp.printStackTrace();
		}

	}
}
