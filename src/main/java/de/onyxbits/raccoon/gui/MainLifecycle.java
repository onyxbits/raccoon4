package de.onyxbits.raccoon.gui;

import java.awt.Window;

import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.Main;
import de.onyxbits.raccoon.appmgr.DetailsViewBuilder;
import de.onyxbits.raccoon.appmgr.GroupEditorBuilder;
import de.onyxbits.raccoon.appmgr.MyAppsViewBuilder;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.DatasetListenerProxy;
import de.onyxbits.raccoon.db.VariableDao;
import de.onyxbits.raccoon.gplay.PlayManager;
import de.onyxbits.raccoon.gplay.PlayProfileDao;
import de.onyxbits.raccoon.gplay.PlayStoreViewBuilder;
import de.onyxbits.raccoon.gplay.ImportBuilder;
import de.onyxbits.raccoon.gplay.ManualDownloadBuilder;
import de.onyxbits.raccoon.gplay.ProfilesMenuBuilder;
import de.onyxbits.raccoon.gplay.UpdateAppAction;
import de.onyxbits.raccoon.net.NetWorker;
import de.onyxbits.raccoon.net.ServerManager;
import de.onyxbits.raccoon.ptools.BridgeManager;
import de.onyxbits.raccoon.ptools.PushUrlAction;
import de.onyxbits.raccoon.ptools.ScreenshotAction;
import de.onyxbits.raccoon.qr.QrToolBuilder;
import de.onyxbits.raccoon.qr.ShareToolBuilder;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.raccoon.transfer.TransferViewBuilder;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.GlobalsFactory;
import de.onyxbits.weave.Lifecycle;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.BrowseAction;
import de.onyxbits.weave.swing.MenuBarBuilder;
import de.onyxbits.weave.swing.WindowBuilder;
import de.onyxbits.weave.util.BusMultiplexer;
import de.onyxbits.weave.util.Version;

/**
 * The main application GUI lifecycle.
 * 
 * @author patrick
 * 
 */
public final class MainLifecycle implements Lifecycle, GlobalsFactory {

	private Object[] dependencies;

	/**
	 * Create a new Lifecycle.
	 * 
	 * @param dependencies
	 *          objects to put into the {@link Globals} storage:
	 *          {@link DatabaseManager}, {@link ServerManager},
	 *          {@link BridgeManager}, {@link Layout}, {@link PlayManager}
	 */
	public MainLifecycle(Object... dependencies) {
		this.dependencies = dependencies;
	}

	public void onBusMessage(Globals globals, Object message) {
		if (message instanceof NetWorker) {
			globals.get(TransferManager.class).schedule(globals, (NetWorker) message,
					TransferManager.LAN);
		}
		if (message instanceof JTextComponent) {
			globals.get(LifecycleManager.class).getWindow(GrantBuilder.ID)
					.setVisible(true);
		}

		globals.get(BusMultiplexer.class).broadcast(globals, message);
	}

	@Override
	public boolean onRequestShutdown(Globals globals) {
		TransferManager tm = globals.get(TransferManager.class);

		if (!tm.isIdle()) {
			int ret = JOptionPane.showConfirmDialog(
					globals.get(LifecycleManager.class).getWindow(),
					Messages.getString("MainLifecycle.confirm_exit.message"),
					Messages.getString("MainLifecycle.confirm_exit.title"),
					JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.NO_OPTION) {
				return false;
			}
			tm.cancelAll();
		}

		return true;
	}

