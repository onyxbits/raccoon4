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

import java.text.MessageFormat;

/**
 * A simple stopwatch utility for timing the execution time of a piece of code
 * (only for debugging purposes).
 * 
 * @author patrick
 * 
 */
public final class Clock {

	private static long now;

	/**
	 * Ready, set, ...
	 * <p>
	 * Start/Reset the timer.
	 * 
	 * @return current system time.
	 */
	public static long go() {
		now = System.currentTimeMillis();
		return now;
	}

	/**
	 * Read the timer (get the time elapsed since {@link Clock#go()} was called
	 * last.
	 * 
	 * @param tmpl
	 *          A template for printing the elapsed time via {@link MessageFormat}
	 * 
	 * @return elapsed time in milliseconds.
	 */
	public static long read(String tmpl) {
		long elapsed = System.currentTimeMillis() - now;
		if (tmpl != null) {
			System.out.println(MessageFormat.format(tmpl, elapsed));
		}
		return elapsed;
	}
}
