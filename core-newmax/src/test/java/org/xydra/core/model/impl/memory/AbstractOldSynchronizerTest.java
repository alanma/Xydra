package org.xydra.core.model.impl.memory;

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
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.ChangeRecorder;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.HasChanged;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.SynchronousCallbackWithOneResult;
import org.xydra.store.XydraStore;
import org.xydra.store.sync.XSynchronizer;


/**
 * Test for {@link XSynchronizer} and {@link SynchronizesChangesImpl} that uses
 * an arbitrary {@link XydraStore}.
 * 
 * Subclasses should implement the abstract methods and set protected members
 * {@link #actorId} and {@link #passwordHash}.
 * 
 * TODO test more with deleted models
 * 
 * @author dscharrer
 */
abstract public class AbstractOldSynchronizerTest {
    
    /**
     * The actor ID used to interact with the {@link #store}. This actor needs
     * to have read/write access. Subclasses should initialize this before
     * {@link #setUp()} gets called.
     */
    protected XId actorId;
    
    /**
     * The password hash for {@link #actorId}. Subclasses should initialize this
     * before {@link #setUp()} gets called.
     */
    protected String passwordHash;
    
    /**
     * The {@link XydraStore} to be used for testing. Subclasses should
     * initialize this before {@link #setUp()} gets called.
     */
    private XydraStore store;
    
    protected abstract XydraStore createStore();
    
    private static final XId NEWMODEL_ID = XX.toId("newmodel");
    
    private XModel model;
    private XAddress repoAddr;
    private XSynchronizer sync;
    
    {
        LoggerTestHelper.init();
    }
    
    private void checkEvents(XModel model) {
        
        XChangeLog cl = model.getChangeLog();
        
        long startRev = cl.getBaseRevisionNumber() + 1;
        Iterator<XEvent> localEvents = cl.getEventsBetween(startRev, Long.MAX_VALUE);
        
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback;
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.actorId, this.passwordHash,
                new GetEventsRequest[] { new GetEventsRequest(model.getAddress(), startRev,
                        Long.MAX_VALUE) }, callback);
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
    
