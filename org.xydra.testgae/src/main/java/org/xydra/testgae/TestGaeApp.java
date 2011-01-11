package org.xydra.testgae;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;


public class TestGaeApp {
	
	public void restless(Restless r, String path) {
		/** a rather useless exception handler */
		r.addExceptionHandler(new RestlessExceptionHandler() {
			
			public boolean handleException(Throwable t, HttpServletRequest req,
			        HttpServletResponse res) {
				System.err.println("Restless error");
				throw new RuntimeException("" + req, t);
			}
		});
		
		EchoResource.restless(r);
		TestResource.restless(r);
	}
	
}
