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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.onyxbits.raccoon.db.DataAccessObject;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.db.Variables;

public class PlayProfileDao extends DataAccessObject implements Variables {

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

	private static void v1(Connection c) throws SQLException {
		Statement st = c.createStatement();
		st.execute("CREATE TABLE playprofiles (alias VARCHAR(255) PRIMARY KEY, user VARCHAR(255), token VARCHAR(2048), agent VARCHAR(1024), proxyaddress VARCHAR(255), proxyport INT, proxyuser VARCHAR(255), proxypass VARCHAR(255), gsfid VARCHAR(255) )");
		st.close();
	}

	/**
	 * Get a Profile by alias
	 * 
	 * @param alias
	 *          the alias name of the profile
	 * @return the profile or null if none exists by the given alias.
	 */
	public PlayProfile get(String alias) {
		List<PlayProfile> profiles = list();
		for (PlayProfile profile : profiles) {
			if (profile.getAlias().equals(alias)) {
				return profile;
			}
		}
		return null;
	}

	/**
	 * Get the default profile
	 * 
	 * @return the profile that should be used by default or null if none is
	 *         configured.
	 */
	public PlayProfile get() {
		return get(manager.get(VariableDao.class).getVar(PLAYPROFILE, null));
	}

	/**
	 * Make a profile the default one
	 * 
	 * @param alias
	 *          identifier of the profile. If null or no profile by that alias
	 *          exists, the default is set to null.
	 */
	public void set(String alias) {
		PlayProfile pp = get(alias);
		if (pp != null) {
			manager.get(VariableDao.class).setVar(PLAYPROFILE, alias);
		}
		else {
			manager.get(VariableDao.class).setVar(PLAYPROFILE, null);
		}
		fireOnDataSetChangeEvent(new PlayProfileEvent(this,
				PlayProfileEvent.ACTIVATED, pp));
	}

	/**
	 * List available profiles
	 * 
	 * @return profiles listed alphabetically by alias.
	 */
	public List<PlayProfile> list() {
		ArrayList<PlayProfile> ret = new ArrayList<PlayProfile>();

		Connection c = manager.connect();
		PreparedStatement st = null;
		ResultSet res = null;

		try {
			st = c.prepareStatement("SELECT * FROM playprofiles ORDER BY alias");
			res = st.executeQuery();
			while (res.next()) {
				PlayProfile tmp = new PlayProfile();
				tmp.setAlias(res.getString("alias"));
				tmp.setUser(res.getString("user"));
				tmp.setToken(res.getString("token"));
				tmp.setAgent(res.getString("agent"));
				tmp.setProxyAddress(res.getString("proxyaddress"));
				tmp.setProxyPort(res.getInt("proxyport"));
				tmp.setProxyUser(res.getString("proxyuser"));
				tmp.setProxyPassword(res.getString("proxypass"));
				tmp.setGsfId(res.getString("gsfid"));
				ret.add(tmp);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			manager.disconnect(c);
			try {
				if (res != null) {
					res.close();
				}
				if (st != null) {
					st.close();
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	public void add(PlayProfile profile) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		try {
			st = c
					.prepareStatement("INSERT INTO playprofiles (alias, user, token, agent, proxyaddress, proxyport, proxyuser, proxypass, gsfid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			st.setString(1, profile.getAlias());
			st.setString(2, profile.getUser());
			st.setString(3, profile.getToken());
			st.setString(4, profile.getAgent());
			st.setString(5, profile.getProxyAddress());
			st.setInt(6, profile.getProxyPort());
			st.setString(7, profile.getProxyUser());
			st.setString(8, profile.getProxyPassword());
			st.setString(9, profile.getGsfId());
			st.execute();
			fireOnDataSetChangeEvent(new PlayProfileEvent(this,
					PlayProfileEvent.CREATE, profile));
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
		}
	}

	public void delete(String alias) {
		Connection c = manager.connect();
		PreparedStatement st = null;
		try {
			PlayProfile tmp = get(alias);
			st = c.prepareStatement("DELETE FROM playprofiles WHERE alias = ?");
			st.setString(1, alias);
			st.execute();
			if (tmp != null) {
				fireOnDataSetChangeEvent(new PlayProfileEvent(this,
						PlayProfileEvent.DELETE, tmp));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			manager.disconnect(c);
			try {
				if (st != null) {
					st.close();
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void update(PlayProfile profile) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		try {
			st = c
					.prepareStatement("UPDATE playprofiles SET user = ?, token = ?, agent = ?, proxyaddress = ?, proxyport = ?, proxyuser = ?, proxypass = ?, gsfid = ? WHERE alias = ?");

			st.setString(1, profile.getUser());
			st.setString(2, profile.getToken());
			st.setString(3, profile.getAgent());
			st.setString(4, profile.getProxyAddress());
			st.setInt(5, profile.getProxyPort());
			st.setString(6, profile.getProxyUser());
			st.setString(7, profile.getProxyPassword());
			st.setString(8, profile.getGsfId());
			st.setString(9, profile.getAlias());
			st.execute();
			fireOnDataSetChangeEvent(new PlayProfileEvent(this,
					PlayProfileEvent.UPDATE, profile));
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
		}
	}

	/**
	 * Add a listener to the listener list and immediately send it an activation
	 * event with the default profile.
	 * 
	 * @param listener
	 *          the listener to subscribe.
	 */
	public void subscribe(DatasetListener listener) {
		addDataSetListener(listener);
		fireOnDataSetChangeEvent(new PlayProfileEvent(this,
				PlayProfileEvent.ACTIVATED | PlayProfileEvent.READ, get()));
	}
}
