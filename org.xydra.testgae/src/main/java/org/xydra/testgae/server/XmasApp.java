package org.xydra.testgae.server;

import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.server.rest.XydraRestServer;
import org.xydra.server.rest.XydraStoreResource;
import org.xydra.testgae.server.rest.AdminDashboardResource;
import org.xydra.testgae.server.rest.ConsistencyTestResource;
import org.xydra.testgae.server.rest.xmas.XmasResource;


/**
 * This is the main Restless app for the TestGae project.
 * 
 * @author xamde
 */
public class XmasApp {
	
	public void restless(Restless r, String path) {
		/** a trivial exception handler */
		r.addExceptionHandler(new RestlessExceptionHandler() {
			
			@Override
			public boolean handleException(Throwable t, IRestlessContext context) {
				System.err.println("Restless error");
				throw new RuntimeException("" + context.getRequest(), t);
			}
			
		});
		
		AdminDashboardResource.restless(r, path);
		
		// FIXME DataloggerResource.restless(r, "");
		
		// a nice sample app
		XmasResource.restless(r, path);
		
		// very cool for debugging consistency/race conditions
		ConsistencyTestResource.restless(r, path);
		
		// additionally run xydra REST endpoint
		XydraRestServer.initializeServer(r);
		XydraStoreResource.restless(r, "/xydra/store/v1");
	}
	
}
