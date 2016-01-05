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

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkListener;

import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * Shown in the {@link OverviewBuilder}. Provides various status messages.
 * 
 * @author patrick
 * 
 */
class InfoBuilder extends AbstractPanelBuilder {

	private HyperTextPane content;
	private String title;
	private HyperlinkListener listener;

	public InfoBuilder(String title) {
		this.title = title;
	}

	public InfoBuilder withHyperLinkListener(HyperlinkListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	protected JPanel assemble() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		content = new HyperTextPane("-").withTransparency();
		if (listener != null) {
			content.addHyperlinkListener(listener);
		}
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new GridLayout());
		wrapper.add(new JLabel(title, new ImageIcon(getClass().getResource(
				"/icons/famfam/icons/bell.png")), SwingConstants.LEADING));
		wrapper.setBackground(wrapper.getBackground().darker());
		wrapper.setBorder(BorderFactory.createEtchedBorder());

		ret.add(wrapper);
		ret.add(content);
		return ret;
	}

	/**
	 * Show an info mesage
	 * 
	 * @param info
	 *          what to display
	 */
	public void setInfo(String info) {
		content.setText(info);
		content.withWidth(content.getPreferredSize().width);
	}

}
