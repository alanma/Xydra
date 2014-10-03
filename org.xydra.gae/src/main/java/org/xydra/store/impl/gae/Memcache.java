package org.xydra.store.impl.gae;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.xgae.XGae;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.memcache.api.IMemCache;

public class Memcache {

	public static final String NULL_ENTITY_KIND = "NULL-ENTITY";

	/**
	 * A null-entity is required to cache e.g. the fact, that the datastore
	 * <em>does not</em> contain a certain key
	 */
	public static final SEntity NULL_ENTITY = XGae.get().datastore()
			.createEntity(XGae.get().datastore().createKey(NULL_ENTITY_KIND, "null"));

	private static boolean useMemache_ = true;

	public static Object get(SKey key) {
		if (!useMemache_)
			return null;

		return XGae.get().memcache().get(KeyStructure.toString(key));
	}

	/**
	 * @param keys
	 *            never null
	 * @return a map with the mappings. See {@link IMemCache#getAll(Collection)}
	 *         . Values might contain {@link #NULL_ENTITY}. Never returns null.
	 */
	@XGaeOperation(memcacheRead = true)
	public static Map<String, Object> getEntities(Collection<String> keys) {
		assert keys != null;
		Map<String, Object> memcachedEntities = null;
		if (useMemache_) {
			memcachedEntities = XGae.get().memcache().getAll(keys);
		}
		if (memcachedEntities == null) {
			return Collections.emptyMap();
		}
		return memcachedEntities;
	}

	public static void put(SKey key, Object value) {
		if (!useMemache_)
			return;
		XGae.get().memcache().put(KeyStructure.toString(key), value);
	}

	public static void clear() {
		if (!useMemache_)
			return;
		XGae.get().memcache().clear();
	}

	public static void setUseMemCache(boolean useMemcache) {
		useMemache_ = useMemcache;
	}

	public static void putChecked(SKey key, Object value) throws IOException {
		if (!useMemache_)
			return;
		XGae.get().memcache().putChecked(KeyStructure.toString(key), value);
	}

}
