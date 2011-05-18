package org.xydra.store.impl.gae;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;

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
	
	private static AsyncDatastoreService datastore;
	
	private static boolean useMemCache = true;
	
	/* used only in test mode */
	private static Set<Key> storedKeys;
	
	/**
	 * @param b turn GAE MemCache off or on at runtime
	 */
	public static void setUseMemCache(boolean b) {
		useMemCache = b;
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
		return

		"MemCache: " + useMemCache + "\n"

		+ getStats();
	}
	
	public static String getStats() {
		return "tbd";
		
		// FIXME "MemCache: " + XydraRuntime.getMemcache().stats();
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key) {
		return getEntity(key, null);
	}
	
	private static final Entity NULL_ENTITY = new Entity("NULL-ENTITY", "null");
	
	public static <T> T waitFor(Future<T> t) {
		while(true) {
			try {
				return t.get();
			} catch(InterruptedException e) {
				e.printStackTrace();
			} catch(ExecutionException e) {
				return null;
			}
		}
	}
	
	/**
	 * Like {@link #getEntity(Key)}, but returns a non-null entity from the
	 * cache if available, without checking the datastore.
	 */
	public static Entity getEntityExists(Key key, Transaction trans) {
		
		assert trans != null;
		
		makeSureDatestoreServiceIsInitialised();
		
		Entity cachedEntity = null;
		if(useMemCache) {
			// try first to get from memcache
			cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
			if(cachedEntity != null) {
				log.debug("Getting entity " + key.toString() + " from MemCache");
				if(!cachedEntity.equals(NULL_ENTITY)) {
					return cachedEntity;
				}
				// FIXME max: why not return null in else-case?
			}
		}
		
		log.debug("Getting entity " + key.toString() + " from GAE data store");
		Future<Entity> entity = datastore.get(trans, key);
		Entity e = waitFor(entity);
		if(useMemCache) {
			updateCachedEntity(key, cachedEntity, e);
		}
		if(e == null) {
			log.debug("--> null");
		}
		
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
				if(useMemCache) {
					updateCachedEntity(this.key, null, this.entity);
				}
				this.future = null;
			}
			return this.entity;
		}
		
	}
	
	public static AsyncEntity getEntityAsync(Key key) {
		
		makeSureDatestoreServiceIsInitialised();
		
		if(useMemCache) {
			// try first to get from memcache
			Entity cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
			if(cachedEntity != null) {
				log.debug("Getting entity " + key.toString() + " from MemCache");
				if(cachedEntity.equals(NULL_ENTITY)) {
					log.debug("--> null");
					return new AsyncEntity(null);
				} else {
					return new AsyncEntity(cachedEntity);
				}
			}
		}
		
		log.debug("Getting entity " + key.toString() + " from GAE data store");
		Future<Entity> entity = datastore.get(key);
		
		return new AsyncEntity(key, entity);
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @param trans The transaction to load the entity in.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key, Transaction trans) {
		
		makeSureDatestoreServiceIsInitialised();
		
		Entity cachedEntity = null;
		if(useMemCache) {
			// try first to get from memcache
			cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
			if(cachedEntity != null && trans == null) {
				log.debug("Getting entity " + key.toString() + " from MemCache");
				if(cachedEntity.equals(NULL_ENTITY)) {
					log.debug("--> null");
					return null;
				} else {
					return cachedEntity;
				}
			} else if(trans != null) {
				/*
				 * If there is a transaction, we must read from the actual
				 * datastore so that the transaction will abort if the value is
				 * changed before trans.commit(). TODO in some cases (ie:
				 * revision grabbing) it is OK to return an old entity, but
				 * returning null could be fatal.
				 */
			}
		}
		
		log.debug("Getting entity " + key.toString() + " from GAE data store");
		Future<Entity> entity = datastore.get(trans, key);
		Entity e = waitFor(entity);
		if(useMemCache) {
			updateCachedEntity(key, cachedEntity, e);
		}
		if(e == null) {
			log.debug("--> null");
		}
		return e;
	}
	
	private static void updateCachedEntity(Key key, Entity oldEntity, Entity newEntity) {
		
		Entity ne = newEntity;
		Entity oe = oldEntity;
		
		if(ne == null) {
			ne = NULL_ENTITY;
		}
		
		while(true) {
			
			Entity e = (Entity)XydraRuntime.getMemcache().put(key, ne);
			
			if(XI.equals(oe, e) || XI.equals(e, ne)) {
				return;
			}
			
			// Someone changed the entity, better change it back to their value.
			
			// FIXME this can still cause short periods where the cache is wrong
			
			oe = ne;
			ne = e;
			
		}
		
	}
	
	private static void makeSureDatestoreServiceIsInitialised() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(datastore == null) {
			datastore = DatastoreServiceFactory.getAsyncDatastoreService();
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
		log.debug("putting " + entity.getKey());
		makeSureDatestoreServiceIsInitialised();
		Future<Key> result = datastore.put(trans, entity);
		Key res = waitFor(result);
		assert res != null;
		if(GaeTestfixer.isEnabled()) {
			makeSureStoredKeyInitialised();
			storedKeys.add(res);
		}
		
		if(useMemCache) {
			// remove first from memcache
			if(trans == null) {
				XydraRuntime.getMemcache().put(entity.getKey(), entity);
			} else {
				XydraRuntime.getMemcache().remove(entity.getKey());
			}
		}
	}
	
	private static void makeSureStoredKeyInitialised() {
		if(storedKeys == null) {
			storedKeys = new HashSet<Key>();
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
		log.debug("putting (async) " + entity.getKey());
		makeSureDatestoreServiceIsInitialised();
		Future<Key> result = datastore.put(trans, entity);
		if(useMemCache) {
			// remove first from memcache
			if(trans == null) {
				XydraRuntime.getMemcache().put(entity.getKey(), entity);
			} else {
				XydraRuntime.getMemcache().remove(entity.getKey());
			}
		}
		if(GaeTestfixer.isEnabled()) {
			makeSureStoredKeyInitialised();
			Key key = waitFor(result);
			storedKeys.add(key);
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
		makeSureDatestoreServiceIsInitialised();
		Future<Transaction> trans = datastore.beginTransaction();
		return waitFor(trans);
	}
	
	/**
	 * Commit the given GAE transaction.
	 * 
	 * @param trans The transaction to commit.
	 */
	public static void endTransaction(Transaction trans) throws ConcurrentModificationException {
		log.debug("-- end transaction --");
		makeSureDatestoreServiceIsInitialised();
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
		makeSureDatestoreServiceIsInitialised();
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
		makeSureDatestoreServiceIsInitialised();
		if(useMemCache) {
			// delete first in memcache
			XydraRuntime.getMemcache().put(key, NULL_ENTITY);
		}
		return datastore.delete(key);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 * @param trans The transaction to remove the entity in.
	 */
	public static void deleteEntity(Key key, Transaction trans) {
		log.debug("deleting " + key);
		makeSureDatestoreServiceIsInitialised();
		if(useMemCache) {
			// delete first in memcache
			if(trans == null) {
				XydraRuntime.getMemcache().put(key, NULL_ENTITY);
			} else {
				XydraRuntime.getMemcache().remove(key);
			}
		}
		Future<Void> result = datastore.delete(trans, key);
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
		log.debug("preparing query");
		makeSureDatestoreServiceIsInitialised();
		return datastore.prepare(trans, query);
	}
	
	/**
	 * @return true if there are transactions active, so that tests can check if
	 *         all transactions are terminated properly.
	 */
	public static boolean transactionsActive() {
		return !datastore.getActiveTransactions().isEmpty();
	}
	
	/**
	 * Delete ALL local data. Use with care.
	 */
	public static void clear() {
		makeSureDatestoreServiceIsInitialised();
		XydraRuntime.getMemcache().clear();
		
		if(GaeTestfixer.isEnabled()) {
			makeSureStoredKeyInitialised();
			log.info("Deleting " + storedKeys.size() + " entities (" + storedKeys
			        + ") from local GAE datastore");
			Future<Void> result = datastore.delete(storedKeys);
			waitFor(result);
			assert result.isDone();
			for(Key key : storedKeys) {
				Future<Entity> keyResult = datastore.get(key);
				Entity e = waitFor(keyResult);
				assert e == null;
			}
		} else {
			deleteAllDataOnLiveDatastore();
		}
	}
	
	private static void deleteAllDataOnLiveDatastore() {
		List<String> kinds = getAllKinds();
		for(String kind : kinds) {
			List<Key> keys = new LinkedList<Key>();
			Query q = new Query(kind).setKeysOnly();
			PreparedQuery pq = datastore.prepare(q);
			for(Entity entity : pq.asIterable()) {
				keys.add(entity.getKey());
			}
			try {
				datastore.delete(keys);
			} catch(Exception e) {
				log.warn("Could not delete kind '" + kind + "'", e);
			}
		}
	}
	
	/**
	 * @return all kinds that do not start with '__'
	 */
	public static List<String> getAllKinds() {
		makeSureDatestoreServiceIsInitialised();
		List<String> kinds = new LinkedList<String>();
		Iterable<Entity> statKinds = datastore.prepare(new Query("__Stat_Kind__")).asIterable();
		for(Entity statKind : statKinds) {
			String kind = statKind.getProperty("kind_name").toString();
			if(!kind.startsWith("__")) {
				kinds.add(kind);
			}
		}
		return kinds;
	}
	
}
