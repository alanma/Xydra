package org.xydra.server.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInJava;
import org.xydra.server.impl.IMemCache;


/**
 * Backed by a simple java Map. Good for in-memory testing.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine
@RunsInJava
public class MemCacheImpl implements IMemCache {
	
	private Map<String,byte[]> map = new HashMap<String,byte[]>();
	
	@Override
	public boolean equals(Object o) {
		return this.map.equals(o);
	}
	
	@Override
	public byte[] get(String key) {
		return this.map.get(key);
	}
	
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}
	
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	public Set<String> keySet() {
		return this.map.keySet();
	}
	
	public void put(String key, byte[] value) {
		this.map.put(key, value);
	}
	
	@Override
	public void putAll(Map<String,byte[]> map) {
		map.putAll(map);
	}
	
	@Override
	public void remove(String key) {
		this.map.remove(key);
	}
	
	public long size() {
		return this.map.size();
	}
	
	public Collection<byte[]> values() {
		return this.map.values();
	}
	
	@Override
	public boolean containsKey(String key) {
		return this.map.containsKey(key);
	}
	
}
