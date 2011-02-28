package org.xydra.store.impl.gae;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
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
		return

		"MemCache: " + XydraRuntime.getMemcache().size() + " entries \n";
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key) {
		return getEntity(key, null);
	}
	
	private static final Entity NULL_ENTITY = new Entity("NULL-ENTITY");
	
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
		
		if(useMemCache) {
			// try first to get from memcache
			Entity cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
			if(cachedEntity != null) {
				log.debug("Getting entity " + key.toString() + " from MemCache");
				if(!cachedEntity.equals(NULL_ENTITY)) {
					return cachedEntity;
				}
			}
		}
		
		log.debug("Getting entity " + key.toString() + " from GAE data store");
		Future<Entity> entity = datastore.get(trans, key);
		Entity e = waitFor(entity);
		if(useMemCache) {
			/*
			 * FIXME race condition: Any changes made to this entity at this
			 * point (between the datastore.get() and the getMemcache.put()
			 * calls) by putEntity() or deleteEntity() might be overwritten in
			 * the cache by us
			 */
			if(e != null) {
				log.debug("Putting entity " + key.toString() + " in MemCache");
				XydraRuntime.getMemcache().put(key, e);
			} else {
				log.debug("Putting NULL_ENTITY " + key.toString() + " in MemCache");
				XydraRuntime.getMemcache().put(key, NULL_ENTITY);
			}
		}
		if(e == null) {
			log.debug("--> null");
		}
		return e;
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @param trans The transaction to load the entity in.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key, Transaction trans) {
		
		makeSureDatestoreServiceIsInitialised();
		
		if(useMemCache && trans == null) {
			// try first to get from memcache
			Entity cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
			if(cachedEntity != null) {
				log.debug("Getting entity " + key.toString() + " from MemCache");
				if(cachedEntity.equals(NULL_ENTITY)) {
					log.debug("--> null");
					return null;
				} else {
					return cachedEntity;
				}
			}
		} else {
			// If there is a transaction, we must read from the actual datastore
			// so that the transaction will abort if the value is changed before
			// trans.commit().
			// TODO in some cases (ie: revision grabbing) it is ok to return an
			// old entity, but returning null could be fatal.
		}
		
		log.debug("Getting entity " + key.toString() + " from GAE data store");
		Future<Entity> entity = datastore.get(trans, key);
		Entity e = waitFor(entity);
		if(useMemCache) {
			if(e != null) {
				log.debug("Putting entity " + key.toString() + " in MemCache");
				XydraRuntime.getMemcache().put(key, e);
			} else {
				log.debug("Putting NULL_ENTITY " + key.toString() + " in MemCache");
				XydraRuntime.getMemcache().put(key, NULL_ENTITY);
			}
		}
		if(e == null) {
			log.debug("--> null");
		}
		return e;
	}
	
	private static void makeSureDatestoreServiceIsInitialised() {
		if(datastore == null) {
			datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		}
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
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
		if(useMemCache) {
			// remove first from memcache
			XydraRuntime.getMemcache().remove(entity.getKey());
		}
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 */
	public static Future<Key> putEntityAsync(Entity entity) {
		log.debug("putting (async) " + entity.getKey());
		makeSureDatestoreServiceIsInitialised();
		Future<Key> result = datastore.put(entity);
		if(useMemCache) {
			// remove first from memcache
			XydraRuntime.getMemcache().remove(entity.getKey());
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
			XydraRuntime.getMemcache().remove(key);
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
			XydraRuntime.getMemcache().remove(key);
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
	
}
