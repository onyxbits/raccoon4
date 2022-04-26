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
			// Note to self: the original implementation did not check for array
			// length.
			// Nowadays it is possible to get keys with empty values and therefore an
			// ArrayIndexOutOfBoundsException. Since we are only interested in
			// "Auth=",
			// we can simply ignore everything that's not a k/v pair.
			if (keyValue.length == 2) {
				keyValueMap.put(keyValue[0], keyValue[1]);
			}
			else {
				// System.err.println("Utils.paseResponse: "+response);
			}
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
		System.arraycopy(keyByteArray, modulusLength + 8, exponentByteArray, 0,
				exponentLength);
		BigInteger publicExponent = new BigInteger(1, exponentByteArray);

		return KeyFactory.getInstance("RSA")
				.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
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
			byte[] arrayOfByte4 = cipher.doFinal(bytes2Encrypt, j * 86,
					(bytes2Encrypt.length - j * 86));
			System.arraycopy(header, 0, cryptedBytes, j * 133, header.length);
			System.arraycopy(arrayOfByte4, 0, cryptedBytes, j * 133 + header.length,
					arrayOfByte4.length);
		}
		return Base64.encodeToString(cryptedBytes, 10);
	}

	private static int readInt(byte[] data, int offset) {
		return (0xFF & data[offset]) << 24 | (0xFF & data[(offset + 1)]) << 16
				| (0xFF & data[(offset + 2)]) << 8 | (0xFF & data[(offset + 3)]);
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
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
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
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static Scheme getMockedScheme()
			throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslcontext = SSLContext.getInstance("TLS");

		sslcontext.init(null, new TrustManager[] { new DummyX509TrustManager() },
				null);
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

		return AndroidCheckinRequest.newBuilder().setId(0)
				.setCheckin(AndroidCheckinProto.newBuilder()
						.setBuild(AndroidBuildProto.newBuilder().setId(
								"samsung/r9qxeea/r9q:12/SP1A.210812.016/G990BXXU1BUL5:user/release-keys")
								.setProduct("r9qxeea").setCarrier("Google")
								.setRadio("I9300XXALF2").setBootloader("G990BXXU1BUL5")
								.setClient("android-google")
								.setTimestamp(new Date().getTime() / 1000).setGoogleServices(16)
								.setDevice("r9q").setSdkVersion(31).setModel("SM-G990B")
								.setManufacturer("samsung").setBuildProduct("r9qxeea")
								.setOtaInstalled(false))
						.setLastCheckinMsec(0).setCellOperator("310260")
						.setSimOperator("310260").setRoaming("mobile-notroaming")
						.setUserNumber(0))
				.setLocale("en_US").setTimeZone("Europe/Berlin").setVersion(3)
				.setDeviceConfiguration(getDeviceConfigurationProto()).setFragment(0)
				.build();
	}

	public static DeviceConfigurationProto getDeviceConfigurationProto() {
		return DeviceConfigurationProto.newBuilder().setTouchScreen(3)
				.setKeyboard(1).setNavigation(1).setScreenLayout(2)
				.setHasHardKeyboard(false).setHasFiveWayNavigation(false)
				.setScreenDensity(480).setGlEsVersion(196610)
				.addAllSystemSharedLibrary(Arrays.asList("libhoaeffects.qti.so",
						"libFood.camera.samsung.so",
						"libFacePreProcessing_jni.camera.samsung.so",
						"lib_nativeJni.dk.samsung.so", "libStride.camera.samsung.so",
						"android.test.base", "android.test.mock",
						"com.samsung.android.nfc.rfcontrol",
						"vendor.qti.hardware.data.connection-V1.1-java",
						"sec_platform_library", "com.samsung.device", "SemAudioThumbnail",
						"libqti-perfd-client.so",
						"libInteractiveSegmentation.camera.samsung.so",
						"libsnap_hidl.snap.samsung.so", "com.sec.esecomm",
						"com.samsung.android.ibs.framework-v1",
						"libvesframework.videoeditor.samsung.so",
						"android.hidl.manager-V1.0-java", "libupdateprof.qti.so",
						"libBeauty_v4.camera.samsung.so",
						"libUltraWideDistortionCorrection.camera.samsung.so",
						"libImageScreener.camera.samsung.so",
						"libsecimaging.camera.samsung.so", "qti-telephony-hidl-wrapper",
						"com.samsung.android.semtelephonesdk.framework-v1",
						"libfastcvopt.so", "libcore2nativeutil.camera.samsung.so",
						"libBright_core.camera.samsung.so", "liblistenjni.qti.so",
						"com.android.hotwordenrollment.common.util",
						"libDualCamBokehCapture.camera.samsung.so", "vsimmanager",
						"libhumantracking_util.camera.samsung.so",
						"libknox_remotedesktopclient.knox.samsung.so",
						"libOpenCv.camera.samsung.so",
						"libtflite2.myfilters.camera.samsung.so",
						"libSceneDetector_v1.camera.samsung.so", "libOpenCL.so",
						"libthermalclient.qti.so", "libhal.wsm.samsung.so",
						"libFoodDetector.camera.samsung.so",
						"libcolor_engine.camera.samsung.so",
						"libSDKRecognitionText.spensdk.samsung.so",
						"libsuperresolution.arcsoft.so", "lib_native_client.dk.samsung.so",
						"sem-telephony-common", "libss_jni.securestorage.samsung.so",
						"com.samsung.android.knox.knoxsdk",
						"libsuperresolution_wrapper_v2.camera.samsung.so",
						"libSwIsp_wrapper_v1.camera.samsung.so",
						"libMyFilterPlugin.camera.samsung.so",
						"libperfsdk.performance.samsung.so", "qti-telephony-utils",
						"libFacePreProcessing.camera.samsung.so",
						"libbinauralrenderer_wrapper.qti.so",
						"libVideoClassifier.camera.samsung.so", "libSEF.quram.so",
						"libdiag_system.qti.so", "libFeature.polarr.so",
						"libhumantracking.arcsoft.so", "libcdsprpc.so",
						"android.hidl.base-V1.0-java",
						"libsume_mediabuffer_jni.media.samsung.so",
						"libvraudio_client.qti.so", "libFacialStickerEngine.arcsoft.so",
						"semsdrvideoconverter", "libtensorflowlite_c.camera.samsung.so",
						"com.samsung.android.spensdk.framework-v1", "com.samsung.bbc",
						"libjpegsq.media.samsung.so",
						"libHprFace_GAE_jni.camera.samsung.so",
						"libheifcapture_jni.media.samsung.so", "samsungkeystoreutils",
						"libObjectDetector_v1.camera.samsung.so",
						"libLocalTM_wrapper.camera.samsung.so", "lib.engmodejni.samsung.so",
						"libQREngine_common.camera.samsung.so",
						"com.samsung.android.knox.analytics.sdk",
						"libRectify.camera.samsung.so",
						"libAIQSolution_MPI.camera.samsung.so", "libadsprpc.so",
						"libPortraitSolution.camera.samsung.so", "libimagecodec.quram.so",
						"com.android.location.provider", "secimaging", "semextendedformat",
						"libsecphotoremaster.camera.samsung.so", "semmediatranscoder",
						"libtensorflowLite.dynamic_viewing.camera.samsung.so",
						"libvesgraphicframework.videoeditor.samsung.so",
						"lib_pet_detection.arcsoft.so", "libsurfaceutil.camera.samsung.so",
						"libMyFilter.camera.samsung.so",
						"libsrib_humanaware_engine.camera.samsung.so",
						"libTracking.polarr.so", "libagifencoder.quram.so",
						"android.net.ipsec.ike",
						"libAiSolution_wrapper_v1.camera.samsung.so",
						"libsecjpeginterface.camera.samsung.so",
						"com.samsung.android.nfc.mpos",
						"libFace_Landmark_API.camera.samsung.so",
						"org.carconnectivity.android.digitalkey.secureelement",
						"com.android.future.usb.accessory",
						"libDocRectifyWrapper.camera.samsung.so",
						"lib_vnd_client.dk.samsung.so", "saiv",
						"androidx.camera.extensions.impl",
						"libsecairevital.camera.samsung.so", "libsdsprpc.so",
						"libMultiFrameProcessing30.camera.samsung.so", "android.ext.shared",
						"libEventDetector.camera.samsung.so", "libveengine.arcsoft.so",
						"libSFEffect.fonteffect.samsung.so", "javax.obex", "izat.xt.srv",
						"libsemimagecrop_jni.media.samsung.so", "com.google.android.gms",
						"libStrideTensorflowLite.camera.samsung.so",
						"libHpr_RecGAE_cvFeature_v1.0.camera.samsung.so",
						"libjpega.camera.samsung.so",
						"libmidas_DNNInterface.camera.samsung.so",
						"libLocalTM_pcc.camera.samsung.so", "liblow_light_hdr.arcsoft.so",
						"libexifa.camera.samsung.so",
						"com.sec.android.sdhmssdk.framework-v1",
						"libImageTagger.camera.samsung.so", "com.samsung.android.nfc.t4t",
						"libHprFace_GAE_api.camera.samsung.so",
						"libImageSegmenter_v1.camera.samsung.so", "imsmanager",
						"scamera_sdk_util", "com.publicnfc",
						"libsecuresnap_hidl.snap.samsung.so",
						"libsrib_CNNInterface.camera.samsung.so",
						"libDLInterface_hidl.camera.samsung.so",
						"libmidas_core.camera.samsung.so",
						"libvesinterface.videoeditor.samsung.so",
						"libswldc_capture_core.camera.samsung.so",
						"org.simalliance.openmobileapi", "libSmartScan.camera.samsung.so",
						"libQOC.qti.so", "EpdgManager", "libhigh_dynamic_range.arcsoft.so",
						"android.test.runner", "libsmart_cropping.camera.samsung.so",
						"libPortraitDistortionCorrection.arcsoft.so",
						"libphotoeditorEngine.camera.samsung.so",
						"libBestComposition.polarr.so", "libYuv.polarr.so",
						"libface_landmark.arcsoft.so",
						"libFaceRestoration.camera.samsung.so", "libPolarrSnap.polarr.so",
						"org.apache.http.legacy", "libhigh_res.arcsoft.so",
						"com.qualcomm.qti.imscmservice@1.0-java",
						"libFacialBasedSelfieCorrection.camera.samsung.so",
						"com.android.cts.ctsshim.shared_library",
						"com.android.media.remotedisplay", "scamera_sep",
						"libneural.snap.samsung.so",
						"com.samsung.android.psitrackersdk.framework-v1", "sfeffect",
						"libsecimaging_pdk.camera.samsung.so",
						"com.android.mediadrm.signer",
						"com.samsung.android.privacydashboard.framework-v1",
						"libsce_v1.crypto.samsung.so", "videoeditor_sdk",
						"com.qualcomm.qti.imscmservice-V2.0-java",
						"com.qualcomm.qti.imscmservice-V2.1-java",
						"libPortraitDistortionCorrectionCali.arcsoft.so",
						"libFacialAttributeDetection.arcsoft.so",
						"com.qualcomm.qti.imscmservice-V2.2-java", "libqape.qti.so",
						"libimage_enhancement.arcsoft.so", "libapex_jni.media.samsung.so",
						"rcsopenapi"

				))
				.addAllSystemAvailableFeature(Arrays.asList(
						"android.hardware.sensor.proximity",
						"com.samsung.android.sdk.camera.processor",
						"com.samsung.feature.aodservice_v09",
						"com.sec.feature.motionrecognition_service",
						"com.sec.feature.cover.sview",
						"android.hardware.telephony.ims.singlereg",
						"android.hardware.sensor.accelerometer",
						"android.software.controls", "android.hardware.faketouch",
						"com.samsung.feature.audio_listenback",
						"android.hardware.usb.accessory", "android.software.backup",
						"android.hardware.touchscreen",
						"android.hardware.touchscreen.multitouch", "android.software.print",
						"android.software.activities_on_secondary_displays",
						"com.sec.feature.nfc_authentication_cover",
						"android.hardware.wifi.rtt",
						"com.samsung.feature.SAMSUNG_EXPERIENCE",
						"com.google.android.feature.ACCESSIBILITY_PRELOAD",
						"com.sec.feature.nfc_authentication",
						"android.software.voice_recognizers",
						"android.software.picture_in_picture",
						"android.hardware.fingerprint", "com.samsung.android.knox.knoxsdk",
						"android.hardware.sensor.gyroscope",
						"android.hardware.audio.low_latency",
						"android.software.vulkan.deqp.level",
						"android.software.cant_save_state",
						"android.hardware.security.model.compatible",
						"com.samsung.feature.device_category_phone",
						"com.samsung.android.nfc.t4temul",
						"com.samsung.android.authfw.tahal", "android.hardware.opengles.aep",
						"com.sec.feature.sensorhub", "android.hardware.bluetooth",
						"android.hardware.camera.autofocus",
						"android.hardware.telephony.gsm", "android.hardware.telephony.ims",
						"com.sec.feature.cocktailpanel",
						"android.software.incremental_delivery",
						"android.software.sip.voip", "android.hardware.se.omapi.ese",
						"com.sec.feature.saccessorymanager",
						"com.samsung.feature.samsung_experience_mobile",
						"com.samsung.android.camerasdkservice", "android.hardware.usb.host",
						"android.hardware.audio.output", "android.software.verified_boot",
						"android.hardware.camera.flash", "android.hardware.camera.front",
						"android.hardware.se.omapi.uicc",
						"android.hardware.strongbox_keystore",
						"android.hardware.screen.portrait",
						"com.google.android.feature.DPS", "android.hardware.nfc",
						"com.google.android.feature.TURBO_PRELOAD",
						"com.samsung.feature.ipsgeofence", "com.nxp.mifare",
						"com.samsung.feature.SAMSUNG_EXPERIENCE_AM",
						"android.hardware.sensor.stepdetector",
						"android.software.home_screen",
						"vendor.android.hardware.camera.preview-dis.back",
						"android.hardware.microphone", "com.samsung.feature.aremoji.v2",
						"android.software.autofill",
						"com.samsung.android.sdk.camera.processor.effect",
						"android.software.securely_removes_users",
						"android.hardware.bluetooth_le", "android.hardware.sensor.compass",
						"android.hardware.touchscreen.multitouch.jazzhand",
						"android.hardware.sensor.barometer", "android.software.app_widgets",
						"android.software.input_methods", "android.hardware.sensor.light",
						"android.hardware.vulkan.version",
						"android.software.companion_device_setup",
						"com.google.android.feature.EEA_V2_DEVICE",
						"com.samsung.feature.galaxyfinder_v7",
						"com.sec.feature.wirelesscharger_authentication",
						"android.software.device_admin",
						"android.hardware.keystore.limited_use_key",
						"android.hardware.wifi.passpoint", "android.hardware.camera",
						"android.hardware.screen.landscape",
						"com.google.android.feature.AER_OPTIMIZED",
						"android.hardware.ram.normal",
						"com.samsung.feature.samsungpositioning.snlp",
						"com.samsung.android.authfw",
						"com.samsung.android.api.version.2402",
						"com.samsung.android.api.version.2403",
						"com.samsung.android.api.version.2501",
						"com.samsung.android.api.version.2502",
						"com.samsung.android.api.version.2601",
						"com.samsung.android.api.version.2701",
						"com.samsung.android.api.version.2801",
						"com.samsung.android.api.version.2802",
						"com.samsung.android.api.version.2803",
						"com.samsung.android.api.version.2901",
						"com.samsung.android.api.version.2902",
						"com.samsung.android.api.version.2903",
						"com.samsung.android.api.version.3001",
						"com.samsung.android.api.version.3002",
						"com.samsung.android.api.version.3101", "com.sec.feature.cover",
						"android.software.managed_users", "com.sec.feature.nsflp",
						"android.software.webview", "android.hardware.sensor.stepcounter",
						"android.hardware.camera.capability.manual_post_processing",
						"android.hardware.camera.any",
						"android.hardware.camera.capability.raw",
						"android.hardware.vulkan.compute",
						"android.software.connectionservice",
						"android.hardware.touchscreen.multitouch.distinct",
						"android.hardware.location.network", "com.sec.android.secimaging",
						"android.software.cts", "android.software.sip",
						"android.hardware.camera.capability.manual_sensor",
						"android.software.app_enumeration",
						"android.hardware.camera.level.full",
						"com.sec.feature.cover.clearsideviewcover",
						"com.sec.feature.usb_authentication",
						"com.google.android.feature.EEA_DEVICE",
						"android.hardware.wifi.direct", "android.software.live_wallpaper",
						"com.sec.feature.pocketmode", "android.software.ipsec_tunnels",
						"com.google.android.paid.chrome",
						"android.software.freeform_window_management",
						"android.hardware.audio.pro", "android.hardware.nfc.hcef",
						"android.hardware.nfc.uicc", "android.hardware.location.gps",
						"android.software.midi", "com.samsung.feature.samsungpositioning",
						"android.hardware.nfc.any", "android.hardware.nfc.ese",
						"android.hardware.nfc.hce", "android.hardware.wifi",
						"android.hardware.location", "com.google.android.paid.search",
						"android.hardware.vulkan.level",
						"com.samsung.android.cameraxservice",
						"com.samsung.android.knox.knoxsdk.api.level.33",
						"com.samsung.android.knox.knoxsdk.api.level.34",
						"com.samsung.android.knox.knoxsdk.api.level.35",
						"android.hardware.wifi.aware",
						"android.software.secure_lock_screen",
						"android.hardware.biometrics.face", "android.hardware.telephony",
						"com.sec.android.smartface.smart_stay",
						"android.software.file_based_encryption"))
				.addAllNativePlatform(
						Arrays.asList("arm64-v8a", "armeabi-v7a", "armeabi"))
				.setScreenWidth(1080).setScreenHeight(2097)
				.addAllSystemSupportedLocale(Arrays.asList("af", "af_ZA", "am", "am_ET",
						"ar", "ar_EG", "bg", "bg_BG", "ca", "ca_ES", "cs", "cs_CZ", "da",
						"da_DK", "de", "de_AT", "de_CH", "de_DE", "de_LI", "el", "el_GR",
						"en", "en_AU", "en_CA", "en_GB", "en_NZ", "en_SG", "en_US", "es",
						"es_ES", "es_US", "fa", "fa_IR", "fi", "fi_FI", "fr", "fr_BE",
						"fr_CA", "fr_CH", "fr_FR", "hi", "hi_IN", "hr", "hr_HR", "hu",
						"hu_HU", "in", "in_ID", "it", "it_CH", "it_IT", "iw", "iw_IL", "ja",
						"ja_JP", "ko", "ko_KR", "lt", "lt_LT", "lv", "lv_LV", "ms", "ms_MY",
						"nb", "nb_NO", "nl", "nl_BE", "nl_NL", "pl", "pl_PL", "pt", "pt_BR",
						"pt_PT", "rm", "rm_CH", "ro", "ro_RO", "ru", "ru_RU", "sk", "sk_SK",
						"sl", "sl_SI", "sr", "sr_RS", "sv", "sv_SE", "sw", "sw_TZ", "th",
						"th_TH", "tl", "tl_PH", "tr", "tr_TR", "ug", "ug_CN", "uk", "uk_UA",
						"vi", "vi_VN", "zh_CN", "zh_TW", "zu", "zu_ZA"))
				.addAllGlExtension(Arrays.asList("GL_EXT_buffer_storage",
						"GL_EXT_discard_framebuffer",
						"GL_KHR_robust_buffer_access_behavior", "GL_OES_EGL_sync",
						"GL_QCOM_shader_framebuffer_fetch_noncoherent",
						"GL_EXT_EGL_image_array", "GL_QCOM_frame_extrapolation",
						"GL_EXT_read_format_bgra", "GL_OES_read_format",
						"GL_OES_shader_image_atomic", "GL_EXT_geometry_shader",
						"GL_OES_texture_npot", "GL_QCOM_tiled_rendering",
						"GL_KHR_blend_equation_advanced", "GL_EXT_texture_border_clamp",
						"GL_ANDROID_extension_pack_es31a", "GL_QCOM_texture_foveated",
						"GL_EXT_texture_format_BGRA8888", "GL_OES_texture_env_crossbar",
						"GL_OES_blend_subtract", "GL_OES_depth_texture_cube_map",
						"GL_KHR_debug", "GL_QCOM_motion_estimation",
						"GL_QCOM_texture_foveated_subsampled_layout",
						"GL_EXT_texture_filter_anisotropic", "GL_OES_matrix_palette",
						"GL_EXT_color_buffer_float", "GL_OES_vertex_half_float",
						"GL_OES_surfaceless_context",
						"GL_EXT_shader_non_constant_global_initializers",
						"GL_OVR_multiview", "GL_OES_texture_storage_multisample_2d_array",
						"GL_EXT_texture_norm16", "GL_APPLE_texture_2D_limited_npot",
						"GL_QCOM_extended_get", "GL_EXT_disjoint_timer_query",
						"GL_EXT_EGL_image_external_wrap_modes", "GL_OES_get_program_binary",
						"GL_QCOM_alpha_test", "GL_EXT_clip_control",
						"GL_EXT_primitive_bounding_box", "GL_OES_standard_derivatives",
						"GL_OES_texture_compression_astc", "GL_EXT_robustness",
						"GL_EXT_protected_textures", "GL_OES_point_size_array",
						"GL_EXT_EGL_image_storage", "GL_OES_blend_equation_separate",
						"GL_OES_rgb8_rgba8", "GL_EXT_clip_cull_distance",
						"GL_QCOM_validate_shader_binary",
						"GL_ARM_shader_framebuffer_fetch_depth_stencil",
						"GL_EXT_blend_func_extended", "GL_EXT_draw_buffers_indexed",
						"GL_EXT_debug_marker", "GL_OVR_multiview2",
						"GL_OES_framebuffer_object", "GL_ARB_vertex_buffer_object",
						"GL_EXT_texture_format_sRGB_override", "GL_EXT_gpu_shader5",
						"GL_OES_texture_mirrored_repeat",
						"GL_QCOM_shader_framebuffer_fetch_rate", "GL_EXT_memory_object_fd",
						"GL_OES_depth_texture", "GL_EXT_external_buffer",
						"GL_EXT_texture_buffer", "GL_OES_texture_view",
						"GL_OES_EGL_image_external", "GL_OES_vertex_array_object",
						"GL_OES_stencil_wrap", "GL_EXT_tessellation_shader",
						"GL_EXT_multisampled_render_to_texture",
						"GL_KHR_blend_equation_advanced_coherent",
						"GL_EXT_shader_io_blocks", "GL_OES_EGL_image",
						"GL_KHR_texture_compression_astc_hdr",
						"GL_OVR_multiview_multisampled_render_to_texture",
						"GL_OES_texture_stencil8", "GL_KHR_texture_compression_astc_ldr",
						"GL_KHR_no_error", "GL_EXT_color_buffer_half_float",
						"GL_OES_texture_3D", "GL_OES_texture_half_float",
						"GL_OES_sample_variables", "GL_EXT_sRGB_write_control",
						"GL_NV_shader_noperspective_interpolation",
						"GL_AMD_performance_monitor", "GL_AMD_compressed_ATC_texture",
						"GL_QCOM_shading_rate", "GL_OES_blend_func_separate",
						"GL_OES_texture_half_float_linear", "GL_EXT_texture_sRGB_decode",
						"GL_OES_packed_depth_stencil", "GL_EXT_texture_cube_map_array",
						"GL_OES_texture_cube_map", "GL_OES_draw_texture",
						"GL_OES_compressed_paletted_texture", "GL_EXT_sRGB",
						"GL_OES_sample_shading", "GL_QCOM_YUV_texture_gather",
						"GL_OES_texture_float", "GL_EXT_memory_object",
						"GL_EXT_shader_framebuffer_fetch", "GL_EXT_blit_framebuffer_params",
						"GL_EXT_fragment_invocation_density",
						"GL_OES_shader_multisample_interpolation", "GL_EXT_debug_label",
						"GL_EXT_YUV_target", "GL_OES_compressed_ETC1_RGB8_texture",
						"GL_EXT_texture_sRGB_R8", "GL_EXT_multisampled_render_to_texture2",
						"GL_EXT_texture_type_2_10_10_10_REV", "GL_OES_depth24",
						"GL_OES_point_sprite", "GL_EXT_copy_image",
						"GL_OES_texture_float_linear", "GL_OES_EGL_image_external_essl3",
						"GL_OES_element_index_uint"))
				.build();
	}
}
