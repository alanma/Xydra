package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.IMemCache.IdentifiableValue;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;


/**
 * Mapping from XModel {@link XAddress} to GAE {@link Entity} and {@link Key}
 * 
 * @author voelkel
 * 
 */
public class GaeUtils {
	
	private static final Logger log = LoggerFactory.getLogger(GaeUtils.class);
	
	private static AsyncDatastoreService asyncDatastore;
	
	private static boolean useMemCacheInThisClass = true;
	
	public static final String DATASTORE_NAME = "[#DS]";
	
	/*
	 * used only in test mode to be able to delete all entities from datastore
	 * that have been created (in this instance)
	 */
	private static Set<Key> datastoreStoredKeys;
	
	private static DatastoreService syncDatastore;
	
	/**
	 * @param b turn GAE MemCache off or on at runtime
	 */
	public static void setUseMemCache(boolean b) {
		useMemCacheInThisClass = b;
	}
	
	/**
	 * To be exposed via Restless.
	 * 
	 * @param memcache must be 'true' or 'false'
	 * @return the current configuration as a string
	 */
	public static String setCacheConf(String memcache) {
		boolean memcache_ = Boolean.parseBoolean(memcache);
		setUseMemCache(memcache_);
		return getConf();
	}
	
	public static String getConf() {
		return "MemCache = " + useMemCacheInThisClass;
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key) {
		return getEntity_MemcacheFirst_DatastoreFinal(key, true);
	}
	
	/**
	 * Ask directly the datastore. Never ask the memcache.
	 * 
	 * @param key The key of the entity to load.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntityFromDatastore(Key key) {
		return getEntity_MemcacheFirst_DatastoreFinal(key, false);
	}
	
	public static final String NULL_ENTITY_KIND = "NULL-ENTITY";
	
	/**
	 * A null-entity is required to cache the fact, that the datastore
	 * <em>does not</em> contain a certain key
	 */
	public static final Entity NULL_ENTITY = new Entity(NULL_ENTITY_KIND, "null");
	
	/**
	 * @param <T> future type
	 * @param t a future
	 * @return value or null
	 */
	public static <T> T waitFor(Future<T> t) {
		while(true) {
			try {
				return t.get();
			} catch(InterruptedException e) {
				log.warn("interrrupted while waiting for datastore get", e);
			} catch(ExecutionException e) {
				return null;
			}
		}
	}
	
	/**
	 * Similar to {@link #getEntity(Key)}
	 * 
	 * @param key .
	 * @param trans never null
	 * @return a non-null entity from the cache if available, without checking
	 *         the datastore.
	 */
	@GaeOperation(memcacheRead = true ,memcacheWrite = true ,datastoreRead = true)
	public static Entity getEntity_MemcachePositive_DatastoreFinal(Key key, Transaction trans) {
		assert trans != null;
		makeSureAsyncDatestoreServiceIsInitialised();
		Entity e;
		if(useMemCacheInThisClass) {
			String keyStr = KeyStructure.toString(key);
			IdentifiableValue cachedIdentifiable = null;
			// try first to get from memcache
			cachedIdentifiable = XydraRuntime.getMemcache().getIdentifiable(keyStr);
			Entity entity = (Entity)cachedIdentifiable.getValue();
			if(entity != null) {
				if(!entity.equals(NULL_ENTITY)) {
					return entity;
				}
			}
			/*
			 * IMPROVE use synchronous code for the synchronous parts -- /!\
			 * gae-transactions might not work across the two datastore
			 * interfaces
			 */
			Future<Entity> futureEntity = asyncDatastore.get(trans, key);
			e = waitFor(futureEntity);
			XydraRuntime.getMemcache().putIfUntouched(keyStr, cachedIdentifiable, e);
		} else {
			/*
			 * IMPROVE use synchronous code for the synchronous parts -- /!\
			 * gae-transactions might not work across the two datastore
			 * interfaces
			 */
			Future<Entity> entity = asyncDatastore.get(trans, key);
			e = waitFor(entity);
		}
		
		log.debug(DebugFormatter.dataGet(DATASTORE_NAME, KeyStructure.toString(key), e));
		return e;
	}
	
	public static class AsyncEntity {
		
		private Future<Entity> future;
		private Entity entity;
		private Key key;
		
		private AsyncEntity(Key key, Future<Entity> future) {
			this.future = future;
			this.entity = null;
			this.key = key;
		}
		
