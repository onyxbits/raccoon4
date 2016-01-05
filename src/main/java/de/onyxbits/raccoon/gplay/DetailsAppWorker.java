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

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.weave.Globals;

/**
 * Gets the details for an app listing.
 * 
 * @author patrick
 * 
 */
class DetailsAppWorker extends SwingWorker<DocV2, Object> {

	private GooglePlayAPI api;
	private String packId;
	private PlayManager owner;

	public DetailsAppWorker(PlayManager owner, Globals globals, String packId) {
		this.api = globals.get(PlayManager.class).createConnection();
		this.packId = packId;
		this.owner = owner;
	}

	@Override
	protected DocV2 doInBackground() throws Exception {
		return api.details(packId).getDocV2();
	}

	@Override
	protected void done() {
		try {
			owner.fireAppView(get(), false);
		}
		catch (InterruptedException e) {
		}
		catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}
