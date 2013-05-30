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
import org.xydra.store.sync.NewSyncer;


/**
 * Test for {@link NewSyncer} and {@link SynchronizesChangesImpl} that uses an
 * arbitrary {@link XydraStore}.
 * 
 * Subclasses should implement the abstract methods and set protected members
 * {@link #actorId} and {@link #passwordHash}.
 * 
 * TODO test more with deleted models
 * 
 * @author dscharrer
 */
abstract public class AbstractSynchronizerTest {
    
    /**
     * The actor ID used to interact with the {@link #remoteStore}. This actor
     * needs to have read/write access. Subclasses should initialize this before
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
    private XydraStore remoteStore;
    
    protected abstract XydraStore createStore();
    
    private static final XId NEWMODEL_ID = XX.toId("newmodel");
    
    private IMemoryModel localModel;
    private XAddress repoAddr;
    private NewSyncer sync;
    
    {
        LoggerTestHelper.init();
    }
    
    private void checkEvents(XModel model) {
        
        XChangeLog cl = model.getChangeLog();
        
        long startRev = cl.getBaseRevisionNumber() + 1;
        Iterator<XEvent> localEvents = cl.getEventsBetween(startRev, Long.MAX_VALUE);
        
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback;
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.remoteStore.getEvents(this.actorId, this.passwordHash,
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
     * Create a phonebook model with the given XId in the store.
     */
    private void createPhonebook(XId modelId) {
        assert this.localModel == null;
        XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(this.repoAddr,
                XCommand.SAFE_STATE_BOUND, modelId);
        executeCommandOnStore(createCommand);
        XAddress modelAddr = createCommand.getChangedEntity();
        XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
        DemoModelUtil.setupPhonebook(modelAddr, tb, false);
        XTransaction trans = tb.build();
        // Apply events individually so there is something in the change log
        // to test
        for(XAtomicCommand ac : trans) {
            executeCommandOnStore(ac);
        }
    }
    
    /**
     * Execute the given command on the store and check that there were no
     * errors.
     */
    private void executeCommandOnStore(XCommand command) {
        SynchronousCallbackWithOneResult<BatchedResult<Long>[]> tc;
        tc = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();
        
        this.remoteStore.executeCommands(this.actorId, this.passwordHash,
                new XCommand[] { command }, tc);
        
        long res = waitForSuccessBatched(tc);
        
        assertTrue("Should not have failed (" + res + "): " + command, res != XCommand.FAILED);
    }
    
    private IMemoryModel loadModel(XId modelId) {
        
        XReadableModel modelSnapshot = loadModelSnapshot(modelId);
        assertNotNull(modelSnapshot);
        
        return (IMemoryModel)XX.wrap(this.actorId, this.passwordHash, modelSnapshot);
    }
    
    private XReadableModel loadModelSnapshot(XId modelId) {
        XAddress modelAddr = XX.resolveModel(this.repoAddr, modelId);
        
        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> tc;
        tc = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        
        this.remoteStore.getModelSnapshots(this.actorId, this.passwordHash,
                new GetWithAddressRequest[] { new GetWithAddressRequest(modelAddr, false) }, tc);
        
        return waitForSuccessBatched(tc);
    }
    
    private void removeModel(XId modelId) {
        XyAssert.xyAssert(this.repoAddr != null, "this.repoAddr != null");
        assert this.repoAddr != null;
        executeCommandOnStore(MemoryRepositoryCommand.createRemoveCommand(this.repoAddr,
                XCommand.FORCED, modelId));
    }
    
    @Before
    public void setUp() {
        
        this.remoteStore = createStore();
        
        assertNotNull(this.actorId);
        assertNotNull(this.passwordHash);
        assertNotNull(this.remoteStore);
        
        // check login
        SynchronousCallbackWithOneResult<Boolean> loginCallback = new SynchronousCallbackWithOneResult<Boolean>();
        this.remoteStore.checkLogin(this.actorId, this.passwordHash, loginCallback);
        assertTrue(waitForSuccess(loginCallback));
        
        // get repository address
        SynchronousCallbackWithOneResult<XId> repoIdCallback = new SynchronousCallbackWithOneResult<XId>();
        this.remoteStore.getRepositoryId(this.actorId, this.passwordHash, repoIdCallback);
        XId repoId = waitForSuccess(repoIdCallback);
        assertNotNull(repoId);
        this.repoAddr = XX.toAddress(repoId, null, null, null);
        
        createPhonebook(DemoModelUtil.PHONEBOOK_ID);
        
        this.localModel = loadModel(DemoModelUtil.PHONEBOOK_ID);
        this.sync = createSyncer(this.remoteStore, this.localModel);
        
        XyAssert.xyAssert(this.repoAddr != null);
        assert this.repoAddr != null;
    }
    
