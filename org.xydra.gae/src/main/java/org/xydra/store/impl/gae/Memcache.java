package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.xydra.store.IMemCache;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


public class Memcache {
	
	public static final String NULL_ENTITY_KIND = "NULL-ENTITY";
	
	/**
	 * A null-entity is required to cache e.g. the fact, that the datastore
	 * <em>does not</em> contain a certain key
	 */
	public static final Entity NULL_ENTITY = new Entity(NULL_ENTITY_KIND, "null");
	
	private static boolean useMemache_ = true;
	
	public static Entity get(Key key) {
		if(!useMemache_)
			return null;
		
		return (Entity)XydraRuntime.getMemcache().get(KeyStructure.toString(key));
	}
	
	/**
	 * @param keys never null
	 * @return a map with the mappings. See {@link IMemCache#getAll(Collection)}
	 *         . Values might contain {@link #NULL_ENTITY}. Never returns null.
	 */
	@GaeOperation(memcacheRead = true)
	public static Map<String,Object> getEntities(Collection<String> keys) {
		assert keys != null;
		Map<String,Object> memcachedEntities = null;
		if(useMemache_) {
			memcachedEntities = XydraRuntime.getMemcache().getAll(keys);
		}
		if(memcachedEntities == null) {
			return Collections.emptyMap();
		}
		return memcachedEntities;
	}
	
	public static void put(Key key, Object value) {
		if(!useMemache_)
			return;
		XydraRuntime.getMemcache().put(KeyStructure.toString(key), value);
	}
	
	public static void clear() {
		if(!useMemache_)
			return;
		XydraRuntime.getMemcache().clear();
	}
	
	public static void setUseMemCache(boolean useMemcache) {
		useMemache_ = useMemcache;
	}
	
}
