package org.xydra.perf;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.IMemCache;
import org.xydra.store.impl.memory.LocalMemcache.IdentifiableImpl;


/**
 * A wrapper around an IMemCache to gather runtime access statistics.
 * 
 * Records every cache get and put, as well as all values a key ever has.
 * 
 * Very memory expensive!
 * 
 * @author xamde
 * 
 */
@RunsInGWT(false)
public class StatsGatheringMemCacheWrapper implements IMemCache {
	
	private MapStats mapstats = new MapStats();
	private Map<String,Long> stats = new HashMap<String,Long>();
	/** Allows any other process easy runtime-access to the stats */
	public static StatsGatheringMemCacheWrapper INSTANCE;
	
	public StatsGatheringMemCacheWrapper(IMemCache memcache) {
		this.base = memcache;
		INSTANCE = this;
	}
	
	@Override
	public int size() {
		return this.base.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.base.isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key) {
		XyAssert.xyAssert(key instanceof String);
		count("containsKey");
		return this.base.containsKey(key instanceof String ? key : key.toString());
	}
	
	private void count(String action) {
		Long l = this.stats.get(action);
		if(l == null) {
			this.stats.put(action, 1L);
		} else {
			this.stats.put(action, l + 1);
		}
	}
	
	@Override
	public boolean containsValue(Object value) {
		count("containsValue");
		return this.base.containsValue(value);
	}
	
	@Override
	public Object get(Object key) {
		XyAssert.xyAssert(key instanceof String);
		count("get");
		String usedKey;
		if(key instanceof String) {
			usedKey = (String)key;
		} else {
			usedKey = key.toString();
		}
		Object value = this.base.get(usedKey);
		this.mapstats.recordGet(key.toString(), value != null, 1);
		return value;
	}
	
	@Override
	public Map<String,Object> getAll(Collection<String> keys) {
		count("getAll");
		Map<String,Object> result = new HashMap<String,Object>();
		for(String key : keys) {
			Object value = this.base.get(key);
			this.mapstats.recordGet(key.toString(), value != null, keys.size());
			if(value != null) {
				result.put(key, value);
			}
		}
		return result;
	}
	
	@Override
	public Object put(String key, Object value) {
		count("put");
		this.mapstats.recordPut(key.toString(), value);
		return this.base.put(key, value);
	}
	
	@Override
	public void putIfValueIsNull(String key, Object value) {
		count("putIfValueIsNull");
		this.mapstats.recordPut(key.toString(), value);
		this.base.putIfValueIsNull(key, value);
	}
	
	@Override
	public Object remove(Object key) {
		count("remove");
		return this.base.remove(key);
	}
	
	@Override
	public void putAll(Map<? extends String,? extends Object> m) {
		count("putAll");
		this.base.putAll(m);
	}
	
	@Override
	public void clear() {
		count("clear");
		this.base.clear();
		// clear also stats
		this.stats.clear();
		this.mapstats.clear();
	}
	
	@Override
	public Set<String> keySet() {
		count("keySet");
		return this.base.keySet();
	}
	
	@Override
	public Collection<Object> values() {
		count("values");
		return this.base.values();
	}
	
	@Override
	public Set<java.util.Map.Entry<String,Object>> entrySet() {
		count("entrySet");
		return this.base.entrySet();
	}
	
	@Override
	public boolean equals(Object o) {
		return this.base.equals(o);
	}
	
	@Override
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
		buf.append("Access stats ===================================================== <br />\n");
		try {
			StringWriter sw = new StringWriter();
			this.mapstats.writeStats(new MiniStreamWriter(sw));
			buf.append(sw.toString());
		} catch(MiniIOException e) {
			throw new RuntimeException(e);
		}
		
		buf.append("BASE stats<br />\n");
		buf.append(this.base.stats() + "<br />\n");
		
		return buf.toString();
	}
	
	@Override
	public boolean putIfUntouched(String key, IdentifiableValue oldValue, Object newValue) {
		count("putIfUntouched");
		synchronized(this.base) {
			Object current = get(key);
			if(current.equals(oldValue.getValue())) {
				// indirect count
				put(key, newValue);
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public IdentifiableValue getIdentifiable(String key) {
		// no counting needed
		Object o = get(key);
		return new IdentifiableImpl(o);
	}
	
	@Override
	public Map<String,Long> incrementAll(Map<String,Long> offsets, long initialValue) {
		count("incrementAll");
		Map<String,Long> result = new HashMap<String,Long>();
		result = this.base.incrementAll(offsets, initialValue);
		for(java.util.Map.Entry<String,Long> entry : result.entrySet()) {
			this.mapstats.recordPut(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
}
