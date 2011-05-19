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
 * An implementation of {@link IMemCache} using the javax.cache API. As this one
 * will be deprecated by Google, we migrate to the {@link GaeLowLevelMemCache}.
 * 
 * @author xamde
 * 
 */
public class GaeMemCache implements IMemCache {
	
	private static final Logger log = LoggerFactory.getLogger(GaeMemCache.class);
	
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
		return this.map.put(key, value);
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
	public GaeMemCache() {
		assert this.javaxCache == null;
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			Cache cache = cacheFactory.createCache(Collections.emptyMap());
			this.javaxCache = cache;
			this.map = cache;
		} catch(CacheException e) {
			log.error("Could not create MemCache instance", e);
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
	
	// @Override
	// public int size() {
	// return this.javaxCache.size();
	// }
	//
	// @Override
	// public boolean isEmpty() {
	// return this.javaxCache.isEmpty();
	// }
	//
	// @Override
	// public boolean containsKey(Object key) {
	// return this.javaxCache.containsKey(key);
	// }
	//
	// @Override
	// public boolean containsValue(Object value) {
	// return this.javaxCache.containsValue(value);
	// }
	//
	// @Override
	// public Object get(Object key) {
	// return this.javaxCache.get(key);
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public Object put(Object key, Object value) {
	// return this.javaxCache.put(key, value);
	// }
	//
	// @Override
	// public Object remove(Object key) {
	// return this.javaxCache.remove(key);
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public void putAll(Map<? extends Object,? extends Object> m) {
	// this.javaxCache.putAll(m);
	//
	// }
	//
	// @Override
	// public void clear() {
	// this.javaxCache.clear();
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public Set<Object> keySet() {
	// return this.javaxCache.keySet();
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public Collection<Object> values() {
	// return this.javaxCache.values();
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public Set<java.util.Map.Entry<Object,Object>> entrySet() {
	// return this.javaxCache.entrySet();
	// }
}
