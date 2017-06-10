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
package de.onyxbits.raccoon.setup;

import static org.junit.Assert.*;

import org.junit.Test;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

public class NewProfile {

	@Test
	public void test() throws Exception {
		String uid = System.getProperty("raccoon.dev.gmail");
		assertNotNull("No credentials!", uid);
		String pwd = System.getProperty("raccoon.dev.gmail.pw");
		assertNotNull("No credentials!", pwd);
		GooglePlayAPI api = new GooglePlayAPI(uid, pwd);
		api.login();
		api.checkin();
		api.uploadDeviceConfig();
		System.out.println(api.details("de.onyxbits.listmyapps"));
	}

}