	@Override
	public Object onCreate(Globals globals, Class<?> requested) {
		// Special cases with dependencies of their own
		if (requested.equals(WindowTogglers.class)) {
			return new WindowTogglers(globals.get(LifecycleManager.class));
		}

		if (requested.equals(Traits.class)) {
			return new Traits(globals.get(DatabaseManager.class).get(
					VariableDao.class));
		}

		if (requested.equals(Version.class)) {
			try {
				return new Version(Main.class.getPackage().getImplementationVersion());
			}
			catch (Exception e) {
				return new Version(0, 0, 0);
			}
		}

		if (requested.equals(PlayManager.class)) {
			return new PlayManager(globals.get(DatabaseManager.class));
		}

		// Things with no external dependencies.
		try {
			return requested.newInstance();
		}
		catch (Exception e) {
			// This is a bug!
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onSetup(Globals globals) {
		globals.put(dependencies);
		globals.setFactory(this);
	}

	@Override
	public Window onCreatePrimaryWindow(Globals globals) {
		WindowTogglers wt = globals.get(WindowTogglers.class);
		PushUrlAction pua = new PushUrlAction(globals);
		ScreenshotAction ssa = new ScreenshotAction(globals);
		UpdateAppAction uaa = new UpdateAppAction(globals);
		PlayProfileDao dao = globals.get(DatabaseManager.class).get(
				PlayProfileDao.class);
		dao.subscribe(new DatasetListenerProxy(uaa));
		dao.subscribe(new DatasetListenerProxy(wt));

		BridgeManager bridgeManager = globals.get(BridgeManager.class);
		bridgeManager.addBridgeListener(ssa);
		bridgeManager.addBridgeListener(pua);
		ProfilesMenuBuilder pmb = new ProfilesMenuBuilder();
		Traits traits = globals.get(Traits.class);
		wt.grants.setEnabled(!traits.isMaxed() || traits.isTrial());

		MenuBarBuilder mbb = new MenuBarBuilder()
				.withLocalizer(Messages.getLocalizer())
				.withMenus("filemenu", "marketmenu", "devicemenu", "viewmenu",
						"helpmenu")
				.addItem("filemenu/share", wt.share)
				.addItem("filemenu/importapps", new ImportAppAction(globals))
				.addSeparator("filemenu/---1")
				.addItem("filemenu/quit", new QuitAction(globals))
				.add("marketmenu/profiles", pmb.assemble(globals))
				.addItem("marketmenu/update", uaa)
				.addSeparator("marketmenu/---1")
				.addCheckbox("marketmenu/manualdownload", wt.manualdownload)
				.addCheckbox("marketmenu/importurls", wt.marketimport)
				.addItem("devicemenu/pushurl", pua)
				.addItem("devicemenu/screenshot", ssa)
				.addCheckbox("viewmenu/myapps", wt.myApps)
				.addCheckbox("viewmenu/qrtool", wt.qrtool)
				.addCheckbox("viewmenu/transfers", wt.transfers)
				.addItem("helpmenu/handbook", new BrowseAction(Bookmarks.HANDBOOK))
				.addItem("helpmenu/featurelist",
						new BrowseAction(Bookmarks.FEATURELIST))
				.addSeparator("helpmenu/---1")
				.addCheckbox("helpmenu/grants", wt.grants);

		Window ret = new WindowBuilder(
				new PlayStoreViewBuilder().withBorder(new EmptyBorder(10, 10, 10, 10)))
				.withTitle(Messages.getString("MainLifecycle.title")).withMenu(mbb)
				.withSize(1200, 768).withIcons("/icons/appicon.png").build(globals);
		ret.addWindowListener(new PostWindowSetup(globals));
		return ret;
	}

	@Override
	public Window onCreateSecondaryWindow(Globals globals, String id) {
		AbstractPanelBuilder builder = null;
		Window primary = globals.get(LifecycleManager.class).getWindow();

		if (id.equals(TransferViewBuilder.ID)) {
			builder = globals.get(TransferViewBuilder.class);
		}

		if (id.equals(ManualDownloadBuilder.ID)) {
			builder = globals.get(ManualDownloadBuilder.class);
		}

		if (id.equals(GroupEditorBuilder.ID)) {
			builder = globals.get(GroupEditorBuilder.class);
		}

		if (id.equals(DetailsViewBuilder.ID)) {
			builder = globals.get(DetailsViewBuilder.class);
		}

		if (id.equals(MyAppsViewBuilder.ID)) {
			builder = globals.get(MyAppsViewBuilder.class);
		}

		if (id.equals(GrantBuilder.ID)) {
			builder = globals.get(GrantBuilder.class);
		}

		if (id.equals(ImportBuilder.ID)) {
			builder = globals.get(ImportBuilder.class);
		}

		if (id.equals(QrToolBuilder.ID)) {
			builder = globals.get(QrToolBuilder.class);
		}

		if (id.equals(ShareToolBuilder.ID)) {
			builder = globals.get(ShareToolBuilder.class);
		}

		Window ret = new WindowBuilder(builder).withFixedSize().withOwner(primary)
				.withCenter(primary)
				.withTitle(Messages.getString("MainLifecycle.title." + id))
				.withIcons("/icons/appicon.png").build(globals);
		CloseTool.bindTo(globals, ret);
		return ret;
	}

	@Override
	public void onDestroySecondaryWindow(String id) {
	}

	@Override
	public void onTeardown(Globals globals) {
	}
}
