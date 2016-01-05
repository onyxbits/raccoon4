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
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

/**
 * Backend class doing the plumbing.
 * 
 * @author patrick
 * 
 */
class ImageLoaderWorker extends SwingWorker<Object, ImageLoaderItem> {

	private Queue<ImageLoaderItem> orders;
	private ImageLoaderService service;
	private WeakHashMap<String, Image> cache = new WeakHashMap<String, Image>();
	private static final BufferedImage BLANK = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB);

	public ImageLoaderWorker(Queue<ImageLoaderItem> orders,
			ImageLoaderService service, WeakHashMap<String, Image> cache) {
		this.service = service;
		this.orders = orders;
		this.cache = cache;
	}

	@Override
	protected Object doInBackground() throws Exception {
		while (!orders.isEmpty() && !isCancelled()) {
			ImageLoaderItem item = orders.remove();
			try {
				ImageLoaderItem res = new ImageLoaderItem();
				res.source = item.source;
				res.callback = item.callback;
				if (cache.containsKey(item.source)) {
					// The URL might have been requested again while the previous worker
					// was fetching it.
					res.image = cache.get(item.source);
				}
				else {
					res.image = ImageIO.read(new URL(item.source));
				}
				publish(res);
			}
			catch (Exception e) {
			}
		}
		return null;
	}

	@Override
	public void process(List<ImageLoaderItem> items) {
		for (ImageLoaderItem item : items) {
			// TODO: Make a callback for images that fail to load.
			if (item.callback != null && item.image != null) {
				item.callback.onImageReady(item.source, item.image);
			}
			cache.put(item.source, item.image);
		}
	}

	@Override
	public void done() {
		if (isCancelled()) {
			// We should probably find a better icon for this.
			while (!orders.isEmpty()) {
				ImageLoaderItem item = orders.remove();
				item.callback.onImageReady(item.source, BLANK);
			}
		}
		service.request(null, null);
	}

}
