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
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XType;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.test.TestLogger;
import org.xydra.core.value.XV;
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
	
	private static final XID NEWMODEL_ID = XX.toId("newmodel");
	
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
	
	@Test
	public void testSendLocalChanges() {
		
		TestLocalChangeCallback c1 = new TestLocalChangeCallback();
		TestLocalChangeCallback c2 = new TestLocalChangeCallback();
		
		TestSynchronizationCallback sc1 = new TestSynchronizationCallback();
		TestSynchronizationCallback sc2 = new TestSynchronizationCallback();
		
		// Create a command manually.
		XCommand command = MemoryModelCommand.createAddCommand(this.model.getAddress(), false, XX
		        .toId("Frank"));
		
		// Apply the command locally.
		this.model.executeCommand(command, c1);
		
		// Now synchronize with the server.
		this.sync.synchronize(sc1);
		
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
		long finalRev = this.model.getRevisionNumber();
		
		this.sync.synchronize(sc2);
		
		// both commands may still not be applied remotely
		
		assertTrue(c1.waitForResult() >= 0);
		assertTrue(c2.waitForResult() >= 0);
		assertTrue(XCompareUtils.equalState(this.model,
		        loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID)));
		checkSyncCallback(sc1);
		checkSyncCallback(sc2);
		assertEquals(finalRev, this.model.getRevisionNumber());
		assertEquals(finalRev, this.model.getSynchronizedRevision());
		
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
	public void testCreateRemoveModel() {
		
		try {
			
			// TODO test listeners
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			XRepository repo = new MemoryRepository(actorId, passwordHash, repoAddr.getRepository());
			
			XModel model = repo.createModel(NEWMODEL_ID);
			XSynchronizer sync = new XSynchronizer(model, store);
			XObject object = model.createObject(XX.toId("bob"));
			XField field = object.createField(XX.toId("cookies"));
			field.setValue(XV.toValue("yummy"));
			long modelRev = model.getRevisionNumber();
			
			TestSynchronizationCallback sc1 = new TestSynchronizationCallback();
			sync.synchronize(sc1);
			checkSyncCallback(sc1);
			
			XBaseModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			assertTrue(XCompareUtils.equalState(model, remoteModel));
			assertEquals(modelRev, model.getRevisionNumber());
			assertEquals(modelRev, model.getSynchronizedRevision());
			
			// check that the local model still works
			model.createObject(XX.toId("jane"));
			
			repo.removeModel(NEWMODEL_ID);
			modelRev = model.getRevisionNumber();
			
			TestSynchronizationCallback sc2 = new TestSynchronizationCallback();
			sync.synchronize(sc2);
			checkSyncCallback(sc2);
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			assertFalse(repo.hasModel(NEWMODEL_ID));
			assertEquals(modelRev, model.getRevisionNumber());
			assertEquals(modelRev, model.getSynchronizedRevision());
			
			// check that local model is removed
			try {
				model.createObject(XX.toId("jane"));
				fail();
			} catch(IllegalStateException ise) {
				// worked
			}
			
			model = repo.createModel(NEWMODEL_ID);
			model.createObject(XX.toId("john"));
			modelRev = model.getRevisionNumber();
			
			TestSynchronizationCallback sc3 = new TestSynchronizationCallback();
			sync = new XSynchronizer(model, store);
			sync.synchronize(sc3);
			checkSyncCallback(sc3);
			
			remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			assertTrue(XCompareUtils.equalState(model, remoteModel));
			assertEquals(model.getRevisionNumber(), model.getSynchronizedRevision());
			
			// check that the local model still works
			model.createObject(XX.toId("jane"));
			
		} finally {
			removeModel(NEWMODEL_ID);
		}
		
	}
	
	@Test
	public void testLoadRemoteChangesRemovedModel() {
		// TODO implement
	}
	
	@Test
	public void testLoadRemoteChangesMissingRevisions() {
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
	
	void checkSyncCallback(TestSynchronizationCallback sc) {
		if(sc.getRequestError() != null) {
			throw new RuntimeException(sc.getRequestError());
		}
		if(sc.getCommandError() != null) {
			throw new RuntimeException(sc.getCommandError());
		}
		if(sc.getEventsError() != null) {
			throw new RuntimeException(sc.getEventsError());
		}
		assertTrue(sc.isSuccess());
	}
	
}