		private AsyncEntity(Entity entity) {
			this.future = null;
			this.entity = entity;
			this.key = null;
		}
		
		public Entity get() {
			if(this.future != null) {
				this.entity = waitFor(this.future);
				if(useMemCacheInThisClass) {
					updateCachedEntity(this.key, this.entity);
				}
				this.future = null;
				log.debug("... async... Datastore(" + this.key + ") = " + this.entity);
			}
			return this.entity;
		}
		
	}
	
	public static AsyncEntity getEntityAsync_MemcacheFirst_DatastoreFinal(Key key) {
		
		makeSureAsyncDatestoreServiceIsInitialised();
		
		if(useMemCacheInThisClass) {
			// try first to get from memcache
			Entity cachedEntity = (Entity)XydraRuntime.getMemcache()
			        .get(KeyStructure.toString(key));
			if(cachedEntity != null) {
				if(cachedEntity.equals(NULL_ENTITY)) {
					return new AsyncEntity(null);
				} else {
					return new AsyncEntity(cachedEntity);
				}
			}
		}
		
		log.debug("Datastore(" + key.toString() + ") = ...async");
		Future<Entity> entity = asyncDatastore.get(key);
		
		return new AsyncEntity(key, entity);
	}
	
	/**
	 * @param keys never null
	 * @return a map with the mappings. See {@link IMemCache#getAll(Collection)}
	 *         . Values might contain {@link #NULL_ENTITY}.
	 */
	public static Map<String,Object> getEntitiesFromMemcache(Collection<String> keys) {
		assert keys != null;
		Map<String,Object> memcachedEntities = null;
		if(useMemCacheInThisClass) {
			memcachedEntities = XydraRuntime.getMemcache().getAll(keys);
		}
		if(memcachedEntities == null) {
			return Collections.emptyMap();
		}
		return memcachedEntities;
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @param useMemcache if true, memcache is used in this request (only if
	 *            also enabled in this class)
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity_MemcacheFirst_DatastoreFinal(Key key, boolean useMemcache) {
		makeSureAsyncDatestoreServiceIsInitialised();
		
		if(useMemCacheInThisClass && useMemcache) {
			// try first to get from memcache
			Entity memcachedEntity = (Entity)XydraRuntime.getMemcache().get(
			        KeyStructure.toString(key));
			if(memcachedEntity != null) {
				if(memcachedEntity.equals(NULL_ENTITY)) {
					return null;
				} else {
					return memcachedEntity;
				}
			}
		}
		
		Future<Entity> futureEntity = asyncDatastore.get(null, key);
		Entity entityFromDatastore = waitFor(futureEntity);
		log.debug(DebugFormatter.dataGet(DATASTORE_NAME, key.toString(), entityFromDatastore));
		
		if(useMemCacheInThisClass && useMemcache) {
			updateCachedEntity(key, entityFromDatastore);
		}
		return entityFromDatastore;
	}
	
	/**
	 * If there is a transaction, we must read from the actual datastore so that
	 * the transaction will abort if the value is changed before trans.commit().
	 * 
	 * @param key never null
	 * @param trans never null
	 * @return the entity from datastore
	 */
	public static Entity getEntityFromDatastore(Key key, Transaction trans) {
		assert key != null;
		assert trans != null;
		makeSureAsyncDatestoreServiceIsInitialised();
		
		Future<Entity> futureEntity = asyncDatastore.get(trans, key);
		Entity entityFromDatastore = waitFor(futureEntity);
		if(useMemCacheInThisClass) {
			updateCachedEntity(key, entityFromDatastore);
		}
		log.debug(DebugFormatter.dataGet(DATASTORE_NAME, key.toString(), entityFromDatastore));
		return entityFromDatastore;
	}
	
	/**
	 * A careful put to the memcache that makes sure no other process has
	 * modified the same key.
	 * 
	 * @param key ..
	 * @param entityToBeCached to be put in memcache, can be null
	 */
	@GaeOperation(memcacheWrite = true)
	private static void updateCachedEntity(Key key, Entity entityToBeCached) {
		Entity entityToBeCached_ = entityToBeCached;
		if(entityToBeCached_ == null) {
			entityToBeCached_ = NULL_ENTITY;
		}
		XydraRuntime.getMemcache().putIfValueIsNull(KeyStructure.toString(key), entityToBeCached_);
	}
	
	private static void makeSureAsyncDatestoreServiceIsInitialised() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(asyncDatastore == null) {
			log.debug(DebugFormatter.init(DATASTORE_NAME));
			asyncDatastore = DatastoreServiceFactory.getAsyncDatastoreService();
		}
	}
	
