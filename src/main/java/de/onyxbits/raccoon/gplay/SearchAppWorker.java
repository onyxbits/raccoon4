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
import com.akdeniz.googleplaycrawler.GooglePlay.ListResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

/**
 * Backend for performing app searches on Google Play off the UI thread.
 * 
 * @author patrick
 * 
 */
class SearchAppWorker extends SwingWorker<SearchEngineResultPage, Object> {

	/**
	 * Number of entries to ask for in the result set by default.
	 */
	public static final int DEFAULTLIMIT = 15;

	private GooglePlayAPI api;
	private String query;
	private PlayManager owner;
	private SearchEngineResultPage serp;

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
			SearchEngineResultPage serp) {
		this.api = api;
		this.query = query;
		this.owner = owner;
		this.serp = serp;
	}

	@Override
	protected SearchEngineResultPage doInBackground() throws Exception {
		String tmp = null;
		if (serp != null) {
			tmp = serp.getNextPageUrl();
		}
		serp = new SearchEngineResultPage(SearchEngineResultPage.SEARCH);
		if (query != null) {
			serp.append(api.searchApp(query));
			tmp = serp.getNextPageUrl();
		}
		if (tmp != null && !"".equals(tmp)) {
			serp.append(api.getList(tmp));
		}

		return serp;
	}

	@Override
	public void done() {
		if (!isCancelled()) {
			try {
				owner.fireAppSearchResult(get());
			}
			catch (Exception e) {
				owner.fireUnauthorized();
			}
		}
	}

}
