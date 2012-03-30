package org.xydra.gaemyadmin;

import com.google.appengine.api.datastore.dev.LocalDatastoreService;
import com.google.appengine.tools.development.ApiProxyLocal;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig.SizeUnit;
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
			LocalDatastoreServiceTestConfig localDatastoreServiceTestConfig = new LocalDatastoreServiceTestConfig()
			        .setNoStorage(true).setBackingStoreLocation(null);
			LocalMemcacheServiceTestConfig localMemcacheServiceTestConfig = new LocalMemcacheServiceTestConfig()
			        .setMaxSize(10, SizeUnit.MB);
			helper_ = new LocalServiceTestHelper(localDatastoreServiceTestConfig,
			        localMemcacheServiceTestConfig);
			
			setUp();
			
			// remember it
			env = ApiProxy.getCurrentEnvironment();
			delegate = ApiProxy.getDelegate();
			
			// hopeless attempt to get a clean datastore in JUnit tests
			ApiProxyLocal proxy = (ApiProxyLocal)delegate;
			proxy.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY, Boolean.TRUE.toString());
			
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
		
		// ApiProxyLocal proxy = (ApiProxyLocal)ApiProxy.getDelegate();
		// LocalDatastoreService datastoreService = (LocalDatastoreService)proxy
		// .getService("datastore_v3");
		// datastoreService.clearProfiles();
		
		helper_.tearDown();
		setup = false;
	}
	
}
