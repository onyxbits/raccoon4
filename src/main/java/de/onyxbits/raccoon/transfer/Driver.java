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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Backend driver for {@link TransferWorker}S. Takes care of making sure things
 * run on the right thread.
 * 
 * @author patrick
 * 
 */
class Driver implements Runnable, ActionListener {

	private Thread workerThread;
	private TransferWorker worker;
	private TransferManager manager;
	private byte[] buffer;
	private Exception error;
	private boolean waiting;
	private float complete;
	private long totalAmount;

	private int state;
	protected static final int PENDING = 0;
	protected static final int STARTING = 1;
	protected static final int TRANSFERRING = 2;
	protected static final int CANCELLED = 3;
	protected static final int FAILED = 4;
	protected static final int TRANSFERRED = 5;
	protected static final int FINISHED = 6;

	public Driver(TransferWorker worker, TransferManager manager, int chunkSize) {
		this.worker = worker;
		this.manager = manager;
		this.buffer = new byte[chunkSize];
		this.state = PENDING;
	}

	public TransferWorker getWorker() {
		return worker;
	}

	public int getState() {
		return state;
	}

	public void beginTransfer() {
		state = STARTING;
		waiting = true;
		worker.getPeer().cancel.addActionListener(this);
		workerThread = new Thread(this);
		workerThread.start();
	}

	public void cancelTransfer() {
		if (workerThread != null) {
			workerThread.interrupt();
		}
	}

	@Override
	public void run() {
		switch (state) {
			case STARTING: {
				state = TRANSFERRING;
				transfer();
				break;
			}
			case TRANSFERRING: {
				JProgressBar progressBar = worker.getPeer().progressBar;
				if (complete >= 0f && complete <= 1f) {
					progressBar.setIndeterminate(false);
					int i = (int) (complete * progressBar.getMaximum());
					progressBar.setValue(i);
					progressBar.setString(TransferManager.humanReadableByteCount(
							totalAmount, false));
				}
				else {
					progressBar.setIndeterminate(true);
					progressBar.setString("");
				}
				waiting = true;
				break;
			}
			case TRANSFERRED: {
				JProgressBar progressBar = worker.getPeer().progressBar;
				progressBar.setIndeterminate(true);
				progressBar.setString(Messages.getString(TransferPeerBuilder.ID
						+ ".waiting"));
				worker.getPeer().view.setEnabled(false);
				worker.getPeer().cancel.setEnabled(false);
				break;
			}
			case FINISHED: {
				JProgressBar progressBar = worker.getPeer().progressBar;
				progressBar.setIndeterminate(false);
				progressBar.setValue(progressBar.getMaximum());
				progressBar.setString(Messages.getString(TransferPeerBuilder.ID
						+ ".complete"));
				worker.getPeer().view.setEnabled(true);
				worker.getPeer().cancel.setEnabled(false);
				break;
			}
			case CANCELLED: {
				JProgressBar progressBar = worker.getPeer().progressBar;
				progressBar.setIndeterminate(false);
				progressBar.setString(Messages.getString(TransferPeerBuilder.ID
						+ ".cancelled"));
				worker.getPeer().view.setEnabled(false);
				worker.getPeer().cancel.setEnabled(false);
				break;
			}
			case FAILED: {
				JProgressBar progressBar = worker.getPeer().progressBar;
				progressBar.setIndeterminate(false);
				progressBar.setString(Messages.getString(TransferPeerBuilder.ID
						+ ".error"));
				progressBar.setToolTipText(error.getLocalizedMessage());
				error.printStackTrace();
				worker.getPeer().view.setEnabled(false);
				worker.getPeer().cancel.setEnabled(false);
				break;
			}
		}
	}

	private void transfer() {
		InputStream in = null;
		OutputStream out = null;
		try {
			worker.onPrepare();
			checkInterrupted();
			while ((in = worker.onNextSource()) != null) {
				checkInterrupted();
				out = worker.onNextDestination();
				checkInterrupted();
				int amount;
				while ((amount = in.read(buffer)) != -1) {
					checkInterrupted();
					out.write(buffer, 0, amount);
					totalAmount += amount;
					complete = worker.onChunk(amount);
					if (waiting) {
						waiting = false;
						SwingUtilities.invokeLater(this);
					}
				}
				out.flush();
			}
			state = TRANSFERRED;
			SwingUtilities.invokeLater(this);
			worker.onComplete();
			state = FINISHED;
		}
		catch (InterruptedException e) {
			worker.onIncomplete(null);
			state = CANCELLED;
		}
		catch (Exception e) {
			worker.onIncomplete(e);
			state = FAILED;
			error = e;
		}
		manager.onDone(worker);
		SwingUtilities.invokeLater(this);
	}

	private void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == worker.getPeer().cancel) {
			worker.getPeer().cancel.setEnabled(false);
			workerThread.interrupt();
		}
	}
}
