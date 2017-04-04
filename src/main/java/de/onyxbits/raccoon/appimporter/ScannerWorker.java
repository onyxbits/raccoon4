/*
 * Copyright 2017 Patrick Ahlbrecht
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
package de.onyxbits.raccoon.appimporter;

import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;

class ScannerWorker extends SwingWorker<Object, Candidate> {

	public static final String ID = ScannerWorker.class.getSimpleName();

	private File root;
	private int depth;
	private DefaultListModel<Candidate> model;
	private PlainDocument doc;
	private AndroidAppDao dao;
	private int found;

	/**
	 * Scan a directory tree
	 * 
	 * @param root
	 *          root of the tree
	 * @param depth
	 *          maximum depth to descend
	 * @param dao
	 *          for checking if we already know that app.
	 */
	public ScannerWorker(File root, int depth, AndroidAppDao dao) {
		this.root = root;
		this.depth = depth;
		model = new DefaultListModel<Candidate>();
		doc = new PlainDocument();
		this.dao = dao;
	}

	@Override
	protected Object doInBackground() throws Exception {
		descend(root, depth);
		return null;
	}

	private void descend(File dir, int left) {
		File[] files = dir.listFiles();
		if (isCancelled()) {
			return;
		}
		for (File file : files) {
			if (isCancelled()) {
				return;
			}
			if (file.isFile() && file.getName().toLowerCase().endsWith(".apk")) {
				AndroidApp app = null;
				try {
					app = AndroidAppDao.analyze(file);
				}
				catch (Exception e) {
					//e.printStackTrace();
				}
				if (app != null) {
					try {
						if (!dao.isStored(app)) {
							// TODO: Filter out dups?
							found++;
							publish(new Candidate(app, file));
						}
					}
					catch (SQLException e) {
					}
				}
			}
			if (file.isDirectory() && left > 0) {
				publish(new Candidate(null, file));
				descend(file, left - 1);
			}
		}
	}

	@Override
	protected void process(List<Candidate> chunks) {
		for (Candidate ic : chunks) {
			if (ic.app != null) {
				model.addElement(ic);
			}
			else {
				try {
					doc.remove(0, doc.getLength());
					doc.insertString(0, ic.src.getAbsolutePath(), null);
				}
				catch (BadLocationException e) {
				}
			}
		}
	}

	@Override
	protected void done() {
		try {
			String tmp = "";
			switch (found) {
				case 0: {
					tmp = Messages.getString(ID.concat(".found.0"));
					break;
				}
				case 1: {
					tmp = Messages.getString(ID.concat(".found.1"));
					break;
				}
				default: {
					tmp = MessageFormat.format(Messages.getString(ID.concat(".found.2")),
							found);
				}
			}
			doc.remove(0, doc.getLength());
			doc.insertString(0, tmp, null);
		}
		catch (BadLocationException e) {
		}
	}

	protected ListModel<Candidate> getModel() {
		return model;
	}

	public Document getDocument() {
		return doc;
	}

}
