/*
 * Copyright 2016 Patrick Ahlbrecht
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
package de.onyxbits.weave.diag;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

/**
 * A general purpose data collection facility that will store report data in a
 * zip container.
 * 
 * @author patrick
 * 
 */
public class Report {

	private static final int BUFSIZE = 1024 * 20;
	private ZipOutputStream zipOut;
	private ByteArrayOutputStream buffer;
	private int imgCount;
	private int propCount;
	private int stackCount;
	private BigInteger fingerprint;
	private String category;

	public Report() {
		buffer = new ByteArrayOutputStream(BUFSIZE);
		zipOut = new ZipOutputStream(buffer);
	}

	/**
	 * Add a {@link Throwable} to the reort.
	 * 
	 * @param path
	 *          storage path
	 * @param e
	 *          the {@link Throwable}.
	 */
	public void add(String path, Throwable e) {
		try {
			zipOut.putNextEntry(new ZipEntry(path));
			e.printStackTrace(new PrintStream(zipOut));
		}
		catch (Exception e1) {
		}
	}

	/**
	 * Add a key/value map to the report.
	 * 
	 * @param path
	 *          storage path
	 * @param p
	 *          Key/Value pairs. NOTE: when adding key/values to a Properties
	 *          always use <code>p.setProperty(""+myKey,""+myValue);</code> to
	 *          make sure only strings get included in the map.
	 */
	public void add(String path, Properties p) {
		try {
			zipOut.putNextEntry(new ZipEntry(path));
			p.store(zipOut, null);
		}
		catch (Exception e) {
		}
	}

	/**
	 * Add an image to the report.
	 * 
	 * @param path
	 *          storage path
	 * @param img
	 *          image data.
	 */
	public void add(String path, RenderedImage img) {
		try {
			zipOut.putNextEntry(new ZipEntry(path));
			ImageIO.write(img, "png", zipOut);
		}
		catch (Exception e) {
		}
	}

	/**
	 * Add a generic binary blob to the report
	 * 
	 * @param path
	 *          storage path
	 * @param data
	 *          blob to include.
	 */
	public void add(String path, byte[] data) {
		try {
			zipOut.putNextEntry(new ZipEntry(path));
			zipOut.write(data);
		}
		catch (IOException e) {
		}
	}

	/**
	 * Add a text message to the report.
	 * 
	 * @param path
	 *          storage path.
	 * @param txt
	 *          message text.
	 */
	public void add(String path, String txt) {
		add(path, txt.getBytes());
	}

	/**
	 * Convenience method for adding generic objects to the report. Pathname is
	 * automatically determined by type.
	 * 
	 * @param o
	 *          the object to store. Unless it is one of the known types, it must
	 *          have a meaningful string representation.
	 */
	public void add(Object o) {
		if (o instanceof RenderedImage) {
			imgCount++;
			String p = MessageFormat.format("image-{0}.png", imgCount);
			add(p, (RenderedImage) o);
			return;
		}
		if (o instanceof Properties) {
			propCount++;
			String p = MessageFormat.format("values-{0}.properties", propCount);
			add(p, (Properties) o);
			return;
		}
		if (o instanceof Throwable) {
			stackCount++;
			String p = MessageFormat.format("stacktrace-{0}.txt", stackCount);
			add(p, (Throwable) o);
			return;
		}
		add(o.getClass().getName().replace('.', '/'), o.toString());
	}

	/**
	 * Finalize the report. Nothing may be added afterwards.
	 * 
	 * @return the zip container as a byte array (a new instance every time this
	 *         method is called).
	 */
	public byte[] export() {
		try {
			zipOut.close();
		}
		catch (IOException e) {
		}
		return buffer.toByteArray();
	}

	/**
	 * Get the fingerprint of this report
	 * 
	 * @return the fingerprint if set or null.
	 */
	public BigInteger getFingerprint() {
		return fingerprint;
	}

	/**
	 * Add a fingerprint. Fingerprints are optional long time stable hashvalues by
	 * which the system can determine if it has already seen a given report and
	 * decide if it wants to process it again. For example, if compiling a bug
	 * report, the fingerprint could be the MD5 sum of the stacktrace.
	 * <p>
	 * The fingerprint is meta information and not encoded into the export.
	 * 
	 * @param fingerprint
	 *          unique identifier.
	 * 
	 */
	public void setFingerprint(BigInteger fingerprint) {
		this.fingerprint = fingerprint;
	}

	/**
	 * Get the category of this report
	 * 
	 * @return the category or null if not set.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Set the (optional) category of this report.
	 * <p>
	 * The category is meta information and not encoded into the export.
	 * 
	 * @param category
	 *          a string for filing. Should be kept to lowercase alphanumeric
	 *          characters only.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

}
