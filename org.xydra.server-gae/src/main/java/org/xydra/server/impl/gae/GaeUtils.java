package org.xydra.server.impl.gae;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
	
	private static Key getXydraKey() {
		/*
		 * we need a common parent between XEntities and XChangeLog entries so
		 * we can have both in the same Transaction
		 */
		return KeyFactory.createKey("XYDRA", "xydra");
	}
	
	private static Key appendIdToKey(Key key, String kind, XID id) {
		if(id == null) {
			return key.getChild(kind, "-");
		} else {
			return key.getChild(kind, id.toString());
		}
	}
	
	private static Key appendAddressToKey(Key key, XAddress address) {
		
		Key k = appendIdToKey(key, "XREPOSITORY", address.getRepository());
		
		boolean f = address.getField() != null;
		boolean o = f || address.getObject() != null;
		boolean m = o || address.getModel() != null;
		
		if(m) {
			
			k = appendIdToKey(k, "XMODEL", address.getModel());
			
			if(o) {
				
				k = appendIdToKey(k, "XOBJECT", address.getObject());
				
				if(f) {
					
					k = appendIdToKey(k, "XFIELD", address.getField());
					
				}
				
			}
			
		}
		
		return k;
	}
	
	public static Key keyForEntity(XAddress address) {
		
		assert address != null;
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Key key = getXydraKey();
		key = key.getChild("XENTITY", "entities");
		key = appendAddressToKey(key, address);
		
		return key;
	}
	
	public static Key keyForLog(XAddress address) {
		
		assert address != null;
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Key key = getXydraKey();
		key = key.getChild("XLOG", "logs");
		key = appendAddressToKey(key, address);
		
		return key;
	}
	
	/**
	 * @param key
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key) {
		makeSureDatestoreServiceIsInitialised();
		return getEntity_worker(key);
	}
	
	private static Entity getEntity_worker(Key key) {
		try {
			Entity entity = datastore.get(key);
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
		makeSureDatestoreServiceIsInitialised();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		datastore.put(entity);
	}
	
	/**
	 * Stores a GAE {@link Entity} in the GAE back-end
	 * 
	 * @param entity
	 */
	public static void putEntity(Entity entity, Transaction trans) {
		assert assertTransaction(trans);
		putEntity(entity);
	}
	
	/**
	 * @param key
	 * @return the GAE Entity for the given key from the store or null
	 */
	public static Entity getEntity(Key key, Transaction trans) {
		assert assertTransaction(trans);
		return getEntity(key);
	}
	
	private static boolean assertTransaction(Transaction trans) {
		// sanity checks
		boolean inAnyTransaction = (trans != null);
		boolean inThisTransaction = false;
		if(inAnyTransaction) {
			inThisTransaction = (datastore.getCurrentTransaction() == trans);
		}
		assert !inAnyTransaction || inThisTransaction : "there should be no transaction or this transaction. In transaction? "
		        + inAnyTransaction + " In this transaction? " + inThisTransaction;
		return true;
	}
	
	/**
	 * Begin a GAE transaction.
	 * 
	 * @return The started transaction.
	 */
	public static Transaction beginTransaction() {
		makeSureDatestoreServiceIsInitialised();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return datastore.beginTransaction();
	}
	
	/**
	 * Commit the given GAE transaction.
	 * 
	 * @param trans
	 */
	public static void endTransaction(Transaction trans) {
		trans.commit();
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key
	 */
	public static void deleteEntity(Key key) {
		makeSureDatestoreServiceIsInitialised();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		datastore.delete(key);
	}
	
	/**
	 * Deletes the {@link Entity} with the given {@link Key} from GAE
	 * 
	 * @param key
	 */
	public static void deleteEntity(Key key, Transaction trans) {
		deleteEntity(key);
		assert assertTransaction(trans);
	}
	
	/**
	 * Prepares the given GAE query.
	 * 
	 * @param query
	 * @return
	 */
	public static PreparedQuery prepareQuery(Query query) {
		
		makeSureDatestoreServiceIsInitialised();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return datastore.prepare(query);
	}
	
}
