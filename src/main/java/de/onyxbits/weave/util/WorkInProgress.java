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

import java.util.Vector;

/**
 * A registry for objects that contain "work in progress" (e.g. text editor
 * windows, download tasks,...). It is designed to be used for implementing
 * "Confirm to quit" dialogs.
 * 
 * @author patrick
 * 
 */
public final class WorkInProgress {

	private Vector<ContentHolder> list;

	public WorkInProgress() {
		list = new Vector<ContentHolder>();
	}

	/**
	 * Add an object to the dirty list
	 * 
	 * @param ch
	 *          object. Nothing happens if null or the object is already on the
	 *          list.
	 */
	public void add(ContentHolder ch) {
		if (ch != null && !list.contains(ch)) {
			list.add(ch);
		}
	}

	/**
	 * Remove an object from the dirty list
	 * 
	 * @param ch
	 *          the object to remove
	 */
	public void remove(ContentHolder ch) {
		list.remove(ch);
	}

	/**
	 * Check if there are any objects flagged as dirty
	 * 
	 * @return false as long as
	 */
	public boolean isClean() {
		for (ContentHolder ch : list) {
			if (ch.isDirty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Invoke {@link ContentHolder#autoClean()} on all registered objects.
	 */
	public void autoclean() {
		@SuppressWarnings("unchecked")
		Vector<ContentHolder> tmp = (Vector<ContentHolder>) list.clone();
		for (ContentHolder ch : tmp) {
			ch.autoClean();
		}
	}
}
