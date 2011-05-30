package org.xydra.restless.example;

import org.xydra.restless.IRestlessContext;
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
	
	/**
	 * @param r provides some context
	 * @param path is either the empty string or a path starting with a slash,
	 *            but not ending with a slash.
	 */
	public void restless(Restless r, String path) {
		ExampleResource.restless(r);
		
		/** a rather useless exception handler */
		r.addExceptionHandler(new RestlessExceptionHandler() {
			
			public boolean handleException(Throwable t, IRestlessContext context) {
				System.err.println("Restless error");
				throw new RuntimeException("" + context.getRequest(), t);
			}
		});
		
	}
	
}
