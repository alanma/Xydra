package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;


/**
 * Abstract test for the write methods of {@link XydraStore}.
 *
 * @author kaidel
 */

/*
 * TODO Comments in {@link XydraStore} state, that the changes made by methods
 * like executeCommand might not be available directly after the execution of
 * the commands. if this is the case, how exactly can I effectively test if the
 * changes are made? Idea: Introduce a parameter "average waiting time", which
 * describe how long it takes on averages after the changes are available on the
 * store
 */

/*
 * TODO Don't forget to test the cases in which commands are passed to an entity
 * which is lower in the hierarchy, i.e. an object command which is sent to an
 * XModel and then executed by one of its XObjects.
 */

public abstract class AbstractStoreWriteMethodsTest extends AbstractStoreTest {

    private static final Logger log = LoggerFactory
            .getLogger(AbstractAllowAllStoreWriteMethodsTest.class);

    protected XId correctUser, incorrectUser, repoId;

    protected String correctUserPass, incorrectUserPass;

    protected XCommandFactory factory;
    protected boolean incorrectActorExists = true;
    protected XydraStore store;
    protected long timeout;

    @Override
	@Before
    public void setUp() {
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

        this.timeout = getCallbackTimeout();

        if(this.timeout <= 0) {
            throw new IllegalArgumentException("Timeout for callbacks must be greater than 0!");
        }

        // get the repository ID of the store
        this.repoId = getRepositoryId();
    }

    @Override
	public void tearDown() {
    }

    /*
     * Tests for executeCommand
     */

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testExecuteCommandsBadAccount() {
        log.info("TEST testExecuteCommandsBadAccount");
        if(!this.incorrectActorExists) {
            return;
        }

        SynchronousCallbackWithOneResult<BatchedResult<Long>[]> callback;

        callback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();

        final XCommand[] commands = new XCommand[] { BaseRuntime.getCommandFactory().createAddModelCommand(
                this.repoId, Base.createUniqueId(), true) };

        this.store.executeCommands(this.incorrectUser, this.incorrectUserPass, commands, callback);

        assertFalse(waitOnCallback(callback));
        assertNull(callback.getEffect());
        assertNotNull(callback.getException());
        assertTrue(callback.getException() instanceof AuthorisationException);
        log.info("/TEST testExecuteCommandsBadAccount");
    }

    private GetWithAddressRequest[] modelAddressRequests(final XId modelId) {
        return new GetWithAddressRequest[] { new GetWithAddressRequest(Base.toAddress(this.repoId,
                modelId, null, null)) };
    }

    /*
     * Tests for RepositoryCommands
     */
    @Test
    public void testExecuteCommandsCorrectRepoCommands() {
        // create a model
        final XId modelId = Base.createUniqueId();

        final long modelRev = executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(
                this.repoId, modelId, true));

        // TODO check that the returned revision matches getModelRevisions

