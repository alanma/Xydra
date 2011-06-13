package org.xydra.testgae;

import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.testgae.xmas.rest.XmasResource;


/**
 * This is not a test -- this is the main Restless app for the TestGAE project.
 * 
 * @author xamde
 * 
 */
public class TestGaeApp {
	
	public void restless(Restless r, String path) {
		/** a rather useless exception handler */
		r.addExceptionHandler(new RestlessExceptionHandler() {
			
			public boolean handleException(Throwable t, IRestlessContext context) {
				System.err.println("Restless error");
				throw new RuntimeException("" + context.getRequest(), t);
			}
			
		});
		
		AssertResource.restless(r);
		EchoResource.restless(r);
		TestResource.restless(r);
		BenchmarkResource.restless(r);
		XmasResource.restless(r, path);
	}
	
}
