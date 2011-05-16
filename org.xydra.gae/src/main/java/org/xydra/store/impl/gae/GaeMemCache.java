package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheEntry;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheListener;
import javax.cache.CacheManager;
import javax.cache.CacheStatistics;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;


public class GaeMemCache implements IMemCache {
	
	private static final Logger log = LoggerFactory.getLogger(GaeMemCache.class);
	
	public GaeMemCache() {
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			Cache cache = cacheFactory.createCache(Collections.emptyMap());
			this.javaxCache = cache;
		} catch(CacheException e) {
			log.error("Could not create MemCache instance", e);
			throw new RuntimeException(e);
		}
	}
	
	public void addListener(CacheListener arg0) {
		this.javaxCache.addListener(arg0);
	}
	
	public boolean containsKey(Object key) {
		return this.javaxCache.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return this.javaxCache.containsValue(value);
	}
	
	public void clear() {
		this.javaxCache.clear();
	}
	
	@SuppressWarnings("unchecked")
	public Set<Map.Entry<Object,Object>> entrySet() {
		return this.javaxCache.entrySet();
	}
	
	public boolean equals(Object o) {
		return this.javaxCache.equals(o);
	}
	
	public void evict() {
		this.javaxCache.evict();
	}
	
	public Object get(Object key) {
		return this.javaxCache.get(key);
	}
	
	public Map<?,?> getAll(Collection<?> arg0) {
		return this.javaxCache.getAll(arg0);
	}
	
	public CacheEntry getCacheEntry(Object arg0) {
		return this.javaxCache.getCacheEntry(arg0);
	}
	
	public CacheStatistics getCacheStatistics() {
		return this.javaxCache.getCacheStatistics();
	}
	
	public boolean isEmpty() {
		return this.javaxCache.isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public Set<Object> keySet() {
		return this.javaxCache.keySet();
	}
	
	public int hashCode() {
		return this.javaxCache.hashCode();
	}
	
	public void load(Object arg0) {
		this.javaxCache.load(arg0);
	}
	
	public void loadAll(Collection<?> arg0) {
		this.javaxCache.loadAll(arg0);
	}
	
	public Object peek(Object arg0) {
		return this.javaxCache.peek(arg0);
	}
	
	@SuppressWarnings("unchecked")
	public Object put(Object key, Object value) {
		return this.javaxCache.put(key, value);
	}
	
	public Object remove(Object key) {
		return this.javaxCache.remove(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map<? extends Object,? extends Object> m) {
		this.javaxCache.putAll(m);
	}
	
	public void removeListener(CacheListener arg0) {
		this.javaxCache.removeListener(arg0);
	}
	
	public int size() {
		return this.javaxCache.size();
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Object> values() {
		return this.javaxCache.values();
	}
	
	private Cache javaxCache;
	
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
