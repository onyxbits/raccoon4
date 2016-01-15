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

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

/**
 * Utility class for localizing {@link Action} objects. Keys in the
 * {@link ResourceBundle} take the following format: <code>
 * [globalPrefix.]id.TYPE
 * </code> e.g. "action.quit.name" or "quit.short_description".
 * 
 * @author patrick
 * 
 */
public class ActionLocalizer {

	/**
	 * See {@link KeyStroke} for the format.
	 */
	public static final String MNEMONIC = "mnemonic";
	public static final String NAME = "name";
	public static final String SHORT_DESCRIPTION = "short_description";
	public static final String LONG_DESCRIPTION = "long_description";
	public static final String SMALL_ICON = "small_icon";
	public static final String LARGE_ICON = "large_icon";

	private ResourceBundle rsrc;
	private String globalPrefix;

	/**
	 * Construct an new localizer
	 * 
	 * @param bundle
	 *          name of the resourcebundle to use for configuration.
	 * @param globalPrefix
	 *          optional key prefix in the given bundle. May be null.
	 */
	public ActionLocalizer(String bundle, String globalPrefix) {
		this(ResourceBundle.getBundle(bundle), globalPrefix);
	}

	/**
	 * Construct a new localizer with a given resource bundle
	 * 
	 * @param rsrc
	 *          the resourcebundle to use for configuration
	 * @param globalPrefix
	 *          optional key prefix in the given bundle. May be null.
	 */
	public ActionLocalizer(ResourceBundle rsrc, String globalPrefix) {
		this.rsrc = rsrc;
		this.globalPrefix = globalPrefix;
	}

	/**
	 * Configure an action
	 * 
	 * @param a
	 *          the action object
	 * @param id
	 *          unique identifier to be used in the key
	 * @return the configured action object.
	 */
	public Action localize(Action a, String id) {
		a.putValue(Action.NAME, lookup(id, NAME));

		a.putValue(Action.SHORT_DESCRIPTION, lookup(id, SHORT_DESCRIPTION));
		a.putValue(Action.LONG_DESCRIPTION, lookup(id, LONG_DESCRIPTION));
		a.putValue(Action.SMALL_ICON, lookup(id, SMALL_ICON));

		String tmp = lookup(id, MNEMONIC);
		if (tmp != null) {
			// Don't catch parsing exceptions! These must bubble up so stuff gets
			// fixed.
			a.putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(tmp).getKeyCode());
		}

		tmp = lookup(id, LARGE_ICON);
		if (tmp != null) {
			ImageIcon ico = new ImageIcon(getClass().getResource(tmp), "");
			a.putValue(Action.LARGE_ICON_KEY, ico);
		}

		return a;
	}

	/**
	 * Create an configure an action.
	 * 
	 * @param id
	 *          the unique identifier to use in the key.
	 * @return an action with no functionality beyond being able to localize
	 *         things like {@link JMenu} instances.
	 */
	public Action localize(String id) {
		return localize(new NoAction(), id);
	}

	/**
	 * Bind an accelerator key to the action
	 * 
	 * @param target
	 *          the action to accelerate
	 * @param keycode
	 *          a VK_ value from {@link KeyEvent}
	 * @param useModifier
	 *          true to add the platform specific modifier key.
	 * @see java.awt.event.KeyEvent
	 * @return the accelerated action.
	 */
	public Action accelerate(Action target, int keycode, boolean useModifier) {
		if (useModifier) {
			target.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keycode,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		else {
			target.putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(keycode, 0));
		}
		return target;
	}

	private String lookup(String id, String val) {
		String key = id + "." + val;
		if (globalPrefix != null) {
			key = globalPrefix + "." + key;
		}
		if (rsrc.containsKey(key)) {
			return (String) rsrc.getObject(key);
		}
		return null;
	}

}
