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

	private int left;
	private int addHeader;
	private DataOutputStream output;

	public PushStream(Device device, Socket socket, String remote)
			throws IOException {
		InputStream input = socket.getInputStream();
		output = new DataOutputStream(socket.getOutputStream());

		ProtocolSupport.send("host:transport:" + device.serial, input, output);
		ProtocolSupport.send("sync:", input, output);
		output.writeBytes("SEND");
		output.writeInt(Integer.reverseBytes(remote.length()));
		output.writeBytes(remote);
		output.flush();
	}

	public PushStream(DataOutputStream out) {
		this.output=out;
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		addHeader = len - off;
		super.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		if (left == 0) {
			// Oh dear. Someone is wasting resources!
			addHeader = 1;
		}
		if (addHeader > 0) {
			output.writeBytes("DATA");
			output.writeInt(Integer.reverseBytes(addHeader));
			left = addHeader;
			addHeader = 0;
		}
		output.write(b);
		left--;
	}

	@Override
	public void close() throws IOException {
		if (output != null) {
			output.writeBytes("DONE");
			// timestamp
			output.writeInt(Integer.reverseBytes(0));
			output.flush();
			output.close();
			output = null;
		}
	}
}
