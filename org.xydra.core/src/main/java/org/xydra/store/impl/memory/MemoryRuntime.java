package org.xydra.store.impl.memory;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.xydra.base.XID;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


public class MemoryRuntime implements XydraPlatformRuntime {
	
	@Override
	public Cache getMemCache() {
		// FIXME this code will change when EHCache moves on
		CacheManager singletonManager = CacheManager.getInstance();
		CacheFactory cacheFactory;
		try {
			cacheFactory = singletonManager.getCacheFactory();
			assert cacheFactory != null;
			
			Map<String,String> config = new HashMap<String,String>();
			config.put("name", "test");
			config.put("maxElementsInMemory", "100000");
			config.put("memoryStoreEvictionPolicy", "LFU");
			config.put("overflowToDisk", "false");
			config.put("eternal", "true");
			config.put("diskPersistent", "false");
			config.put("timeToLiveSeconds", "10000");
			config.put("timeToIdleSeconds", "10000");
			config.put("diskExpiryThreadIntervalSeconds", "120");
			
			Cache memcache = cacheFactory.createCache(config);
			singletonManager.registerCache("test", memcache);
			return memcache;
		} catch(CacheException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		return new MemoryPersistence(repositoryId);
	}
	
}
