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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JTextField;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import de.onyxbits.raccoon.appmgr.AndroidApp;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.raccoon.vfs.AppExpansionMainNode;
import de.onyxbits.raccoon.vfs.AppExpansionPatchNode;
import de.onyxbits.raccoon.vfs.AppInstallerNode;
import de.onyxbits.raccoon.vfs.Layout;
import de.onyxbits.weave.LifecycleManager;

/**
 * A handler that allows for downloading a list of apps via HTTP.
 * 
 * @author patrick
 * 
 */
class AppListHandler extends AbstractHandler {

	private LifecycleManager lm;
	private HashStore<AndroidApp> mappings;
	private String index;
	private List<String> keys;

	private Layout layout;

	public AppListHandler(Layout layout, LifecycleManager lm) {
		if (layout == null) {
			throw new NullPointerException();
		}
		this.layout = layout;
		mappings = new HashStore<AndroidApp>();
		keys = new ArrayList<String>();
		this.lm = lm;
	}

	/**
	 * Set the list of apps for download.
	 * 
	 * @param apps
	 *          the apps that can be downloaded.
	 */
	public String setList(List<AndroidApp> apps) {
		mappings.clear();
		keys.clear();
		for (AndroidApp app : apps) {
			keys.add(mappings.register(app));
		}
		index = mappings.nextKey(20);
		return index;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if (target.equals("/" + index)) {
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			printIndex(response.getWriter());
			baseRequest.setHandled(true);
			return;
		}

		String[] components = target.split("/");
		if (components.length != 3) {
			// components[0] is the empty root path.
			return;
		}

		AndroidApp app = mappings.lookup(components[1]);
		if (app == null) {
			return;
		}

		AppInstallerNode ain = new AppInstallerNode(layout, app.getPackageName(),
				app.getVersionCode());
		AppExpansionPatchNode patch = new AppExpansionPatchNode(layout,
				app.getPackageName(), app.getPatchVersion());
		AppExpansionMainNode main = new AppExpansionMainNode(layout,
				app.getPackageName(), app.getVersionCode());

		if (ain.toIcon().getFileName().equals(components[2])) {
			response.setContentType("image/png");
			response.setStatus(HttpServletResponse.SC_OK);
			OutputStream out = response.getOutputStream();
			FileInputStream in = new FileInputStream(ain.toIcon().resolve());
			try {
				IOUtils.copy(in, out);
			}
			catch (IOException e) {
				throw e;
			}
			in.close();
			out.flush();
			baseRequest.setHandled(true);
			return;
		}
		
		Object obj = getServer().getAttribute(Traits.class.getName());
		if (obj instanceof Traits) {
			if (!((Traits) obj).isAvailable("4.0.x")) {
				lm.sendBusMessage(new JTextField(Messages.getString(getClass()
						.getSimpleName() + ".about")));
				response.setContentType("text/html");
				baseRequest.setHandled(true);
				return;
			}
		}

		if (components[2].equals(ain.getFileName())) {
			File file = ain.resolve();
			response.setHeader("Content-Length", new Long(file.length()).toString());

			response.setContentType("application/vnd.android.package-archive");
			String s = MessageFormat.format(Messages.getString("destination"),
					baseRequest.getRemoteAddr());
			NetWorker nw = new NetWorker(file, response, s);
			lm.sendBusMessage(nw);
			blockTillTransferred(nw);
			baseRequest.setHandled(true);
			return;
		}

		if (components[2].equals(main.getFileName())) {
			File file = main.resolve();
			response.setHeader("Content-Length", new Long(file.length()).toString());

			response.setContentType("application/octet-stream");
			String s = MessageFormat.format(Messages.getString("destination"),
					baseRequest.getRemoteAddr());
			NetWorker nw = new NetWorker(file, response, s);
			lm.sendBusMessage(nw);
			blockTillTransferred(nw);
			baseRequest.setHandled(true);
			return;
		}

		if (components[2].equals(patch.getFileName())) {
			File file = main.resolve();
			response.setHeader("Content-Length", new Long(file.length()).toString());

			response.setContentType("application/octet-stream");
			String s = MessageFormat.format(Messages.getString("destination"),
					baseRequest.getRemoteAddr());
			NetWorker nw = new NetWorker(file, response, s);
			lm.sendBusMessage(nw);
			blockTillTransferred(nw);
			baseRequest.setHandled(true);
			return;
		}
	}
	
	private void blockTillTransferred(NetWorker nw) {
		synchronized (nw) {
			try {
				while (nw.isPending()) {
					nw.wait();
				}
			}
			catch (InterruptedException e) {
			}
		}
	}

	private void printIndex(PrintWriter out) {
		out.println("<html><head><link rel=\"stylesheet\" href=\""
				+ ServerManager.RSRCPATH
				+ "/style.css\"></head><body><table class=\"outerlist\" align=\"center\" >");
		for (String key : keys) {
			AndroidApp app = mappings.lookup(key);
			printListItem(out, app, key);
		}

		if (keys.size() == 0) {
			out.println("<tr><td>" + Messages.getString("noapps") + "</td></tr>");
		}
		out.println("</table></body></html>");
	}

	private void printListItem(PrintWriter out, AndroidApp app, String key) {
		AppInstallerNode ain = new AppInstallerNode(layout, app.getPackageName(),
				app.getVersionCode());
		AppExpansionPatchNode patch = new AppExpansionPatchNode(layout,
				app.getPackageName(), app.getPatchVersion());
		AppExpansionMainNode main = new AppExpansionMainNode(layout,
				app.getPackageName(), app.getVersionCode());

		out.println("<tr><td><table width=100% class=\"applistitem\">");
		out.println("<tr><th class=\"apptitlecell\" colspan=\"2\">" + app.getName()
				+ "<span class=\"appversion\"><sup>" + app.getVersion()
				+ "</sup></span></th></tr>");

		out.println("<tr>");
		out.println("<td class=\"appiconcell\"><img class=\"appicon\" src=\"" + key
				+ "/" + ain.toIcon().getFileName() + "\"></td>");
		out.println("<td class=\"appfilelistcell\"><ul>");
		out.println("<li><a href=\"" + key + "/" + ain.getFileName() + "\">"
				+ ain.getFileName() + "</a>");
		if (main.resolve().exists()) {
			out.println("<li><a href=\"" + key + "/" + main.getFileName() + "\">"
					+ main.getFileName() + "</a>");
		}
		if (patch.resolve().exists()) {
			out.println("<li><a href=\"" + key + "/" + patch.getFileName() + "\">"
					+ patch.getFileName() + "</a>");
		}
		out.println("</ul></td></tr>");
		out.println("</table></tr></td>");
	}
}
