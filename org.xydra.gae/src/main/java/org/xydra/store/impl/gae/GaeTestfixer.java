package org.xydra.store.impl.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.utils.SystemProperty;


/**
 * A helper class to turn the simulated local AppEngine environment into a
 * singleton.
 * 
 * If we run in a JUnit test behind Jersey, which uses reflection, we are in a
 * different thread and need to take additional measure to tell this thread
 * about the GAE-test-setup
 * 
 * TODO Remove class if AppEngine issue is ever fixed by Google
 * 
 * http://code.google.com/p/googleappengine/issues/detail?id=2201
 * 
 * @author xamde
 */
public class GaeTestfixer {
	
	private static boolean enabled = false;
	/** checking for production env only once makes this run faster */
	private static boolean checkedProduction = false;
	
	public static void enable() {
		enabled = true;
		checkedProduction = false;
	}
	
	/**
	 * @return true if app is running on a real remote GAE server
	 */
	public static boolean inProduction() {
		return SystemProperty.environment.get() != null
		        && SystemProperty.environment.value().equals(
		                SystemProperty.Environment.Value.Production);
	}
	
	/**
	 * Fix testing in development mode which spawns multiple threads, which
	 * cannot not happen on AppEngine in production mode.
	 * 
	 * This method just returns, doing nothing if {@link GaeTestfixer#enable()}
	 * is not called from main code.
	 */
	public static void initialiseHelperAndAttachToCurrentThread() {
		if(!enabled) {
			return;
		}
		
		/* if enabled and in production: self-disable */
		if(!checkedProduction) {
			checkedProduction = true;
			if(inProduction()) {
				enabled = false;
				return;
			}
			
			/* second check: can we load this class: 'LocalServiceTestHelper' ? */
			try {
				Class.forName("com.google.appengine.tools.development.testing.LocalServiceTestHelper");
			} catch(ClassNotFoundException e) {
				/* ah, we are in production */
				enabled = false;
				return;
			}
		}
		GaeTestFixer_LocalPart.initialiseHelperAndAttachToCurrentThread();
	}
	
	/**
	 * @param datastore
	 * @return true if the given datastore contains a defined marker
	 *         {@link Entity}
	 */
	public static boolean containsMarker(DatastoreService datastore) {
		try {
			Key key = KeyFactory.createKey("test", "marker");
			@SuppressWarnings("unused")
			Entity entity = datastore.get(key);
			return true;
		} catch(EntityNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Add marker Entity to GAE dataStore
	 * 
	 * @param datastore
	 */
	public static void setMarkerEntity(DatastoreService datastore) {
		Key key = KeyFactory.createKey("test", "marker");
		Entity marker = new Entity(key);
		datastore.put(marker);
		assert GaeTestfixer.containsMarker(datastore);
	}
	
	public static synchronized void tearDown() {
		GaeTestFixer_LocalPart.tearDown();
	}
	
}
