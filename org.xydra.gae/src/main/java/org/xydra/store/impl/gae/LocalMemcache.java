package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
class LocalMemcache implements IMemCache {
	
	private static final Logger log = LoggerFactory.getLogger(LocalMemcache.class);
	
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
	
	public LocalMemcache() {
		log.info("Using LocalMemcache");
		this.map = new ConcurrentHashMap<Object,Object>();
	}
	
	@Override
	public String stats() {
		return "In-memory, items: " + size();
	}
	
	@Override
	public Map<Object,Object> getAll(Collection<Object> keys) {
		Map<Object,Object> result = new HashMap<Object,Object>();
		for(Object key : keys) {
			Object value = this.get(key);
			if(value != null) {
				result.put(key, value);
			}
		}
		return result;
	}
	
}
