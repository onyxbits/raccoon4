package de.onyxbits.raccoon.repo;

import java.io.File;


/**
 * Represents an APK file
 * 
 * @author patrick
 * 
 */
public class AppInstallerNode extends AppNode {

	public AppInstallerNode(Layout layout, String packageName, int versionCode) {
		super(layout, packageName, versionCode);
	}

	@Override
	public String getFileName() {
		return packageName + "-" + versionCode + ".apk";
	}

	/**
	 * Convenience method for getting the app icon
	 * 
	 * @return the corresponding {@link AppIconNode}
	 */
	public AppIconNode toIcon() {
		return new AppIconNode(layout, packageName, versionCode);
	}

	/**
	 * Delete the APK, the icon, obb files if no longer needed and the directory
	 * if empty.
	 */
	public void delete() {
		toIcon().resolve().delete();
		File me = resolve();
		me.delete();
		if (me.getParentFile().list().length == 0) {
			me.getParentFile().delete();
		}
	}

}
