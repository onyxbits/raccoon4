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
package de.onyxbits.raccoon.gui;

import java.util.HashMap;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * An adapter for showing an app's permission list in a {@link JTree}
 * 
 * @author patrick
 * 
 */
public class PermissionModel extends DefaultTreeModel {

	private PermissionModel(TreeNode root) {
		super(root);
	}

	/**
	 * Create a new model
	 * 
	 * @param permissions
	 *          list of permissions (should be sorted). May be null to create a
	 *          "loading" model.
	 * @return a ready to use model.
	 */
	public static PermissionModel create(List<String> permissions) {
		MutableTreeNode root = new DefaultMutableTreeNode();
		PermissionModel ret = new PermissionModel(root);

		if (permissions == null) {
			String s = Messages
					.getString(ret.getClass().getSimpleName() + ".loading");
			root.insert(new DefaultMutableTreeNode(s), 0);
			return ret;
		}
		if (permissions.size() == 0) {
			String s = Messages.getString(ret.getClass().getSimpleName() + ".noperm");
			root.insert(new DefaultMutableTreeNode(s), 0);
		}
		else {
			HashMap<String, MutableTreeNode> nodes = new HashMap<String, MutableTreeNode>();

			for (String permission : permissions) {
				int pos = permission.lastIndexOf('.');
				char[] chars = permission.toCharArray();
				String g = new String(chars, 0, pos);
				String p = new String(chars, pos + 1, chars.length - pos - 1);
				MutableTreeNode n = nodes.get(g);
				if (n == null) {
					n = new DefaultMutableTreeNode(g);
					nodes.put(g, n);
					root.insert(n, root.getChildCount());
				}
				n.insert(new DefaultMutableTreeNode(p), n.getChildCount());
			}
		}
		return ret;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
