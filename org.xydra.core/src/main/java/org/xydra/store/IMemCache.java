package org.xydra.store;

import java.util.Collection;
import java.util.Map;


public interface IMemCache extends Map<Object,Object> {
	
	/**
	 * @return a human-readable String with statistical information
	 */
	String stats();
	
	/**
	 * Batch get. Performs a get of multiple keys at once. This is more
	 * efficient than multiple separate calls to get(Object), and allows a
	 * single call to both test for contains(Object) and also fetch the value,
	 * because the return will not include mappings for keys not found.
	 * 
	 * @param keys a collection of keys for which values should be retrieved
	 * @return a mapping from keys to values of any entries found. If a
	 *         requested key is not found in the cache, the key will not be in
	 *         the returned Map.
	 */
	Map<Object,Object> getAll(Collection<Object> keys);
	
	/**
	 * Put entityToBeCached if currently the {@link #get(Object)} would return
	 * null.
	 * 
	 * @param key
	 * @param entityToBeCached
	 */
	void putIfValueIsNull(Object key, Object entityToBeCached);
	
}
