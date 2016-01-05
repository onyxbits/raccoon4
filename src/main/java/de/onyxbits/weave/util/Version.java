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
package de.onyxbits.weave.util;

import java.text.ParseException;

public class Version implements Comparable<Version> {

	public final int major, minor, patch;

	/**
	 * Parse a version string
	 * 
	 * @param version
	 *          version in flat string format
	 * @throws ParseException
	 *           if parsing fails
	 */
	public Version(String version) throws IllegalArgumentException {
		try {
			String[] tmp = version.split("\\.", 4);
			major = Integer.parseInt(tmp[0]);
			minor = Integer.parseInt(tmp[1]);
			patch = Integer.parseInt(tmp[2]);
		}
		catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}

	public Version(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}

	@Override
	public int compareTo(Version v) {
		if (major < v.major) {
			return -1;
		}
		if (major > v.major) {
			return 1;
		}
		if (major == v.major) {
			if (minor < v.minor) {
				return -1;
			}
			if (minor > v.minor) {
				return 1;
			}
			if (minor == v.minor) {
				if (patch < v.patch) {
					return -1;
				}
				if (patch > v.patch) {
					return 1;
				}
				if (patch == v.patch) {
					return 0;
				}
			}
		}
		return 0; // We can't actually get here, but eclipse doesn't understand
							// that.
	}
}
