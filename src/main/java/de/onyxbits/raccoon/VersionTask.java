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
package de.onyxbits.raccoon;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.util.Version;

/**
 * Checks if there is a new version available
 * 
 * @author patrick
 * 
 */
public class VersionTask {

	private LifecycleManager lifecycleManager;

	public VersionTask(LifecycleManager lm) {
		this.lifecycleManager = lm;
	}

	public void run() {
		InputStream in = null;
		try {
			in = Bookmarks.LATEST.toURL().openStream();
			Version latest = new Version(IOUtils.toString(in, "UTF-8").trim());
			lifecycleManager.sendBusMessage(new VersionMessage(latest));
		}
		catch (Exception e) {
			// Not important enough to make a fuss about.
			//e.printStackTrace();
		}

		try {
			in.close();
		}
		catch (Exception e) {

		}
	}
}
