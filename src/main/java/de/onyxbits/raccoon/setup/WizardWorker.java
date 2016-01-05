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
package de.onyxbits.raccoon.setup;

import javax.swing.SwingWorker;

/**
 * A driver for doing the background tasks in the {@link WizardBuilder}
 * 
 * @author patrick
 * 
 */
class WizardWorker extends SwingWorker<Object, Object> {

	private WizardBuilder owner;

	public WizardWorker(WizardBuilder owner) {
		this.owner = owner;
	}

	@Override
	protected Object doInBackground() throws Exception {
		owner.onDoInBackground();
		return null;
	}

	@Override
	protected void done() {
		owner.onDone();
	}

}
