package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.cache.CacheStatistics;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;


/**
 * To fix an issue with the Gae local test implementation (takes more and more
 * memory), this implementation can be restricted in memory use. This
 * implementation keeps 5 MB of memory free.
 * 
 * @author xamde
 * 
 */
public class LocalMemcache implements IMemCache {
	
	private static final Logger log = LoggerFactory.getLogger(LocalMemcache.class);
	
	private Cache javaxCache;
	
	public int size() {
		return this.map.size();
	}
	
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}
	
	public Object get(Object key) {
		return this.map.get(key);
	}
	
	public Object put(Object key, Object value) {
		controlCacheSize();
		return this.map.put(key, value);
	}
	
	private static final long RESERVE_MEMORY = 5 * 1024 * 1024;
	
	private void controlCacheSize() {
		long free = Runtime.getRuntime().freeMemory();
		if(free < RESERVE_MEMORY) {
			log.warn("Free memory = " + free + ", require " + RESERVE_MEMORY + " -> Auto-clear.");
			this.clear();
			System.gc();
		}
	}
	
	public Object remove(Object key) {
		return this.map.remove(key);
	}
	
	public void putAll(Map<? extends Object,? extends Object> m) {
		this.map.putAll(m);
	}
	
	public void clear() {
		this.map.clear();
	}
	
	public Set<Object> keySet() {
		return this.map.keySet();
	}
	
	public Collection<Object> values() {
		return this.map.values();
	}
	
	public Set<java.util.Map.Entry<Object,Object>> entrySet() {
		return this.map.entrySet();
	}
	
	@Override
	public boolean equals(Object o) {
		return this.map.equals(o);
	}
	
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}
	
	private Map<Object,Object> map;
	
	@SuppressWarnings("unchecked")
	public LocalMemcache() {
		log.info("Using LocalMemcache");
		assert this.javaxCache == null;
		try {
			// FIXME make sure to keep cache memory usage below some threshold
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			Cache cache = cacheFactory.createCache(Collections.emptyMap());
			this.javaxCache = cache;
			this.map = cache;
		} catch(CacheException e) {
			log.error("Could not create LocalMemcache instance", e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String stats() {
		CacheStatistics stats = this.javaxCache.getCacheStatistics();
		int hits = stats.getCacheHits();
		int misses = stats.getCacheMisses();
		int objectcount = stats.getObjectCount();
		return "In-memory, size: " + size() + " objectcount: " + objectcount + " hits: " + hits
		        + " misses: " + misses;
	}
	
}