    /**
     * Wait for the given callback and check that there were no errors.
     */
    private static void checkSyncCallback(ForTestSynchronizationCallback sc) {
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
     * Create a phonebook model with the given XId in the store.
     */
    private void createPhonebook(XId modelId) {
        XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(this.repoAddr,
                XCommand.SAFE, modelId);
        executeCommand(createCommand);
        XAddress modelAddr = createCommand.getChangedEntity();
        XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
        DemoModelUtil.setupPhonebook(modelAddr, tb, false);
        XTransaction trans = tb.build();
        // Apply events individually so there is something in the change log
        // to test
        for(XAtomicCommand ac : trans) {
            executeCommand(ac);
        }
    }
    
    /**
     * Execute the given command on the store and check that there were no
     * errors.
     */
    private void executeCommand(XCommand command) {
        SynchronousCallbackWithOneResult<BatchedResult<Long>[]> tc;
        tc = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();
        
        this.store.executeCommands(this.actorId, this.passwordHash, new XCommand[] { command }, tc);
        
        long res = waitForSuccessBatched(tc);
        
        assertTrue("Should not have failed (" + res + "): " + command, res != XCommand.FAILED);
    }
    
    private XModel loadModel(XId modelId) {
        
        XReadableModel modelSnapshot = loadModelSnapshot(modelId);
        assertNotNull(modelSnapshot);
        
        return XX.wrap(this.actorId, this.passwordHash, modelSnapshot);
    }
    
    private XReadableModel loadModelSnapshot(XId modelId) {
        XAddress modelAddr = XX.resolveModel(this.repoAddr, modelId);
        
        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> tc;
        tc = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        
        this.store.getModelSnapshots(this.actorId, this.passwordHash,
                new GetWithAddressRequest[] { new GetWithAddressRequest(modelAddr, false) }, tc);
        
        return waitForSuccessBatched(tc);
    }
    
    private void removeModel(XId modelId) {
        XyAssert.xyAssert(this.repoAddr != null, "this.repoAddr != null");
        assert this.repoAddr != null;
        executeCommand(MemoryRepositoryCommand.createRemoveCommand(this.repoAddr, XCommand.FORCED,
                modelId));
    }
    
    @Before
    public void setUp() {
        
        this.store = createStore();
        
        assertNotNull(this.actorId);
        assertNotNull(this.passwordHash);
        assertNotNull(this.store);
        
        // check login
        SynchronousCallbackWithOneResult<Boolean> loginCallback = new SynchronousCallbackWithOneResult<Boolean>();
        this.store.checkLogin(this.actorId, this.passwordHash, loginCallback);
        assertTrue(waitForSuccess(loginCallback));
        
        // get repository address
        SynchronousCallbackWithOneResult<XId> repoIdCallback = new SynchronousCallbackWithOneResult<XId>();
        this.store.getRepositoryId(this.actorId, this.passwordHash, repoIdCallback);
        XId repoId = waitForSuccess(repoIdCallback);
        assertNotNull(repoId);
        this.repoAddr = XX.toAddress(repoId, null, null, null);
        
        createPhonebook(DemoModelUtil.PHONEBOOK_ID);
        
        this.model = loadModel(DemoModelUtil.PHONEBOOK_ID);
        this.sync = new XSynchronizer(this.model, this.store);
        
        XyAssert.xyAssert(this.repoAddr != null);
        assert this.repoAddr != null;
    }
    
    /**
     * Synchronize and check that there were no errors.
     */
    private static void synchronize(XSynchronizer sync) {
        ForTestSynchronizationCallback sc = new ForTestSynchronizationCallback();
        sync.synchronize(sc);
        checkSyncCallback(sc);
    }
    
    @After
    public void tearDown() {
        removeModel(DemoModelUtil.PHONEBOOK_ID);
    }
    
    // FIXME MONKEY @Test
    public void testCreateRemoveModel() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            XRepository repo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            
            XModel model = repo.createModel(NEWMODEL_ID);
            XSynchronizer sync = new XSynchronizer(model, this.store);
            XObject object = model.createObject(XX.toId("bob"));
            XField field = object.createField(XX.toId("cookies"));
            field.setValue(XV.toValue("yummy"));
            long modelRev = model.getRevisionNumber();
            HasChanged hc1 = HasChanged.listen(model);
            XReadableModel testModel = XCopyUtils.createSnapshot(model);
            
            synchronize(sync);
            
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
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
            
            sync = new XSynchronizer(model, this.store);
            synchronize(sync);
            
            remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            assertTrue(XCompareUtils.equalTree(model, testModel));
            assertEquals(model.getRevisionNumber(), model.getSynchronizedRevision());
            assertTrue(hc2.eventsReceived);
            
            checkEvents(model);
            
            // check that the local model still works
            model.createObject(XX.toId("jane"));
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    static final XId bobId = XX.toId("Bob");
    static final XId janeId = XX.toId("Jane");
    static final XId cookiesId = XX.toId("cookies");
    static final XValue cookiesValue = XV.toValue("gone");
    
    @Test
    public void testDoSafeTxn() {
        XCommand command = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
                bobId);
        executeCommand(command);
        
        final XAddress janeAddr = XX.resolveObject(this.model.getAddress(), janeId);
        final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
        
        XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
        tb.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
        tb.addField(janeAddr, XCommand.SAFE, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE, cookiesValue);
        
        executeCommand(tb.buildCommand());
    }
    
    @Test
    public void testLoadRemoteChanges() {
        
        XModel modelCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.model);
        List<XEvent> events = ChangeRecorder.record(this.model);
        assertTrue(XCompareUtils.equalState(modelCopy, this.model));
        
