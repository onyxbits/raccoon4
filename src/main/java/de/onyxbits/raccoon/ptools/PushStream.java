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
package de.onyxbits.raccoon.ptools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A Stream that can handle Push operations over the bridge
 * 
 * @author patrick
 * 
 */
class PushStream extends OutputStream {

	private OutputStream output;
	private final byte[] DATA = "DATA".getBytes();

	public PushStream(Device device, Socket socket, String remote)
			throws IOException {
		InputStream input = socket.getInputStream();
		output = socket.getOutputStream();

		ProtocolSupport.send("host:transport:" + device.serial, input, output);
		ProtocolSupport.send("sync:", input, output);
		writeBytes("SEND");
		writeInt(Integer.reverseBytes(remote.length()));
		writeBytes(remote);
		output.flush();
	}

	public PushStream(DataOutputStream out) {
		this.output = out;
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		for (int i = 0; i < DATA.length; i++) {
			output.write((byte) DATA[i]);
		}
		writeInt(Integer.reverseBytes(len - off));
		output.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		for (int i = 0; i < DATA.length; i++) {
			output.write((byte) DATA[i]);
		}
		writeInt(Integer.reverseBytes(1));
		output.write(b);
	}

	@Override
	public void close() throws IOException {
		if (output != null) {
			writeBytes("DONE");
			// timestamp
			writeInt(Integer.reverseBytes(0));
			output.flush();
			output.close();
			output = null;
		}
	}

	private final void writeInt(int v) throws IOException {
		output.write((v >>> 24) & 0xFF);
		output.write((v >>> 16) & 0xFF);
		output.write((v >>> 8) & 0xFF);
		output.write((v >>> 0) & 0xFF);
	}

	private final void writeBytes(String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			output.write((byte) s.charAt(i));
		}

	}
}
