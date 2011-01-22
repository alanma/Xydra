package org.xydra.store.impl.gae;

import java.util.ConcurrentModificationException;

import org.xydra.base.XAddress;

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
	
	private static DatastoreService datastore;
	
	/**
	 * @param key
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key) {
		return getEntity(key, null);
	}
	
	/**
	 * @param key
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key, Transaction trans) {
		makeSureDatestoreServiceIsInitialised();
		try {
			Entity entity = datastore.get(trans, key);
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
	 * @param entity
	 */
	public static void putEntity(Entity entity) {
		putEntity(entity, null);
	}
	
	/**
	 * Stores a GAE {@link Entity} in the GAE back-end
	 * 
	 * @param entity
	 */
	public static void putEntity(Entity entity, Transaction trans) {
		makeSureDatestoreServiceIsInitialised();
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
	 * @param trans
	 */
	public static void endTransaction(Transaction trans) throws ConcurrentModificationException {
		makeSureDatestoreServiceIsInitialised();
		trans.commit();
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key
	 */
	public static void deleteEntity(Key key) {
		deleteEntity(key, null);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key
	 */
	public static void deleteEntity(Key key, Transaction trans) {
		makeSureDatestoreServiceIsInitialised();
		datastore.delete(trans, key);
	}
	
	/**
	 * Prepares the given GAE query.
	 * 
	 * @param query
	 * @return a GAE prepared query
	 */
	public static PreparedQuery prepareQuery(Query query) {
		return prepareQuery(query, null);
	}
	
	/**
	 * Prepares the given GAE query.
	 * 
	 * @param query
	 * @return a GAE prepared query
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
