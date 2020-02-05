package de.onyxbits.raccoon.net;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Dirty hack to maintain key order.
 * 
 * 
 * @author patrick
 * 
 */
class OrderedHashtable extends Hashtable<Integer, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Vector<Integer> ordered = new Vector<>();

	public synchronized Object put(Integer key, Object value) {
		super.put(key, value);
		ordered.add(key);
		return value;
	}

	public synchronized Enumeration<Integer> keys() {
		return ordered.elements();
	}
}
