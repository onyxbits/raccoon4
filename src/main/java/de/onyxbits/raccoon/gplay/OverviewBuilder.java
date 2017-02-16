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
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.db.DatabaseManager;
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
import de.onyxbits.weave.swing.BrowseAction;
import de.onyxbits.weave.util.Version;

final class OverviewBuilder extends AbstractPanelBuilder implements
		BridgeListener, PlayListener, HyperlinkListener {

	private static final String ID = OverviewBuilder.class.getSimpleName();
	private static final String INSTALL = "install://platformtools";
	private TitleStrip titleStrip;

	private JPanel versionPanel;
	private JPanel adbPanel;
	private Border border;

	@Override
	protected JPanel assemble() {
		border = new EmptyBorder(10, 5, 5, 5);
		titleStrip = new TitleStrip("", "", TitleStrip.BLANK);
		URL feed = null;
		try {
			feed = Bookmarks.SHOUTBOXFEED.toURL();
		}
		catch (MalformedURLException e) {
		}
		AbstractPanelBuilder shouts = new SyndicationBuilder(Messages.getString(ID
				+ ".shoutfeed"), feed).withBorder(border);
		titleStrip.setSubTitle(Messages.getString(ID + ".waitadb"));

		versionPanel = new JPanel(false);
		versionPanel.setVisible(false);
		adbPanel = new JPanel(false);
		adbPanel.setVisible(false);

		JComponent plugPanel;
		if (showPlug()) {
			InfoBuilder plug = new InfoBuilder(Messages.getString(ID + ".plug.title"));
			plugPanel = plug.build(globals);
			plug.setInfo(MessageFormat.format(
					Messages.getString(ID + ".plug.message"), Bookmarks.ORDER));
			plugPanel.setBorder(border);
		}
		else {
			plugPanel = Box.createVerticalBox();
		}

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		ret.add(titleStrip, gbc);

		gbc.gridy++;
		ret.add(plugPanel, gbc);

		gbc.gridy++;
		ret.add(versionPanel, gbc);

		gbc.gridy++;
		ret.add(adbPanel, gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		ret.add(shouts.build(globals), gbc);

		PlayManager pm = globals.get(PlayManager.class);
		setTitle(pm);
		pm.addPlayListener(this);

		globals.get(BridgeManager.class).addBridgeListener(this);
		new VersionWorker(this).execute();
		return ret;
	}

	public void onVersion(Version latest) {
		Version current = globals.get(Version.class);
		if (current.compareTo(latest) < 0) {
			String s = MessageFormat.format(Messages.getString(ID + ".newversion"),
					Bookmarks.RELEASES.toString(), latest);
			InfoBuilder version = new InfoBuilder(Messages.getString(ID + ".info"));
			versionPanel.add(version.build(globals));
			version.setInfo(s);
			versionPanel.setBorder(border);
			versionPanel.setLayout(new GridLayout(1, 0, 0, 0));
			versionPanel.setVisible(true);
		}
	}

	@Override
	public void onDeviceActivated(BridgeManager manager) {
		setSubTitle(manager);
	}

	@Override
	public void onConnectivityChange(BridgeManager manager) {
		String s = MessageFormat.format(Messages.getString(ID + ".noadb"), INSTALL,
				Bookmarks.USB_DEBUGGING);

		adbPanel.setVisible(!manager.isRunning());
		if (manager.isRunning()) {
			adbPanel.removeAll();
			adbPanel.setVisible(false);
		}
		else {
			InfoBuilder adb = new InfoBuilder(Messages.getString(ID + ".info"))
					.withHyperLinkListener(this);
			adbPanel.add(adb.build(globals));
			adb.setInfo(s);
			adbPanel.setBorder(border);
			adbPanel.setLayout(new GridLayout(1, 0, 0, 0));
			adbPanel.setVisible(true);
		}
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
			if (!globals.get(Traits.class).isMaxed()) {
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
		else {
			if (e.getEventType() == EventType.ACTIVATED) {
				try {
					BrowseAction.open(e.getURL().toURI());
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
