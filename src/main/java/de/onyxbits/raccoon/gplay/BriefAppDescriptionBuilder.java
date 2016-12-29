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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ImageLoaderListener;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * Shows an apps badge.
 * 
 * @author patrick
 * 
 */
class BriefAppDescriptionBuilder extends AbstractPanelBuilder implements
		ImageLoaderListener, ActionListener {

	private JButton button;
	private DocV2 doc;
	private static final Icon SPINNER;

	static {
		SPINNER = new ImageIcon(
				BriefAppDescriptionBuilder.class.getResource("/icons/spinner_96.gif"));
	}

	public BriefAppDescriptionBuilder(DocV2 doc) {
		this.doc = doc;
	}

	@Override
	protected JPanel assemble() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JLabel appNameLabel = new JLabel(doc.getTitle(), SwingConstants.CENTER);
		Font font = appNameLabel.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 2);
		appNameLabel.setFont(boldFont);
		appNameLabel.setToolTipText(doc.getTitle());
		Dimension tmp = appNameLabel.getPreferredSize();
		tmp.width = 150;
		appNameLabel.setPreferredSize(tmp);

		JLabel vendorNameLabel = new JLabel(doc.getCreator(), SwingConstants.CENTER);
		tmp = vendorNameLabel.getPreferredSize();
		tmp.width = 150;
		vendorNameLabel.setPreferredSize(tmp);
		vendorNameLabel.setToolTipText(doc.getCreator());

		button = new JButton();
		button.addActionListener(this);
		button.setIcon(SPINNER);

		globals.get(ImageLoaderService.class).request(this,
				DocUtil.getAppIconUrl(doc));

		JPanel stars = new StarPanel(5,
				doc.getAggregateRating().getStarRating() / 5);
		DecimalFormat df = new DecimalFormat("#.## \u2605");
		stars.setToolTipText(df.format(doc.getAggregateRating().getStarRating()));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.bottom = 10;
		panel.add(button, gbc);

		gbc.insets.bottom = 0;
		gbc.gridy++;
		panel.add(appNameLabel, gbc);

		gbc.gridy++;
		panel.add(vendorNameLabel, gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.top = 10;
		gbc.insets.left = 15;
		gbc.insets.right = 15;
		gbc.insets.bottom = 15;
		panel.add(stars, gbc);

		return panel;
	}

	@Override
	public void onImageReady(String src, Image img) {
		button.setIcon(new ImageIcon(img.getScaledInstance(SPINNER.getIconWidth(),
				SPINNER.getIconHeight(), Image.SCALE_SMOOTH)));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button) {
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
				globals.get(TransferManager.class).schedule(globals,
						new AppDownloadWorker(globals, doc), TransferManager.WAN);
				return;
			}
			if ((e.getModifiers() & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK) {
				System.err.println(doc.toString());
				return;
			}
			globals.get(PlayManager.class).fireAppView(doc, true);
		}
	}
}
