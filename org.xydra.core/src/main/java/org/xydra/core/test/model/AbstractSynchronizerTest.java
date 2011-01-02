package org.xydra.core.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.core.XCompareUtils;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.sync.XSynchronizer;
import org.xydra.core.test.ChangeRecorder;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.test.HasChanged;
import org.xydra.core.test.TestLogger;
import org.xydra.core.value.XV;
import org.xydra.core.value.XValue;
import org.xydra.store.BatchedResult;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;
import org.xydra.store.test.SynchronousTestCallback;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * an arbitrary {@link XydraStore}.
 * 
 * Subclasses should set the model state backend via
 * {@link XSPI#setStateStore(org.xydra.core.model.state.XStateStore)}, set
 * {@link #store} to a concrete implementation, and set the {@link #actorId}/
 * {@link #passwordHash} pair to one that has sufficient access (to
 * create/modify/delete/read the {@link DemoModelUtil#PHONEBOOK_ID} and
 * {@link #NEWMODEL_ID} models).
 * 
 * @author dscharrer
 */
abstract public class AbstractSynchronizerTest {
	
	{
		TestLogger.init();
	}
	
	protected static XID actorId;
	protected static String passwordHash;
	
	protected static XydraStore store;
	
	private XAddress repoAddr;
	private XModel model;
	private XSynchronizer sync;
	
	private static final XID NEWMODEL_ID = XX.toId("newmodel");
	
	@Before
	public void setUp() {
		
		assertNotNull(actorId);
		assertNotNull(passwordHash);
		assertNotNull(store);
		
		// check login
		SynchronousTestCallback<Boolean> loginCallback = new SynchronousTestCallback<Boolean>();
		store.checkLogin(actorId, passwordHash, loginCallback);
		assertTrue(waitForSuccess(loginCallback));
		
		// get repository address
		SynchronousTestCallback<XID> repoIdCallback = new SynchronousTestCallback<XID>();
		store.getRepositoryId(actorId, passwordHash, repoIdCallback);
		XID repoId = waitForSuccess(repoIdCallback);
		assertNotNull(repoId);
		this.repoAddr = XX.toAddress(repoId, null, null, null);
		
		createPhonebook(DemoModelUtil.PHONEBOOK_ID);
		
		this.model = loadModel(DemoModelUtil.PHONEBOOK_ID);
		this.sync = new XSynchronizer(this.model, store);
	}
	
	@After
	public void tearDown() {
		removeModel(DemoModelUtil.PHONEBOOK_ID);
	}
	
	@Test
	public void testSendLocalChanges() {
		
		TestLocalChangeCallback c1 = new TestLocalChangeCallback();
		TestLocalChangeCallback c2 = new TestLocalChangeCallback();
		
		TestSynchronizationCallback sc1 = new TestSynchronizationCallback();
		TestSynchronizationCallback sc2 = new TestSynchronizationCallback();
		
		// Create a command manually.
		final XID frankId = XX.toId("Frank");
		XCommand command = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
		        frankId);
		
		// Apply the command locally.
		assertTrue(this.model.executeCommand(command, c1) >= 0);
		
		assertTrue(this.model.hasObject(frankId));
		
		// Now synchronize with the server.
		this.sync.synchronize(sc1);
		
		// command may not be applied remotely yet!
		
		// We don't have to create all commands manually but can use a
		// ChangedModel.
		ChangedModel changedModel = new ChangedModel(this.model);
		
		// Make modifications to the changed model.
		final XID newfieldId = XX.toId("newField");
		changedModel.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
		assertTrue(changedModel.removeObject(DemoModelUtil.PETER_ID));
		
		// Create the command(s) describing the changes made to the
		// ChangedModel.
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		tb.applyChanges(changedModel);
		XCommand autoCommand = tb.buildCommand();
		
		// We can also modify the model directly.
		final XID janeId = XX.toId("jane");
		this.model.createObject(janeId);
		
		// Now apply the command locally. It should be automatically
		// sent to the server.
		this.model.executeCommand(autoCommand, c2);
		long finalRev = this.model.getRevisionNumber();
		
		assertTrue(this.model.hasObject(frankId));
		assertTrue(this.model.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
		assertFalse(this.model.hasObject(DemoModelUtil.PETER_ID));
		assertTrue(this.model.hasObject(janeId));
		
		this.sync.synchronize(sc2);
		
		// both commands may still not be applied remotely
		
		assertTrue(c1.waitForResult() >= 0);
		assertTrue(c2.waitForResult() >= 0);
		checkSyncCallback(sc1);
		checkSyncCallback(sc2);
		
		// check model state
		assertTrue(this.model.hasObject(frankId));
		assertTrue(this.model.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
		assertFalse(this.model.hasObject(DemoModelUtil.PETER_ID));
		assertTrue(this.model.hasObject(janeId));
		assertTrue(XCompareUtils.equalState(this.model,
		        loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID)));
		assertEquals(finalRev, this.model.getRevisionNumber());
		assertEquals(finalRev, this.model.getSynchronizedRevision());
		
		checkEvents(this.model);
		
	}
	
	@Test
	public void testLoadRemoteChanges() {
		
		XModel modelCopy = XCopyUtils.copyModel(actorId, passwordHash, this.model);
		List<XEvent> events = ChangeRecorder.record(this.model);
		assertTrue(XCompareUtils.equalState(modelCopy, this.model));
		
		// make some remote changes
		final XID bobId = XX.toId("Bob");
		XCommand command = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
		        bobId);
		executeCommand(command);
		
		// make more remote changes
		final XID janeId = XX.toId("Jane");
		final XID cookiesId = XX.toId("cookies");
		final XValue cookiesValue = XV.toValue("gone");
		final XAddress janeAddr = XX.resolveObject(this.model.getAddress(), janeId);
		final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		tb.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
		tb.addField(janeAddr, XCommand.SAFE, cookiesId);
		tb.addValue(cookiesAddr, XCommand.NEW, cookiesValue);
		executeCommand(tb.buildCommand());
		
		XBaseModel testModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
		
		// synchronize
		synchronize(this.sync);
		
		// check the local model
		assertTrue(this.model.hasObject(bobId));
		XObject jane = this.model.getObject(janeId);
		assertNotNull(jane);
		XField cookies = jane.getField(cookiesId);
		assertNotNull(cookies);
		assertEquals(cookiesValue, cookies.getValue());
		assertEquals(jane.getRevisionNumber(), cookies.getRevisionNumber());
		assertEquals(this.model.getRevisionNumber(), jane.getRevisionNumber());
		assertTrue(XCompareUtils.equalState(testModel, this.model));
		
		// check the remote model
		XBaseModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
		assertTrue(XCompareUtils.equalState(testModel, remoteModel));
		
		// check that the correct events were sent
		AbstractSynchronizeTest.replaySyncEvents(modelCopy, events);
		assertTrue(XCompareUtils.equalTree(modelCopy, this.model));
		
		checkEvents(this.model);
		
	}
	
	@Test
	public void testMergeChanges() {
		
		XModel startCopy = XCopyUtils.copyModel(actorId, passwordHash, this.model);
		List<XEvent> startEvents = ChangeRecorder.record(this.model);
		assertTrue(XCompareUtils.equalState(startCopy, this.model));
		
		XModel startCopy2 = XCopyUtils.copyModel(actorId, passwordHash, this.model);
		assertTrue(XCompareUtils.equalState(startCopy2, this.model));
		
		// make some remote changes
		final XID cakesId = XX.toId("cakes");
		XCommand command = MemoryObjectCommand.createAddCommand(this.model.getObject(
		        DemoModelUtil.JOHN_ID).getAddress(), false, cakesId);
		executeCommand(command);
		
		// make more remote changes
		final XID janeId = XX.toId("Jane");
		final XID cookiesId = XX.toId("cookies");
		final XValue cookiesValue = XV.toValue("gone");
		final XAddress janeAddr = XX.resolveObject(this.model.getAddress(), janeId);
		final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		tb.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
		tb.addField(janeAddr, XCommand.SAFE, cookiesId);
		tb.addValue(cookiesAddr, XCommand.NEW, cookiesValue);
		executeCommand(tb.buildCommand());
		
		// and some more remote changes
		XCommand command2 = MemoryObjectCommand.createAddCommand(this.model.getObject(
		        DemoModelUtil.PETER_ID).getAddress(), false, cakesId);
		executeCommand(command2);
		
		// and even more remote changes
		final XID bobId = XX.toId("Bob");
		XCommand command4 = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
		        bobId);
		executeCommand(command4);
		
		// also make some local changes
		// should be reverted on sync because of conflicting remote changes
		TestLocalChangeCallback tlc1 = new TestLocalChangeCallback();
		XTransactionBuilder tb2 = new XTransactionBuilder(this.model.getAddress());
		tb2.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
		tb2.addField(janeAddr, XCommand.SAFE, cakesId);
		assertTrue(this.model.executeCommand(tb2.buildCommand(), tlc1) >= 0);
		// should survive the sync
		final XID newfieldId = XX.toId("newField");
		this.model.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
		// should be reverted on sync
		TestLocalChangeCallback tlc2 = new TestLocalChangeCallback();
		XCommand command3 = MemoryModelCommand.createRemoveCommand(this.model.getAddress(),
		        this.model.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(),
		        DemoModelUtil.PETER_ID);
		assertTrue(this.model.executeCommand(command3, tlc2) >= 0);
		// should sync to XCommand#NOCHANGE
		TestLocalChangeCallback tlc3 = new TestLocalChangeCallback();
		XCommand command5 = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
		        bobId);
		assertTrue(this.model.executeCommand(command5, tlc3) >= 0);
		
		// check the local model
		XObject localJane = this.model.getObject(janeId);
		assertNotNull(localJane);
		assertTrue(localJane.hasField(cakesId));
		assertFalse(localJane.hasField(cookiesId));
		assertEquals(localJane.getRevisionNumber(), localJane.getField(cakesId).getRevisionNumber());
		assertFalse(this.model.hasObject(DemoModelUtil.PETER_ID));
		assertTrue(this.model.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
		assertFalse(this.model.getObject(DemoModelUtil.JOHN_ID).hasField(cakesId));
		assertTrue(this.model.hasObject(bobId));
		
		// check the remote model
		XBaseModel testModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
		assertTrue(testModel.getObject(DemoModelUtil.JOHN_ID).hasField(cakesId));
		assertFalse(testModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
		XBaseObject remoteJane = testModel.getObject(janeId);
		assertNotNull(remoteJane);
		assertTrue(remoteJane.hasField(cookiesId));
		assertFalse(remoteJane.hasField(cakesId));
		assertEquals(remoteJane.getRevisionNumber(), remoteJane.getField(cookiesId)
		        .getRevisionNumber());
		assertTrue(testModel.hasObject(DemoModelUtil.PETER_ID));
		assertTrue(testModel.getObject(DemoModelUtil.PETER_ID).hasField(cakesId));
		assertTrue(testModel.hasObject(bobId));
		
		// check events sent so far
		AbstractSynchronizeTest.replaySyncEvents(startCopy2, startEvents);
		assertTrue(XCompareUtils.equalTree(startCopy2, this.model));
		
		XModel midCopy = XCopyUtils.copyModel(actorId, passwordHash, this.model);
		List<XEvent> midEvents = ChangeRecorder.record(this.model);
		assertTrue(XCompareUtils.equalState(midCopy, this.model));
		
		// synchronize
		synchronize(this.sync);
		
		// check local model
		XObject jane = this.model.getObject(janeId);
		assertNotNull(jane);
		assertTrue(jane.hasField(cookiesId));
		assertFalse(jane.hasField(cakesId));
		assertEquals(jane.getRevisionNumber(), jane.getField(cookiesId).getRevisionNumber());
		assertTrue(this.model.hasObject(DemoModelUtil.PETER_ID));
		assertTrue(this.model.getObject(DemoModelUtil.PETER_ID).hasField(cakesId));
		assertEquals(testModel.getObject(janeId).getRevisionNumber(), this.model.getObject(janeId)
		        .getRevisionNumber());
		assertTrue(testModel.getObject(DemoModelUtil.JOHN_ID).getRevisionNumber() < this.model
		        .getObject(DemoModelUtil.JOHN_ID).getRevisionNumber());
		assertEquals(testModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(), this.model
		        .getObject(DemoModelUtil.PETER_ID).getRevisionNumber());
		assertEquals(testModel.getObject(DemoModelUtil.PETER_ID).getField(cakesId)
		        .getRevisionNumber(), this.model.getObject(DemoModelUtil.PETER_ID)
		        .getField(cakesId).getRevisionNumber());
		assertTrue(this.model.hasObject(bobId));
		assertEquals(testModel.getObject(bobId).getRevisionNumber(), this.model.getObject(bobId)
		        .getRevisionNumber());
		
		// check the remote model
		XBaseModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
		assertTrue(XCompareUtils.equalState(this.model, remoteModel));
		
		// check that the correct events were sent
		AbstractSynchronizeTest.replaySyncEvents(startCopy, startEvents);
		assertTrue(XCompareUtils.equalTree(startCopy, this.model));
		
		AbstractSynchronizeTest.replaySyncEvents(midCopy, midEvents);
		assertTrue(XCompareUtils.equalTree(midCopy, this.model));
		
		assertEquals(XCommand.FAILED, tlc1.waitForResult());
		assertEquals(XCommand.FAILED, tlc2.waitForResult());
		
		checkEvents(this.model);
		
	}
	
	@Test
	public void testCreateRemoveModel() {
		
		try {
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			XRepository repo = new MemoryRepository(actorId, passwordHash, this.repoAddr
			        .getRepository());
			
			XModel model = repo.createModel(NEWMODEL_ID);
			XSynchronizer sync = new XSynchronizer(model, store);
			XObject object = model.createObject(XX.toId("bob"));
			XField field = object.createField(XX.toId("cookies"));
			field.setValue(XV.toValue("yummy"));
			long modelRev = model.getRevisionNumber();
			HasChanged hc1 = HasChanged.listen(model);
			XBaseModel testModel = XCopyUtils.createSnapshot(model);
			
			synchronize(sync);
			
			XBaseModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			assertTrue(XCompareUtils.equalState(model, remoteModel));
			assertTrue(XCompareUtils.equalState(model, testModel));
			assertEquals(modelRev, model.getRevisionNumber());
			assertEquals(modelRev, model.getSynchronizedRevision());
			assertFalse(hc1.eventsReceived);
			
			checkEvents(model);
			
			// check that the local model still works
			model.createObject(XX.toId("jane"));
			
			repo.removeModel(NEWMODEL_ID);
			modelRev = model.getRevisionNumber();
			hc1.eventsReceived = false;
			
			synchronize(sync);
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			assertFalse(repo.hasModel(NEWMODEL_ID));
			assertEquals(modelRev, model.getRevisionNumber());
			assertEquals(modelRev, model.getSynchronizedRevision());
			assertFalse(hc1.eventsReceived);
			
			checkEvents(model);
			
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
			HasChanged hc2 = HasChanged.listen(model);
			testModel = XCopyUtils.createSnapshot(model);
			
			sync = new XSynchronizer(model, store);
			synchronize(sync);
			
			remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			assertTrue(XCompareUtils.equalState(model, remoteModel));
			assertTrue(XCompareUtils.equalTree(model, testModel));
			assertEquals(model.getRevisionNumber(), model.getSynchronizedRevision());
			assertFalse(hc2.eventsReceived);
			
			checkEvents(model);
			
			// check that the local model still works
			model.createObject(XX.toId("jane"));
			
		} finally {
			removeModel(NEWMODEL_ID);
		}
		
	}
	
	@Test
	public void testLoadRemoteChangesRemovedThenCreatedModel() {
		
		try {
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			// create a model
			XRepository repo = new MemoryRepository(actorId, passwordHash, this.repoAddr
			        .getRepository());
			XModel model = repo.createModel(NEWMODEL_ID);
			XObject object = model.createObject(XX.toId("bob"));
			XField field = object.createField(XX.toId("cookies"));
			field.setValue(XV.toValue("yummy"));
			XSynchronizer sync = new XSynchronizer(model, store);
			synchronize(sync);
			XModel modelCopy = loadModel(NEWMODEL_ID);
			assertTrue(XCompareUtils.equalState(model, modelCopy));
			long modelRev = model.getRevisionNumber();
			HasChanged hc3 = HasChanged.listen(model);
			HasChanged hc4 = HasChanged.listen(modelCopy);
			checkEvents(model);
			
			removeModel(NEWMODEL_ID);
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			// test synchronizing the model with repository
			HasChanged hc1 = new HasChanged();
			repo.addListenerForModelEvents(hc1);
			synchronize(sync);
			assertFalse(repo.hasModel(NEWMODEL_ID));
			assertEquals(modelRev + 1, model.getRevisionNumber());
			assertEquals(modelRev + 1, model.getSynchronizedRevision());
			assertTrue(hc1.eventsReceived);
			assertTrue(hc3.eventsReceived);
			checkEvents(model);
			try {
				model.createObject(XX.toId("jane"));
				fail();
			} catch(IllegalStateException ise) {
				// worked
			}
			
			// test synchronizing the model without repository
			XSynchronizer sync2 = new XSynchronizer(modelCopy, store);
			synchronize(sync2);
			assertEquals(modelRev + 1, modelCopy.getRevisionNumber());
			assertEquals(modelRev + 1, modelCopy.getSynchronizedRevision());
			assertTrue(hc4.eventsReceived);
			checkEvents(modelCopy);
			try {
				modelCopy.createObject(XX.toId("jane"));
				fail();
			} catch(IllegalStateException ise) {
				// worked
			}
			
			// now re-create the model, with some content
			createPhonebook(NEWMODEL_ID);
			XBaseModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			
			// test synchronizing the model with repository
			HasChanged hc2 = new HasChanged();
			repo.addListenerForModelEvents(hc2);
			hc3.eventsReceived = false;
			synchronize(sync);
			assertTrue(repo.hasModel(NEWMODEL_ID));
			assertTrue(hc2.eventsReceived);
			assertTrue(hc3.eventsReceived);
			checkEvents(model);
			// test that the model is not removed
			model.createObject(XX.toId("jane"));
			
			// test synchronizing the model without repository
			hc4.eventsReceived = false;
			synchronize(sync2);
			assertTrue(hc4.eventsReceived);
			checkEvents(modelCopy);
			// test that the model is not removed
			modelCopy.createObject(XX.toId("jane"));
			
		} finally {
			removeModel(NEWMODEL_ID);
		}
		
	}
	
	@Test
	public void testLoadRemoteChangesRemovedCreatedModel() {
		
		try {
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			// create a model
			XRepository repo = new MemoryRepository(actorId, passwordHash, this.repoAddr
			        .getRepository());
			XModel model = repo.createModel(NEWMODEL_ID);
			XSynchronizer sync = new XSynchronizer(model, store);
			synchronize(sync);
			XModel modelCopy = loadModel(NEWMODEL_ID);
			assertTrue(XCompareUtils.equalState(model, modelCopy));
			long modelRev = model.getRevisionNumber();
			HasChanged hc2 = HasChanged.listen(model);
			HasChanged hc3 = HasChanged.listen(modelCopy);
			checkEvents(model);
			
			removeModel(NEWMODEL_ID);
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			// now re-create the model, with some content
			XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
			        this.repoAddr, XCommand.SAFE, NEWMODEL_ID);
			executeCommand(createCommand);
			XBaseModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			
			// test synchronizing the model with repository
			HasChanged hc1 = new HasChanged();
			repo.addListenerForModelEvents(hc1);
			synchronize(sync);
			assertTrue(repo.hasModel(NEWMODEL_ID));
			assertEquals(modelRev + 2, model.getRevisionNumber());
			assertEquals(modelRev + 2, model.getSynchronizedRevision());
			assertFalse(hc1.eventsReceived);
			assertFalse(hc2.eventsReceived);
			assertTrue(XCompareUtils.equalState(model, remoteModel));
			checkEvents(model);
			// test that the model is not removed
			model.createObject(XX.toId("jane"));
			
			// test synchronizing the model without repository
			XSynchronizer sync2 = new XSynchronizer(modelCopy, store);
			synchronize(sync2);
			assertEquals(modelRev + 2, modelCopy.getRevisionNumber());
			assertEquals(modelRev + 2, modelCopy.getSynchronizedRevision());
			assertFalse(hc3.eventsReceived);
			assertTrue(XCompareUtils.equalState(modelCopy, remoteModel));
			checkEvents(modelCopy);
			// test that the model is not removed
			modelCopy.createObject(XX.toId("jane"));
			
		} finally {
			removeModel(NEWMODEL_ID);
		}
		
	}
	
	@Test
	public void testSyncModelCreatedWithoutRepository() {
		
		try {
			
			assertNull(loadModelSnapshot(NEWMODEL_ID));
			
			// create a local model
			XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
			// must be created with a repository ID to be synchronized
			XModel model = new MemoryModel(actorId, passwordHash, modelAddr);
			DemoModelUtil.setupPhonebook(model);
			
			XBaseModel snapshot = XCopyUtils.createSnapshot(model);
			
			synchronize(new XSynchronizer(model, store));
			
			assertTrue(XCompareUtils.equalTree(snapshot, model));
			XBaseModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
			assertNotNull(remoteModel);
			assertTrue(XCompareUtils.equalState(model, remoteModel));
			checkEvents(model);
			
		} finally {
			removeModel(NEWMODEL_ID);
		}
		
	}
	
	/**
	 * Create a phonebook model with the given XID in the store.
	 */
	private void createPhonebook(XID modelId) {
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(this.repoAddr,
		        XCommand.SAFE, modelId);
		executeCommand(createCommand);
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		XTransaction trans = tb.build();
		// Apply events individually so there is something in the change log
		// to test
		for(XAtomicCommand ac : trans) {
			executeCommand(ac);
		}
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
		XAddress modelAddr = XX.resolveModel(this.repoAddr, modelId);
		
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> tc;
		tc = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		store.getModelSnapshots(actorId, passwordHash, new XAddress[] { modelAddr }, tc);
		
		return waitForSuccessBatched(tc);
	}
	
	private void removeModel(XID modelId) {
		executeCommand(MemoryRepositoryCommand.createRemoveCommand(this.repoAddr, XCommand.FORCED,
		        modelId));
	}
	
	/**
	 * Execute the given command on the store and check that there were no
	 * errors.
	 */
	private void executeCommand(XCommand command) {
		SynchronousTestCallback<BatchedResult<Long>[]> tc;
		tc = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		store.executeCommands(actorId, passwordHash, new XCommand[] { command }, tc);
		
		long res = waitForSuccessBatched(tc);
		
		assertTrue(res != XCommand.FAILED);
	}
	
	/**
	 * Wait for the given callback and check that there were no errors.
	 * 
	 * TODO this could also be of use to other store tests
	 * 
	 * @return the result passed to the callback.
	 */
	private <T> T waitForSuccess(SynchronousTestCallback<T> tc) {
		
		assertEquals(SynchronousTestCallback.SUCCESS, tc.waitOnCallback(0));
		
		assertNull(tc.getException());
		return tc.getEffect();
	}
	
	/**
	 * Wait for the given callback and check that there were no errors and
	 * exactly one batched result.
	 * 
	 * TODO this could also be of use to other store tests
	 * 
	 * @return the result passed to the callback.
	 */
	private <T> T waitForSuccessBatched(SynchronousTestCallback<BatchedResult<T>[]> tc) {
		
		BatchedResult<T>[] results = waitForSuccess(tc);
		assertNotNull(results);
		assertEquals(1, results.length);
		BatchedResult<T> result = results[0];
		assertNotNull(result);
		assertNull(result.getException());
		return result.getResult();
	}
	
	/**
	 * Wait for the given callback and check that there were no errors.
	 */
	private void checkSyncCallback(TestSynchronizationCallback sc) {
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
	
	/**
	 * Synchronize and check that there were no errors.
	 */
	private void synchronize(XSynchronizer sync) {
		TestSynchronizationCallback sc = new TestSynchronizationCallback();
		sync.synchronize(sc);
		checkSyncCallback(sc);
	}
	
	private void checkEvents(XModel model) {
		
		XChangeLog cl = model.getChangeLog();
		
		long startRev = cl.getFirstRevisionNumber();
		Iterator<XEvent> localEvents = cl.getEventsBetween(startRev, Long.MAX_VALUE);
		
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback;
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		store.getEvents(actorId, passwordHash, new GetEventsRequest[] { new GetEventsRequest(model
		        .getAddress(), startRev, Long.MAX_VALUE) }, callback);
		XEvent[] result = waitForSuccessBatched(callback);
		Iterator<XEvent> remoteEvents = Arrays.asList(result).iterator();
		
		while(remoteEvents.hasNext()) {
			assertTrue(localEvents.hasNext());
			XEvent remote = remoteEvents.next();
			XEvent local = localEvents.next();
			assertEquals(remote, local);
		}
		assertFalse(localEvents.hasNext());
		
	}
}
