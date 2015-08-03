package org.xydra.store;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.LoggerTestHelper;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.store.impl.gae.GaePersistence;

public class GaeStoreSpecialTest {

	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}

	private GaePersistence pers;
	private XId actorId;
	private XAddress modelAddress1;
	private XId repoID;
	private XId modelId1;
	private XAddress repoAddr;

	public void doStuff() {
		// creating some models
		final XId objectId1 = Base.toId("TestObject1");
		final XId objectId2 = Base.toId("TestObject2");
		final XId objectId3 = Base.toId("TestObject3");

		/*
		 * FIXME In a secure store you need to give the correctUser the rights
		 * to access these models and objects -
		 */
		/*
		 * Comment by me: this should not be done by this abstract test, but
		 * rather by the implementation. As stated in the documentation of the
		 * "getCorrectUser" method, the test assumes that the user returned by
		 * this method is allowed to execute the following commands ~Bjoern
		 */

		final XCommand modelCommand1 = BaseRuntime.getCommandFactory().createAddModelCommand(this.repoID,
				this.modelId1, true);

		final XCommand objectCommand1 = BaseRuntime.getCommandFactory().createAddObjectCommand(
				Base.resolveModel(this.repoID, this.modelId1), objectId1, true);
		final XCommand objectCommand2 = BaseRuntime.getCommandFactory().createAddObjectCommand(
				Base.resolveModel(this.repoID, this.modelId1), objectId2, true);
		final XCommand objectCommand3 = BaseRuntime.getCommandFactory().createAddObjectCommand(
				Base.resolveModel(this.repoID, this.modelId1), objectId3, true);

		final XCommand[] commands = { modelCommand1, objectCommand1, objectCommand2, objectCommand3 };

		for (final XCommand command : commands) {
			final long result = this.pers.executeCommand(this.actorId, command);
			assert result >= 0 : result;
			if (result == XCommand.FAILED) {
				throw new RuntimeException(
						"ExecuteCommands did not work properly in setUp: command failed!");
			}
			// TODO is this check necessary?
			// TODO this fails with the GaeStore which cannot be reset
			if (result == XCommand.NOCHANGE) {
				throw new RuntimeException(
						"ExecuteCommands did not work properly in setUp: command did not change anything! "
								+ commands);
			}
		}

		this.modelAddress1 = Base.toAddress(this.repoID, this.modelId1, null, null);

	}

	public void deleteModel1() {
		final XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(this.repoAddr,
				XCommand.FORCED, this.modelId1);
		final long l = this.pers.executeCommand(this.actorId, removeCommand);
		assert l >= 0;
		assert !this.pers.getModelRevision(new GetWithAddressRequest(this.modelAddress1))
				.modelExists();
	}

	@Test
	public void doTest() {
		this.modelId1 = Base.toId("TestModel1");
		this.actorId = Base.toId("actor");
		this.pers = new GaePersistence(Base.toId("special"));
		this.repoID = this.pers.getRepositoryId();
		this.repoAddr = Base.toAddress(this.pers.getRepositoryId(), null, null, null);

		doStuff();
		deleteModel1();
		doStuff();
		deleteModel1();
		doStuff();
		final ModelRevision rev1 = this.pers.getModelRevision(new GetWithAddressRequest(
				this.modelAddress1));
		assertEquals(13, rev1.revision());
		final ModelRevision rev2 = this.pers.getModelRevision(new GetWithAddressRequest(
				this.modelAddress1));
		assertEquals(rev1.revision(), rev2.revision());
	}

}
