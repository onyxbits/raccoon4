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
package de.onyxbits.raccoon.db;

/**
 * For tracking what app was downloaded by which profile. This is a 1 to n
 * relationship. A given app can only be owned by one profile at a time, though
 * ownership may be transferred at will. Apps are not required to have owners.
 * 
 * Ownership is used for determining which profile should be used for pulling
 * updates.
 * 
 * @author patrick
 * 
 */
public class PlayAppOwner {

	public final AndroidApp app;
	public final PlayProfile profile;

	public PlayAppOwner(AndroidApp app, PlayProfile profile) {
		this.app = app;
		this.profile = profile;
	}
}
