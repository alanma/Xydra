package org.xydra.testgae;

import java.io.File;
import java.net.URI;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Jetty;
import org.xydra.store.impl.gae.GaeTestfixer;


/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from src/main/webapp. This class is not
 * required to run the webapp.
 * 
 * @author voelkel
 * 
 */
public class RunXmasJetty {
	
	private static final Logger log = LoggerFactory.getLogger(RunXmasJetty.class);
	
	public static void main(String[] args) throws Exception {
		
		/*
		 * Enable tests with GAE (especially mail)
		 */
		GaeTestfixer.enable();
		/* Make this thread GAE-test-ready */
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		CopyGwt.copyGwt();
		
		// start jetty
		Jetty jetty = new Jetty(8787);
		
		URI uri = jetty.startServer("", new File("src/main/webapp"));
		
		log.info("Started embedded Jetty server. User interface is at " + uri.toString());
	}
}