    private NewSyncer createSyncer(XydraStore store, IMemoryModel model) {
        return new NewSyncer(this.remoteStore, this.localModel, this.localModel.getState(),
                this.localModel.getRoot(), this.actorId, this.passwordHash,
                this.localModel.getSynchronizedRevision());
    }
    
    /**
     * Synchronize and check that there were no errors.
     */
    private static void synchronize(NewSyncer sync) {
        sync.startSync();
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
            
            IMemoryModel model = (IMemoryModel)repo.createModel(NEWMODEL_ID);
            NewSyncer sync = createSyncer(this.remoteStore, model);
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
            
            model = (IMemoryModel)repo.createModel(NEWMODEL_ID);
            model.createObject(XX.toId("john"));
            modelRev = model.getRevisionNumber();
            HasChanged hc2 = HasChanged.listen(model);
            testModel = XCopyUtils.createSnapshot(model);
            
            sync = createSyncer(this.remoteStore, model);
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
        XCommand command = MemoryModelCommand.createAddCommand(this.localModel.getAddress(), false,
                bobId);
        executeCommandOnStore(command);
        
        final XAddress janeAddr = XX.resolveObject(this.localModel.getAddress(), janeId);
        final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
        
        XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, janeId);
        tb.addField(janeAddr, XCommand.SAFE_STATE_BOUND, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE_STATE_BOUND, cookiesValue);
        
