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

import static org.junit.Assert.*;
import org.junit.Test;

public class VariableDaoTest extends DaoTest {

	@Test
	public void testSetVar() {
		VariableDao dao = dbm.get(VariableDao.class);
		dao.setVar("hello", "world");
		assertEquals("world", dao.getVar("hello", null));
		dao.setVar("hello", "universe");
		assertEquals("universe", dao.getVar("hello", null));
	}

	@Test
	public void testDelVar() {
		VariableDao dao = dbm.get(VariableDao.class);
		dao.setVar("delete", "me");
		assertEquals("me", dao.getVar("delete", null));
		dao.setVar("delete", null);
		assertNull(dao.getVar("delete", null));
	}

}
