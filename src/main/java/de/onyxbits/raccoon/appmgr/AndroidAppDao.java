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
package de.onyxbits.raccoon.appmgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.onyxbits.raccoon.db.DataAccessObject;
import de.onyxbits.raccoon.db.DatasetEvent;

import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;

public final class AndroidAppDao extends DataAccessObject {

	/**
	 * Table version
	 */
	protected static final int VERSION = 1;

	@Override
	protected void upgradeFrom(int oldVersion, Connection c) throws SQLException {
		switch (oldVersion + 1) {
			case 1: {
				DaoSupport.v1Shared(c);
			}
		}
	}

	@Override
	protected int getVersion() {
		return 1;
	}

	/**
	 * Save an app into the database. If the app is already registered (by name
	 * and versioncode, not app id), it will be deleted first.
	 * 
	 * @param app
	 *          the app object
	 * @return the submitted app with its auto assigned ID.
	 * @throws SQLException
	 */
	public AndroidApp saveOrUpdate(AndroidApp app) throws SQLException {
		PreparedStatement st = null;
		Connection c = manager.connect();
		ResultSet res = null;

		try {
			c.setAutoCommit(false);
			st = c
					.prepareStatement("DELETE FROM androidapps WHERE packagename = ? and versioncode = ?");
			st.setString(1, app.getPackageName());
			st.setInt(2, app.getVersionCode());
			st.execute();
			st.close();

			st = c
					.prepareStatement(
							"INSERT INTO androidapps ( aid, packagename, versioncode, mainversion, patchversion, name, version, minsdk) VALUES ( DEFAULT, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);

			st.setString(1, app.getPackageName());
			st.setInt(2, app.getVersionCode());
			st.setInt(3, app.getMainVersion());
			st.setInt(4, app.getPatchVersion());
			st.setString(5, app.getName());
			st.setString(6, app.getVersion());
			st.setInt(7, app.getMinSdk());
			st.executeUpdate();

			res = st.getGeneratedKeys();
			res.next();
			app.setAppId(res.getInt(1));
			st.close();

			if (app.getUsesPermissions() != null) {
				st = c
						.prepareStatement("INSERT INTO permissions (pid, name) VALUES (?, ?)");
				for (String p : app.getUsesPermissions()) {
					st.setLong(1, app.getAppId());
					st.setString(2, p);
					st.execute();
				}
				st.close();
			}

			if (app.getGroups() != null) {
				st = c
						.prepareStatement("INSERT INTO androidapps_appgroups (aid, gid) VALUES (?, ?)");
				for (AppGroup g : app.getGroups()) {
					st.setLong(1, app.getAppId());
					st.setLong(2, g.getGroupId());
					st.execute();
				}
			}
			c.commit();
			fireOnDataSetChangeEvent(new DatasetEvent(this, DatasetEvent.CREATION
					| DatasetEvent.MODIFICATION));
		}
		catch (SQLException e) {
			c.rollback();
			throw e;
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

		return app;
	}

	public void delete(AndroidApp... apps) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;

		try {
			st = c.prepareStatement("DELETE FROM androidapps WHERE aid = ?");
			c.setAutoCommit(false);
			for (AndroidApp app : apps) {
				st.setLong(1, app.getAppId());
				st.execute();
			}
			c.commit();
			fireOnDataSetChangeEvent(new DatasetEvent(this, DatasetEvent.DELETION));
		}
		catch (SQLException e) {
			c.rollback();
			throw e;
		}
		finally {
			if (st != null)
				st.close();
			manager.disconnect(c);
		}
	}

	/**
	 * Check if an app is already stored in the database
	 * 
	 * @param app
	 *          the app to check
	 * @return true if an app by that packagename and versioncode is already
	 *         stored.
	 * @throws SQLException
	 */
	public boolean isStored(AndroidApp app) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;

		try {
			st = c
					.prepareStatement("SELECT aid FROM androidapps WHERE packagename = ? AND versioncode = ?");
			st.setString(1, app.getPackageName());
			st.setInt(2, app.getVersionCode());
			return st.execute();
		}
		finally {
			if (st != null)
				st.close();
			manager.disconnect(c);
		}
	}

	/**
	 * List apps that fit a criteria
	 * 
	 * @param name
	 *          name criteria
	 * @param group
	 *          group criteria
	 * @return A lazy loaded list of matching apps. Collections are not filled in
	 *         and must be queried using details().
	 * @throws SQLException
	 */
	public List<AndroidApp> list() throws SQLException {
		List<AndroidApp> ret = new ArrayList<AndroidApp>(100);
		Connection c = manager.connect();
		ResultSet res = null;
		PreparedStatement st = null;

		try {
			st = c
					.prepareStatement("SELECT aid, packagename, versioncode, mainversion, patchversion, name, version, minsdk FROM androidapps ORDER BY name, versioncode");
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

	/**
	 * Fill in the details of an app (anything that is pulled in from secondary
	 * tables).
	 * 
	 * @param app
	 *          the app to complete
	 * @return the submitted app with all the info filled in.
	 */
	public AndroidApp details(AndroidApp app) throws SQLException {
		Connection c = manager.connect();
		ResultSet res = null;
		PreparedStatement st = null;
		List<String> perms = new ArrayList<String>(50);

		try {
			st = c.prepareStatement("SELECT name FROM permissions WHERE pid = ?");
			st.setLong(1, app.getAppId());
			res = st.executeQuery();
			while (res.next()) {
				perms.add(res.getString(1));
			}
			st.close();
			res.close();

			st = c
					.prepareStatement("SELECT gid,name FROM androidapps_appgroups JOIN appgroups ON (appgroups.gid=androidapps_appgroups.gid) WHERE aid = ? ");
			st.setLong(1, app.getAppId());
			res = st.executeQuery();
			ArrayList<AppGroup> lst = new ArrayList<AppGroup>();
			while (res.next()) {
				AppGroup ag = new AppGroup();
				ag.setGroupId(res.getLong(1));
				ag.setName(res.getString(2));
				lst.add(ag);
			}
			app.setGroups(lst);
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
		app.setUsesPermissions(perms);
		return app;
	}

	/**
	 * Create an App instance by analyzing an APK file.
	 * 
	 * @param apk
	 *          installer file
	 * @return either an App object or null if the file cannot be parsed for any
	 *         reason.
	 */
	public static AndroidApp analyze(File apk) {
		ApkParser apkParser = null;
		FileInputStream fis = null;
		ApkMeta meta = null;
		try {
			apkParser = new ApkParser(apk);
			apkParser.setPreferredLocale(Locale.getDefault());
			meta = apkParser.getApkMeta();
			apkParser.close();
			fis = new FileInputStream(apk);
			fis.close();
		}
		catch (Exception e) {
			// Lets try to stay compatible with Java6 for now.
			try {
				apkParser.close();
				fis.close();
			}
			catch (IOException e1) {
			}
			return null;
		}

		AndroidApp ret = new AndroidApp();
		ret.setPackageName(meta.getPackageName());
		ret.setName(meta.getLabel());
		ret.setVersionCode(meta.getVersionCode().intValue());
		ret.setVersion(meta.getVersionName());
		ret.setUsesPermissions(meta.getUsesPermissions());
		try {
			ret.setMinSdk(Integer.parseInt(meta.getMinSdkVersion()));
		}
		catch (NumberFormatException e) {
			// Not that important that we would make a fuss about it.
		}
		return ret;
	}
}
