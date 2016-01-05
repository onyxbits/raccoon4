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
package de.onyxbits.raccoon.gplay;

import java.io.IOException;
import java.io.InputStream;

/**
 * A dummy stream that just delivers a set amount of zero bytes.
 * 
 * @author patrick
 * 
 */
class DevZeroInputStream extends InputStream {

	private long amount;
	private long count;

	public DevZeroInputStream(long amount) {
		this.amount = amount;
	}

	@Override
	public int read() throws IOException {
		if (count <= amount) {
			count++;
			return 0;
		}
		return -1;
	}

}
