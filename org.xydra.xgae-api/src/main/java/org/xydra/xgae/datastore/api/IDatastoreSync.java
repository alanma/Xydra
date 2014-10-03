package org.xydra.xgae.datastore.api;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;

/**
 * @author xamde
 */
public interface IDatastoreSync extends IDatastoreShared {

	/**
	 * Delete ALL local data. Use with care.
	 */
	@ModificationOperation
	void clear();

	/**
	 * @param it
	 *            never null
	 */
	@ModificationOperation
	void deleteEntities(Iterable<SKey> it);

	/**
	 * Deletes the entity with the given key from GAE
	 * 
	 * @param key
	 *            The entity to remove from the datastore.
	 */
	@ModificationOperation
	void deleteEntity(SKey key);

	/**
	 * Deletes the E with the given K asynchronously from GAE
	 * 
	 * @param key
	 *            The entity to remove from the datastore.
	 * @param txn
	 */
	@ModificationOperation
	void deleteEntity(SKey key, STransaction txn);

	Map<SKey, SEntity> getEntities(Collection<SKey> keys);

	/**
	 * Batch get.
	 * 
	 * Retrieves the set of Entities matching keys. The result Map will only
	 * contain Ks for which Entities could be found.
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
	Map<SKey, SEntity> getEntities(Collection<SKey> keys, STransaction txn);

	/**
	 * @param key
	 *            never null
	 * @return a non-null entity from the datastore.
	 */
	SEntity getEntity(@NeverNull SKey key);

	/**
	 * @param key
	 *            never null
	 * @param txn
	 *            never null
	 * @return a non-null entity from the cache if available, without checking
	 *         the datastore.
	 */
	SEntity getEntity(SKey key, STransaction txn);

	/**
	 * Batch put
	 * 
	 * @param it
	 */
	@ModificationOperation
	void putEntities(Iterable<SEntity> it);

	/**
	 * Stores a GAE entity asynchronously in the GAE back-end
	 * 
	 * @param entity
	 *            The entity to write to the datastore.
	 */
	@ModificationOperation
	void putEntity(SEntity entity);

	/**
	 * Stores a GAE entity asynchronously in the GAE back-end
	 * 
	 * @param entity
	 *            The entity to write to the datastore.
	 * @param txn
	 */
	@ModificationOperation
	void putEntity(SEntity entity, STransaction txn);

	/**
	 * Begin a synchronous GAE Transaction.
	 * 
	 * @return The started Transaction.
	 */
	STransaction beginTransaction();

	/**
	 * Commit the given GAE Transaction.
	 * 
	 * @param txn
	 *            The Transaction to commit.
	 * @throws ConcurrentModificationException
	 */
	void endTransaction(STransaction txn) throws ConcurrentModificationException;

	List<String> getAllKinds();

}
