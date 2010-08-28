package org.xydra.server;

import javax.servlet.http.HttpServletResponse;

import org.xydra.core.test.DemoModelUtil;
import org.xydra.restless.Restless;


public class GSetupResource {
	
	public void restless(String prefix) {
		
		Restless.addGet(prefix + "/setup", this, "init");
		
	}
	
	public void init(HttpServletResponse res) {
		
		// this persists the phonebook also in underlying GAE
		DemoModelUtil.addPhonebookModel(RepositoryManager.getRepository());
		
		// TODO IMRPOVE return very short XHTML document that tells user what
		// happened
		XydraServer.textResponse(res, HttpServletResponse.SC_OK, "done");
	}
	
}
