package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Abstract test for the write methods of {@link XydraStore}.
 * 
 * @author Kaidel
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
	
	protected XID correctUser, incorrectUser, repoId;
	
	protected String correctUserPass, incorrectUserPass;
	
	protected XCommandFactory factory;
	protected boolean incorrectActorExists = true;
	protected XydraStore store;
	protected long timeout;
	
	@Before
	public void before() {
		this.store = this.getStore();
		this.factory = this.getCommandFactory();
		
		if(this.store == null) {
			throw new RuntimeException("XydraStore could not be initalized in the setUp method!");
		}
		if(this.factory == null) {
			throw new RuntimeException(
			        "XCommandFactory could not be initalized in the setUp method!");
		}
		
		this.correctUser = this.getCorrectUser();
		this.correctUserPass = this.getCorrectUserPasswordHash();
		
		if(this.correctUser == null || this.correctUserPass == null) {
			throw new IllegalArgumentException("correctUser or correctUserPass were null");
		}
		
		this.incorrectUser = this.getIncorrectUser();
		this.incorrectUserPass = this.getIncorrectUserPasswordHash();
		this.incorrectActorExists = (this.incorrectUser != null);
		
		this.timeout = getCallbackTimeout();
		
		if(this.timeout <= 0) {
			throw new IllegalArgumentException("Timeout for callbacks must be greater than 0!");
		}
		
		// get the repository ID of the store
		this.repoId = getRepositoryId();
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
		
		SynchronousTestCallback<BatchedResult<Long>[]> callback;
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddModelCommand(
		        this.repoId, XX.createUniqueId(), true) };
		
		this.store.executeCommands(this.incorrectUser, this.incorrectUserPass, commands, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
		log.info("/TEST testExecuteCommandsBadAccount");
	}
	
	/*
	 * Tests for RepositoryCommands
	 */
	@Test
	public void testExecuteCommandsCorrectRepoCommands() {
		// create a model
		XID modelId = XX.createUniqueId();
		
		long modelRev = executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(
		        this.repoId, modelId, true));
		
		// TODO check that the returned revision matches getModelRevisions
		
		// check if the model was created
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		XAddress[] modelAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		assertNotNull(result2[0].getResult());
		assertEquals(modelId, result2[0].getResult().getID());
		
		// remove the model again
		
		executeSucceedingCommand(X.getCommandFactory().createRemoveModelCommand(this.repoId,
		        modelId, modelRev, true));
		
		// check if the model was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		modelAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		// FIXME why expect null?
		assertNull("expected null, got: " + result2[0].getResult(), result2[0].getResult());
		assertNull(result2[0].getException());
	}
	
	@Test
	public void testExecuteCommandsIncorrectRepoCommands() {
		// try to remove non-existing model
		XID modelId = XX.createUniqueId();
		
		executeFailingCommand(X.getCommandFactory().createRemoveModelCommand(this.repoId, modelId,
		        42, false));
		
		// add a model
		long modelRev = executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(
		        this.repoId, modelId, true));
		
		// try to remove the model but use the wrong revision number
		executeFailingCommand(X.getCommandFactory().createRemoveModelCommand(this.repoId, modelId,
		        modelRev + 1, false));
		
		// try to add the same model again with a not-forced command -> should
		// fail
		executeFailingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        false));
		
	}
	
	@Test
	public void testExecuteCommandsMixedCorrectRepoCommands() {
		
		// create some models
		int modelCount = 5;
		XID[] modelIds = new XID[modelCount];
		XCommand[] commands = new XCommand[modelCount];
		
		for(int i = 0; i < modelIds.length; i++) {
			modelIds[i] = XX.createUniqueId();
			commands[i] = X.getCommandFactory().createAddModelCommand(this.repoId, modelIds[i],
			        true);
		}
		
		long[] modelRevs = this.executeSucceedingCommands(commands);
		
		// check if the models were created
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		XAddress[] modelAddresses = new XAddress[modelCount];
		for(int i = 0; i < modelCount; i++) {
			modelAddresses[i] = XX.toAddress(this.repoId, modelIds[i], null, null);
		}
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		assertNull(callback2.getException());
		
		BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		for(int i = 0; i < modelCount; i++) {
			assertEquals(modelAddresses[i].getModel(), result2[i].getResult().getID());
			assertNull(result2[i].getException());
		}
		
		// remove the models again
		commands = new XCommand[modelCount];
		for(int i = 0; i < modelCount; i++) {
			commands[i] = X.getCommandFactory().createRemoveModelCommand(this.repoId, modelIds[i],
			        modelRevs[i], true);
		}
		
		this.executeSucceedingCommands(commands);
		
		// check if the models were removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
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
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// try to remove non-existing object
		XID objectId = XX.createUniqueId();
		
		executeFailingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
		        objectId, 42, false));
		
		// add an object
		long revNr = executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(
		        this.repoId, modelId, objectId, true));
		
		// try to remove the object but use the wrong revision number
		executeFailingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
		        objectId, revNr + 1, false));
		
		// try to add the same object again with a not-forced command, should
		// fail
		executeFailingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, false));
		
	}
	
	@Test
	public void testExecuteCommandsCorrectModelCommands() {
		
		// create a model first
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create an object
		XID objectId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// check if the object was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback3 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback3);
		assertTrue(this.waitOnCallback(callback3));
		
		BatchedResult<XReadableObject>[] result2 = callback3.getEffect();
		assertNotNull(result2);
		assertNotNull(result2[0]);
		assertNotNull(result2[0].getResult());
		assertEquals(objectId, result2[0].getResult().getID());
		
		// remove the object again
		executeSucceedingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId,
		        modelId, objectId, XCommand.FORCED, true));
		
		// check if the object was removed
		callback3 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback3);
		assertTrue(this.waitOnCallback(callback3));
		
		result2 = callback3.getEffect();
		assertNotNull(result2);
		assertNull(result2[0].getResult());
		assertNull(result2[0].getException());
	}
	
	@Test
	public void testExecuteCommandsMixedCorrectModelCommands() {
		// create a model first
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create some objects
		
		int objectCount = 5;
		XID[] objectIds = new XID[objectCount];
		XCommand[] commands = new XCommand[objectCount];
		for(int i = 0; i < objectIds.length; i++) {
			objectIds[i] = XX.createUniqueId();
			commands[i] = X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
			        objectIds[i], true);
		}
		
		long[] revs = this.executeSucceedingCommands(commands);
		
		// check if the objects were created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		XAddress[] objectAddresses = new XAddress[objectCount];
		for(int i = 0; i < objectCount; i++) {
			objectAddresses[i] = XX.toAddress(this.repoId, modelId, objectIds[i], null);
		}
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddresses,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		assertNull(callback2.getException());
		
		BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		for(int i = 0; i < objectCount; i++) {
			assertEquals(modelId, result2[i].getResult().getAddress().getModel());
			assertEquals(objectAddresses[i].getObject(), result2[i].getResult().getID());
			assertNull(result2[i].getException());
		}
		
		// remove the objects again
		commands = new XCommand[objectCount];
		for(int i = 0; i < objectCount; i++) {
			commands[i] = X.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
			        objectIds[i], revs[i], true);
		}
		
		this.executeSucceedingCommands(commands);
		
		// check if the object were removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddresses,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
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
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create an object
		XID objectId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// create an object
		XID fieldId = XX.createUniqueId();
		
		long revNr = executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(
		        this.repoId, modelId, objectId, fieldId, true));
		
		// check if the field was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		XReadableObject object = result2[0].getResult();
		assertEquals(objectId, object.getID());
		assertTrue(object.hasField(fieldId));
		
		// remove the field again
		
		executeSucceedingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId,
		        modelId, objectId, fieldId, revNr, true));
		
		// check if the field was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		object = result2[0].getResult();
		assertEquals(objectId, object.getID());
		assertFalse(object.hasField(fieldId));
	}
	
	@Test
	public void testExecuteCommandsIncorrectObjectCommands() {
		// create a model
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// add an object
		XID objectId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// try to remove non-existing field
		XID fieldId = XX.createUniqueId();
		
		executeFailingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
		        objectId, fieldId, 42, false));
		
		// add a field
		long revNr = executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(
		        this.repoId, modelId, objectId, fieldId, true));
		
		// try to remove the field but use the wrong revision number
		
		executeFailingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
		        objectId, fieldId, revNr + 1, false));
		
		// try to add the same field again with a not-forced command, should
		// fail
		executeFailingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false));
		
	}
	
	@Test
	public void testExecuteCommandsMixedCorrectObjectCommands() {
		// create a model and object first
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// create some field
		int fieldCount = 5;
		XID[] fieldIds = new XID[fieldCount];
		XCommand[] commands = new XCommand[fieldCount];
		for(int i = 0; i < fieldIds.length; i++) {
			fieldIds[i] = XX.createUniqueId();
			commands[i] = X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
			        objectId, fieldIds[i], true);
		}
		
		long[] revs = this.executeSucceedingCommands(commands);
		
		// check if the fields were created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		assertNull(callback2.getException());
		
		BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
		assertNull(result2[0].getException());
		assertNotNull(result2);
		for(int i = 0; i < fieldCount; i++) {
			assertNotNull(result2[0].getResult().getField(fieldIds[i]));
		}
		
		// remove the fields again
		commands = new XCommand[fieldCount];
		for(int i = 0; i < fieldCount; i++) {
			commands[i] = X.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
			        objectId, fieldIds[i], revs[i], true);
		}
		
		this.executeSucceedingCommands(commands);
		
		// check if the fields were removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
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
		XID modelId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create an object
		XID objectId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// create a field
		XID fieldId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// add a value
		XValue testValue = XX.createUniqueId();
		long revNr = XCommand.NEW;
		
		executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, testValue, true));
		
		revNr++;
		
		// check if the field was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		XReadableObject object = result2[0].getResult();
		assertEquals(objectId, object.getID());
		assertTrue(object.hasField(fieldId));
		
		XReadableField field = object.getField(fieldId);
		assertNotNull(field.getValue());
		assertEquals(testValue, field.getValue());
		
		// change the value
		XValue testValue2 = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createChangeValueCommand(this.repoId,
		        modelId, objectId, fieldId, revNr, testValue2, true));
		revNr++;
		
		// check if the value was changed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		object = result2[0].getResult();
		assertEquals(objectId, object.getID());
		assertTrue(object.hasField(fieldId));
		
		field = object.getField(fieldId);
		assertNotNull(field.getValue());
		assertEquals(testValue2, field.getValue());
		assertFalse(field.getValue().equals(testValue));
		
		// remove the value again
		executeSucceedingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId,
		        modelId, objectId, fieldId, revNr, true));
		revNr++;
		
		// check if the value was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		object = result2[0].getResult();
		assertEquals(objectId, object.getID());
		assertTrue(object.hasField(fieldId));
		
		field = object.getField(fieldId);
		assertNull(field.getValue());
	}
	
	@Test
	public void testExecuteCommandsIncorrectFieldCommands() {
		// create model, object and field
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// try to remove non-existing value
		executeFailingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId, modelId,
		        objectId, fieldId, XCommand.NEW, false));
		
		// try to change a non-existing value
		
		XValue testValue = X.getValueFactory().createStringValue("Test");
		
		executeFailingCommand(X.getCommandFactory().createChangeValueCommand(this.repoId, modelId,
		        objectId, fieldId, 0, testValue, false));
		
		// add a value
		long revNr = executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(
		        this.repoId, modelId, objectId, fieldId, XCommand.FORCED, testValue, true));
		
		// try to remove the value but use the wrong revision number
		executeFailingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId, modelId,
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
		
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback;
		
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		
		GetEventsRequest[] requests = new GetEventsRequest[1];
		
		// Add a valid request.
		requests[0] = new GetEventsRequest(XX.toAddress("/data/somewhere"), 0, 1);
		
		this.store.getEvents(this.incorrectUser, this.incorrectUserPass, requests, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	@Test
	public void testGetEventsBadRevisions() {
		// create a model first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create an object and check if event is being thrown
		XID objectId = XX.createUniqueId();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		
		/*
		 * begin revision is greater than end revision - should throw a
		 * RequestException according to the {@link XydraStore} interface
		 * documentation.
		 */
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev + 1, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNull(callback.getEffect()[0].getResult());
		Throwable exception = callback.getEffect()[0].getException();
		assertNotNull(exception);
		assertTrue(exception instanceof RequestException);
	}
	
	@Test
	public void testGetEventsBadAddress() {
		XAddress randomAddress = XX.toAddress(this.repoId, XX.createUniqueId(), null, null);
		
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(randomAddress,
		        0, 1) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
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
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create an object and check if event is being thrown
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		
		checkEvent(event, objectAddress, ChangeType.ADD, XType.XMODEL, modelRev);
	}
	
	@Test
	public void testGetEventsModelEventsRemoveType() {
		// create a model first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		// create an object and check if event is being thrown
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		// remove the object again
		executeSucceedingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId,
		        modelId, objectId, XCommand.FORCED, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		
		// check if event was thrown
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		checkEvent(event, objectAddress, ChangeType.REMOVE, XType.XMODEL, modelRev);
	}
	
	// Tests for Object Events
	@Test
	public void testGetEventsObjectEventsAddType() {
		// create a model and object first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// create a field and check if event is being thrown
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		
		// get event from model first
		// get the event
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		checkEvent(event, fieldAddress, ChangeType.ADD, XType.XOBJECT, objectRev);
		
		// get event from object
		request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event2 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event2);
	}
	
	@Test
	public void testGetEventsObjectEventsRemoveType() {
		// create a model and object first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// create and remove a field and check if event is being thrown
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		executeSucceedingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId,
		        modelId, objectId, fieldId, XCommand.FORCED, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		
		// check if event was thrown
		// get event from model first
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		checkEvent(event, fieldAddress, ChangeType.REMOVE, XType.XOBJECT, objectRev);
		
		// get event from object
		request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event2 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event2);
	}
	
	// Tests for Field Events
	@Test
	public void testGetEventsFieldEventsAddType() {
		// create a model, object and field first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// add a value to the field and check, if event is being thrown
		long fieldRev = getRevisionNumber(fieldAddress);
		
		XValue value1 = XX.createUniqueId();
		executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, fieldRev, value1, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		fieldRev = getRevisionNumber(fieldAddress);
		
		// get event from model first
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(callback.getEffect()[0].getResult().length, 1);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		checkEvent(event, fieldAddress, ChangeType.ADD, XType.XFIELD, fieldRev);
		
		assertEquals(value1, ((XFieldEvent)event).getNewValue());
		
		// get event from object
		request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event2 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event2);
		
		// get event from field
		request = new GetEventsRequest[] { new GetEventsRequest(fieldAddress, fieldRev, fieldRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event3 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event3);
	}
	
	@Test
	public void testGetEventsFieldEventsChangeType() {
		// create a model, object and field first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// add a value to the field, change it and check, if event is being
		// thrown
		long fieldRev = getRevisionNumber(fieldAddress);
		
		XValue value1 = XX.createUniqueId();
		executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, fieldRev, value1, true));
		// change the value
		XValue value2 = XX.createUniqueId();
		executeSucceedingCommand(X.getCommandFactory().createChangeValueCommand(this.repoId,
		        modelId, objectId, fieldId, fieldRev, value2, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		fieldRev = getRevisionNumber(fieldAddress);
		
		// get event from model first
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		checkEvent(event, fieldAddress, ChangeType.CHANGE, XType.XFIELD, fieldRev);
		
		assertEquals(value2, ((XFieldEvent)event).getNewValue());
		
		// get event from object
		request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event2 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event2);
		
		// get event from field
		request = new GetEventsRequest[] { new GetEventsRequest(fieldAddress, fieldRev, fieldRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event3 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event3);
	}
	
	@Test
	public void testGetEventsFieldEventsRemoveType() {
		// create a model, object and field first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// add a value to the field, change it and check, if event is being
		// thrown
		long fieldRev = getRevisionNumber(fieldAddress);
		
		XValue value1 = XX.createUniqueId();
		executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, fieldRev, value1, true));
		
		// remove the value and check if event is being thrown
		executeSucceedingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId,
		        modelId, objectId, fieldId, XCommand.FORCED, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		fieldRev = getRevisionNumber(fieldAddress);
		
		// get event from model first
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev, modelRev) };
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event = callback.getEffect()[0].getResult()[0];
		checkEvent(event, fieldAddress, ChangeType.REMOVE, XType.XFIELD, fieldRev);
		
		assertEquals(null, ((XFieldEvent)event).getNewValue());
		
		// get event from object
		request = new GetEventsRequest[] { new GetEventsRequest(objectAddress, objectRev, objectRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event2 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event2);
		
		// get event from field
		request = new GetEventsRequest[] { new GetEventsRequest(fieldAddress, fieldRev, fieldRev) };
		callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		this.store.getEvents(this.correctUser, this.correctUserPass, request, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect()[0].getResult());
		assertEquals(1, callback.getEffect()[0].getResult().length);
		
		XEvent event3 = callback.getEffect()[0].getResult()[0];
		assertEquals(event, event3);
	}
	
	/*
	 * Tests for executeCommandsAndGetEvents
	 */
	// Tests for Model Events
	@Test
	public void testExecuteCommandsAndGetEventsModelEventsAddType() {
		// create a model first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddObjectCommand(
		        this.repoId, modelId, objectId, true) };
		
		/*
		 * revision number is incremented in the requests because a command will
		 * be executed before they are used
		 */

		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev + 1, modelRev + 1) };
		
		SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();
		
		this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
		        request, callback);
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect().getFirst());
		assertNotNull(callback.getEffect().getSecond());
		
		BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
		BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();
		
		assertEquals(1, commandResult.length);
		assertEquals(1, eventResult.length);
		
		assertNotNull(commandResult[0].getResult());
		assertNull(commandResult[0].getException());
		assertEquals((modelRev + 1), (long)commandResult[0].getResult());
		
		assertNotNull(eventResult[0].getResult());
		assertNull(eventResult[0].getException());
		assertEquals(1, eventResult[0].getResult().length);
		
		XEvent event = eventResult[0].getResult()[0];
		
		checkEvent(event, objectAddress, ChangeType.ADD, XType.XMODEL, modelRev + 1);
	}
	
	@Test
	public void testExecuteCommandsAndGetEventsModelEventsRemoveType() {
		// create a model first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createRemoveObjectCommand(
		        this.repoId, modelId, objectId, XCommand.FORCED, true) };
		
		/*
		 * revision number is incremented in the requests because a command will
		 * be executed before they are used
		 */
		GetEventsRequest[] request = new GetEventsRequest[] { new GetEventsRequest(modelAddress,
		        modelRev + 1, modelRev + 1) };
		
		SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();
		
		this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
		        request, callback);
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect().getFirst());
		assertNotNull(callback.getEffect().getSecond());
		
		BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
		BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();
		
		assertEquals(1, commandResult.length);
		assertEquals(1, eventResult.length);
		
		assertNotNull(commandResult[0].getResult());
		assertNull(commandResult[0].getException());
		assertEquals((modelRev + 1), (long)commandResult[0].getResult());
		
		assertNotNull(eventResult[0].getResult());
		assertNull(eventResult[0].getException());
		assertEquals(1, eventResult[0].getResult().length);
		
		XEvent event = eventResult[0].getResult()[0];
		
		checkEvent(event, objectAddress, ChangeType.REMOVE, XType.XMODEL, modelRev + 1);
	}
	
	// Tests for Object Events
	@Test
	public void testExecuteCommandsAndGetEventsObjectEventsAddType() {
		// create a model & object first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddFieldCommand(
		        this.repoId, modelId, objectId, fieldId, true) };
		
		/*
		 * revision number is incremented in the requests because a command will
		 * be executed before they are used
		 */
		GetEventsRequest[] request = new GetEventsRequest[] {
		        new GetEventsRequest(modelAddress, modelRev + 1, modelRev + 1),
		        new GetEventsRequest(objectAddress, objectRev + 1, objectRev + 1) };
		
		SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();
		
		this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
		        request, callback);
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect().getFirst());
		assertNotNull(callback.getEffect().getSecond());
		
		BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
		BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();
		
		assertEquals(1, commandResult.length);
		assertEquals(2, eventResult.length);
		
		assertNotNull(commandResult[0].getResult());
		assertNull(commandResult[0].getException());
		assertEquals((modelRev + 1), (long)commandResult[0].getResult());
		
		assertNotNull(eventResult[0].getResult());
		assertNull(eventResult[0].getException());
		assertEquals(1, eventResult[0].getResult().length);
		
		assertNotNull(eventResult[1].getResult());
		assertNull(eventResult[1].getException());
		assertEquals(1, eventResult[1].getResult().length);
		
		// check event returned by the model first
		XEvent event = eventResult[0].getResult()[0];
		checkEvent(event, fieldAddress, ChangeType.ADD, XType.XOBJECT, objectRev + 1);
		// check the event returned by the object
		assertEquals(event, eventResult[1].getResult()[0]);
	}
	
	@Test
	public void testExecuteCommandsAndGetEventsObjectEventsRemoveType() {
		// create a model & object first
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.toAddress(this.repoId, modelId, null, null);
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true));
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.toAddress(this.repoId, modelId, objectId, null);
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true));
		
		XID fieldId = XX.createUniqueId();
		XAddress fieldAddress = XX.toAddress(this.repoId, modelId, objectId, fieldId);
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true));
		
		// get the right revision numbers
		long modelRev = getRevisionNumber(modelAddress);
		long objectRev = getRevisionNumber(objectAddress);
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createRemoveFieldCommand(
		        this.repoId, modelId, objectId, fieldId, XCommand.FORCED, true) };
		
		/*
		 * revision number is incremented in the requests because a command will
		 * be executed before they are used
		 */
		GetEventsRequest[] request = new GetEventsRequest[] {
		        new GetEventsRequest(modelAddress, modelRev + 1, modelRev + 1),
		        new GetEventsRequest(objectAddress, objectRev + 1, objectRev + 1) };
		
		SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();
		
		this.store.executeCommandsAndGetEvents(this.correctUser, this.correctUserPass, commands,
		        request, callback);
		assertTrue(this.waitOnCallback(callback));
		assertNull(callback.getException());
		assertNotNull(callback.getEffect());
		assertNotNull(callback.getEffect().getFirst());
		assertNotNull(callback.getEffect().getSecond());
		
		BatchedResult<Long>[] commandResult = callback.getEffect().getFirst();
		BatchedResult<XEvent[]>[] eventResult = callback.getEffect().getSecond();
		
		assertEquals(1, commandResult.length);
		assertEquals(2, eventResult.length);
		
		assertNotNull(commandResult[0].getResult());
		assertNull(commandResult[0].getException());
		assertEquals((modelRev + 1), (long)commandResult[0].getResult());
		
		assertNotNull(eventResult[0].getResult());
		assertNull(eventResult[0].getException());
		assertEquals(1, eventResult[0].getResult().length);
		
		assertNotNull(eventResult[1].getResult());
		assertNull(eventResult[1].getException());
		assertEquals(1, eventResult[1].getResult().length);
		
		// check event returned by the model first
		XEvent event = eventResult[0].getResult()[0];
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
	protected long[] executeSucceedingCommands(XCommand[] commands) {
		
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue("Callback failed with " + callback.getException(), this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == commands.length);
		assertNull(callback.getException());
		
		long[] revisions = new long[commands.length];
		for(int i = 0; i < commands.length; i++) {
			assertTrue((callback.getEffect())[i].getResult() >= 0);
			assertNull((callback.getEffect())[i].getException());
			revisions[i] = callback.getEffect()[i].getResult();
		}
		return revisions;
	}
	
	protected long executeSucceedingCommand(XCommand command) {
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
	protected void executeFailingCommand(XCommand command) {
		
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		this.store.executeCommands(this.correctUser, this.correctUserPass,
		        new XCommand[] { command }, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() == XCommand.FAILED);
		assertNull((callback.getEffect())[0].getException());
	}
	
	/**
	 * Executes the given command (which is supposed to succeed but not change
	 * anything) and checks if everything went as expected.
	 * 
	 * @param command The command which is to be executed
	 * @param callback The callback which is used to get the information about
	 *            the commands failure
	 */
	protected void executeNochangeCommand(XCommand command) {
		
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		this.store.executeCommands(this.correctUser, this.correctUserPass,
		        new XCommand[] { command }, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() == XCommand.NOCHANGE);
		assertNull((callback.getEffect())[0].getException());
	}
	
	// private, because it makes assumptions about the tests
	private void checkEvent(XEvent event, XAddress changedEntity, ChangeType type,
	        XType expectedType, long revision) {
		assertEquals(changedEntity, event.getChangedEntity());
		assertEquals(this.correctUser, event.getActor());
		assertEquals(type, event.getChangeType());
		
		switch(expectedType) {
		case XMODEL:
			assertEquals(changedEntity.getParent(), event.getTarget());
			
			assertTrue(event instanceof XModelEvent);
			XModelEvent modelEvent = (XModelEvent)event;
			assertEquals(this.repoId, modelEvent.getRepositoryId());
			assertEquals(changedEntity.getModel(), modelEvent.getModelId());
			assertEquals(changedEntity.getObject(), modelEvent.getObjectId());
			break;
		case XOBJECT:
			assertEquals(changedEntity.getParent(), event.getTarget());
			
			assertTrue(event instanceof XObjectEvent);
			XObjectEvent objectEvent = (XObjectEvent)event;
			assertEquals(this.repoId, objectEvent.getRepositoryId());
			assertEquals(changedEntity.getModel(), objectEvent.getModelId());
			assertEquals(changedEntity.getObject(), objectEvent.getObjectId());
			assertEquals(changedEntity.getField(), objectEvent.getFieldId());
			
			break;
		case XFIELD:
			assertEquals(changedEntity, event.getTarget());
			
			assertTrue(event instanceof XFieldEvent);
			XFieldEvent fieldEvent = (XFieldEvent)event;
			assertEquals(this.repoId, fieldEvent.getRepositoryId());
			assertEquals(changedEntity.getModel(), fieldEvent.getModelId());
			assertEquals(changedEntity.getObject(), fieldEvent.getObjectId());
			assertEquals(changedEntity.getField(), fieldEvent.getFieldId());
			break;
		case XREPOSITORY:
			// TODO implement
			break;
		}
		
		// check revision numbers
		assertEquals(revision, event.getRevisionNumber());
		switch(expectedType) {
		// attention: break-statements are missing on purpose
		
		/*
		 * since the tests never do anything more than one change at a time, we
		 * may use "revision -1" here for the old revisions
		 */
		case XFIELD:
			assertEquals(revision - 1, event.getOldFieldRevision());
			//$FALL-THROUGH$
		case XOBJECT:
			if(event.getOldObjectRevision() != XEvent.RevisionNotAvailable) {
				assertEquals(revision - 1, event.getOldObjectRevision());
			}
			//$FALL-THROUGH$
		case XMODEL:
			assertEquals(revision - 1, event.getOldModelRevision());
			//$FALL-THROUGH$
		case XREPOSITORY:
		}
	}
	
	protected long getRevisionNumber(XAddress address) {
		assert address.getAddressedType() != XType.XREPOSITORY;
		XAddress[] addresses;
		XType type = address.getAddressedType();
		
		if(type == XType.XMODEL) {
			SynchronousTestCallback<BatchedResult<RevisionState>[]> revCallback = new SynchronousTestCallback<BatchedResult<RevisionState>[]>();
			addresses = new XAddress[] { address };
			this.store.getModelRevisions(this.correctUser, this.correctUserPass, addresses,
			        revCallback);
			assertTrue(this.waitOnCallback(revCallback));
			
			return revCallback.getEffect()[0].getResult().revision();
		} else {
			SynchronousTestCallback<BatchedResult<XReadableObject>[]> objectCallback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
			
			addresses = new XAddress[] { XX.toAddress(address.getRepository(), address.getModel(),
			        address.getObject(), null) };
			this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, addresses,
			        objectCallback);
			assertTrue(this.waitOnCallback(objectCallback));
			
			XReadableObject object = objectCallback.getEffect()[0].getResult();
			if(type == XType.XOBJECT) {
				return object.getRevisionNumber();
			} else {
				return object.getField(address.getField()).getRevisionNumber();
			}
		}
	}
	
	/*
	 * Test that getModelSnapshot works if there revision numbers in the change
	 * log that don't have events associated with them.
	 */
	@Test
	public void testGetModelSnapshotWithHolesInChangeLog() {
		
		XAddress repoAddr = XX.toAddress(getRepositoryId(), null, null, null);
		XID modelId = XX.toId("model");
		
		executeSucceedingCommand(MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
		
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		XID objectId = XX.toId("object");
		executeSucceedingCommand(MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		XID fieldA = XX.toId("A");
		executeSucceedingCommand(MemoryObjectCommand.createAddCommand(objectAddr, true, fieldA));
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldA);
		executeSucceedingCommand(MemoryFieldCommand.createAddCommand(fieldAddr, XCommand.FORCED,
		        XV.toValue("test")));
		executeNochangeCommand(MemoryFieldCommand.createAddCommand(fieldAddr, XCommand.FORCED,
		        XV.toValue("test")));
		executeSucceedingCommand(MemoryObjectCommand.createAddCommand(objectAddr, true,
		        XX.toId("B")));
		
		// check if we can get a snapshot
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		XAddress[] modelAddress = new XAddress[] { modelAddr };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		assertEquals(modelId, result2[0].getResult().getID());
		
	}
	
}
