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
package de.onyxbits.raccoon.db;

import de.onyxbits.raccoon.gplay.PlayProfile;

/**
 * Name constants for the "variables" table.
 * 
 * @author patrick
 * 
 */
public interface Variables {

	/**
	 * The alias of the {@link PlayProfile} to use by default.
	 */
	public static final String PLAYPROFILE = "playprofile";

	/**
	 * When the database was created
	 */
	public static final String CREATED = "created";
}
