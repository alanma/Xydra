package org.xydra.xgae.datastore.api;

import java.util.List;

public interface SPreparedQuery extends SWrapper {

	/**
	 * @return an iterator of entities. If the query was key-only, the entites
	 *         are empty besides their key.
	 */
	Iterable<SEntity> asIterable();

	/**
	 * @return an iterator of keys.
	 * @throws IllegalArgumentException
	 *             if this method is called on a query that is not key-only
	 */
	Iterable<SKey> asKeysIterable() throws IllegalArgumentException;

	/**
	 * @param chunkSize
	 *            a hint to the backend how many items to get in batch from an
	 *            underlying storage system
	 * @return a list of SEntites
	 * @deprecated Was just a shorthand for setChunkSize & asList
	 */
	@Deprecated
	List<SEntity> asListWithChunkSize(int chunkSize);

	/**
	 * Implementation-dependent if this limit is enforced
	 *
	 * @param limit
	 */
	void setLimit(int limit);

	/**
	 * @return a list of SEntites
	 */
	List<SEntity> asList();

	/**
	 * @param chunkSize
	 *            a hint to the backend how many items to get in batch from an
	 *            underlying storage system
	 */
	void setChunkSize(int chunkSize);
}
