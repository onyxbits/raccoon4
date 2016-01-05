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

import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

/**
 * A builder that models the menu bar as a treelike data structure. This way,
 * the location of every menu component can be given by a path. For example, the
 * path "file/new" would identify the "New" operation in the "File" menu. The
 * following rules apply when specifying a path:
 * <ul>
 * <li>As path components may be used for localization, they may only consist of
 * characters that are legal as keys in {@link ResourceBundle}S (minus the '.'
 * character).
 * <li>A path component must consist of at least one character (e.g. "file//new"
 * is illegal).
 * <li>All paths are relative to the menubar (root component) and menubars don't
 * have a concept of "empty directories". Therefore there may not be leading or
 * trailing slashes.
 * <li>Path component names may be chosen arbitrarily (e.g. "datei/neu" instead
 * of "file/new"). They have no meaning to the builder. It is the hierarchy that
 * matters.
 * <li>Every menu component must have a unique path string (this goes for
 * separators as well)!
 * </ul>
 * 
 * @author patrick
 * 
 */
public class MenuBarBuilder {

	/**
	 * Separator to use for path components.
	 */
	public static final char PATHSEPARATOR = '/';

	private HashMap<String, JComponent> maps;
	private JMenuBar mbar;
	private ActionLocalizer localizer;
	private boolean fullPath;
	private String prefix;

	public MenuBarBuilder() {
		maps = new HashMap<String, JComponent>();
		mbar = new JMenuBar();
	}

	/**
	 * Use an {@link ActionLocalizer} to automatically localize items. Only items
	 * that are added after a call to this method will be localized. Likewise,
	 * setting the localizer to null again will stop localization for the
	 * following items.
	 * 
	 * @param localizer
	 *          the localizer to use. May be null.
	 * @return this reference for chaining
	 */
	public MenuBarBuilder withLocalizer(ActionLocalizer localizer) {
		this.localizer = localizer;
		return this;
	}

	/**
	 * Use the menu path (replacing '/' with '.') as the localization key? This
	 * only applies to items following this method call.
	 * 
	 * @param fp
	 *          true to use the full path, false to only use the last path
	 *          component as key.
	 * @return
	 */
	public MenuBarBuilder withFullPath(boolean fp) {
		fullPath = fp;
		return this;
	}

	/**
	 * Use a prefix when constructing the localization key. This applies only to
	 * items added after this call.
	 * 
	 * @param prefix
	 *          prefix (no trailing '.') or null.
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder withPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	/**
	 * Create multiple menus at once through their path specification. NOTE: this
	 * method requires a localizer. Otherwise all menus will be blank.
	 * 
	 * @param paths
	 *          where to add
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder withMenus(String... paths) {
		for (String path : paths) {
			addMenu(path,
					localizer.localize(new NoAction(), getLocalizationKey(path)));
		}
		return this;
	}

	/**
	 * Add an item/menu to the menubar. Items are appended to their parent
	 * container in the same order in which they are added. This implies that
	 * parent items must be created before children can be added.
	 * 
	 * @param path
	 *          where to add.
	 * @param item
	 *          what to add. NOTE: this should be an instance of {@link JMenu},
	 *          {@link JMenuItem}, {@link JRadioButtonMenuItem},
	 *          {@link JCheckBoxMenuItem} or {@link JSeparator}. Nesting menus is
	 *          perfectly fine (though not from a usability point of view).
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder add(String path, JComponent item) {
		maps.put(path, item);
		if (localizer != null && item instanceof JMenuItem) {
			Action a = ((JMenuItem) item).getAction();
			localizer.localize(a, getLocalizationKey(path));
		}
		String parentPath = path.substring(0,
				Math.max(path.lastIndexOf(PATHSEPARATOR), 0));
		JComponent parent = maps.get(parentPath);
		if (item.getName() == null) {
			item.setName(path); // for unit tests
		}
		if (parent == null) {
			mbar.add(item);
		}
		else {
			parent.add(item);
		}
		return this;
	}

	private String getLocalizationKey(String path) {
		StringBuilder ret = new StringBuilder();
		if (prefix != null) {
			ret.append(prefix);
			ret.append(".");
		}
		if (fullPath) {
			ret.append(path.replaceAll("/", "."));
		}
		else {
			ret.append(path.substring(path.lastIndexOf('/') + 1));
		}
		return ret.toString();
	}

	/**
	 * Construct a {@link JMenu} and forward it to add().
	 * 
	 * @param path
	 *          where to add
	 * @param a
	 *          configuration
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder addMenu(String path, Action a) {
		return add(path, new JMenu(a));
	}

	/**
	 * Construct a {@link JMenuItem} and forward it to add()
	 * 
	 * @param path
	 *          where to add
	 * @param a
	 *          configuration
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder addItem(String path, Action a) {
		return add(path, new JMenuItem(a));
	}

	/**
	 * Construct a {@link JCheckBoxMenuItem} and forward it to add()
	 * 
	 * @param path
	 *          where to add
	 * @param a
	 *          configuration
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder addCheckbox(String path, Action a) {
		return add(path, new JCheckBoxMenuItem(a));
	}

	/**
	 * Construct a {@link JRadioButtonMenuItem} and forward it to add()
	 * 
	 * @param path
	 *          where to add
	 * @param a
	 *          configuration
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder addRadio(String path, Action a) {
		return add(path, new JRadioButtonMenuItem(a));
	}

	/**
	 * Construct a {@link JSeparator} and forward it to add()
	 * 
	 * @param path
	 *          where to add
	 * @return this reference for chaining.
	 */
	public MenuBarBuilder addSeparator(String path) {
		return add(path, new JSeparator());
	}

	/**
	 * Construct the configured menubar
	 * 
	 * @return the menubar.
	 */
	public JMenuBar build() {
		return mbar;
	}

}
