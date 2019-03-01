/*
 * Copyright 2019 Patrick Ahlbrecht
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

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultDesktopManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.repo.Layout;

public class SearchWorkerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		DatabaseManager dbm = new DatabaseManager(Layout.DEFAULT.databaseDir);
		PlayManager pm = new PlayManager(dbm);
		SearchAppWorker worker = new SearchAppWorker(pm, pm.createConnection(),
				"test", new SearchEngineResultPage(SearchEngineResultPage.SEARCH));
		System.err.println(worker.doInBackground());
	}

}