	private static void makeSureSyncDatestoreServiceIsInitialised() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(syncDatastore == null) {
			log.debug(DebugFormatter.init(DATASTORE_NAME));
			syncDatastore = DatastoreServiceFactory.getDatastoreService();
		}
	}
	
	/**
	 * Stores a GAE {@link Entity} in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 */
	public static void putEntity(Entity entity) {
		putEntity(entity, null);
	}
	
	/**
	 * Stores a GAE {@link Entity} in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 * @param trans The transaction to write the entity in.
	 */
	public static void putEntity(Entity entity, Transaction trans) {
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, KeyStructure.toString(entity.getKey()),
		        entity));
		makeSureAsyncDatestoreServiceIsInitialised();
		Future<Key> result = asyncDatastore.put(trans, entity);
		Key res = waitFor(result);
		assert res != null;
		if(GaeTestfixer.isEnabled()) {
			makeSureStoredKeyInitialised();
			datastoreStoredKeys.add(res);
		}
		
		if(useMemCacheInThisClass) {
			// remove first from memcache
			if(trans == null) {
				XydraRuntime.getMemcache().put(KeyStructure.toString(entity.getKey()), entity);
			} else {
				XydraRuntime.getMemcache().remove(KeyStructure.toString(entity.getKey()));
			}
		}
	}
	
	private static void makeSureStoredKeyInitialised() {
		if(datastoreStoredKeys == null) {
			datastoreStoredKeys = new HashSet<Key>();
		}
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 */
	public static Future<Key> putEntityAsync(Entity entity) {
		return putEntityAsync(entity, null);
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 */
	public static Future<Key> putEntityAsync(Entity entity, Transaction trans) {
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME + "...async...",
		        KeyStructure.toString(entity.getKey()), entity));
		makeSureAsyncDatestoreServiceIsInitialised();
		Future<Key> result = asyncDatastore.put(trans, entity);
		if(useMemCacheInThisClass) {
			// remove first from memcache
			String keyStr = KeyStructure.toString(entity.getKey());
			if(trans == null) {
				XydraRuntime.getMemcache().put(keyStr, entity);
			} else {
				XydraRuntime.getMemcache().remove(keyStr);
			}
		}
		if(GaeTestfixer.isEnabled()) {
			makeSureStoredKeyInitialised();
			Key key = waitFor(result);
			datastoreStoredKeys.add(key);
		}
		return result;
	}
	
	/**
	 * Begin a GAE transaction.
	 * 
	 * @return The started transaction.
	 */
	public static Transaction beginTransaction() {
		log.debug("-- begin transaction --");
		makeSureAsyncDatestoreServiceIsInitialised();
		Future<Transaction> trans = asyncDatastore.beginTransaction();
		return waitFor(trans);
	}
	
	/**
	 * Commit the given GAE transaction.
	 * 
	 * @param trans The transaction to commit.
	 */
	public static void endTransaction(Transaction trans) throws ConcurrentModificationException {
		log.debug("-- end transaction --");
		makeSureAsyncDatestoreServiceIsInitialised();
		trans.commit();
	}
	
	/**
	 * Commit the given GAE transaction asynchonously.
	 * 
	 * @param trans The transaction to commit.
	 */
	public static Future<Void> endTransactionAsync(Transaction trans)
	        throws ConcurrentModificationException {
		log.debug("-- end transaction (async) --");
		makeSureAsyncDatestoreServiceIsInitialised();
		return trans.commitAsync();
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 */
	public static void deleteEntity(Key key) {
		deleteEntity(key, null);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} asynchronously from
	 * GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 */
	public static Future<Void> deleteEntityAsync(Key key) {
		log.debug("deleting (async) " + key);
		makeSureAsyncDatestoreServiceIsInitialised();
		if(useMemCacheInThisClass) {
			// delete first in memcache
			XydraRuntime.getMemcache().put(KeyStructure.toString(key), NULL_ENTITY);
		}
		return asyncDatastore.delete(key);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 * @param trans The transaction to remove the entity in.
	 */
	public static void deleteEntity(Key key, Transaction trans) {
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, key.toString(), null));
		makeSureAsyncDatestoreServiceIsInitialised();
		if(useMemCacheInThisClass) {
			// delete first in memcache
			if(trans == null) {
				XydraRuntime.getMemcache().put(KeyStructure.toString(key), NULL_ENTITY);
			} else {
				XydraRuntime.getMemcache().remove(KeyStructure.toString(key));
			}
		}
		Future<Void> result = asyncDatastore.delete(trans, key);
		waitFor(result);
	}
	
	/**
	 * Prepares the given GAE query.
	 * 
	 * @param query The query to prepare.
	 * @return a GAE prepared query
	 * 
	 * @see DatastoreService#prepare(Query)
	 */
	public static PreparedQuery prepareQuery(Query query) {
		return prepareQuery(query, null);
	}
	
	/**
	 * Prepares the given GAE query.
	 * 
	 * @param query The query to prepare.
	 * @return a GAE prepared query
	 * 
	 * @see DatastoreService#prepare(Transaction, Query)
	 */
	public static PreparedQuery prepareQuery(Query query, Transaction trans) {
		makeSureAsyncDatestoreServiceIsInitialised();
		return asyncDatastore.prepare(trans, query);
	}
	
	/**
	 * @return true if there are transactions active, so that tests can check if
	 *         all transactions are terminated properly.
	 */
	public static boolean transactionsActive() {
		return !asyncDatastore.getActiveTransactions().isEmpty();
	}
	
	/**
	 * Delete ALL local data. Use with care.
	 */
	public static void clear() {
		log.info("Datastore & Memcache CLEAR");
		makeSureAsyncDatestoreServiceIsInitialised();
		XydraRuntime.getMemcache().clear();
		
		if(GaeTestfixer.isEnabled()) {
			makeSureStoredKeyInitialised();
			log.info("Deleting " + datastoreStoredKeys.size() + " entities (" + datastoreStoredKeys
			        + ") from local GAE datastore");
			Future<Void> result = asyncDatastore.delete(datastoreStoredKeys);
			waitFor(result);
			assert result.isDone();
			for(Key key : datastoreStoredKeys) {
				Future<Entity> keyResult = asyncDatastore.get(key);
				Entity e = waitFor(keyResult);
				assert e == null;
			}
			datastoreStoredKeys.clear();
		} else {
			deleteAllDataOnLiveDatastore();
		}
	}
	
	private static void deleteAllDataOnLiveDatastore() {
		List<String> kinds = getAllKinds();
		for(String kind : kinds) {
			List<Key> keys = new LinkedList<Key>();
			Query q = new Query(kind).setKeysOnly();
			PreparedQuery pq = asyncDatastore.prepare(q);
			for(Entity entity : pq.asIterable()) {
				keys.add(entity.getKey());
			}
			try {
				asyncDatastore.delete(keys);
			} catch(Exception e) {
				log.warn("Could not delete kind '" + kind + "'", e);
			}
		}
	}
	
	/**
	 * @return all kinds that do not start with '__'
	 */
	public static List<String> getAllKinds() {
		makeSureAsyncDatestoreServiceIsInitialised();
		List<String> kinds = new LinkedList<String>();
		Iterable<Entity> statKinds = asyncDatastore.prepare(new Query("__Stat_Kind__"))
		        .asIterable();
		for(Entity statKind : statKinds) {
			String kind = statKind.getProperty("kind_name").toString();
			if(!kind.startsWith("__")) {
				kinds.add(kind);
			}
		}
		return kinds;
	}
	
	/**
	 * Batch get.
	 * 
	 * Retrieves the set of Entities matching keys. The result Map will only
	 * contain Keys for which Entities could be found.
	 * 
	 * Note: If there is a current transaction, this operation will execute
	 * within that transaction. In this case it is up to the caller to commit or
	 * rollback. If there is no current transaction, the behavior of this method
	 * with respect to transactions will be determined by the
	 * ImplicitTransactionManagementPolicy available on the
	 * DatastoreServiceConfig.
	 * 
	 * @param keys never null
	 * @return a mapping for all keys that could be found
	 */
	public static Map<Key,Entity> getEntitiesFromDatastore(Collection<Key> keys) {
		makeSureSyncDatestoreServiceIsInitialised();
		Map<Key,Entity> result = syncDatastore.get(keys);
		log.debug(DebugFormatter.dataGet(DATASTORE_NAME, keys, result));
		return result;
	}
	
}
