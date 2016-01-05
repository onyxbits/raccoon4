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
package de.onyxbits.weave.swing;

import java.awt.Image;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utility class for loading images asynchronously. Images are fetched serially
 * on a background thread in request order and cached internally.
 * <p>
 * This class is designed to allow multiple instances to coexist (e.g. to have
 * one loader for different categories of images or different hosts).
 * 
 * @author patrick
 * 
 */
public class ImageLoaderService {

	private WeakHashMap<String, Image> cache;

	private ImageLoaderWorker worker;
	private LinkedBlockingQueue<ImageLoaderItem> queue;

	public ImageLoaderService() {
		queue = new LinkedBlockingQueue<ImageLoaderItem>();
		cache = new WeakHashMap<String, Image>();
	}

	/**
	 * Enqueue a request to load an image.
	 * 
	 * @param callback
	 *          the object to notify when the image is ready to use
	 * @param source
	 *          the url to load the image from.
	 */
	public void request(ImageLoaderListener callback, String source) {
		// Big fat warning: source is of type String instead of URL because
		// URL.equals() involves a hostname lookup.
		Image img = cache.get(source);
		if (img != null) {
			callback.onImageReady(source, img);
			return;
		}
		if (callback != null && source != null) {
			ImageLoaderItem item = new ImageLoaderItem();
			item.callback = callback;
			item.source = source;
			try {
				queue.put(item);
			}
			catch (InterruptedException e) {
			}
		}
		if ((worker == null || worker.isDone()) && queue.size()>0) {
			worker = new ImageLoaderWorker(queue, this, cache);
			queue = new LinkedBlockingQueue<ImageLoaderItem>();
			worker.execute();
		}
	}

	/**
	 * Cancel all waiting operations.
	 */
	public void cancelPending() {
		if (worker != null && !worker.isDone()) {
			queue.clear();
			worker.cancel(true);
		}
	}
}
