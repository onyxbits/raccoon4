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
package de.onyxbits.raccoon.gplay;

import java.util.List;
import java.util.Vector;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

/**
 * Wrapper for getting data out of a DocV2 instance.
 * 
 * @author patrick
 * 
 */
public class DocUtil {

	/**
	 * Get the Url of an app icon
	 * 
	 * @param doc
	 *          the doc instance
	 * @return the url to the icon on the web or null.
	 */
	public static String getAppIconUrl(DocV2 doc) {
		if (doc != null) {
			for (int i = 0; i < doc.getImageCount(); i++) {
				if (doc.getImage(i).getImageType() == 4) { // Magic Number
					return doc.getImage(i).getImageUrl();
				}
			}
		}
		return null;
	}

	/**
	 * Get the URLs of the screenshots
	 * 
	 * @param doc
	 *          doc instance
	 * @return a list of screenshot urls. May be empty, may not be null
	 */
	public static List<String> getScreenShots(DocV2 doc) {
		Vector<String> ret = new Vector<String>();
		if (doc != null) {
			for (int i = 0; i < doc.getImageCount(); i++) {
				if (doc.getImage(i).getImageType() == 1) { // Magic Number
					ret.add(doc.getImage(i).getImageUrl());
				}
			}
		}
		return ret;
	}

	/**
	 * Get the video associated with the app
	 * 
	 * @param doc
	 *          doc instance
	 * @return video url or null
	 */
	public static String getVideoUrl(DocV2 doc) {
		if (doc != null) {
			for (int i = 0; i < doc.getImageCount(); i++) {
				if (doc.getImage(i).getImageType() == 3) { // Magic Number
					return doc.getImage(i).getImageUrl();
				}
			}
		}
		return null;
	}

}
