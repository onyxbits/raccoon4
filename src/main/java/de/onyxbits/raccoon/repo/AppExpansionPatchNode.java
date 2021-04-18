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
package de.onyxbits.raccoon.repo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Models a patch .OBB file.
 * 
 * @author patrick
 * 
 */
public class AppExpansionPatchNode extends AppNode implements FilenameFilter {

	public AppExpansionPatchNode(Layout layout, String packageName,
			int versionCode) {
		super(layout, packageName, versionCode);
	}

	@Override
	public String getFileName() {
		return "patch." + versionCode + "." + packageName + ".obb";
	}

	@Override
	public boolean accept(File dir, String name) {
		if (name.startsWith("patch.") && name.endsWith(".obb")) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Parse a string into an object
	 * 
	 * @param layout
	 *          layout for resolving
	 * @param fname
	 *          filename (in standard notation)
	 * @return either a node or null if the filename doesn't match the standard
	 *         notation.
	 */
	public static AppExpansionPatchNode parse(Layout layout, String fname) {
		if (fname.startsWith("patch.") && fname.endsWith(".obb")) {
			try {
				String stripped = fname.substring("patch.".length(),
						fname.lastIndexOf('.'));
				String[] tmp = stripped.split("\\.", 2);
				return new AppExpansionPatchNode(layout, tmp[1],
						Integer.parseInt(tmp[0]));
			}
			catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * Find the expansionfile responsible for an installer.
	 * 
	 * @param ain
	 *          descriptor
	 * @return the expansion node or null if no suitable expansion exists.
	 */
	public static AppExpansionPatchNode findFor(AppInstallerNode ain) {
		AppExpansionPatchNode tmp = new AppExpansionPatchNode(ain.layout,
				ain.packageName, ain.versionCode);
		String[] candidates = tmp.resolveContainer("").list(tmp);
		if (candidates.length == 0) {
			return null;
		}
		Arrays.sort(candidates);
		int i = 0;
		tmp = parse(ain.layout, candidates[0]);
		if (tmp.versionCode > ain.versionCode) {
			// Uh-oh! We are either missing a file or we got multiple versions of the
			// app and this is an earlier one that doesn't use expansions,yet.
			return null;
		}

		while (i < candidates.length && ain.versionCode > tmp.versionCode) {
			tmp = parse(ain.layout, candidates[i]);
			i++;
		}
		return tmp;
	}

}
