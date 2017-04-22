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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

	private String host;
	private int port;

	private ArrayList<String> libs;
	private ArrayList<String> features;
	private ArrayList<String> abis;
	private Properties propertyCache;

	protected Device(String serial) {
		this(null, 5037, serial);
	}

	protected Device(String host, int port, String serial) {
		this.host = host;
		this.port = port;
		this.serial = serial;
		this.propertyCache = new Properties();
	}

	public String getSerial() {
		return serial;
	}

	/**
	 * Read a system property
	 * 
	 * @param name
	 *          property name
	 * @param def
	 *          default value to return if name not found.
	 * @return the value or the default value.
	 */
	public String getProperty(String name, String def) {
		if (propertyCache.containsKey(name)) {
			return propertyCache.getProperty(name);
		}
		String tmp = null;
		try {
			// Note to self: the Android property system sources more than just
			// /system/build.prop, so there is no way to prefetch everything.
			tmp = exec("getprop " + name).trim();
		}
		catch (IOException e) {
		}
		if (tmp == null || tmp.length() == 0) {
			return def;
		}
		propertyCache.put(name, tmp);
		return tmp;
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
		String ret = exec("echo $EXTERNAL_STORAGE").trim();
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
	 * List available libraries
	 * 
	 * @return installed libraries
	 */
	public List<String> getSharedLibraries() {
		if (libs == null) {
			libs = new ArrayList<String>();
			try {
				String[] output = exec("pm list libraries").split("\\r?\\n");
				for (String line : output) {
					String[] tmp = line.split(":");
					if (tmp.length == 2) {
						libs.add(tmp[1]);
					}
				}
			}
			catch (Exception e) {
			}
		}
		return libs;
	}

	/**
	 * List available features
	 * 
	 * @return device features
	 */
	public List<String> getSystemFeatures() {
		if (features == null) {
			features = new ArrayList<String>();
			try {
				String[] output = exec("pm list features").split("\\r?\\n");
				for (String line : output) {
					String[] tmp = line.split(":");
					if (tmp.length == 2) {
						features.add(tmp[1]);
					}
				}
			}
			catch (Exception e) {
			}
		}
		return features;
	}

	public List<String> getAbis() {
		if (abis == null) {
			abis = new ArrayList<String>();

			try {
				// Stuff works differently pre-lollipop
				if (Integer.parseInt(getProperty("ro.build.version.sdk", null)) < 21) {
					String tmp = getProperty("ro.product.cpu.abi", null);
					if (tmp != null) {
						abis.add(tmp);
					}
					tmp = getProperty("ro.product.cpu.abi2", null);
					if (tmp != null) {
						abis.add(tmp);
					}
				}
				else {
					String[] tmp = getProperty("ro.product.cpu.abilist", "").split(
							" *, *");
					for (String s : tmp) {
						abis.add(s);
					}
				}
			}
			catch (Exception e) {
			}
		}
		return abis;
	}

}