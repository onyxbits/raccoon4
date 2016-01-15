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


import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import de.onyxbits.weave.Globals;

/**
 * Panel builders compose {@link JComponent}S onto {@link JPanel}S and implement
 * that part of the business logic which deals with component interaction.
 * 
 * @author patrick
 * 
 */
public abstract class AbstractPanelBuilder {

	/**
	 * Configured border (may be null)
	 */
	protected Border border;

	/**
	 * Configured component name (may be null)
	 */
	protected String componentName;

	/**
	 * Central registry.
	 */
	protected Globals globals;

	/**
	 * Build the panel
	 * 
	 * @return the final panel.
	 */
	public final JPanel build(Globals globals) {
		this.globals = globals;
		JPanel ret = assemble();
		if (componentName != null) {
			ret.setName(componentName);
		}
		if (border != null) {
			ret.setBorder(border);
		}
		return ret;
	}

	/**
	 * Configure a border
	 * 
	 * @param border
	 *          border to add
	 * @return this reference for chaining.
	 */
	public AbstractPanelBuilder withBorder(Border border) {
		this.border = border;
		return this;
	}

	/**
	 * Configure a name for the component.
	 * 
	 * @param name
	 *          name (e.g. for unit testing)
	 * @return this reference for chaining.
	 */
	public AbstractPanelBuilder withComponentName(String name) {
		this.componentName = name;
		return this;
	}

	/**
	 * Subclasses must override this to plug their functionality into the provided
	 * panel
	 * 
	 * @return the panel that will be returned by build()
	 */
	protected abstract JPanel assemble();

	/**
	 * Release resources. Default implementation does nothing.
	 */
	public void destroy() {
	}
}
