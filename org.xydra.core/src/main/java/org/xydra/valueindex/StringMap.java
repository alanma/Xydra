package org.xydra.valueindex;

/*
 * TODO document
 */

public interface StringMap {
	void put(String key, String value);
	
	String get(String key);
	
	void remove(String key);
}
