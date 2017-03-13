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
package de.onyxbits.raccoon.gplay;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.onyxbits.raccoon.db.DatasetEvent;
import de.onyxbits.raccoon.db.DatasetListener;
import de.onyxbits.weave.Globals;

/**
 * Triggers an update search and puts the results directly in the download
 * queue.
 * 
 * @author patrick
 * 
 */
public class UpdateAppAction extends AbstractAction implements DatasetListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Globals globals;

	public UpdateAppAction(Globals globals) {
		this.globals = globals;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new UpdateAppWorker(globals).execute();
	}

	@Override
	public void onDataSetChange(DatasetEvent event) {
		if (event instanceof PlayProfileEvent) {
			PlayProfileEvent ppe = (PlayProfileEvent) event;
			if (ppe.isConnection()) {
				setEnabled(ppe.isActivation());
			}
		}
	}

}
