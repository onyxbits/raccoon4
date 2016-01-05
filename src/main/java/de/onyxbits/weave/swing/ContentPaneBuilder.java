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

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A general purpose top level builder to populate the content pane of a
 * {@link JFrame}. This builder uses a {@link BorderLayout} for its panel which
 * places the main content in the center and offers additional slots around the
 * sides (e.g. for toolbars).
 * 
 * @author patrick
 * 
 */
public class ContentPaneBuilder extends AbstractPanelBuilder {

	private AbstractPanelBuilder content;
	private HashMap<AbstractPanelBuilder, String> sideComponents;
	private JScrollPane scrollPane;

	/**
	 * Create a new builder with a given content main content area
	 * 
	 * @param content
	 *          builder for the main content.
	 */
	public ContentPaneBuilder(AbstractPanelBuilder content) {
		this.content = content;
	}

	/**
	 * Place components around the sides of the main content.
	 * 
	 * @param component
	 *          the component to add
	 * @param toolbarLocation
	 *          where to add (use {@link BorderLayout} NORTH, EAST,SOUTH or WEST).
	 * @return this reference for chaining
	 */
	public ContentPaneBuilder addSideContent(AbstractPanelBuilder component,
			String location) {
		if (sideComponents == null) {
			sideComponents = new HashMap<AbstractPanelBuilder, String>();
		}
		sideComponents.put(component, location);
		return this;
	}

	/**
	 * Wrap the content pane inside a scrollpane
	 * 
	 * @param scrollPane
	 *          wrapping pane
	 * @return this reference for chaining.
	 */
	public ContentPaneBuilder withScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		return this;
	}

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		if (sideComponents != null) {
			Set<AbstractPanelBuilder> components = sideComponents.keySet();
			for (AbstractPanelBuilder component : components) {
				panel.add(component.build(globals), sideComponents.get(component));
			}
		}
		if (scrollPane != null) {
			scrollPane.add(content.build(globals));
			panel.add(scrollPane, BorderLayout.CENTER);
		}
		else {
			panel.add(content.build(globals), BorderLayout.CENTER);
		}

		return panel;
	}

	@Override
	public void destroy() {
		content.destroy();
		Set<AbstractPanelBuilder> components = sideComponents.keySet();
		for (AbstractPanelBuilder component : components) {
			component.destroy();
		}
		scrollPane = null;
		sideComponents = null;
	}
}
