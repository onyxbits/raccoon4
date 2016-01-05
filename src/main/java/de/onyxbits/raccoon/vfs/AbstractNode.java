package de.onyxbits.raccoon.vfs;

import java.io.File;

/**
 * Superclass of all mapped files
 * 
 * @author patrick
 * 
 */
public abstract class AbstractNode {

	protected final Layout layout;

	/**
	 * Construct a new node
	 * 
	 * @param layout
	 *          the directory layout to use for resolving.
	 */
	public AbstractNode(Layout layout) {
		this.layout = layout;
	}

	/**
	 * Map the node to a file/directory
	 * 
	 * @return the location of the node on the filesystem
	 */
	public abstract File resolve();

}
