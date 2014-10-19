package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.MemorySyncLog;

public class SyncLogTest {

	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}

	private XId actorId = XX.toId("EventDeltaTest");
	private String password = null; // TODO auth: where to get this?

	XId repo = XX.toId("remoteRepo");

	@Test
	public void testBasicFunctionality() {

		XAddress modelAddress = XX.resolveModel(this.repo, DemoModelUtil.PHONEBOOK_ID);
		ISyncLog syncLog = new MemorySyncLog(modelAddress);

		XRepository repo = new MemoryRepository(this.actorId, this.password, this.repo);
		DemoModelUtil.addPhonebookModel(repo);

		syncLog.setSynchronizedRevision(DemoLocalChangesAndServerEvents.SYNC_REVISION);

		XModel localModel = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		ChangedModel changedModel = new ChangedModel(localModel);
		DemoLocalChangesAndServerEvents.addLocalChangesToModel(changedModel);
		XTransactionBuilder builder = new XTransactionBuilder(modelAddress);
		builder.applyChanges(changedModel);

		XTransaction transaction = builder.build();
		HashMap<Long, XCommand> commandMap = new HashMap<Long, XCommand>();
		for (XAtomicCommand xAtomicCommand : transaction) {

			localModel.executeCommand(xAtomicCommand);
			commandMap.put(xAtomicCommand.getRevisionNumber(), xAtomicCommand);
		}

		Iterator<XEvent> modelChangeEvents = localModel.getChangeLog().getEventsSince(
				DemoLocalChangesAndServerEvents.SYNC_REVISION + 1);

		while (modelChangeEvents.hasNext()) {
			XEvent xEvent = modelChangeEvents.next();

			XCommand command = commandMap.get(xEvent.getRevisionNumber());
			syncLog.appendSyncLogEntry(command, xEvent);
		}

	}

}
