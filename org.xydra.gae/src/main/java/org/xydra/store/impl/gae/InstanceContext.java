package org.xydra.store.impl.gae;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


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
