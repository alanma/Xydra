package org.xydra.server.rest.demo;

import javax.servlet.http.HttpServletResponse;

import org.xydra.core.test.DemoModelUtil;
import org.xydra.restless.Restless;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


/**
 * Add a demo data to the repository
 * 
 * @author voelkel
 */
public class AddDemoDataResource {
	
	public static void restless(Restless restless, String prefix) {
		restless.addGet(prefix + "/demodata", AddDemoDataResource.class, "init");
	}
	
	public void init(Restless restless, HttpServletResponse res) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		
		// this persists the phonebook
		DemoModelUtil.addPhonebookModel(server.getRepository());
		
		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, "Added phonebook model.");
	}
	
}
