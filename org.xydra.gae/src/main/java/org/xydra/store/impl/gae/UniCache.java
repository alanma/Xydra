package org.xydra.store.impl.gae;

import java.io.Serializable;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * A universal cache system for instanceCache, memcache and datastore.
 * 
 * Datastore entries are stored as kind "XCACHE".
 * 
 * @author xamde
 * 
 * @param <T> type
 */
public class UniCache<T> {
	
	public static final Logger log = LoggerFactory.getLogger(UniCache.class);
	
	public static class StorageOptions {
		boolean instance;
		boolean memcache;
		boolean datastore;
		
		/**
		 * @param instance true if value should be stored in local instance
		 *            cache
		 * @param memcache true if value should be stored in shared but volatile
		 *            memcache
		 * @param datastore true if value should be stored in persistent but
		 *            slow datastore
		 * @return options objects
		 */
		public static StorageOptions create(boolean instance, boolean memcache, boolean datastore) {
			StorageOptions so = new StorageOptions();
			so.instance = instance;
			so.memcache = memcache;
			so.datastore = datastore;
			return so;
		}
		
		@Override
		public String toString() {
			return "instance:" + this.instance + "," + "memcache:" + this.memcache + ","
			        + "datastore:" + this.datastore;
		}
	}
	
	private CacheEntryHandler<T> entryHandler;
	
	public UniCache(CacheEntryHandler<T> entryHandler) {
		this.entryHandler = entryHandler;
	}
	
	/**
	 * @param key must be unique
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
				log.debug("Return " + key + " from instance cache");
				return (T)o;
			}
		}
		if(storeOpts.memcache) {
			Object o = XydraRuntime.getMemcache().get(key);
			if(o != null) {
				log.debug("Return " + key + " from memcache");
				return this.entryHandler.fromSerializable((Serializable)o);
			}
		}
		if(storeOpts.datastore) {
			Key datastoreKey = createCacheKey(key);
			Entity entity = SyncDatastore.getEntity(datastoreKey);
			if(entity != null) {
				log.debug("Return " + key + " from datastore XCACHE entity");
				return this.entryHandler.fromEntity(entity);
			}
		}
		
		log.debug(key + " not found in any cache. Opts: " + storeOpts);
		return null;
	}
	
	/**
	 * @param s must be unique
	 * @return a gae Key
	 */
	private static Key createCacheKey(String s) {
		return KeyFactory.createKey("XCACHE", s);
	}
	
	public static interface DatastoreEntryHandler<T> {
		Entity toEntity(Key datastoreKey, T entry);
		
		T fromEntity(Entity entity);
	}
	
	public static interface MemcacheEntryHandler<T> {
		Serializable toSerializable(T entry);
		
		T fromSerializable(Serializable s);
	}
	
	public static interface CacheEntryHandler<T> extends DatastoreEntryHandler<T>,
	        MemcacheEntryHandler<T> {
	}
	
}
