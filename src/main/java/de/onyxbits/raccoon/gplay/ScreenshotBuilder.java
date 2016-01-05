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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ImageLoaderListener;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A Panel for displaying screenshots. This class tightly couples with
 * {@link FullAppDescriptionBuilder}. Screenshots are only loaded while this
 * builders panel is visible in the containing tabbed pane.
 * 
 * @author patrick
 * 
 */
class ScreenshotBuilder extends AbstractPanelBuilder implements
		ImageLoaderListener, ChangeListener {

	private static final Icon SPINNER;

	static {
		SPINNER = new ImageIcon(
				BriefAppDescriptionBuilder.class.getResource("/icons/spinner_96.gif"));
	}
	
	private HashMap<String, JLabel> labels;
	private JPanel container;
	private GridBagConstraints gbc;
	private boolean visible;
	private Vector<String> todo;
	private ImageLoaderService loaderService;
	
	

	public ScreenshotBuilder() {
		loaderService = new ImageLoaderService();
	}

	@Override
	protected JPanel assemble() {
		labels = new HashMap<String, JLabel>();
		container = new JPanel();
		// FIXME don't hardcode this. Figure out how to detect the JEditorPane
		// background color.
		container.setBackground(Color.WHITE);
		container.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets.top = 20;
		gbc.insets.left = 10;
		return container;
	}

	public void load(List<String> urls) {
		loaderService.cancelPending();
		labels.clear();
		container.removeAll();
		todo = new Vector<String>(urls);
		for (String url : urls) {
			JLabel tmp = new JLabel(SPINNER);
			labels.put(url, tmp);
			container.add(tmp, gbc);
			if (visible) {
				schedule();
			}
		}
		container.revalidate();
	}

	@Override
	public void onImageReady(String source, Image img) {
		JLabel label = labels.get(source);
		if (label != null) {
			label.setIcon(new ImageIcon(img));
		}
	}

	private void schedule() {
		for (String url : todo) {
			loaderService.request(this, url);
		}
	}

	/**
	 * Cacnel the image loading.
	 */
	public void stopLoading() {
		loaderService.cancelPending();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane src = (JTabbedPane) e.getSource();
			JScrollPane scroll = (JScrollPane) src.getSelectedComponent();
			if (scroll.getViewport().getComponent(0) == container) {
				visible = true;
				schedule();
			}
			else {
				loaderService.cancelPending();
				visible = false;
			}
		}
	}
}
