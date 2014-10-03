package org.xydra.server.rest.demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.restless.Restless;
import org.xydra.server.rest.XydraRestServer;
import org.xydra.store.BatchedResult;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraStore;

/**
 * Add a demo data (phonebook) to the repository
 * 
 * @author voelkel
 */
public class AddDemoDataResource {

	public static void restless(Restless restless, String prefix) {
		restless.addGet(prefix + "/demodata", AddDemoDataResource.class, "init");
	}

	public void init(Restless restless, HttpServletRequest req, HttpServletResponse res) {

		XydraStore store = XydraRestServer.getStore(restless);

		// TODO use a real user
		XId actorId = XX.toId("admin");
		String passwordHash = "secret";

		WaitingCallback<XId> repoAddr = new WaitingCallback<XId>();
		store.getRepositoryId(actorId, passwordHash, repoAddr);

		WaitingCallback<BatchedResult<Long>[]> result = new WaitingCallback<BatchedResult<Long>[]>();

		// TODO move command into transaction
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
				XX.toAddress(repoAddr.getResult(), null, null, null), XCommand.SAFE_STATE_BOUND,
				DemoModelUtil.PHONEBOOK_ID);
		store.executeCommands(actorId, passwordHash, new XCommand[] { createCommand }, result);

		result.getException(); // wait for command to execute

		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb, true);
		store.executeCommands(actorId, passwordHash, new XCommand[] { tb.build() }, null);

		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, "Added phonebook model.");
	}
}
