package de.onyxbits.raccoon.repo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Models files that belong to an App
 * @author patrick
 *
 */
public abstract class AppNode extends AbstractNode {

	/**
	 * App identifier
	 */
	protected final String packageName;
	
	/**
	 * App identifier
	 */
	protected final int versionCode;

	public AppNode(Layout layout, String packageName, int versionCode) {
		super(layout);
		this.packageName = packageName;
		this.versionCode = versionCode;
	}
	
	/**
	 * Import the file in the storage from an external location.
	 * @param src
	 * @throws IOException
	 */
	public void importFrom(File src) throws IOException {
		FileUtils.copyFile(src, resolve());
	}

	public final File resolve(String appsDir) {
		return new File(resolveContainer(appsDir), getFileName());
	}

	public final File resolve() {
		return new File(resolveContainer(""), getFileName());
	}

	/**
	 * Get the directory containing all of the app's files (in various versions).
	 * 
	 * @param layout
	 *          layout to resolve against
	 * @return the directory of the app.
	 */
	public final File resolveContainer(String appsDir) {
		if (appsDir != "") {
			return new File(appsDir, packageName);
		} else {
			return new File(layout.appsDir, packageName);
		}
	}

	/**
	 * Compute the filename
	 * 
	 * @return the name under which the file is to be stored.
	 */
	public abstract String getFileName();
}
