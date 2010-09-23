package org.xydra.server.impl.gae;

import org.xydra.restless.Restless;


/**
 * Make sure to set 'app' in web.xml to {@link GaeXydraServer}, this app.
 * 
 * The order of apps determines the initialisation order.
 * 
 * @author voelkel
 */
public class GaeXydraApp {
	
	public void restless(Restless restless, String prefix) {
		// TODO are these needed?
		new GSetupResource().restless(restless, prefix);
		new LogTestResource().restless(restless, prefix);
	}
	
}
