package org.xydra.xgae.datastore.api;

public interface IDatastoreShared {

	/**
	 * Prepares the given query.
	 * 
	 * Impl note: Yes, this method behaves differently on sync and sync
	 * datastore.
	 * 
	 * @param kind
	 *            for creating the lowKey and the highKey which together form
	 *            the queried interval (lowKey,highKey).
	 * @param keysOnly
	 *            if true, the query is very fast and returns entities that have
	 *            only the key defined
	 * @param lowName
	 *            inclusive, for the name part of the lowKey @CanBeNull to
	 *            denote that there is no minimal key
	 * @param highName
	 *            inclusive, for the name part of the highKey
	 * @param query
	 *            The query to prepare.
	 * @return a prepared query
	 */
	SPreparedQuery prepareRangeQuery(String kind, boolean keysOnly, String lowName, String highName);

	/**
	 * Prepares the given query.
	 * 
	 * Impl note: Yes, this method behaves differently on sync and sync
	 * datastore.
	 * 
	 * @param kind
	 *            for creating the lowKey and the highKey which together form
	 *            the queried interval (lowKey,highKey).
	 * @param keysOnly
	 *            if true, the query is very fast and returns entities that have
	 *            only the key defined
	 * @param lowestName
	 *            inclusive, @CanBeNull to denote there is no lower bound -
	 *            requires a highestName in this case
	 * @param highestName
	 *            inclusive @CanBeNull to denote there is no upper bound -
	 *            requires a lowestName in this case
	 * @param transaction
	 *            query context (all writes within the same txn can be read)
	 * @param query
	 *            The query to prepare.
	 * @return a prepared query
	 */
	SPreparedQuery prepareRangeQuery(String kind, boolean keysOnly, String lowestName,
			String highestName, STransaction transaction);

	/**
	 * @return true if there are Transactions active, so that tests can check if
	 *         all Transactions are terminated properly.
	 */
	boolean isTransactionsActive();

	/**
	 * @return a data store name useful for debugging
	 */
	String getDatastoreName();

}
