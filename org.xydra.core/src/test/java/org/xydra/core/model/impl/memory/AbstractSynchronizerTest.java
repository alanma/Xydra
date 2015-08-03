package org.xydra.core.model.impl.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
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
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.ChangeRecorder;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.HasChangedListener;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.index.iterator.Iterators;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.SynchronousCallbackWithOneResult;
import org.xydra.store.XydraStore;
import org.xydra.store.sync.NewSyncer;


/**
 * Test for {@link NewSyncer} and {@link XSynchronizesChanges} that uses an
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

    private static final Logger log = LoggerFactory.getLogger(AbstractSynchronizerTest.class);

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

    private static final XId MODEL_1_ID = XX.toId("newmodel1");

    private static final XId JOHN = XX.toId("john");

    private IMemoryModel localModel;

    private XAddress repoAddr;

    private NewSyncer sharedSyncer;

    {
        LoggerTestHelper.init();
    }

    /**
     * After synchronising, all events in a certain interval should be equal
     * between local and remote. This method checks ALL events.
     *
     * @param localModel
     */
    private void checkEvents(final XModel localModel) {

        final XChangeLog changeLog = localModel.getChangeLog();
        final long startRev = changeLog.getBaseRevisionNumber() + 1;
        log.info("Getting changes of model " + localModel.getAddress() + " since " + startRev);
        Iterator<XEvent> localEventsIt = changeLog.getEventsBetween(startRev, Long.MAX_VALUE);
        final ArrayList<XEvent> localEvents = new ArrayList<XEvent>();
        Iterators.addAll(localEventsIt, localEvents);
        log.info(" ... found changes of model " + localModel.getAddress() + " since " + startRev
                + " = " + localEvents.size());

        final SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.remoteStore.getEvents(this.actorId, this.passwordHash,
                new GetEventsRequest[] { new GetEventsRequest(localModel.getAddress(), startRev,
                        Long.MAX_VALUE) }, callback);
        final XEvent[] result = waitForSuccessBatched(callback);
        final List<XEvent> remoteEvents = Arrays.asList(result);

        log.debug("****** COMPARING");
        localEventsIt = localEvents.iterator();
        while(localEventsIt.hasNext()) {
            final XEvent xEvent = localEventsIt.next();
            log.debug("LOCAL : " + xEvent);
        }
        for(final XEvent remoteEvent : remoteEvents) {
            log.debug("REMOTE: " + remoteEvent);
        }
        log.debug("****** /COMPARING");

        localEventsIt = localEvents.iterator();
        for(final XEvent remoteEvent : remoteEvents) {
            log.debug("Remote event=" + remoteEvent + " checkign for local...");
            assertTrue("there should be localEvents if there are remoteEvents",
                    localEventsIt.hasNext());
            final XEvent localEvent = localEventsIt.next();
            log.debug("Local event =" + localEvent);
            assertEquals(remoteEvent.getRevisionNumber(), localEvent.getRevisionNumber());
            assertEquals(remoteEvent, localEvent);
        }
        assertFalse(localEventsIt.hasNext());
    }

    /**
     * Create a phonebook model with the given XId in the store.
     */
    private void createPhonebook(final XId modelId) {
        assert this.localModel == null;
        final XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(this.repoAddr,
                XCommand.SAFE_STATE_BOUND, modelId);
        executeCommandOnStore(createCommand);
        final XAddress modelAddr = createCommand.getChangedEntity();
        final XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
        DemoModelUtil.setupPhonebook(modelAddr, tb, false);
        final XTransaction trans = tb.build();
        // Apply events individually so there is something in the change log
        // to test
        for(final XAtomicCommand ac : trans) {
            executeCommandOnStore(ac);
        }
    }

    /**
     * Execute the given command on the store and check that there were no
     * errors.
     */
    private void executeCommandOnStore(final XCommand command) {
        SynchronousCallbackWithOneResult<BatchedResult<Long>[]> tc;
        tc = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();

        this.remoteStore.executeCommands(this.actorId, this.passwordHash,
                new XCommand[] { command }, tc);

        final long res = waitForSuccessBatched(tc);

        assertTrue("Should not have failed (" + res + "): " + command, res != XCommand.FAILED);
    }

    private IMemoryModel loadModel(final XId modelId) {

        final XReadableModel modelSnapshot = loadModelSnapshot(modelId);
        assertNotNull("model " + modelId + " was null", modelSnapshot);

        return (IMemoryModel)XX.wrap(this.actorId, this.passwordHash, modelSnapshot);
    }

    private XReadableModel loadModelSnapshot(final XId modelId) {
        final XAddress modelAddr = Base.resolveModel(this.repoAddr, modelId);

        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> tc;
        tc = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        this.remoteStore.getModelSnapshots(this.actorId, this.passwordHash,
                new GetWithAddressRequest[] { new GetWithAddressRequest(modelAddr, false) }, tc);

        return waitForSuccessBatched(tc);
    }

    private void removeModel(final XId modelId) {
        XyAssert.xyAssert(this.repoAddr != null, "this.repoAddr != null");
        assert this.repoAddr != null;
        executeCommandOnStore(MemoryRepositoryCommand.createRemoveCommand(this.repoAddr,
                XCommand.FORCED, modelId));
        // TODO here was a problem with the test design: variable localModel was
        // not held in sync
        this.localModel = null;
    }

    @Before
    public void setUp() {

        this.remoteStore = createStore();

        assertNotNull(this.actorId);
        assertNotNull(this.passwordHash);
        assertNotNull(this.remoteStore);

        // check login
        final SynchronousCallbackWithOneResult<Boolean> loginCallback = new SynchronousCallbackWithOneResult<Boolean>();
        this.remoteStore.checkLogin(this.actorId, this.passwordHash, loginCallback);
        assertTrue(waitForSuccess(loginCallback));

        // get repository address
        final SynchronousCallbackWithOneResult<XId> repoIdCallback = new SynchronousCallbackWithOneResult<XId>();
        this.remoteStore.getRepositoryId(this.actorId, this.passwordHash, repoIdCallback);
        final XId repoId = waitForSuccess(repoIdCallback);
        assertNotNull(repoId);
        this.repoAddr = Base.toAddress(repoId, null, null, null);

        createPhonebook(DemoModelUtil.PHONEBOOK_ID);
        XyAssert.xyAssert(this.repoAddr != null);
        assert this.repoAddr != null;

        this.localModel = loadModel(DemoModelUtil.PHONEBOOK_ID);
        assertEquals(46, this.localModel.getRevisionNumber());
        assertEquals(46, this.localModel.getSynchronizedRevision());

        this.sharedSyncer = createSyncer(this.remoteStore, this.localModel);
    }

    private NewSyncer createSyncer(final XydraStore store, final IMemoryModel model) {
        return new NewSyncer(store, model, model.getState(), model.getRoot(), this.actorId,
                this.passwordHash, model.getSynchronizedRevision());
    }

    /**
     * Synchronize and check that there were no errors.
     */
    private static void synchronize(final NewSyncer sync) {
        sync.startSync(null);
    }

    @After
    public void tearDown() {
        removeModel(DemoModelUtil.PHONEBOOK_ID);
    }

    /**
     * TODO create more realistic sync tests (shorter ones, too)
     */
    @Test
    public void testCreateRemoveModel() {
        try {
            /* Sync 1: Create model locally and sync */
            assertNull("initially, model should not exist", loadModelSnapshot(MODEL_1_ID));

            final XRepository localRepo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            {
                final IMemoryModel localNewBobModel = (IMemoryModel)localRepo.createModel(MODEL_1_ID);
                assertEquals("so far, nothing synchronized ever", -1,
                        localNewBobModel.getSynchronizedRevision());
                assertEquals("local model was created, so rev=0", 0,
                        localNewBobModel.getRevisionNumber());

                // add changes
                final XObject localObject = localNewBobModel.createObject(BOB);
                final XField localField = localObject.createField(COOKIES);
                localField.setValue(COOKIES_YUMMY);
                long localModelRev = localNewBobModel.getRevisionNumber();
                assertEquals(-1, localNewBobModel.getSynchronizedRevision());
                assertEquals(3, localNewBobModel.getRevisionNumber());
                assertEquals(4, localNewBobModel.getRoot().getSyncLog().getSize());

                final HasChangedListener hasChanged1 = HasChangedListener.listen(localNewBobModel);
                final XReadableModel localNewModel_beforeSync = XCopyUtils
                        .createSnapshot(localNewBobModel);

                // check remote before sync
                XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
                assertNull("remoteModel should be null", remoteModel);

                log.info("------------- Syncing 1............");
                final NewSyncer syncer = createSyncer(this.remoteStore, localNewBobModel);
                synchronize(syncer);
                log.info("............. Syncing 1 done.");

                /*
                 * local model should remain unchanged, there are no changes
                 * from server
                 */
                assertEquals(localModelRev, localNewBobModel.getRevisionNumber());
                assertTrue(XCompareUtils.equalState(localNewBobModel, localNewModel_beforeSync));
                assertEquals("same synclog size", 4, localNewBobModel.getRoot().getSyncLog()
                        .getSize());
                assertEquals("higher syncrev now", 3, localNewBobModel.getSynchronizedRevision());

                /* remote model should be updated */
                remoteModel = loadModelSnapshot(MODEL_1_ID);
                assertNotNull("remoteModel should not be null", remoteModel);
                assertTrue(XCompareUtils.equalState(localNewBobModel, remoteModel));
                assertFalse(hasChanged1.hasEventsReceived());

                checkEvents(localNewBobModel);

                /* Sync 2: Do local changes, then delete model */

                // check that the local model still works
                localNewBobModel.createObject(JANE);
                assertEquals(4, localNewBobModel.getRevisionNumber());

                localRepo.removeModel(MODEL_1_ID);
                assertEquals(5, localNewBobModel.getRevisionNumber());
                localModelRev = localNewBobModel.getRevisionNumber();
                hasChanged1.reset();

                System.out.println("------------- Syncing 2............");
                synchronize(syncer);
                System.out.println("............. Syncing 2 done.");

                assertNull(loadModelSnapshot(MODEL_1_ID));
                assertFalse(localRepo.hasModel(MODEL_1_ID));
                assertEquals(localModelRev, localNewBobModel.getRevisionNumber());
                assertEquals(localModelRev, localNewBobModel.getSynchronizedRevision());
                assertFalse(hasChanged1.hasEventsReceived());

                checkEvents(localNewBobModel);

                // check that local model is removed
                try {
                    // this fails
                    localNewBobModel.createObject(JANE);
                    fail();
                } catch(final IllegalStateException ise) {
                    // worked
                }

                remoteModel = loadModelSnapshot(MODEL_1_ID);
                assertNull(remoteModel);
            }

            {
                /*
                 * Sync 3: Re-create the same model locally (server still has
                 * all the history); this time add different content 'john'
                 */
                final IMemoryModel localNewJohnModel = (IMemoryModel)localRepo.createModel(MODEL_1_ID);
                assertEquals(0, localNewJohnModel.getRevisionNumber());
                localNewJohnModel.createObject(JOHN);
                final long localModelRev = localNewJohnModel.getRevisionNumber();
                assertEquals(1, localModelRev);
                final HasChangedListener hasChanges2 = HasChangedListener.listen(localNewJohnModel);
                final XExistsRevWritableModel localNewModel_beforeSync = XCopyUtils
                        .createSnapshot(localNewJohnModel);
                assertEquals(1, localNewModel_beforeSync.getRevisionNumber());

                XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
                assertNull(remoteModel);

                System.out.println("------------- Syncing 3............");
                // we have a new syncer, so we sync from begin of time
                final NewSyncer syncer = createSyncer(this.remoteStore, localNewJohnModel);
                synchronize(syncer);
                System.out.println("............. Syncing 3 done.");

                remoteModel = loadModelSnapshot(MODEL_1_ID);
                assertNotNull(remoteModel);
                assertEquals(7, remoteModel.getRevisionNumber());
                assertEquals(7, localNewJohnModel.getRevisionNumber());

                assertEquals(localNewJohnModel.getRevisionNumber(),
                        localNewJohnModel.getSynchronizedRevision());

                log.info("Comparing state");
                assertTrue(XCompareUtils.equalTree(localNewJohnModel, localNewModel_beforeSync));
                assertTrue("revNrs should match",
                        XCompareUtils.equalState(localNewJohnModel, remoteModel));

                assertFalse("nothing happened on the server what is not yet happened locally",
                        hasChanges2.hasEventsReceived());

                checkEvents(localNewJohnModel);

                // check that the local model still works
                localNewJohnModel.createObject(JANE);
            }

        } finally {
            log.info("*********** Test done, finally remove model " + MODEL_1_ID);
            removeModel(MODEL_1_ID);
        }

    }

    static final XId BOB = XX.toId("Bob");
    static final XId JANE = XX.toId("Jane");
    static final XId COOKIES = XX.toId("cookies");
    static final XValue COOKIES_GONE = XV.toValue("gone");
    static final XValue COOKIES_YUMMY = XV.toValue("yummy");

    @Test
    public void testDoSafeTxn() {
        final XCommand command = MemoryModelCommand.createAddCommand(this.localModel.getAddress(), false,
                BOB);
        executeCommandOnStore(command);

        final XAddress janeAddr = Base.resolveObject(this.localModel.getAddress(), JANE);
        final XAddress cookiesAddr = Base.resolveField(janeAddr, COOKIES);

        final XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, JANE);
        tb.addField(janeAddr, XCommand.SAFE_STATE_BOUND, COOKIES);
        tb.addValue(cookiesAddr, XCommand.SAFE_STATE_BOUND, COOKIES_GONE);

        executeCommandOnStore(tb.buildCommand());
    }

    @Test
    public void testLoadRemoteChanges() {
        final XModel modelCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        assertEquals(46, modelCopy.getRevisionNumber());
        assertEquals(46, modelCopy.getSynchronizedRevision());

        final List<XEvent> events = ChangeRecorder.record(this.localModel);
        assertTrue(XCompareUtils.equalState(modelCopy, this.localModel));

        // make remote changes
        final XCommand command = MemoryModelCommand.createAddCommand(this.localModel.getAddress(), false,
                BOB);
        executeCommandOnStore(command);
        // make more remote changes
        final XAddress janeAddr = Base.resolveObject(this.localModel.getAddress(), JANE);
        final XAddress cookiesAddr = Base.resolveField(janeAddr, COOKIES);
        final XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, JANE);
        tb.addField(janeAddr, XCommand.SAFE_STATE_BOUND, COOKIES);
        tb.addValue(cookiesAddr, XCommand.SAFE_STATE_BOUND, COOKIES_GONE);
        final XCommand txn = tb.buildCommand();
        executeCommandOnStore(txn);
        final XReadableModel remoteSnapshotModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertEquals(48, remoteSnapshotModel.getRevisionNumber());

        // local model did not change
        assertEquals(46, this.localModel.getRevisionNumber());
        assertEquals(46, this.localModel.getSynchronizedRevision());

        // synchronize
        synchronize(this.sharedSyncer);

        // check the local model got the changes
        assertEquals(48, this.localModel.getRevisionNumber());
        assertEquals(48, this.localModel.getSynchronizedRevision());
        assertTrue(this.localModel.hasObject(BOB));
        final XObject jane = this.localModel.getObject(JANE);
        assertNotNull(jane);
        final XField cookies = jane.getField(COOKIES);
        assertNotNull(cookies);
        assertEquals(COOKIES_GONE, cookies.getValue());
        assertEquals(jane.getRevisionNumber(), cookies.getRevisionNumber());
        assertEquals(this.localModel.getRevisionNumber(), jane.getRevisionNumber());
        assertTrue(XCompareUtils.equalState(remoteSnapshotModel, this.localModel));

        // check the remote model
        final XReadableModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(XCompareUtils.equalState(remoteSnapshotModel, remoteModel));

        // check that the correct events were sent
        SynchronizeTest.replaySyncEvents(modelCopy, events);
        assertTrue(XCompareUtils.equalTree(modelCopy, this.localModel));

        checkEvents(this.localModel);
    }

    @Test
    public void testLoadRemoteChangesRemovedCreatedModel() {

        try {

            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a model
            final XRepository repo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            final IMemoryModel model = (IMemoryModel)repo.createModel(MODEL_1_ID);
            final NewSyncer sync = createSyncer(this.remoteStore, model);
            synchronize(sync);
            final IMemoryModel modelCopy = loadModel(MODEL_1_ID);
            assertTrue(XCompareUtils.equalState(model, modelCopy));
            final long modelRev = model.getRevisionNumber();
            final HasChangedListener hc2 = HasChangedListener.listen(model);
            final HasChangedListener hc3 = HasChangedListener.listen(modelCopy);
            checkEvents(model);

            removeModel(MODEL_1_ID);
            assertNull(loadModelSnapshot(MODEL_1_ID));

            // now re-create the model, with some content
            final XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
                    this.repoAddr, XCommand.SAFE_STATE_BOUND, MODEL_1_ID);
            executeCommandOnStore(createCommand);
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);

            // test synchronizing the model with repository
            final HasChangedListener hc1 = new HasChangedListener();
            repo.addListenerForModelEvents(hc1);
            synchronize(sync);
            assertTrue(repo.hasModel(MODEL_1_ID));
            assertEquals(modelRev + 2, model.getRevisionNumber());
            assertEquals(modelRev + 2, model.getSynchronizedRevision());
            assertFalse(hc1.hasEventsReceived());
            assertFalse(hc2.hasEventsReceived());
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);
            // test that the model is not removed
            model.createObject(Base.toId("jane"));

            // test synchronizing the model without repository
            final NewSyncer sync2 = createSyncer(this.remoteStore, modelCopy);
            synchronize(sync2);
            assertEquals(modelRev + 2, modelCopy.getRevisionNumber());
            assertEquals(modelRev + 2, modelCopy.getSynchronizedRevision());
            assertFalse(hc3.hasEventsReceived());
            assertTrue(XCompareUtils.equalState(modelCopy, remoteModel));
            checkEvents(modelCopy);
            // test that the model is not removed
            modelCopy.createObject(Base.toId("jane"));

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    @Test
    public void testLoadRemoteChangesRemovedThenCreatedModel() {

        try {

            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a model
            final XRepository repo = new MemoryRepository(this.actorId, this.passwordHash,
                    this.repoAddr.getRepository());
            final IMemoryModel model = (IMemoryModel)repo.createModel(MODEL_1_ID);
            final XObject object = model.createObject(Base.toId("bob"));
            final XField field = object.createField(Base.toId("cookies"));
            field.setValue(XV.toValue("yummy"));
            final NewSyncer sync = createSyncer(this.remoteStore, model);
            synchronize(sync);
            final IMemoryModel modelCopy = loadModel(MODEL_1_ID);
            assertTrue(XCompareUtils.equalState(model, modelCopy));
            final long modelRev = model.getRevisionNumber();
            final HasChangedListener hc3 = HasChangedListener.listen(model);
            final HasChangedListener hc4 = HasChangedListener.listen(modelCopy);
            checkEvents(model);

            removeModel(MODEL_1_ID);
            assertNull(loadModelSnapshot(MODEL_1_ID));
            /* at this point there is no "newModel1" at the remote repository */

            // test synchronizing the model with repository
            final HasChangedListener hc1 = new HasChangedListener();
            repo.addListenerForModelEvents(hc1);
            synchronize(sync);
            /*
             * at this point there should be no "newModel1 at the local
             * MemoryRepository
             */
            assertFalse(repo.hasModel(MODEL_1_ID));
            assertEquals(modelRev + 1, model.getRevisionNumber());
            assertEquals(modelRev + 1, model.getSynchronizedRevision());
            assertTrue(hc1.hasEventsReceived());
            assertTrue(hc3.hasEventsReceived());
            checkEvents(model);
            try {
                model.createObject(Base.toId("jane"));
                fail();
            } catch(final IllegalStateException ise) {
                // worked
            }

            // test synchronizing the model without repository
            final NewSyncer sync2 = createSyncer(this.remoteStore, modelCopy);
            synchronize(sync2);
            assertEquals(modelRev + 1, modelCopy.getRevisionNumber());
            assertEquals(modelRev + 1, modelCopy.getSynchronizedRevision());
            assertTrue(hc4.hasEventsReceived());
            checkEvents(modelCopy);
            try {
                modelCopy.createObject(Base.toId("jane"));
                fail();
            } catch(final IllegalStateException ise) {
                // worked
            }

            // now re-create the model, with some content
            createPhonebook(MODEL_1_ID);
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);

            // test synchronizing the model with repository
            final HasChangedListener hc2 = new HasChangedListener();
            repo.addListenerForModelEvents(hc2);
            hc3.reset();
            synchronize(sync);
            assertTrue(repo.hasModel(MODEL_1_ID));
            assertTrue(hc2.hasEventsReceived());
            assertTrue(hc3.hasEventsReceived());
            checkEvents(model);
            // test that the model is not removed
            model.createObject(Base.toId("jane"));

            // test synchronizing the model without repository
            hc4.reset();
            synchronize(sync2);
            assertTrue(hc4.hasEventsReceived());
            checkEvents(modelCopy);
            // test that the model is not removed
            modelCopy.createObject(Base.toId("jane"));

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    @Test
    public void testMergeChanges() {

        final XModel startCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        final List<XEvent> startEvents = ChangeRecorder.record(this.localModel);
        assertTrue(XCompareUtils.equalState(startCopy, this.localModel));

        final XModel startCopy2 = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        assertTrue(XCompareUtils.equalState(startCopy2, this.localModel));

        // make some remote changes
        final XId cakesId = Base.toId("cakes");
        final XCommand command = MemoryObjectCommand.createAddCommand(
                this.localModel.getObject(DemoModelUtil.JOHN_ID).getAddress(), false, cakesId);
        executeCommandOnStore(command);

        // make more remote changes
        final XId janeId = Base.toId("Jane");
        final XId cookiesId = Base.toId("cookies");
        final XValue cookiesValue = XV.toValue("gone");
        final XAddress janeAddr = Base.resolveObject(this.localModel.getAddress(), janeId);
        final XAddress cookiesAddr = Base.resolveField(janeAddr, cookiesId);
        final XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, janeId);
        tb.addField(janeAddr, XCommand.SAFE_STATE_BOUND, cookiesId);
        tb.addValue(cookiesAddr, XCommand.SAFE_STATE_BOUND, cookiesValue);
        executeCommandOnStore(tb.buildCommand());

        // and some more remote changes
        final XCommand command2 = MemoryObjectCommand.createAddCommand(
                this.localModel.getObject(DemoModelUtil.PETER_ID).getAddress(), false, cakesId);
        executeCommandOnStore(command2);

        // and even more remote changes
        final XId bobId = Base.toId("Bob");
        final XCommand command4 = MemoryModelCommand.createAddCommand(this.localModel.getAddress(),
                false, bobId);
        executeCommandOnStore(command4);

        // also make some local changes
        // should be reverted on sync because of conflicting remote changes
        final XTransactionBuilder tb2 = new XTransactionBuilder(this.localModel.getAddress());
        tb2.addObject(this.localModel.getAddress(), XCommand.SAFE_STATE_BOUND, janeId);
        tb2.addField(janeAddr, XCommand.SAFE_STATE_BOUND, cakesId);
        assertTrue(this.localModel.executeCommand(tb2.buildCommand()) >= 0);
        // should survive the sync
        final XId newfieldId = Base.toId("newField");
        this.localModel.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
        // TODO test: should be reverted on sync
        final XCommand command3 = MemoryModelCommand.createRemoveCommand(this.localModel.getAddress(),
                this.localModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(),
                DemoModelUtil.PETER_ID);
        assertTrue(this.localModel.executeCommand(command3) >= 0);
        // TODO test: should sync to XCommand#NOCHANGE
        final XCommand command5 = MemoryModelCommand.createAddCommand(this.localModel.getAddress(),
                false, bobId);
        assertTrue(this.localModel.executeCommand(command5) >= 0);

        // check the local model
        final XObject localJane = this.localModel.getObject(janeId);
        assertNotNull(localJane);
        assertTrue(localJane.hasField(cakesId));
        assertFalse(localJane.hasField(cookiesId));
        assertEquals(localJane.getRevisionNumber(), localJane.getField(cakesId).getRevisionNumber());
        assertFalse(this.localModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        assertFalse(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(cakesId));
        assertTrue(this.localModel.hasObject(bobId));

        // check the remote model
        final XReadableModel testModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
        assertTrue(testModel.getObject(DemoModelUtil.JOHN_ID).hasField(cakesId));
        assertFalse(testModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        final XReadableObject remoteJane = testModel.getObject(janeId);
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

        final XModel midCopy = XCopyUtils.copyModel(this.actorId, this.passwordHash, this.localModel);
        final List<XEvent> midEvents = ChangeRecorder.record(this.localModel);
        assertTrue(XCompareUtils.equalState(midCopy, this.localModel));

        // synchronize
        synchronize(this.sharedSyncer);

        // check local model
        final XObject jane = this.localModel.getObject(janeId);
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
        final XReadableModel remoteModel = loadModelSnapshot(DemoModelUtil.PHONEBOOK_ID);
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
        final XId frankId = Base.toId("Frank");
        final XCommand command = MemoryModelCommand.createAddCommand(this.localModel.getAddress(), false,
                frankId);

        // Apply the command locally.
        assertTrue(this.localModel.executeCommand(command) >= 0);

        assertTrue(this.localModel.hasObject(frankId));

        // Now synchronize with the server.
        this.sharedSyncer.startSync(null);

        // command may not be applied remotely yet!

        // We don't have to create all commands manually but can use a
        // ChangedModel.
        final ChangedModel changedModel = new ChangedModel(this.localModel);

        // Make modifications to the changed model.
        final XId newfieldId = Base.toId("newField");
        changedModel.getObject(DemoModelUtil.JOHN_ID).createField(newfieldId);
        assertTrue(changedModel.removeObject(DemoModelUtil.PETER_ID));

        // Create the command(s) describing the changes made to the
        // ChangedModel.
        final XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
        tb.applyChanges(changedModel);
        final XCommand autoCommand = tb.buildCommand();

        // We can also modify the model directly.
        final XId janeId = Base.toId("jane");
        this.localModel.createObject(janeId);

        // Now apply the command locally. It should be automatically
        // sent to the server.
        this.localModel.executeCommand(autoCommand);
        final long finalRev = this.localModel.getRevisionNumber();

        assertTrue(this.localModel.hasObject(frankId));
        assertTrue(this.localModel.getObject(DemoModelUtil.JOHN_ID).hasField(newfieldId));
        assertFalse(this.localModel.hasObject(DemoModelUtil.PETER_ID));
        assertTrue(this.localModel.hasObject(janeId));

        this.sharedSyncer.startSync(null);

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
            // before
            assertNull(loadModelSnapshot(MODEL_1_ID));

            /* Create local model; add phonebook; */
            // must be created with a repository ID to be synchronized later
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            final IMemoryModel localModel = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(localModel);

            final NewSyncer synchronizer = createSyncer(this.remoteStore, localModel);

            // create txn
            XTransactionBuilder txBuilder = new XTransactionBuilder(localModel.getAddress());
            ChangedModel cm = new ChangedModel(localModel);
            XWritableObject johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(XV.toValue("Title-1"));
            johnObject.getField(DemoModelUtil.ALIASES_ID).setValue(
                    XV.toValue(new String[] { "Ali-A-1", "Ali-B-1", "Ali-C-1" }));
            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();

            // execute txn locally
            long result = localModel.executeCommand(tx);
            assertTrue(XCommandUtils.success(result));

            // build txn2
            txBuilder = new XTransactionBuilder(localModel.getAddress());
            cm = new ChangedModel(localModel);
            johnObject = cm.getObject(DemoModelUtil.JOHN_ID);
            johnObject.getField(DemoModelUtil.TITLE_ID).setValue(XV.toValue("Title-2"));
            johnObject.getField(DemoModelUtil.ALIASES_ID).setValue(
                    XV.toValue(new String[] { "Ali-A-2", "Ali-B-2", "Ali-C-2" }));
            txBuilder.applyChanges(cm);
            tx = txBuilder.build();

            // execute txn2 locally
            result = localModel.executeCommand(tx);
            assertTrue(XCommandUtils.success(result));
            assertEquals("Title-2",
                    localModel.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.TITLE_ID)
                            .getValue().toString());

            final XReadableModel localModel_t1 = XCopyUtils.createSnapshot(localModel);
            assertEquals("Title-2",
                    localModel_t1.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.TITLE_ID)
                            .getValue().toString());
            assertEquals("Title-2",
                    localModel.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.TITLE_ID)
                            .getValue().toString());

            log.info("****** Sync");
            synchronize(synchronizer);

            assertEquals("Title-2",
                    localModel_t1.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.TITLE_ID)
                            .getValue().toString());
            assertEquals("Title-2",
                    localModel.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.TITLE_ID)
                            .getValue().toString());
            assertTrue("syncing to a remote repo should not affect local state",
                    XCompareUtils.equalTree(localModel_t1, localModel));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull("remote model " + MODEL_1_ID + " should now exist", remoteModel);
            assertEquals("Title-2",
                    remoteModel.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.TITLE_ID)
                            .getValue().toString());
            assertTrue("remoteModel should have gotten state from local model",
                    XCompareUtils.equalState(localModel, remoteModel));
            checkEvents(localModel);

        } finally {
            log.info("******** finally, cleaning up");
            removeModel(MODEL_1_ID);
        }

    }

    /**
     * A test to replicate the observed client sync failures during fixCommands,
     * the difference to the previous test is that this test initially sync with
     * the server.
     */
    @Test
    public void testSyncModelCreatedWithoutRepositoryReplicateClientWithInitialSync() {

        try {

            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a local model
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            // must be created with a repository ID to be synchronized
            final IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);

            final NewSyncer synchronizer = createSyncer(this.remoteStore, model);
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

            final XReadableModel snapshot = XCopyUtils.createSnapshot(model);

            synchronize(synchronizer);

            assertTrue(XCompareUtils.equalTree(snapshot, model));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClient() {

        try {
            removeModel(MODEL_1_ID);
            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a local model
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            // must be created with a repository ID to be synchronized
            final IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);

            final NewSyncer synchronizer = createSyncer(this.remoteStore, model);

            XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());

            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.createObject(DemoModelUtil.JOHN_ID);
            final XWritableField titleField = johnObject.createField(DemoModelUtil.TITLE_ID);
            titleField.setValue(XV.toValue("A new title"));
            final XWritableField aliasesField = johnObject.createField(DemoModelUtil.ALIASES_ID);

            aliasesField.setValue(XV.toValue(new String[] { "Decoupled distributed systems",
                    "Fries", "Bacon" }));

            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();

            model.executeCommand(tx);

            txBuilder = new XTransactionBuilder(model.getAddress(), true);

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

            final XReadableModel snapshot = XCopyUtils.createSnapshot(model);

            synchronize(synchronizer);

            assertTrue(XCompareUtils.equalTree(snapshot, model));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    /**
     * A minimal test to replicate the observed client sync failures during
     * fixCommands. No txn used.
     */
    @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClientWithInitialSyncNoTxn() {

        try {

            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a local model
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            // must be created with a repository ID to be synchronized
            final IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);

            final NewSyncer synchronizer = createSyncer(this.remoteStore, model);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);

            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.createObject(DemoModelUtil.JOHN_ID);
            final XWritableField titleField = johnObject.createField(DemoModelUtil.TITLE_ID);
            titleField.setValue(XV.toValue("A new title"));
            final XWritableField aliasesField = johnObject.createField(DemoModelUtil.ALIASES_ID);

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

            final XReadableModel snapshot = XCopyUtils.createSnapshot(model);

            synchronize(synchronizer);

            assertTrue(XCompareUtils.equalTree(snapshot, model));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    /**
     * A minimal test to replicate the observed client sync failures during
     * fixCommands. No txn used.
     */
    @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClientWithInitialSyncNoTxnSafeCmd() {

        try {

            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a local model
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            // must be created with a repository ID to be synchronized
            final IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);

            final NewSyncer synchronizer = createSyncer(this.remoteStore, model);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);

            final XModelCommand cmd1 = BaseRuntime.getCommandFactory().createSafeAddObjectCommand(modelAddr,
                    DemoModelUtil.JOHN_ID);
            model.executeCommand(cmd1);

            final XObjectCommand cmd2 = BaseRuntime.getCommandFactory().createSafeAddFieldCommand(
                    Base.resolveObject(modelAddr, DemoModelUtil.JOHN_ID), DemoModelUtil.TITLE_ID);
            model.executeCommand(cmd2);

            final XWritableObject johnObject = model.getObject(DemoModelUtil.JOHN_ID);

            XWritableField titleField = johnObject.getField(DemoModelUtil.TITLE_ID);
            final XFieldCommand cmd3 = BaseRuntime.getCommandFactory().createSafeAddValueCommand(
                    Base.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.TITLE_ID),

                    titleField.getRevisionNumber(), XV.toValue("A new title"));
            model.executeCommand(cmd3);

            final XObjectCommand cmd4 = BaseRuntime.getCommandFactory().createSafeAddFieldCommand(
                    Base.resolveObject(modelAddr, DemoModelUtil.JOHN_ID), DemoModelUtil.ALIASES_ID);
            model.executeCommand(cmd4);

            final XWritableField aliasesField = johnObject.getField(DemoModelUtil.ALIASES_ID);
            final XFieldCommand cmd5 = BaseRuntime.getCommandFactory().createSafeAddValueCommand(
                    Base.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.ALIASES_ID),

                    aliasesField.getRevisionNumber(),
                    XV.toValue(new String[] { "Decoupled distributed systems", "Fries", "Bacon" }));
            model.executeCommand(cmd5);

            titleField = johnObject.getField(DemoModelUtil.TITLE_ID);
            final long revBefore = titleField.getRevisionNumber();

            final XFieldCommand cmd6 = BaseRuntime.getCommandFactory().createSafeChangeValueCommand(
                    Base.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.TITLE_ID),

                    titleField.getRevisionNumber(), XV.toValue("A brand new title"));
            final long result = model.executeCommand(cmd6);
            assertTrue(XCommandUtils.success(result));

            titleField = johnObject.getField(DemoModelUtil.TITLE_ID);
            final long revAfter = titleField.getRevisionNumber();
            assertTrue(revBefore != revAfter);

            final XFieldCommand cmd7 = BaseRuntime.getCommandFactory().createSafeChangeValueCommand(
                    Base.resolveField(modelAddr.getRepository(), modelAddr.getModel(),
                            DemoModelUtil.JOHN_ID, DemoModelUtil.ALIASES_ID),

                    aliasesField.getRevisionNumber(),
                    XV.toValue(new String[] { "Highly decoupled distributed systems", "Fries",
                            "Bacon" }));
            model.executeCommand(cmd7);

            final XReadableModel snapshot = XCopyUtils.createSnapshot(model);

            synchronize(synchronizer);

            assertTrue(XCompareUtils.equalTree(snapshot, model));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    /**
     * A minimal test to replicate the observed client sync failures during
     * fixCommands.
     */
    @Test
    public void testSyncModelCreatedWithoutRepositoryMinimalReplicateClientWithInitialSyncWithTxn() {

        try {
            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a local model
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            // must be created with a repository ID to be synchronized
            final IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);

            final NewSyncer synchronizer = createSyncer(this.remoteStore, model);
            // The intitial sync is the only difference to the previous test and
            // makes this test fail. Why?
            synchronize(synchronizer);

            XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());

            ChangedModel cm = new ChangedModel(model);
            XWritableObject johnObject = cm.createObject(DemoModelUtil.JOHN_ID);
            final XWritableField titleField = johnObject.createField(DemoModelUtil.TITLE_ID);
            titleField.setValue(XV.toValue("A new title"));
            final XWritableField aliasesField = johnObject.createField(DemoModelUtil.ALIASES_ID);

            aliasesField.setValue(XV.toValue(new String[] { "Decoupled distributed systems",
                    "Fries", "Bacon" }));

            txBuilder.applyChanges(cm);
            XTransaction tx = txBuilder.build();

            model.executeCommand(tx);

            txBuilder = new XTransactionBuilder(model.getAddress(), true);

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

            final XReadableModel snapshot = XCopyUtils.createSnapshot(model);

            synchronize(synchronizer);

            assertTrue(XCompareUtils.equalTree(snapshot, model));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    @Test
    public void testSyncModelCreatedWithoutRepository() {

        try {

            assertNull(loadModelSnapshot(MODEL_1_ID));

            // create a local model
            final XAddress modelAddr = Base.resolveModel(this.repoAddr, MODEL_1_ID);
            // must be created with a repository ID to be synchronized
            final IMemoryModel model = new MemoryModel(this.actorId, this.passwordHash, modelAddr);
            DemoModelUtil.setupPhonebook(model);

            final XReadableModel snapshot = XCopyUtils.createSnapshot(model);

            synchronize(createSyncer(this.remoteStore, model));

            assertTrue(XCompareUtils.equalTree(snapshot, model));
            final XReadableModel remoteModel = loadModelSnapshot(MODEL_1_ID);
            assertNotNull(remoteModel);
            assertTrue(XCompareUtils.equalState(model, remoteModel));
            checkEvents(model);

        } finally {
            removeModel(MODEL_1_ID);
        }

    }

    /**
     * Wait for the given callback and check that there were no errors.
     *
     * TODO arch: this could also be of use to other store tests
     *
     * @return the result passed to the callback.
     */
    private static <T> T waitForSuccess(final SynchronousCallbackWithOneResult<T> tc) {

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
            final SynchronousCallbackWithOneResult<BatchedResult<T>[]> tc) {

        final BatchedResult<T>[] results = waitForSuccess(tc);
        assertNotNull(results);
        assertEquals(1, results.length);
        final BatchedResult<T> result = results[0];
        assertNotNull(result);
        assertNull(result.toString(), result.getException());
        return result.getResult();
    }

}
