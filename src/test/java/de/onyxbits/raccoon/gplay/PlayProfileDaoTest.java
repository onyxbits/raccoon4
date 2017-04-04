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

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.onyxbits.raccoon.db.DaoTest;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;

public class PlayProfileDaoTest extends DaoTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test if a profile can be modified.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {
		PlayProfile profile = new PlayProfile();
		profile.setAlias("someone");
		PlayProfileDao dao = dbm.get(PlayProfileDao.class);
		dao.add(profile);
		profile.setAgent("agent");
		dao.update(profile);
		assertEquals("agent", dao.get("someone").getAgent());
	}

	/**
	 * Test if an app can be reassigned to another {@link PlayProfile}
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testOwnerChange() throws SQLException {
		PlayProfile first = new PlayProfile();
		first.setAlias("first");
		PlayProfile second = new PlayProfile();
		second.setAlias("second");

		AndroidApp app = new AndroidApp();
		app.setPackageName("de.onyxbits.testapp");
		app.setName("A Test App");
		app.setVersion("1.0");
		app.setVersionCode(1);
		AndroidAppDao appDao = dbm.get(AndroidAppDao.class);
		appDao.saveOrUpdate(app);

		PlayProfileDao profileDao = dbm.get(PlayProfileDao.class);
		profileDao.add(first);
		profileDao.add(second);

		PlayAppOwnerDao ownerDao = dbm.get(PlayAppOwnerDao.class);

		ownerDao.own(app, first);
		assertEquals(app.getAppId(), ownerDao.list(first).get(0).getAppId());
		assertEquals(0, ownerDao.list(second).size());

		ownerDao.own(app, second);
		assertEquals(app.getAppId(), ownerDao.list(second).get(0).getAppId());
		assertEquals(0, ownerDao.list(first).size());
	}

}
