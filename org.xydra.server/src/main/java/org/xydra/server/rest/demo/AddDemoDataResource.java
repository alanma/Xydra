package org.xydra.server.rest.demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.test.DemoModelUtil;
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
