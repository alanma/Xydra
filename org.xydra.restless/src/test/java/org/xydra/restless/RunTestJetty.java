package org.xydra.restless;

import java.io.File;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from source code. This class is not
 * required to run the webapp.
 * 
 * <p>
 * If only static files have been modified, no call is neccesary as this Jetty
 * is configured to load the directly from src/main/webapp.
 * 
 * @author voelkel
 * 
 */
public class RunTestJetty {
	
	private static final Logger log = LoggerFactory.getLogger(RunTestJetty.class);
	
	public static void main(String[] args) throws Exception {
		
		Restless.addExceptionHandler(new RestlessExceptionHandler() {
			
			public boolean handleException(Throwable t, HttpServletRequest req,
			        HttpServletResponse res) {
				System.err.println("Restless error");
				throw new RuntimeException("" + req, t);
			}
		});
		
		Jetty jetty = new Jetty();
		
		URI uri = jetty.startServer("", new File("src/test/resources"));
		log.info("Started embedded Jetty server. User interface is at " + uri.toString());
		
	}
}
