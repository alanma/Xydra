package org.xydra.xgae.memcache.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;

/**
 * @author xamde
 *
 */
@RunsInGWT(true)
public interface IMemCache extends Map<String, Object> {

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
	 * @param keys
	 *            a collection of keys for which values should be retrieved
	 * @return a mapping from keys to values of any entries found. If a
	 *         requested key is not found in the cache, the key will not be in
	 *         the returned Map.
	 */
	Map<String, Object> getAll(Collection<String> keys);

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
	 * @param key
	 * @param oldIdentifiableValue
	 *            created via {@link #getIdentifiable(String)}
	 * @param newValue
	 * @return true if changed something. Note that storing the same value again
	 *         does count as a "change" for this purpose.
	 */
	boolean putIfUntouched(@NeverNull String key,
			@NeverNull IdentifiableValue oldIdentifiableValue, @CanBeNull Object newValue);

	IdentifiableValue getIdentifiable(String key);

	public static interface IdentifiableValue {
		Object getValue();
	}

	/**
	 * Use this method instead of {@link #put(String, Object)} to have checked
	 * {@link IOException}
	 *
	 * Associates the specified value with the specified key in this map
	 * (optional operation). If the map previously contained a mapping for the
	 * key, the old value is replaced by the specified value. (A map <tt>m</tt>
	 * is said to contain a mapping for a key <tt>k</tt> if and only if
	 * {@link #containsKey(Object) m.containsKey(k)} would return <tt>true</tt>
	 * .)
	 *
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
	 *         if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
	 *         can also indicate that the map previously associated
	 *         <tt>null</tt> with <tt>key</tt>, if the implementation supports
	 *         <tt>null</tt> values.)
	 * @throws UnsupportedOperationException
	 *             if the <tt>put</tt> operation is not supported by this map
	 * @throws ClassCastException
	 *             if the class of the specified key or value prevents it from
	 *             being stored in this map
	 * @throws NullPointerException
	 *             if the specified key or value is null and this map does not
	 *             permit null keys or values
	 * @throws IllegalArgumentException
	 *             if some property of the specified key or value prevents it
	 *             from being stored in this map
	 * @throws IOException
	 *             if an underlying IO system fails
	 */
	Object putChecked(String key, Object value) throws IOException;

	/**
	 * Batch increment.
	 *
	 * @param offsets
	 *            a mapping of separate controllable offsets for each key
	 *            individually. Good for incrementing by a sum and a count in
	 *            parallel.
	 * @param initialValue
	 *            an initial value for the keys to take on if they are not
	 *            already present in the cache.
	 * @return mapping keys to their new values; values will be null if they
	 *         could not be incremented for whatever reason
	 */
	Map<String, Long> incrementAll(Map<String, Long> offsets, long initialValue);

}
