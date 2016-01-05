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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

/**
 * An Android device
 * 
 * @author patrick
 * 
 */
public class Device {

	/**
	 * The serial number of the device
	 */
	public final String serial;

	private String alias;
	private String host;
	private int port;

	protected Device(String serial) {
		this(null, 5037, serial);
	}

	protected Device(String host, int port, String serial) {
		this.host = host;
		this.port = port;
		this.serial = serial;
	}

	public String getSerial() {
		return serial;
	}

	@Override
	public String toString() {
		if (alias != null) {
			return alias;
		}
		return serial;
	}

	/**
	 * Query the devices human readable name
	 * 
	 * @return the alias name of the device
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Every device can have a human readable name
	 * 
	 * @param alias
	 *          a string that describes the device better than the serial number.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Create a stream for reading a file from the device
	 * 
	 * @param fname
	 *          remote filename
	 * @throws IOException
	 */
	public InputStream createPullStream(String fname) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			ProtocolSupport.send("host:transport:" + serial, in, out);

			ProtocolSupport.send("sync:", in, out);
			output.writeBytes("RECV");
			output.writeInt(Integer.reverseBytes(fname.length()));
			output.writeBytes(fname);
			output.flush();
			return new PullStream(in);
		}
		catch (IOException e) {
			try {
				socket.close();
			}
			catch (IOException e1) {
			}
			throw e;
		}
	}

	/**
	 * Create a stream for copying a file to the device
	 * 
	 * @param fname
	 *          remote filename
	 * @param mode
	 *          access flags (e.g. 0644)
	 * @throws IOException
	 */
	public PushStream createPushStream(String fname, int mode) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			InputStream in = socket.getInputStream();
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			ProtocolSupport.send("host:transport:" + serial, in, out);

			ProtocolSupport.send("sync:", in, out);
			out.writeBytes("SEND");
			String target = fname + "," + mode;
			out.writeInt(Integer.reverseBytes(target.length()));
			out.writeBytes(target);
			out.flush();
			return new PushStream(out);
		}
		catch (IOException e) {
			try {
				socket.close();
			}
			catch (IOException e1) {
			}
			throw e;
		}
	}

	/**
	 * Execute a shell command on the device
	 * 
	 * @param cmd
	 *          the command
	 * @return whatever the command prints to sdtout/stderr
	 * @throws IOException
	 */
	public String exec(String cmd) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			ProtocolSupport.send("host:transport:" + serial, in, out);

			ProtocolSupport.send("shell:" + cmd, in, out);
			int i = -1;
			StringBuilder sb = new StringBuilder();
			while ((i = in.read()) != -1) {
				sb.append((char) i);
			}
			return sb.toString();
		}
		catch (IOException e) {
			try {
				socket.close();
			}
			catch (IOException e1) {
			}
			throw e;
		}
	}

	/**
	 * Get file info
	 * 
	 * @param remotePath
	 *          the file to stat
	 * @return file state
	 * @throws IOException
	 */
	public RemoteFile stat(String remotePath) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			InputStream in = socket.getInputStream();
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			ProtocolSupport.send("host:transport:" + serial, in, out);

			ProtocolSupport.send("sync:", in, out);
			out.writeBytes("STAT");
			out.writeInt(Integer.reverseBytes(remotePath.length()));
			out.writeBytes(remotePath);

			byte[] buf = new byte[4];
			in.read(buf);
			if (ProtocolSupport.parseInt(buf, 0) == ProtocolSupport.STAT) {
				buf = new byte[12];
				in.read(buf);
				return new RemoteFile(remotePath, ProtocolSupport.parseInt(buf, 0),
						ProtocolSupport.parseInt(buf, 4), ProtocolSupport.parseInt(buf, 8));
			}
			throw new RuntimeException("File did not stat!");
		}
		catch (IOException e) {
			try {
				socket.close();
			}
			catch (IOException e1) {
			}
			throw e;
		}
	}

	/**
	 * List a directory
	 * 
	 * @param remotePath
	 *          directory name
	 * @return file listing.
	 * @throws IOException
	 * @throws BridgeException
	 */
	public List<RemoteFile> list(String remotePath) throws IOException {

		Socket socket = null;
		try {
			socket = new Socket(host, port);
			InputStream in = socket.getInputStream();
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			ProtocolSupport.send("host:transport:" + serial, in, out);

			ProtocolSupport.send("sync:", in, out);
			out.writeBytes("LIST");
			out.writeInt(Integer.reverseBytes(remotePath.length()));
			out.writeBytes(remotePath);
			byte[] buf = new byte[512];
			List<RemoteFile> result = new Vector<RemoteFile>();
			in.read(buf, 0, 4);
			while (ProtocolSupport.parseInt(buf, 0) == ProtocolSupport.DENT) {
				in.read(buf, 0, 16);
				int len = ProtocolSupport.parseInt(buf, 12);
				in.read(buf, 0, len);
				String fname = new String(buf, 0, len);
				result
						.add(new RemoteFile(fname, ProtocolSupport.parseInt(buf, 0),
								ProtocolSupport.parseInt(buf, 4), ProtocolSupport.parseInt(buf,
										8)));
				in.read(buf, 0, 4);
			}
			return result;
		}
		catch (IOException e) {
			try {
				socket.close();
			}
			catch (IOException e1) {
			}
			throw e;
		}

	}

	/**
	 * Get the external storage path
	 * 
	 * @return the external storage directory (guaranteed to include a trailing
	 *         slash).
	 */
	public String getExternalStorageDir() throws IOException {
		String ret = exec("echo $EXTERNAL_STORAGE").replace('\n', ' ').trim();
		if (!(ret.endsWith("/"))) {
			ret += "/";
		}
		return ret;
	}

	/**
	 * Get the tmp directory of the user partition
	 * 
	 * @return a pathname (trailing slash included).
	 * @throws IOException
	 */
	public String getUserTempDir() throws IOException {
		return "/data/local/tmp/";
	}

	/**
	 * Query the device's SDK version
	 * 
	 * @return the SDK version
	 * @throws IOException
	 */
	public int getSdkVersion() throws IOException {
		String s = exec("getprop ro.build.version.sdk").replace('\n', ' ').trim();
		return Integer.parseInt(s);
	}

	/**
	 * Figure out what this device would identify itself to Google Play.
	 * 
	 * @return a user agent string.
	 * @throws IOException
	 */
	public String getPlayUserAgent() throws IOException {
		// Safe defaults from a Nexus 7 2012
		String vn = "3.10.10";
		String vc = "8016010";
		try {
			vn = exec("dumpsys package com.android.vending | grep versionName")
					.replace('\n', ' ').trim().split("=")[1];
			vc = exec("dumpsys package com.android.vending | grep versionCode")
					.replace('\n', ' ').trim().split("=")[1];
		}
		catch (Exception e) {
			// Maybe a custom ROM?
		}
		Object[] args = { vn, vc, getSdkVersion(),
				exec("getprop ro.product.device").replace('\n', ' ').trim(),
				exec("getprop ro.hardware").replace('\n', ' ').trim(),
				exec("getprop ro.build.product").replace('\n', ' ').trim(),
				exec("getprop ro.build.id").replace('\n', ' ').trim(),
				exec("getprop ro.build.type").replace('\n', ' ').trim() };
		MessageFormat ret = new MessageFormat(
				"Android-Finsky/{0} (versionCode={1},sdk={2},device={3},hardware={4},product={5},build={6}:{7})");
		return ret.format(args);
	}
}