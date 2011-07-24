package org.xydra.store.impl.gae;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	
	private static final byte[] NULL_VALUE = "null_value".getBytes();
	
	public int size() {
		return this.internalMap.size();
	}
	
	public boolean isEmpty() {
		return this.internalMap.isEmpty();
	}
	
	public boolean containsKey(Object key) {
		return this.internalMap.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return this.internalMap.containsValue(valueToStored(value));
	}
	
	public Object get(Object key) {
		byte[] bytes = this.internalMap.get(key);
		if(bytes == null) {
			return null;
		}
		Object result = storedToValue(bytes);
		return result;
	}
	
	public Object put(Object key, Object value) {
		controlCacheSize();
		// transform null value & clone value
		byte[] oldValue = this.internalMap.put(key, valueToStored(value));
		if(oldValue == null) {
			return null;
		}
		return storedToValue(oldValue);
	}
	
	private static final Object storedToValue(byte[] stored) {
		assert stored != null;
		if(stored == NULL_VALUE) {
			return null;
		}
		// else
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(stored);
			ObjectInputStream oin = new ObjectInputStream(bis);
			Object result = oin.readObject();
			oin.close();
			return result;
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final byte[] valueToStored(Object value) {
		if(value == null) {
			return NULL_VALUE;
		}
		// else
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(value);
			oos.close();
			byte[] bytes = bos.toByteArray();
			return bytes;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
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
		return this.internalMap.remove(key);
	}
	
	public void putAll(Map<? extends Object,? extends Object> m) {
		// transform values implicitly
		for(Map.Entry<? extends Object,? extends Object> entry : m.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	public void clear() {
		this.internalMap.clear();
	}
	
	public Set<Object> keySet() {
		return this.internalMap.keySet();
	}
	
	public Collection<Object> values() {
		// transform null values
		List<Object> result = new LinkedList<Object>();
		for(byte[] o : this.internalMap.values()) {
			result.add(storedToValue(o));
		}
		return result;
	}
	
	public Set<java.util.Map.Entry<Object,Object>> entrySet() {
		// transform null values
		Map<Object,Object> result = new HashMap<Object,Object>();
		for(Map.Entry<Object,byte[]> e : this.internalMap.entrySet()) {
			result.put(e.getKey(), storedToValue(e.getValue()));
		}
		return result.entrySet();
	}
	
	@Override
	public boolean equals(Object o) {
		return this.internalMap.equals(o);
	}
	
	@Override
	public int hashCode() {
		return this.internalMap.hashCode();
	}
	
	private Map<Object,byte[]> internalMap;
	
	public LocalMemcache() {
		log.info("Using LocalMemcache");
		this.internalMap = new ConcurrentHashMap<Object,byte[]>();
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
