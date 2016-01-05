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
package de.onyxbits.weave.util;

import java.util.Vector;

import de.onyxbits.weave.Globals;
import de.onyxbits.weave.Lifecycle;

/**
 * A tool for dispatching a {@link Object} to one or more handlers. Note: this
 * multiplexer simply broadcasts to all receivers in a single rush. Care should
 * be taken about the number of subscribers to be notified. If in doubt, use
 * several domain specific multiplexers. For example, if your application
 * contains subsystems that constantly exchange status updates (e.g. concurrent
 * download threads that update progress bars), then you would subclass the
 * {@link BusMultiplexer} (so you can have multiple multiplexers in the
 * {@link Globals} instance), group your {@link Object} commands into having
 * common prefixes and switch by prefix in
 * {@link Lifecycle#onBusMessage(Globals, Object)}.
 * 
 * @author patrick
 * 
 */
public class BusMultiplexer {

	private Vector<BusMessageHandler> receivers = new Vector<BusMessageHandler>();
	private Vector<BusMessageHandler> pendingSubscriptions = new Vector<BusMessageHandler>();
	private Vector<BusMessageHandler> pendingUnsubscriptions = new Vector<BusMessageHandler>();
	private boolean broadcasting;

	/**
	 * Register a receiver for processing messages.
	 * 
	 * @param noteHandler
	 *          the receiver.
	 * @return this reference for chaining.
	 */
	public final BusMultiplexer subscribe(BusMessageHandler noteHandler) {
		if (noteHandler == null) {
			return this;
		}
		if (broadcasting) {
			if (!pendingSubscriptions.contains(noteHandler)) {
				pendingSubscriptions.add(noteHandler);
			}
		}
		else {
			if (!receivers.contains(noteHandler)) {
				receivers.add(noteHandler);
			}
		}
		return this;
	}

	/**
	 * Remove a receiver
	 * 
	 * @param noteHandler
	 *          the receiver.
	 * @return this reference for chaining.
	 */
	public final BusMultiplexer unsubscribe(BusMessageHandler noteHandler) {
		if (broadcasting) {
			if (!pendingUnsubscriptions.contains(noteHandler)) {
				pendingUnsubscriptions.add(noteHandler);
			}
		}
		else {
			receivers.remove(noteHandler);
		}
		return this;
	}

	/**
	 * Pass an {@link Object} to all subscribed handlers.
	 * 
	 * @param globals
	 *          global registry
	 * @param note
	 *          the message object.
	 */
	public final void broadcast(Globals globals, Object note) {
		broadcasting = true;
		for (BusMessageHandler receiver : receivers) {
			receiver.onBusMessage(globals, note);
		}
		broadcasting = false;

		// We have to do this because Vector doesn't like to be modified while being
		// iterated.
		if (pendingSubscriptions.size() > 0) {
			for (BusMessageHandler pendingSubscription : pendingSubscriptions) {
				subscribe(pendingSubscription);
			}
			pendingSubscriptions.clear();
		}
		if (pendingUnsubscriptions.size() > 0) {
			for (BusMessageHandler pendingUnsubscription : pendingUnsubscriptions) {
				unsubscribe(pendingUnsubscription);
			}
			pendingUnsubscriptions.clear();
		}
	}

}