        // check if the model was created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
                modelAddressRequests(modelId), callback2);
        assertTrue(waitOnCallback(callback2));

        BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
        assertNotNull(result2);
        assertNotNull(result2[0].getResult());
        assertEquals(modelId, result2[0].getResult().getId());

        // remove the model again

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveModelCommand(this.repoId,
                modelId, modelRev, true));

        // check if the model was removed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
                modelAddressRequests(modelId), callback2);
        assertTrue(waitOnCallback(callback2));

        result2 = callback2.getEffect();
        assertNotNull(result2);
        // TODO why expect null?
        assertNull("expected null, got: " + result2[0].getResult(), result2[0].getResult());
        assertNull(result2[0].getException());
    }

    @Test
    public void testExecuteCommandsIncorrectRepoCommands() {
        // try to remove non-existing model
        final XId modelId = Base.createUniqueId();

        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveModelCommand(this.repoId, modelId,
                42, false));

        // add a model
        final long modelRev = executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(
                this.repoId, modelId, true));

        // try to remove the model but use the wrong revision number
        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveModelCommand(this.repoId, modelId,
                modelRev + 1, false));

        // try to add the same model again with a not-forced command -> should
        // fail
        executeFailingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                false));

    }

    @Test
    public void testExecuteCommandsMixedCorrectRepoCommands() {

        // create some models
        final int modelCount = 5;
        final XId[] modelIds = new XId[modelCount];
        XCommand[] commands = new XCommand[modelCount];

        for(int i = 0; i < modelIds.length; i++) {
            modelIds[i] = Base.createUniqueId();
            commands[i] = BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelIds[i],
                    true);
        }

        final long[] modelRevs = executeSucceedingCommands(commands);

        // check if the models were created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        final GetWithAddressRequest[] modelAddressRequests = new GetWithAddressRequest[modelCount];
        for(int i = 0; i < modelCount; i++) {
            modelAddressRequests[i] = new GetWithAddressRequest(Base.toAddress(this.repoId,
                    modelIds[i], null, null));
        }

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddressRequests,
                callback2);
        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());

        BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
        assertNotNull(result2);
        for(int i = 0; i < modelCount; i++) {
            assertEquals(modelAddressRequests[i].address.getModel(), result2[i].getResult().getId());
            assertNull(result2[i].getException());
        }

        // remove the models again
        commands = new XCommand[modelCount];
        for(int i = 0; i < modelCount; i++) {
            commands[i] = BaseRuntime.getCommandFactory().createRemoveModelCommand(this.repoId, modelIds[i],
                    modelRevs[i], true);
        }

        executeSucceedingCommands(commands);

        // check if the models were removed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddressRequests,
                callback2);
        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());

        result2 = callback2.getEffect();
        assertNotNull(result2);
        for(int i = 0; i < modelCount; i++) {
            assertNull(result2[i].getResult());
            assertNull(result2[i].getException());
        }
    }

    // TODO Test "noChange"-cases

    /*
     * Tests for ModelCommands
     */
    @Test
    public void testExecuteCommandsIncorrectModelCommands() {

        // create a model
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // try to remove non-existing object
        final XId objectId = Base.createUniqueId();

        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
                objectId, 42, false));

        // add an object
        final long revNr = executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // try to remove the object but use the wrong revision number
        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
                objectId, revNr + 1, false));

        // try to add the same object again with a not-forced command, should
        // fail
        executeFailingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, false));

    }

    @Test
    public void testExecuteCommandsCorrectModelCommands() {

        // create a model first
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create an object
        final XId objectId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // check if the object was created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback3 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        final GetWithAddressRequest[] objectAddressRequests = new GetWithAddressRequest[] { new GetWithAddressRequest(
                Base.toAddress(this.repoId, modelId, objectId, null)) };

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
                objectAddressRequests, callback3);
        assertTrue(waitOnCallback(callback3));

        BatchedResult<XReadableObject>[] result2 = callback3.getEffect();
        assertNotNull(result2);
        assertNotNull(result2[0]);
        assertNotNull(result2[0].getResult());
        assertEquals(objectId, result2[0].getResult().getId());

        // remove the object again
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveObjectCommand(this.repoId,
                modelId, objectId, XCommand.FORCED, true));

        // check if the object was removed
        callback3 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
                objectAddressRequests, callback3);
        assertTrue(waitOnCallback(callback3));

        result2 = callback3.getEffect();
        assertNotNull(result2);
        assertNull(result2[0].getResult());
        assertNull(result2[0].getException());
    }

    @Test
    public void testExecuteCommandsMixedCorrectModelCommands() {
        // create a model first
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create some objects

        final int objectCount = 5;
        final XId[] objectIds = new XId[objectCount];
        XCommand[] commands = new XCommand[objectCount];
        for(int i = 0; i < objectIds.length; i++) {
            objectIds[i] = Base.createUniqueId();
            commands[i] = BaseRuntime.getCommandFactory().createAddObjectCommand(
                    Base.resolveModel(this.repoId, modelId), objectIds[i], true);
        }

        final long[] revs = executeSucceedingCommands(commands);

        // check if the objects were created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        final GetWithAddressRequest[] objectAddresses = new GetWithAddressRequest[objectCount];
        for(int i = 0; i < objectCount; i++) {
            objectAddresses[i] = new GetWithAddressRequest(Base.toAddress(this.repoId, modelId,
                    objectIds[i], null));
        }

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddresses,
                callback2);
        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());

        BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
        assertNotNull(result2);
        for(int i = 0; i < objectCount; i++) {
            assertEquals(modelId, result2[i].getResult().getAddress().getModel());
            assertEquals(objectAddresses[i].address.getObject(), result2[i].getResult().getId());
            assertNull(result2[i].getException());
        }

        // remove the objects again
        commands = new XCommand[objectCount];
        for(int i = 0; i < objectCount; i++) {
            commands[i] = BaseRuntime.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
                    objectIds[i], revs[i], true);
        }

        executeSucceedingCommands(commands);

        // check if the object were removed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddresses,
                callback2);
        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());

        result2 = callback2.getEffect();
        assertNotNull(result2);
        for(int i = 0; i < objectCount; i++) {
            assertNull(result2[i].getResult());
            assertNull(result2[i].getException());
        }
    }

    /*
     * Tests for ObjectCommands
     */
    @Test
    public void testExecuteCommandsCorrectObjectCommands() {
        // create a model first
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create an object
        final XId objectId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // create an object
        final XId fieldId = Base.createUniqueId();

        final long revNr = executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // check if the field was created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        final GetWithAddressRequest[] objectAddress = new GetWithAddressRequest[] { new GetWithAddressRequest(
                Base.toAddress(this.repoId, modelId, objectId, null)) };

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));

        BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
        assertNotNull(result2);
        XReadableObject object = result2[0].getResult();
        assertEquals(objectId, object.getId());
        assertTrue(object.hasField(fieldId));

        // remove the field again

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveFieldCommand(this.repoId,
                modelId, objectId, fieldId, revNr, true));

        // check if the field was removed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));

        result2 = callback2.getEffect();
        assertNotNull(result2);
        object = result2[0].getResult();
        assertEquals(objectId, object.getId());
        assertFalse(object.hasField(fieldId));
    }

    @Test
    public void testExecuteCommandsIncorrectObjectCommands() {
        // create a model
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // add an object
        final XId objectId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // try to remove non-existing field
        final XId fieldId = Base.createUniqueId();

        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
                objectId, fieldId, 42, false));

        // add a field
        final long revNr = executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // try to remove the field but use the wrong revision number

        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
                objectId, fieldId, revNr + 1, false));

        // try to add the same field again with a not-forced command, should
        // fail
        executeFailingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, false));

    }

    @Test
    public void testExecuteCommandsMixedCorrectObjectCommands() {
        // create a model and object first
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // create some field
        final int fieldCount = 5;
        final XId[] fieldIds = new XId[fieldCount];
        XCommand[] commands = new XCommand[fieldCount];
        for(int i = 0; i < fieldIds.length; i++) {
            fieldIds[i] = Base.createUniqueId();
            commands[i] = BaseRuntime.getCommandFactory().createAddFieldCommand(
                    Base.resolveObject(this.repoId, modelId, objectId), fieldIds[i], true);
        }

        final long[] revs = executeSucceedingCommands(commands);

        // check if the fields were created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        final GetWithAddressRequest[] objectAddress = new GetWithAddressRequest[] { new GetWithAddressRequest(
                Base.toAddress(this.repoId, modelId, objectId, null)) };

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());

        BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
        assertNull(result2[0].getException());
        assertNotNull(result2);
        for(int i = 0; i < fieldCount; i++) {
            assertNotNull(result2[0].getResult());
            assertNotNull(result2[0].getResult().getField(fieldIds[i]));
        }

        // remove the fields again
        commands = new XCommand[fieldCount];
        for(int i = 0; i < fieldCount; i++) {
            commands[i] = BaseRuntime.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
                    objectId, fieldIds[i], revs[i], true);
        }

        executeSucceedingCommands(commands);

        // check if the fields were removed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));
        assertNull(callback2.getException());

        result2 = callback2.getEffect();
        assertNotNull(result2);
        for(int i = 0; i < fieldCount; i++) {
            assertNull(result2[0].getResult().getField(fieldIds[i]));
            assertNull(result2[0].getException());
        }
    }

    /*
     * Tests for FieldCommands
     */
    @Test
    public void testExecuteCommandsCorrectFieldCommands() {
        // create a model first
        final XId modelId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create an object
        final XId objectId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // create a field
        final XId fieldId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // add a value
        final XValue testValue = Base.createUniqueId();
        long revNr = XCommand.NEW;

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), revNr, testValue, true));

        revNr++;

        // check if the field was created
        SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        final GetWithAddressRequest[] objectAddress = new GetWithAddressRequest[] { new GetWithAddressRequest(
                Base.toAddress(this.repoId, modelId, objectId, null)) };

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));

        BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
        assertNotNull(result2);
        XReadableObject object = result2[0].getResult();
        assertEquals(objectId, object.getId());
        assertTrue(object.hasField(fieldId));

        XReadableField field = object.getField(fieldId);
        assertNotNull(field.getValue());
        assertEquals(testValue, field.getValue());

        // change the value
        final XValue testValue2 = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createChangeValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), revNr, testValue2, true));
        revNr++;

        // check if the value was changed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));

        result2 = callback2.getEffect();
        assertNotNull(result2);
        object = result2[0].getResult();
        assertEquals(objectId, object.getId());
        assertTrue(object.hasField(fieldId));

        field = object.getField(fieldId);
        assertNotNull(field.getValue());
        assertEquals(testValue2, field.getValue());
        assertFalse(field.getValue().equals(testValue));

        // remove the value again
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveValueCommand(this.repoId,
                modelId, objectId, fieldId, revNr, true));
        revNr++;

        // check if the value was removed
        callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));

        result2 = callback2.getEffect();
        assertNotNull(result2);
        object = result2[0].getResult();
        assertEquals(objectId, object.getId());
        assertTrue(object.hasField(fieldId));

        field = object.getField(fieldId);
        assertNull(field.getValue());
    }

    @Test
    public void testExecuteCommandsIncorrectFieldCommands() {
        // create model, object and field
        final XId modelId = Base.toId("model6");
        final XId objectId = Base.toId("object6");
        final XId fieldId = Base.toId("field6");

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // try to remove non-existing value
        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveValueCommand(this.repoId, modelId,
                objectId, fieldId, XCommand.NEW, false));

        // try to change a non-existing value

        final XValue testValue = BaseRuntime.getValueFactory().createStringValue("Test");

        executeFailingCommand(BaseRuntime.getCommandFactory().createChangeValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), 0, testValue, false));

        // add a value
        final long revNr = executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), XCommand.FORCED,
                testValue, true));

        // try to remove the value but use the wrong revision number
        executeFailingCommand(BaseRuntime.getCommandFactory().createRemoveValueCommand(this.repoId, modelId,
                objectId, fieldId, revNr + 1, false));

    }

    /*
     * Tests for getEvents
     *
     * Technically, this is a read method, but I think the tests for this method
     * fit here better, since it's heavily connected with executeCommands()
     * ~Bjoern
     */

    // Test if it behaves correctly for wrong account + password
    // combinations
    @Test
    public void testGetEventsBadAccount() {
        if(!this.incorrectActorExists) {
            return;
        }

        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback;

        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();

        final GetEventsRequest[] requests = new GetEventsRequest[1];

        // Add a valid request.
        requests[0] = new GetEventsRequest(Base.toAddress("/data/somewhere"), 0, 1);

        this.store.getEvents(this.incorrectUser, this.incorrectUserPass, requests, callback);

        assertFalse(waitOnCallback(callback));
        assertNull(callback.getEffect());
        assertNotNull(callback.getException());
        assertTrue(callback.getException() instanceof AuthorisationException);
    }

    @Test
    public void testGetEventsBadRevisions() {
        // create a model first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create an object and check if event is being thrown
        final XId objectId = Base.createUniqueId();

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);

        /*
         * begin revision is greater than end revision - should throw a
         * RequestException according to the {@link XydraStore} interface
         * documentation.
         */
        final GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev + 1, modelRev) };
        final SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNull(callback.getEffect()[0].getResult());
        final Throwable exception = callback.getEffect()[0].getException();
        assertNotNull(exception);
        assertTrue(exception instanceof RequestException);
    }

    @Test
    public void testGetEventsBadAddress() {
        final XAddress randomAddress = Base.toAddress(this.repoId, Base.createUniqueId(), null, null);

        final GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(randomAddress,
                0, 1) };
        final SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());

        /*
         * the request address does not exist, therefore the returned
         * BatchedResult should contain "null" according to the {@link
         * XydraStore} interface documentation.
         */
        assertNull(callback.getEffect()[0].getResult());
        assertNull(callback.getEffect()[0].getException());
    }

    // Tests for Model Events
    @Test
    public void testGetEventsModelEventsAddType() {
        // create a model first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create an object and check if event is being thrown
        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);

        final GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        final SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event = callback.getEffect()[0].getResult()[0];

        checkEvent(event, objectAddress, ChangeType.ADD, XType.XMODEL, modelRev);
    }

    @Test
    public void testGetEventsModelEventsRemoveType() {
        // create a model first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        // create an object and check if event is being thrown
        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // remove the object again
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveObjectCommand(this.repoId,
                modelId, objectId, XCommand.FORCED, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);

        // check if event was thrown
        final GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        final SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event = callback.getEffect()[0].getResult()[0];
        checkEvent(event, objectAddress, ChangeType.REMOVE, XType.XMODEL, modelRev);
    }

    // Tests for Object Events
    @Test
    public void testGetEventsObjectEventsAddType() {
        // create a model and object first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // create a field and check if event is being thrown
        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);

        // get event from model first
        // get the event
        GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event = callback.getEffect()[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.ADD, XType.XOBJECT, objectRev);

        // get event from object
        request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event2 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event2);
    }

    @Test
    public void testGetEventsObjectEventsRemoveType() {
        // create a model and object first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // create and remove a field and check if event is being thrown
        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveFieldCommand(this.repoId,
                modelId, objectId, fieldId, XCommand.FORCED, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);

        // check if event was thrown
        // get event from model first
        GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event = callback.getEffect()[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.REMOVE, XType.XOBJECT, objectRev);

        // get event from object
        request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event2 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event2);
    }

    // Tests for Field Events
    @Test
    public void testGetEventsFieldEventsAddType() {
        // create a model, object and field first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // add a value to the field and check, if event is being thrown
        long fieldRev = getRevisionNumber(fieldAddress);

        final XValue value1 = Base.createUniqueId();
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), fieldRev, value1, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);
        fieldRev = getRevisionNumber(fieldAddress);

        // get event from model first
        GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(callback.getEffect()[0].getResult().length, 1);

        final XEvent event = callback.getEffect()[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.ADD, XType.XFIELD, fieldRev);

        assertEquals(value1, ((XFieldEvent)event).getNewValue());

        // get event from object
        request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event2 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event2);

        // get event from field
        request = new GetEventsRequest[] { new GetEventsRequest(fieldAddress, fieldRev, fieldRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event3 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event3);
    }

    @Test
    public void testGetEventsFieldEventsChangeType() {
        // create a model, object and field first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // add a value to the field, change it and check, if event is being
        // thrown
        long fieldRev = getRevisionNumber(fieldAddress);

        final XValue value1 = Base.createUniqueId();
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), fieldRev, value1, true));
        // change the value
        final XValue value2 = Base.createUniqueId();
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createChangeValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), fieldRev, value2, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);
        fieldRev = getRevisionNumber(fieldAddress);

        // get event from model first
        GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event = callback.getEffect()[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.CHANGE, XType.XFIELD, fieldRev);

        assertEquals(value2, ((XFieldEvent)event).getNewValue());

        // get event from object
        request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event2 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event2);

        // get event from field
        request = new GetEventsRequest[] { new GetEventsRequest(fieldAddress, fieldRev, fieldRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event3 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event3);
    }

    @Test
    public void testGetEventsFieldEventsRemoveType() {
        // create a model, object and field first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // add a value to the field, change it and check, if event is being
        // thrown
        long fieldRev = getRevisionNumber(fieldAddress);

        final XValue value1 = Base.createUniqueId();
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), fieldRev, value1, true));

        // remove the value and check if event is being thrown
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createRemoveValueCommand(this.repoId,
                modelId, objectId, fieldId, XCommand.FORCED, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);
        fieldRev = getRevisionNumber(fieldAddress);

        // get event from model first
        GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev, modelRev) };
        SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event = callback.getEffect()[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.REMOVE, XType.XFIELD, fieldRev);

        assertEquals(null, ((XFieldEvent)event).getNewValue());

        // get event from object
        request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event2 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event2);

        // get event from field
        request = new GetEventsRequest[] { new GetEventsRequest(fieldAddress, fieldRev, fieldRev) };
        callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);

        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect()[0].getResult());
        assertEquals(1, callback.getEffect()[0].getResult().length);

        final XEvent event3 = callback.getEffect()[0].getResult()[0];
        assertEquals(event, event3);
    }

    /*
     * Tests for executeCommandsAndGetEvents
     */
    // Tests for Model Events
    @Test
    public void testExecuteCommandsAndGetEventsModelEventsAddType() {
        // create a model first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);

        final XCommand[] commands = new XCommand[] { BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true) };

        /*
         * revision number is incremented in the requests because a command will
         * be executed before they are used
         */

        final GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev + 1, modelRev + 1) };

        final SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();

        this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
                request, callback);
        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect().getFirst());
        assertNotNull(callback.getEffect().getSecond());

        final BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
        final BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();

        assertEquals(1, commandResult.length);
        assertEquals(1, eventResult.length);

        assertNotNull(commandResult[0].getResult());
        assertNull(commandResult[0].getException());
        assertEquals(modelRev + 1, (long)commandResult[0].getResult());

        assertNotNull(eventResult[0].getResult());
        assertNull(eventResult[0].getException());
        assertEquals(1, eventResult[0].getResult().length);

        final XEvent event = eventResult[0].getResult()[0];

        checkEvent(event, objectAddress, ChangeType.ADD, XType.XMODEL, modelRev + 1);
    }

    @Test
    public void testExecuteCommandsAndGetEventsModelEventsRemoveType() {
        // create a model first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);

        final XCommand[] commands = new XCommand[] { BaseRuntime.getCommandFactory().createRemoveObjectCommand(
                this.repoId, modelId, objectId, XCommand.FORCED, true) };

        /*
         * revision number is incremented in the requests because a command will
         * be executed before they are used
         */
        final GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
                modelRev + 1, modelRev + 1) };

        final SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();

        this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
                request, callback);
        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect().getFirst());
        assertNotNull(callback.getEffect().getSecond());

        final BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
        final BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();

        assertEquals(1, commandResult.length);
        assertEquals(1, eventResult.length);

        assertNotNull(commandResult[0].getResult());
        assertNull(commandResult[0].getException());
        assertEquals(modelRev + 1, (long)commandResult[0].getResult());

        assertNotNull(eventResult[0].getResult());
        assertNull(eventResult[0].getException());
        assertEquals(1, eventResult[0].getResult().length);

        final XEvent event = eventResult[0].getResult()[0];

        checkEvent(event, objectAddress, ChangeType.REMOVE, XType.XMODEL, modelRev + 1);
    }

    // Tests for Object Events
    @Test
    public void testExecuteCommandsAndGetEventsObjectEventsAddType() {
        // create a model & object first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);

        final XCommand[] commands = new XCommand[] { BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true) };

        /*
         * revision number is incremented in the requests because a command will
         * be executed before they are used
         */
        final GetEventsRequest[] request = new GetEventsRequest[] {
                new GetEventsRequest(modelAddress, modelRev + 1, modelRev + 1),
                new GetEventsRequest(objectAddress, objectRev + 1, objectRev + 1) };

        final SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();

        this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
                request, callback);
        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect().getFirst());
        assertNotNull(callback.getEffect().getSecond());

        final BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
        final BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();

        assertEquals(1, commandResult.length);
        assertEquals(2, eventResult.length);

        assertNotNull(commandResult[0].getResult());
        assertNull(commandResult[0].getException());
        assertEquals(modelRev + 1, (long)commandResult[0].getResult());

        assertNotNull(eventResult[0].getResult());
        assertNull(eventResult[0].getException());
        assertEquals(1, eventResult[0].getResult().length);

        assertNotNull(eventResult[1].getResult());
        assertNull(eventResult[1].getException());
        assertEquals(1, eventResult[1].getResult().length);

        // check event returned by the model first
        final XEvent event = eventResult[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.ADD, XType.XOBJECT, objectRev + 1);
        // check the event returned by the object
        assertEquals(event, eventResult[1].getResult()[0]);
    }

    @Test
    public void testExecuteCommandsAndGetEventsObjectEventsRemoveType() {
        // create a model & object first
        final XId modelId = Base.createUniqueId();
        final XAddress modelAddress = Base.toAddress(this.repoId, modelId, null, null);

        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddModelCommand(this.repoId, modelId,
                true));

        final XId objectId = Base.createUniqueId();
        final XAddress objectAddress = Base.toAddress(this.repoId, modelId, objectId, null);
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, true));

        final XId fieldId = Base.createUniqueId();
        final XAddress fieldAddress = Base.toAddress(this.repoId, modelId, objectId, fieldId);
        executeSucceedingCommand(BaseRuntime.getCommandFactory().createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, true));

        // get the right revision numbers
        final long modelRev = getRevisionNumber(modelAddress);
        final long objectRev = getRevisionNumber(objectAddress);

        final XCommand[] commands = new XCommand[] { BaseRuntime.getCommandFactory().createRemoveFieldCommand(
                this.repoId, modelId, objectId, fieldId, XCommand.FORCED, true) };

        /*
         * revision number is incremented in the requests because a command will
         * be executed before they are used
         */
        final GetEventsRequest[] request = new GetEventsRequest[] {
                new GetEventsRequest(modelAddress, modelRev + 1, modelRev + 1),
                new GetEventsRequest(objectAddress, objectRev + 1, objectRev + 1) };

        final SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousCallbackWithOneResult<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();

        this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
                request, callback);
        assertTrue(waitOnCallback(callback));
        assertNull(callback.getException());
        assertNotNull(callback.getEffect());
        assertNotNull(callback.getEffect().getFirst());
        assertNotNull(callback.getEffect().getSecond());

        final BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
        final BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();

        assertEquals(1, commandResult.length);
        assertEquals(2, eventResult.length);

        assertNotNull(commandResult[0].getResult());
        assertNull(commandResult[0].getException());
        assertEquals(modelRev + 1, (long)commandResult[0].getResult());

        assertNotNull(eventResult[0].getResult());
        assertNull(eventResult[0].getException());
        assertEquals(1, eventResult[0].getResult().length);

        assertNotNull(eventResult[1].getResult());
        assertNull(eventResult[1].getException());
        assertEquals(1, eventResult[1].getResult().length);

        // check event returned by the model first
        final XEvent event = eventResult[0].getResult()[0];
        checkEvent(event, fieldAddress, ChangeType.REMOVE, XType.XOBJECT, objectRev + 1);
        // check the event returned by the object
        assertEquals(event, eventResult[1].getResult()[0]);
    }

    /**
     * Executes the given command (which is supposed to succeed) and checks if
     * everything went as expected.
     *
     * @param command The command which is to be executed
     * @param callback The callback which is used to get the information about
     *            the commands success
     */
    protected long[] executeSucceedingCommands(final XCommand[] commands) {

        final SynchronousCallbackWithOneResult<BatchedResult<Long>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();

        this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);

        assertTrue("Callback failed with " + callback.getException(), waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertTrue(callback.getEffect().length == commands.length);
        assertNull(callback.getException());

        final long[] revisions = new long[commands.length];
        for(int i = 0; i < commands.length; i++) {
            assertTrue(
                    "callback should have a positive result bus has "
                            + callback.getEffect()[i].getResult(),
                    callback.getEffect()[i].getResult() >= 0);
            assertNull(callback.getEffect()[i].getException());
            revisions[i] = callback.getEffect()[i].getResult();
        }
        return revisions;
    }

    protected long executeSucceedingCommand(final XCommand command) {
        return executeSucceedingCommands(new XCommand[] { command })[0];
    }

    /**
     * Executes the given command (which is supposed to fail) and checks if
     * everything went as expected.
     *
     * @param command The command which is to be executed
     * @param callback The callback which is used to get the information about
     *            the commands failure
     */
    protected void executeFailingCommand(final XCommand command) {

        final SynchronousCallbackWithOneResult<BatchedResult<Long>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();

        this.store.executeCommands(this.correctUser, this.correctUserPass,
                new XCommand[] { command }, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertTrue(callback.getEffect().length == 1);
        assertNull(callback.getException());
        assertTrue("command " + command + " should fail",
                callback.getEffect()[0].getResult() == XCommand.FAILED);
        assertNull(callback.getEffect()[0].getException());
    }

    /**
     * Executes the given command (which is supposed to succeed but not change
     * anything) and checks if everything went as expected.
     *
     * @param command The command which is to be executed
     * @param callback The callback which is used to get the information about
     *            the commands failure
     */
    protected void executeNochangeCommand(final XCommand command) {

        final SynchronousCallbackWithOneResult<BatchedResult<Long>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();

        this.store.executeCommands(this.correctUser, this.correctUserPass,
                new XCommand[] { command }, callback);

        assertTrue(waitOnCallback(callback));
        assertNotNull(callback.getEffect());
        assertTrue(callback.getEffect().length == 1);
        assertNull(callback.getException());
        assertTrue(callback.getEffect()[0].getResult() == XCommand.NOCHANGE);
        assertNull(callback.getEffect()[0].getException());
    }

    // private, because it makes assumptions about the tests
    private void checkEvent(final XEvent event, final XAddress changedEntity, final ChangeType type,
            final XType expectedType, final long revision) {
        assertEquals(changedEntity, event.getChangedEntity());
        assertEquals(this.correctUser, event.getActor());
        assertEquals(type, event.getChangeType());

        switch(expectedType) {
        case XMODEL:
            assertEquals(changedEntity.getParent(), event.getTarget());

            assertTrue(event instanceof XModelEvent);
            final XModelEvent modelEvent = (XModelEvent)event;
            assertEquals(this.repoId, modelEvent.getRepositoryId());
            assertEquals(changedEntity.getModel(), modelEvent.getModelId());
            assertEquals(changedEntity.getObject(), modelEvent.getObjectId());
            break;
        case XOBJECT:
            assertEquals(changedEntity.getParent(), event.getTarget());

            assertTrue(event instanceof XObjectEvent);
            final XObjectEvent objectEvent = (XObjectEvent)event;
            assertEquals(this.repoId, objectEvent.getRepositoryId());
            assertEquals(changedEntity.getModel(), objectEvent.getModelId());
            assertEquals(changedEntity.getObject(), objectEvent.getObjectId());
            assertEquals(changedEntity.getField(), objectEvent.getFieldId());

            break;
        case XFIELD:
            assertEquals(changedEntity, event.getTarget());

            assertTrue(event instanceof XFieldEvent);
            final XFieldEvent fieldEvent = (XFieldEvent)event;
            assertEquals(this.repoId, fieldEvent.getRepositoryId());
            assertEquals(changedEntity.getModel(), fieldEvent.getModelId());
            assertEquals(changedEntity.getObject(), fieldEvent.getObjectId());
            assertEquals(changedEntity.getField(), fieldEvent.getFieldId());
            break;
        case XREPOSITORY:
            // TODO test: implement checkEvent for repo
            break;
        }

        // check revision numbers
        assertEquals(revision, event.getRevisionNumber());
        switch(expectedType) {
        // attention: break-statements are missing on purpose

        /*
         * since the tests never do anything more than one change at a time, we
         * may use "revision - 1" here for the old revisions
         */
        case XFIELD:
            assertEquals("event " + event, revision - 1, event.getOldFieldRevision());
            //$FALL-THROUGH$
        case XOBJECT:
            if(event.getOldObjectRevision() != XEvent.REVISION_NOT_AVAILABLE) {
                assertEquals("event " + event, revision - 1, event.getOldObjectRevision());
            }
            //$FALL-THROUGH$
        case XMODEL:
            assertEquals("event " + event, revision - 1, event.getOldModelRevision());
            //$FALL-THROUGH$
        case XREPOSITORY:
        }
    }

    protected long getRevisionNumber(final XAddress address) {
        XyAssert.xyAssert(address.getAddressedType() != XType.XREPOSITORY);
        GetWithAddressRequest[] addressRequests;
        final XType type = address.getAddressedType();

        if(type == XType.XMODEL) {
            final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> revCallback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();
            addressRequests = new GetWithAddressRequest[] { new GetWithAddressRequest(address) };
            this.store.getModelRevisions(this.correctUser, this.correctUserPass, addressRequests,
                    revCallback);
            assertTrue(waitOnCallback(revCallback));

            return revCallback.getEffect()[0].getResult().revision();
        } else {
            final SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> objectCallback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();

            addressRequests = new GetWithAddressRequest[] { new GetWithAddressRequest(Base.toAddress(
                    address.getRepository(), address.getModel(), address.getObject(), null)) };
            this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, addressRequests,
                    objectCallback);
            assertTrue(waitOnCallback(objectCallback));

            final XReadableObject object = objectCallback.getEffect()[0].getResult();
            assert object != null;
            if(type == XType.XOBJECT) {
                return object.getRevisionNumber();
            } else {
                return object.getField(address.getField()).getRevisionNumber();
            }
        }
    }

    /*
     * Test that getModelSnapshot works, even if there are revision numbers in
     * the change log that don't have events associated with them.
     */
    @Test
    public void testGetModelSnapshotWithHolesInChangeLog() {

        /* create successful events */
        final XAddress repoAddr = Base.toAddress(getRepositoryId(), null, null, null);
        final XId modelId = Base.toId("model");
        executeSucceedingCommand(MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        final XAddress modelAddr = Base.resolveModel(repoAddr, modelId);
        final XId objectId = Base.toId("object");
        executeSucceedingCommand(MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
        final XAddress objectAddr = Base.resolveObject(modelAddr, objectId);
        final XId fieldA = Base.toId("A");
        executeSucceedingCommand(MemoryObjectCommand.createAddCommand(objectAddr, true, fieldA));
        final XAddress fieldAddr = Base.resolveField(objectAddr, fieldA);
        executeSucceedingCommand(MemoryFieldCommand.createAddCommand(fieldAddr, XCommand.FORCED,
                XV.toValue("test")));
        /* create noChange events */
        executeNochangeCommand(MemoryFieldCommand.createAddCommand(fieldAddr, XCommand.FORCED,
                XV.toValue("test")));
        executeSucceedingCommand(MemoryObjectCommand.createAddCommand(objectAddr, true,
                Base.toId("B")));

        // check if we can get a snapshot
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback2 = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        final GetWithAddressRequest[] modelAddress = new GetWithAddressRequest[] { new GetWithAddressRequest(
                modelAddr) };

        this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
                callback2);
        assertTrue(waitOnCallback(callback2));

        final BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
        assertNotNull(result2);
        assertEquals(modelId, result2[0].getResult().getId());

    }

}
