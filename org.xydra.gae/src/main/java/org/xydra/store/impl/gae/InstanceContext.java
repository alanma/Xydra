package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A context object that can be passed around during a single web request.
 * Within one request, it is considered OK to retrieve fresh data only once from
 * the back-end store.
 * 
 * @author xamde
 */
public class InstanceContext {
	
	private static Map<String,Object> sharedCache;
	
	/**
	 * @return the static cache. Use with care (about race conditions).
	 */
	public static synchronized Map<String,Object> getInstanceCache() {
		if(sharedCache == null) {
			sharedCache = new ConcurrentHashMap<String,Object>();
		}
		return sharedCache;
	}
	
	public static void clear() {
		if(sharedCache != null) {
			sharedCache.clear();
		}
	}
	
	private static ThreadLocal<Map<String,Object>> threadContext;
	
	/**
	 * @return a map unique for each thread. Never null.
	 */
	public static synchronized Map<String,Object> getTheadContext() {
		Map<String,Object> map;
		if(threadContext == null) {
			threadContext = new ThreadLocal<Map<String,Object>>();
			map = threadContext.get();
			if(map == null) {
				map = new HashMap<String,Object>();
				threadContext.set(map);
			}
			return map;
		} else {
			map = threadContext.get();
		}
		return map;
	}
	
	/**
	 * Make sure thread context is empty for calling thread.
	 */
	public static synchronized void clearThreadContext() {
		if(threadContext == null) {
			// done, cannot contain content
		} else {
			threadContext.set(null);
		}
	}
	
}
