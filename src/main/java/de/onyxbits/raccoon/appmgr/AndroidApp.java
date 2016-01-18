package de.onyxbits.raccoon.appmgr;

import java.util.List;

/**
 * Models an Android app in the "appstore".
 * 
 * @author patrick
 * 
 */
public class AndroidApp {

	private long appId;

	private String packageName;

	private String version;

	private int versionCode;

	private int mainVersion;

	private int patchVersion;

	private String name;

	private String description;

	private List<AppGroup> groups;

	private int minSdk;

	private String minScreen;

	private List<String> usesPermissions;

	/**
	 * Check if this app is a member of the given group
	 * 
	 * @param group
	 *          the group.
	 * @return false if not member of the group or no groups have been set.
	 */
	public boolean memberOf(AppGroup group) {
		if (groups == null) {
			return false;
		}
		for (AppGroup g : groups) {
			if (g.getGroupId() == group.getGroupId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Wrapper for usesPermissions
	 * 
	 * @return permissions as a list
	 */
	public List<String> getUsesPermissions() {
		return usesPermissions;
	}

	/**
	 * Wrapper for usesPermissions
	 * 
	 * @param perms
	 *          list of permissions.
	 */
	public void setUsesPermissions(List<String> perms) {
		usesPermissions = perms;
	}

	public long getAppId() {
		return appId;
	}

	public void setAppId(long id) {
		this.appId = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMinSdk() {
		return minSdk;
	}

	public void setMinSdk(int minSdk) {
		this.minSdk = minSdk;
	}

	public String getMinScreen() {
		return minScreen;
	}

	public void setMinScreen(String minScreen) {
		this.minScreen = minScreen;
	}

	public List<AppGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<AppGroup> groups) {
		this.groups = groups;
	}

	public int getMainVersion() {
		return mainVersion;
	}

	public void setMainVersion(int mainVersion) {
		this.mainVersion = mainVersion;
	}

	public int getPatchVersion() {
		return patchVersion;
	}

	public void setPatchVersion(int patchVersion) {
		this.patchVersion = patchVersion;
	}

}
