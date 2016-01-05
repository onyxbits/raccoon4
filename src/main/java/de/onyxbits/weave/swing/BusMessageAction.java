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
package de.onyxbits.weave.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * An action that posts a message on the system bus.
 * 
 * @author patrick
 * 
 */
public class BusMessageAction extends AbstractAction {

	private Globals globals;
	private Object note;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new action
	 * 
	 * @param globals
	 *          registry
	 * @param message
	 *          what to post.
	 */
	public BusMessageAction(Globals globals, Object message) {
		this.globals = globals;
		this.note = message;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		globals.get(LifecycleManager.class).sendBusMessage(note);
	}

}
