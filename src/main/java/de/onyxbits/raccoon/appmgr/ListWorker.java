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

import java.net.URI;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import de.onyxbits.raccoon.db.AndroidApp;
import de.onyxbits.raccoon.db.AndroidAppDao;
import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.net.ServerManager;
import de.onyxbits.raccoon.qr.QrPanel;
import de.onyxbits.raccoon.vfs.AppIconNode;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.Globals;

/**
 * A worker for the {@link ListViewBuilder} to populate the list on a background
 * thread.
 * 
 * @author patrick
 * 
 */
class ListWorker extends SwingWorker<List<AndroidApp>, ListItemBuilder> {

	private ListViewBuilder owner;
	private Layout layout;
	private Globals globals;
	private QrPanel transfer;

	public ListWorker(ListViewBuilder owner, QrPanel transfer, Globals globals) {
		this.layout = globals.get(Layout.class);
		this.owner = owner;
		this.globals = globals;
		this.transfer = transfer;
	}

	@Override
	protected List<AndroidApp> doInBackground() throws Exception {
		AndroidAppDao dao = globals.get(DatabaseManager.class).get(
				AndroidAppDao.class);
		List<AndroidApp> apps = dao.list();

		for (AndroidApp app : apps) {
			if (isCancelled()) {
				break;
			}
			dao.details(app);
			AppIconNode ain = new AppIconNode(layout, app.getPackageName(),
					app.getVersionCode());
			try {
			publish(new ListItemBuilder(app, ImageIO.read(ain.resolve())));
			}
			catch (IIOException e) {
				publish(new ListItemBuilder(app,null));
			}
		}
		return apps;
	}

	@Override
	public void process(List<ListItemBuilder> apps) {
		if (!isCancelled()) {
			owner.onAdd(apps);
		}
	}

	@Override
	public void done() {
		try {
			ServerManager sm = globals.get(ServerManager.class);
			sm.setAtttribute(Traits.class.getName(), globals.get(Traits.class));
			URI uri = sm.serve(get());
			transfer.setContentString(uri.toString());
		}
		catch (CancellationException e) {
			// No problem.
			return;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
