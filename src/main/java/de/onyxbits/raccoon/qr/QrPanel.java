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
package de.onyxbits.raccoon.qr;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * A {@link JPanel} that paints a QR code on its background.
 * 
 * @author patrick
 * 
 */
public class QrPanel extends JPanel {

	/**
  *
  */
	private static final long serialVersionUID = 1L;

	private static final BufferedImage BLANK = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB);

	private static final int[] PIX = { 0, 0, 0, 255 };
	private static final int[] BACK = { 0, 0, 0, 0 };

	private BufferedImage image;

	private int size;
	private QRCodeWriter qrCodeWriter;
	private HashMap<EncodeHintType, Object> hintMap;
	private JPopupMenu menu;
	private boolean toggle;
	private String contentString;

	/**
	 * Create a new panel of a given size.
	 * 
	 * @param size
	 *          panel width/height
	 */
	public QrPanel(int size) {
		this.size = size;
		qrCodeWriter = new QRCodeWriter();
		hintMap = new HashMap<EncodeHintType, Object>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		image = BLANK;
	}

	/**
	 * Add popup menu to the panel.
	 * 
	 * @param menuActions
	 *          actions to put into the popup menu. a null entry creates a
	 *          separator.
	 */
	public QrPanel withActions(Action... menuActions) {
		if (menuActions != null) {
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
			menu = new JPopupMenu();
			for (Action act : menuActions) {
				if (act == null) {
					menu.add(new JSeparator());
				}
				else {
					menu.add(act);
				}
			}
		}
		return this;
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (toggle) {
				menu.setVisible(false);
			}
			else {
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
			toggle = !toggle;
		}
	}

	/**
	 * Create a new panel with standard size.
	 */
	public QrPanel() {
		this(200);
	}

	/**
	 * Change the contents of the panel
	 * 
	 * @param content
	 *          string to render. A string that cannot be rendered (empty, null,
	 *          too long) will result in a blank panel.
	 */
	public void setContentString(String content) {
		contentString = content;
		try {
			BitMatrix byteMatrix = qrCodeWriter.encode(content,
					BarcodeFormat.QR_CODE, size, size, hintMap);
			int size = byteMatrix.getWidth();

			image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			WritableRaster raster = image.getRaster();

			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if (byteMatrix.get(i, j)) {
						raster.setPixel(i, j, PIX);
					}
					else {
						raster.setPixel(i, j, BACK);
					}
				}
			}
		}
		catch (Exception e) {
			image = BLANK;
		}
		repaint();
	}

	/**
	 * Query the image
	 * 
	 * @return the currently showing code.
	 */
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public Dimension getPreferredSize() {
		Insets in = getInsets();
		return new Dimension(size + in.left + in.right + 5, size + in.top
				+ in.bottom + 5);
	}

	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		// Draw the QR code centered; Use a bitshift to divide by 2 (potentially
		// faster).
		int x = (getWidth() >> 1) - (size >> 1);
		int y = (getHeight() >> 1) - (size >> 1);
		gr.drawImage(image, x, y, size, size, null);
	}

	public String getContentString() {
		return contentString;
	}

}
