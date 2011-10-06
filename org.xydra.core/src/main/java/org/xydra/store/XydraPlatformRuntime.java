package org.xydra.store;

import org.xydra.base.XID;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * The binding for platform-specific {@link XydraRuntime} implementation.
 * 
 * @author xamde
 * 
 */
public interface XydraPlatformRuntime {
	
	/**
	 * JCache Features Not Supported (at least not on GAE)
	 * <ol>
	 * <li>
	 * The JCache listener API is partially supported for listeners that can
	 * execute during the processing of a app's API call, such as for onPut and
	 * onRemove listeners. Listeners that require background processing, like
	 * onEvict, are not supported.</li>
	 * <li>
	 * An app can test whether the cache contains a given key, but it cannot
	 * test whether the cache contains a given value (containsValue() is not
	 * supported).</li>
	 * <li>
	 * An app cannot dump the contents of the cache's keys or values.</li>
	 * <li>
	 * An app cannot manually reset cache statistics.</li>
	 * <li>
	 * Asynchronous cache loading is not supported.</li>
	 * <li>
	 * The put() method does not return the previous known value for a key. It
	 * always returns null.</li>
	 * 
	 * @return a new instance of a platform specific Cache implementation or the
	 *         Java default version if no other version has been configured.
	 */
	public IMemCache getMemCache();
	
	/**
	 * @param repositoryId XID of new repository
	 * @return a new instance of a platform specific {@link XydraPersistence}
	 *         implementation with the given repositoryId
	 */
	public XydraPersistence getPersistence(XID repositoryId);
	
	/**
	 * Signals the Xydra platform that the current (web) request has been
	 * finished.
	 * 
	 * A server implementation might wish to e.g. clear thread-local variables
	 * to defend against a web container which is recycling threads.
	 */
	public void finishRequest();
	
	/**
	 * Signals the Xydra platform that a new (web) request has been started.
	 * 
	 * A server implementation might wish to e.g. clear thread-local variables
	 * to defend against a web container which is recycling threads.
	 */
	public void startRequest();
	
}
