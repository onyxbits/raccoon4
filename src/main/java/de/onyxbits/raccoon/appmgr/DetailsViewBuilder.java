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
package de.onyxbits.raccoon.appmgr;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.raccoon.db.DatasetListenerProxy;
import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.PermissionModel;
import de.onyxbits.raccoon.gui.TitleStrip;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.net.ServerManager;
import de.onyxbits.raccoon.ptools.BridgeManager;
import de.onyxbits.raccoon.qr.CopyContentAction;
import de.onyxbits.raccoon.qr.QrPanel;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.AppGroupDao;
import de.onyxbits.raccoon.repo.AppInstallerNode;
import de.onyxbits.raccoon.repo.Layout;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.BrowseAction;
import de.onyxbits.weave.swing.ImageLoaderListener;
import de.onyxbits.weave.swing.ImageLoaderService;

/**
 * A dialog for showing the details of a specific {@link AndroidApp}
 * 
 * @author patrick
 * 
 */
public class DetailsViewBuilder extends AbstractPanelBuilder implements
		ImageLoaderListener, ActionListener, DatasetListener {

	/**
	 * ID for referencing this builder.
	 */
	public static final String ID = DetailsViewBuilder.class.getSimpleName();

	private static final String APP = "app";
	private static final String NOAPP = "noapp";

	private TitleStrip titleStrip;
	private JTree permissions;
	private BrowseAction showFiles;
	private JButton deleteApp;
	private JButton installApp;
	private JButton renameApp;
	private InstallAction installAction;
	private JButton extract;
	private QrPanel transfer;
	private AndroidApp current;
	private GroupListBuilder groupList;
	private JTree contents;
	private ImageLoaderService loaderService;
	private JPanel container;

	@Override
	protected JPanel assemble() {
		ActionLocalizer al = Messages.getLocalizer();
		permissions = new JTree();
		permissions.setRootVisible(false);
		permissions.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));

		titleStrip = new TitleStrip();
		showFiles = new BrowseAction(null);
		al.localize(showFiles, "show");
		installAction = new InstallAction(globals);
		globals.get(BridgeManager.class).addBridgeListener(installAction);
		installApp = new JButton(al.localize(installAction, "install"));
		deleteApp = new JButton(al.localize("delete"));
		extract = new JButton(al.localize("extract"));
		renameApp = new JButton(al.localize("rename"));
		deleteApp.addActionListener(this);
		extract.addActionListener(this);
		renameApp.addActionListener(this);
		transfer = new QrPanel(200);
		transfer.withActions(new CopyContentAction(globals, transfer));
		transfer.setBorder(new TitledBorder(Messages.getString(ID + ".transfer")));
		groupList = new GroupListBuilder();
		contents = new JTree();
		contents.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));

		JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		contentsPanel.add(new JScrollPane(contents), gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets.right = 5;
		gbc.weighty = 0;
		contentsPanel.add(extract, gbc);

		JTabbedPane tabs = new JTabbedPane();
		tabs.setPreferredSize(new Dimension(400, 500));
		tabs.add(new JScrollPane(groupList.build(globals)),
				Messages.getString(ID + ".groups"));
		tabs.add(new JScrollPane(permissions),
				Messages.getString(ID + ".permissions"));
		tabs.add(contentsPanel, Messages.getString(ID + ".contents"));

		JPanel actions = new ButtonBarBuilder().addButton(showFiles)
				.add(installApp).add(renameApp)
				.add(deleteApp)
				// .addButton(recommendApp)
				.withVerticalAlignment()
				.withBorder(new TitledBorder(Messages.getString(ID + ".actions")))
				.build(globals);

		JPanel appPanel = new JPanel();
		appPanel.setLayout(new GridBagLayout());

		container = new JPanel();
		container.setLayout(new CardLayout());

		container.add(new JLabel(Messages.getString(ID + ".deleted"),
				SwingConstants.CENTER), NOAPP);
		container.add(appPanel, APP);

		gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets.top = 10;
		gbc.insets.left = 5;
		appPanel.add(actions, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		appPanel.add(transfer, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		appPanel.add(tabs, gbc);

		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ret.add(titleStrip, gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		ret.add(container, gbc);

		loaderService = new ImageLoaderService();

		globals.get(DatabaseManager.class).get(AppGroupDao.class)
				.addDataSetListener(new DatasetListenerProxy(this));
		globals.get(DatabaseManager.class).get(AndroidAppDao.class)
				.addDataSetListener(new DatasetListenerProxy(this));
		return ret;
	}

	/**
	 * Put an app on display. The window must already exist.
	 * 
	 * @param app
	 *          the app to show.
	 */
	public void setApp(AndroidApp app) {
		AppInstallerNode ain = new AppInstallerNode(globals.get(Layout.class),
				app.getPackageName(), app.getVersionCode());
		CardLayout l = (CardLayout) container.getLayout();
		if (ain.resolve().exists()) {
			setContent(app);
			l.show(container, APP);
		}
		else {
			l.show(container, NOAPP);
		}
	}

	private void setContent(AndroidApp app) {
		current = app;
		installAction.setApps(current);
		try {
			globals.get(DatabaseManager.class).get(AndroidAppDao.class)
					.details(current);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		titleStrip.setTitle(current.getName());
		titleStrip.setSubTitle(current.getVersion());
		Layout layout = globals.get(Layout.class);
		AppInstallerNode ain = new AppInstallerNode(layout,
				current.getPackageName(), current.getVersionCode());
		showFiles.setLocation(ain.toIcon().resolve().getParentFile().toURI());
		loaderService.request(this, ain.toIcon().resolve().toURI().toString());
		permissions.setModel(PermissionModel.create(current.getUsesPermissions()));
		for (int i = 0; i < permissions.getRowCount(); i++) {
			permissions.expandRow(i);
		}

		// description.setText(current.getDescription());
		new ContentsWorker(contents, ain.resolve()).execute();
		groupList.setApp(current);
		ServerManager sm = globals.get(ServerManager.class);
		sm.setAtttribute(Traits.class.getName(), globals.get(Traits.class));
		String location = sm.serve(current).toString();
		transfer.setContentString(location);
	}

	@Override
	public void onImageReady(String source, Image img) {
		titleStrip.setIcon(new ImageIcon(img.getScaledInstance(TitleStrip.ICONSIZE,
				TitleStrip.ICONSIZE, Image.SCALE_SMOOTH)));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == deleteApp) {
			doDelete();
		}
		if (src == renameApp) {
			doRename();
		}
		if (src == extract) {
			if (globals.get(Traits.class).isAvailable("4.0.x")) {
				doExtract(true);
			}
			else {
				globals.get(LifecycleManager.class).sendBusMessage(new JTextField());
			}
		}
	}

	private void doRename() {
		Window window = SwingUtilities.getWindowAncestor(container);
		String ret = JOptionPane.showInputDialog(window,
				Messages.getString(ID + ".rename.message"),
				Messages.getString(ID + ".rename.title"), JOptionPane.QUESTION_MESSAGE);
		if (ret != null && ret.length() > 0 && !ret.equals(current.getName())) {
			AndroidAppDao dao = globals.get(DatabaseManager.class).get(
					AndroidAppDao.class);
			current.setName(ret);
			titleStrip.setTitle(current.getName());
			try {
				dao.saveOrUpdate(current);
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void doExtract(boolean all) {
		Window window = SwingUtilities.getWindowAncestor(container);
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int res = jfc.showSaveDialog(window);
		if (res == JFileChooser.APPROVE_OPTION) {
			Layout layout = globals.get(Layout.class);
			AppInstallerNode ain = new AppInstallerNode(layout,
					current.getPackageName(), current.getVersionCode());
			new ExtractWorker(ain.resolve(), jfc.getSelectedFile(), null).execute();
		}
	}

	private void doDelete() {
		Window window = SwingUtilities.getWindowAncestor(container);
		int ret = JOptionPane.showConfirmDialog(window,
				Messages.getString(ID + ".delete.message"),
				Messages.getString(ID + ".delete.title"), JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION && current != null) {
			try {
				Layout layout = globals.get(Layout.class);
				globals.get(DatabaseManager.class).get(AndroidAppDao.class)
						.delete(layout, current);

				globals.get(MyAppsViewBuilder.class).reloadList();
				globals.get(LifecycleManager.class).hideWindow(ID);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {
		if (current != null) {
			AppInstallerNode ain = new AppInstallerNode(globals.get(Layout.class),
					current.getPackageName(), current.getVersionCode());
			if (!ain.resolve().exists()) {
				CardLayout l = (CardLayout) container.getLayout();
				l.show(container, NOAPP);
				// Slightly redundant:
				globals.get(LifecycleManager.class).hideWindow(ID);
			}
		}
		groupList.reload();
	}

}
