package org.xydra.store.impl.gae;

import java.util.Collections;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.CacheStatistics;

import org.xydra.server.impl.IGaeMemCache;


/**
 * Currently not used anywhere.
 * 
 * @author xamde
 */
public class GaeMemCacheUnused implements IGaeMemCache {
	
	private Cache cache;
	
	public boolean containsKey(Object key) {
		return this.cache.containsKey(key);
	}
	
	@Override
	public boolean equals(Object o) {
		return this.cache.equals(o);
	}
	
	public byte[] get(String key) {
		return (byte[])this.cache.get(key);
	}
	
	@Override
	public int hashCode() {
		return this.cache.hashCode();
	}
	
	public boolean isEmpty() {
		return this.cache.isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public void put(String key, byte[] value) {
		this.cache.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public void putAll(Map<String,byte[]> map) {
		this.cache.putAll(map);
	}
	
	public void remove(String key) {
		this.cache.remove(key);
	}
	
	public long size() {
		return this.cache.size();
	}
	
	public GaeMemCacheUnused() {
		try {
			this.cache = CacheManager.getInstance().getCacheFactory()
			        .createCache(Collections.emptyMap());
		} catch(CacheException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public boolean containsKey(String key) {
		return this.cache.containsKey(key);
	}
	
	@Override
	public String stats() {
		CacheStatistics stats = this.cache.getCacheStatistics();
		int hits = stats.getCacheHits();
		int misses = stats.getCacheMisses();
		int objectcount = stats.getObjectCount();
		return "In-memory, size: " + size() + " objectcount: " + objectcount + " hits: " + hits
		        + " misses: " + misses;
	}
	
}
