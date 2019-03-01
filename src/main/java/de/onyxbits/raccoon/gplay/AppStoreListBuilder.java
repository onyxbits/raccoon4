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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * Display search results as a list of {@link BriefAppDescriptionBuilder}S. The
 * panel is suppose to be wrapped in a {@link JScrollPane} for which the builder
 * is subscribed as {@link AdjustmentListener}. Scrolling down results in more
 * entries being loaded.
 * 
 * @author patrick
 * 
 */
class AppStoreListBuilder extends AbstractPanelBuilder implements
		AdjustmentListener, PlayListener {

	/**
	 * Number of entries per result page.
	 */
	protected static final int PAGESIZE = 20;

	private JPanel catalog;
	private boolean loading;

	private boolean padded;

	@Override
	protected JPanel assemble() {
		catalog = new JPanel();
		catalog.setLayout(new GridLayout(0, 3, 5, 30));
		globals.get(PlayManager.class).addPlayListener(this);
		return catalog;
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() instanceof JScrollBar) {
			JScrollBar bar = (JScrollBar) e.getSource();
			if (e.getValue() > (bar.getMaximum() - bar.getModel().getExtent()) * 0.85f) {
				if (!loading && !padded) {
					globals.get(PlayManager.class).moreApps();
				}
			}
		}
	}

	@Override
	public void onAppSearchResult(List<DocV2> apps, boolean append) {
		if (!append) {
			catalog.removeAll();
			catalog.getParent().revalidate();
			if (catalog.getParent() instanceof JScrollPane) {
				((JScrollPane)catalog.getParent()).getVerticalScrollBar().setValue(0);
			}
		}

		padded = false;
		for (DocV2 app : apps) {
			catalog.add(new BriefAppDescriptionBuilder(app).build(globals));
		}
		for (int x = apps.size(); x < PAGESIZE; x++) {
			// Pad if we don't have enough results.
			catalog.add(Box.createGlue());
			padded = true;
		}
		catalog.revalidate();
		if (!append) {
			catalog.getParent().revalidate();
			if (catalog.getParent() instanceof JScrollPane) {
				JScrollBar bar = ((JScrollPane)catalog.getParent()).getVerticalScrollBar();
				bar.setValue(bar.getMinimum());
			}
		}
		loading = false;
	}

	@Override
	public void onAppSearch() {
		loading = true;
	}

	@Override
	public void onAppView(DocV2 app, boolean brief) {
	}

}
