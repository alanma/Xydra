package org.xydra.store.impl.gae;

import java.util.concurrent.Future;

import org.xydra.store.impl.gae.AsyncDatastore.AsyncEntity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


public class GaeUtils2 {
	
	/* Compile time flag */
	private static final boolean useMemCache = true;
	
	public static AsyncEntity getEntityFromMemcacheAndAsyncDatatore(Key key) {
		if(useMemCache) {
			// try first to get from memcache
			Entity cachedEntity = Memcache.get(key);
			if(cachedEntity != null) {
				if(cachedEntity.equals(Memcache.NULL_ENTITY)) {
					return new AsyncEntity(null);
				} else {
					return new AsyncEntity(cachedEntity);
				}
			}
		}
		Future<Entity> entity = AsyncDatastore.getEntity(key);
		return new AsyncEntity(key, entity);
	}
	
}
