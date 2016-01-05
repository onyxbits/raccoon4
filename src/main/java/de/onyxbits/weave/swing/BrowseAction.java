/*
 * Copyright 2015 Patrick Ahlbrecht
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
package de.onyxbits.weave.swing;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * An {@link Action} that understands URIs and how to open them in an external
 * program. Two non standard schemes are supported:
 * <ul>
 * <li>print
 * <li>edit
 * </ul>
 * Both are an alias for the "file" scheme and instruct the action to open a
 * file for printing/editing.
 * 
 * @author patrick
 * 
 */
public class BrowseAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private URI uri;

	/**
	 * Configure this action with an URI to open.
	 * 
	 * @param uri
	 *          the uri. If the scheme is not understood/supported, then the
	 *          action will disable itself.
	 */
	public BrowseAction(URI uri) {
		setLocation(uri);
	}

	/**
	 * Change the location
	 * 
	 * @param uri
	 *          new target URI.
	 */
	public void setLocation(URI uri) {
		this.uri = uri;
		setEnabled(isSupported(uri));
	}

	/**
	 * Check if the uri can be handled.
	 * 
	 * @param uri
	 *          uri
	 * @return true if this {@link Action} knows how to handle the uri.
	 */
	public boolean isSupported(URI uri) {
		if (uri == null) {
			return false;
		}

		if (java.awt.Desktop.isDesktopSupported()) {
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
			String scheme = uri.getScheme().toLowerCase();
			if ("http".equals(scheme) || "https".equals(scheme)
					|| "ftp".equals(scheme)) {
				return desktop.isSupported(java.awt.Desktop.Action.BROWSE);
			}
			if ("mail".equals(scheme)) {
				return desktop.isSupported(java.awt.Desktop.Action.MAIL);
			}
			if ("file".equals(scheme)) {
				return desktop.isSupported(java.awt.Desktop.Action.OPEN);
			}
		}
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		open(uri);
	}

	public static void open(URI uri) {
		String scheme = uri.getScheme().toLowerCase();
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		try {
			if ("http".equals(scheme) || "https".equals(scheme)
					|| "ftp".equals(scheme)) {
				desktop.browse(uri);
			}
			if ("mailto".equals(scheme)) {
				desktop.mail(uri);
			}
			if ("file".equals(scheme) || "jar".equals(scheme)) {
				desktop.open(new File(uri));
			}
		}
		catch (IOException exp) {
		}
	}

}
