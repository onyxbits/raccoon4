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
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * An {@link Action} that that calls through to other {@link ActionListener}S.
 * 
 * @author patrick
 * 
 */
public class ForwardingAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionListener receivers[];

	/**
	 * Register receivers
	 * 
	 * @param receivers
	 *          the receivers to call through to. If null, the action disables
	 *          itself.
	 * @return this reference.
	 */
	public ForwardingAction forwardTo(ActionListener... receivers) {
		this.receivers = receivers;
		if (receivers == null) {
			setEnabled(false);
		}
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (receivers != null) {
			for (ActionListener receiver : receivers) {
				receiver.actionPerformed(e);
			}
		}
	}

}
