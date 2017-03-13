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

import java.util.EventObject;

/**
 * Contains details on which dataset changed and how it did change. DOAs that
 * provide more than CRUD operations should subclass this and give high level
 * details on what happened.
 * 
 * @author patrick
 * 
 */
public class DatasetEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Something was inserted into the dataset
	 */
	public static final int CREATION = 1;

	/**
	 * Something got deleted from the dataset
	 */
	public static final int DELETION = 2;

	/**
	 * The dataset was modified
	 */
	public static final int MODIFICATION = 4;

	/**
	 * DAO specific ops must use numbers larger than this.
	 */
	public static final int CUSTOM_OP = 256;

	/**
	 * Bitfield, giving details of the operation that took place.
	 */
	public final int op;

	/**
	 * Create a new event object
	 * 
	 * @param source
	 *          the DAO that is responsible for this event.
	 * @param op
	 *          the database operation
	 */
	public DatasetEvent(DataAccessObject source, int op) {
		super(source);
		this.op = op;
	}

	public boolean isModification() {
		return (op & MODIFICATION) == MODIFICATION;
	}

	public boolean isDeletion() {
		return (op & DELETION) == DELETION;
	}

	public boolean isCreation() {
		return (op & CREATION) == CREATION;
	}

}
