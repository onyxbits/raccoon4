package com.akdeniz.googleplaycrawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.akdeniz.googleplaycrawler.GooglePlay.AndroidBuildProto;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinProto;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinRequest;
import com.akdeniz.googleplaycrawler.GooglePlay.DeviceConfigurationProto;
import com.akdeniz.googleplaycrawler.misc.Base64;
import com.akdeniz.googleplaycrawler.misc.DummyX509TrustManager;

/**
 * 
 * @author akdeniz
 * 
 */
public class Utils {

    private static final String GOOGLE_PUBLIC_KEY = "AAAAgMom/1a/v0lblO2Ubrt60J2gcuXSljGFQXgcyZWveWLEwo6prwgi3"
	    + "iJIZdodyhKZQrNWp5nKJ3srRXcUW+F1BD3baEVGcmEgqaLZUNBjm057pKRI16kB0YppeGx5qIQ5QjKzsR8ETQbKLNWgRY0Q"
	    + "RNVz34kMJR3P/LgHax/6rmf5AAAAAwEAAQ==";

    /**
     * Parses key-value response into map.
     */
    public static Map<String, String> parseResponse(String response) {

	Map<String, String> keyValueMap = new HashMap<String, String>();
	StringTokenizer st = new StringTokenizer(response, "\n\r");

	while (st.hasMoreTokens()) {
	    String[] keyValue = st.nextToken().split("=");
	    keyValueMap.put(keyValue[0], keyValue[1]);
	}

	return keyValueMap;
    }

    private static PublicKey createKey(byte[] keyByteArray) throws Exception {

	int modulusLength = readInt(keyByteArray, 0);
	byte[] modulusByteArray = new byte[modulusLength];
	System.arraycopy(keyByteArray, 4, modulusByteArray, 0, modulusLength);
	BigInteger modulus = new BigInteger(1, modulusByteArray);

	int exponentLength = readInt(keyByteArray, modulusLength + 4);
	byte[] exponentByteArray = new byte[exponentLength];
	System.arraycopy(keyByteArray, modulusLength + 8, exponentByteArray, 0, exponentLength);
	BigInteger publicExponent = new BigInteger(1, exponentByteArray);

	return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
    }

    /**
     * Encrypts given string with Google Public Key.
     * 
     */
    public static String encryptString(String str2Encrypt) throws Exception {

	byte[] keyByteArray = Base64.decode(GOOGLE_PUBLIC_KEY, Base64.DEFAULT);

	byte[] header = new byte[5];
	byte[] digest = MessageDigest.getInstance("SHA-1").digest(keyByteArray);
	header[0] = 0;
	System.arraycopy(digest, 0, header, 1, 4);

	PublicKey publicKey = createKey(keyByteArray);

	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING");
	byte[] bytes2Encrypt = str2Encrypt.getBytes("UTF-8");
	int len = ((bytes2Encrypt.length - 1) / 86) + 1;
	byte[] cryptedBytes = new byte[len * 133];

	for (int j = 0; j < len; j++) {
	    cipher.init(1, publicKey);
	    byte[] arrayOfByte4 = cipher.doFinal(bytes2Encrypt, j * 86, (bytes2Encrypt.length - j * 86));
	    System.arraycopy(header, 0, cryptedBytes, j * 133, header.length);
	    System.arraycopy(arrayOfByte4, 0, cryptedBytes, j * 133 + header.length, arrayOfByte4.length);
	}
	return Base64.encodeToString(cryptedBytes, 10);
    }

    private static int readInt(byte[] data, int offset) {
	return (0xFF & data[offset]) << 24 | (0xFF & data[(offset + 1)]) << 16 | (0xFF & data[(offset + 2)]) << 8
		| (0xFF & data[(offset + 3)]);
    }

    /**
     * Reads all contents of the input stream.
     * 
     */
    public static byte[] readAll(InputStream inputStream) throws IOException {

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];

	int k = 0;
	for (; (k = inputStream.read(buffer)) != -1;) {
	    outputStream.write(buffer, 0, k);
	}

