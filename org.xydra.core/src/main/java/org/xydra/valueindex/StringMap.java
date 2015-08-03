package org.xydra.valueindex;

/**
 * Simple Map-interface for Strings for use with GWT.
 *
 * @author kaidel
 *
 */

public interface StringMap {

	/**
	 * Associates the given value to the given key.
	 *
	 * @param key The key under which the given value is to be stored.
	 * @param value The value which is to be stored
	 *
	 * @return the old value which was associated with the given key or null, if
	 *         no value was associated with the key before.
	 */
	String put(String key, String value);

	/**
	 * Returns the value which is associated with the given key.
	 *
	 * @param key The key
	 * @return the value which is associated with the given key
	 */
	String get(String key);

	/**
	 * Removes the given key and its associated value from the map.
	 *
	 * @param key The key which is to be removed.
	 */
	void remove(String key);
}
