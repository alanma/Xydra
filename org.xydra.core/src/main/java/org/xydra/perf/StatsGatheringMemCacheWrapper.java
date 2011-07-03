package org.xydra.perf;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xydra.store.IMemCache;


/**
 * A wrapper around an IMemCache to gather runtime access statistics
 * 
 * @author xamde
 * 
 */
public class StatsGatheringMemCacheWrapper implements IMemCache {
	
	private MapStats mapstats = new MapStats();
	private Map<String,Long> stats = new HashMap<String,Long>();
	
	public StatsGatheringMemCacheWrapper(IMemCache memcache) {
		this.base = memcache;
	}
	
	public int size() {
		return this.base.size();
	}
	
	public boolean isEmpty() {
		return this.base.isEmpty();
	}
	
	public boolean containsKey(Object key) {
		count("containsKey");
		return this.base.containsKey(key);
	}
	
	private void count(String action) {
		Long l = this.stats.get(action);
		if(l == null) {
			this.stats.put(action, 1L);
		} else {
			this.stats.put(action, l + 1);
		}
	}
	
	public boolean containsValue(Object value) {
		count("containsValue");
		return this.base.containsValue(value);
	}
	
	public Object get(Object key) {
		Object value = this.base.get(key);
		this.mapstats.recordGet(key.toString(), value != null);
		return value;
	}
	
	public Object put(Object key, Object value) {
		count("put");
		this.mapstats.recordPut(key.toString(), value);
		return this.base.put(key, value);
	}
	
	public Object remove(Object key) {
		count("remove");
		return this.base.remove(key);
	}
	
	public void putAll(Map<? extends Object,? extends Object> m) {
		count("putAll");
		this.base.putAll(m);
	}
	
	public void clear() {
		count("clear");
		this.base.clear();
		// clear also stats
		this.stats.clear();
		this.mapstats.clear();
	}
	
	public Set<Object> keySet() {
		count("keySet");
		return this.base.keySet();
	}
	
	public Collection<Object> values() {
		count("values");
		return this.base.values();
	}
	
	public Set<java.util.Map.Entry<Object,Object>> entrySet() {
		count("entrySet");
		return this.base.entrySet();
	}
	
	public boolean equals(Object o) {
		return this.base.equals(o);
	}
	
	public int hashCode() {
		return this.base.hashCode();
	}
	
	private IMemCache base;
	
	@Override
	public String stats() {
		StringBuffer buf = new StringBuffer();
		for(String s : this.stats.keySet()) {
			buf.append(s + " = " + this.stats.get(s) + "<br />\n");
		}
		buf.append("Access stats<br />\n");
		try {
			StringWriter sw = new StringWriter();
			this.mapstats.writeStats(sw);
			buf.append(sw.getBuffer().toString());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		buf.append("BASE stats<br />\n");
		buf.append(this.base.stats() + "<br />\n");
		
		return buf.toString();
	}
	
}
