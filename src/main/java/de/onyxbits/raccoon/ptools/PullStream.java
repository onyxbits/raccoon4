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

/**
 * A Stream for issuing a Pull operation via the bridge
 * 
 * @author patrick
 * 
 */
class PullStream extends InputStream {

	private InputStream input;
	private int left;

	public PullStream(InputStream in) {
		this.input = in;
	}

	@Override
	public int read() throws IOException {
		if (input == null) {
			return -1;
		}

		// Data comes in chunks of up to 65kb. Each chunk has a 8 byte header. The
		// first 4 byte give the type, the second the length. We have to cut these
		// headers out of the stream here.

		if (left == 0) {
			byte[] buf = new byte[8];
			input.read(buf);
			int res = ProtocolSupport.parseInt(buf, 0);
			int len = ProtocolSupport.parseInt(buf, 4);
			if (res == ProtocolSupport.DONE) {
				this.close();
				return -1;
			}
			if (res == ProtocolSupport.FAIL) {
				buf = new byte[len];
				input.read(buf);
				input.close();
				throw new IOException(new String(buf));
			}
			if (res == ProtocolSupport.DATA) {
				left = len;
			}
		}
		left--;
		return input.read();
	}

	@Override
	public void close() throws IOException {
		if (input != null) {
			input.close();
			input = null;
		}
	}

}
