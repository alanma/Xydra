package org.xydra.store.impl.gae;

import java.io.Serializable;
import java.util.ConcurrentModificationException;

import org.xydra.annotations.NeverNull;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.cache.Cache;


/**
 * A universal <em>cache</em> system for instanceCache, memcache and datastore.
 * 
 * 
 * @author xamde
 * 
 * @param <T> type
 */
public class UniCache<T> {
	
	public static final Logger log = LoggerFactory.getLogger(UniCache.class);
	
	public static class StorageOptions implements Serializable {
		private static final long serialVersionUID = 9163196946298146291L;
		int instanceSize;
		boolean memcache;
		boolean datastore;
		boolean computeIfNull;
		
		/**
		 * @param instance true if value should be stored in local instance
		 *            cache
		 * @param memcache true if value should be stored in shared but volatile
		 *            memcache
		 * @param datastore true if value should be stored in persistent but
		 *            slow datastore
		 * @return options objects
		 */
		@Deprecated
		public static StorageOptions create(boolean instance, boolean memcache, boolean datastore) {
			StorageOptions so = new StorageOptions();
			so.instanceSize = instance ? 10 : 0;
			so.memcache = memcache;
			so.datastore = datastore;
			return so;
		}
		
		/**
		 * @param instanceSize 0 = off. n = number of instance entries if value
		 *            should be stored in local instance cache
		 * @param memcache true if value should be stored in shared but volatile
		 *            memcache
		 * @param datastore true if value should be stored in persistent but
		 *            slow datastore
		 * @param computeIfNull when the cache returns null, should a new value
		 *            be computed on the fly? This might not always be possible
		 *            and the UniCache cannot do the calculation itself. It
		 *            merely manages the configuration.
		 * @return options objects
		 */
		public static StorageOptions create(int instanceSize, boolean memcache, boolean datastore,
		        boolean computeIfNull) {
			StorageOptions so = new StorageOptions();
			so.instanceSize = instanceSize;
			so.memcache = memcache;
			so.datastore = datastore;
			so.computeIfNull = computeIfNull;
			return so;
		}
		
		@Override
		public String toString() {
			return "instance:" + this.instanceSize + "," + "memcache:" + this.memcache + ","
			        + "datastore:" + this.datastore + " computeIfNull?" + this.computeIfNull;
		}
		
		public boolean isComputeIfNull() {
			return this.computeIfNull;
		}
	}
	
	private CacheEntryHandler<T> entryHandler;
	
	private String kindName;
	
	/**
	 * Datastore entries are stored as kind "XCACHE", by default.
	 * 
	 * @param entryHandler
	 */
	public UniCache(CacheEntryHandler<T> entryHandler) {
		this(entryHandler, "XCACHE");
	}
	
	/**
	 * @param entryHandler
	 * @param kindName the GAE KIND of datastore keys
	 */
	public UniCache(CacheEntryHandler<T> entryHandler, String kindName) {
		this.entryHandler = entryHandler;
		this.kindName = kindName;
	}
	
	/**
	 * @param key must be unique
	 * @param value ..
	 * @param storeOpts where to put
	 */
	public void put(String key, T value, StorageOptions storeOpts) {
		if(storeOpts.instanceSize > 0) {
			Cache<String,Object> instanceCache = InstanceContext.getInstanceCache();
			synchronized(instanceCache) {
				instanceCache.put(key, value);
			}
		}
		if(storeOpts.memcache) {
			Serializable memcacheValue = this.entryHandler.toSerializable(value);
			XydraRuntime.getMemcache().put(key, memcacheValue);
		}
		if(storeOpts.datastore) {
			Key datastoreKey = createCacheKey(key);
			try {
				Entity entity = this.entryHandler.toEntity(datastoreKey, value);
				SyncDatastore.putEntity(entity);
			} catch(ConcurrentModificationException cme) {
				// assume thats fine
			} catch(DatastoreFailureException e) {
				log.warn("Could not cache " + key, e);
			}
		}
		
	}
	
	/**
	 * @param key must be unique for {@link InstanceContext}, Memcache AND
	 *            datastore. Choose your keys wisely.
	 * @param storeOpts where to look
	 * @return null or stored entity
	 */
	@SuppressWarnings("unchecked")
	public T get(String key, StorageOptions storeOpts) {
		if(storeOpts.instanceSize > 0) {
			Cache<String,Object> instanceCache = InstanceContext.getInstanceCache();
			Object o = null;
			synchronized(instanceCache) {
				o = instanceCache.getIfPresent(key);
			}
			if(o != null) {
				log.debug("Return '" + key + "' from instance cache");
				return (T)o;
			}
		}
		if(storeOpts.memcache) {
			Object o = XydraRuntime.getMemcache().get(key);
			if(o != null) {
				log.debug("Return '" + key + "' from memcache");
				return this.entryHandler.fromSerializable((Serializable)o);
			}
		}
		if(storeOpts.datastore) {
			Key datastoreKey = createCacheKey(key);
			Entity entity = SyncDatastore.getEntity(datastoreKey);
			if(entity != null) {
				log.debug("Return '" + key + "' from datastore entity");
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
	private Key createCacheKey(String s) {
		return KeyFactory.createKey(this.kindName, s);
	}
	
	public static interface DatastoreEntryHandler<T> {
		Entity toEntity(Key datastoreKey, T entry);
		
		T fromEntity(@NeverNull Entity entity);
	}
	
	public static interface MemcacheEntryHandler<T> {
		Serializable toSerializable(T entry);
		
		T fromSerializable(@NeverNull Serializable s);
	}
	
	public static interface CacheEntryHandler<T> extends DatastoreEntryHandler<T>,
	        MemcacheEntryHandler<T> {
	}
	
}
