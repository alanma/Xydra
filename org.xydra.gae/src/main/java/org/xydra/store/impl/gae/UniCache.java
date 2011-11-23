package org.xydra.store.impl.gae;

import java.io.Serializable;

import org.xydra.store.XydraRuntime;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * A universal cache system for instanceCache, memcache and datastore.
 * 
 * @author xamde
 * 
 * @param <T> type
 */
public class UniCache<T> {
	
	public static class StorageOptions {
		boolean instance;
		boolean memcache;
		boolean datastore;
		
		public static StorageOptions create(boolean instance, boolean memcache, boolean datastore) {
			StorageOptions so = new StorageOptions();
			so.instance = instance;
			so.memcache = memcache;
			so.datastore = datastore;
			return so;
		}
	}
	
	private CacheEntryHandler<T> entryHandler;
	
	public UniCache(CacheEntryHandler<T> entryHandler) {
		this.entryHandler = entryHandler;
	}
	
	/**
	 * @param key ..
	 * @param value ..
	 * @param storeOpts where to put
	 */
	public void put(String key, T value, StorageOptions storeOpts) {
		if(storeOpts.instance) {
			InstanceContext.getInstanceCache().put(key, value);
		}
		if(storeOpts.memcache) {
			Serializable memcacheValue = this.entryHandler.toSerializable(value);
			XydraRuntime.getMemcache().put(key, memcacheValue);
		}
		if(storeOpts.datastore) {
			Key datastoreKey = createCacheKey(key);
			Entity entity = this.entryHandler.toEntity(datastoreKey, value);
			SyncDatastore.putEntity(entity);
		}
		
	}
	
	/**
	 * @param key ..
	 * @param storeOpts where to look
	 * @return null or stored entity
	 */
	@SuppressWarnings("unchecked")
	public T get(String key, StorageOptions storeOpts) {
		if(storeOpts.instance) {
			Object o = InstanceContext.getInstanceCache().get(key);
			if(o != null) {
				return (T)o;
			}
		}
		if(storeOpts.memcache) {
			Object o = XydraRuntime.getMemcache().get(key);
			if(o != null) {
				return this.entryHandler.fromSerializable((Serializable)o);
			}
		}
		if(storeOpts.datastore) {
			Key datastoreKey = createCacheKey(key);
			Entity entity = SyncDatastore.getEntity(datastoreKey);
			if(entity != null) {
				return this.entryHandler.fromEntity(entity);
			}
		}
		
		return null;
	}
	
	private static Key createCacheKey(String s) {
		return KeyFactory.createKey("XCACHE", s);
	}
	
	public static interface CacheEntryHandler<T> {
		Entity toEntity(Key datastoreKey, T entry);
		
		T fromEntity(Entity entity);
		
		Serializable toSerializable(T entry);
		
		T fromSerializable(Serializable s);
		
	}
	
}
