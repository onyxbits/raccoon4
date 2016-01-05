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
package de.onyxbits.raccoon.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import de.onyxbits.raccoon.transfer.TransferPeerBuilder;
import de.onyxbits.raccoon.transfer.TransferWorker;

/**
 * A worker for controlling downloads through the {@link TransferWorker}.
 * 
 * @author patrick
 * 
 */
public class NetWorker implements TransferWorker {

	private File source;
	private HttpServletResponse output;
	private TransferPeerBuilder peer;
	private long totalBytes;
	private long bytesReceived;

	protected NetWorker(File input, HttpServletResponse output, String remote) {
		this.source = input;
		this.output = output;
		peer = new TransferPeerBuilder(input.getName()).withChannel(remote);
		totalBytes = input.length();
	}

	@Override
	public TransferPeerBuilder getPeer() {
		return peer;
	}

	@Override
	public InputStream onNextSource() throws Exception {
		if (bytesReceived == 0) {
			return new FileInputStream(source);
		}
		return null;
	}

	protected boolean isPending() {
		return (bytesReceived == 0 || totalBytes != bytesReceived);
	}

	@Override
	public OutputStream onNextDestination() throws Exception {
		return output.getOutputStream();
	}

	@Override
	public float onChunk(int size) {
		bytesReceived += size;
		return (float) bytesReceived / (float) totalBytes;
	}

	@Override
	public void onPrepare() throws Exception {
	}

	@Override
	public void onComplete() throws Exception {
		cleanup();
	}

	@Override
	public void onIncomplete(Exception e) {
		cleanup();
	}

	private void cleanup() {
		synchronized (this) {
			notifyAll();
		}
	}
}
