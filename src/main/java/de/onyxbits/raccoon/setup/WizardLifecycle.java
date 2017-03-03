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
package de.onyxbits.raccoon.setup;

import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.gplay.PlayProfile;
import de.onyxbits.raccoon.gplay.PlayProfileDao;
import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.DialogBuilder;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.Lifecycle;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.AdapterBuilder;
import de.onyxbits.weave.swing.BrowseAction;
import de.onyxbits.weave.swing.WindowBuilder;

/**
 * The life cycle of the Setup wizard.
 * 
 * @author patrick
 * 
 */
public class WizardLifecycle implements ActionListener, Lifecycle {

	/**
	 * Intent action for switching between config screens. The extra must be the
	 * classname of the builder of the desired card.
	 */
	public static final String SHOW = "show";

	public static final String FINISH = "finish";

	private CardLayout cardLayout;
	private JPanel panel;
	private JButton next;
	private JButton previous;
	private WizardBuilder[] builders;
	private WizardBuilder active;
	private URI helpurl;

	private DatabaseManager databaseManager;
	private String edit;

	/**
	 * Add or edit a {@link PlayProfileDao}
	 * 
	 * @param dbm
	 *          database connection
	 * @param edit
	 *          alias of the profile to edit or null to create a profile from
	 *          scratch.
	 */
	public WizardLifecycle(DatabaseManager dbm, String edit) {
		this.databaseManager = dbm;
		this.edit = edit;
	}

	@Override
	public void onSetup(Globals globals) {
		globals.put(new Layout());
		globals.put(databaseManager);
		globals
				.put(new ActionLocalizer("de/onyxbits/raccoon/setup/messages", null));
		PlayProfile pp = new PlayProfile();

		PlayProfile existing = databaseManager.get(PlayProfileDao.class).get(edit);
		if (existing != null) {
			pp.setAlias(existing.getAlias());
			pp.setUser(existing.getUser());
			pp.setAgent(existing.getAgent());
			pp.setGsfId(existing.getGsfId());
			pp.setProxyAddress(existing.getProxyAddress());
			pp.setProxyPort(existing.getProxyPort());
			pp.setProxyUser(existing.getProxyUser());
			pp.setProxyPassword(existing.getProxyPassword());
		}

		globals.put(pp);

		helpurl = Bookmarks.SETUP;
	}

	@Override
	public final Window onCreatePrimaryWindow(Globals globals) {

		panel = new JPanel();
		cardLayout = new CardLayout();
		panel.setLayout(cardLayout);
		WizardBuilder[] tmp = { new AccountLogic(), new LoginLogic(),
				new DeviceLogic(), new UploadLogic(), new ProxyLogic(),
				new MimicLogic(), new AliasLogic() };
		builders = tmp;

		ActionLocalizer al = globals.get(ActionLocalizer.class);
		next = new JButton(al.localize("next"));
		previous = new JButton(al.localize("previous"));
		next.addActionListener(this);
		previous.addActionListener(this);

		for (WizardBuilder logic : builders) {
			panel.add(logic.build(globals), logic.getClass().getName());
		}
		active = builders[0];
		active.onActivate(next, previous);

		DialogBuilder db = new DialogBuilder(new AdapterBuilder(panel))
				.withTitle(Messages.getString("WizardLifecycle.titlestrip.title"))
				.withSubTitle(Messages.getString("WizardLifecycle.titlestrip.subtitle"))
				.withHelp(al.localize(new BrowseAction(helpurl), "helpbutton"))
				.withButtons(new ButtonBarBuilder().add(previous).add(next));

		Window ret = new WindowBuilder(db).withIcons("/icons/appicon.png")
				.withFixedSize().withTitle(Messages.getString("WizardLifecycle.title"))
				.build(globals);
		ret.setAlwaysOnTop(true);
		return ret;
	}

	@Override
	public final void onBusMessage(Globals globals, Object note) {
		StateMessage n = (StateMessage) note;
		if (StateMessage.FINISH.equals(n.action)) {
			onFinish(globals);
			return;
		}

		cardLayout.show(panel, n.id);
		for (WizardBuilder logic : builders) {
			if (n.id.equals(logic.getClass().getName())) {
				active = logic;
				active.onActivate(next, previous);
				break;
			}
		}
	}

	@Override
	public final void actionPerformed(ActionEvent e) {
		if (e.getSource() == next) {
			active.onNext();
		}
		if (e.getSource() == previous) {
			active.onPrevious();
		}
	}

	protected void onFinish(Globals globals) {
		PlayProfile pp = globals.get(PlayProfile.class);
		try {
			PlayProfileDao dao = databaseManager.get(PlayProfileDao.class);
			if (dao.get(pp.getAlias()) == null) {
				dao.add(pp);
			}
			else {
				dao.update(pp);
			}
			dao.set(pp.getAlias());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		globals.get(LifecycleManager.class).getWindow().setVisible(false);
		globals.get(LifecycleManager.class).shutdown();
	}

	@Override
	public void onTeardown(Globals globals) {
	}

	@Override
	public boolean onRequestShutdown(Globals globals) {
		return true;
	}

	@Override
	public Window onCreateSecondaryWindow(Globals globals, String id) {
		return null;
	}

	@Override
	public void onDestroySecondaryWindow(String id) {
	}

}
