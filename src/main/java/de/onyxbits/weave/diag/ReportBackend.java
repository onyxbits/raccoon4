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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.SwingUtilities;

/**
 * Does the dirty work for the report system.
 * 
 * @author patrick
 * 
 */
class ReportBackend implements Runnable, UncaughtExceptionHandler {

	private Report report;

	public ReportBackend(Report report) {
		this.report = report;
	}

	@Override
	public void run() {
		capture(report);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (ReportManager.print) {
			e.printStackTrace();
		}
		Report report = ReportManager.createBugReport(e);
		report.setCategory(ReportManager.UNCAUGHT);

		ReportManager.handle(report);
	}

	static void capture(Report report) {
		if (SwingUtilities.isEventDispatchThread()) {
			Window[] windows = Window.getWindows();
			Properties prop = new Properties();
			for (int i = 0; i < windows.length; i++) {
				String prefix = "window_" + i;
				BufferedImage img = ReportManager.render(windows[i]);
				report.add(prefix + ".png", img);
				// Do this by hand! It's more robust and we don't need what we can see
				// in the image anyways!
				prop.setProperty("name", "" + windows[i].getName());
				prop.setProperty("focused", "" + windows[i].isFocused());
				prop.setProperty("visible", "" + windows[i].isVisible());
				prop.setProperty("bounds", "" + windows[i].getBounds().toString());
				if (windows[i] instanceof Frame) {
					prop.setProperty("title", "" + ((Frame) windows[i]).getTitle());
					prop.setProperty("state", "" + ((Frame) windows[i]).getState());
				}
				if (windows[i] instanceof Dialog) {
					prop.setProperty("title", ((Dialog) windows[i]).getTitle());
				}
				report.add(prefix + ".properties", prop);
			}
		}
		else {
			try {
				SwingUtilities.invokeAndWait(new ReportBackend(report));
			}
			catch (InvocationTargetException e) {
			}
			catch (InterruptedException e) {
			}
		}
	}

}
