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

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;

/**
 * An implementation of the {@link ReportHandler} interface that writes to the
 * file system.
 * 
 * @author patrick
 * 
 */
public class DefaultReportHandler implements ReportHandler {

	private static final String TMPL = "{0}_{1}.zip";
	private File reportDir;

	/**
	 * Create reports in the system temp dir.
	 */
	public DefaultReportHandler() {
		this(new File(System.getProperty("java.io.tmpdir")));
	}

	/**
	 * Create reports in a given directory
	 * 
	 * @param reportDir
	 *          the directory to write to (will be autocreated by the first report
	 *          if it doesn't exist).
	 */
	public DefaultReportHandler(File reportDir) {
		this.reportDir = reportDir;
	}

	@Override
	public void handle(Report report) {
		try {
			reportDir.mkdirs();
			FileOutputStream fos = new FileOutputStream(getFile(report));
			fos.write(report.export());
			fos.close();
		}
		catch (Exception e) {
		}
	}

	@Override
	public boolean isDuplicate(Report report) {
		return getFile(report).exists();
	}

	private File getFile(Report report) {
		String hashtext = report.getFingerprint().toString(16);
		if (hashtext == null) {
			hashtext = "";
		}
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		String category = report.getCategory();
		if (category == null) {
			category = "report";
		}
		else {
			category = category.toLowerCase();
		}

		return new File(reportDir, MessageFormat.format(TMPL, category, hashtext));
	}
}
