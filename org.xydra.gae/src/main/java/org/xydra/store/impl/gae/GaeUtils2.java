package org.xydra.store.impl.gae;

import java.util.concurrent.Future;

import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.util.AsyncEntity;

public class GaeUtils2 {

	/* Compile time flag */
	private static final boolean useMemCache = true;

	public static final String KEY = "__key__";

	public static final String LAST_UNICODE_CHAR = "\uFFFF";

	public static AsyncEntity getEntityFromMemcacheAndAsyncDatatore(SKey key) {
		if (useMemCache) {
			// try first to get from memcache
			SEntity cachedEntity = (SEntity) Memcache.get(key);
			if (cachedEntity != null) {
				if (cachedEntity.equals(Memcache.NULL_ENTITY)) {
					return new AsyncEntity(null);
				} else {
					return new AsyncEntity(cachedEntity);
				}
			}
		}
		Future<SEntity> entity = XGae.get().datastore().async().getEntity(key);
		return new AsyncEntity(key, entity);
	}

}
