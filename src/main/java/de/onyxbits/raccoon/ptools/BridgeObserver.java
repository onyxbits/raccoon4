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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Stack;

/**
 * Backend class for polling the socket and changing the active device on the
 * manager as devices get plugged in/removed.
 * 
 * @author patrick
 * 
 */
class BridgeObserver implements Runnable {

	private BridgeManager owner;
	private Socket socket;
	private boolean running;

	protected boolean ready;

	public BridgeObserver(BridgeManager owner) {
		this.owner = owner;
	}

	/**
	 * Shutdown
	 */
	public void kill() {
		try {
			socket.close();
		}
		catch (Exception e) {
		}
	}

	@Override
	public void run() {
		try {
			running = true;
			socket = owner.createSocket();
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			ProtocolSupport.send("host:track-devices", in, out);
			ready = true;
			synchronized (this) {
				notifyAll();
			}
			while (true) {
				owner.setActiveDevice(pollActive(in, out));
			}
		}
		catch (Exception e) {
			ready = true;
			running = false;
			// Probably kill()
			// e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return running;
	}

	private Device pollActive(InputStream in, OutputStream out)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		Stack<Device> devs = new Stack<Device>();

		byte[] buf = new byte[4];
		in.read(buf);
		int len = Integer.parseInt(new String(buf, 0, 4), 16);
		if (len == 0) {
			// All devices got unplugged
			return null;
		}

		for (int i = 0; i < len; i++) {
			int a = in.read();
			switch (a) {
				case '\t': {
					devs.push(new Device(sb.toString()));
					break;
				}
				case '\n': {
					sb.setLength(0);
					break;
				}
				default: {
					sb.append((char) a);
				}
			}
		}
		return devs.pop();
	}
}
