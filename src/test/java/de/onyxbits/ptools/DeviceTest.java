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
package de.onyxbits.ptools;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.onyxbits.raccoon.ptools.BridgeManager;
import de.onyxbits.raccoon.ptools.Device;
import de.onyxbits.raccoon.repo.Layout;

public class DeviceTest {

	private static BridgeManager bridgeManager;
	private static Device device;

	@BeforeClass
	public static void setUp() throws Exception {
		File f = Files.createTempDirectory("raccoontest").toFile();
		f.deleteOnExit();
		bridgeManager = new BridgeManager(new Layout(f));
		bridgeManager.startup();
		while (device == null) {
			Thread.sleep(100);
			device = bridgeManager.getActiveDevice();
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		bridgeManager.shutdown();
	}

	@Test
	public void testGetSharedLibraries() {
		List<String> lst = device.getSharedLibraries();
		for (String lib : lst) {
			if ("android.test.runner".equals(lib)) {
				return;
			}
		}
		fail("Device missing standard libs!?");
	}

	@Test
	public void testGetSystemFeatures() {
		List<String> lst = device.getSystemFeatures();
		for (String feature : lst) {
			if ("android.hardware.wifi".equals(feature)) {
				return;
			}
		}
		fail("Device lacks WiFi!?");
	}

	@Test
	public void testGetProperty() {
		assertEquals(device.serial, device.getProperty("ro.serialno", null));
	}

}
