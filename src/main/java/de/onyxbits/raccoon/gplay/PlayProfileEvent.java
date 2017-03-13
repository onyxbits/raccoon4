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

import de.onyxbits.raccoon.db.DatasetEvent;

public class PlayProfileEvent extends DatasetEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Profile was set as the default profile
	 */
	public static final int ACTIVATED = 256;

	/**
	 * The profile in question
	 */
	public final PlayProfile profile;

	public PlayProfileEvent(PlayProfileDao source, int op, PlayProfile profile) {
		super(source, op);
		this.profile = profile;
		if (!((op & ACTIVATED) == ACTIVATED) && profile == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Check if this event is relevant for how to connect to Play
	 * 
	 * @return true if type contains ACTIVATED
	 */
	public boolean isConnection() {
		return (op & ACTIVATED) == ACTIVATED;
	}

	public boolean isActivation() {
		return (op & ACTIVATED) == ACTIVATED && (profile != null);
	}

	public boolean isDeActivation() {
		return (op & ACTIVATED) == ACTIVATED && (profile == null);
	}

}
