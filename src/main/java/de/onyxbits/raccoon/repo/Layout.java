package de.onyxbits.raccoon.repo;

import java.io.File;

import javax.swing.filechooser.FileSystemView;


/**
 * Maps all the paths
 * 
 * @author patrick
 * 
 */
public final class Layout {

	/**
	 * Default layout. NOTE: don't reference this directly from anywhere within a
	 * {@link Lifecycle} except when putting it into the {@link Globals}.
	 */
	public static final Layout DEFAULT = new Layout().mkdirs();

	/**
	 * System property name
	 */
	public static final String HOMEDIRSYSPROP = "raccoon.home";

	/**
	 * Application home dir (top level directory for all other files)
	 */
	public final File homeDir;

	/**
	 * Directory for keeping database files in
	 */
	public final File databaseDir;

	/**
	 * Top level directory for all user media files
	 */
	public final File contentDir;

	/**
	 * Directory for storing apps.
	 */
	public final File appsDir;

	/**
	 * Directory for keeping shared files
	 */
	public final File shareDir;

	/**
	 * Toplevel directory for keeping external scripts and binaries in. May have
	 * subdirectories.
	 */
	public final File binDir;

	/**
	 * Construct a layout with a given top level directory
	 * 
	 * @param homeDir
	 *          the directory to which all others are relative to.
	 */
	public Layout(File homeDir) {
		this.homeDir = homeDir;
		binDir = new File(homeDir, "bin");
		shareDir = new File(homeDir, "share");
		contentDir = new File(homeDir, "content");
		databaseDir = new File(contentDir, "database");
		appsDir = new File(contentDir, "apps");
	}

	/**
	 * Construct a new Layout with the default root location. The default location
	 * is "~/Raccoon" on Linux and "My Documents" on Windows. unless overridden by
	 * the HOMEDIR system property.
	 */
	public Layout() {
		this(whereami());
	}

	/**
	 * Figure out where the Raccoon homedir is
	 * 
	 * @return the top level raccoon folder
	 */
	private static File whereami() {
		// Does the user want the Raccoon dir to be in a custom place?
		String tmp = System.getProperty(HOMEDIRSYSPROP, null);
		if (tmp == null) {
			// Nope!
			return new File(FileSystemView.getFileSystemView().getDefaultDirectory(),
					"Raccoon");
		}

		// User wants a custom directory, check if suitable.
		File f = new File(tmp);
		if (f.exists() && !f.isDirectory()) {
			return new File(FileSystemView.getFileSystemView().getDefaultDirectory(),
					"Raccoon");
		}
		else {
			return f;
		}
	}

	/**
	 * Create directories if necessary.
	 * 
	 * @return this reference
	 */
	public Layout mkdirs() {
		homeDir.mkdirs();
		binDir.mkdir();
		shareDir.mkdir();

		contentDir.mkdir();
		databaseDir.mkdir();
		appsDir.mkdir();
		return this;
	}
}
