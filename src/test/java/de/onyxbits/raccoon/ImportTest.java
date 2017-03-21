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
package de.onyxbits.raccoon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AppExpansionMainNode;
import de.onyxbits.raccoon.repo.AppExpansionPatchNode;
import de.onyxbits.raccoon.repo.AppIconNode;
import de.onyxbits.raccoon.repo.AppInstallerNode;
import de.onyxbits.raccoon.repo.Layout;

public class ImportTest {

	private File testInstaller;
	private File testMain;
	private File testPatch;
	private Layout layout;

	@Before
	public void setup() {
		testInstaller = new File(getClass().getResource("/test.apk").getFile());
		testMain = new File(getClass().getResource("/test-main.obb").getFile());
		testPatch = new File(getClass().getResource("/test-patch.obb").getFile());
		layout = new Layout(new File(getClass().getResource("/testhome").getFile()));
		layout.mkdirs();
	}

	@After
	public void tearDown() {
		//SessionSource.destroyInstance(layout.databaseDir);
	}

	/**
	 * Try to import app files from an external location into the media pool.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testImportFiles() throws IOException {
		/*
		AndroidApp app = AndroidApp.analyze(testInstaller);
		int ver = app.getVersionCode();
		String pack = app.getPackageName();

		assertEquals(4, ver);
		assertEquals("de.onyxbits.geobookmark", pack);
		assertEquals(
				"android.permission.ACCESS_FINE_LOCATION,android.permission.ACCESS_COARSE_LOCATION,com.android.launcher.permission.INSTALL_SHORTCUT,android.permission.ACCESS_MOCK_LOCATION",
				app.getUsesPermissionsOUT());
		assertEquals(8, app.getMinSdk());
		assertTrue(app.getName().startsWith("Geo")); // Problem: localization

		// APK
		AppInstallerNode inst = new AppInstallerNode(layout, pack, ver);
		assertFalse(inst.resolve().exists());
		inst.importFrom(testInstaller);
		assertEquals(268967l, inst.resolve().length());
		assertEquals("de.onyxbits.geobookmark_4.apk", inst.getFileName());
		inst.resolve().delete();

		// Icon
		AppIconNode icon = new AppIconNode(layout, pack, ver);
		assertFalse(icon.resolve().exists());
		icon.extractFrom(testInstaller);
		assertEquals("appicon_4.png", icon.getFileName());
		assertEquals(22579l, icon.resolve().length());
		icon.resolve().delete();

		// Main OBB
		AppExpansionMainNode main = new AppExpansionMainNode(layout, pack, ver);
		assertFalse(main.resolve().exists());
		main.importFrom(testMain);
		assertEquals("main.4.de.onyxbits.geobookmark.obb", main.getFileName());
		assertEquals(21l, main.resolve().length());
		main.resolve().delete();

		// Patch OBB
		AppExpansionPatchNode patch = new AppExpansionPatchNode(layout, pack, ver);
		assertFalse(patch.resolve().exists());
		patch.importFrom(testPatch);
		assertEquals("patch.4.de.onyxbits.geobookmark.obb", patch.getFileName());
		assertEquals(22l, patch.resolve().length());
		patch.resolve().delete();

		patch.resolveContainer().delete();
		*/
	}
}
