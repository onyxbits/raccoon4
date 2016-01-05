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
package de.onyxbits.raccoon.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * Stores
 * 
 * @author patrick
 * 
 * @param <T>
 */
public class HashStore<T> {
	private HashMap<String, T> mappings;
	private Random rng;

	private static final char[] SYMBOLS;

	static {
		StringBuilder tmp = new StringBuilder();
		for (char ch = '0'; ch <= '9'; ++ch)
			tmp.append(ch);
		for (char ch = 'a'; ch <= 'z'; ++ch)
			tmp.append(ch);
		SYMBOLS = tmp.toString().toCharArray();
	}

	public HashStore() {
		mappings = new HashMap<String, T>();
		rng = new Random(System.currentTimeMillis());
	}

	/**
	 * Register a file for sharing
	 * 
	 * @param file
	 *          the file to share. May be a directory.
	 * @return a unique, randomly generated key by which the file may be retrieved
	 *         until it gets unregistered.
	 */
	public synchronized String register(T item) {
		String ret;
		do {
			ret = nextKey(12);
		} while (mappings.containsKey(ret));
		mappings.put(ret, item);
		return ret;
	}

	public String nextKey(int len) {
		char[] buf = new char[len];
		for (int idx = 0; idx < buf.length; ++idx) {
			buf[idx] = SYMBOLS[rng.nextInt(SYMBOLS.length)];
		}
		return new String(buf);
	}

	/**
	 * clear all registrations.
	 */
	public synchronized void clear() {
		mappings.clear();
	}

	/**
	 * Lookup a file by its key.
	 * 
	 * @param key
	 *          the key
	 * @return the requested file or null if not registered.
	 */
	public synchronized T lookup(String key) {
		return mappings.get(key);
	}

	/**
	 * List all keys
	 */
	public synchronized Iterator<String> list() {
		return mappings.keySet().iterator();
	}

}
