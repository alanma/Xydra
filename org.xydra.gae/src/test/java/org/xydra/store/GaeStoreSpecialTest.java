package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeStoreSpecialTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private GaePersistence pers;
	private XID actorId;
	private XAddress modelAddress1;
	private XID repoID;
	private XID modelId1;
	private XAddress repoAddr;
	
	public void doStuff() {
		// creating some models
		
		Set<XID> modelids = this.pers.getModelIds();
		assert !modelids.contains(this.modelId1);
		
		XID objectId1 = XX.toId("TestObject1");
		XID objectId2 = XX.toId("TestObject2");
		XID objectId3 = XX.toId("TestObject3");
		
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

		XCommand modelCommand1 = X.getCommandFactory().createAddModelCommand(this.repoID,
		        this.modelId1, true);
		
		XCommand objectCommand1 = X.getCommandFactory().createAddObjectCommand(this.repoID,
		        this.modelId1, objectId1, true);
		XCommand objectCommand2 = X.getCommandFactory().createAddObjectCommand(this.repoID,
		        this.modelId1, objectId2, true);
		XCommand objectCommand3 = X.getCommandFactory().createAddObjectCommand(this.repoID,
		        this.modelId1, objectId3, true);
		
		XCommand[] commands = { modelCommand1, objectCommand1, objectCommand2, objectCommand3 };
		
		for(XCommand command : commands) {
			RevisionState pair = this.pers.executeCommand(this.actorId, command);
			long result = pair.revision();
			assert result >= 0;
			if(result == XCommand.FAILED) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp: command failed!");
			}
			// TODO is this check necessary?
			// TODO this fails with the GaeStore which cannot be reset
			if(result == XCommand.NOCHANGE) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp: command did not change anything! "
				                + commands);
			}
		}
		
		this.modelAddress1 = XX.toAddress(this.repoID, this.modelId1, null, null);
		
	}
	
	public void deleteModel1() {
		XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(this.repoAddr,
		        XCommand.FORCED, this.modelId1);
		RevisionState l = this.pers.executeCommand(this.actorId, removeCommand);
		assert l.revision() >= 0;
		assert this.pers.getModelIds().size() == 0;
	}
	
	@Test
	public void doTest() {
		this.modelId1 = XX.toId("TestModel1");
		this.actorId = XX.toId("actor");
		this.pers = new GaePersistence(XX.toId("special"));
		this.repoID = this.pers.getRepositoryId();
		this.repoAddr = XX.toAddress(this.pers.getRepositoryId(), null, null, null);
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		doStuff();
		deleteModel1();
		doStuff();
		deleteModel1();
		doStuff();
		RevisionState rev1 = this.pers.getModelRevision(this.modelAddress1);
		assertEquals(13, rev1.revision());
		RevisionState rev2 = this.pers.getModelRevision(this.modelAddress1);
		assertEquals(rev1.revision(), rev2.revision());
	}
	
}
