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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.onyxbits.raccoon.Bookmarks;
import de.onyxbits.raccoon.gui.WindowTogglers;
import de.onyxbits.raccoon.qr.QrToolBuilder;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Export a selection of apps in various text formats.
 * 
 * @author patrick
 * 
 */
public class ExportAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String ID = ExportAction.class.getSimpleName();

	private static final String[] FORMATS = { "HTML", "BBCode", "Markdown",
			"market://" };

	private Globals globals;

	private List<AndroidApp> apps;

	public ExportAction(Globals globals) {
		this.globals = globals;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object sel = JOptionPane.showInputDialog(globals
				.get(LifecycleManager.class).getWindow(MyAppsViewBuilder.ID), Messages
				.getString(ID + ".message"), Messages.getString(ID + ".title"),
				JOptionPane.QUESTION_MESSAGE, null, FORMATS, FORMATS[0]);
		if (sel != null) {
			String tmp = "";
			if (sel.equals(FORMATS[0])) {
				tmp = toHtmlList();
			}
			if (sel.equals(FORMATS[1])) {
				tmp = toBbcodeList();
			}
			if (sel.equals(FORMATS[2])) {
				tmp = toMarkdownList();
			}
			if (sel.equals(FORMATS[3])) {
				tmp = toMarketList();
			}
			globals.get(QrToolBuilder.class).setValue(tmp);
			globals.get(WindowTogglers.class).qrtool.showWindow();
			StringSelection stringSelection = new StringSelection(tmp);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}

	public void setApps(List<AndroidApp> apps) {
		this.apps = apps;
		setEnabled(apps.size() > 0);
	}

	/**
	 * Convert the current selection of apps to a text list
	 * 
	 * @return list in market:// format.
	 */
	public String toMarketList() {
		StringBuilder sb = new StringBuilder();
		for (AndroidApp app : apps) {
			sb.append("market://details?id=");
			sb.append(app.getPackageName());
			sb.append("\n");
		}
		sb.append(MessageFormat.format(Messages.getString(ID + ".credits.market"),
				Bookmarks.BASE.toString()));
		return sb.toString();
	}

	/**
	 * Convert the current selection of apps to a text list
	 * 
	 * @return list in HTML
	 */
	public String toHtmlList() {
		StringBuilder sb = new StringBuilder("<ul>\n");
		for (AndroidApp app : apps) {
			sb.append("<li><a href=\"http://play.google.com/store/apps/details?id=");
			sb.append(app.getPackageName());
			sb.append("\">");
			sb.append(app.getName());
			sb.append("</a>\n");
		}
		sb.append("</ul>\n");
		sb.append(MessageFormat.format(Messages.getString(ID + ".credits.html"),
				Bookmarks.BASE.toString()));
		return sb.toString();
	}

	/**
	 * Convert the current selection of apps to a text list
	 * 
	 * @return list in Markdown
	 */
	public String toMarkdownList() {
		StringBuilder sb = new StringBuilder();
		for (AndroidApp app : apps) {
			sb.append("* [");
			sb.append(app.getName());
			sb.append("](http://play.google.com/store/apps/details?id=");
			sb.append(app.getPackageName());
			sb.append(")\n");
		}
		sb.append("\n");
		sb.append(MessageFormat.format(
				Messages.getString(ID + ".credits.markdown"), Bookmarks.BASE.toString()));
		return sb.toString();
	}

	/**
	 * Convert the current selection of apps to a text list
	 * 
	 * @return list in BBcode
	 */
	public String toBbcodeList() {
		StringBuilder sb = new StringBuilder("[list]\n");
		for (AndroidApp app : apps) {
			sb.append("[*] [url=http://play.google.com/store/apps/details?id=");
			sb.append(app.getPackageName());
			sb.append("]");
			sb.append(app.getName());
			sb.append("[/url]\n");
		}
		sb.append("[/list]\n");
		sb.append(MessageFormat.format(Messages.getString(ID + ".credits.bbcode"),
				Bookmarks.BASE.toString()));
		return sb.toString();
	}
}