        // make some remote changes
        XCommand command = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
                bobId);
        executeCommand(command);
        
        // make more remote changes
        final XAddress janeAddr = XX.resolveObject(this.model.getAddress(), janeId);
        final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
        XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
        tb.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
        tb.addField(janeAddr, XCommand.SAFE, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE, cookiesValue);
        
        XCommand txn = tb.buildCommand();
        executeCommand(txn);
        
        XReadableModel testModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        
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
        XReadableModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(XCompareUtils.equalState(testModel, remoteModel));
        
        // check that the correct events were sent
        SynchronizeTest.replaySyncEvents(modelCopy, events);
        assertTrue(XCompareUtils.equalTree(modelCopy, this.model));
        
        checkEvents(this.model);
        
    }
    
    @Test
    public void testLoadRemoteChangesRemovedCreatedModel() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a model
            XRepository repo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            XModel model = repo.createModel(NEWMODEL_ID);
            XSynchronizer sync = new XSynchronizer(model, this.store);
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
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
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
            XSynchronizer sync2 = new XSynchronizer(modelCopy, this.store);
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
    
    // FIXME MONKEY @Test
    public void testLoadRemoteChangesRemovedThenCreatedModel() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a model
            XRepository repo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            XModel model = repo.createModel(NEWMODEL_ID);
            XObject object = model.createObject(XX.toId("bob"));
            XField field = object.createField(XX.toId("cookies"));
            field.setValue(XV.toValue("yummy"));
            XSynchronizer sync = new XSynchronizer(model, this.store);
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
            XSynchronizer sync2 = new XSynchronizer(modelCopy, this.store);
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
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
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
    public void testMergeChanges() {
        
        XModel startCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.model);
        List<XEvent> startEvents = ChangeRecorder.record(this.model);
        assertTrue(XCompareUtils.equalState(startCopy, this.model));
        
        XModel startCopy2 = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.model);
        assertTrue(XCompareUtils.equalState(startCopy2, this.model));
        
        // make some remote changes
        final XId cakesId = XX.toId("cakes");
        XCommand command = MemoryObjectCommand.createAddCommand(
                this.model.getObject(DemoModelUtil.JOHN_ID).getAddress(), false, cakesId);
        executeCommand(command);
        
        // make more remote changes
        final XId janeId = XX.toId("Jane");
        final XId cookiesId = XX.toId("cookies");
        final XValue cookiesValue = XV.toValue("gone");
        final XAddress janeAddr = XX.resolveObject(this.model.getAddress(), janeId);
        final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
        XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
        tb.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
        tb.addField(janeAddr, XCommand.SAFE, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE, cookiesValue);
        executeCommand(tb.buildCommand());
        
        // and some more remote changes
        XCommand command2 = MemoryObjectCommand.createAddCommand(
                this.model.getObject(DemoModelUtil.PETER_ID).getAddress(), false, cakesId);
        executeCommand(command2);
        
        // and even more remote changes
        final XId bobId = XX.toId("Bob");
        XCommand command4 = MemoryModelCommand.createAddCommand(this.model.getAddress(), false,
                bobId);
        executeCommand(command4);
        
        // also make some local changes
        // should be reverted on sync because of conflicting remote changes
        ForTestLocalChangeCallback tlc1 = new ForTestLocalChangeCallback();
        XTransactionBuilder tb2 = new XTransactionBuilder(this.model.getAddress());
        tb2.addObject(this.model.getAddress(), XCommand.SAFE, janeId);
        tb2.addField(janeAddr, XCommand.SAFE, cakesId);
        assertTrue(this.model.executeCommand(tb2.buildCommand(), tlc1) >= 0);
        // should survive the sync
        final XId newfieldId = XX.toId("newField");
        this.model.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
        // should be reverted on sync
        ForTestLocalChangeCallback tlc2 = new ForTestLocalChangeCallback();
        XCommand command3 = MemoryModelCommand.createRemoveCommand(this.model.getAddress(),
                this.model.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(),
                DemoModelUtil.PETER_ID);
        assertTrue(this.model.executeCommand(command3, tlc2) >= 0);
        // should sync to XCommand#NOCHANGE
        ForTestLocalChangeCallback tlc3 = new ForTestLocalChangeCallback();
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
        XReadableModel testModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(testModel.getObject(DemoModelUtil.JOHN_ID).hasField(cakesId));
        assertFalse(testModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        XReadableObject remoteJane = testModel.getObject(janeId);
        assertNotNull(remoteJane);
        assertTrue(remoteJane.hasField(cookiesId));
        assertFalse(remoteJane.hasField(cakesId));
        assertEquals(remoteJane.getRevisionNumber(), remoteJane.getField(cookiesId)
                .getRevisionNumber());
        assertTrue(testModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(testModel.getObject(DemoModelUtil.PETER_ID).hasField(cakesId));
        assertTrue(testModel.hasObject(bobId));
        
        // check events sent so far
        SynchronizeTest.replaySyncEvents(startCopy2, startEvents);
        assertTrue(XCompareUtils.equalTree(startCopy2, this.model));
        
        XModel midCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.model);
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
        XReadableModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(XCompareUtils.equalState(this.model, remoteModel));
        
        // check that the correct events were sent
        SynchronizeTest.replaySyncEvents(startCopy, startEvents);
        assertTrue(XCompareUtils.equalTree(startCopy, this.model));
        
        SynchronizeTest.replaySyncEvents(midCopy, midEvents);
        assertTrue(XCompareUtils.equalTree(midCopy, this.model));
        
        assertEquals(XCommand.FAILED, tlc1.waitForResult());
        assertEquals(XCommand.FAILED, tlc2.waitForResult());
        
        checkEvents(this.model);
        
    }
    
    @Test
    public void testSendLocalChanges() {
        
        ForTestLocalChangeCallback c1 = new ForTestLocalChangeCallback();
        ForTestLocalChangeCallback c2 = new ForTestLocalChangeCallback();
        
        ForTestSynchronizationCallback sc1 = new ForTestSynchronizationCallback();
        ForTestSynchronizationCallback sc2 = new ForTestSynchronizationCallback();
        
        // Create a command manually.
        final XId frankId = XX.toId("Frank");
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
        final XId newfieldId = XX.toId("newField");
        changedModel.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
        assertTrue(changedModel.removeObject(DemoModelUtil.PETER_ID));
        
        // Create the command(s) describing the changes made to the
        // ChangedModel.
        XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
        tb.applyChanges(changedModel);
        XCommand autoCommand = tb.buildCommand();
        
        // We can also modify the model directly.
        final XId janeId = XX.toId("jane");
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
    
    /**
     * A test to replicate the observed client sync failures during fixCommands
     */
    @Test
    public void testSyncModelCreatedWithoutRepositoryReplicateClient() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);
            
            XSynchronizer synchronizer = new XSynchronizer(model, this.store);
            
            XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());
            
            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(XV.toValue("A new title"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID).setValue(
                    XV.toValue(new String[] { "Decoupled distributed systems", "Fries", "Bacon" }));
            
            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            txBuilder = new XTransactionBuilder(model.getAddress());
            
            cm = new ChangedModel(model);
            johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(
                    XV.toValue("A new title second time"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID)
                    .setValue(
                            XV.toValue(new String[] { "Highly decoupled distributed systems",
                                    "Ham", "Eggs" }));
            
            txBuilder.applyChanges(cm);
            tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(synchronizer);
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    /**
     * A test to replicate the observed client sync failures during fixCommands,
     * the difference to the previous test is that this test initially sync with
     * the server.
     */
    // FIXME MONKEY @Test
    public void testSyncModelCreatedWithoutRepositoryReplicateClientWithInitialSync() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);
            
            XSynchronizer synchronizer = new XSynchronizer(model, this.store);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);
            
            XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());
            
            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(XV.toValue("A new title"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID).setValue(
                    XV.toValue(new String[] { "Decoupled distributed systems", "Fries", "Bacon" }));
            
            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            txBuilder = new XTransactionBuilder(model.getAddress());
            
            cm = new ChangedModel(model);
            johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(
                    XV.toValue("A new title second time"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID)
                    .setValue(
                            XV.toValue(new String[] { "Highly decoupled distributed systems",
                                    "Ham", "Eggs" }));
            
            txBuilder.applyChanges(cm);
            tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(synchronizer);
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClient() {
        
        try {
            removeModel(NEWMODEL_ID);
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            XSynchronizer synchronizer = new XSynchronizer(model, this.store);
            
            XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());
            
            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.createObject(DemoModelUtil.JOHN_ID);
            XWritableField titleField = johnObject.createField(DemoModelUtil.TITLE_ID);
            titleField.setValue(XV.toValue("A new title"));
            XWritableField aliasesField = johnObject.createField(DemoModelUtil.ALIASES_ID);
            
            aliasesField.setValue(XV.toValue(new String[] { "Decoupled distributed systems",
                    "Fries", "Bacon" }));
            
            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            txBuilder = new XTransactionBuilder(model.getAddress());
            
            cm = new ChangedModel(model);
            johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(
                    XV.toValue("A new title second time"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID)
                    .setValue(
                            XV.toValue(new String[] { "Highly decoupled distributed systems",
                                    "Ham", "Eggs" }));
            
            txBuilder.applyChanges(cm);
            tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(synchronizer);
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    /**
     * A minimal test to replicate the observed client sync failures during
     * fixCommands. No txn used.
     */
    // FIXME MONKEY @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClientWithInitialSyncNoTxn() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            XSynchronizer synchronizer = new XSynchronizer(model, this.store);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);
            
            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.createObject(DemoModelUtil.JOHN_ID);
            XWritableField titleField = johnObject.createField(DemoModelUtil.TITLE_ID);
            titleField.setValue(XV.toValue("A new title"));
            XWritableField aliasesField = johnObject.createField(DemoModelUtil.ALIASES_ID);
            
            aliasesField.setValue(XV.toValue(new String[] { "Decoupled distributed systems",
                    "Fries", "Bacon" }));
            
            ChangedModel.commitTo(cm, model);
            
            cm = new ChangedModel(model);
            johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(
                    XV.toValue("A new title second time"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID)
                    .setValue(
                            XV.toValue(new String[] { "Highly decoupled distributed systems",
                                    "Ham", "Eggs" }));
            ChangedModel.commitTo(cm, model);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(synchronizer);
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    /**
     * A minimal test to replicate the observed client sync failures during
     * fixCommands. No txn used.
     */
    // FIXME MONKEY @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClientWithInitialSyncNoTxnSafeCmd() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            XSynchronizer synchronizer = new XSynchronizer(model, this.store);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);
            
            XModelCommand cmd1 = X.getCommandFactory().createSafeAddObjectCommand(modelAddr,
                    DemoModelUtil.JOHN_ID);
            model.executeCommand(cmd1);
            
            XObjectCommand cmd2 = X.getCommandFactory().createSafeAddFieldCommand(
                    XX.resolveObject(modelAddr, DemoModelUtil.JOHN_ID), DemoModelUtil.TITLE_ID);
            model.executeCommand(cmd2);
            
            XWritableObject johnObject = model.getObject(DemoModelUtil.JOHN_ID);
            
            XWritableField titleField = johnObject.getField(DemoModelUtil.TITLE_ID);
            XFieldCommand cmd3 = X.getCommandFactory().createSafeAddValueCommand(
                    XX.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.TITLE_ID),
                    
                    titleField.getRevisionNumber(), XV.toValue("A new title"));
            model.executeCommand(cmd3);
            
            XObjectCommand cmd4 = X.getCommandFactory().createSafeAddFieldCommand(
                    XX.resolveObject(modelAddr, DemoModelUtil.JOHN_ID), DemoModelUtil.ALIASES_ID);
            model.executeCommand(cmd4);
            
            XWritableField aliasesField = johnObject.getField(DemoModelUtil.ALIASES_ID);
            XFieldCommand cmd5 = X.getCommandFactory().createSafeAddValueCommand(
                    XX.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.ALIASES_ID),
                    
                    aliasesField.getRevisionNumber(),
                    XV.toValue(new String[] { "Decoupled distributed systems", "Fries", "Bacon" }));
            model.executeCommand(cmd5);
            
            titleField = johnObject.getField(DemoModelUtil.TITLE_ID);
            long revBefore = titleField.getRevisionNumber();
            
            XFieldCommand cmd6 = X.getCommandFactory().createSafeChangeValueCommand(
                    XX.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.TITLE_ID),
                    
                    titleField.getRevisionNumber(), XV.toValue("A brand new title"));
            long result = model.executeCommand(cmd6);
            assertTrue(XCommandUtils.success(result));
            
            titleField = johnObject.getField(DemoModelUtil.TITLE_ID);
            long revAfter = titleField.getRevisionNumber();
            assertTrue(revBefore != revAfter);
            
            XFieldCommand cmd7 = X.getCommandFactory().createSafeChangeValueCommand(
                    XX.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.ALIASES_ID),
                    
                    aliasesField.getRevisionNumber(),
                    XV.toValue(new String[] { "Highly decoupled distributed systems", "Fries",
                            "Bacon" }));
            model.executeCommand(cmd7);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(synchronizer);
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    /**
     * A minimal test to replicate the observed client sync failures during
     * fixCommands.
     */
    @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClientWithInitialSyncWithTxn() {
        
        try {
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            XSynchronizer synchronizer = new XSynchronizer(model, this.store);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);
            
            XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());
            
            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.createObject(DemoModelUtil.JOHN_ID);
            XWritableField titleField = johnObject.createField(DemoModelUtil.TITLE_ID);
            titleField.setValue(XV.toValue("A new title"));
            XWritableField aliasesField = johnObject.createField(DemoModelUtil.ALIASES_ID);
            
            aliasesField.setValue(XV.toValue(new String[] { "Decoupled distributed systems",
                    "Fries", "Bacon" }));
            
            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            txBuilder = new XTransactionBuilder(model.getAddress());
            
            cm = new ChangedModel(model);
            johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(
                    XV.toValue("A new title second time"));
            
            johnObject.getField(DemoModelUtil.ALIASES_ID)
                    .setValue(
                            XV.toValue(new String[] { "Highly decoupled distributed systems",
                                    "Ham", "Eggs" }));
            
            txBuilder.applyChanges(cm);
            tx = txBuilder.build();
            
            model.executeCommand(tx);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(synchronizer);
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    // FIXME MONKEY @Test
    public void testSyncModelCreatedWithoutRepository() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a local model
            XAddress modelAddr = XX.resolveModel(this.repoAddr, NEWMODEL_ID);
            // must be created with a repository ID to be synchronized
            XModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(new XSynchronizer(model, this.store));
            
            assertTrue(XCompareUtils.equalTree(snapshot, model));
            XReadableModel remoteModel = loadModelSnapshot(NEWMODEL_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            
        } finally {
            removeModel(NEWMODEL_ID);
        }
        
    }
    
    /**
     * Wait for the given callback and check that there were no errors.
     * 
     * TODO arch: this could also be of use to other store tests
     * 
     * @return the result passed to the callback.
     */
    private static <T> T waitForSuccess(SynchronousCallbackWithOneResult<T> tc) {
        
        assertEquals(SynchronousCallbackWithOneResult.SUCCESS, tc.waitOnCallback(0));
        
        assertNull(tc.getException());
        return tc.getEffect();
    }
    
    /**
     * Wait for the given callback and check that there were no errors and
     * exactly one batched result.
     * 
     * TODO arch: this could also be of use to other store tests
     * 
     * @return the result passed to the callback.
     */
    private static <T> T waitForSuccessBatched(
            SynchronousCallbackWithOneResult<BatchedResult<T>[]> tc) {
        
        BatchedResult<T>[] results = waitForSuccess(tc);
        assertNotNull(results);
        assertEquals(1, results.length);
        BatchedResult<T> result = results[0];
        assertNotNull(result);
        assertNull(result.toString(), result.getException());
        return result.getResult();
    }
    
}