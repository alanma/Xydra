package org.xydra.server.rest.demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.restless.Restless;
import org.xydra.server.rest.XydraRestServer;
import org.xydra.store.BatchedResult;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraStore;

/**
 * Add a demo data (phonebook) to the repository
 *
 * @author xamde
 */
public class AddDemoDataResource {

	public static void restless(final Restless restless, final String prefix) {
		restless.addGet(prefix + "/demodata", AddDemoDataResource.class, "init");
	}

	public void init(final Restless restless, final HttpServletRequest req, final HttpServletResponse res) {

		final XydraStore store = XydraRestServer.getStore(restless);

		// TODO use a real user
		final XId actorId = Base.toId("admin");
		final String passwordHash = "secret";

		final WaitingCallback<XId> repoAddr = new WaitingCallback<XId>();
		store.getRepositoryId(actorId, passwordHash, repoAddr);

		final WaitingCallback<BatchedResult<Long>[]> result = new WaitingCallback<BatchedResult<Long>[]>();

		// TODO move command into transaction
		final XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
				Base.toAddress(repoAddr.getResult(), null, null, null), XCommand.SAFE_STATE_BOUND,
				DemoModelUtil.PHONEBOOK_ID);
		store.executeCommands(actorId, passwordHash, new XCommand[] { createCommand }, result);

		result.getException(); // wait for command to execute

		final XAddress modelAddr = createCommand.getChangedEntity();
		final XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb, true);
		store.executeCommands(actorId, passwordHash, new XCommand[] { tb.build() }, null);

		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, "Added phonebook model.");
	}
}
