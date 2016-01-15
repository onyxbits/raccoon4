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
package de.onyxbits.raccoon.rss;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;


public class FeedWorker extends SwingWorker<List<FeedItem>, Integer> {

	private URL source;
	private SyndicationBuilder owner;

	public FeedWorker(SyndicationBuilder owner, URL source) {
		this.source = source;
		this.owner = owner;
	}

	@Override
	protected List<FeedItem> doInBackground() throws Exception {
		return new Parser(source).readFeed();
	}

	@Override
	protected void done() {
		try {
			owner.onResult(get());
		}
		catch (InterruptedException e) {
		}
		catch (ExecutionException e) {
		}
	}

}
