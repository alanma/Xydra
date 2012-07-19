package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
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
 * Utility to use asynchronous datastore
 * 
 * @author voelkel
 */
public class AsyncDatastore {
	
	private static final Logger log = LoggerFactory.getLogger(AsyncDatastore.class);
	
	private static AsyncDatastoreService asyncDatastore;
	
	public static final String DATASTORE_NAME = "[#DSa]";
	
	/**
	 * @param key never null
	 * @return a non-null entity from the datastore.
	 */
	@GaeOperation(datastoreRead = true)
	public static Future<Entity> getEntity(Key key) {
		return getEntity(key, null);
	}
	
	private static void makeSureDatestoreServiceIsInitialised() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(asyncDatastore == null) {
			log.debug(DebugFormatter.init(DATASTORE_NAME));
			asyncDatastore = DatastoreServiceFactory.getAsyncDatastoreService();
		}
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 * @return a future that returns the key of the putted entity on success
	 */
	public static Future<Key> putEntity(Entity entity) {
		return putEntity(entity, null);
	}
	
	/**
	 * Stores a GAE {@link Entity} asynchronously in the GAE back-end
	 * 
	 * @param entity The entity to write to the datastore.
	 * @param txn
	 * @return a future that returns the key of the putted entity on success
	 */
	@GaeOperation(datastoreWrite = true)
	public static Future<Key> putEntity(Entity entity, Transaction txn) {
		XyAssert.xyAssert(entity != null, "entity is null");
		assert entity != null;
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, KeyStructure.toString(entity.getKey()),
		        entity, Timing.Started));
		makeSureDatestoreServiceIsInitialised();
		
		// fake async version
		// SyncDatastore.putEntity(entity, txn);
		// syncDatastore.put(txn, entity);
		// return FutureUtils.createCompleted(null);
		
		// true async version
		Future<Key> f = asyncDatastore.put(txn, entity);
		return new FuturePutEntity(entity, f);
	}
	
	/**
	 * Batch put
	 * 
	 * @param it
	 * @return a future that returns the keys of the putted entities on success
	 */
	@GaeOperation(datastoreWrite = true)
	public static FuturePutEntities putEntities(Iterable<Entity> it) {
		XyAssert.xyAssert(it != null, "iterable is null");
		assert it != null;
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, "entities", "many", Timing.Now));
		makeSureDatestoreServiceIsInitialised();
		Future<List<Key>> f = asyncDatastore.put(it);
		return new FuturePutEntities(it, f);
	}
	
	/**
	 * Begin a synchronous GAE Transaction.
	 * 
	 * @return The started Transaction.
	 */
	@GaeOperation(datastoreWrite = true)
	public static Future<Transaction> beginTransaction() {
		log.debug("-- begin Transaction --");
		makeSureDatestoreServiceIsInitialised();
		return asyncDatastore.beginTransaction();
	}
	
	/**
	 * Commit the given GAE Transaction.
	 * 
	 * @param txn The Transaction to commit.
	 * @throws ConcurrentModificationException
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
	 * @return a Future to be aware when the delete actually happens
	 */
	@GaeOperation(datastoreWrite = true)
	public static Future<Void> deleteEntity(Key key) {
		return deleteEntity(key, null);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} asynchronously from
	 * GAE
	 * 
	 * @param key The entity to remove from the datastore.
	 * @param txn
	 * @return a Future to be aware when the delete actually happens
	 */
	@GaeOperation(datastoreWrite = true)
	public static Future<Void> deleteEntity(Key key, Transaction txn) {
		assert key != null;
		log.debug(DebugFormatter.dataPut(DATASTORE_NAME, key.toString(), null, Timing.Started));
		makeSureDatestoreServiceIsInitialised();
		
		// FIXME ASYNC AGAIN
		SyncDatastore.deleteEntity(key, txn);
		return FutureUtils.createCompleted(null);
		
		// Future<Void> future = asyncDatastore.delete(txn, key);
		// return new FutureDeleteEntity(key, future);
	}
	
	/**
	 * Prepares the given GAE query.
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
	 * @param txn
	 * @return a GAE prepared query
	 * 
	 * @see DatastoreService#prepare(Transaction, Query)
	 */
	@GaeOperation()
	public static PreparedQuery prepareQuery(Query query, Transaction txn) {
		assert query != null;
		makeSureDatestoreServiceIsInitialised();
		return asyncDatastore.prepare(txn, query);
	}
	
	/**
	 * @return true if there are Transactions active, so that tests can check if
	 *         all Transactions are terminated properly.
	 */
	@GaeOperation()
	public static boolean TransactionsActive() {
		return !asyncDatastore.getActiveTransactions().isEmpty();
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
	 * @param txn
	 * @return a mapping for all keys that could be found
	 */
	@GaeOperation(datastoreRead = true)
	public static Future<Map<Key,Entity>> getEntities(Collection<Key> keys, Transaction txn) {
		assert keys != null;
		makeSureDatestoreServiceIsInitialised();
		Future<Map<Key,Entity>> result;
		if(keys.isEmpty()) {
			Map<Key,Entity> emptyMap = Collections.emptyMap();
			result = FutureUtils.createCompleted(emptyMap);
		} else {
			result = asyncDatastore.get(txn, keys);
		}
		return new FutureGetEntities(keys, result);
	}
	
	/**
	 * Print debug info at moment of retrieval
	 * 
	 * @author xamde
	 */
	private static class FutureGetEntities extends DebugFuture<Collection<Key>,Map<Key,Entity>> {
		
		public FutureGetEntities(Collection<Key> keys, Future<Map<Key,Entity>> f) {
			super(keys, f);
		}
		
		@Override
		void log(Collection<Key> key, Map<Key,Entity> result) {
			log.debug(DebugFormatter.dataGet(DATASTORE_NAME, key, result, Timing.Finished));
		}
	}
	
	private static abstract class DebugFuture<K, V> implements Future<V> {
		
		private Future<V> f;
		private K key;
		
		public DebugFuture(K key, Future<V> f) {
			this.key = key;
			this.f = f;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return this.f.cancel(mayInterruptIfRunning);
		}
		
		@Override
		public boolean isCancelled() {
			return this.f.isCancelled();
		}
		
		@Override
		public boolean isDone() {
			return this.f.isDone();
		}
		
		@Override
		public V get() throws InterruptedException, ExecutionException {
			V result = this.f.get();
			log(this.key, result);
			return result;
		}
		
		abstract void log(K key, V result);
		
		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
		        TimeoutException {
			V result = this.f.get(timeout, unit);
			log(this.key, result);
			return result;
		}
		
	}
	
	private static class FuturePutEntity extends DebugFuture<Entity,Key> {
		public FuturePutEntity(Entity key, Future<Key> f) {
			super(key, f);
		}
		
		@Override
		void log(Entity key, Key result) {
			log.debug(DebugFormatter.dataPut(DATASTORE_NAME, KeyStructure.toString(result), key,
			        Timing.Finished));
		}
	}
	
	private static class FuturePutEntities extends DebugFuture<Iterable<Entity>,List<Key>> {
		public FuturePutEntities(Iterable<Entity> it, Future<List<Key>> f) {
			super(it, f);
		}
		
		@Override
		void log(Iterable<Entity> key, List<Key> result) {
			log.debug(DebugFormatter.dataPut(DATASTORE_NAME, "manyKeys", key, Timing.Finished));
		}
	}
	
	private static class FutureGetEntity extends DebugFuture<Key,Entity> {
		public FutureGetEntity(Key key, Future<Entity> f) {
			super(key, f);
		}
		
		@Override
		void log(Key key, Entity result) {
			log.debug(DebugFormatter.dataGet(DATASTORE_NAME, KeyStructure.toString(key), result,
			        Timing.Finished));
		}
	}
	
	@SuppressWarnings("unused")
	private static class FutureDeleteEntity extends DebugFuture<Key,Void> {
		public FutureDeleteEntity(Key key, Future<Void> f) {
			super(key, f);
		}
		
		@Override
		void log(Key key, Void result) {
			log.debug(DebugFormatter.dataPut(DATASTORE_NAME, KeyStructure.toString(key), null,
			        Timing.Finished));
		}
	}
	
	public static Future<Map<Key,Entity>> getEntities(Collection<Key> keys) {
		return getEntities(keys, null);
	}
	
	/**
	 * @param key never null
	 * @param txn never null
	 * @return a non-null entity from the cache if available, without checking
	 *         the datastore.
	 */
	@GaeOperation(datastoreRead = true)
	public static Future<Entity> getEntity(Key key, Transaction txn) {
		assert key != null;
		makeSureDatestoreServiceIsInitialised();
		Future<Entity> e = asyncDatastore.get(txn, key);
		return new FutureGetEntity(key, e);
	}
	
	/**
	 * Wraps a Future<Entity> and remember the result of {@link Future#get()}
	 * 
	 * @author scharrer
	 */
	// TODO remove this class or what purpose does it serve?
	public static class AsyncEntity {
		
		private Future<Entity> future;
		private Entity entity;
		@SuppressWarnings("unused")
		private Key key;
		
		AsyncEntity(Key key, Future<Entity> future) {
			this.future = future;
			this.entity = null;
			this.key = key;
		}
		
		AsyncEntity(Entity entity) {
			this.future = null;
			this.entity = entity;
			this.key = null;
		}
		
		public Entity get() {
			if(this.future != null) {
				this.entity = FutureUtils.waitFor(this.future);
				this.future = null;
			}
			return this.entity;
		}
		
	}
	
}
