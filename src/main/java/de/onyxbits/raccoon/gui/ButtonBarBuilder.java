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
package de.onyxbits.raccoon.gui;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * Create a row of buttons. Buttons are added to the panel in configuration
 * order. All buttons will be equal in size.
 * 
 * @author patrick
 * 
 */
public class ButtonBarBuilder extends AbstractPanelBuilder {

	private ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>();

	private boolean verticalAlignment;
	private int vgap = 5;
	private int hgap = 5;

	/**
	 * Add a button
	 * 
	 * @param b
	 *          the button
	 * @return this reference for chaining.
	 */
	public ButtonBarBuilder add(AbstractButton b) {
		buttons.add(b);
		return this;
	}

	/**
	 * Add a simple JButton
	 * 
	 * @param act
	 *          configuration
	 * @return this reference for chaining.
	 */
	public ButtonBarBuilder addButton(Action act) {
		return add(new JButton(act));
	}

	/**
	 * Add a checkbox
	 * 
	 * @param act
	 *          configuration
	 * @return this reference for chaining.
	 */
	public ButtonBarBuilder addCheckBox(Action act) {
		return add(new JCheckBox(act));
	}

	/**
	 * Add a toggle button
	 * 
	 * @param act
	 *          configuration
	 * @return this reference for chaining.
	 */
	public ButtonBarBuilder addToggleButton(Action act) {
		return add(new JToggleButton(act));
	}

	/**
	 * Switch to a vertical layout (default is horizontal).
	 * 
	 * @return this reference for chaining.
	 */
	public ButtonBarBuilder withVerticalAlignment() {
		verticalAlignment = true;
		return this;
	}

	/**
	 * Configure space between buttons.
	 * 
	 * @param hgap
	 *          horizontal space
	 * @param vgap
	 *          vertical space.
	 * @return this reference for chaining.
	 */
	public ButtonBarBuilder withGaps(int hgap, int vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
		return this;
	}

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();

		if (verticalAlignment) {
			panel.setLayout(new GridLayout(0, 1, hgap, vgap));
		}
		else {
			panel.setLayout(new GridLayout(1, 0, hgap, vgap));
		}
		for (int i = 0; i < buttons.size(); i++) {
			panel.add(buttons.get(i));
		}

		if (verticalAlignment) {
			JPanel ret = new JPanel();
			panel.setBorder(new EmptyBorder(15, 0, 15, 0));
			ret.add(panel);
			return ret;
		}
		else {
			return panel;
		}
	}

}
