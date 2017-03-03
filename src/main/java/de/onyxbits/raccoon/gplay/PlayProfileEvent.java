/*
 * Copyright 2017 Patrick Ahlbrecht
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

import java.util.EventObject;

public class PlayProfileEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Profile got created
	 */
	public static final int CREATED = 1;

	/**
	 * Profile got destroyed
	 */
	public static final int DESTROYED = 2;

	/**
	 * Profile was modified
	 */
	public static final int MODIFIED = 4;

	/**
	 * Profile was set as the default profile
	 */
	public static final int ACTIVATED = 8;

	/**
	 * The profile in question
	 */
	public final PlayProfile profile;

	/**
	 * Event type bitmask (CREATED, DESTROYED, MODIFIED or ACTIVATED)
	 */
	public final int type;

	public PlayProfileEvent(Object source, PlayProfile profile, int type) {
		super(source);
		this.profile = profile;
		this.type = type;
		if (!((type & ACTIVATED) == ACTIVATED) && profile == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Check if this event is relevant for how to connect to Play
	 * 
	 * @return true if type contains ACTIVATED
	 */
	public boolean isConnection() {
		return (type & ACTIVATED) == ACTIVATED;
	}

	public boolean isActivation() {
		return (type & ACTIVATED) == ACTIVATED && (profile != null);
	}

	public boolean isDeActivation() {
		return (type & ACTIVATED) == ACTIVATED && (profile == null);
	}

	public boolean isModification() {
		return (type & MODIFIED) == MODIFIED;
	}

	public boolean isDestruction() {
		return (type & DESTROYED) == DESTROYED;
	}

	public boolean isCreation() {
		return (type & CREATED) == CREATED;
	}

}
