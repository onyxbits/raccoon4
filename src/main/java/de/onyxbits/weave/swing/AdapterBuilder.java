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


/**
 * A builder that simply wraps a {@link JComponent}. It is mainly intended for
 * testing, so a component can quickly be plugged in where a builder is
 * requested.
 * 
 * @author patrick
 * 
 */
public class AdapterBuilder extends AbstractPanelBuilder {

	private JComponent component;

	public AdapterBuilder(JComponent component) {
		this.component = component;
	}

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.add(component);
		panel.setName(getClass().getName());
		return panel;
	}

}
