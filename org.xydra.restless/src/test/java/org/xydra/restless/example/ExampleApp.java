package org.xydra.restless.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.restless.RunTestJetty;


/**
 * 1) The 'app' must have a parameter-less constructor.
 * 
 * 2) Restless will call 'restless(Restless, String prefix)' on the instance.
 * The method MAY be a static method.
 * 
 * Run {@link RunTestJetty} as a Java application to see Restless in action and
 * go to 'http://localhost:8080/a/foo?name=john'
 * 
 * @author voelkel
 * 
 */
public class ExampleApp {
	
	public void restless(Restless r, String path) {
		ExampleResource.restless(r);
		
		/** a rather useless exception handler */
		r.addExceptionHandler(new RestlessExceptionHandler() {
			
			public boolean handleException(Throwable t, HttpServletRequest req,
			        HttpServletResponse res) {
				System.err.println("Restless error");
				throw new RuntimeException("" + req, t);
			}
		});
		
	}
	
}
