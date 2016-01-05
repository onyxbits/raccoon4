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
package de.onyxbits.raccoon.net;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JTextField;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.transfer.TransferManager;
import de.onyxbits.weave.LifecycleManager;

/**
 * A handler that serves a collection of files.
 * 
 * @author patrick
 * 
 */
class FileHandler extends AbstractHandler {

	private LifecycleManager lm;
	private FileNameMap fileNameMap;
	private HashStore<File> mappings;
	private List<String> keys;
	private String index;
	private DateFormat timeFormat;
	private boolean zebra;

	public FileHandler(LifecycleManager lm) {
		this.lm = lm;
		fileNameMap = URLConnection.getFileNameMap();
		mappings = new HashStore<File>();
		keys = new ArrayList<String>();
		timeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM);
	}

	public String setFiles(File... files) {
		mappings.clear();
		keys.clear();
		String tmp = "";
		index = mappings.nextKey(20);
		for (File file : files) {
			tmp = mappings.register(file);
			keys.add(tmp);
		}
		if (files.length == 1) {
			return tmp;
		}
		else {
			return index;
		}
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if (target.equals("/" + index)) {
			// List files
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			list(response.getWriter());
			baseRequest.setHandled(true);
			return;
		}

		Object obj = getServer().getAttribute(Traits.class.getName());
		if (obj instanceof Traits) {
			if (!((Traits) obj).isAvailable("4.0.x")) {
				lm.sendBusMessage(new JTextField(Messages.getString(getClass()
						.getSimpleName() + ".about")));
				baseRequest.setHandled(true);
				return;
			}
		}

		File file = mappings.lookup(target.substring(1));
		if (file != null) {
			// File transfer
			String type = fileNameMap.getContentTypeFor(file.toURI().toString());
			response.setHeader("Content-Length", new Long(file.length()).toString());
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ file.getName() + "\"");

			response.setContentType(type);
			String s = MessageFormat.format(Messages.getString("destination"),
					baseRequest.getRemoteAddr());
			NetWorker nw = new NetWorker(file, response, s);
			lm.sendBusMessage(nw);
			synchronized (nw) {
				try {
					while (nw.isPending()) {
						nw.wait();
					}
				}
				catch (InterruptedException e) {
				}
			}
			baseRequest.setHandled(true);
		}
	}

	private void list(PrintWriter out) {
		out.write("<html><link rel=\"icon\" type=\"image/png\" href=\""
				+ ServerManager.RSRCPATH
				+ "favicon.png\"><link rel=\"stylesheet\" href=\""
				+ ServerManager.RSRCPATH
				+ "/style.css\"><body><table class=\"filelist\">");
		for (String key : keys) {
			File f = mappings.lookup(key);
			out.write(makeRow(f, key, null));
		}
		out.write("</table></body></html>");
	}

	private String makeRow(File content, String key, String nameOverride) {
		StringBuilder sb = new StringBuilder();
		if (content != null) {
			sb.append("<tr ");
			if (zebra) {
				sb.append("class=\"evenrow\">");
			}
			else {
				sb.append("class=\"oddrow\">");
			}
			sb.append("<td>");
			sb.append("<a href=\"");
			sb.append(key);
			sb.append("\">");
			if (content.isDirectory()) {
				sb.append("<img src=\"" + ServerManager.RSRCPATH + "/directory.png\"> ");
			}
			else {
				sb.append("<img src=\"" + ServerManager.RSRCPATH + "/file.png\"> ");
			}
			if (nameOverride != null) {
				sb.append(nameOverride);
			}
			else {
				sb.append(content.getName());
			}
			sb.append("</a>");
			sb.append("</td>");
			sb.append("<td>");
			if (content.isDirectory()) {
				sb.append("-");
			}
			else {
				sb.append(TransferManager.humanReadableByteCount(content.length(), true));
			}
			sb.append("</td>");
			sb.append("<td>");
			sb.append(timeFormat.format(content.lastModified()));
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb.toString();
	}
}
