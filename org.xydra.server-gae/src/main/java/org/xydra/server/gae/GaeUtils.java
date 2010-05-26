package org.xydra.server.gae;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.core.X;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.impl.memory.MemoryAddress;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;



/**
 * Mapping from XModel {@link XAddress} to GAE {@link Entity} and {@link Key}
 * 
 * @author voelkel
 * 
 */
public class GaeUtils {
	
	private static DatastoreService datastore;
	
	/**
	 * @param repositoryID
	 * @param modelID
	 * @return a GAE key
	 */
	public static Key toGaeKey(XAddress address) {
		assert address != null;
		String kind = MemoryAddress.getAddressedType(address).name();
		assert kind != null;
		Key key;
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// try {
		key = KeyFactory.createKey(kind, address.toString());
		// } catch(NullPointerException e) {
		// if(e.getMessage().equals("No API environment is registered for this thread."))
		// {
		// // handle
		// // FIXME
		// System.out.println("Sie werden geholfen bei Address " +
		// address.toString()
		// + " kind=" + kind);
		//				
		// // ApiProxy.setEnvironmentForCurrentThread(new
		// // LocalStubEnvironment());
		//				
		// // LocalServiceTestHelper helper = new LocalServiceTestHelper(
		// // new LocalDatastoreServiceTestConfig());
		// // helper.setUp();
		//				
		// key = KeyFactory.createKey(kind, address.toString());
		// } else {
		// throw new RuntimeException(e);
		// }
		// }
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
				XID id = X.getIDProvider().fromString(idString);
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
			idList.add(X.getIDProvider().fromString(s));
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
