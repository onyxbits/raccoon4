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
package de.onyxbits.raccoon.ptools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.onyxbits.weave.Globals;

/**
 * Superclass for actions that require a device. A BridgeAction will
 * automatically enable/disable itself depending on device availability. The
 * action will not register itself as a {@link BridgeListener}. This has to be
 * done externally.
 * 
 * @author patrick
 * 
 */
public class BridgeAction extends AbstractAction implements BridgeListener {

	protected Globals globals;

	/**
	 * Subclasses must call super
	 * 
	 * @param globals
	 *          registry
	 */
	public BridgeAction(Globals globals) {
		this.globals = globals;
		super.setEnabled(isReady(globals.get(BridgeManager.class)) && canEnable());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void onDeviceActivated(BridgeManager manager) {
		super.setEnabled(isReady(manager) && canEnable());
	}

	@Override
	public void onConnectivityChange(BridgeManager manager) {
		super.setEnabled(isReady(manager) && canEnable());
	}

	private boolean isReady(BridgeManager bm) {
		return (bm != null && bm.isRunning() && bm.getActiveDevice() != null);
	}

	/**
	 * Enable/disable the action. This may be overruled.
	 */
	@Override
	public final void setEnabled(boolean enable) {
		BridgeManager m = globals.get(BridgeManager.class);
		if (isReady(m) && canEnable()) {
			super.setEnabled(enable);
		}
		else {
			super.setEnabled(false);
		}
	}

	/**
	 * Subclasses may overwrite this if they got a condition that should prevent
	 * the action from being enabled.
	 * 
	 * @return default implementation returns true
	 */
	protected boolean canEnable() {
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

}
