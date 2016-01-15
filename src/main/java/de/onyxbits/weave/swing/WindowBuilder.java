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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import de.onyxbits.weave.Globals;
import de.onyxbits.weave.Lifecycle;

/**
 * Builds top level windows.
 * 
 * @author patrick
 * 
 */
public final class WindowBuilder {

	private String title;
	private String[] iconresources;
	private MenuBarBuilder menuBarBuilder;
	private AbstractPanelBuilder content;
	private boolean undecorated;
	private Dimension size;
	private String name;
	private boolean fixedSize;
	private Window owner;
	private Component center;

	/**
	 * Construct a new builder
	 * 
	 * @param content
	 *          the builder for creating the content pane.
	 */
	public WindowBuilder(AbstractPanelBuilder content) {
		this.content = content;
	}

	/**
	 * Configure the component name
	 * 
	 * @param name
	 *          name of the component
	 * @return this reference for chaining
	 */
	public WindowBuilder withName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Center window
	 * 
	 * @param center
	 *          the component to center on
	 * @return this reference for chaining.
	 */
	public WindowBuilder withCenter(Component center) {
		this.center = center;
		return this;
	}

	/**
	 * Configure the window title
	 * 
	 * @param title
	 *          title to show in the title bar
	 * @return this reference for chaining.
	 */
	public WindowBuilder withTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Configure the icons to show in the window title bar and task manager.
	 * 
	 * @param iconresources
	 *          PNG image resource on the classpath. If multiple are given, the
	 *          one(s) with the best fitting size are chosen (standard sizes:
	 *          16,24,48,64,96,128).
	 * @return this reference for chaining.
	 */
	public WindowBuilder withIcons(String... iconresources) {
		this.iconresources = iconresources;
		return this;
	}

	/**
	 * Configure the menubar
	 * 
	 * @param mbb
	 *          the builder for creating a menubar.
	 * @return this reference for chaining.
	 */
	public WindowBuilder withMenu(MenuBarBuilder mbb) {
		this.menuBarBuilder = mbb;
		return this;
	}

	/**
	 * Disable window decorations
	 * 
	 * @return this reference for chaining.
	 */
	public WindowBuilder withoutDecoration() {
		undecorated = true;
		return this;
	}

	/**
	 * Configure the window size
	 * 
	 * @param width
	 *          width
	 * @param height
	 *          height
	 * @return this reference for chaining.
	 */
	public WindowBuilder withSize(int width, int height) {
		this.size = new Dimension(width, height);
		return this;
	}

	/**
	 * Configure the window to no be resizable by the user.
	 * 
	 * @return this reference for chaining.
	 */
	public WindowBuilder withFixedSize() {
		fixedSize = true;
		return this;
	}

	/**
	 * Configure whether to build a standalone {@link JFrame} or a {@link JDialog}
	 * that is linked to another window. In general, all secondary windows should
	 * have their owner set to the primary window. That way the OS can group
	 * windows that belong together.
	 * <p>
	 * WARNING: this method allows for creating circular dependencies which will
	 * result in a {@link StackOverflowError}. Do not make a call to
	 * {@link Lifecycle#onCreateSecondaryWindow(Globals, String)} from anywhere
	 * within {@link Lifecycle#onCreatePrimaryWindow(Globals)} if the secondary
	 * windows are owned.
	 * 
	 * @param owner
	 *          the owning window.
	 * @return this reference for chaining
	 */
	public WindowBuilder withOwner(Window owner) {
		this.owner = owner;
		return this;
	}

	/**
	 * Build the window
	 * 
	 * @param globals
	 *          global registry
	 * @return a {@link JFrame} or a {@link JDialog} (depending on whether the
	 *         window is owned or not).
	 */
	public Window build(Globals globals) {
		Window ret = null;
		if (owner != null) {
			JDialog tmp = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
			tmp.setUndecorated(undecorated);
			if (menuBarBuilder != null) {
				tmp.setJMenuBar(menuBarBuilder.build());
			}
			if (content != null) {
				tmp.setContentPane(content.build(globals));
			}
			tmp.setResizable(!fixedSize);
			ret = tmp;
		}
		else {
			JFrame tmp = new JFrame(title);
			tmp.setUndecorated(undecorated);
			if (menuBarBuilder != null) {
				tmp.setJMenuBar(menuBarBuilder.build());
			}
			if (content != null) {
				tmp.setContentPane(content.build(globals));
			}
			tmp.setResizable(!fixedSize);
			ret = tmp;
		}

		ret.setName(name);
		if (iconresources != null) {

			List<Image> iconlst = new ArrayList<Image>(iconresources.length);
			Class<?> clazz = getClass();
			Toolkit tk = Toolkit.getDefaultToolkit();
			for (String icon : iconresources) {
				iconlst.add(tk.getImage(clazz.getResource(icon)));
			}
			ret.setIconImages(iconlst);
		}

		if (size != null) {
			ret.setSize(size);
			ret.revalidate();
		}
		else {
			ret.pack();
		}

		ret.setLocationRelativeTo(center);
		return ret;
	}
}
