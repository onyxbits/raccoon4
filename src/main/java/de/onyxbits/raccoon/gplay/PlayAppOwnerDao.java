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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.onyxbits.raccoon.db.DataAccessObject;
import de.onyxbits.raccoon.repo.AndroidApp;

public class PlayAppOwnerDao extends DataAccessObject {

	@Override
	protected void upgradeFrom(int tableVersion, Connection c)
			throws SQLException {

		switch (tableVersion + 1) {
			case 1: {
				v1(c);
			}
		}
	}

	private static void v1(Connection c) throws SQLException {
		PreparedStatement st = c
				.prepareStatement("CREATE TABLE playappownership (aid BIGINT FOREIGN KEY REFERENCES androidapps ON DELETE CASCADE, pid VARCHAR(255) FOREIGN KEY REFERENCES playprofiles ON DELETE CASCADE )");
		st.execute();
		st.close();
	}

	@Override
	protected int getVersion() {
		return 1;
	}

	/**
	 * Establish an owned by relationship between app and profile. If the app is
	 * already owned, relink it.
	 * 
	 * @param app
	 * @param profile
	 * @throws SQLException
	 */
	public void own(AndroidApp app, PlayProfile profile) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		ResultSet res = null;
		try {
			c.setAutoCommit(false);
			st = c.prepareStatement("DELETE FROM playappownership WHERE aid = ?");
			st.setLong(1, app.getAppId());
			st.execute();
			st.close();

			st = c
					.prepareStatement("INSERT INTO playappownership (aid, pid) VALUES (?, ?)");
			st.setLong(1, app.getAppId());
			st.setString(2, profile.getAlias());
			st.execute();
			st.close();
			c.commit();
		}

		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
			if (res != null) {
				res.close();
			}
		}
	}

	/**
	 * Destroy an owned by relationship between app and profile
	 * 
	 * @param app
	 * @throws SQLException
	 */
	public void release(AndroidApp app) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		ResultSet res = null;
		try {
			st = c.prepareStatement("DELETE FROM playappownership WHERE aid=?");
			st.setLong(0, app.getAppId());
			st.execute();
			st.close();
		}

		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
			if (res != null) {
				res.close();
			}
		}
	}

	/**
	 * List apps by owner
	 * 
	 * @param profile
	 *          the owning profile
	 * @return the owned apps.
	 * @throws SQLException
	 */
	public List<AndroidApp> list(PlayProfile profile) throws SQLException {
		List<AndroidApp> ret = new ArrayList<AndroidApp>(100);
		Connection c = manager.connect();
		ResultSet res = null;
		PreparedStatement st = null;

		try {
			st = c
					.prepareStatement("SELECT aid, packagename, versioncode, mainversion, patchversion, name, version, minsdk FROM androidapps NATURAL JOIN playappownership WHERE pid = ? ORDER BY name, versioncode");
			st.setString(1, profile.getAlias());
			st.execute();
			res = st.getResultSet();
			while (res.next()) {
				AndroidApp app = new AndroidApp();
				app.setAppId(res.getLong(1));
				app.setPackageName(res.getString(2));
				app.setVersionCode(res.getInt(3));
				app.setMainVersion(res.getInt(4));
				app.setPatchVersion(res.getInt(5));
				app.setName(res.getString(6));
				app.setVersion(res.getString(7));
				app.setMinSdk(res.getInt(8));
				ret.add(app);
			}
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
			if (res != null) {
				res.close();
			}
		}
		return ret;
	}
}
