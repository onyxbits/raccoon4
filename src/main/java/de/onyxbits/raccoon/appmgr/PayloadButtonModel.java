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
package de.onyxbits.raccoon.appmgr;

import javax.swing.ButtonModel;
import javax.swing.JToggleButton.ToggleButtonModel;

/**
 * A {@link ButtonModel} that can carry a user object.
 * 
 * @author patrick
 * 
 * @param <T>
 */
public class PayloadButtonModel<T> extends ToggleButtonModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private T payload;

	public PayloadButtonModel(T payload) {
		super();
		this.payload = payload;
	}

	public T getPayload() {
		return payload;
	}
}
