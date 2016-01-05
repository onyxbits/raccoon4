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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.imageio.ImageIO;

import de.onyxbits.raccoon.gui.TransferableImage;
import de.onyxbits.raccoon.transfer.TransferPeerBuilder;
import de.onyxbits.raccoon.transfer.TransferWorker;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.swing.BrowseAction;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A worker for pulling a screenshot off the device.
 * 
 * @author patrick
 * 
 */
public class ScreenshotWorker implements TransferWorker, ActionListener {

	private static final String ID = ScreenshotWorker.class.getSimpleName();

	private Globals globals;
	private TransferPeerBuilder control;

	private long totalBytes;
	private long bytesReceived;
	private Device device;
	private String fileName;
	private String remote;
	private File local;

	private InputStream inputStream;
	private OutputStream outputStream;

	public ScreenshotWorker(Globals globals) {
		this.globals = globals;
		device = globals.get(BridgeManager.class).getActiveDevice();
		fileName = MessageFormat.format(Messages.getString(ID + ".title"),
				device.getSerial(), System.currentTimeMillis() + "");
		control = new TransferPeerBuilder(fileName).withChannel(Messages
				.getString(ID + ".channel"));
		try {
			remote = device.getUserTempDir() + fileName;
		}
		catch (IOException e) {
		}
		local = new File(globals.get(Layout.class).screenshotDir, fileName);
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
		return null;
	}

	@Override
	public OutputStream onNextDestination() throws Exception {
		return outputStream;
	}

	@Override
	public float onChunk(int size) {
		bytesReceived += size;
		return (float) bytesReceived / (float) totalBytes;
	}

	@Override
	public void onPrepare() throws Exception {
		String res = device.exec("screencap " + remote);
		if (res.length() > 0) {
			// the screencap program doesn't print to stdout, so this is an error
			throw new RuntimeException(res);
		}
		inputStream = device.createPullStream(remote);
		outputStream = new FileOutputStream(local);
	}

	@Override
	public void onComplete() throws Exception {
		globals.get(ImageLoaderService.class).request(control,
				local.toURI().toString());
		cleanUp();
		new TransferableImage().publish(ImageIO.read(local));
	}

	@Override
	public void onIncomplete(Exception e) {
		local.delete();
		cleanUp();
	}

	private void cleanUp() {
		try {
			device.exec("rm " + remote);
		}
		catch (Exception e) {

		}
		try {
			inputStream.close();
			outputStream.close();
		}
		catch (Exception e) {
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		BrowseAction.open(local.toURI());
	}
}
