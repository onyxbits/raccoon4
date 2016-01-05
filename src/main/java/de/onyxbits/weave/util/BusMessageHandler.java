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

import de.onyxbits.weave.Globals;
import de.onyxbits.weave.Lifecycle;

/**
 * In complex applications, the {@link Lifecycle} probably doesn't want to
 * handle (all) bus messages by itself, but rather forward them to more
 * sophisticated code. This interface defines the receiving logic when using an
 * {@link BusMultiplexer}.
 * 
 * @author patrick
 * 
 */
public interface BusMessageHandler {

	/**
	 * Called when an {@link Object} needs to be processed. It is the implementors
	 * responsibility to decide whether or not it can handle the message.
	 * 
	 * @param globals
	 *          registry
	 * @param message
	 *          the intent to handle.
	 */
	public void onBusMessage(Globals globals, Object message);
}
