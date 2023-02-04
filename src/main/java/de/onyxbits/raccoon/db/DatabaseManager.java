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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Stack;

import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.persist.HsqlProperties;

/**
 * Takes care of setting up a database connection and coordinates Java/SQL
 * bridging via DAOs.
 * 
 * @author patrick
 * 
 */
public final class DatabaseManager {

	/**
	 * Filename for the database
	 */
	public static final String DBNAME = "raccoondb_4";

	private Stack<Connection> pool;
	private HsqlProperties props;
	private HashMap<Class<?>, Object> daos;
	private HashMap<String, Integer> daoversions;

	/**
	 * Create a new manager.
	 * 
	 * @param databaseDir
	 *          directory to keep the files in.
	 * @throws SQLException
	 *           if the DAO table cannot be initialized.
	 */
	public DatabaseManager(File databaseDir) throws SQLException {
		pool = new Stack<Connection>();
		props = new HsqlProperties();
		props.setProperty("connection_type", "file:");
		props.setProperty("database",
				new File(databaseDir, DBNAME).getAbsolutePath());
		daos = new HashMap<Class<?>, Object>();
		daoversions = new HashMap<String, Integer>();

		Connection c = new JDBCConnection(props);
		Statement st = null;
		ResultSet res = null;
		try {
			// Load the DAO version table
			st = c.createStatement();
			st.execute("CREATE TABLE IF NOT EXISTS versions (dao VARCHAR(255), version INT)");
			st.close();

			st = c.createStatement();
			st.execute("SELECT dao, version FROM versions");
			res = st.getResultSet();
			while (res.next()) {
				daoversions.put(res.getString(1), res.getInt(2));
			}
		}
		finally {
			if (res != null) {
				res.close();
			}
			if (st != null) {
				st.close();
			}
			pool.push(c);
		}
	}

	/**
	 * Lookup a DAO by class. Tables are automatically created/updated if
	 * necessary. Trying to access a table from a database file that is newer than
	 * it's dao will result in an {@link IllegalStateException}.
	 * 
	 * @param daoclass
	 *          class of the database object to get
	 * @return the DAO
	 */
	@SuppressWarnings("unchecked")
	public <T extends DataAccessObject> T get(Class<T> daoclass) {
		T ret = (T) daos.get(daoclass);
		if (ret == null) {
			try {
				ret = daoclass.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}

			// Make sure DAO and TABLE versions match!
			int codeVer = ret.getVersion();
			int dbVer = 0; // 0: Table not yet created
			Integer tmp = daoversions.get(daoclass.getSimpleName());
			if (tmp != null) {
				dbVer = tmp.intValue();
			}
			if (codeVer > dbVer) {
				Connection c = null;
				try {
					c = connect();
					c.setAutoCommit(false);
					ret.upgradeFrom(dbVer, c);
					versionTo(codeVer, daoclass.getSimpleName(), c);
					c.commit();
				}
				catch (SQLException e) {
					try {
						c.rollback();
					}
					catch (SQLException e1) {
						throw new RuntimeException(e1);
					}
				}
				finally {
					if (c != null) {
						try {
							c.setAutoCommit(true);
						}
						catch (SQLException e) {
							throw new RuntimeException(e);
						}
						disconnect(c);
					}
				}
			}
			if (dbVer > codeVer) {
				throw new IllegalStateException("Database version conflict!");
			}

			ret.setManager(this);
			daos.put(daoclass, ret);
		}
		return ret;
	}

	/**
	 * Set the version of a DAO in the version table
	 * 
	 * @param version
	 * @param dao
	 * @param c
	 * @throws SQLException
	 */
	private static void versionTo(int version, String dao, Connection c)
			throws SQLException {
		PreparedStatement st = null;
		try {
			st = c.prepareStatement("MERGE INTO versions USING (VALUES (?, ?)) AS vals(x,y) ON versions.dao = vals.x WHEN MATCHED THEN UPDATE SET versions.version=vals.y WHEN NOT MATCHED THEN INSERT VALUES vals.x , vals.y");
			st.setString(1, dao);
			st.setInt(2, version);
			st.execute();
		}
		finally {
			if (st != null) {
				st.close();
			}
		}
	}

	/**
	 * Release resources
	 * 
	 * @throws SQLException
	 */
	public void shutdown() {
		try {
			Connection c = connect();
			Statement st = c.createStatement();
			st.execute("SHUTDOWN");
			st.close();
			c.close();
			while (!pool.isEmpty()) {
				pool.pop().close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called by DAOs to get a connection object
	 * 
	 * @return a connection connection from the pool. The connection should be
	 *         returned using disconnect() after doing it's unit of work.
	 */
	public Connection connect() {
		if (pool.isEmpty()) {
			try {
				// Speedhack: The proper way of creating a connection is by looking it
				// up via JDBC url. However, that's time consuming and we need the
				// connection ASAP.
				return new JDBCConnection(props);
			}
			catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		return pool.pop();
	}

	/**
	 * Called by DAOs to return a connection after they have finished with it.
	 * 
	 * @param c
	 *          connection to return to the pool (do not close it).
	 */
	public void disconnect(Connection c) {
		try {
			if (!c.getAutoCommit()) {
				throw new RuntimeException(
						"Connection must be set to autocommit before returning it to the pool!");
			}
			else {
				pool.push(c);
			}
			return;
		}
		catch (SQLException e) {
			// The connection is beyond repair. Just abandon it.
		}

		try {
			c.close();
		}
		catch (SQLException e) {
			// Don't care.
		}
	}
}
