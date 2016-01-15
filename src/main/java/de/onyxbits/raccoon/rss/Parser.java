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
package de.onyxbits.raccoon.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;


public class Parser {
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String LINK = "link";
	static final String ITEM = "item";
	static final String PUB_DATE = "pubDate";
	static final String GUID = "guid";
	static final String CREATOR = "creator";
	static final String AUTHOR = "author";

	private URL url;

	public Parser(URL url) {
		this.url = url;
	}

	public List<FeedItem> readFeed() {
		FeedItem item = null;
		Vector<FeedItem> ret = new Vector<FeedItem>();
		DateFormat formatter = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		InputStream in = null;
		try {
			in = url.openStream();
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					String localPart = event.asStartElement().getName().getLocalPart();
					if (localPart.equals(ITEM)) {
						item = new FeedItem();
						item.setSource(url.toString());
					}
					if (localPart.equals(TITLE) && item != null) {
						item.setTitle(getCharacterData(event, eventReader));
					}
					if (localPart.equals(DESCRIPTION) && item != null) {
						item.setDescription(getCharacterData(event, eventReader));
					}
					if (localPart.equals(LINK) && item != null) {
						item.setLink(getCharacterData(event, eventReader));
					}
					if (localPart.equals(AUTHOR) && item != null) {
						item.setAuthor(getCharacterData(event, eventReader));
					}
					if (localPart.equals(CREATOR) && item != null) {
						item.setAuthor(getCharacterData(event, eventReader));
					}
					if (localPart.equals(GUID) && item != null) {
						item.setGuid(getCharacterData(event, eventReader));
					}
					if (localPart.equals(PUB_DATE) && item != null) {
						try {
							item.setPublished(new Timestamp(formatter.parse(
									getCharacterData(event, eventReader)).getTime()));
						}
						catch (Exception e) {
							item.setPublished(new Timestamp(System.currentTimeMillis()));
							//e.printStackTrace();
						}
					}
				}
				if (event.isEndElement()) {
					if (event.asEndElement().getName().getLocalPart().equals(ITEM)) {
						ret.add(item);
						item = null;
					}
				}
			}
		}
		catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
		}

		try {
			in.close();
		}
		catch (Exception e) {
		}
		return ret;
	}

	private String getCharacterData(XMLEvent event, XMLEventReader eventReader)
			throws XMLStreamException {
		String result = "";
		event = eventReader.nextEvent();
		if (event instanceof Characters) {
			result = event.asCharacters().getData();
		}
		return result;
	}

}