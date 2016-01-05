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

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.onyxbits.raccoon.vfs.Layout;

public class AndroidAppTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private DatabaseManager dbm;

	@Before
	public void setUp() throws Exception {
		// dbm = new DatabaseManager(folder.newFolder("db"));
		dbm = new DatabaseManager(Layout.DEFAULT.databaseDir);
		dbm.startup();
	}

	@After
	public void tearDown() throws Exception {
		dbm.shutdown();
	}

	@Test
	public void test() throws Exception {
		URL url = this.getClass().getResource("/test.apk");
		AndroidApp app = AndroidAppDao.analyze(new File(url.getFile()));
		app.setDescription("Hello\" Wrold");
		dbm.get(AndroidAppDao.class).saveOrUpdate(app);
		System.err.println(dbm.get(AndroidAppDao.class).isStored(app));
	}

}
