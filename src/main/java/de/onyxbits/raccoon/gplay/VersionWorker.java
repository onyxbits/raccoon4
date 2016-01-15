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
package de.onyxbits.raccoon.gplay;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.io.IOUtils;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.weave.util.Version;

class VersionWorker extends SwingWorker<Version, Integer> {

	private OverviewBuilder owner;

	public VersionWorker(OverviewBuilder owner) {
		this.owner = owner;
	}

	@Override
	protected Version doInBackground() throws Exception {
		InputStream in = null;
		try {
			in = Bookmarks.LATEST.toURL().openStream();
			return new Version(IOUtils.toString(in, "UTF-8").trim());
		}
		catch (Exception e) {
			// Not important enough to make a fuss about.
			// e.printStackTrace();
		}

		try {
			in.close();
		}
		catch (Exception e) {

		}
		return new Version("0.0.0");
	}

	@Override
	protected void done() {
		try {
			owner.onVersion(get());
		}
		catch (InterruptedException e) {
		}
		catch (ExecutionException e) {
		}
	}

}
