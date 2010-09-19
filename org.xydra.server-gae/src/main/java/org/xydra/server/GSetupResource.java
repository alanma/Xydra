package org.xydra.server;

import javax.servlet.http.HttpServletResponse;

import org.xydra.core.test.DemoModelUtil;
import org.xydra.restless.Restless;
import org.xydra.server.rest.XydraRestServer;


public class GSetupResource {
	
	public void restless(Restless restless, String prefix) {
		
		restless.addGet(prefix + "/setup", this, "init");
		
	}
	
	public void init(Restless restless, HttpServletResponse res) {
		
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		
		// this persists the phonebook also in underlying GAE
		DemoModelUtil.addPhonebookModel(server.getRepository());
		
		// TODO IMRPOVE return very short XHTML document that tells user what
		// happened
		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, "done");
	}
	
}
