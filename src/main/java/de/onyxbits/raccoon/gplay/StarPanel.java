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
package de.onyxbits.raccoon.gplay;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * A panel for displaying a "five star" rating as a bar.
 * 
 * @author patrick
 * 
 */
public class StarPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float fill;
	private int stars;

	private static final Color BACK = new Color(40, 40, 40);
	private static final Color UNFILLED = new Color(95, 95, 95);
	private static final Color POOR = new Color(200, 0, 0);
	private static final Color MEDIOCRE = new Color(200, 200, 0);
	private static final Color GOOD = new Color(0, 200, 0);

	/**
	 * 
	 * @param stars
	 *          number of star levels
	 * @param fill
	 *          percentage to fill.
	 */
	public StarPanel(int stars, float fill) {
		this.stars = stars;
		this.fill = fill;
	}

	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		Color save = gr.getColor();
		int width = getWidth();
		int height = getHeight();

		gr.setColor(BACK);
		gr.fillRect(0, 0, width, height);

		gr.setColor(UNFILLED);
		gr.fillRect(2, 2, width - 4, height - 4);

		Color bar = POOR;
		gr.setColor(POOR);
		if (fill > 0.5f) {
			bar = MEDIOCRE;
		}
		if (fill > 0.70f) {
			bar = GOOD;
		}
		gr.setColor(bar);
		gr.fillRect(2, 2, (int) (width * fill) - 4, height - 4);

		gr.setColor(bar.brighter());
		gr.fillRect(2, 2, (int) (width * fill) - 4, (height - 4) / 2);

		gr.setColor(BACK);

		for (int i = 0; i < stars; i++) {
			gr.drawLine((int) (width / stars * i), 0, (int) (width / stars * i),
					height);
		}

		gr.setColor(save);
	}
}
