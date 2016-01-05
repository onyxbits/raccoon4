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
package de.onyxbits.raccoon.transfer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A transfer worker plugs into the UI of the {@link TransferManager} and takes
 * care of copying a dataset from location A to location B.
 * 
 * @author patrick
 * 
 */
public interface TransferWorker {

	/**
	 * Get a peer through which the worker plugs into the {@link TransferManager}
	 * UI
	 * 
	 * @return always the same object.
	 */
	public TransferPeerBuilder getPeer();

	/**
	 * A transfer may consist of one or more datasets ("files").
	 * 
	 * @return the stream to read from next or null if everything has been
	 *         transferred.
	 */
	public InputStream onNextSource() throws Exception;

	/**
	 * Get the stream to write to next.
	 * 
	 * @return where to write to.
	 * @throws Exception
	 */
	public OutputStream onNextDestination() throws Exception;

	/**
	 * Calculate the progress of the transfer. This method is called repeatedly
	 * whenever a chunk of data has been successfully transferred.
	 * 
	 * @param size
	 *          the number of bytes that were transferred since this method was
	 *          called last.
	 * @return percentage of completion (a value between 0 and 1 or -1 to indicate
	 *         that the progress cannot be computed.
	 */
	public float onChunk(int size);

	/**
	 * Called before any transfer takes place.
	 */
	public void onPrepare() throws Exception;

	/**
	 * Called after all requested data has been copied.
	 */
	public void onComplete() throws Exception;

	/**
	 * Called when the transfer ends prematurely (either because of an exception
	 * or because the user cancelled it).
	 * 
	 * @param e
	 *          the exception that caused the failure or null if aborted by the
	 *          user.
	 */
	public void onIncomplete(Exception e);

}
