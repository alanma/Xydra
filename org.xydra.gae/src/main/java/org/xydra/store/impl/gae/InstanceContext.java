package org.xydra.store.impl.gae;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A context object that can be passed around during a single web request.
 * Within one request, it is considered OK to retrieve fresh data only once from
 * the back-end store.
 * 
 * @author xamde
 */
public class InstanceContext {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(InstanceContext.class);
	
	private static Map<String,Object> sharedCache;
	
	/**
	 * @return the static cache. Use with care (about race conditions).
	 */
	public static synchronized Map<String,Object> getInstanceCache() {
		if(sharedCache == null) {
			// FIXME 2012-02 use Guava limited cache here to avoid memory leak
			sharedCache = new ConcurrentHashMap<String,Object>();
		}
		return sharedCache;
	}
	
	/**
	 * Clears instance context
	 */
	public static void clear() {
		clearInstanceContext();
	}
	
	public static void clearInstanceContext() {
		if(sharedCache != null) {
			sharedCache.clear();
		}
	}
	
}
