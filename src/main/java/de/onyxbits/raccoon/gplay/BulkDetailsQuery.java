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
package de.onyxbits.raccoon.gplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.cli.GlobalsProvider;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.weave.Globals;

/**
 * Utility for executing a bulk details query and printing the result. The query
 * is performed with the default database, profile and language unless
 * overwritten by -Draccoon.homdir, -Draccoon.playprofile, and -Duser.language
 * <p>
 * The main method either excepts a list of packagenames as input parameters or
 * the path to a textfile containing packagenames (one name per line, empty
 * lines and lines starting with a "#" are ignored). Output is either written to
 * the stdout or to individual textfiles if the packagenames were supplied
 * through a file.
 * 
 * @author patrick
 * 
 */
public class BulkDetailsQuery {

	private static void fail(String reason) {
		System.err.println(reason);
		System.exit(1);
	}
	
	/**
	 * Execute a bulk query, dump the result on the console or into files.
	 * 
	 * @param args
	 *          list of packagenames or a path to a filename containing one
	 *          package per line (empty liens and lines starting with "#" are
	 *          ignored).
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			fail("Either supply a text file with one packagename per line or a list of packagenames as argument(s).");
		}

		Vector<String> tmp = new Vector<String>();
		File input = new File(args[0]);
		File parent = null;
		if (input.isFile() && input.canRead()) {
			parent = input.getParentFile();
			BufferedReader bi = new BufferedReader(new FileReader(input));
			String line = null;
			while ((line = bi.readLine()) != null) {
				if (line.length() > 0 && !line.startsWith("#")) {
					tmp.add(line);
				}
			}
			bi.close();
		}
		else {
			for (String arg : args) {
				tmp.add(arg);
			}
		}

		if (tmp.size() == 0) {
			fail("No package names!");
		}

		Globals globals = GlobalsProvider.getGlobals();
		if (globals.get(DatabaseManager.class) == null) {
			fail("Database in use by another process or incompatible with this version.");
		}

		PlayManager playManager = globals.get(PlayManager.class);
		if (playManager == null) {
			fail("No profile to connect with");
		}

		GooglePlayAPI api = playManager.createConnection();
		BulkDetailsResponse res = api.bulkDetails(tmp);

		if (parent == null) {
			System.out.println(res);
		}
		else {
			for (int i = 0; i < tmp.size(); i++) {
				File output = new File(parent, tmp.get(i));
				FileWriter fw = new FileWriter(output, false);
				fw.write(res.getEntry(i).toString());
				fw.close();
			}
		}
	}
}
