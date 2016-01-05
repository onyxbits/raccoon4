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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for bridge communication.
 * 
 * @author patrick
 * 
 */
class ProtocolSupport {

	// Response codes
	protected static final int OKAY = parseInt("OKAY".getBytes(), 0);
	protected static final int FAIL = parseInt("FAIL".getBytes(), 0);
	protected static final int DONE = parseInt("DONE".getBytes(), 0);
	protected static final int DATA = parseInt("DATA".getBytes(), 0);
	protected static final int DENT = parseInt("DENT".getBytes(), 0);
	protected static final int STAT = parseInt("STAT".getBytes(), 0);

	/**
	 * Interpret 4 bytes of a byte array as an integer
	 * 
	 * @param buffer
	 *          the array
	 * @param offset
	 *          the index into the array after which to read 4 bytes.
	 * @return buffer[offset]+3 as an integer.
	 */
	protected static int parseInt(byte[] buffer, int offset) {
		return ((buffer[offset] & 0xFF)) | ((buffer[offset + 1] & 0xFF) << 8)
				| ((buffer[offset + 2] & 0xFF) << 16)
				| (buffer[offset + 3] & 0xFF) << 24;
	}

	/**
	 * Send a command string
	 * 
	 * @param command
	 *          the command to send
	 * @param out
	 *          stream to write to
	 * @param in
	 *          stream to read the response from
	 * @throws IOException
	 *           if the replay is not OKAY.
	 */
	protected static void send(String command, InputStream in, OutputStream out)
			throws IOException {
		out.write(String.format("%04x", command.length()).getBytes());
		out.write(command.getBytes());
		out.flush();
		byte[] buf = new byte[4];
		in.read(buf);
		int response = parseInt(buf, 0);
		if (response != OKAY) {
			in.read(buf);
			buf = new byte[Integer.parseInt(new String(buf, 0, 4), 16)];
			in.read(buf);
			throw new IOException(new String(buf));
		}
	}
}
