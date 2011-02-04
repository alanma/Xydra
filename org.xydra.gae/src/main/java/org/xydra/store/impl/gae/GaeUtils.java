package org.xydra.store.impl.gae;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

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
	
	private static boolean useLocalVmCache = true;
	
	private static Map<Key,Entity> localVmCache = new HashMap<Key,Entity>();
	
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
	 * @param localvmcache must be 'true' or 'false'
	 * @return the current configuration as a string
	 */
	public static String setCacheConf(String memcache, String localvmcache) {
		boolean memcache_ = Boolean.parseBoolean(memcache);
		boolean localvmcache_ = Boolean.parseBoolean(localvmcache);
		setUseMemCache(memcache_);
		setUseLocalVmCache(localvmcache_);
		return getConf();
	}
	
	public static void setUseLocalVmCache(boolean b) {
		useLocalVmCache = b;
	}
	
	public static String getConf() {
		return

		"MemCache: " + useMemCache + "\n" +

		"LocalVmCache: " + useLocalVmCache + "\n"

		+ getStats();
	}
	
	public static String getStats() {
		return

		"MemCache: " + XydraRuntime.getMemcache().size() + " entries \n" +

		"LocalVmCache: " + localVmCache.size() + " entries";
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key) {
		return getEntity(key, null);
	}
	
	/**
	 * @param key The key of the entity to load.
	 * @param trans The transaction to load the entity in.
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key, Transaction trans) {
		makeSureDatestoreServiceIsInitialised();
		try {
			if(useLocalVmCache) {
				// try first to get from memcache
				Entity cachedEntity = localVmCache.get(key);
				if(cachedEntity != null) {
					log.debug("Getting entity " + key.toString() + " from LocalVmCache");
					return cachedEntity;
				}
			}
			if(useMemCache) {
				// try first to get from memcache
				Entity cachedEntity = (Entity)XydraRuntime.getMemcache().get(key);
				if(cachedEntity != null) {
					log.debug("Getting entity " + key.toString() + " from MemCache");
					return cachedEntity;
				}
			}
			
			log.debug("Getting entity " + key.toString() + " from GAE data store");
			Entity entity = datastore.get(trans, key);
			
			if(useLocalVmCache) {
				localVmCache.put(entity.getKey(), entity);
			}
			if(useMemCache) {
				// add also to the memcache
				XydraRuntime.getMemcache().put(entity.getKey(), entity);
			}
			
			return entity;
		} catch(EntityNotFoundException e) {
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
		if(useLocalVmCache) {
			localVmCache.put(entity.getKey(), entity);
		}
		if(useMemCache) {
			// add first to memcache
			XydraRuntime.getMemcache().put(entity.getKey(), entity);
		}
		datastore.put(trans, entity);
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
		if(useLocalVmCache) {
			localVmCache.remove(key);
		}
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
