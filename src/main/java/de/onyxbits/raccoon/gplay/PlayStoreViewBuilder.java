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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.db.DatasetListenerProxy;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.db.VariableEvent;
import de.onyxbits.raccoon.ptools.BridgeListener;
import de.onyxbits.raccoon.ptools.BridgeManager;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ImageLoaderService;
import de.onyxbits.weave.window.Focus;

/**
 * The main interface for browsing Google Play
 * 
 * @author patrick
 * 
 */
public class PlayStoreViewBuilder extends AbstractPanelBuilder implements
		PlayListener, BridgeListener, ActionListener, DatasetListener {

	public static final String ID = PlayStoreViewBuilder.class.getSimpleName();

	private static final String WELCOME = "welcome";
	private static final String RESULTS = "results";
	private static final String NORESULTS = "noresults";

	private AppStoreListBuilder listLogic;
	private JLabel busy;
	private JPanel serp;
	private JPanel sidebar;

	private Icon loader;
	private Icon blankloader;
	private JTextField query;
	private JButton search;

	@Override
	protected JPanel assemble() {
		blankloader = new ImageIcon(new BufferedImage(43, 11,
				BufferedImage.TYPE_INT_ARGB));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		sidebar = new JPanel();
		sidebar.setLayout(new CardLayout());
		sidebar.add(
				new OverviewBuilder().withBorder(BorderFactory.createEtchedBorder())
						.build(globals), WELCOME);
		sidebar.setPreferredSize(new Dimension(450, 700));

		serp = new JPanel();
		serp.setLayout(new CardLayout());
		serp.add(new JPanel(), WELCOME);
		serp.setPreferredSize(new Dimension(550, 700));

		query = new JTextField(20);
		query.setMargin(new Insets(2, 2, 2, 2));

		search = new JButton(Messages.getLocalizer().localize("search"));
		query.addActionListener(this);
		search.addActionListener(this);
		query.requestFocusInWindow();

		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());

		busy = new JLabel(blankloader);
		busy.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 30, 0, 30);
		container.add(busy, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1;
		gbc.insets = new Insets(0, 0, 0, 5);
		container.add(query, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		container.add(search, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 10, 10, 0);
		container.add(new JSeparator(Separator.HORIZONTAL), gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 10, 0, 0);
		container.add(serp, gbc);

		panel.add(container);
		panel.add(sidebar, BorderLayout.WEST);

		globals.get(PlayManager.class).addPlayListener(this);
		globals.get(BridgeManager.class).addBridgeListener(this);
		globals.get(DatabaseManager.class).get(VariableDao.class)
				.addDataSetListener(new DatasetListenerProxy(this));
		globals.get(DatabaseManager.class).get(PlayProfileDao.class)
				.subscribe(new DatasetListenerProxy(this));

		Focus.on(query);
		return panel;
	}

	@Override
	public void onAppSearchResult(List<DocV2> apps, boolean append) {
		busy.setIcon(blankloader);
		CardLayout l = (CardLayout) serp.getLayout();
		if (apps.size() == 0 && append == false) {
			l.show(serp, NORESULTS);
		}
		else {
			l.show(serp, RESULTS);
		}
	}

	@Override
	public void onAppSearch() {
		if (loader == null) {
			// Speedhack: lazily load what's not immediately needed to bring up the
			// GUI ASAP.

			loader = new ImageIcon(getClass().getResource("/icons/loader.gif"));

			listLogic = new AppStoreListBuilder();
			JScrollPane listLogicScroll = new JScrollPane(listLogic.withBorder(
					new EmptyBorder(10, 10, 10, 10)).build(globals));
			listLogicScroll.setBorder(null);
			listLogicScroll.getVerticalScrollBar().setUnitIncrement(32);
			listLogicScroll.getVerticalScrollBar().addAdjustmentListener(listLogic);
			serp.add(new JLabel(Messages.getString(ID + ".noresults"),
					SwingConstants.CENTER), NORESULTS);
			serp.add(listLogicScroll, RESULTS);

		}
		busy.setIcon(loader);
		overview();
	}

	@Override
	public void onAppView(DocV2 app, boolean brief) {
		if (sidebar.getComponentCount() == 1) {
			// Performance hack: Postpone the creation of the details panel till
			// there actually is something to show in order to bring the GUI up
			// faster.
			FullAppDescriptionBuilder fadb = new FullAppDescriptionBuilder();
			sidebar.add(
					fadb.withBorder(BorderFactory.createEtchedBorder()).build(globals),
					RESULTS);
			fadb.onAppView(app, brief);

			// Another hack: Make sure the builder is registered as a PlayListener.
			globals.get(ManualDownloadBuilder.class).onAppView(app, brief);
			globals.get(LifecycleManager.class).getWindow(ManualDownloadBuilder.ID);
		}
		CardLayout l = (CardLayout) sidebar.getLayout();
		l.show(sidebar, RESULTS);
		query.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		globals.get(PlayManager.class).searchApps(query.getText());
		query.requestFocusInWindow();
		query.selectAll();
		globals.get(ImageLoaderService.class).cancelPending();
	}

	@Override
	public void onDeviceActivated(BridgeManager manager) {
		overview();
	}

	@Override
	public void onConnectivityChange(BridgeManager manager) {
		overview();
	}

	/**
	 * Show the overview panel.
	 */
	private void overview() {
		CardLayout l = (CardLayout) sidebar.getLayout();
		l.show(sidebar, WELCOME);
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {

		if (event instanceof VariableEvent) {
			overview();
		}

		if (event instanceof PlayProfileEvent) {
			PlayProfileEvent ppe = (PlayProfileEvent) event;
			if (ppe.isConnection()) {
				boolean a = ppe.isActivation();
				query.setEnabled(a);
				search.setEnabled(a);
			}
		}
	}

}
