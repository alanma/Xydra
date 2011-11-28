package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.changes.ThreadLocalGaeModelRevision;


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
			// FIXME !!! use Guava limited cache here
			sharedCache = new ConcurrentHashMap<String,Object>();
		}
		return sharedCache;
	}
	
	public static void clear() {
		clearInstanceContext();
		clearThreadContext();
	}
	
	public static void clearInstanceContext() {
		if(sharedCache != null) {
			sharedCache.clear();
		}
	}
	
	/**
	 * A map from modelAddress to {@link ThreadLocalGaeModelRevision} for each
	 * {@link Thread}
	 */
	private static ThreadLocal<Map<String,ThreadLocalGaeModelRevision>> threadContext;
	
	/**
	 * @return a map unique for each thread. Never null.
	 */
	public static Map<String,ThreadLocalGaeModelRevision> getThreadContext() {
		synchronized(InstanceContext.class) {
			if(threadContext == null) {
				threadContext = new ThreadLocal<Map<String,ThreadLocalGaeModelRevision>>();
			}
		}
		Map<String,ThreadLocalGaeModelRevision> map = threadContext.get();
		if(map == null) {
			map = new HashMap<String,ThreadLocalGaeModelRevision>();
			threadContext.set(map);
		}
		return map;
	}
	
	/**
	 * Make sure thread context is empty for calling thread.
	 */
	public static synchronized void clearThreadContext() {
		if(threadContext == null) {
			log.info("ThreadLocal context is null, no clear necessary");
			// done, cannot contain content
		} else {
			log.info("Clear ThreadLocal context of " + AboutAppEngine.getThreadInfo());
			Map<String,ThreadLocalGaeModelRevision> tcMap = getThreadContext();
			for(String key : tcMap.keySet()) {
				ThreadLocalGaeModelRevision threadRevInfo = tcMap.get(key);
				if(threadRevInfo != null) {
					threadRevInfo.clear();
				}
			}
			threadContext.set(null);
		}
	}
	
}
