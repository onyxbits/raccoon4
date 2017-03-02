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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.gui.HyperTextPane;
import de.onyxbits.raccoon.gui.PermissionModel;
import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.raccoon.vfs.AppInstallerNode;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.ImageLoaderListener;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A panel showing the full description of an app.
 * 
 * @author patrick
 * 
 */
class FullAppDescriptionBuilder extends AbstractPanelBuilder implements
		ActionListener, PlayListener, ImageLoaderListener, HyperlinkListener {

	public static final String ID = FullAppDescriptionBuilder.class
			.getSimpleName();

	private static final String ALLAPPS = "allapps";

	private static URL spinner;

	private JButton download;
	private HyperTextPane about;
	private JTree permissionList;
	private JLabel published;
	private JLabel size;
	private JLabel starRating;
	private JLabel ratingCount;
	private JLabel price;
	private JLabel downloads;
	private DocV2 current;
	private TitleStrip titleStrip;
	private ScreenshotBuilder screenshots;
	private WeakHashMap<String, DocV2> docCache = new WeakHashMap<String, DocV2>();

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == download) {
			if ((e.getModifiers() & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK) {
				System.err.println(current.toString());
				return;
			}
			globals.get(TransferManager.class).schedule(globals,
					new AppDownloadWorker(globals, current), TransferManager.WAN);
			download.setEnabled(false);
		}
	}

	@Override
	protected JPanel assemble() {
		if (spinner == null) {
			spinner = getClass().getResource("/icons/spinner_96.gif");
		}
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		ActionLocalizer al = Messages.getLocalizer();
		titleStrip = new TitleStrip();
		download = new JButton(al.localize("appdownload"));
		download.addActionListener(this);
		about = new HyperTextPane("").withWidth(350);
		about.addHyperlinkListener(this);
		permissionList = new JTree();
		permissionList.setRootVisible(false);
		permissionList.setBorder(new EmptyBorder(2, 2, 2, 2));
		screenshots = new ScreenshotBuilder();
		JScrollPane screenShotScroll = new JScrollPane(screenshots.build(globals));
		screenShotScroll.getVerticalScrollBar().setUnitIncrement(25);
		JTabbedPane details = new JTabbedPane(SwingConstants.BOTTOM);
		details.add(Messages.getString(ID + ".about"), new JScrollPane(about));
		details.add(Messages.getString(ID + ".permissions"), new JScrollPane(
				permissionList));
		details.add(Messages.getString(ID + ".screenshots"), screenShotScroll);
		details.addChangeListener(screenshots);

		published = new JLabel("Maximum Width",
				loadIcon("/icons/famfam/icons/calendar.png"), SwingConstants.LEFT);
		Dimension max = published.getPreferredSize();
		published.setPreferredSize(max);
		size = new JLabel(loadIcon("/icons/famfam/icons/package.png"),
				SwingConstants.LEFT);
		size.setPreferredSize(max);
		price = new JLabel(loadIcon("/icons/famfam/icons/money.png"),
				SwingConstants.LEFT);
		price.setPreferredSize(max);
		downloads = new JLabel(loadIcon("/icons/famfam/icons/arrow_down.png"),
				SwingConstants.LEFT);
		downloads.setPreferredSize(max);
		starRating = new JLabel(loadIcon("/icons/famfam/icons/star.png"),
				SwingConstants.LEFT);
		starRating.setPreferredSize(max);
		ratingCount = new JLabel(loadIcon("/icons/famfam/icons/user.png"),
				SwingConstants.LEFT);
		ratingCount.setPreferredSize(max);

		JPanel stats = new JPanel();
		stats.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		stats.add(size, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		stats.add(published, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		stats.add(starRating, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		stats.add(ratingCount, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		stats.add(price, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1;
		stats.add(downloads, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel.add(titleStrip, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;

		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets.bottom = 15;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.top = 10;
		panel.add(stats, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(download, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.insets.bottom = 0;
		gbc.insets.left = 0;
		gbc.insets.right = 0;
		gbc.insets.top = 0;
		gbc.fill = GridBagConstraints.BOTH;

		panel.add(details, gbc);

		globals.get(PlayManager.class).addPlayListener(this);
		return panel;
	}

	private void showBrief(DocV2 doc) {
		titleStrip.setTitle(doc.getTitle());
		titleStrip.setSubTitle(doc.getCreator());
		titleStrip.setIcon(TitleStrip.BLANK);

		starRating.setText(""
				+ new DecimalFormat("#.##").format(doc.getAggregateRating()
						.getStarRating()));
		ratingCount.setText("" + doc.getAggregateRating().getRatingsCount());
		if (doc.getOfferCount() > 0) {
			price.setText(doc.getOffer(0).getFormattedAmount());
		}
		published.setText(doc.getDetails().getAppDetails().getUploadDate());
		downloads.setText(doc.getDetails().getAppDetails().getNumDownloads());

		size.setText(TransferManager.humanReadableByteCount(doc.getDetails()
				.getAppDetails().getInstallationSize(), false));
		AppInstallerNode ain = new AppInstallerNode(globals.get(Layout.class),
				doc.getBackendDocid(), doc.getDetails().getAppDetails()
						.getVersionCode());
		download.setEnabled(!ain.resolve().exists());
		if (download.isEnabled()) {
			download.setToolTipText(null);
		}
		else {
			download.setToolTipText(Messages.getString(ID + ".alreadydownloaded"));
		}
	}

	private void showLoading() {
		about.setText("<img src=\"" + spinner + "\"/>");
		permissionList.setModel(PermissionModel.create(null));
		screenshots.load(Collections.<String> emptyList());
	}

	private void showFull(DocV2 doc) {

		List<String> permissions = doc.getDetails().getAppDetails()
				.getPermissionList();
		permissionList.setModel(PermissionModel.create(permissions));
		for (int i = 0; i < permissionList.getRowCount(); i++) {
			permissionList.expandRow(i);
		}

		screenshots.load(DocUtil.getScreenShots(doc));

		String devWeb = doc.getDetails().getAppDetails().getDeveloperWebsite();
		String devEmail = doc.getDetails().getAppDetails().getDeveloperEmail();
		String play = doc.getShareUrl();
		String video = DocUtil.getVideoUrl(doc);
		StringBuilder sb = new StringBuilder(doc.getDescriptionHtml());
		sb.append("<p><hr><dl><dt>");
		sb.append(doc.getDetails().getAppDetails().getVersionString());
		sb.append("</dt><dd>");
		sb.append(doc.getDetails().getAppDetails().getRecentChangesHtml());
		sb.append("</dd><hr><ul>");
		if (video != null) {
			sb.append("<li> <a href=\"" + video + "\">"
					+ Messages.getString(ID + ".links.video") + "</a>");
		}
		sb.append("<li> <a href=\"" + play + "\">"
				+ Messages.getString(ID + ".links.play") + "</a>");
		if (devWeb != null) {
			sb.append("<li> <a href=\"" + devWeb + "\">"
					+ Messages.getString(ID + ".links.dev.website") + "</a>");
		}
		if (devEmail != null) {
			sb.append("<li><a href=\"mailto:" + devEmail + "\">"
					+ Messages.getString(ID + ".links.dev.email") + "</a>");
		}
		sb.append("<li> <a href=\"");
		sb.append(ALLAPPS);
		sb.append("\">");
		sb.append(Messages.getString(ID + ".links.dev.allapps"));
		sb.append("</a>");

		about.setText(sb.toString());
		about.setCaretPosition(0);
	}

	@Override
	public void onImageReady(String src, Image img) {
		if (src.equals(DocUtil.getAppIconUrl(current))) { // Still the same?
			titleStrip.setIcon(new ImageIcon(img.getScaledInstance(
					TitleStrip.ICONSIZE, TitleStrip.ICONSIZE, Image.SCALE_SMOOTH)));
		}
	}

	private ImageIcon loadIcon(String path) {
		return new ImageIcon(getClass().getResource(path));
	}

	@Override
	public void onAppSearch() {
	}

	@Override
	public void onAppSearchResult(List<DocV2> apps, boolean append) {
	}

	@Override
	public void onAppView(DocV2 app, boolean brief) {
		screenshots.stopLoading();
		showBrief(app);

		if (brief) {
			current = app;
			if (docCache.get(current.getBackendDocid()) == null) {
				new DetailsAppWorker(globals.get(PlayManager.class), globals,
						current.getBackendDocid()).execute();
				showLoading();
			}
			else {
				// Maybe the user switched back to something we already have?
				showFull(docCache.get(current.getBackendDocid()));
			}
		}
		else {
			docCache.put(app.getBackendDocid(), app);
			// Are we still suppose to show the same thing?
			if (app.getBackendDocid().equals(current.getBackendDocid())) {
				showFull(app);
			}
		}
		globals.get(ImageLoaderService.class).request(this,
				DocUtil.getAppIconUrl(app));
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == EventType.ACTIVATED) {
			if (ALLAPPS.equals(e.getDescription())) {
				globals.get(PlayManager.class).searchApps(current.getCreator());
			}
		}
	}
}
