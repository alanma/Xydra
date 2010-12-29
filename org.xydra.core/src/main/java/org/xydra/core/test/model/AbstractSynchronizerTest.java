package org.xydra.core.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.core.XCompareUtils;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XType;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.test.TestLogger;
import org.xydra.store.BatchedResult;
import org.xydra.store.XydraStore;
import org.xydra.store.test.SynchronousTestCallback;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl}.
 * 
 * @author dscharrer
 */
abstract public class AbstractSynchronizerTest {
	
	{
		TestLogger.init();
	}
	
	protected static XID actorId;
	protected static String passwordHash;
	
	protected static XAddress repoAddr;
	protected static XydraStore store;
	
	private XModel model;
	private XSynchronizer sync;
	
	@Before
	public void setUp() {
		
		assertNotNull(actorId);
		assertNotNull(passwordHash);
		assertNotNull(repoAddr);
		assertEquals(XType.XREPOSITORY, repoAddr.getAddressedType());
		assertNotNull(store);
		
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(repoAddr,
		        XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		executeCommand(createCommand);
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		XTransaction trans = tb.build();
		// Apply events individually so there is something in the change log to
		// test
		for(XAtomicCommand ac : trans) {
			executeCommand(ac);
		}
		
		this.model = loadModel(DemoModelUtil.PHONEBOOK_ID);
		this.sync = new XSynchronizer(this.model, store);
	}
	
	@After
	public void tearDown() {
		removeModel(DemoModelUtil.PHONEBOOK_ID);
	}
	
	private void removeModel(XID modelId) {
		executeCommand(MemoryRepositoryCommand.createRemoveCommand(repoAddr, XCommand.FORCED,
		        modelId));
	}
	
	static class TestCallback implements XLocalChangeCallback {
		
		boolean applied = false;
		boolean failed = false;
		
		synchronized public void failed() {
			
			assertFalse("double fail detected", this.failed);
			assertFalse("fail after apply detected", this.applied);
			
			this.failed = true;
			notifyAll();
		}
		
		synchronized public void applied(long revision) {
			
			assertFalse("double apply detected", this.applied);
			assertFalse("apply after fail detected", this.failed);
			
			this.applied = true;
			notifyAll();
		}
		
		synchronized public void waitForSuccess() {
			
			long time = System.currentTimeMillis();
			while(!this.applied) {
				
				if(this.failed) {
					fail("a command failed to apply remotely");
				}
				
				assertFalse("timeout waiting for command to apply", System.currentTimeMillis()
				        - time > 1000);
				try {
					wait(1100);
				} catch(InterruptedException e) {
					// ignore
				}
			}
		}
		
	}
	
	@Test
	public void testSendLocalChanges() {
		
		final TestCallback c1 = new TestCallback();
		final TestCallback c2 = new TestCallback();
		
		// Create a command manually.
		XCommand command = MemoryModelCommand.createAddCommand(this.model.getAddress(), false, XX
		        .toId("Frank"));
		
		// Apply the command locally.
		this.model.executeCommand(command, c1);
		
		// Now synchronize with the server.
		this.sync.synchronize();
		
		// command may not be applied remotely yet!
		
		// We don't have to create all commands manually but can use a
		// ChangedModel.
		ChangedModel changedModel = new ChangedModel(this.model);
		
		// Make modifications to the changed model.
		changedModel.getObject(DemoModelUtil.JOHN_ID).createField(XX.toId("newField"));
		changedModel.removeObject(DemoModelUtil.PETER_ID);
		
		// Create the command(s) describing the changes made to the
		// ChangedModel.
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		tb.applyChanges(changedModel);
		XCommand autoCommand = tb.buildCommand();
		
		// Now apply the command locally. It should be automatically
		// sent to the server.
		this.model.executeCommand(autoCommand, c2);
		
		this.sync.synchronize();
		
		// both commands may still not be applied remotely
		
		c1.waitForSuccess();
		c2.waitForSuccess();
		assertTrue(XCompareUtils.equalState(this.model,
		        loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID)));
		
	}
	
	@Test
	public void testLoadRemoteChanges() {
		// TODO implement
	}
	
	@Test
	public void testMergeChanges() {
		// TODO implement
	}
	
	@Test
	public void testCreateModel() {
		// TODO implement
	}
	
	@Test
	public void testRecreateModel() {
		// TODO implement
	}
	
	@Test
	public void testRemoveModel() {
		// TODO implement
	}
	
	@Test
	public void testLoadRemoteChangesRemovedModel() {
		// TODO implement
	}
	
	private XModel loadModel(XID modelId) {
		
		XBaseModel modelSnapshot = loadModelSnapshot(modelId);
		assertNotNull(modelSnapshot);
		
		// TODO there should be a better way to get a proper XModel from an
		// XBaseModel
		XModel model = XCopyUtils.copyModel(actorId, passwordHash, modelSnapshot);
		return model;
	}
	
	private XBaseModel loadModelSnapshot(XID modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> tc;
		tc = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		store.getModelSnapshots(actorId, passwordHash, new XAddress[] { modelAddr }, tc);
		
		return waitCallbackSuccess(tc);
	}
	
	private void executeCommand(XCommand command) {
		SynchronousTestCallback<BatchedResult<Long>[]> tc;
		tc = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		store.executeCommands(actorId, passwordHash, new XCommand[] { command }, tc);
		
		long res = waitCallbackSuccess(tc);
		
		assertTrue(res != XCommand.FAILED);
	}
	
	private <T> T waitCallbackSuccess(SynchronousTestCallback<BatchedResult<T>[]> tc) {
		
		assertEquals(SynchronousTestCallback.SUCCESS, tc.waitOnCallback(0));
		
		assertNull(tc.getException());
		BatchedResult<T>[] results = tc.getEffect();
		assertNotNull(results);
		assertEquals(1, results.length);
		BatchedResult<T> result = results[0];
		assertNotNull(result);
		assertNull(result.getException());
		return result.getResult();
	}
	
}
