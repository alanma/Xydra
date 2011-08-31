package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;


/**
 * An implementation of {@link IMemCache} using the low-level GAE memcache API.
 * 
 * @author xamde
 */
public class GaeLowLevelMemCache implements IMemCache {
	
	private static final Logger log = LoggerFactory.getLogger(GaeLowLevelMemCache.class);
	
	private MemcacheService memcacheService;
	/* used to prefix all keys */
	@SuppressWarnings("unused")
	private String appVersion;
	
	@GaeOperation()
	public GaeLowLevelMemCache() {
		this.memcacheService = MemcacheServiceFactory.getMemcacheService();
		// format: user-chosen-versionId-from-appengine-xml '.' timestamp
		this.appVersion = AboutAppEngine.getVersion();
	}
	
	@Override
	@GaeOperation(memcacheRead = true)
	public String stats() {
		Stats stats = this.memcacheService.getStatistics();
		return "In-memory, size: " + size()

		+ " maxTimeWithoutAccess: " + stats.getBytesReturnedForHits()

		+ " hits: " + stats.getHitCount()

		+ " misses: " + stats.getMissCount()

		+ " TotalItemBytes: " + stats.getTotalItemBytes()

		+ " BytesReturnedForHits: " + stats.getBytesReturnedForHits();
	}
	
	@Override
	@GaeOperation(memcacheRead = true)
	public int size() {
		return (int)this.memcacheService.getStatistics().getItemCount();
	}
	
	@Override
	@GaeOperation(memcacheRead = true)
	public boolean isEmpty() {
		return this.size() == 0;
	}
	
	@Override
	@GaeOperation(memcacheRead = true)
	public boolean containsKey(Object key) {
		return this.memcacheService.contains(keyUniqueForCurrentAppVersion(key));
	}
	
	@GaeOperation()
	private Object keyUniqueForCurrentAppVersion(Object key) {
		return key;
		/*
		 * TODO(stability) @Daniel: use a memcache key that is specific for a
		 * certain app-version to avoid conflicts
		 */

		// if(key instanceof String) {
		// return this.appVersion + key;
		// } else {
		// // fall back to binary
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// try {
		// bos.write(this.appVersion.getBytes());
		// ObjectOutputStream oos = new ObjectOutputStream(bos);
		// oos.writeObject(key);
		// oos.close();
		// return bos.toByteArray();
		// } catch(IOException e) {
		// throw new
		// RuntimeException("Error converting memcache key to unique key", e);
		// }
		// }
	}
	
	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}
	
	@Override
	@GaeOperation(memcacheRead = true)
	public Object get(Object key) {
		Object o = this.memcacheService.get(keyUniqueForCurrentAppVersion(key));
		log.trace("get key '" + key + "' => "
		        + (o == null ? "null" : o.getClass().getCanonicalName() + "=" + o.toString()));
		return o;
	}
	
	@Override
	@GaeOperation(memcacheRead = true)
	public Map<Object,Object> getAll(Collection<Object> keys) {
		return this.memcacheService.getAll(keys);
	}
	
	/**
	 * This implementation violates the API contract. Instead of returning the
	 * previous value, if present, it <em>always</em> returns null. This is
	 * faster.
	 */
	@Override
	@GaeOperation(memcacheWrite = true)
	public Object put(Object key, Object value) {
		this.memcacheService.put(keyUniqueForCurrentAppVersion(key), value);
		return null;
	}
	
	@Override
	@GaeOperation(memcacheWrite = true)
	public Object remove(Object key) {
		return this.memcacheService.delete(keyUniqueForCurrentAppVersion(key));
	}
	
	@Override
	@GaeOperation(memcacheWrite = true)
	public void putAll(Map<? extends Object,? extends Object> m) {
		// transform keys
		Map<Object,Object> keyTransformedMap = new HashMap<Object,Object>();
		for(java.util.Map.Entry<? extends Object,? extends Object> entry : m.entrySet()) {
			keyTransformedMap.put(keyUniqueForCurrentAppVersion(entry.getKey()), entry.getValue());
		}
		this.memcacheService.putAll(keyTransformedMap);
	}
	
	@Override
	@GaeOperation(memcacheWrite = true)
	public void clear() {
		this.memcacheService.clearAll();
	}
	
	@Override
	public Set<Object> keySet() {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}
	
	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}
	
	@Override
	public Set<java.util.Map.Entry<Object,Object>> entrySet() {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}
	
	@Override
	// Expires in 10 days. There is no default.
	public void putIfValueIsNull(Object key, Object entityToBeCached) {
		this.memcacheService.put(key, entityToBeCached,
		        Expiration.byDeltaSeconds(60 * 60 * 24 * 10), SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
	}
	
}
