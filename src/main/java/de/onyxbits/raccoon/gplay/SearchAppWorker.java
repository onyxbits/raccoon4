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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

/**
 * Backend for performing app searches on Google Play off the UI thread.
 * 
 * @author patrick
 * 
 */
class SearchAppWorker extends SwingWorker<Object, Object> {

	/**
	 * Number of entries to ask for in the result set by default.
	 */
	public static final int DEFAULTLIMIT = 15;

	private GooglePlayAPI api;
	private int limit;
	private int offset;
	private String query;
	private PlayManager owner;
	private DocV2 bestMatch;
	private List<DocV2> serp;

	/**
	 * 
	 * @param api
	 *          connection object.
	 * @param query
	 *          what to search for
	 * @param offset
	 *          start after offset
	 * @param limit
	 *          number of entries
	 */
	public SearchAppWorker(PlayManager owner, GooglePlayAPI api, String query,
			int offset, int limit) {
		this.api = api;
		this.limit = limit;
		this.offset = offset;
		this.query = query;
		this.owner = owner;
		serp = Collections.emptyList();
	}

	@Override
	protected Object doInBackground() throws Exception {
		SearchResponse res = null;
		try {
			res = api.search(query, offset, limit);
			// System.err.println(res);
		}
		catch (Exception e) {
			owner.login();
			res = owner.createConnection().search(query, offset, limit);
		}

		if (res.getDocCount() > 0) {
			serp = descent(res.getDoc(0));
		}
		if (serp == null) {
			serp = new ArrayList<DocV2>();
		}
		return null;
	}

	private List<DocV2> descent(DocV2 doc) {
		if (doc.getDocType() == 45) {
			return doc.getChildList();
		}
		for (DocV2 d : doc.getChildList()) {
			List<DocV2> tmp = descent(d);
			if (tmp != null) {
				return tmp;
			}
		}
		return null;
	}

	@Override
	public void done() {
		if (!isCancelled()) {
			try {
				get();
				if (bestMatch != null) {
					owner.fireAppView(bestMatch, true);
				}
				owner.fireAppSearchResult(serp);
			}
			catch (Exception e) {
				owner.fireUnauthorized();
			}
		}
	}

}
