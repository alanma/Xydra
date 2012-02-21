package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;


/**
 * Utility to use synchronous datastore
 * 
 * @author voelkel
 */
public class SyncDatastore {
	
	private static final Logger log = LoggerFactory.getLogger(SyncDatastore.class);
	
	private static DatastoreService syncDatastore;
	
	public static final String DATASTORE_NAME = "[#DSs]";
	
	/**
	 * @param key never null
	 * @return a non-null entity from the datastore.
	 */
	@GaeOperation(datastoreRead = true)
	public static Entity getEntity(Key key) {
		return getEntity(key, null);
	}
	
	private static void makeSureDatestoreServiceIsInitialised() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(syncDatastore == null) {
			log.debug(DebugFormatter.init(DATASTORE_NAME));
			syncDatastore = DatastoreServiceFactory.getDatastoreService();
		}
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 */
	public static void putEntity(Entity entity) {
		putEntity(entity, null);
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 */
	@GaeOperation(datastoreWrite = true)
	public static void putEntity(Entity entity, Transaction txn) {
		GaeAssert.gaeAssert(entity != null, "entity is null");
		assert entity != null;
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, KeyStructure.toString(entity.getKey()),
		        entity, Timing.Now));
		makeSureDatestoreServiceIsInitialised();
		syncDatastore.put(txn, entity);
	}
	
	/**
	 * Begin a synchronous GAE Transaction.
	 * 
	 * @return The started Transaction.
	 */
	@GaeOperation(datastoreWrite = true)
	public static Transaction beginTransaction() {
		log.debug("-- begin Transaction --");
		makeSureDatestoreServiceIsInitialised();
		return syncDatastore.beginTransaction();
	}
	
	/**
	 * Commit the given GAE Transaction.
	 * 
	 * @param txn The Transaction to commit.
	 */
	@GaeOperation(datastoreWrite = true)
	public static void endTransaction(Transaction txn) throws ConcurrentModificationException {
		log.debug("-- end Transaction --");
		makeSureDatestoreServiceIsInitialised();
		txn.commit();
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 */
	@GaeOperation(datastoreWrite = true)
	public static void deleteEntity(Key key) {
		deleteEntity(key, null);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} asynchronously from
	 * GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 */
	@GaeOperation(datastoreWrite = true)
	public static void deleteEntity(Key key, Transaction txn) {
		assert key != null;
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, key.toString(), null, Timing.Now));
		makeSureDatestoreServiceIsInitialised();
		syncDatastore.delete(txn, key);
	}
	
	/**
	 * Prepares the given GAE query.
	 * 
	 * How-to:
	 * 
	 * <pre>
	 * Query q = new Query(kind).addFilter(PROP_KEY, FilterOperator.GREATER_THAN, first).setKeysOnly();
	 * </pre>
	 * 
	 * @param query The query to prepare.
	 * @return a GAE prepared query
	 * 
	 * @see DatastoreService#prepare(Query)
	 */
	@GaeOperation()
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
	@GaeOperation()
	public static PreparedQuery prepareQuery(Query query, Transaction txn) {
		assert query != null;
		makeSureDatestoreServiceIsInitialised();
		return syncDatastore.prepare(txn, query);
	}
	
	/**
	 * @return true if there are Transactions active, so that tests can check if
	 *         all Transactions are terminated properly.
	 */
	@GaeOperation()
	public static boolean TransactionsActive() {
		return !syncDatastore.getActiveTransactions().isEmpty();
	}
	
	/**
	 * Delete ALL local data. Use with care.
	 */
	@GaeOperation(datastoreWrite = true ,datastoreRead = true)
	public static void clear() {
		log.info("Datastore clear");
		makeSureDatestoreServiceIsInitialised();
		deleteAllDataOnLiveDatastore();
		// FIXME for local usage
		deleteAllEntitiesOneByOne();
	}
	
	public static void deleteAllEntitiesOneByOne() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query mydeleteq = new Query();
		PreparedQuery pq = datastore.prepare(mydeleteq);
		for(Entity result : pq.asIterable()) {
			datastore.delete(result.getKey());
		}
	}
	
	@GaeOperation(datastoreWrite = true)
	private static void deleteAllDataOnLiveDatastore() {
		List<String> kinds = getAllKinds();
		for(String kind : kinds) {
			List<Key> keys = new LinkedList<Key>();
			Query q = new Query(kind).setKeysOnly();
			PreparedQuery pq = syncDatastore.prepare(q);
			for(Entity entity : pq.asIterable()) {
				keys.add(entity.getKey());
			}
			try {
				syncDatastore.delete(keys);
			} catch(Exception e) {
				log.warn("Could not delete kind '" + kind + "'", e);
			}
		}
	}
	
	/**
	 * @return all kinds that do not start with '__'
	 */
	@GaeOperation(datastoreRead = true)
	public static List<String> getAllKinds() {
		makeSureDatestoreServiceIsInitialised();
		List<String> kinds = new LinkedList<String>();
		Iterable<Entity> statKinds = syncDatastore.prepare(new Query("__Stat_Kind__")).asIterable();
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
	 * Note: If there is a current Transaction, this operation will execute
	 * within that Transaction. In this case it is up to the caller to commit or
	 * rollback. If there is no current Transaction, the behavior of this method
	 * with respect to Transactions will be determined by the
	 * ImplicitTransactionManagementPolicy available on the
	 * DatastoreServiceConfig.
	 * 
	 * @param keys never null
	 * @return a mapping for all keys that could be found
	 */
	@GaeOperation(datastoreRead = true)
	public static Map<Key,Entity> getEntities(Collection<Key> keys, Transaction txn) {
		assert keys != null;
		makeSureDatestoreServiceIsInitialised();
		Map<Key,Entity> result;
		if(keys.isEmpty()) {
			result = Collections.emptyMap();
		} else {
			result = syncDatastore.get(txn, keys);
		}
		log.debug(DebugFormatter.dataGet(DATASTORE_NAME, keys, result, Timing.Now));
		return result;
	}
	
	public static Map<Key,Entity> getEntities(Collection<Key> keys) {
		return getEntities(keys, null);
	}
	
	/**
	 * @param key never null
	 * @param txn never null
	 * @return a non-null entity from the cache if available, without checking
	 *         the datastore.
	 */
	@GaeOperation(datastoreRead = true)
	public static Entity getEntity(Key key, Transaction txn) {
		assert key != null;
		makeSureDatestoreServiceIsInitialised();
		Entity e;
		try {
			e = syncDatastore.get(txn, key);
		} catch(EntityNotFoundException e1) {
			e = null;
		}
		log.debug(DebugFormatter.dataGet(DATASTORE_NAME, KeyStructure.toString(key), e, Timing.Now));
		return e;
	}
	
}
