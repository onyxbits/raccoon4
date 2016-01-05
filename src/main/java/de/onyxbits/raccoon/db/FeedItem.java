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
package de.onyxbits.raccoon.db;

import java.sql.Date;

/**
 * An item in an RSS feed. Multiple feeds can be stored in the same table. The
 * feedLink property is used to tell them apart.
 * 
 * @author patrick
 * 
 */
public class FeedItem {

	/**
	 * The item was fetched in the current session and was not clicked.
	 */
	public static final int JUSTIN = 0;

	/**
	 * The item has not been marked as read
	 */
	public static final int UNREAD = 1;

	/**
	 * Old news
	 */
	public static final int SEEN = 2;

	private String source;

	private String title;

	private String description;

	private String link;

	private Date published;

	private int state;

	private String guid;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = clip(description, 2048);
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = clip(link, 255);
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getPublished() {
		return published;
	}

	public void setPublished(Date date) {
		this.published = date;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public static String clip(String input, int maxlen) {
		if (input.length() <= maxlen) {
			return input;
		}
		else {
			return input.substring(0, maxlen);
		}
	}

	@Override
	public String toString() {
		return "FeedMessage [title=" + title + ", description=" + description
				+ ", link=" + link + ", guid=" + guid + "]";
	}

}
