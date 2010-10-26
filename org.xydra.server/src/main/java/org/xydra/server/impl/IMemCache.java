package org.xydra.server.impl;

import java.util.Map;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInJava;


/**
 * The essence of what GoogleAppengine supports in its JCache implementation.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine
@RunsInJava
public interface IMemCache {
	
	/**
	 * Overwrite existing keys-value assignment, if present.
	 * 
	 * @param key
	 * @param value
	 */
	void put(String key, byte[] value);
	
	/**
	 * @param key
	 * @return the entry stored for the given key
	 */
	byte[] get(String key);
	
	/**
	 * Batch put
	 * 
	 * @param map
	 */
	void putAll(Map<String,byte[]> map);
	
	/**
	 * Remove the given key-entry, if present.
	 * 
	 * @param key
	 */
	void remove(String key);
	
	/**
	 * @param key
	 * @return true if this cache contains a value for the given key.
	 */
	boolean containsKey(String key);
	
	/**
	 * @return true if the cache is empty
	 */
	boolean isEmpty();
	
	/**
	 * @return the number of cache entries (key-value)
	 */
	long size();
	
	/*
	 * other methods are not supported on purpose, because GAE doesn't support
	 * more of the JCache API
	 */

}
