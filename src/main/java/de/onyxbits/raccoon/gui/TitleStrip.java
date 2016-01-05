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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A decorative panel with a main title, a sub title and an icon. Should be used
 * on dialog tpye windows.
 * 
 * @author patrick
 * 
 */
public class TitleStrip extends JPanel {

	/**
	 * The width/height the icon should be scaled to (for a consistent look).
	 */
	public static final int ICONSIZE = 64;

	/**
	 * A transparent blank default icon of ICONSIZE.
	 */
	public static final Icon BLANK = new ImageIcon(new BufferedImage(ICONSIZE,
			ICONSIZE, BufferedImage.TYPE_INT_ARGB));

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel title;
	private JLabel subTitle;
	private JLabel icon;

	public TitleStrip(String title, String subTitle, Icon icon) {
		this.title = new JLabel(title);
		this.subTitle = new JLabel(subTitle);
		this.icon = new JLabel(icon);
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		Font font = this.title.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 4);
		this.title.setFont(boldFont);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1;
		gbc.insets.top = 5;
		gbc.insets.left = 5;
		gbc.insets.right = 5;
		gbc.insets.bottom = 5;
		add(this.title, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.insets.left = 15;
		add(this.subTitle, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		add(this.icon, gbc);
	}

	public TitleStrip() {
		this("", "", BLANK);
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public void setSubTitle(String subTitle) {
		this.subTitle.setText(subTitle);
	}

	public void setIcon(Icon icon) {
		this.icon.setIcon(icon);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Point2D start = new Point2D.Float(0, 0);
		Point2D end = new Point2D.Float(0, getHeight());
		float[] dist = { 0.0f, 0.8f, .95f, 1f };
		Color bg = getBackground();
		Color bgd = getBackground().darker();
		Color[] colors = { bgd, bg, bgd, bgd.darker() };
		LinearGradientPaint gp1 = new LinearGradientPaint(start, end, dist, colors);
		g2d.setPaint(gp1);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}
}
