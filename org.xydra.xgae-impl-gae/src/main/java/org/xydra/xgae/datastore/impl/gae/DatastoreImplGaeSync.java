package org.xydra.xgae.datastore.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.NeverNull;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.IDatastoreSync;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SPreparedQuery;
import org.xydra.xgae.datastore.api.STransaction;
import org.xydra.xgae.gaeutils.GaeTestfixer;
import org.xydra.xgae.util.XGaeDebugHelper;
import org.xydra.xgae.util.XGaeDebugHelper.Timing;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

/**
 * Utility to use synchronous datastore
 * 
 * @author xamde
 */
public class DatastoreImplGaeSync extends DatastoreImplGaeBase implements IDatastoreSync {

	private final Logger log = LoggerFactory.getLogger(DatastoreImplGaeSync.class);

	private DatastoreService syncDatastore;

	public static final String DATASTORE_NAME = "[#DSs]";

	public DatastoreImplGaeSync() {
		// FIXME efficient enough?
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}

	/**
	 * @param key
	 *            never null
	 * @return a non-null entity from the datastore.
	 */
	@Override
	@XGaeOperation(datastoreRead = true)
	public SEntity getEntity(@NeverNull SKey key) {
		return getEntity(key, null);
	}

	private void makeSureDatestoreServiceIsInitialised() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if (this.syncDatastore == null) {
			this.log.debug(XGaeDebugHelper.init(DATASTORE_NAME));
			DatastoreServiceConfig config = DatastoreServiceConfig.Builder
					.withImplicitTransactionManagementPolicy(
							ImplicitTransactionManagementPolicy.NONE).maxEntityGroupsPerRpc(1);

			/*
			 * Not allowed in a txn context:
			 * 
			 * ReadPolicy(Consistency.EVENTUAL))
			 */

			this.syncDatastore = DatastoreServiceFactory.getDatastoreService(config);
		}
	}

	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity
	 *            The entity to write to the datastore.
	 */
	@Override
	public void putEntity(SEntity entity) {
		putEntity(entity, null);
	}

	/**
	 * @param entities
	 *            must be 2 - 5
	 */
	@XGaeOperation(datastoreWrite = true)
	public void putEntitiesInCrossGroupTransaction(GEntity... entities) {
		this.log.debug("-- begin XG Transaction --");
		makeSureDatestoreServiceIsInitialised();
		XyAssert.xyAssert(entities.length >= 2);
		XyAssert.xyAssert(entities.length <= 5);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn = datastore.beginTransaction(options);

		for (GEntity e : entities) {
			datastore.put(txn, e.raw());
		}

		txn.commit();
	}

	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity
	 *            The entity to write to the datastore.
	 * @param txn
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public void putEntity(SEntity entity, STransaction txn) {
		XyAssert.xyAssert(entity != null, "entity is null");
		assert entity != null;
		this.log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME,
				XGaeDebugHelper.toString(entity.getKey()), entity, Timing.Now));
		makeSureDatestoreServiceIsInitialised();
		assert this.syncDatastore != null;
		this.syncDatastore.put(txn == null ? null : (Transaction) txn.raw(), (Entity) entity.raw());
	}

	/**
	 * Batch put
	 * 
	 * @param it
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public void putEntities(Iterable<SEntity> it) {
		XyAssert.xyAssert(it != null, "iterable is null");
		assert it != null;
		this.log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, "entities", "many", Timing.Now));
		makeSureDatestoreServiceIsInitialised();
		this.syncDatastore.put(GEntity.unwrap(it));
	}

	/**
	 * Begin a synchronous GAE Transaction.
	 * 
	 * @return The started Transaction.
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public STransaction beginTransaction() {
		this.log.debug("-- begin Transaction --");
		makeSureDatestoreServiceIsInitialised();
		return GTransaction.wrap(this.syncDatastore.beginTransaction());
	}

	/**
	 * Commit the given GAE Transaction.
	 * 
	 * @param txn
	 *            The Transaction to commit.
	 * @throws ConcurrentModificationException
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public void endTransaction(STransaction txn) throws ConcurrentModificationException {
		this.log.debug("-- end Transaction --");
		makeSureDatestoreServiceIsInitialised();
		((Transaction) txn.raw()).commit();
	}

	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key
	 *            The entity to remove from the datastore.
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public void deleteEntity(SKey key) {
		deleteEntity(key, null);
	}

	/**
	 * Deletes the {@link Entity} with the given {@link Key} asynchronously from
	 * GAE
	 * 
	 * @param key
	 *            The entity to remove from the datastore.
	 * @param txn
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public void deleteEntity(SKey key, STransaction txn) {
		assert key != null;
		this.log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, key.toString(), null, Timing.Now));
		makeSureDatestoreServiceIsInitialised();
		this.syncDatastore.delete(txn == null ? null : (Transaction) txn.raw(), (Key) key.raw());
	}

	/**
	 * @param it
	 *            never null
	 */
	@Override
	@XGaeOperation(datastoreWrite = true)
	public void deleteEntities(Iterable<SKey> it) {
		assert it != null;
		if (this.log.isDebugEnabled()) {
			Map<Key, Object> map = new HashMap<Key, Object>();
			for (SKey k : it) {
				map.put((Key) k.raw(), null);
			}
			this.log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, map, Timing.Now));
		}
		makeSureDatestoreServiceIsInitialised();
		this.syncDatastore.delete(GKey.unwrap(it));
	}

	/**
	 * @return true if there are Transactions active, so that tests can check if
	 *         all Transactions are terminated properly.
	 */
	@Override
	@XGaeOperation()
	public boolean isTransactionsActive() {
		return !this.syncDatastore.getActiveTransactions().isEmpty();
	}

	/**
	 * Delete ALL local data. Use with care.
	 */
	@Override
	@XGaeOperation(datastoreWrite = true, datastoreRead = true)
	public void clear() {
		this.log.info("Datastore clear");
		makeSureDatestoreServiceIsInitialised();
		deleteAllDataOnLiveDatastore();
		// FIXME for local usage
		deleteAllEntitiesOneByOne();
	}

	public void deleteAllEntitiesOneByOne() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query mydeleteq = new Query();
		PreparedQuery pq = datastore.prepare(mydeleteq);
		for (Entity result : pq.asIterable()) {
			datastore.delete(result.getKey());
		}
	}

	@XGaeOperation(datastoreWrite = true)
	private void deleteAllDataOnLiveDatastore() {
		List<String> kinds = getAllKinds();
		for (String kind : kinds) {
			List<Key> keys = new LinkedList<Key>();
			Query q = new Query(kind).setKeysOnly();
			PreparedQuery pq = this.syncDatastore.prepare(q);
			for (Entity entity : pq.asIterable()) {
				keys.add(entity.getKey());
			}
			try {
				this.syncDatastore.delete(keys);
			} catch (Exception e) {
				this.log.warn("Could not delete kind '" + kind + "'", e);
			}
		}
	}

	/**
	 * @return all kinds that do not start with '__'
	 */
	@Override
	@XGaeOperation(datastoreRead = true)
	public List<String> getAllKinds() {
		makeSureDatestoreServiceIsInitialised();
		List<String> kinds = new LinkedList<String>();
		Iterable<Entity> statKinds = this.syncDatastore.prepare(new Query("__Stat_Kind__"))
				.asIterable();
		for (Entity statKind : statKinds) {
			String kind = statKind.getProperty("kind_name").toString();
			if (!kind.startsWith("__")) {
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
	 * @param keys
	 *            never null
	 * @param txn
	 * @return a mapping for all keys that could be found
	 */
	@Override
	@XGaeOperation(datastoreRead = true)
	public Map<SKey, SEntity> getEntities(Collection<SKey> keys, STransaction txn) {
		assert keys != null;
		makeSureDatestoreServiceIsInitialised();
		Map<Key, Entity> result;
		if (keys.isEmpty()) {
			result = Collections.emptyMap();
		} else {
			result = this.syncDatastore.get(txn == null ? null : (Transaction) txn.raw(),
					GKey.unwrap(keys));
		}
		this.log.debug(XGaeDebugHelper.dataGet(DATASTORE_NAME, keys, result, Timing.Now));
		return GMaps.wrap(result);
	}

	@Override
	public Map<SKey, SEntity> getEntities(Collection<SKey> keys) {
		return getEntities(keys, null);
	}

	/**
	 * @param key
	 *            never null
	 * @param txn
	 *            never null
	 * @return a non-null entity from the cache if available, without checking
	 *         the datastore.
	 */
	@Override
	@XGaeOperation(datastoreRead = true)
	public GEntity getEntity(SKey key, STransaction txn) {
		assert key != null;
		makeSureDatestoreServiceIsInitialised();
		Entity e;
		try {
			e = this.syncDatastore.get(txn == null ? null : (Transaction) txn.raw(),
					(Key) key.raw());
		} catch (EntityNotFoundException e1) {
			e = null;
		}
		this.log.debug(XGaeDebugHelper.dataGet(DATASTORE_NAME, XGaeDebugHelper.toString(key), e,
				Timing.Now));
		return GEntity.wrap(e);
	}

	@Override
	public SPreparedQuery prepareRangeQuery(String kind, boolean keysOnly, String lowName,
			String highName) {
		return prepareRangeQuery(kind, keysOnly, lowName, highName, null);
	}

	@Override
	public SPreparedQuery prepareRangeQuery(String kind, boolean keysOnly, String lowName,
			String highName, STransaction txn) {
		makeSureDatestoreServiceIsInitialised();
		Query query = createRangeQuery(kind, keysOnly, lowName, highName);
		assert query != null;
		assert this.syncDatastore != null;
		return GPreparedQuery.wrap(this.syncDatastore.prepare(txn == null ? null
				: (Transaction) txn.raw(), query));
	}

	@Override
	public String getDatastoreName() {
		return DATASTORE_NAME;
	}

}
