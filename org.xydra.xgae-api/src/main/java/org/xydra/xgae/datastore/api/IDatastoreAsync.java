package org.xydra.xgae.datastore.api;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface IDatastoreAsync extends IDatastoreShared {

	/**
	 * Deletes the {@link SEntity} with the given {@link SKey} from GAE
	 *
	 * @param key
	 *            The entity to remove from the datastore.
	 * @return a Future to be aware when the delete actually happens
	 */
	Future<Void> deleteEntity(SKey key);

	/**
	 * Deletes the {@link SEntity} with the given {@link SKey} asynchronously
	 * from GAE
	 *
	 * @param key
	 *            The entity to remove from the datastore.
	 * @param txn
	 * @return a Future to be aware when the delete actually happens
	 */
	Future<Void> deleteEntity(SKey key, STransaction txn);

	Future<Map<SKey, SEntity>> getEntities(Collection<SKey> keys);

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
	Future<Map<SKey, SEntity>> getEntities(Collection<SKey> keys, STransaction txn);

	/**
	 * @param key
	 *            never null
	 * @return a non-null entity from the datastore.
	 */
	Future<SEntity> getEntity(SKey key);

	/**
	 * @param key
	 *            never null
	 * @param txn
	 *            never null
	 * @return a non-null entity from the cache if available, without checking
	 *         the datastore.
	 */
	Future<SEntity> getEntity(SKey key, STransaction txn);

	/**
	 * Batch put
	 *
	 * @param it
	 * @return a future that returns the keys of the putted entities on success
	 */
	Future<List<SKey>> putEntities(Iterable<SEntity> it);

	/**
	 * Stores a GAE {@link SEntity} asynchronously in the GAE back-end
	 *
	 * @param entity
	 *            The entity to write to the datastore.
	 * @return a future that returns the key of the putted entity on success
	 */
	Future<SKey> putEntity(SEntity entity);

	/**
	 * Stores a GAE {@link SEntity} asynchronously in the GAE back-end
	 *
	 * @param entity
	 *            The entity to write to the datastore.
	 * @param txn
	 * @return a future that returns the key of the putted entity on success
	 */
	Future<SKey> putEntity(SEntity entity, STransaction txn);

	/**
	 * Begin a synchronous GAE Transaction.
	 *
	 * @return The started Transaction.
	 */
	Future<STransaction> beginTransaction();

	/**
	 * Commit the given GAE Transaction.
	 *
	 * @param txn
	 *            The Transaction to commit.
	 * @throws ConcurrentModificationException
	 */
	void endTransaction(STransaction txn) throws ConcurrentModificationException;

}
