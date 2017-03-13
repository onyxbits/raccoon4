/*
 * Copyright 2017 Patrick Ahlbrecht
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
package de.onyxbits.raccoon.db;

public class VariableEvent extends DatasetEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The current value of the variable. Null if the variable was unset
	 */
	public final String newValue;

	/**
	 * The previous value of the variable. Null if the variable wasn't set before.
	 */
	public final String oldValue;

	/**
	 * Name of the variable that was changed.
	 */
	public final String name;

	public VariableEvent(VariableDao source, int op, String name,
			String oldValue, String newValue) {
		super(source, op);
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.name = name;
	}

}
