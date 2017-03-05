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
import de.onyxbits.raccoon.db.Variables;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.ptools.BridgeManager;
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
final class GlobalsProvider implements GlobalsFactory, Variables {

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

		if (requested.equals(Traits.class)) {
			return new Traits(globals.get(DatabaseManager.class).get(
					VariableDao.class));
		}

		if (requested.equals(BridgeManager.class)) {
			BridgeManager ret = new BridgeManager(Layout.DEFAULT);
			ret.startup();
			if (!ret.isRunning()) {
				Router.fail("ptools.nobridge");
			}
			return ret;
		}

		if (requested.equals(DatabaseManager.class)) {
			try {
				return new DatabaseManager(Layout.DEFAULT.databaseDir);
			}
			catch (Exception e) {
				Router.fail("db.inuse");
				return null;
			}
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
