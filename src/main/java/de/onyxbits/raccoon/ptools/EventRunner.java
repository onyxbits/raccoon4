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

import java.util.List;

/**
 * Helper class for dispatching bridge events on the EDT.
 * 
 * @author patrick
 * 
 */
class EventRunner implements Runnable {

	public static final int ACTIVATION = 0;
	public static final int CONNECTIVITY = 1;

	private int type;
	private BridgeManager manager;
	private List<BridgeListener> listeners;

	public EventRunner(BridgeManager manager, int type,
			List<BridgeListener> listeners) {
		this.manager = manager;
		this.listeners = listeners;
		this.type = type;
	}

	@Override
	public void run() {
		for (BridgeListener listener : listeners) {
			switch (type) {
				case ACTIVATION: {
					listener.onDeviceActivated(manager);
					break;
				}
				case CONNECTIVITY: {
					listener.onConnectivityChange(manager);
					break;
				}
			}
		}
	}

}
