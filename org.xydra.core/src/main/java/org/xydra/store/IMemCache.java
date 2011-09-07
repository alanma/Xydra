package org.xydra.store;

import java.util.Collection;
import java.util.Map;


public interface IMemCache extends Map<String,Object> {
	
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
	Map<String,Object> getAll(Collection<String> keys);
	
	/**
	 * Put entityToBeCached if currently the {@link #get(Object)} would return
	 * null.
	 * 
	 * @param key
	 * @param entityToBeCached
	 */
	void putIfValueIsNull(String key, Object entityToBeCached);
	
	/**
	 * Store newValue only if no other value has been stored since oldValue was
	 * retrieved. oldValue is an #IdentifiableValue that was returned from a
	 * previous call to getIdentifiable(java.lang.Object). If another value in
	 * the cache for key has been stored, or if this cache entry has been
	 * evicted, then nothing is stored by this call and false is returned.
	 * 
	 * @param key never null
	 * @param oldIdentifiableValue never null, created via
	 *            {@link #getIdentifiable(String)}
	 * @param newValue can be null
	 * @return true if changed something. Note that storing the same value again
	 *         does count as a "change" for this purpose.
	 */
	boolean putIfUntouched(String key, IdentifiableValue oldIdentifiableValue, Object newValue);
	
	IdentifiableValue getIdentifiable(String key);
	
	public static interface IdentifiableValue {
		Object getValue();
	}
	
	/**
	 * Batch increment.
	 * 
	 * @param offsets a mapping of separate controllable offsets for each key
	 *            individually. Good for incrementing by a sum and a count in
	 *            parallel.
	 * @param initialValue an initial value for the keys to take on if they are
	 *            not already present in the cache.
	 * @return mapping keys to their new values; values will be null if they
	 *         could not be incremented for whatever reason
	 */
	Map<String,Long> incrementAll(Map<String,Long> offsets, long initialValue);
	
}
