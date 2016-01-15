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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.PlayProfile;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.ptools.BridgeListener;
import de.onyxbits.raccoon.ptools.BridgeManager;
import de.onyxbits.raccoon.ptools.Device;
import de.onyxbits.raccoon.ptools.FetchToolsWorker;
import de.onyxbits.raccoon.rss.SyndicationBuilder;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.raccoon.transfer.TransferWorker;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.util.Version;

final class OverviewBuilder extends AbstractPanelBuilder implements
		BridgeListener, PlayListener, HyperlinkListener {

	private static final String ID = OverviewBuilder.class.getSimpleName();
	private static final String INSTALL = "install://platformtools";
	private TitleStrip titleStrip;

	private InfoBuilder version;
	private InfoBuilder adb;
	private InfoBuilder plug;
	private JPanel versionPanel;
	private JPanel plugPanel;
	private JPanel adbPanel;
	private JPanel panel;

	@Override
	protected JPanel assemble() {
		Border border = new EmptyBorder(10, 5, 5, 5);
		titleStrip = new TitleStrip("", "", TitleStrip.BLANK);
		URL feed = null;
		try {
			feed = Bookmarks.SHOUTBOXFEED.toURL();
		}
		catch (MalformedURLException e) {
		}
		AbstractPanelBuilder shouts = new SyndicationBuilder(Messages.getString(ID
				+ ".shoutfeed"), feed).withBorder(border);
		version = new InfoBuilder(Messages.getString(ID + ".info"));
		versionPanel = version.build(globals);
		versionPanel.setVisible(false);
		versionPanel.setBorder(border);
		titleStrip.setSubTitle(Messages.getString(ID + ".waitadb"));

		adb = new InfoBuilder(Messages.getString(ID + ".info"))
				.withHyperLinkListener(this);
		adbPanel = adb.build(globals);
		adbPanel.setVisible(false);
		adbPanel.setBorder(border);

		plug = new InfoBuilder(Messages.getString(ID + ".plug.title"));
		plugPanel = plug.build(globals);
		plug.setInfo(MessageFormat.format(Messages.getString(ID + ".plug.message"),
				Bookmarks.ORDER));
		plugPanel.setVisible(showPlug());
		plugPanel.setBorder(border);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		panel.add(titleStrip, gbc);

		gbc.gridy++;
		panel.add(plugPanel, gbc);

		gbc.gridy++;
		panel.add(versionPanel, gbc);

		gbc.gridy++;
		panel.add(adbPanel, gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		panel.add(shouts.build(globals), gbc);

		PlayManager pm = globals.get(PlayManager.class);
		setTitle(pm);
		pm.addPlayListener(this);

		globals.get(BridgeManager.class).addBridgeListener(this);
		new VersionWorker(this).execute();
		return panel;
	}

	public void onVersion(Version latest) {
		Version current = globals.get(Version.class);
		if (current.compareTo(latest) < 0) {
			String s = MessageFormat.format(Messages.getString(ID + ".newversion"),
					Bookmarks.RELEASES.toString(), latest);
			version.setInfo(s);
			versionPanel.setVisible(true);
		}
	}

	@Override
	public void onDeviceActivated(BridgeManager manager) {
		setSubTitle(manager);
	}

	@Override
	public void onConnectivityChange(BridgeManager manager) {
		String s = MessageFormat.format(Messages.getString(ID + ".noadb"), INSTALL);
		adb.setInfo(s);
		adbPanel.setVisible(!manager.isRunning());
	}

	private void setSubTitle(BridgeManager m) {
		Device d = m.getActiveDevice();
		if (d == null) {
			titleStrip.setSubTitle(Messages.getString(ID + ".nodevice"));
		}
		else {
			titleStrip.setSubTitle(d.getSerial());
		}
	}

	private boolean showPlug() {
		long now = System.currentTimeMillis();
		long created = Long.parseLong(globals.get(DatabaseManager.class)
				.get(VariableDao.class).getVar(VariableDao.CREATED, "0"));
		// Two weeks should be a reasonable trial time.
		if (now - created > 1000 * 60 * 60 * 24 * 7 * 2) {
			if (!globals.get(Traits.class).isAvailable("4.0.x")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onProfileActivated(PlayManager pm) {
		setTitle(pm);
	}

	private void setTitle(PlayManager pm) {
		PlayProfile pp = pm.getActiveProfile();
		if (pp != null) {
			titleStrip.setTitle(MessageFormat.format(
					Messages.getString(ID + ".welcome"), pp.getAlias()));
		}
	}

	@Override
	public void onAppSearch() {
	}

	@Override
	public void onAppSearchResult(List<DocV2> apps, boolean append) {
	}

	@Override
	public void onAppView(DocV2 app, boolean brief) {
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (INSTALL.equals(e.getDescription())) {
			if (e.getEventType() == EventType.ACTIVATED) {
				TransferWorker w = new FetchToolsWorker(globals);
				globals.get(TransferManager.class).schedule(globals, w,
						TransferManager.WAN);
			}
		}
	}

}
