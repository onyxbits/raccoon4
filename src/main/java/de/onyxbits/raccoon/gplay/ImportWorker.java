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

import java.util.List;

import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.Globals;

/**
 * Do a bulk query for a number of given packages, then schedule their download.
 * 
 * @author patrick
 * 
 */
class ImportWorker extends SwingWorker<List<BulkDetailsEntry>, Object> {

	private List<String> packs;
	private Globals globals;

	public ImportWorker(Globals globals, List<String> packs) {
		this.globals = globals;
		this.packs = packs;
	}

	@Override
	protected List<BulkDetailsEntry> doInBackground() throws Exception {
		GooglePlayAPI service = globals.get(PlayManager.class).createConnection();
		return service.bulkDetails(packs).getEntryList();
	}

	@Override
	public void done() {
		DocV2 doc = null;
		try {
			List<BulkDetailsEntry> entries = get();
			for (BulkDetailsEntry entry : entries) {
				if (!entry.hasDoc()) continue; // Entry does not have doc ignore
				doc = entry.getDoc();
				globals.get(TransferManager.class).schedule(globals,
						new AppDownloadWorker(globals, doc),TransferManager.WAN);
			}
		}
		catch (Exception e) {
			// Silently ignore packages we can't find.
		}
	}

}
