package org.xydra.store.impl.gae;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

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
	
	private static final Logger log = LoggerFactory.getLogger(GaeTestfixer.class);
	
	private static boolean enabled = false;
	/** checking for production env only once makes this run faster */
	private static boolean checkedProduction = false;
	
	public static void enable() {
		log.debug("Enabling test fixer.");
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
			log.trace("Testing if we are on the real GAE in production...");
			checkedProduction = true;
			if(inProduction()) {
				log.debug("Testfixer: Running on AppEngine in production: Auto-disabled test fixer.");
				enabled = false;
				return;
			} else {
				log.debug("Testfixer: Running locally");
			}
			
			/* second check: can we load this class: 'LocalServiceTestHelper' ? */
			try {
				Class.forName("com.google.appengine.tools.development.testing.LocalServiceTestHelper");
				log.trace("We can load the test classes.");
			} catch(ClassNotFoundException e) {
				/* ah, we are in production */
				log.warn(
				        "We are in fact in production (or a jar is missing): Auto-disabled test fixer.",
				        e);
				enabled = false;
				return;
			} catch(NoClassDefFoundError e) {
				/* ah, we are in production */
				log.warn(
				        "We are in fact in production (or a jar is missing): Auto-disabled test fixer.",
				        e);
				enabled = false;
				return;
			}
		}
		
		GaeTestFixer_LocalPart.initialiseHelperAndAttachToCurrentThread();
	}
	
	/**
	 * @param datastore The datastore to check for a marker.
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
	 * @param datastore The datastore to set a marker in.
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
