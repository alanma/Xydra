package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;


/**
 * Abstract test class for classes implementing the {@link XydraStore}
 * interface.
 *
 *
 * This test assumes that this test alone (no other threads) operates on the
 * {@link XydraStore} that is being tested. Some methods may fail if
 * someone/something else operates on the same {@link XydraStore} at the same
 * time, even though the {@link XydraStore} is working correctly.
 *
 * @author kaidel
 */
public abstract class AbstractStoreReadMethodsTest extends AbstractStoreTest {

    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractStoreReadMethodsTest.class);
    private XId correctUser, incorrectUser;

    protected String correctUserPass, incorrectUserPass;
    protected XCommandFactory factory;
    protected boolean incorrectActorExists = true;
    protected GetWithAddressRequest[] modelAddressRequests;
    protected GetWithAddressRequest notExistingModelRequest;

    protected GetWithAddressRequest notExistingObjectRequest;
    protected GetWithAddressRequest[] objectAddressRequests;
    protected XydraStore store;

    @Override
	@After
    public void tearDown() {
        super.tearDown();
    }

    @Override
	@Before
    public void setUp() {
        super.setUp();
        this.store = createStore();
        this.factory = getCommandFactory();

        if(this.store == null) {
            throw new RuntimeException("XydraStore could not be initalized in the setUp method!");
        }
        if(this.factory == null) {
            throw new RuntimeException(
                    "XCommandFactory could not be initalized in the setUp method!");
        }

        this.correctUser = getCorrectUser();
        this.correctUserPass = getCorrectUserPasswordHash();

        if(this.correctUser == null || this.correctUserPass == null) {
            throw new IllegalArgumentException("correctUser or correctUserPass were null");
        }

        this.incorrectUser = getIncorrectUser();
        this.incorrectUserPass = getIncorrectUserPasswordHash();
        this.incorrectActorExists = this.incorrectUser != null;

        // creating some models
        final XId modelId1 = Base.toId("TestModel1");
        final XId modelId2 = Base.toId("TestModel2");
        final XId modelId3 = Base.toId("TestModel3");

        final SynchronousCallbackWithOneResult<Set<XId>> callback = new SynchronousCallbackWithOneResult<Set<XId>>();
        this.store.getModelIds(this.correctUser, this.correctUserPass, callback);
        waitOnCallback(callback);
        XyAssert.xyAssert(callback.effect != null, "callback.effect != null");
        assert callback.effect != null;
        XyAssert.xyAssert(!callback.effect.contains(modelId1));

        final XId objectId1 = Base.toId("TestObject1");
        final XId objectId2 = Base.toId("TestObject2");
        final XId objectId3 = Base.toId("TestObject3");

        /*
         * TODO auth: In a secure store you need to give the correctUser the
         * rights to access these models and objects -
         */
        /*
         * Comment by me: this should not be done by this abstract test, but
         * rather by the implementation. As stated in the documentation of the
         * "getCorrectUser" method, the test assumes that the user returned by
         * this method is allowed to execute the following commands ~Bjoern
         */

        final XId repoID = getRepositoryId();

        final XCommand modelCommand1 = this.factory.createAddModelCommand(repoID, modelId1, true);
        final XCommand modelCommand2 = this.factory.createAddModelCommand(repoID, modelId2, true);
        final XCommand modelCommand3 = this.factory.createAddModelCommand(repoID, modelId3, true);

        final XCommand objectCommand1 = this.factory.createAddObjectCommand(
                Base.resolveModel(repoID, modelId1), objectId1, true);
        final XCommand objectCommand2 = this.factory.createAddObjectCommand(
                Base.resolveModel(repoID, modelId1), objectId2, true);
        final XCommand objectCommand3 = this.factory.createAddObjectCommand(
                Base.resolveModel(repoID, modelId1), objectId3, true);

        final XCommand[] commands = { modelCommand1, modelCommand2, modelCommand3, objectCommand1,
                objectCommand2, objectCommand3 };
        final SynchronousCallbackWithOneResult<BatchedResult<Long>[]> commandCallback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();

        this.store.executeCommands(this.correctUser, this.correctUserPass, commands,
                commandCallback);
        waitOnCallback(commandCallback);

        final BatchedResult<Long>[] result = commandCallback.getEffect();
        if(commandCallback.getException() != null) {
            throw new RuntimeException(
                    "ExecuteCommands did not work properly in setUp (threw an Exception), here's its message text: ",
                    commandCallback.getException());
        }

        if(result.length <= 0) {
            throw new RuntimeException(
                    "ExecuteCommands did not work properly in setUp (returned no results) - tests can not be run!");
        }

        for(int i = 0; i < result.length; i++) {
            if(result[i].getResult() == XCommand.FAILED) {
                throw new RuntimeException(
                        "ExecuteCommands did not work properly in setUp: command at index " + i
                                + " failed!");
            }
            // TODO this fails with the GaeStore which cannot be reset
            if(result[i].getResult() == XCommand.NOCHANGE) {
                throw new RuntimeException(
                        "ExecuteCommands did not work properly in setUp: command at index " + i
                                + " did not change anything! " + commands[i]);
            }

            if(result[i].getException() != null) {
                throw new RuntimeException(
                        "ExecuteCommands did not work properly in setUp (executing command at index "
                                + i + " threw an Exception), here's its message text: "
                                + commandCallback.getException().getMessage() + '\n'
                                + " and here is its StackTrace: " + '\n'
                                + commandCallback.getException().getStackTrace());
            }
        }

        final XAddress modelAddress1 = Base.toAddress(repoID, modelId1, null, null);
        final XAddress modelAddress2 = Base.toAddress(repoID, modelId2, null, null);
        final XAddress modelAddress3 = Base.toAddress(repoID, modelId3, null, null);

        this.modelAddressRequests = new GetWithAddressRequest[] {
                new GetWithAddressRequest(modelAddress1), new GetWithAddressRequest(modelAddress2),
                new GetWithAddressRequest(modelAddress3) };
        this.notExistingModelRequest = new GetWithAddressRequest(Base.toAddress(repoID,
                Base.toId("TestModelDoesntExist"), null, null));

        final XAddress objectAddress1 = Base.toAddress(repoID, modelId1, objectId1, null);
        final XAddress objectAddress2 = Base.toAddress(repoID, modelId1, objectId2, null);
        final XAddress objectAddress3 = Base.toAddress(repoID, modelId1, objectId3, null);
        this.objectAddressRequests = new GetWithAddressRequest[] {

        new GetWithAddressRequest(objectAddress1), new GetWithAddressRequest(objectAddress2),
                new GetWithAddressRequest(objectAddress3) };
        this.notExistingObjectRequest = new GetWithAddressRequest(Base.toAddress(repoID, modelId1,
                Base.toId("TestObjectDoesntExist"), null));
    }

    @Test
    public void testBogus1() {
        // all ok, just trigger @Before
    }

    @Test
    public void testBogus2() {
        // all ok, just trigger @Before
    }

    @Test
    public void getRevs() {
        final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();
        this.store.getModelRevisions(this.correctUser, this.correctUserPass,
                this.modelAddressRequests, revisionCallback);
        assertTrue(waitOnCallback(revisionCallback));
        assertNotNull(revisionCallback.getEffect());
        assertNull(revisionCallback.getException());
        final BatchedResult<ModelRevision>[] revisionResult = revisionCallback.getEffect();
        for(final BatchedResult<ModelRevision> l : revisionResult) {
            log.debug("Got rev = " + l.getFirst() + " - " + l.getSecond() + " - " + l.getResult());
        }
    }

    /*
     * Test if it behaves correctly for addresses of XModels the user has access
     * to
     */
    @Test
    public void testGetModelRevisions() {
        log.info("testGetModelRevisions");
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> snapshotCallback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        // Get revisions
        this.store.getModelRevisions(this.correctUser, this.correctUserPass,
                this.modelAddressRequests, revisionCallback);
        assertTrue(waitOnCallback(revisionCallback));
        assertNotNull(revisionCallback.getEffect());
        assertNull(revisionCallback.getException());

        // Get Model Snapshots to compare revision numbers
        this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
                this.modelAddressRequests, snapshotCallback);
        assertTrue(waitOnCallback(snapshotCallback));
        assertNotNull(snapshotCallback.getEffect());
        assertNull(snapshotCallback.getException());

        final BatchedResult<XReadableModel>[] snapshotResult = snapshotCallback.getEffect();
        assertEquals(this.modelAddressRequests.length, snapshotResult.length);

        final BatchedResult<ModelRevision>[] revisionResult = revisionCallback.getEffect();
        assertEquals(this.modelAddressRequests.length, revisionResult.length);

        // check order of returned snapshots
        for(int i = 0; i < this.modelAddressRequests.length; i++) {

            // test addresses
            assertNull("Unexpected exception: " + snapshotResult[i].getException(),
                    snapshotResult[i].getException());
            assertNotNull(snapshotResult[i].getResult());
            assertEquals(this.modelAddressRequests[i].address, snapshotResult[i].getResult()
                    .getAddress());

            // compare revision numbers
            assertNotNull(revisionResult[i].getResult());
            assertNull(revisionResult[i].getException());
            final long revBySnapshot = snapshotResult[i].getResult().getRevisionNumber();
            final long revDirect = revisionResult[i].getResult().revision();
            assertEquals("" + snapshotResult[i].getResult().getAddress(), revDirect, revBySnapshot);
        }
    }

    /**
     * Tests for the checkLogin()-method
     */

    // basic functionality test for checkLogin
    // Testing a login that should succeed
    @Test
    public void testCheckLoginSuccess() {
        final SynchronousCallbackWithOneResult<Boolean> callback = new SynchronousCallbackWithOneResult<Boolean>();
        XyAssert.xyAssert(this.store != null);
        assert this.store != null;
        this.store.checkLogin(this.correctUser, this.correctUserPass, callback);

        assertTrue(waitOnCallback(callback));
        assertEquals(true, callback.getEffect());
        assertNull(callback.getException());

    }

    /*
     * Testing a login that should fail because of a wrong actorId-passwordHash
     * combination
     */
    @Test
    public void testCheckLoginFailure() {
        if(!this.incorrectActorExists) {
            // This test only makes sense if an incorrect actorID - passwordhash
            // combination can be provided
            return;
        }

        final SynchronousCallbackWithOneResult<Boolean> callback = new SynchronousCallbackWithOneResult<Boolean>();

        this.store.checkLogin(this.incorrectUser, this.incorrectUserPass, callback);

        assertTrue(waitOnCallback(callback));
        assertEquals(false, callback.getEffect());
        assertNull(callback.getException());
    }

    @Test
    public void testCheckLoginSuccessAfterFailure() {
        if(!this.incorrectActorExists) {
            // This test only makes sense if an incorrect actorID - passwordhash
            // combination can be provided
            return;
        }

        final SynchronousCallbackWithOneResult<Boolean> callback = new SynchronousCallbackWithOneResult<Boolean>();

        this.store.checkLogin(this.correctUser, Base.createUniqueId().toString(), callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertEquals(false, callback.getEffect());

        final SynchronousCallbackWithOneResult<Boolean> callback2 = new SynchronousCallbackWithOneResult<Boolean>();

        this.store.checkLogin(this.correctUser, this.correctUserPass, callback2);

        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());
        assertEquals(true, callback2.getEffect());
    }

    // Test if checkLogin actually throws IllegalArgumentExceptions if null is
    // passed
    @Test
    public void testCheckLoginPassingNull() {
        // check IllegalArgumentException
        // first parameter equals null
        SynchronousCallbackWithOneResult<Boolean> callback = new SynchronousCallbackWithOneResult<Boolean>();

        try {
            this.store.checkLogin(null, this.correctUserPass, callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // second parameter equals null
        callback = new SynchronousCallbackWithOneResult<Boolean>();

        try {
            this.store.checkLogin(this.correctUser, null, callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // both parameters equal null
        callback = new SynchronousCallbackWithOneResult<Boolean>();

        try {
            this.store.checkLogin(null, null, callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // callback is null
        try {
            this.store.checkLogin(this.correctUser, this.correctUserPass, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // all parameters equal null
        callback = new SynchronousCallbackWithOneResult<Boolean>();

        try {
            this.store.checkLogin(null, null, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }
    }

    /*
     * Test if it behaves correctly for a correct account + password combination
     *
     *
     * Please note: This is only a rudimentary test of the functionality of
     * {@link XydraStore#getModelIds()}. Since this method is heavily connected
     * with account access rights and this test assumes no specific access right
     * management implementation, every implementation of {@link
     * AbstractStoreReadMethodsTest} should provide further tests for this
     * method that actually consider the access right management used by the
     * {@link XydraStore} implementation they are testing.
     */
    @Test
    public void testGetModelIds() {
        final SynchronousCallbackWithOneResult<Set<XId>> callback = new SynchronousCallbackWithOneResult<Set<XId>>();

        this.store.getModelIds(this.correctUser, this.correctUserPass, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final Set<XId> result = callback.getEffect();

        /*
         * check if it contains the XIds of the XModels created by this test
         * (the result should contain each of these XIds, since the user with
         * the account XId this.correctUser has access to everything in the
         * store)
         */
        for(int i = 0; i < this.modelAddressRequests.length; i++) {
            assertTrue(
                    result + " should contain " + this.modelAddressRequests[i].address.getModel(),
                    result.contains(this.modelAddressRequests[i].address.getModel()));
        }

    }

    /**
     * Tests for getModelIds
     */

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testGetModelIdsBadAccount() {
        if(!this.incorrectActorExists) {
            // This test only makes sense if an incorrect actorID - passwordhash
            // combination can be provided
            return;
        }

        SynchronousCallbackWithOneResult<Set<XId>> callback;

        // Test if it behaves correctly for wrong account + password
        // combinations
        callback = new SynchronousCallbackWithOneResult<Set<XId>>();

        this.store.getModelIds(this.incorrectUser, this.incorrectUserPass, callback);

        assertFalse("callback should be false", waitOnCallback(callback));
        assertNull(callback.getEffect());
        assertNotNull(callback.getException());
        assertTrue("unexpected exception: " + callback.getException(),
                callback.getException() instanceof AuthorisationException);
    }

    // Test IllegalArgumentException
    @Test
    public void testGetModelIdsPassingNull() {
        SynchronousCallbackWithOneResult<Set<XId>> callback = new SynchronousCallbackWithOneResult<Set<XId>>();

        // first parameter equals null
        try {
            this.store.getModelIds(null, this.correctUserPass, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // second parameter equals null
        callback = new SynchronousCallbackWithOneResult<Set<XId>>();

        try {
            this.store.getModelIds(this.correctUser, null, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // all parameters equal null
        callback = new SynchronousCallbackWithOneResult<Set<XId>>();

        try {
            this.store.getModelIds(null, null, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // callback equals null
        try {
            this.store.getModelIds(this.correctUser, this.correctUserPass, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // everything equals null
        try {
            this.store.getModelIds(null, null, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }
    }

    // Test IllegalArgumentException
    @Test
    public void testGetModelRevisionPassingNull() {
        SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        // first parameter equals null
        try {
            this.store.getModelRevisions(null, this.correctUserPass, this.modelAddressRequests,
                    revisionCallback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // second parameter equals null
        revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        try {
            this.store.getModelRevisions(this.correctUser, null, this.modelAddressRequests,
                    revisionCallback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // third parameter equals null
        revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        try {
            this.store.getModelRevisions(this.correctUser, this.correctUserPass, null,
                    revisionCallback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // all parameters equal null
        revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        try {
            this.store.getModelRevisions(null, null, null, revisionCallback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // callback equals null - should not throw an IllegalArgumentException
        try {
            this.store.getModelRevisions(this.correctUser, this.correctUserPass,
                    this.modelAddressRequests, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // everything equals null
        try {
            this.store.getModelRevisions(null, null, null, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }
    }

    /**
     * Tests for the GetModelRevisions-Method
     */

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testGetModelRevisionsBadAccount() {
        if(!this.incorrectActorExists) {
            // This test only makes sense if an incorrect actorID - passwordhash
            // combination can be provided
            return;
        }

        SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> revisionCallback;

        revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass,
                this.modelAddressRequests, revisionCallback);
        assertFalse("bad account should get false", waitOnCallback(revisionCallback));
        assertNull(revisionCallback.getEffect());
        assertNotNull(revisionCallback.getException());
        assertTrue(revisionCallback.getException() instanceof AuthorisationException);
    }

    // Test if it behaves correctly for mixes of the cases above
    @Test
    public void testGetModelRevisionsMixedAddresses() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> snapshotCallback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> revisionCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();

        final GetWithAddressRequest[] tempArray = new GetWithAddressRequest[this.modelAddressRequests.length + 1];
        System.arraycopy(this.modelAddressRequests, 0, tempArray, 0,
                this.modelAddressRequests.length);
        tempArray[this.modelAddressRequests.length] = this.notExistingModelRequest;

        this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray,
                revisionCallback);
        assertTrue(waitOnCallback(revisionCallback));
        assertNotNull(revisionCallback.getEffect());
        assertNull(revisionCallback.getException());

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray,
                snapshotCallback);
        assertTrue(waitOnCallback(snapshotCallback));
        assertNotNull(snapshotCallback.getEffect());
        assertNull(snapshotCallback.getException());

        final BatchedResult<XReadableModel>[] snapshotResult = snapshotCallback.getEffect();
        assertEquals(tempArray.length, snapshotResult.length);

        final BatchedResult<ModelRevision>[] revisionResult = revisionCallback.getEffect();
        assertEquals(tempArray.length, revisionResult.length);

        // check order of returned snapshots
        for(int i = 0; i < this.modelAddressRequests.length; i++) {
            if(i == this.modelAddressRequests.length) {
                // this index contains an XAddress of a not existing XModel
                assertNull(snapshotResult[i].getResult());
                assertEquals(XCommand.FAILED, revisionResult[i].getResult().revision());
                assertNull(revisionResult[i].getException());
            } else {
                assertNotNull(snapshotResult[i]);
                assertNotNull(snapshotResult[i].getResult());
                assertEquals(this.modelAddressRequests[i].address, snapshotResult[i].getResult()
                        .getAddress());

                assertNotNull(revisionResult[i].getResult());
                assertNull(revisionResult[i].getException());
                // bug: 4 revisions off
                assertEquals(snapshotResult[i].getResult().getRevisionNumber(), revisionResult[i]
                        .getResult().revision());
            }
        }

        // IMPROVE Maybe test more complex mixes?
    }

    // Test if it behaves correctly for addresses of XModels that don't
    // exist
    @Test
    public void testGetModelRevisionsNotExistingModel() {
        final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();
        final GetWithAddressRequest[] tempArray = new GetWithAddressRequest[] { this.notExistingModelRequest };

        this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<ModelRevision>[] result = callback.getEffect();
        assertNotNull(result[0].getResult());
        assertNull(result[0].getException());
        assertEquals(XCommand.FAILED, result[0].getResult().revision());
    }

    // Test if it behaves correctly for addresses that do not address an XModel
    @Test
    public void testGetModelRevisionsWrongAddress() {
        final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();
        log.warn("Expect three warnings because we ask with objectAdresses for modelRevisions");
        this.store.getModelRevisions(this.correctUser, this.correctUserPass,
                this.objectAddressRequests, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<ModelRevision>[] result = callback.getEffect();

        for(int i = 0; i < result.length; i++) {
            // assertNotNull(result[i].getResult());
            assertNotNull(result[i].getException());
            assertTrue(result[i].getException() instanceof RequestException);
        }
    }

    // Test if it behaves correctly for addresses of XModels a correct user has
    // access to
    @Test
    public void testGetModelSnapshots() {
        log.info("testGetModelSnapshots");
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
                this.modelAddressRequests, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableModel>[] result = callback.getEffect();
        assertEquals(this.modelAddressRequests.length, result.length);

        // check order of returned snapshots
        for(int i = 0; i < this.modelAddressRequests.length; i++) {
            assertNotNull(result[i].getResult());
            assertNull(result[i].getException());
            assertEquals(this.modelAddressRequests[i].address, result[i].getResult().getAddress());
        }
    }

    /**
     * Tests for the getModelSnapshots-method
     */

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testGetModelSnapshotsBadAccount() {
        if(!this.incorrectActorExists) {
            return;
        }

        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback;

        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass,
                this.modelAddressRequests, callback);

        assertFalse(waitOnCallback(callback));
        assertNull(callback.getEffect());
        assertNotNull(callback.getException());
        assertTrue(callback.getException() instanceof AuthorisationException);
    }

    // Test if it behaves correctly for mixes of the cases above
    @Test
    public void testGetModelSnapshotsMixedAddressTypes() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        final GetWithAddressRequest[] tempArray = new GetWithAddressRequest[this.modelAddressRequests.length + 1];
        System.arraycopy(this.modelAddressRequests, 0, tempArray, 0,
                this.modelAddressRequests.length);
        tempArray[this.modelAddressRequests.length] = this.notExistingModelRequest;

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableModel>[] result = callback.getEffect();
        assertEquals(tempArray.length, result.length);

        // check order of returned snapshots
        for(int i = 0; i < this.modelAddressRequests.length; i++) {
            if(i == this.modelAddressRequests.length) {
                // This index contains an XAddress of a not existing XModel
                assertNull(result[i].getResult());
                assertNotNull(result[i].getException());
                assertTrue(result[i].getException() instanceof RequestException);
            } else {
                assertNotNull(result[i].getResult());
                assertNull(result[i].getException());
                assertEquals(this.modelAddressRequests[i].address, result[i].getResult()
                        .getAddress());
            }
        }
        // IMPROVE Maybe test more complex mixes? (use objectadresses too?)
    }

    // Test if it behaves correctly for addresses of XModels that don't
    // exist
    @Test
    public void testGetModelSnapshotsNotExistingModel() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        final GetWithAddressRequest[] tempArray = new GetWithAddressRequest[] { this.notExistingModelRequest };

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableModel>[] result = callback.getEffect();
        assertNull(result[0].getResult());
        assertNull(result[0].getException());
    }

    // Test if IllegalArgumentException are thrown when null values are passed
    @Test
    public void testGetModelSnapshotsPassingNull() {
        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        // first parameter equals null
        try {
            this.store.getModelSnapshots(null, this.correctUserPass, this.modelAddressRequests,
                    callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // second parameter equals null
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        try {
            this.store.getModelSnapshots(this.correctUser, null, this.modelAddressRequests,
                    callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // third parameter equals null
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        try {
            this.store.getModelSnapshots(this.correctUser, this.correctUserPass, null, callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // all parameters equal null
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        try {
            this.store.getModelSnapshots(null, null, null, callback);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // callback equals null
        try {
            this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
                    this.modelAddressRequests, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // everything equals null
        try {
            this.store.getModelSnapshots(null, null, null, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }
    }

    // Test if it behaves correctly for addresses that do not address an XModel
    @Test
    public void testGetModelSnapshotsWrongAddress() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
                this.objectAddressRequests, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableModel>[] result = callback.getEffect();
        for(int i = 0; i < result.length; i++) {
            assertNull(result[i].getResult());
            assertNotNull(result[i].getException());
            assertTrue(result[i].getException() instanceof RequestException);
        }
    }

    // Test if it behaves correctly for addresses of XObjects the user has
    // access to
    @Test
    public void testGetObjectSnapshots() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
                this.objectAddressRequests, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableObject>[] result = callback.getEffect();
        assertEquals(this.objectAddressRequests.length, result.length);

        // check order of returned snapshots
        for(int i = 0; i < this.objectAddressRequests.length; i++) {
            assertNotNull(result[i].getResult());
            assertNull(result[i].getException());
            assertEquals(this.objectAddressRequests[i].address, result[i].getResult().getAddress());
        }
    }

    /**
     * Tests for the getObjectSnapshot-Method
     */

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testGetObjectSnapshotsBadAccount() {
        if(!this.incorrectActorExists) {
            // This test only makes sense if an incorrect actorID - passwordhash
            // combination can be provided
            return;
        }

        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback;

        // Test if it behaves correctly for wrong account + password
        // combinations
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass,
                this.objectAddressRequests, callback);

        assertFalse(waitOnCallback(callback));
        assertNull(callback.getEffect());
        assertNotNull(callback.getException());
        assertTrue(callback.getException() instanceof AuthorisationException);
    }

    // Test if it behaves correctly for mixes of the cases above
    @Test
    public void testGetObjectSnapshotsMixedAddresses() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        final GetWithAddressRequest[] tempArray = new GetWithAddressRequest[this.objectAddressRequests.length + 1];
        System.arraycopy(this.objectAddressRequests, 0, tempArray, 0,
                this.objectAddressRequests.length);
        tempArray[this.objectAddressRequests.length] = this.notExistingObjectRequest;

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableObject>[] result = callback.getEffect();
        assertEquals(tempArray.length, result.length);

        // check order of returned snapshots
        for(int i = 0; i < this.objectAddressRequests.length; i++) {
            if(i == this.objectAddressRequests.length) {
                // this index contains an XAddress of a not existing XObject
                assertNull(result[i].getResult());
                assertNotNull(result[i].getException());
                assertTrue(result[i].getException() instanceof RequestException);
            } else {
                assertNotNull(result[i].getResult());
                assertNull(result[i].getException());
                assertEquals(this.objectAddressRequests[i].address, result[i].getResult()
                        .getAddress());
            }
        }

        // IMPROVE Maybe test more complex mixes?
    }

    // Test if it behaves correctly for addresses of XObjects that don't
    // exist
    @Test
    public void testGetObjectSnapshotsNotExistingObject() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        final GetWithAddressRequest[] tempArray = new GetWithAddressRequest[] { this.notExistingObjectRequest };

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableObject>[] result = callback.getEffect();
        assertNull(result[0].getResult());
        assertNull(result[0].getException());
    }

    // Test IllegalArgumentException
    @Test
    public void testGetObjectSnapshotsPassingNull() {
        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        // first parameter equals null
        try {
            this.store.getObjectSnapshots(null, this.correctUserPass, this.objectAddressRequests,
                    callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // second parameter equals null
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        try {
            this.store.getObjectSnapshots(this.correctUser, null, this.objectAddressRequests,
                    callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // third parameter equals null
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        try {
            this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, null, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // all parameters equal null
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        try {
            this.store.getObjectSnapshots(null, null, null, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // callback equals null
        try {
            this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
                    this.objectAddressRequests, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // everything equals null
        try {
            this.store.getObjectSnapshots(null, null, null, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }
    }

    // Test if it behaves correctly for addresses that do not address an XObject
    @Test
    public void testGetObjectSnapshotsWrongAddress() {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
                this.modelAddressRequests, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertNull(callback.getException());

        final BatchedResult<XReadableObject>[] result = callback.getEffect();
        for(int i = 0; i < result.length; i++) {
            assertNull(result[i].getResult());
            assertNotNull(result[i].getException());
            assertTrue(result[i].getException() instanceof RequestException);
        }
    }

    /**
     * Tests for getRepositoryId
     */

    /*
     * Test if it behaves correctly for a correct account + password combination
     */
    @Test
    public void testGetRepositoryId() {
        final XId correctUser = getCorrectUser();
        final String correctUserPass = getCorrectUserPasswordHash();

        final SynchronousCallbackWithOneResult<XId> callback = new SynchronousCallbackWithOneResult<XId>();

        this.store.getRepositoryId(correctUser, correctUserPass, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertEquals(getRepositoryId(), callback.getEffect());
        assertNull(callback.getException());
    }

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testGetRepositoryIdBadAccount() {
        if(!this.incorrectActorExists) {
            // This test only makes sense if an incorrect actorID - passwordhash
            // combination can be provided
            return;
        }

        final SynchronousCallbackWithOneResult<XId> callback = new SynchronousCallbackWithOneResult<XId>();

        this.store.getRepositoryId(this.incorrectUser, this.incorrectUserPass, callback);

        assertFalse(waitOnCallback(callback));
        assertNull(callback.getEffect());
        assertNotNull(callback.getException());
        assertTrue(callback.getException() instanceof AuthorisationException);
    }

    // Test IllegalArgumentException
    @Test
    public void testGetRepositoryIdPassingNull() {
        SynchronousCallbackWithOneResult<XId> callback = new SynchronousCallbackWithOneResult<XId>();

        // first parameter equals null
        try {
            this.store.getRepositoryId(null, this.correctUserPass, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // second parameter equals null
        callback = new SynchronousCallbackWithOneResult<XId>();

        try {
            this.store.getRepositoryId(this.correctUser, null, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // all parameters equal null
        callback = new SynchronousCallbackWithOneResult<XId>();

        try {
            this.store.getRepositoryId(null, null, callback);
            // there's something wrong if we reached this
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // callback equals null
        try {
            this.store.getRepositoryId(this.correctUser, this.correctUserPass, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }

        // everything equals null
        try {
            this.store.getRepositoryId(null, null, null);
            // if we reach this, the method didn't work as expected
            fail();
        } catch(final IllegalArgumentException iae) {
        }
    }

}
