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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

/**
 * A completely synthetic device that is configured through a properties file.
 * 
 * @author patrick
 * 
 */
public class SyntheticDevice extends MockDevice {

	private Properties device;

	public SyntheticDevice(Properties device) {
		this.device = device;
	}

	public static void convert(RealDevice device, PrintWriter writer)
			throws IOException {

	}

	@Override
	public int getFinskyVersionCode() {
		return 0;
	}

	@Override
	public String getFinskyVersion() {
		return null;
	}

	@Override
	public int getSdkVersion() {
		return 0;
	}

	@Override
	public String getDevice() {
		return null;
	}

	@Override
	public String getHardware() {
		return null;
	}

	@Override
	public String getBuildProduct() {
		return null;
	}

	@Override
	public String getBuildId() {
		return null;
	}

	@Override
	public String getBuildType() {
		return null;
	}

	@Override
	public String getManufacturer() {
		return null;
	}

	@Override
	public String getBootloader() {
		return null;
	}

	@Override
	public String getFingerprint() {
		return null;
	}

	@Override
	public int getScreenDensity() {
		return 0;
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
		return 0;
	}

	@Override
	public List<String> getNativePlatforms() {
		return null;
	}

	@Override
	public List<String> getSharedLibraries() {
		return null;
	}

	@Override
	public List<String> getSystemFeatures() {
		return null;
	}

}
