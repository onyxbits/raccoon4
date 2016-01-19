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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Superclass for data access objects. All DAOs are versioned by their simple
 * name, so the simple name must be unique (we use the simple name instead of
 * the FQN because the later would create havoc in case a DAO containing package
 * gets renamed or the DAO is moved to a different package).
 * <p>
 * DAO instances should only be acquired through
 * {@link DatabaseManager#get(Class)}.
 * 
 * @author patrick
 * 
 */
public abstract class DataAccessObject {

	protected DatabaseManager manager;

	/**
	 * Set the owning manager.
	 * 
	 * @param m
	 *          the manager this DAO belongs to
	 * @param c
	 *          the connection to use.
	 */
	public void setManager(DatabaseManager m) {
		this.manager = m;
	}

	/**
	 * Called on startup to upgrade the database tables if needed. The correct way
	 * to implement this method is to switch over tableVersion+1, then fall
	 * through subsequent cases. That is, case 1 should contain the CREATE TABLE
	 * statement, while all following cases perform an ALTER TABLE.
	 * 
	 * @param tableVersion
	 *          the version of the DAO that created the database table. This may
	 *          be zero if the table doesn't exist, yet.
	 * @param c
	 *          the connection (in transaction mode) to use for altering tables.
	 * @throws SQLException
	 */
	protected abstract void upgradeFrom(int tableVersion, Connection c)
			throws SQLException;

	/**
	 * Get the version of the DAO object
	 * 
	 * @return a number >=1.
	 */
	protected abstract int getVersion();
}