        executeCommandOnStore(tb.buildCommand());
    }
    
    @Test
    public void testLoadRemoteChanges() {
        
        XModel modelCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        List<XEvent> events = ChangeRecorder.record(this.localModel);
        assertTrue(XCompareUtils.equalState(modelCopy, this.localModel));
        
        // make some remote changes
        XCommand command = MemoryModelCommand.createAddCommand(this.localModel.getAddress(), false,
                bobId);
        executeCommandOnStore(command);
        
        // make more remote changes
        final XAddress janeAddr = XX.resolveObject(this.localModel.getAddress(), janeId);
        final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
        XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, janeId);
        tb.addField(janeAddr, XCommand.SAFE_STATE_BOUND, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE_STATE_BOUND, cookiesValue);
        XCommand txn = tb.buildCommand();
        executeCommandOnStore(txn);
        
        XReadableModel testModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        
        // synchronize
        synchronize(this.sync);
        
        // check the local model
        assertTrue(this.localModel.hasObject(bobId));
        XObject jane = this.localModel.getObject(janeId);
        assertNotNull(jane);
        XField cookies = jane.getField(cookiesId);
        assertNotNull(cookies);
        assertEquals(cookiesValue, cookies.getValue());
        assertEquals(jane.getRevisionNumber(), cookies.getRevisionNumber());
        assertEquals(this.localModel.getRevisionNumber(), jane.getRevisionNumber());
        assertTrue(XCompareUtils.equalState(testModel, this.localModel));
        
        // check the remote model
        XReadableModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(XCompareUtils.equalState(testModel, remoteModel));
        
        // check that the correct events were sent
        SynchronizeTest.replaySyncEvents(modelCopy, events);
        assertTrue(XCompareUtils.equalTree(modelCopy, this.localModel));
        
        checkEvents(this.localModel);
        
    }
    
    @Test
    public void testLoadRemoteChangesRemovedCreatedModel() {
        
        try {
            
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // create a model
            XRepository repo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            IMemoryModel model = (IMemoryModel)repo.createModel(NEWMODEL_ID);
            NewSyncer sync = createSyncer(this.remoteStore, model);
            synchronize(sync);
            IMemoryModel modelCopy = loadModel(NEWMODEL_ID);
            assertTrue(XCompareUtils.equalState(model, modelCopy));
            long modelRev = model.getRevisionNumber();
            HasChanged hc2 = HasChanged.listen(model);
            HasChanged hc3 = HasChanged.listen(modelCopy);
            checkEvents(model);
            
            removeModel(NEWMODEL_ID);
            assertNull(loadModelSnapshot(NEWMODEL_ID));
            
            // now re-create the model, with some content
            XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
                    this.repoAddr, XCommand.SAFE_STATE_BOUND, NEWMODEL_ID);
            executeCommandOnStore(createCommand);
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
            NewSyncer sync2 = createSyncer(this.remoteStore, modelCopy);
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
            IMemoryModel model = (IMemoryModel)repo.createModel(NEWMODEL_ID);
            XObject object = model.createObject(XX.toId("bob"));
            XField field = object.createField(XX.toId("cookies"));
            field.setValue(XV.toValue("yummy"));
            NewSyncer sync = createSyncer(this.remoteStore, model);
            synchronize(sync);
            IMemoryModel modelCopy = loadModel(NEWMODEL_ID);
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
            NewSyncer sync2 = createSyncer(this.remoteStore, modelCopy);
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
        
        XModel startCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        List<XEvent> startEvents = ChangeRecorder.record(this.localModel);
        assertTrue(XCompareUtils.equalState(startCopy, this.localModel));
        
        XModel startCopy2 = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        assertTrue(XCompareUtils.equalState(startCopy2, this.localModel));
        
        // make some remote changes
        final XId cakesId = XX.toId("cakes");
        XCommand command = MemoryObjectCommand.createAddCommand(
                this.localModel.getObject(DemoModelUtil.JOHN_ID).getAddress(), false, cakesId);
        executeCommandOnStore(command);
        
        // make more remote changes
        final XId janeId = XX.toId("Jane");
        final XId cookiesId = XX.toId("cookies");
        final XValue cookiesValue = XV.toValue("gone");
        final XAddress janeAddr = XX.resolveObject(this.localModel.getAddress(), janeId);
        final XAddress cookiesAddr = XX.resolveField(janeAddr, cookiesId);
        XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, janeId);
        tb.addField(janeAddr, XCommand.SAFE_STATE_BOUND, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE_STATE_BOUND, cookiesValue);
        executeCommandOnStore(tb.buildCommand());
        
        // and some more remote changes
        XCommand command2 = MemoryObjectCommand.createAddCommand(
                this.localModel.getObject(DemoModelUtil.PETER_ID).getAddress(), false, cakesId);
        executeCommandOnStore(command2);
        
        // and even more remote changes
        final XId bobId = XX.toId("Bob");
        XCommand command4 = MemoryModelCommand.createAddCommand(this.localModel.getAddress(),
                false, bobId);
        executeCommandOnStore(command4);
        
        // also make some local changes
        // should be reverted on sync because of conflicting remote changes
        XTransactionBuilder tb2 = new XTransactionBuilder(this.localModel.getAddress());
        tb2.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, janeId);
        tb2.addField(janeAddr, XCommand.SAFE_STATE_BOUND, cakesId);
        assertTrue(this.localModel.executeCommand(tb2.buildCommand()) >= 0);
        // should survive the sync
        final XId newfieldId = XX.toId("newField");
        this.localModel.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
        // should be reverted on sync
        ForTestLocalChangeCallback tlc2 = new ForTestLocalChangeCallback();
        XCommand command3 = MemoryModelCommand.createRemoveCommand(this.localModel.getAddress(),
                this.localModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(),
                DemoModelUtil.PETER_ID);
        assertTrue(this.localModel.executeCommand(command3) >= 0);
        // should sync to XCommand#NOCHANGE
        ForTestLocalChangeCallback tlc3 = new ForTestLocalChangeCallback();
        XCommand command5 = MemoryModelCommand.createAddCommand(this.localModel.getAddress(),
                false, bobId);
        assertTrue(this.localModel.executeCommand(command5) >= 0);
        
        // check the local model
        XObject localJane = this.localModel.getObject(janeId);
        assertNotNull(localJane);
        assertTrue(localJane.hasField(cakesId));
        assertFalse(localJane.hasField(cookiesId));
        assertEquals(localJane.getRevisionNumber(), localJane.getField(cakesId).getRevisionNumber());
        assertFalse(this.localModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        assertFalse(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(cakesId));
        assertTrue(this.localModel.hasObject(bobId));
        
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
        assertTrue(XCompareUtils.equalTree(startCopy2, this.localModel));
        
        XModel midCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        List<XEvent> midEvents = ChangeRecorder.record(this.localModel);
        assertTrue(XCompareUtils.equalState(midCopy, this.localModel));
        
        // synchronize
        synchronize(this.sync);
        
        // check local model
        XObject jane = this.localModel.getObject(janeId);
        assertNotNull(jane);
        assertTrue(jane.hasField(cookiesId));
        assertFalse(jane.hasField(cakesId));
        assertEquals(jane.getRevisionNumber(), jane.getField(cookiesId).getRevisionNumber());
        assertTrue(this.localModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(this.localModel.getObject(DemoModelUtil.PETER_ID).hasField(cakesId));
        assertEquals(testModel.getObject(janeId).getRevisionNumber(),
                this.localModel.getObject(janeId).getRevisionNumber());
        assertTrue(testModel.getObject(DemoModelUtil.JOHN_ID).getRevisionNumber() < this.localModel
                .getObject(DemoModelUtil.JOHN_ID).getRevisionNumber());
        assertEquals(testModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(),
                this.localModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber());
        assertEquals(testModel.getObject(DemoModelUtil.PETER_ID).getField(cakesId)
                .getRevisionNumber(),
                this.localModel.getObject(DemoModelUtil.PETER_ID).getField(cakesId)
                        .getRevisionNumber());
        assertTrue(this.localModel.hasObject(bobId));
        assertEquals(testModel.getObject(bobId).getRevisionNumber(),
                this.localModel.getObject(bobId).getRevisionNumber());
        
        // check the remote model
        XReadableModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(XCompareUtils.equalState(this.localModel, remoteModel));
        
        // check that the correct events were sent
        SynchronizeTest.replaySyncEvents(startCopy, startEvents);
        assertTrue(XCompareUtils.equalTree(startCopy, this.localModel));
        
        SynchronizeTest.replaySyncEvents(midCopy, midEvents);
        assertTrue(XCompareUtils.equalTree(midCopy, this.localModel));
        
        checkEvents(this.localModel);
        
    }
    
    @Test
    public void testSendLocalChanges() {
        
        // Create a command manually.
        final XId frankId = XX.toId("Frank");
        XCommand command = MemoryModelCommand.createAddCommand(this.localModel.getAddress(), false,
                frankId);
        
        // Apply the command locally.
        assertTrue(this.localModel.executeCommand(command) >= 0);
        
        assertTrue(this.localModel.hasObject(frankId));
        
        // Now synchronize with the server.
        this.sync.startSync();
        
        // command may not be applied remotely yet!
        
        // We don't have to create all commands manually but can use a
        // ChangedModel.
        ChangedModel changedModel = new ChangedModel(this.localModel);
        
        // Make modifications to the changed model.
        final XId newfieldId = XX.toId("newField");
        changedModel.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
        assertTrue(changedModel.removeObject(DemoModelUtil.PETER_ID));
        
        // Create the command(s) describing the changes made to the
        // ChangedModel.
        XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.applyChanges(changedModel);
        XCommand autoCommand = tb.buildCommand();
        
        // We can also modify the model directly.
        final XId janeId = XX.toId("jane");
        this.localModel.createObject(janeId);
        
        // Now apply the command locally. It should be automatically
        // sent to the server.
        this.localModel.executeCommand(autoCommand);
        long finalRev = this.localModel.getRevisionNumber();
        
        assertTrue(this.localModel.hasObject(frankId));
        assertTrue(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        assertFalse(this.localModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(this.localModel.hasObject(janeId));
        
        this.sync.startSync();
        
        // both commands may still not be applied remotely
        
        // check model state
        assertTrue(this.localModel.hasObject(frankId));
        assertTrue(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        assertFalse(this.localModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(this.localModel.hasObject(janeId));
        assertTrue(XCompareUtils.equalState(this.localModel,
                loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID)));
        assertEquals(finalRev, this.localModel.getRevisionNumber());
        assertEquals(finalRev, this.localModel.getSynchronizedRevision());
        
        checkEvents(this.localModel);
        
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);
            
            NewSyncer synchronizer = createSyncer(this.remoteStore, model);
            
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);
            
            NewSyncer synchronizer = createSyncer(this.remoteStore, model);
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            NewSyncer synchronizer = createSyncer(this.remoteStore, model);
            
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            NewSyncer synchronizer = createSyncer(this.remoteStore, model);
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            NewSyncer synchronizer = createSyncer(this.remoteStore, model);
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            
            NewSyncer synchronizer = createSyncer(this.remoteStore, model);
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
            IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);
            
            XReadableModel snapshot = XCopyUtils.createSnapshot(model);
            
            synchronize(createSyncer(this.remoteStore, model));
            
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
