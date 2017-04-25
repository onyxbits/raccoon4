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

import java.util.ArrayList;
import java.util.List;

import de.onyxbits.raccoon.ptools.Device;

/**
 * Create a mock device based on an existing device, connected via ADB.
 * 
 * @author patrick
 * 
 */
public class RealDevice extends MockDevice {

	private Device device;
	private String versionName;
	private int versionCode;

	/**
	 * Create a new mock. Note: we don't attempt to retrieve version information
	 * of the Finsky app from the device. The premise of Raccoon is that the user
	 * either does not have Play Services installed or doesn't want to use them
	 * (and hence does not keep them up to date).
	 * 
	 * @param device
	 *          a device, connected via ADB
	 * @param versionName
	 *          version name of the Finsky app
	 * @param versionCode
	 *          version code of the Finsky app
	 */
	public RealDevice(Device device, String versionName, int versionCode) {
		this.device = device;
		this.versionName = versionName;
		this.versionCode = versionCode;
	}

	@Override
	public int getFinskyVersionCode() {
		return versionCode;
	}

	@Override
	public String getFinskyVersion() {
		return versionName;
	}

	@Override
	public int getSdkVersion() {
		try {
			return Integer.parseInt(device.getProperty("ro.build.version.sdk",
					UNKNOWN));
		}
		catch (NumberFormatException e) {
			return 14;
		}
	}

	@Override
	public String getDevice() {
		return device.getProperty("ro.product.device", UNKNOWN);
	}

	@Override
	public String getHardware() {
		return device.getProperty("ro.hardware", UNKNOWN);
	}

	@Override
	public String getBuildProduct() {
		return device.getProperty("ro.build.product", UNKNOWN);
	}

	@Override
	public String getBuildId() {
		return device.getProperty("ro.build.id", UNKNOWN);
	}

	@Override
	public String getBuildType() {
		return device.getProperty("ro.build.type", UNKNOWN);
	}

	@Override
	public List<String> getNativePlatforms() {
		List<String> abis = new ArrayList<String>();
		try {
			// Stuff works differently pre-lollipop
			if (Integer.parseInt(device.getProperty("ro.build.version.sdk", null)) < 21) {
				String tmp = device.getProperty("ro.product.cpu.abi", null);
				if (tmp != null) {
					abis.add(tmp);
				}
				tmp = device.getProperty("ro.product.cpu.abi2", null);
				if (tmp != null) {
					abis.add(tmp);
				}
			}
			else {
				String[] tmp = device.getProperty("ro.product.cpu.abilist", "").split(
						" *, *");
				for (String s : tmp) {
					abis.add(s);
				}
			}
		}
		catch (Exception e) {
		}
		return abis;
	}

	@Override
	public List<String> getSharedLibraries() {
		return device.getSharedLibraries();
	}

	@Override
	public List<String> getSystemFeatures() {
		return device.getSystemFeatures();
	}

	@Override
	public int getScreenDensity() {
		try {
			return Integer.parseInt(device.getProperty("ro.sf.lcd_density", UNKNOWN));
		}
		catch (NumberFormatException e) {
			return 240;
		}
	}

	@Override
	public String getManufacturer() {
		return device.getProperty("ro.product.manufacturer", UNKNOWN);
	}

	@Override
	public String getBootloader() {
		return device.getProperty("ro.bootloader", UNKNOWN);
	}

	@Override
	public String getFingerprint() {
		return device.getProperty("ro.build.fingerprint", UNKNOWN);
	}

	@Override
	public int getScreenWidth() {
		return 0;
	}

	@Override
	public int getScreenHeight() {
		return 0;
	}

	@Override
	public int getGlEsVersion() {
		try {
			return Integer.parseInt(device
					.getProperty("ro.opengles.version", UNKNOWN));
		}
		catch (NumberFormatException e) {
			return 310260;
		}
	}

}
