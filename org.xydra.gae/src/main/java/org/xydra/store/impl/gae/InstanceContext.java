package org.xydra.store.impl.gae;

import java.util.concurrent.TimeUnit;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * A cache shared by the whole JVM. On GAE thats ca. 128 MB on a small instance
 * minus application code (ca. 64 MB). So storing more than 64 MB here will
 * result in a forced reboot.
 * 
 * TODO Needs better usage of space. Some items are almost must-cache, others
 * are nice-to-cache. Add different importance levels & expire dates within each
 * layer.
 * 
 * @author xamde
 */
public class InstanceContext {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(InstanceContext.class);
	
	private static Cache<String,Object> sharedCache;
	
	/**
	 * @return the static cache. Use with care (about race conditions).
	 */
	public static synchronized Cache<String,Object> getInstanceCache() {
		if(sharedCache == null) {
			// use Guava limited cache here to avoid memory leak
			sharedCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
			        .maximumSize(100).build();
			// new ConcurrentHashMap<String,Object>();
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
			sharedCache.invalidateAll();
			sharedCache.cleanUp();
		}
	}
	
}
