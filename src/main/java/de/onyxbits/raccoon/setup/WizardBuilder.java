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
package de.onyxbits.raccoon.setup;

import javax.swing.JButton;

import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;

/**
 * Superclass for the individual setup screens.
 * 
 * @author patrick
 * 
 */
abstract class WizardBuilder extends AbstractPanelBuilder {

	protected JButton next;
	protected JButton previous;

	/**
	 * Called every time this dialog is shown to the user.
	 * 
	 * @param globals
	 *          registry.
	 * @param next
	 *          the next button
	 * @param previous
	 *          the previous button.
	 */
	public final void onActivate(JButton next, JButton previous) {
		this.next = next;
		this.previous = previous;
		onActivate();
	}

	/**
	 * Subclasses should override this to enable/disable buttons.
	 */
	protected void onActivate() {
		next.setEnabled(false);
		previous.setEnabled(false);
	}

	/**
	 * Send a message to show another dialog
	 * 
	 * @id class of the {@link WizardBuilder} to show;
	 */
	public void show(Class<?> id) {
		postBusMessage(new StateMessage(StateMessage.SHOW, id.getName()));
	}

	/**
	 * Tell the lifecycle that we are ready to go
	 */
	public void finish() {
		postBusMessage(new StateMessage(StateMessage.FINISH, null));
	}

	/**
	 * Called when the next button is clicked
	 */
	protected abstract void onNext();

	/**
	 * Called when the previous button is clicked
	 */
	protected abstract void onPrevious();

	/**
	 * Trigger a background thread to do long running stuff
	 */
	protected final void doInBackground() {
		new WizardWorker(this).execute();
	}

	/**
	 * Executes on a background thread when {@link WizardBuilder#doInBackground()}
	 * is called.
	 */
	protected void onDoInBackground() {
	}

	/**
	 * Called on the EDT after {@link WizardBuilder#onDoInBackground()} returns
	 */
	protected void onDone() {
	}

	/**
	 * Called after activation
	 * 
	 * @return what to put into the titlestrips subtitle.
	 */
	protected String getSubtitle() {
		return Messages.getString(this.getClass().getSimpleName() + ".subtitle");
	}
	
	/**
	 * Convenience method for sending a message
	 * 
	 * @param message
	 *          what to post.
	 */
	protected void postBusMessage(Object message) {
		globals.get(LifecycleManager.class).sendBusMessage(message);
	}

}
