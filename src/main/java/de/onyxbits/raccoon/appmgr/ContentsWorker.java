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

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * Reads ZIPed files into a {@link JTree}
 * 
 * @author patrick
 * 
 */
public class ContentsWorker extends SwingWorker<TreeNode, Integer> {

	private JTree tree;
	private File zipfile;

	public ContentsWorker(JTree tree, File zipfile) {
		this.tree = tree;
		this.zipfile = zipfile;
	}

	@Override
	protected TreeNode doInBackground() throws Exception {
		ZipFile zip = new ZipFile(zipfile);
		Enumeration<? extends ZipEntry> entries = zip.entries();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(zipfile.getName());
		Vector<String> names = new Vector<String>();
		while (entries.hasMoreElements()) {
			names.add(entries.nextElement().getName());
		}
		zip.close();
		Collections.sort(names);
		for (String name : names) {
			String[] elements = name.split("/");
			DefaultMutableTreeNode tmp = root;
			for (String element : elements) {
				tmp = getNode(tmp, element);
			}
		}
		return root;
	}

	private DefaultMutableTreeNode getNode(DefaultMutableTreeNode node,
			String path) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) node.getChildAt(i);
			if (path.equals(tn.toString())) {
				return tn;
			}
		}
		DefaultMutableTreeNode ret = new DefaultMutableTreeNode(path);
		node.add(ret);
		return ret;
	}

	@Override
	protected void done() {
		try {
			tree.setModel(new DefaultTreeModel(get()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
