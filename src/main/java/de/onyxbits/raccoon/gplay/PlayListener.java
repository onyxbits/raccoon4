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

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

/**
 * Callback interface for the {@link PlayManager}
 * 
 * @author patrick
 * 
 */
public interface PlayListener {

	/**
	 * Called when a search gets triggered.
	 */
	public void onAppSearch();

	/**
	 * Called when new search results arrived
	 * 
	 * @param apps
	 *          list of search results
	 * @param append
	 *          true if this is a follow up search.
	 */
	public void onAppSearchResult(List<DocV2> apps, boolean append);

	/**
	 * Called when app details arrived
	 * 
	 * @param app
	 *          details of the app in question.
	 * @param brief
	 *          true if this is not the detailed doc
	 */
	public void onAppView(DocV2 app, boolean brief);

	/**
	 * Called when another profile becomes the active one.
	 * 
	 * @param playManager
	 *          reference to the manager.
	 */
	public void onProfileActivated(PlayManager playManager);
}
