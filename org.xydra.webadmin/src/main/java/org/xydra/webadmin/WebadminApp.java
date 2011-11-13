package org.xydra.webadmin;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.core.util.Clock;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;


/**
 * Run this either by configuring your Restless servlet to run this
 * {@link WebadminApp} or embed this admin tool in your own app by calling
 * {@link WebadminResource#restless(Restless)} from your own restless app.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class WebadminApp {
	
	public static final Logger log = LoggerFactory.getLogger(WebadminApp.class);
	
	public static void restless(Restless restless, String prefix) {
		Clock c = new Clock().start();
		WebadminResource.restless(restless);
		c.stop("WebadminResource.restless");
		log.info("Loaded WebadminApp " + c.getStats());
	}
	
}
