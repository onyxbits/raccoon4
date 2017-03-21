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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import de.onyxbits.raccoon.repo.Layout;
import de.onyxbits.raccoon.transfer.TransferPeerBuilder;
import de.onyxbits.raccoon.transfer.TransferWorker;
import de.onyxbits.weave.Globals;

/**
 * A worker for fetching a file from an URL.
 * 
 * @author patrick
 * 
 */
public class FetchToolsWorker implements TransferWorker {

	private static final String ID = FetchToolsWorker.class.getSimpleName();

	private Globals globals;
	private TransferPeerBuilder control;
	private File toolFile;
	private File usbFile;

	private long totalBytes;
	private long bytesReceived;
	private InputStream input;
	private OutputStream output;
	private boolean needsDriver;

	public FetchToolsWorker(Globals globals) {
		this.globals = globals;
		if (isWindows()) {
			control = new TransferPeerBuilder(
					Messages.getString(ID + ".title.driver")).withChannel(Messages
					.getString(ID + ".channel"));
			needsDriver = true;
		}
		else {
			control = new TransferPeerBuilder(Messages.getString(ID + ".title"))
					.withChannel(Messages.getString(ID + ".channel"));
		}
	}

	@Override
	public TransferPeerBuilder getPeer() {
		return control;
	}

	@Override
	public InputStream onNextSource() throws Exception {
		if (input != null) {
			input.close();
			output.close();
			input = null;
			output = null;
		}
		if (bytesReceived == 0) {
			input = new URL(getToolsUrl()).openStream();
			output = new FileOutputStream(toolFile);
			return input;
		}
		if (needsDriver) {
			needsDriver = false;
			input = new URL(getDriverUrl()).openStream();
			output = new FileOutputStream(usbFile);
			return input;
		}
		return null;
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
		toolFile = File.createTempFile("adb", "zip");
		usbFile = File.createTempFile("usbdriver", "zip");
		toolFile.deleteOnExit();
		usbFile.deleteOnExit();
	}

	@Override
	public void onComplete() throws Exception {
		Layout layout = globals.get(Layout.class);
		if (isLinux() || isMac()) {
			new ToolSupport(layout.binDir).install(toolFile, null);
		}

		if (isWindows()) {
			new ToolSupport(layout.binDir).install(toolFile, usbFile);
		}
		globals.get(BridgeManager.class).startup();
	}

	@Override
	public void onIncomplete(Exception e) {
	}

	private static boolean isWindows() {
		return System.getProperty("os.name").contains("indows");
	}

	private static boolean isLinux() {
		return System.getProperty("os.name").contains("inux");
	}

	private static boolean isMac() {
		return System.getProperty("os.name").contains("ac OS");
	}

	private String getToolsUrl() {
		// TODO: we should actually parse
		// https://dl.google.com/android/repository/repository-10.xml for these
		// urls.
		if (isLinux()) {
			totalBytes = 2520021;
			return "https://dl.google.com/android/repository/platform-tools_r23.0.1-linux.zip";
		}
		if (isMac()) {
			totalBytes = 2489850;
			return "https://dl.google.com/android/repository/platform-tools_r23.0.1-macosx.zip";
		}
		totalBytes = 2402978 + 8682859;
		return "https://dl.google.com/android/repository/platform-tools_r23.0.1-windows.zip";
	}

	private String getDriverUrl() {
		// TODO: we should actually parse
		// https://dl.google.com/android/repository/addon.xml
		// for this url.
		if (isWindows()) {
			return "https://dl.google.com/android/repository/usb_driver_r11-windows.zip";
		}
		return null;
	}

}
