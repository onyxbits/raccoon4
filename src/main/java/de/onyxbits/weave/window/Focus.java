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
package de.onyxbits.weave.window;

import java.awt.Component;
import java.awt.EventQueue;

/**
 * Ensure that a certain component gets the input focus by scheduling a focus
 * transfer request to it at the end of the {@link EventQueue}.
 * 
 * @author patrick
 * 
 */
public final class Focus implements Runnable {

	private Component c;

	@Override
	public void run() {
		c.requestFocusInWindow();
	}

	/**
	 * Transfer focus
	 * 
	 * @param c
	 *          the component to move the input focus to.
	 */
	public static void on(Component c) {
		Focus af = new Focus();
		af.c = c;
		EventQueue.invokeLater(af);
	}

}
