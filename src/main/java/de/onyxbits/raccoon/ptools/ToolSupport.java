/*
 * Copyright 2016 Patrick Ahlbrecht
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
package de.onyxbits.raccoon.ptools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

/**
 * Support class for handling platform specific aspects of the ADB tool.
 * 
 * @author patrick
 * 
 */
class ToolSupport {

	private static final String TOOLS = "platform-tools";
	private static final String USB = "usb_driver";

	/**
	 * The ADB binary
	 */
	public final File adb;

	/**
	 * The SQL light binary
	 */
	public final File sqlite;

	/**
	 * The fastboot binary
	 */
	public final File fastboot;

	private File binDir;

	/**
	 * 
	 * @param binDir
	 *          Raccoon binary dir.
	 */
	public ToolSupport(File binDir) {
		this.binDir = binDir;
		if (isWindows()) {
			adb = new File(new File(binDir, TOOLS), "adb.exe");
			sqlite = new File(new File(binDir, TOOLS), "sqlite3.exe");
			fastboot = new File(new File(binDir, TOOLS), "fastboot.exe");
		}
		else {
			adb = new File(new File(binDir, TOOLS), "adb");
			sqlite = new File(new File(binDir, TOOLS), "sqlite3");
			fastboot = new File(new File(binDir, TOOLS), "fastboot");
		}
	}

	/**
	 * Unpack and install tools and drivers
	 * 
	 * @param adbzip
	 *          zip file containing the tools package
	 * @param usbzip
	 *          zip file containing the usbdrivers (may be null).
	 */
	protected void install(File adbzip, File usbzip) throws IOException {
		if (adbzip != null) {
			unzip(adbzip);
		}
		if (usbzip != null) {
			unzip(usbzip);
		}

		try {
			Runtime rt = Runtime.getRuntime();
			if (isWindows()) {
				File inf = new File(new File(binDir, USB), "android_winusb.inf");
				rt.exec("pnputil -i -a " + inf.getAbsolutePath()).waitFor();
			}
			else {
				rt.exec("chmod +x " + adb.getAbsolutePath()).waitFor();
				rt.exec("chmod +x " + sqlite.getAbsolutePath()).waitFor();
				rt.exec("chmod +x " + fastboot.getAbsolutePath()).waitFor();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static boolean isWindows() {
		return System.getProperty("os.name").contains("indows");
	}

	private void unzip(File file) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(binDir, entry.getName());
				if (entry.isDirectory())
					entryDestination.mkdirs();
				else {
					entryDestination.getParentFile().mkdirs();
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();
				}
			}
		}
		finally {
			zipFile.close();
		}
	}
}
