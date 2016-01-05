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
package de.onyxbits.raccoon.ptools;

class RemoteFile {

	private final int mode;
	private final long size;
	private final long lastModified;
	private String path;

	public RemoteFile(String path, int mode, long size, long lastModified) {
		this.path = path;
		this.mode = mode;
		this.size = size;
		this.lastModified = lastModified;
	}

	public String getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	public long getLastModified() {
		return lastModified;
	}

	public boolean isDirectory() {
		return (mode & (1 << 14)) == (1 << 14);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RemoteFile that = (RemoteFile) o;

		if (!path.equals(that.path))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(mode);
		ret.append("\t");
		ret.append(size);
		ret.append(" ");
		ret.append(lastModified);
		ret.append(" ");
		ret.append(path);
		return ret.toString();
	}
}
