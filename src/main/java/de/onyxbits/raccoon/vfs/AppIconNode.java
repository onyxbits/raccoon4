package de.onyxbits.raccoon.vfs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;

public class AppIconNode extends AppNode {

	public AppIconNode(Layout layout, String packageName, int versionCode) {
		super(layout, packageName, versionCode);
	}

	@Override
	public String getFileName() {
		return "appicon_" + versionCode + ".png";
	}

	/**
	 * Extract the icon from an installer file.
	 * 
	 * @param apk
	 *          the installer file (may live anywhere).
	 * @throws IOException
	 *           if extraction fails. NOTE: This is not necessarily an error. APK
	 *           files are not required to contain an icon.
	 */
	public void extractFrom(File apk) throws IOException {
		ApkParser apkParser = null;
		File icon = new AppIconNode(layout, packageName, versionCode).resolve();
		icon.getParentFile().mkdirs();
		apkParser = new ApkParser(apk);
		ApkMeta meta = apkParser.getApkMeta();
		if (meta.getIcon()==null) {
			apkParser.close();
			throw new IOException("No icon in APK");
		}
		byte[] data = apkParser.getFileData(meta.getIcon());
		if (data == null) {
			apkParser.close();
			throw new IOException("No icon in APK");
		}
		FileUtils.writeByteArrayToFile(icon, data);
		apkParser.close();
	}
}
