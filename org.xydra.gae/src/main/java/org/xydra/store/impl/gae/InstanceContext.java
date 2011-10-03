package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.changes.ThreadRevisionInfo;


/**
 * A context object that can be passed around during a single web request.
 * Within one request, it is considered OK to retrieve fresh data only once from
 * the back-end store.
 * 
 * @author xamde
 */
public class InstanceContext {
	
	private static final Logger log = LoggerFactory.getLogger(InstanceContext.class);
	
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
	
	/**
	 * A map from modelAddress to {@link ThreadRevisionInfo} for each
	 * {@link Thread}
	 */
	private static ThreadLocal<Map<String,ThreadRevisionInfo>> threadContext;
	
	/**
	 * @return a map unique for each thread. Never null.
	 */
	public static Map<String,ThreadRevisionInfo> getThreadContext() {
		synchronized(InstanceContext.class) {
			if(threadContext == null) {
				threadContext = new ThreadLocal<Map<String,ThreadRevisionInfo>>();
			}
		}
		Map<String,ThreadRevisionInfo> map = threadContext.get();
		if(map == null) {
			map = new HashMap<String,ThreadRevisionInfo>();
			threadContext.set(map);
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
			log.info("Clear ThreadLocal context of " + AboutAppEngine.getThreadInfo());
			Map<String,ThreadRevisionInfo> tc = getThreadContext();
			for(String key : tc.keySet()) {
				ThreadRevisionInfo threadRevInfo = tc.get(key);
				threadRevInfo.clear();
			}
			threadContext.set(null);
		}
	}
	
}
