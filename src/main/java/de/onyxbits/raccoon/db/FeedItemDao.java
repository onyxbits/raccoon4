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
package de.onyxbits.raccoon.db;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

public class FeedItemDao extends DataAccessObject {

	/**
	 * Table version
	 */
	protected static final int VERSION = 1;

	@Override
	protected void upgradeFrom(int oldVersion, Connection c) throws SQLException {
		switch (oldVersion + 1) {
			case 1: {
				v1(c);
			}
		}
	}
	
	@Override
	protected int getVersion() {
		return 1;
	}

	protected static void v1(Connection c) throws SQLException {
		Statement st = c.createStatement();
		st.execute("CREATE TABLE rss (guid VARCHAR(512) PRIMARY KEY, state INT, published DATE, description VARCHAR(2048), link VARCHAR(512), source VARCHAR(512), title VARCHAR(255) )");
		st.close();
	}

	/**
	 * Save a list of feed items
	 * 
	 * @param items
	 *          items to save. If an item by the same GUID already exists, it will
	 *          be replaced.
	 * @throws SQLException
	 */
	public void save(List<FeedItem> items) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		try {
			c.setAutoCommit(false);
			for (FeedItem item : items) {
				st = c.prepareStatement("DELETE FROM rss WHERE guid = ?");
				st.setString(1, item.getGuid());
				st.execute();
				st.close();

				st = c
						.prepareStatement("INSERT INTO rss (guid, published, description, link, source, title) VALUES (?, ?, ?, ?, ?, ?)");
				st.setString(1, chop(item.getGuid(), 512));
				st.setDate(2, item.getPublished());
				st.setString(3, chop(item.getDescription(), 2048));
				st.setString(4, chop(item.getLink(), 512));
				st.setString(5, chop(item.getSource(), 512));
				st.setString(6, chop(item.getTitle(), 255));
				st.execute();
			}
			c.commit();
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
		}
	}

	/**
	 * List feed items by source
	 * 
	 * @param source
	 *          the url to filter by
	 * @return all matching feeditems
	 */
	public List<FeedItem> list(URI source) {
		Vector<FeedItem> ret = new Vector<FeedItem>();
		Connection c = manager.connect();
		ResultSet res = null;
		PreparedStatement st = null;
		try {
			st = c
					.prepareStatement("SELECT * from rss WHERE source = ? ORDER by published");
			st.setString(1, source.toString());
			res = st.executeQuery();
			while (res.next()) {
				FeedItem item = new FeedItem();
				item.setDescription(res.getString("description"));
				item.setGuid(res.getString("guid"));
				item.setLink(res.getString("link"));
				item.setSource(res.getString("source"));
				item.setTitle(res.getString("title"));
				item.setPublished(res.getDate("published"));
				ret.add(item);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			manager.disconnect(c);
			try {
				res.close();
				st.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * Delete old entries.
	 */
	public void trim() {
	}

	private static String chop(String in, int max) {
		int len = Math.min(max, in.length());
		return in.substring(0, len);
	}
}
