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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.security.MessageDigest;

//import de.onyxbits.weave.core.LifecycleManager;

/**
 * An alternative to using a logging framework.
 * <p>
 * Most logging frameworks are designed with syslogd in mind: a singleton
 * service that accepts (flat string) messages from various sources and appends
 * them to a central text file. This approach is suitable for unmonitored
 * (server) processes in the care of trained system administrators, but not for
 * interactive applications deployed on end user desktop machines. If something
 * goes wrong there, logfiles rarely provide suitable debugging information.
 * <p>
 * The {@link ReportManager} is designed to expect the unexpected. That is,
 * automatically compile a comprehensive bugreport whenever an uncaught
 * exception occurs (as oppose to relying on littering the application code with
 * debug statements in the hope of catching something of interest).
 * <p>
 * In the most basic use case, only the {@link ReportManager#supervise()} method
 * needs to be called once from anywhere in the application. Afterwards, all
 * uncaught exceptions will automatically create a comprehensive bug report in
 * the system's temp directory.
 * 
 * @author patrick
 * 
 */
public final class ReportManager {

	/**
	 * The {@link Report} category for uncaught exceptions.
	 */
	public static final String UNCAUGHT = "uncaught";

	/**
	 * The {@link Report} category for reporting caught exceptions.
	 */
	public static final String BUG = "bug";

	/**
	 * 
	 */
	public static final String CONFIG = "config";

	/**
	 * If true, print a stacktrace when building a crash report.
	 */
	public static boolean print;

	//private static LifecycleManager lifecycleManager;
	private static ReportHandler reportHandler;

	static {
    // lifecycleManager = null;
		reportHandler = new DefaultReportHandler();
		print = true;
	}

	/**
	 * Compile a comprehensive crash report from a {@link Throwable}. The report
	 * will include:
	 * <ul>
	 * <li>the stacktrace
	 * <li>a textual representation of the event that likely triggered the
	 * exception
	 * <li>the system properties
	 * <li>screenshots of every window in the application (regardless of
	 * visibility) along with metadata.
	 * <p>
	 * This method may be called from any thread.
	 * 
	 * @param e
	 *          The {@link Throwable} in question.
	 * @return the report (may be amended).
	 */
	public static Report createBugReport(Throwable e) {
		Report report = new Report();
		report.add("stacktrace.txt", e);
		report.add("event.txt", "" + EventQueue.getCurrentEvent());
		report.add("system.properties", System.getProperties());
		ReportBackend.capture(report);

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(e.getMessage().getBytes());
			byte[] digest = md.digest();
			report.setFingerprint(new BigInteger(1, digest));
		}
		catch (Exception exp) {
		}
		return report;

	}

	/**
	 * Compile a system configuration report
	 * 
	 * @param o
	 *          optional objects to include
	 * @return the report (may be amended).
	 */
	public static Report createConfigReport(Object... cfg) {
		Report report = new Report();
		report.add("system.properties", System.getProperties());
		for (Object o : cfg) {
			report.add(o);
		}
		report.setCategory(CONFIG);
		return report;
	}

	/**
	 * Configure a {@link LifecycleManager} that will handle report submission.
	 * 
	 * @param lm
	 *          The manager of a lifecycle that knows how to handle {@link Report}
	 *          S sent via {@link LifecycleManager#sendBusMessage(Object)}. May be
	 *          null.
	 */
	//public static void setReportHandler(LifecycleManager lm) {
	//	lifecycleManager = lm;
	//}

	/**
	 * Configure the internal {@link ReportHandler}.
	 * 
	 * @param rh
	 *          new handler, may not be null.
	 */
	public static void setReportHandler(ReportHandler rh) {
		if (rh != null) {
			reportHandler = rh;
		}
	}

	/**
	 * Install a handler that automatically redirects all uncaught exceptions to
	 * the {@link ReportManager#createBugReport(Throwable)}.
	 */
	public static void supervise() {
		Thread.setDefaultUncaughtExceptionHandler(new ReportBackend(null));
	}

	/**
	 * Utility method to screenshot a window into a buffer (must be called on the
	 * EDT).
	 * 
	 * @param w
	 *          the window to render
	 * @return the rendered window.
	 */
	public static BufferedImage render(Window w) {
		// Don't use java.awt.Robot for this! We want the window itself, not what's
		// on screen.
		Dimension size = w.getSize();
		BufferedImage img = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics gr = img.createGraphics();
		w.paint(gr);
		gr.dispose();
		return img;
	}

	/**
	 * Handle a report. If a {@link LifecycleManager} is configured, the report is
	 * forwarded to it via {@link LifecycleManager#sendBusMessage(Object)}.
	 * Otherwise it will be processed by managers internal {@link ReportHandler}
	 * if it is not a duplicate.
	 * 
	 * @param report
	 *          the report to process.
	 */
	public static void handle(Report report) {
		//if (lifecycleManager != null) {
		//	lifecycleManager.sendBusMessage(report);
		//	return;
		//}
		if (!reportHandler.isDuplicate(report)) {
			reportHandler.handle(report);
		}
	}

}
