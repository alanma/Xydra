package org.xydra.server.gae;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XStateTransaction;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
	public static void putEntity(Entity entity, XStateTransaction trans) {
		putEntity(entity);
		assert assertTransaction(trans);
	}
	
	private static boolean assertTransaction(XStateTransaction trans) {
		// sanity checks
		boolean inAnyTransaction = (trans != null);
		boolean inThisTransaction = false;
		if(inAnyTransaction) {
			inThisTransaction = (datastore.getCurrentTransaction() == asTransaction(trans));
		}
		assert !inAnyTransaction || inThisTransaction : "there should be no transaction or this transaction. In transaction? "
		        + inAnyTransaction + " In this transaction? " + inThisTransaction;
		return true;
	}
	
	public static XStateTransaction beginTransaction() {
		// work around error in DataApiTest
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return new GaeStateTransactionImpl(datastore.beginTransaction());
	}
	
	public static class GaeStateTransactionImpl implements XStateTransaction {
		
		Transaction gaeTransaction;
		
		public GaeStateTransactionImpl(Transaction transaction) {
			this.gaeTransaction = transaction;
		}
		
		@Override
		public int hashCode() {
			return this.gaeTransaction.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			return other instanceof GaeStateTransactionImpl
			        && ((GaeStateTransactionImpl)other).gaeTransaction.equals(this.gaeTransaction);
		}
		
	}
	
	public static Transaction asTransaction(XStateTransaction trans) {
		if(!(trans instanceof GaeStateTransactionImpl)) {
			throw new IllegalArgumentException("unexpected transaction object oy type "
			        + trans.getClass());
		}
		
		return ((GaeStateTransactionImpl)trans).gaeTransaction;
	}
	
	public static void endTransaction(XStateTransaction trans) {
		asTransaction(trans).commit();
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
	public static void deleteEntity(Key key, XStateTransaction trans) {
		deleteEntity(key);
		assert assertTransaction(trans);
	}
	
	/**
	 * A pure in-memory String manipulation method
	 * 
	 * @param idList
	 * @return the {@link XID}s as a single string, separated by a colon.
	 * @deprecated Store Collections directly in GAE
	 */
	@Deprecated
	public static String toString(List<XID> idList) {
		return toString(idList.iterator());
	}
	
	/**
	 * A pure in-memory String manipulation method
	 * 
	 * @param idList
	 * @return the {@link XID}s as a single string, separated by a colon.
	 * @deprecated Store Collections directly in GAE
	 */
	@Deprecated
	public static String toString(Iterator<XID> idList) {
		StringBuffer buf = new StringBuffer();
		while(idList.hasNext()) {
			XID id = idList.next();
			buf.append(id.toString());
			buf.append(":");
		}
		return buf.toString();
	}
	
	/**
	 * A pure in-memory String manipulation method
	 * 
	 * @param idListAsString
	 * @return List of {@link XID}s parsed from a single colon-separated string
	 * @deprecated Store Collections directly in GAE
	 */
	@Deprecated
	public static List<XID> toIdList(String idListAsString) {
		String[] idStrings = idListAsString.split(":");
		LinkedList<XID> ids = new LinkedList<XID>();
		for(String idString : idStrings) {
			if(idString.equals("")) {
				// skip
			} else {
				XID id = XX.toId(idString);
				ids.add(id);
			}
		}
		return ids;
	}
	
	public static List<XID> toListOfXID(List<String> stringList) {
		if(stringList == null)
			return Collections.emptyList();
		
		List<XID> idList = new LinkedList<XID>();
		for(String s : stringList) {
			assert s != null;
			idList.add(XX.toId(s));
		}
		return idList;
	}
	
	public static List<String> toListOfString(Iterator<XID> iterator) {
		List<String> stringList = new LinkedList<String>();
		while(iterator.hasNext()) {
			stringList.add(iterator.next().toString());
		}
		return stringList;
	}
	
}
