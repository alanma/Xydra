package org.xydra.store.impl.gae;

import java.util.ConcurrentModificationException;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
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
	
	private static DatastoreService datastore;
	
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
	
	/**
	 * @param key The key of the entity to load.
	 * @param trans The transaction to load the entity in.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key, Transaction trans) {
		makeSureDatestoreServiceIsInitialised();
		try {
			if(useMemCache) {
				// try first to get from memcache
				Entity cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
				if(cachedEntity != null) {
					log.debug("Getting entity " + key.toString() + " from MemCache");
					if(cachedEntity.equals(NULL_ENTITY)) {
						return null;
					} else {
						return cachedEntity;
					}
				}
			}
			
			log.debug("Getting entity " + key.toString() + " from GAE data store");
			Entity entity = datastore.get(trans, key);
			if(useMemCache) {
				log.debug("Putting entity " + key.toString() + " in MemCache");
				XydraRuntime.getMemcache().put(key, entity);
			}
			return entity;
		} catch(EntityNotFoundException e) {
			if(useMemCache) {
				log.debug("Putting NULL_ENTITY " + key.toString() + " in MemCache");
				XydraRuntime.getMemcache().put(key, NULL_ENTITY);
			}
			
			return null;
		}
	}
	
	private static void makeSureDatestoreServiceIsInitialised() {
		if(datastore == null) {
			datastore = DatastoreServiceFactory.getDatastoreService();
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
		makeSureDatestoreServiceIsInitialised();
		datastore.put(trans, entity);
		if(useMemCache) {
			// remove first from memcache
			XydraRuntime.getMemcache().remove(entity.getKey());
		}
	}
	
	/**
	 * Begin a GAE transaction.
	 * 
	 * @return The started transaction.
	 */
	public static Transaction beginTransaction() {
		makeSureDatestoreServiceIsInitialised();
		return datastore.beginTransaction();
	}
	
	/**
	 * Commit the given GAE transaction.
	 * 
	 * @param trans The transaction to commit.
	 */
	public static void endTransaction(Transaction trans) throws ConcurrentModificationException {
		makeSureDatestoreServiceIsInitialised();
		trans.commit();
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
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 * @param trans The transaction to remove the entity in.
	 */
	public static void deleteEntity(Key key, Transaction trans) {
		makeSureDatestoreServiceIsInitialised();
		if(useMemCache) {
			// delete first in memcache
			XydraRuntime.getMemcache().remove(key);
		}
		datastore.delete(trans, key);
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
