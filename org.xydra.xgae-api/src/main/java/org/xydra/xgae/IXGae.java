package org.xydra.xgae;

import org.xydra.xgae.datastore.api.IDatastore;
import org.xydra.xgae.memcache.api.IMemCache;


/**
 * Central entry point for accessing the wrapper around a Google-Appengine-like
 * environment, which provides a datastore and a memcache.
 * 
 * @author xamde
 */
public interface IXGae {
    
    /**
     * @return a datastore that supports sync and async operations
     */
    IDatastore datastore();
    
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
    IMemCache memcache();
    
    /**
     * @return a short, informative string about the provider implementation
     */
    String getProviderVersionString();
    
    /**
     * @return true if app is running on a real remote server
     */
    boolean inProduction();
    
    /**
     * @return true iff in a local development mode
     */
    boolean inDevelopment();
    
    String getInstanceId();
    
    /**
     * @return true if on AppEngine (regardless whether in production or in
     *         development mode)
     */
    boolean onAppEngine();
    
    /**
     * @return 'inProduction', 'inDevelopment' or 'notOnAppengine'
     */
    String inModeAsString();
    
    /**
     * @return number of milliseconds after which the environment we live in
     *         cuts us hard; -1 for no limit;
     */
    long getRuntimeLimitInMillis();
    
}
