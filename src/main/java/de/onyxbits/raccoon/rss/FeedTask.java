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
package de.onyxbits.raccoon.rss;

import java.sql.SQLException;
import java.util.List;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.FeedItem;
import de.onyxbits.raccoon.db.FeedItemDao;
import de.onyxbits.weave.LifecycleManager;

/**
 * The background task for fetching feeds.
 * 
 * @author patrick
 * 
 */
public class FeedTask {

	private List<FeedItem> items;
	private DatabaseManager databaseManager;
	private LifecycleManager lifecycleManager;

	public FeedTask(LifecycleManager lm, DatabaseManager dbm) {
		this.lifecycleManager = lm;
		this.databaseManager = dbm;
	}

	public void run() {
		try {
			// update(new Parser(Bookmarks.APPSFEED.toURL()));
			update(new Parser(Bookmarks.NEWSFEED.toURL()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			lifecycleManager.sendBusMessage(this);
		}
		catch (Exception e) {
			// Lifecycle got shut down before we could get the feed -> no biggie.
		}
	}

	private void update(Parser parser) {
		items = parser.readFeed();
		try {
			databaseManager.get(FeedItemDao.class).save(items);
			databaseManager.get(FeedItemDao.class).trim();
			items = null;
		}
		catch (SQLException e) {
			// Likely the Lifecycle was shut down while we were fetching the feed.
			// This is nothing to make a fuss about.
		}
	}

}
