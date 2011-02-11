package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * A local cache running just inside one virtual machine.
 * 
 * @author xamde
 * 
 */
public class LocalVmCache {
	
	public static Map<Key,Entity> localVmCache = new HashMap<Key,Entity>();
	
}
