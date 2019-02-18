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
package de.onyxbits.raccoon.transfer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.ImageLoaderListener;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * Create a standard control panel for linking a {@link TransferWorker} in the
 * {@link TransferViewBuilder}.
 * 
 * @author patrick
 * 
 */
public final class TransferPeerBuilder extends AbstractPanelBuilder implements
		ImageLoaderListener {

	public static final String ID = TransferPeerBuilder.class.getSimpleName();

	private static final Icon GENERIC48;

	static {
		GENERIC48 = new ImageIcon(
				TransferPeerBuilder.class.getResource("/icons/generic48.png"));
	}

	protected JLabel icon;
	protected JLabel name;
	protected JLabel channel;
	protected JButton view;
	protected JButton cancel;
	protected JProgressBar progressBar;
	protected TransferWorker owner;

	private ActionListener viewListener;

	public Image iconImage;

	/**
	 * Construct a new builder and personalize it with a title string.
	 * 
	 * @param title
	 *          What to put into the titlebar the icon has to be supplied by a
	 *          {@link ImageLoaderService}. The title describes what is being
	 *          transferred.
	 */
	public TransferPeerBuilder(String title) {
		this.name = new JLabel(title, SwingConstants.LEFT);
		Font font = name.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 4);
		Font italicFont = new Font(font.getFontName(), Font.ITALIC,
				font.getSize() - 2);
		name.setFont(boldFont);
		name.setToolTipText(title);
		this.icon = new JLabel(GENERIC48);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setString(Messages.getString(ID + ".waiting"));
		cancel = new JButton();
		view = new JButton();
		channel = new JLabel();
		channel.setFont(italicFont);
	}

	/**
	 * Add a channel to be shown below the title.
	 * 
	 * @param name
	 *          channel description. this should give the user a clue about where
	 *          something is transferred from/to
	 * @return this reference for chaining
	 */
	public TransferPeerBuilder withChannel(String name) {
		channel.setText(name);
		return this;
	}

	/**
	 * Hide the "view" button
	 * 
	 * @return this reference for chaining.
	 */
	public TransferPeerBuilder withViewAction(ActionListener listener) {
		this.viewListener = listener;
		return this;
	}

	public JPanel assemble() {
		JPanel ret = new JPanel();
		ret = new JPanel();
		ActionLocalizer al = Messages.getLocalizer();
		cancel.setAction(al.localize("cancel"));
		view.setAction(al.localize("view"));

		// A download can finish before this panel is ever shown!
		view.setEnabled(progressBar.getValue() == 100);
		cancel.setEnabled(progressBar.getValue() != 100);

		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		gbc.insets.bottom = 0;
		gbc.insets.right = 10;
		gbc.insets.left = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		ret.add(icon, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 3;
		gbc.insets.bottom = 5;
		gbc.insets.right = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(name, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 3;
		gbc.insets.bottom = 5;
		gbc.insets.right = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(channel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		//gbc.insets.bottom = 0;
		ret.add(progressBar, gbc);

		gbc.gridy = 2;
		gbc.gridx = 2;
		gbc.insets.bottom = 5;
		gbc.insets.left = 10;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		ret.add(cancel, gbc);

		gbc.gridy = 2;
		gbc.gridx = 3;
		gbc.insets.bottom = 5;
		gbc.insets.left = 5;
		gbc.insets.right = 5;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		if (viewListener != null) {
			ret.add(view, gbc);
			view.addActionListener(viewListener);
		}
		else {
			ret.add(Box.createHorizontalStrut(view.getPreferredSize().width), gbc);
		}

		return ret;
	}

	@Override
	public void onImageReady(String source, Image img) {
		iconImage = img;
		icon.setIcon(new ImageIcon(img
				.getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
	}
}
