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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.Layout;
import de.onyxbits.weave.Lifecycle;
import de.onyxbits.weave.LifecycleManager;

public class ServerManager {

	protected static final String APPCOLLECTIONPATH = "/apps";
	protected static final String APPSINGLEPATH = "/app";
	protected static final String RSRCPATH = "/rsrc";
	protected static final String FILEPATH = "/files";

	private Server server;
	private AppListHandler appListHandler;
	private AppListHandler focusedAppHandler;
	private FileHandler fileHandler;
	private ResourceHandler resourceHandler;
	private Layout layout;
	private Object lock;

	public ServerManager(Layout layout) {
		this.layout = layout;
		this.lock = new Object();
	}

	/**
	 * Make a list of apps available for download.
	 * 
	 * @param apps
	 *          the apps to list
	 * @return the url where the app index may be found.
	 */
	public URI serve(List<AndroidApp> apps) {
		waitForServer();
		return whereIs(APPCOLLECTIONPATH + "/" + appListHandler.setList(apps));
	}

	/**
	 * Make a single app available for download
	 * 
	 * @param app
	 *          the app
	 * @return the url where the app may be found.
	 */
	public URI serve(AndroidApp app) {
		waitForServer();
		ArrayList<AndroidApp> tmp = new ArrayList<AndroidApp>();
		tmp.add(app);
		return whereIs(APPSINGLEPATH + "/" + focusedAppHandler.setList(tmp));
	}

	/**
	 * Make a file available for download
	 * 
	 * @param file
	 *          the file (may be a directory in which case it's contents are
	 *          listed).
	 * @return the url where the file may be retrieved.
	 */
	public URI serve(File... files) {
		waitForServer();
		return whereIs(FILEPATH + "/" + fileHandler.setFiles(files));
	}

	/**
	 * Start the webserver.
	 * 
	 * @param lm
	 *          the {@link Lifecycle} to notify when a client initiates a transfer
	 *          the user should know about.
	 */
	public void startup(LifecycleManager lm) {
		this.appListHandler = new AppListHandler(layout, lm);
		this.focusedAppHandler = new AppListHandler(layout, lm);
		this.fileHandler = new FileHandler(lm);
		this.resourceHandler = new ResourceHandler();

		ContextHandler list = new ContextHandler(APPCOLLECTIONPATH);
		list.setHandler(appListHandler);

		ContextHandler focused = new ContextHandler(APPSINGLEPATH);
		focused.setHandler(focusedAppHandler);

		ContextHandler files = new ContextHandler(FILEPATH);
		files.setHandler(fileHandler);

		ContextHandler rsrc = new ContextHandler(RSRCPATH);
		rsrc.setHandler(resourceHandler);

		AbstractHandler[] tmp = { list, focused, files, rsrc };

		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.setHandlers(tmp);

		server = new Server(0);
		server.setHandler(handlers);

		try {
			server.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (lock) {
			lock.notifyAll();
		}
	}

	// TODO: Blocking till we have a running server is a poor design. This will
	// inevitably be executed on the UI thread.
	private void waitForServer() {
		while (server == null || !server.isStarted()) {
			try {
				synchronized (lock) {
					lock.wait();
				}
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Figure out where a handler could be reached.
	 * 
	 * @param context
	 *          the context path for the handler
	 * @return the network address by which a handler can be reached or null if
	 *         the server is not available.
	 */
	private URI whereIs(String context) {
		URI ret = null;
		try {

			Connector con = server.getConnectors()[0];
			// Try to discover a public interface by making an outgoing connection.
			DatagramSocket s = new DatagramSocket();
			s.connect(InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 }), 0);
			InetSocketAddress address = new InetSocketAddress(s.getLocalAddress(),
					con.getLocalPort());
			s.close();
			ret = new URI("http", null, address.getAddress().getHostAddress(),
					con.getLocalPort(), context, null, null);
		}
		catch (NullPointerException e) {
			// Not part of a server.
		}
		catch (URISyntaxException e) {
			// Should not happen
			// e.printStackTrace();
			throw new RuntimeException(e);
		}
		catch (SocketException e) {
			e.printStackTrace();
		}
		catch (UnknownHostException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * kill the server
	 */
	public void shutdown() {
		try {
			server.stop();
			server = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setAtttribute(String name, Object value) {
		waitForServer();
		server.setAttribute(name, value);
	}

}
