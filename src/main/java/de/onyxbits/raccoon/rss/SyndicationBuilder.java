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
package de.onyxbits.raccoon.rss;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * A panel for showing RSS feeds
 * 
 * @author patrick
 * 
 */
public class SyndicationBuilder extends AbstractPanelBuilder {

	private String title;
	private HyperTextPane content;
	private URL source;

	/**
	 * 
	 * @param title
	 * @param source
	 *          rss url
	 */
	public SyndicationBuilder(String title, URL source) {
		this.title = title;
		this.source = source;
	}

	@Override
	protected JPanel assemble() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		content = new HyperTextPane("").withWidth(300).withLinkToolTip();
		content.setMargin(new Insets(5, 5, 5, 5));
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new GridLayout());
		wrapper.add(new JLabel(title, new ImageIcon(getClass().getResource(
				"/icons/famfam/icons/feed.png")), SwingConstants.LEADING));
		wrapper.setBackground(wrapper.getBackground().darker());
		wrapper.setBorder(BorderFactory.createEtchedBorder());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		ret.add(wrapper, gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		JScrollPane scroll = new JScrollPane(content);
		ret.add(scroll, gbc);
		reload();

		return ret;
	}

	public void reload() {
		new FeedWorker(this, source).execute();
	}

	public void onResult(List<FeedItem> items) {
		StringBuilder sb = new StringBuilder("<dl>");
		for (FeedItem item : items) {
			sb.append("<dt style=\"margin:3px;font-weight:700;\"><a href=\"");
			sb.append(item.getLink());
			sb.append("\">");
			if (!"".equals(item.getTitle())) { 
			sb.append(item.getTitle());
			}
			else {
				sb.append(item.getPublished());
			}
			sb.append("</a></dt>");
			sb.append("<dd style=\"margin:5px 15px 10px 15px;\">");
			sb.append(item.getDescription());
			sb.append("</dd>");
		}
		sb.append("</dl>");
		if (!content.getText().equals(sb.toString())) {
			// Avoid screenflicker.
			content.setText(sb.toString());
			content.setCaretPosition(0);
		}
	}

}
