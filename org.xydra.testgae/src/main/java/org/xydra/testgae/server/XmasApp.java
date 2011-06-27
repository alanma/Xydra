package org.xydra.testgae.server;

import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.testgae.server.rest.AdminDashboardResource;
import org.xydra.testgae.server.rest.GaeInfoResource;
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
			
			public boolean handleException(Throwable t, IRestlessContext context) {
				System.err.println("Restless error");
				throw new RuntimeException("" + context.getRequest(), t);
			}
			
		});
		
		GaeInfoResource.restless(r);
		
		AdminDashboardResource.restless(r);
		
		XmasResource.restless(r, path);
	}
	
}