package org.xydra.server.impl.gae;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.store.impl.gae.GaeTestfixer;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Mapping from XModel {@link XAddress} to GAE {@link Entity} and {@link Key}
 * 
 * @author voelkel
 * 
 */
public class OldGaeUtils {
	
	/**
	 * @return a common parent key, never null
	 */
	private static Key getXydraKey() {
		/*
		 * we need a common parent between XEntities and XChangeLog entries so
		 * we can have both in the same Transaction
		 * 
		 * TODO isn't this causing datastore contention? ~max
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
	
	/**
	 * @param address
	 * @return a GAE {@link Key} for an entity (repo,object,model,field) with
	 *         the given address. Never null.
	 */
	public static Key keyForEntity(XAddress address) {
		assert address != null;
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Key key = getXydraKey();
		key = key.getChild("XENTITY", "entities");
		key = appendAddressToKey(key, address);
		return key;
	}
	
	/**
	 * @param address
	 * @return a GAE {@link Key} for a change log of an entity with the given
	 *         address. Never null.
	 */
	public static Key keyForLog(XAddress address) {
		assert address != null;
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Key key = getXydraKey();
		key = key.getChild("XLOG", "logs");
		key = appendAddressToKey(key, address);
		return key;
	}
	
}
