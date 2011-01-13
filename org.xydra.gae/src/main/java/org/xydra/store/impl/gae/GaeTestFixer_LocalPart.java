package org.xydra.store.impl.gae;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Delegate;
import com.google.apphosting.api.ApiProxy.Environment;


/**
 * The part of {@link GaeTestfixer} that requires stub classes only available
 * for local testing.
 * 
 * @author xamde
 * 
 */
public class GaeTestFixer_LocalPart {
	
	private static Environment env;
	private static Delegate<?> delegate;
	private static LocalServiceTestHelper helper_;
	private static boolean setup = false;
	
	public static void initialiseHelperAndAttachToCurrentThread() {
		if(helper_ == null) {
			// create new environment
			helper_ = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
			setUp();
			
			// remember it
			env = ApiProxy.getCurrentEnvironment();
			delegate = ApiProxy.getDelegate();
			
			GaeTestfixer.setMarkerEntity(DatastoreServiceFactory.getDatastoreService());
			
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
