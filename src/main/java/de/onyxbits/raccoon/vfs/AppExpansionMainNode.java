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
package de.onyxbits.raccoon.vfs;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Models the main .OBB file
 * 
 * @author patrick
 * 
 */
public class AppExpansionMainNode extends AppNode implements FilenameFilter {

	public AppExpansionMainNode(Layout layout, String packageName, int versionCode) {
		super(layout, packageName, versionCode);
	}

	@Override
	public String getFileName() {
		return "main." + versionCode + "." + packageName + ".obb";
	}

	@Override
	public boolean accept(File dir, String name) {
		if (name.startsWith("main.") && name.endsWith(".obb")) {
			return true;
		}
		else {
			return false;
		}
	}
}
