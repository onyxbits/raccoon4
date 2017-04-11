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
package de.onyxbits.raccoon.cli;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.onyxbits.weave.util.Version;

/**
 * Core class of the package. Knows how to parse the command line arguments and
 * route them to the desired functions.
 * 
 * @author patrick
 * 
 */
public class Router {

	/**
	 * Print an error message and exit
	 * 
	 * @param reason
	 *          property (minus the "fail." prefix) that contains the error
	 *          message.
	 * @param args
	 *          format option.
	 */
	public static void fail(String reason, Object... args) {
		System.err.println(MessageFormat.format(
				Messages.getString("fail." + reason), args));
		System.exit(1);
	}

	private static final String DESC = "description.";

	public static void main(String[] args) {
		Options options = new Options();

		Option property = Option.builder("D").argName("property=value")
				.numberOfArgs(2).valueSeparator().desc(Messages.getString(DESC + "D"))
				.build();
		options.addOption(property);

		Option help = new Option("h", "help", false, Messages.getString(DESC + "h"));
		options.addOption(help);

		Option version = new Option("v", "version", false, Messages.getString(DESC
				+ "v"));
		options.addOption(version);

		// GPA: Google Play Apps (we might add different markets later)
		Option playAppDetails = new Option(null, "gpa-details", true,
				Messages.getString(DESC + "gpa-details"));
		playAppDetails.setArgName("package");
		options.addOption(playAppDetails);

		Option playAppBulkDetails = new Option(null, "gpa-bulkdetails", true,
				Messages.getString(DESC + "gpa-bulkdetails"));
		playAppBulkDetails.setArgName("file");
		options.addOption(playAppBulkDetails);

		Option playAppBatchDetails = new Option(null, "gpa-batchdetails", true,
				Messages.getString(DESC + "gpa-batchdetails"));
		playAppBatchDetails.setArgName("file");
		options.addOption(playAppBatchDetails);
		
		Option playAppSearch = new Option(null, "gpa-search", true,
				Messages.getString(DESC + "gpa-search"));
		playAppSearch.setArgName("query");
		options.addOption(playAppSearch);

		CommandLine commandLine = null;
		try {
			commandLine = new DefaultParser().parse(options, args);
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		if (commandLine.hasOption(property.getOpt())) {
			System.getProperties().putAll(
					commandLine.getOptionProperties(property.getOpt()));
		}

		if (commandLine.hasOption(help.getOpt())) {
			new HelpFormatter().printHelp("raccoon", Messages.getString("header"),
					options, Messages.getString("footer"),true);
			System.exit(0);
		}

		if (commandLine.hasOption(version.getOpt())) {
			System.out.println(GlobalsProvider.getGlobals().get(Version.class));
			System.exit(0);
		}

		if (commandLine.hasOption(playAppDetails.getLongOpt())) {
			Play.details(commandLine.getOptionValue(playAppDetails.getLongOpt()));
			System.exit(0);
		}

		if (commandLine.hasOption(playAppBulkDetails.getLongOpt())) {
			Play.bulkDetails(new File(commandLine.getOptionValue(playAppBulkDetails
					.getLongOpt())));
			System.exit(0);
		}

		if (commandLine.hasOption(playAppBatchDetails.getLongOpt())) {
			Play.details(new File(commandLine.getOptionValue(playAppBatchDetails
					.getLongOpt())));
			System.exit(0);
		}
		
		if (commandLine.hasOption(playAppSearch.getLongOpt())) {
			Play.search(commandLine.getOptionValue(playAppSearch.getLongOpt()));
			System.exit(0);
		}
	}
}
