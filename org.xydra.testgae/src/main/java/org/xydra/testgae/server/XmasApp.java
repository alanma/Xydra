package org.xydra.testgae.server;

import org.xydra.gae.datalogger.DataloggerResource;
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
		
		DataloggerResource.restless(r, "");
		
		XmasResource.restless(r, path);
		
		ConsistencyTestResource.restless(r, path);
		
		// additionally run xydra rest endpoint
		
		XydraRestServer.initializeServer(r);
		
		XydraStoreResource.restless(r, "/xydra/store/v1");
	}
	
}
