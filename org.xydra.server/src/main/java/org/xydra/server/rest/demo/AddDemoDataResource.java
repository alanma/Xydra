package org.xydra.server.rest.demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.restless.Restless;
import org.xydra.server.IXydraSession;
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
	
	public void init(Restless restless, HttpServletRequest req, HttpServletResponse res) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		// TODO move command into transaction
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(session
		        .getRepositoryAddress(), XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		session.executeCommand(createCommand);
		
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		session.executeCommand(tb.build());
		
		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, "Added phonebook model.");
	}
	
}
