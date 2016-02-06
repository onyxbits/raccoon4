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
package de.onyxbits.raccoon.cli;

import de.onyxbits.raccoon.Main;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.gplay.PlayManager;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.GlobalsFactory;
import de.onyxbits.weave.util.Version;

/**
 * A {@link GlobalsFactory} for creating configurable objects in a consistent
 * way.
 * 
 * @author patrick
 * 
 */
public final class GlobalsProvider implements GlobalsFactory {

	/**
	 * System property name
	 */
	public static final String PLAYPROFILESYSPROP = "raccoon.playprofile";

	private static Globals globals;

	private GlobalsProvider() {
	}

	/**
	 * Factory method for getting the single globals instance.
	 * 
	 * @return the globals instance
	 */
	public static Globals getGlobals() {
		if (globals == null) {
			globals = new Globals();
			globals.setFactory(new GlobalsProvider());
		}
		return globals;
	}

	@Override
	public Object onCreate(Globals globals, Class<?> requested) {
		if (requested.equals(Version.class)) {
			try {
				return new Version(Main.class.getPackage().getImplementationVersion());
			}
			catch (Exception e) {
				return new Version(0, 0, 0);
			}
		}

		if (requested.equals(Layout.class)) {
			return Layout.DEFAULT;
		}

		if (requested.equals(DatabaseManager.class)) {
			DatabaseManager ret = new DatabaseManager(Layout.DEFAULT.databaseDir);
			try {
				ret.startup();
				if (!ret.isCompatible(VariableDao.class)) {
					throw new RuntimeException("Version missmatch");
				}
			}
			catch (Exception e) {
				// e.printStackTrace();
				return null;
			}
			return ret;
		}

		if (requested.equals(PlayManager.class)) {
			DatabaseManager dbm = globals.get(DatabaseManager.class);
			if (dbm == null) {
				return null;
			}
			PlayManager ret = new PlayManager(dbm);
			String alias = dbm.get(VariableDao.class).getVar(
					VariableDao.PLAYPASSPORT_ALIAS,
					System.getProperty(PLAYPROFILESYSPROP, dbm.get(VariableDao.class)
							.getVar(VariableDao.PLAYPASSPORT_ALIAS, null)));
			if (alias == null) {
				return null;
			}
			ret.selectProfile(alias);
			return ret;
		}

		// Things with no external dependencies.
		try {
			return requested.newInstance();
		}
		catch (Exception e) {
			// This is a bug!
			e.printStackTrace();
		}
		return null;
	}

}
