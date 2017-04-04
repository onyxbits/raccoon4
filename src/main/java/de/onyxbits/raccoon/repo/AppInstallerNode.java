package de.onyxbits.raccoon.repo;

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

}
