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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private DataAccessObject[] daos;
	private Stack<Connection> pool;
	private List<EntityListener> listeners;
	private HsqlProperties props;

	/**
	 * Create a new manager
	 * 
	 * @param databaseDir
	 *          directory to keep the files in.
	 */
	public DatabaseManager(File databaseDir) {
		// Register all available DAOS here!
		DataAccessObject[] tmp = { new AndroidAppDao(), new AppGroupDao(),
				new FeedItemDao(), new PlayProfileDao(), new VariableDao(),
				new PlayAppOwnerDao() };
		daos = tmp;

		pool = new Stack<Connection>();
		listeners = new ArrayList<EntityListener>();
		props = new HsqlProperties();
		props.setProperty("connection_type", "file:");
		props.setProperty("database",
				new File(databaseDir, DBNAME).getAbsolutePath());
	}

	/**
	 * Lookup a DAO by class
	 * 
	 * @param daoclass
	 *          class of the database object to get
	 * @return the DAO
	 */
	@SuppressWarnings("unchecked")
	public <T extends DataAccessObject> T get(Class<T> daoclass) {
		for (DataAccessObject dao : daos) {
			if (daoclass.equals(dao.getClass())) {
				return (T) dao;
			}
		}
		// We should not be able to get here (assuming all daos are registered).
		return null;
	}

	/**
	 * Initialize the database (create/update if necessary).
	 * 
	 * @throws SQLException
	 */
	public void startup() throws Exception {
		// Speedhack: The pool is empty, don't bother going through connect().
		Connection c = new JDBCConnection(props);
		Statement st = null;
		ResultSet res = null;
		HashMap<String, Integer> daover = new HashMap<String, Integer>();
		try {
			// Load the DAO version table
			st = c.createStatement();
			st.execute("CREATE TABLE IF NOT EXISTS versions (dao VARCHAR(255), version INT)");
			st.close();

			st = c.createStatement();
			st.execute("SELECT dao, version FROM versions");
			res = st.getResultSet();
			while (res.next()) {
				daover.put(res.getString(1), res.getInt(2));
			}
			st.close();
			// Check if any table needs an update.
			c.setAutoCommit(false);

			for (DataAccessObject dao : daos) {
				dao.setOwner(this);
				int codeVer = dao.getVersion();
				int dbVer = 0; // 0: Table not yet created
				Integer tmp = daover.get(dao.getClass().getSimpleName());
				if (tmp != null) {
					dbVer = tmp.intValue();
				}
				if (codeVer > dbVer) {
					dao.upgradeFrom(dbVer, c);
					versionTo(codeVer, dao.getClass().getSimpleName(), c);
				}
			}

			c.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			c.rollback();
			throw e;
		}
		finally {
			if (res != null) {
				res.close();
			}
			if (st != null) {
				st.close();
			}
			disconnect(c);
		}
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
			st = c.prepareStatement("DELETE FROM versions WHERE dao = ?");
			st.setString(1, dao);
			st.close();
			st = c
					.prepareStatement("INSERT INTO versions (dao,version) VALUES (? , ?)");
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
	protected Connection connect() {
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
	protected void disconnect(Connection c) {
		try {
			if (c.getAutoCommit() == false) {
				// Getting here is actually a bug.
				c.rollback();
				c.setAutoCommit(true);
			}
			pool.push(c);
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

	public void addEntityListener(EntityListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeEntityListener(EntityListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all listeners that a table has changed and they should reload all
	 * entity instances they might be holding. This method must be called on the
	 * EDT.
	 * 
	 * @param entities
	 *          the entities that should be reloaded.
	 */
	public void fireEntityInvalidated(Class<?>... entities) {
		for (EntityListener listener : listeners) {
			listener.onInvalidated(entities);
		}
	}
}
