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
package de.onyxbits.raccoon.gui;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import de.onyxbits.raccoon.gui.Traits;

public class FeaturesTest {

	@Test
	public void testRed() {
		assertEquals("nyzam", Traits.red("almnz"));
	}

	@Test
	public void testSaat() {
		String[] ret = { "kxew", "knwn", "iwpr", "jkkg", "mcdi", "mryy", "lbqd",
				"lokt", "ogfv" };
		for (int i = 0; i < ret.length; i++) {
			assertEquals(ret[i], Traits.saat(new Random(i)));
		}
	}

	@Test
	public void testInterpret() {
		String[] in = Traits.interpret("CQcPCA5YFAsTDgc=", "abcd");
		assertEquals("hello", in[0]);
		assertEquals("world", in[1]);
		assertEquals(2, in.length);
	}
}
