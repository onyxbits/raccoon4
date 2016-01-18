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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.DialogBuilder;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;
import de.onyxbits.weave.swing.AdapterBuilder;
import de.onyxbits.weave.swing.BrowseAction;

/**
 * A dialog that allows the user to submit market:// urls and import the
 * according apps from GPlay.
 * 
 * @author patrick
 * 
 */
public class ImportBuilder extends AbstractPanelBuilder implements
		ActionListener {

	public static final String ID = ImportBuilder.class.getSimpleName();

	private JTextArea list;
	private JButton importUrls;

	@Override
	protected JPanel assemble() {
		ActionLocalizer al = Messages.getLocalizer();
		importUrls = new JButton(al.localize("appdownload"));
		importUrls.addActionListener(this);
		ButtonBarBuilder bbb = new ButtonBarBuilder().add(importUrls);
		list = new JTextArea(15, 45);
		list.setMargin(new Insets(2, 2, 2, 2));
		DialogBuilder db = new DialogBuilder(new AdapterBuilder(new JScrollPane(
				list))).withTitle(Messages.getString(ID + ".title"))
				.withSubTitle(Messages.getString(ID + ".subtitle")).withButtons(bbb);
		return db.build(globals);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String[] urls = list.getText().split("\\n");
		List<String> fetch = new Vector<String>();
		for (String url : urls) {
			if (url.startsWith("#")) {
				// Skip comment
				continue;
			}
			try {
				String[] split = url.split("=");
				if ("market://details?id".equals(split[0])) {
					if (!fetch.contains(split[1]) && split[1].length() > 0) {
						fetch.add(split[1]);
					}
				}
			}
			catch (Exception e1) {
				e1.printStackTrace();
				// Just ignore junk
			}
		}
		new ImportWorker(globals, fetch).execute();
	}

}