	return outputStream.toByteArray();
    }

    public static String bytesToHex(byte[] bytes) {
	final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	char[] hexChars = new char[bytes.length * 2];
	int v;
	for (int j = 0; j < bytes.length; j++) {
	    v = bytes[j] & 0xFF;
	    hexChars[j * 2] = hexArray[v >>> 4];
	    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	}
	return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
	int len = s.length();
	byte[] data = new byte[len / 2];
	for (int i = 0; i < len; i += 2) {
	    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
	}
	return data;
    }

    public static Scheme getMockedScheme() throws NoSuchAlgorithmException, KeyManagementException {
	SSLContext sslcontext = SSLContext.getInstance("TLS");

	sslcontext.init(null, new TrustManager[] { new DummyX509TrustManager() }, null);
	SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
	Scheme https = new Scheme("https", 443, sf);

	return https;
    }

    /**
     * Generates android checkin request with properties of "Galaxy S3".
     * 
     * <a href=
     * "http://www.glbenchmark.com/phonedetails.jsp?benchmark=glpro25&D=Samsung+GT-I9300+Galaxy+S+III&testgroup=system"
     * > http://www.glbenchmark.com/phonedetails.jsp?benchmark=glpro25&D=Samsung
     * +GT-I9300+Galaxy+S+III&testgroup=system </a>
     */
    public static AndroidCheckinRequest generateAndroidCheckinRequest() {

	return AndroidCheckinRequest
		.newBuilder()
		.setId(0)
		.setCheckin(
			AndroidCheckinProto
				.newBuilder()
				.setBuild(
					AndroidBuildProto.newBuilder()
						.setId("samsung/m0xx/m0:4.0.4/IMM76D/I9300XXALF2:user/release-keys")
						.setProduct("smdk4x12").setCarrier("Google").setRadio("I9300XXALF2")
						.setBootloader("PRIMELA03").setClient("android-google")
						.setTimestamp(new Date().getTime() / 1000).setGoogleServices(16).setDevice("m0")
						.setSdkVersion(21).setModel("GT-I9300").setManufacturer("Samsung")
						.setBuildProduct("m0xx").setOtaInstalled(false)).setLastCheckinMsec(0)
				.setCellOperator("310260").setSimOperator("310260").setRoaming("mobile-notroaming")
				.setUserNumber(0)).setLocale("en_US").setTimeZone("Europe/Istanbul").setVersion(3)
		.setDeviceConfiguration(getDeviceConfigurationProto()).setFragment(0).build();
    }

    public static DeviceConfigurationProto getDeviceConfigurationProto() {
	return DeviceConfigurationProto
		.newBuilder()
		.setTouchScreen(3)
		.setKeyboard(1)
		.setNavigation(1)
		.setScreenLayout(2)
		.setHasHardKeyboard(false)
		.setHasFiveWayNavigation(false)
		.setScreenDensity(320)
		.setGlEsVersion(131072)
		.addAllSystemSharedLibrary(
			Arrays.asList("android.test.runner", "com.android.future.usb.accessory", "com.android.location.provider",
				"com.android.nfc_extras", "com.google.android.maps", "com.google.android.media.effects",
				"com.google.widevine.software.drm", "javax.obex"))
		.addAllSystemAvailableFeature(
			Arrays.asList("android.hardware.bluetooth", "android.hardware.camera",
				"android.hardware.camera.autofocus", "android.hardware.camera.flash",
				"android.hardware.camera.front", "android.hardware.faketouch", "android.hardware.location",
				"android.hardware.location.gps", "android.hardware.location.network",
				"android.hardware.microphone", "android.hardware.nfc", "android.hardware.screen.landscape",
				"android.hardware.screen.portrait", "android.hardware.sensor.accelerometer",
				"android.hardware.sensor.barometer", "android.hardware.sensor.compass",
				"android.hardware.sensor.gyroscope", "android.hardware.sensor.light",
				"android.hardware.sensor.proximity", "android.hardware.telephony",
				"android.hardware.telephony.gsm", "android.hardware.touchscreen",
				"android.hardware.touchscreen.multitouch", "android.hardware.touchscreen.multitouch.distinct",
				"android.hardware.touchscreen.multitouch.jazzhand", "android.hardware.usb.accessory",
				"android.hardware.usb.host", "android.hardware.wifi", "android.hardware.wifi.direct",
				"android.software.live_wallpaper", "android.software.sip", "android.software.sip.voip",
				"com.cyanogenmod.android", "com.cyanogenmod.nfc.enhanced",
				"com.google.android.feature.GOOGLE_BUILD", "com.nxp.mifare", "com.tmobile.software.themes"))
		.addAllNativePlatform(Arrays.asList("armeabi-v7a", "armeabi"))
		.setScreenWidth(720)
		.setScreenHeight(1184)
		.addAllSystemSupportedLocale(
			Arrays.asList("af", "af_ZA", "am", "am_ET", "ar", "ar_EG", "bg", "bg_BG", "ca", "ca_ES", "cs", "cs_CZ",
				"da", "da_DK", "de", "de_AT", "de_CH", "de_DE", "de_LI", "el", "el_GR", "en", "en_AU", "en_CA",
				"en_GB", "en_NZ", "en_SG", "en_US", "es", "es_ES", "es_US", "fa", "fa_IR", "fi", "fi_FI", "fr",
				"fr_BE", "fr_CA", "fr_CH", "fr_FR", "hi", "hi_IN", "hr", "hr_HR", "hu", "hu_HU", "in", "in_ID",
				"it", "it_CH", "it_IT", "iw", "iw_IL", "ja", "ja_JP", "ko", "ko_KR", "lt", "lt_LT", "lv",
				"lv_LV", "ms", "ms_MY", "nb", "nb_NO", "nl", "nl_BE", "nl_NL", "pl", "pl_PL", "pt", "pt_BR",
				"pt_PT", "rm", "rm_CH", "ro", "ro_RO", "ru", "ru_RU", "sk", "sk_SK", "sl", "sl_SI", "sr",
				"sr_RS", "sv", "sv_SE", "sw", "sw_TZ", "th", "th_TH", "tl", "tl_PH", "tr", "tr_TR", "ug",
				"ug_CN", "uk", "uk_UA", "vi", "vi_VN", "zh_CN", "zh_TW", "zu", "zu_ZA"))
		.addAllGlExtension(
			Arrays.asList("GL_EXT_debug_marker", "GL_EXT_discard_framebuffer", "GL_EXT_multi_draw_arrays",
				"GL_EXT_shader_texture_lod", "GL_EXT_texture_format_BGRA8888",
				"GL_IMG_multisampled_render_to_texture", "GL_IMG_program_binary", "GL_IMG_read_format",
				"GL_IMG_shader_binary", "GL_IMG_texture_compression_pvrtc", "GL_IMG_texture_format_BGRA8888",
				"GL_IMG_texture_npot", "GL_IMG_vertex_array_object", "GL_OES_EGL_image",
				"GL_OES_EGL_image_external", "GL_OES_blend_equation_separate", "GL_OES_blend_func_separate",
				"GL_OES_blend_subtract", "GL_OES_byte_coordinates", "GL_OES_compressed_ETC1_RGB8_texture",
				"GL_OES_compressed_paletted_texture", "GL_OES_depth24", "GL_OES_depth_texture",
				"GL_OES_draw_texture", "GL_OES_egl_sync", "GL_OES_element_index_uint",
				"GL_OES_extended_matrix_palette", "GL_OES_fixed_point", "GL_OES_fragment_precision_high",
				"GL_OES_framebuffer_object", "GL_OES_get_program_binary", "GL_OES_mapbuffer",
				"GL_OES_matrix_get", "GL_OES_matrix_palette", "GL_OES_packed_depth_stencil",
				"GL_OES_point_size_array", "GL_OES_point_sprite", "GL_OES_query_matrix", "GL_OES_read_format",
				"GL_OES_required_internalformat", "GL_OES_rgb8_rgba8", "GL_OES_single_precision",
				"GL_OES_standard_derivatives", "GL_OES_stencil8", "GL_OES_stencil_wrap",
				"GL_OES_texture_cube_map", "GL_OES_texture_env_crossbar", "GL_OES_texture_float",
				"GL_OES_texture_half_float", "GL_OES_texture_mirrored_repeat", "GL_OES_vertex_array_object",
				"GL_OES_vertex_half_float")).build();
    }
}
