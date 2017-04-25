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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.akdeniz.googleplaycrawler.GooglePlay.DeviceConfigurationProto;

public abstract class MockDevice {

	/**
	 * The string to use for an unknwon property.
	 */
	public static final String UNKNOWN = "unknown";

	public abstract int getFinskyVersionCode();

	public abstract String getFinskyVersion();

	public abstract int getSdkVersion();

	public abstract String getDevice();

	public abstract String getHardware();

	public abstract String getBuildProduct();

	public abstract String getBuildId();

	public abstract String getBuildType();

	public abstract String getManufacturer();

	public abstract String getBootloader();

	public abstract String getFingerprint();

	public abstract int getScreenDensity();

	public abstract int getScreenWidth();

	public abstract int getScreenHeight();

	public abstract int getGlEsVersion();

	public List<String> getSupportedLocales() {
		return Arrays.asList("af", "af_ZA", "am", "am_ET", "ar", "ar_EG", "bg",
				"bg_BG", "ca", "ca_ES", "cs", "cs_CZ", "da", "da_DK", "de", "de_AT",
				"de_CH", "de_DE", "de_LI", "el", "el_GR", "en", "en_AU", "en_CA",
				"en_GB", "en_NZ", "en_SG", "en_US", "es", "es_ES", "es_US", "fa",
				"fa_IR", "fi", "fi_FI", "fr", "fr_BE", "fr_CA", "fr_CH", "fr_FR", "hi",
				"hi_IN", "hr", "hr_HR", "hu", "hu_HU", "in", "in_ID", "it", "it_CH",
				"it_IT", "iw", "iw_IL", "ja", "ja_JP", "ko", "ko_KR", "lt", "lt_LT",
				"lv", "lv_LV", "ms", "ms_MY", "nb", "nb_NO", "nl", "nl_BE", "nl_NL",
				"pl", "pl_PL", "pt", "pt_BR", "pt_PT", "rm", "rm_CH", "ro", "ro_RO",
				"ru", "ru_RU", "sk", "sk_SK", "sl", "sl_SI", "sr", "sr_RS", "sv",
				"sv_SE", "sw", "sw_TZ", "th", "th_TH", "tl", "tl_PH", "tr", "tr_TR",
				"ug", "ug_CN", "uk", "uk_UA", "vi", "vi_VN", "zh_CN", "zh_TW", "zu",
				"zu_ZA");
	}

	public abstract List<String> getNativePlatforms();

	public abstract List<String> getSharedLibraries();

	public abstract List<String> getSystemFeatures();

	public DeviceConfigurationProto toDeviceConfigurationProto() {
		return DeviceConfigurationProto.newBuilder().
				setHasFiveWayNavigation(false).setGlEsVersion(getGlEsVersion()).setHasHardKeyboard(false).setKeyboard(1).
				build();
	}

	/**
	 * Create the user agent string of the play client app
	 * 
	 * @return user agent string
	 */
	public String toFinskyUserAgent() {
		MessageFormat tmpl = new MessageFormat(
				"Android-Finsky/{0} (versionCode={1},sdk={2},device={3},hardware={4},product={5},build={6}:{7})");
		String[] args = { getFinskyVersion(), getFinskyVersionCode() + "",
				getSdkVersion() + "", getBuildProduct(), getDevice(), getHardware(),
				getBuildProduct(), getBuildId(), getBuildType() };
		return tmpl.format(args);
	}
}
