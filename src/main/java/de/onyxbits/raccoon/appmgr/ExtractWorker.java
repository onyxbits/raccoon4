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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.dongliu.apk.parser.parser.BinaryXmlParser;
import net.dongliu.apk.parser.parser.ResourceTableParser;
import net.dongliu.apk.parser.parser.XmlTranslator;
import net.dongliu.apk.parser.struct.AndroidConstants;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.utils.Utils;

import org.apache.commons.io.IOUtils;

import de.onyxbits.weave.swing.BrowseAction;

/**
 * A worker for extracting APK files. It automatically translates binary XML.
 * 
 * @author patrick
 * 
 */
class ExtractWorker extends SwingWorker<File, Integer> {

	private File source;
	private List<String> filenames;
	private File dest;
	private ResourceTable resourceTable;

	/**
	 * 
	 * @param source
	 *          zipfile to extract
	 * @param dest
	 *          target directory
	 * @param filenames
	 *          entries to extract or null to extract everything
	 */
	public ExtractWorker(File source, File dest, List<String> filenames) {
		this.source = source;
		this.filenames = filenames;
		this.dest = dest;
	}

	@Override
	protected File doInBackground() throws Exception {
		ZipFile zip = new ZipFile(source);
		parseResourceTable();
		if (filenames == null) {
			Enumeration<? extends ZipEntry> e = zip.entries();
			Vector<String> tmp = new Vector<String>();
			while (e.hasMoreElements()) {
				tmp.add(e.nextElement().getName());
			}
			filenames = tmp;
		}

		for (String filename : filenames) {
			ZipEntry entry = zip.getEntry(filename);
			InputStream in = zip.getInputStream(entry);
			OutputStream out = openDestination(filename);

			if (isBinaryXml(filename)) {
				XmlTranslator xmlTranslator = new XmlTranslator();
				ByteBuffer buffer = ByteBuffer.wrap(Utils.toByteArray(in));
				BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer,
						resourceTable);
				binaryXmlParser.setLocale(Locale.getDefault());
				binaryXmlParser.setXmlStreamer(xmlTranslator);
				binaryXmlParser.parse();
				IOUtils.write(xmlTranslator.getXml(), out);
			}
			else {
				// Simply extract
				IOUtils.copy(in, out);
			}
			in.close();
			out.close();
		}

		zip.close();
		return dest;
	}

	/**
	 * Decide whether or not a file is binary XML
	 * 
	 * @param filename
	 *          the name of the file
	 * @return if filename is the manifest or an xml in the res folder.
	 */
	private boolean isBinaryXml(String filename) {
		if (AndroidConstants.MANIFEST_FILE.equals(filename)) {
			return true;
		}
		if (filename.startsWith(AndroidConstants.RES_PREFIX)
				&& filename.toLowerCase().endsWith(".xml")) {
			return true;
		}
		return false;
	}

	private OutputStream openDestination(String fname)
			throws FileNotFoundException {
		String[] elements = fname.split("/");
		File tmp = dest;
		for (String element : elements) {
			tmp = new File(tmp, element);
		}
		tmp.getParentFile().mkdirs();
		return new FileOutputStream(tmp);
	}

	@Override
	public void done() {
		try {
			BrowseAction.open(get().toURI());
		}
		catch (InterruptedException e) {
		}
		catch (ExecutionException e) {
			JOptionPane.showMessageDialog(null, e.getCause().getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private void parseResourceTable() throws IOException {
		ZipFile zf = new ZipFile(source);
		ZipEntry entry = Utils.getEntry(zf, AndroidConstants.RESOURCE_FILE);
		if (entry == null) {
			// if no resource entry has been found, we assume it is not needed by this
			// APK
			this.resourceTable = new ResourceTable();
			return;
		}

		this.resourceTable = new ResourceTable();

		InputStream in = zf.getInputStream(entry);
		ByteBuffer buffer = ByteBuffer.wrap(Utils.toByteArray(in));
		ResourceTableParser resourceTableParser = new ResourceTableParser(buffer);
		resourceTableParser.parse();
		this.resourceTable = resourceTableParser.getResourceTable();
	}

}
