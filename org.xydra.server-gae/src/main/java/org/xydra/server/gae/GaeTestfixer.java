package org.xydra.server.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Delegate;
import com.google.apphosting.api.ApiProxy.Environment;


/**
 * A helper class to turn the simulated local AppEngine environment into a
 * singleton.
 * 
 * If we run in a JUnit test behind Jersey, which uses reflection, we are in a
 * different thread and need to take additional measure to tell this thread
 * about the GAE-test-setup
 * 
 * TODO Remove class if AppEngine issue is ever fixed by Google
 * http://code.google
 * .com/p/googleappengine/issues/detail?id=2201&q=junit&colspec
 * =ID%20Type%20Status%20Priority%20Stars%20Owner%20Summary%20Log%20Component
 * 
 * @author voelkel
 */
public class GaeTestfixer {
	
	private static LocalServiceTestHelper helper_;
	private static Environment env;
	private static Delegate<?> delegate;
	private static boolean enabled = false;
	
	public static void enable() {
		enabled = true;
	}
	
	public static void initialiseHelperAndAttachToCurrentThread() {
		if(!enabled) {
			return;
		}
		
		if(helper_ == null) {
			// create new environment
			helper_ = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
			setUp();
			
			// remember it
			env = ApiProxy.getCurrentEnvironment();
			delegate = ApiProxy.getDelegate();
			
			// add marker Entity to GAE dataStore
			Key key = KeyFactory.createKey("test", "marker");
			Entity marker = new Entity(key);
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(marker);
			assert containsMarker(datastore);
		} else if(ApiProxy.getCurrentEnvironment() == null) {
			
			// make sure to setup, if necessary
			if(!setup) {
				setUp();
			}
			
			// if(SystemProperty.Environment.environment.get() == null) {
			// throw new IllegalStateException(
			// "You seem to run locally, but have not enabled the testfixer.");
			// }
			
			// // we must be on Dev-Server
			// if(SystemProperty.Environment.environment.value() !=
			// SystemProperty.Environment.Value.Development) {
			// throw new AssertionError(
			// "We are on production server, but current environment is null.");
			// }
			
			// a second thread needs the test stubs - attach to thread
			ApiProxy.setEnvironmentForCurrentThread(env);
			ApiProxy.setDelegate(delegate);
		}
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
	
	private static boolean setup = false;
	
	public static synchronized void setUp() {
		helper_.setUp();
		setup = true;
	}
	
	public static synchronized void tearDown() {
		assert setup;
		helper_.tearDown();
		setup = false;
	}
	
}
