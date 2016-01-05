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
import java.net.URI;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.FeedItem;
import de.onyxbits.raccoon.db.FeedItemDao;
import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.raccoon.gui.UnavailableBuilder;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.util.BusMessageHandler;
import de.onyxbits.weave.util.BusMultiplexer;

/**
 * A panel for showing RSS feeds
 * 
 * @author patrick
 * 
 */
public class SyndicationBuilder extends AbstractPanelBuilder implements
		BusMessageHandler {

	private String title;
	private HyperTextPane content;
	private URI source;

	/**
	 * 
	 * @param title
	 * @param source
	 *          rss url
	 */
	public SyndicationBuilder(String title, URI source) {
		this.title = title;
		this.source = source;
	}

	@Override
	protected JPanel assemble() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		content = new HyperTextPane(title).withWidth(300);
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
		globals.get(BusMultiplexer.class).subscribe(this);
		reload();

		return ret;
	}

	@Override
	public void onBusMessage(Globals globals, Object message) {
		if (message instanceof FeedTask) {
			reload();
		}
		if (message instanceof JTextComponent) {
			globals.get(UnavailableBuilder.class).setAbout(
					((JTextComponent) message).getText());
			globals.get(LifecycleManager.class).getWindow(UnavailableBuilder.ID)
					.setVisible(true);
		}
	}

	private void reload() {

		List<FeedItem> items = globals.get(DatabaseManager.class)
				.get(FeedItemDao.class).list(source);
		StringBuilder sb = new StringBuilder("<dl>");
		for (FeedItem item : items) {
			sb.append("<dt><a href=\"" + item.getLink() + "\">" + item.getTitle()
					+ "</a></dt>");
			sb.append("<dd>" + item.getDescription() + "<p></dd>");
		}
		sb.append("</dl>");
		if (!content.getText().equals(sb.toString())) {
			// Avoid screenflicker.
			content.setText(sb.toString());
			content.setCaretPosition(0);
		}
	}

}
