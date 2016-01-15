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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;

import de.onyxbits.weave.swing.BrowseAction;

/**
 * A subclass of {@link JEditorPane} that supports smooth rendering of HTML text
 * and clickable links.
 * 
 * @author patrick
 * 
 */
public class HyperTextPane extends JEditorPane {

	/**
  *
  */
	private static final long serialVersionUID = 1L;
	private boolean tooltip;

	public HyperTextPane(String txt) {
		super("text/html", txt);
		setEditable(false);
		setFocusable(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D graphics2d = (Graphics2D) g;
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
	}

	/**
	 * Only draw the content (behave like a JLabel).
	 * 
	 * @return this reference for chaining.
	 */
	public HyperTextPane withTransparency() {
		setBackground(new Color(0, 0, 0, 0));
		setOpaque(false);
		return this;
	}

	/**
	 * Force a fixed width (and a dynamic height).
	 * 
	 * @param width
	 *          width of the pane
	 * @return this reference for chaining.
	 */
	public HyperTextPane withWidth(int width) {
		setSize(width, Short.MAX_VALUE);
		revalidate();
		setPreferredSize(new Dimension(width, getPreferredSize().height));
		return this;
	}

	/**
	 * Show the target URL in a tooltip when hovering over a link
	 * 
	 * @return this reference for chaining.
	 */
	public HyperTextPane withLinkToolTip() {
		tooltip = true;
		return this;
	}

	@Override
	public void fireHyperlinkUpdate(HyperlinkEvent e) {
		if (tooltip) {
			if (e.getEventType() == EventType.ENTERED) {
				try {
					setToolTipText(e.getURL().toURI().toString());
				}
				catch (URISyntaxException e1) {
					setToolTipText(null);
				}
			}
			if (e.getEventType() == EventType.EXITED) {
				setToolTipText(null);
			}
		}

		if (e.getEventType() == EventType.ACTIVATED) {
			try {
				BrowseAction.open(e.getURL().toURI());
			}
			catch (Exception e1) {
			}
		}
		super.fireHyperlinkUpdate(e);
	}

}
