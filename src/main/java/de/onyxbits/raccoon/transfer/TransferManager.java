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

import java.util.Vector;

import de.onyxbits.raccoon.gui.WindowTogglers;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * Backend queue for scheduling transfers. This manager ensures that only one
 * transfer (per queue) is active at any given time, so we don't clog up
 * bottlenecks.
 * 
 * @author patrick
 * 
 */
public class TransferManager {

	/**
	 * Queue to schedule on.
	 */
	public static final int WAN = 0;

	/**
	 * Queue to schedule on.
	 */
	public static final int LAN = 1;

	/**
	 * Queue to schedule on.
	 */
	public static final int USB = 2;

	private Vector<Driver> wanWorkers = new Vector<Driver>();
	private Vector<Driver> lanWorkers = new Vector<Driver>();
	private Vector<Driver> usbWorkers = new Vector<Driver>();
	private TransferViewBuilder peer;
	private int unfinished;

	/**
	 * Schedule a transfer. It will start whenever the pipeline is empty.
	 * 
	 * @param globals
	 *          registry
	 * @param worker
	 *          the worker.
	 * @param queue
	 *          LAN, WAN or USB pipeline.
	 */
	public void schedule(Globals globals, TransferWorker worker, int queue) {
		switch (queue) {
			case WAN: {
				wanWorkers.add(new Driver(worker, this, 1024 * 8));
				break;
			}
			case LAN: {
				lanWorkers.add(new Driver(worker, this, 1024 * 16));
				break;
			}
			case USB: {
				// 64kb is the maximum allowed by the ADB protocol.
				usbWorkers.add(new Driver(worker, this, 1024 * 64));
				break;
			}
			default: {
				throw new IllegalArgumentException("Unknown queue");
			}
		}
		// Force the creation of the transfer view. It will register itself as a
		// peer and we can pass the worker to it.
		globals.get(LifecycleManager.class).getWindow(TransferViewBuilder.ID);
		peer.add(worker);

		// TBD: Should a transfer really popup the control window? Pro: new users
		// need the feedback that something is happening. Con: advanced users might
		// be annoyed by it. For now let's side with the new users until the GUI
		// matures to a point that allows more subtle feedback.
		globals.get(WindowTogglers.class).transfers.showWindow();
		globals.get(LifecycleManager.class).getWindow(TransferViewBuilder.ID)
				.toFront();
		unfinished++;
		adjustBanner();
		beginNext();
	}

	protected void setPeer(TransferViewBuilder peer) {
		this.peer = peer;
	}

	/**
	 * Callback for when a worker is finished. 
	 * @param driver
	 */
	protected void onDone(TransferWorker worker) {
		unfinished--;
		adjustBanner();
		beginNext();
	}

	private void adjustBanner() {
		if (peer != null) {
			// TODO: put the number of unfinished transfers into the diloags subtitle.
		}
	}

	/**
	 * Start the next pending worker on each pipeline. Do nothing if there are no
	 * more waiting workers or if a download is currently on progress.
	 */
	private void beginNext() {
		int state;
		for (Driver worker : wanWorkers) {
			state = worker.getState();
			if (state == Driver.TRANSFERRING || state == Driver.STARTING) {
				// Only allow one running at a time.
				break;
			}
			if (worker.getState() == Driver.PENDING) {
				worker.beginTransfer();
				break;
			}
		}

		for (Driver worker : lanWorkers) {
			state = worker.getState();
			if (state == Driver.TRANSFERRING || state == Driver.STARTING) {
				// Only allow one running at a time.
				break;
			}
			if (worker.getState() == Driver.PENDING) {
				worker.beginTransfer();
				break;
			}
		}

		for (Driver worker : usbWorkers) {
			state = worker.getState();
			if (state == Driver.TRANSFERRING || state == Driver.STARTING) {
				// Only allow one running at a time.
				break;
			}
			if (worker.getState() == Driver.PENDING) {
				worker.beginTransfer();
				break;
			}
		}
	}

	/**
	 * check if a given worker is in the queue.
	 * 
	 * @param tw
	 *          the worker
	 * @return true if this worker is in the queue
	 */
	public boolean contains(TransferWorker tw) {
		return wanWorkers.contains(tw);
	}

	/**
	 * Check if there are ongoing transfers
	 * 
	 * @return true if no worker is transferring and there are no more scheduled
	 *         jobs.
	 */
	public boolean isIdle() {
		synchronized (wanWorkers) {
			for (Driver worker : wanWorkers) {
				if (inProgress(worker)) {
					return false;
				}
			}
		}
		synchronized (lanWorkers) {
			for (Driver worker : lanWorkers) {
				if (inProgress(worker)) {
					return false;
				}
			}
		}
		synchronized (usbWorkers) {
			for (Driver worker : usbWorkers) {
				if (inProgress(worker)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean inProgress(Driver driver) {
		int state = driver.getState();
		return (!(state == Driver.CANCELLED || state == Driver.FAILED || state == Driver.FINISHED));
	}

	/**
	 * Format a filesize
	 * 
	 * @param bytes
	 *          number of bytes
	 * @param si
	 *          true to use 1000 bytes as 1kb false to use 1024 bytes as 1kb.
	 * @return a formatted string
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public void cancelAll() {
		for (Driver worker : wanWorkers) {
			worker.cancelTransfer();
		}
		for (Driver worker : lanWorkers) {
			worker.cancelTransfer();
		}
		for (Driver worker : usbWorkers) {
			worker.cancelTransfer();
		}
	}

}
