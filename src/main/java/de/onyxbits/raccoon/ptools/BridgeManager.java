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
package de.onyxbits.raccoon.ptools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import de.onyxbits.raccoon.vfs.Layout;

/**
 * High level wrapper around the ADB functionality
 * 
 * @author patrick
 * 
 */
public class BridgeManager {

	/**
	 * The port on which to expect the server.
	 */
	public static final int PORT = 5037;

	private Device activeDevice;
	private BridgeObserver backend;
	private List<BridgeListener> listeners;
	private Layout layout;

	public BridgeManager(Layout layout) {
		listeners = new ArrayList<BridgeListener>();
		this.layout = layout;
	}

	/**
	 * Must be called to initialize the manager. This will connect the manager to
	 * the daemon and start polling for devices. This method is safe to call if
	 * the manager is already running and it may be used to restart it.
	 */
	public void startup() {
		if (isRunning()) {
			return;
		}

		try {
			runAdb();
		}
		catch (IOException e) {
			// We might lack ADB, but there is still hope that the binary is just not
			// in the search path and the server is running nevertheless.
			// e.printStackTrace();
		}
		catch (InterruptedException e) {
		}

		Socket socket = null;
		try {
			socket = createSocket();
			ProtocolSupport.send("host:version", socket.getInputStream(),
					socket.getOutputStream());
			socket.close();
			backend = new BridgeObserver(this);
			new Thread(backend).start();
			synchronized (backend) {
				while (!backend.ready) {
					backend.wait();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			socket.close();
		}
		catch (Exception e) {
		}
		fireBridgeEvent(EventRunner.CONNECTIVITY);
	}

	private void runAdb() throws InterruptedException, IOException {
		ToolSupport ts = new ToolSupport(layout.binDir);
		Runtime rt = Runtime.getRuntime();
		if (ts.adb.exists()) {
			// Try the private installation first
			rt.exec(ts.adb.getAbsolutePath() + " start-server").waitFor();
		}
		else {
			// Let's hope ADB is in the PATH.
			rt.exec("adb start-server").waitFor();
		}
	}

	/**
	 * Must be called to release resources.
	 */
	public void shutdown() {
		if (backend != null) {
			backend.kill();
			backend = null;
		}
	}

	public void addBridgeListener(BridgeListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeBridgeListener(BridgeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Check if the server is running and we can connect to it.
	 * 
	 * @return true if the server is ready to use.
	 */
	public boolean isRunning() {
		return (backend != null && backend.isRunning());
	}

	/**
	 * Create a new socket for talking to the server.
	 * 
	 * @return a socket
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	protected Socket createSocket() throws IOException {
		try {
			return new Socket("localhost", PORT);
		}
		catch (UnknownHostException e) {
			// This should not happen
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the device to talk to
	 * 
	 * @return the device, the user focused on. May be null.
	 */
	public Device getActiveDevice() {
		return activeDevice;
	}

	public List<Device> listDevices() {
		Socket socket = null;
		try {
			socket = createSocket();
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			ProtocolSupport.send("host:devices", in, out);
			byte[] buf = new byte[4];
			int len = Integer.parseInt(new String(buf, 0, 4), 16);
			buf = new byte[len];
			in.read(buf);
			String[] lines = new String(buf, 0, len).split("\n");
			ArrayList<Device> devices = new ArrayList<Device>(lines.length);
			for (String line : lines) {
				String[] parts = line.split("\t");
				if (parts.length > 1) {
					devices.add(new Device(parts[0]));
				}
			}
			socket.close();
			return devices;
		}
		catch (Exception e) {
			if (socket != null) {
				try {
					socket.close();
				}
				catch (IOException e1) {
				}
			}
			e.printStackTrace();
			return new ArrayList<Device>(0);
		}
	}

	/**
	 * Set the device focus
	 * 
	 * @param activeDevice
	 *          the device to talk to from now on. May be null.
	 */
	public void setActiveDevice(Device activeDevice) {
		this.activeDevice = activeDevice;
		fireBridgeEvent(EventRunner.ACTIVATION);
	}

	private void fireBridgeEvent(int type) {
		SwingUtilities.invokeLater(new EventRunner(this, type, listeners));
	}

}
