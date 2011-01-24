package org.xydra.store.impl.gae;

import java.util.Collections;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * @author xamde
 * 
 */
public class GaePlatformRuntime implements XydraPlatformRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(GaePlatformRuntime.class);
	
	@Override
	public Map<Object,Object> getMemCache() {
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			Cache cache = cacheFactory.createCache(Collections.emptyMap());
			@SuppressWarnings("unchecked")
			Map<Object,Object> map = cache;
			return map;
		} catch(CacheException e) {
			log.error("Could not create MemCache instance", e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		return new GaePersistence(repositoryId);
	}
	
	/**
	 * FIXME make sure this method is call from servlet early enough
	 */
	public static void register() {
		XydraRuntime.setPlatformRuntime(new GaePlatformRuntime());
	}
	
}
