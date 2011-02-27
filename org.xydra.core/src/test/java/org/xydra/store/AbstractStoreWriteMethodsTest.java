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
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;


/**
 * Abstract test for the write methods of {@link XydraStore}.
 * 
 * @author Bj�rn
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
	
	protected XID correctUser, incorrectUser, repoId;
	
	protected String correctUserPass, incorrectUserPass;
	
	protected XCommandFactory factory;
	protected boolean incorrectActorExists = true;
	protected XydraStore store;
	protected long timeout;
	
	@Before
	public void setUp() {
		
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
		if(!this.incorrectActorExists) {
			return;
		}
		
		SynchronousTestCallback<BatchedResult<Long>[]> callback;
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddModelCommand(
		        this.repoId, XX.createUniqueID(), true) };
		
		this.store.executeCommands(this.incorrectUser, this.incorrectUserPass, commands, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	/*
	 * Tests for RepositoryCommands
	 */
	@Test
	public void testExecuteCommandsCorrectRepoCommands() {
		// create a model
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		long modelRev = executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(
		        this.repoId, modelId, true), callback);
		
		// TODO check that the returned revision matches getModelRevisions
		
		// check if the model was created
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		XAddress[] modelAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		assertEquals(result2[0].getResult().getID(), modelId);
		
		// remove the model again
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeSucceedingCommand(X.getCommandFactory().createRemoveModelCommand(this.repoId,
		        modelId, modelRev, true), callback);
		
		// check if the model was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		modelAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		assertNull(result2[0].getResult());
		assertNull(result2[0].getException());
	}
	
	@Test
	public void testExecuteCommandsIncorrectRepoCommands() {
		// try to remove non-existing model
		XID modelId = XX.createUniqueID();
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeFailingCommand(X.getCommandFactory().createRemoveModelCommand(this.repoId, modelId,
		        42, false), callback);
		
		// add a model
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		long modelRev = executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(
		        this.repoId, modelId, true), callback);
		
		// try to remove the model but use the wrong revision number
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createRemoveModelCommand(this.repoId, modelId,
		        modelRev + 1, false), callback);
		
		// try to add the same model again with a not-forced command -> should
		// fail
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        false), callback);
		
	}
	
	@Test
	public void testExecuteCommandsMixedCorrectRepoCommands() {
		// create some models
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		int modelCount = 5;
		XID[] modelIds = new XID[modelCount];
		XCommand[] commands = new XCommand[modelCount];
		
		for(int i = 0; i < modelIds.length; i++) {
			modelIds[i] = XX.createUniqueID();
			commands[i] = X.getCommandFactory().createAddModelCommand(this.repoId, modelIds[i],
			        true);
		}
		
		this.executeSucceedingCommands(commands, callback);
		
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
			assertEquals(result2[i].getResult().getID(), modelAddresses[i].getModel());
			assertNull(result2[i].getException());
		}
		
		// remove the models again
		commands = new XCommand[modelCount];
		for(int i = 0; i < modelCount; i++) {
			commands[i] = X.getCommandFactory().createRemoveModelCommand(this.repoId, modelIds[i],
			        (callback.getEffect())[i].getResult(), true);
		}
		
		this.executeSucceedingCommands(commands, callback);
		
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
		XID modelId = XX.createUniqueID();
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		// try to remove non-existing object
		XID objectId = XX.createUniqueID();
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeFailingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
		        objectId, 42, false), callback);
		
		// add an object
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback);
		
		// try to remove the object but use the wrong revision number
		long revNr = (callback.getEffect())[0].getResult();
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
		        objectId, revNr + 1, false), callback);
		
		// try to add the same object again with a not-forced command, should
		// fail
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, false), callback);
		
	}
	
	@Test
	public void testExecuteCommandsCorrectModelCommands() {
		// create a model first
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		// create an object
		SynchronousTestCallback<BatchedResult<Long>[]> callback2 = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID objectId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback2);
		
		// check if the object was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback3 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback3);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableObject>[] result2 = callback3.getEffect();
		assertNotNull(result2);
		assertEquals(result2[0].getResult().getID(), objectId);
		
		// remove the object again
		callback2 = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createRemoveObjectCommand(this.repoId,
		        modelId, objectId, (callback.getEffect())[0].getResult(), true), callback2);
		
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
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		// create some objects
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		int objectCount = 5;
		XID[] objectIds = new XID[objectCount];
		XCommand[] commands = new XCommand[objectCount];
		for(int i = 0; i < objectIds.length; i++) {
			objectIds[i] = XX.createUniqueID();
			commands[i] = X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
			        objectIds[i], true);
		}
		
		this.executeSucceedingCommands(commands, callback);
		
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
			assertEquals(result2[i].getResult().getAddress().getModel(), modelId);
			assertEquals(result2[i].getResult().getID(), objectAddresses[i].getObject());
			assertNull(result2[i].getException());
		}
		
		// remove the objects again
		commands = new XCommand[objectCount];
		for(int i = 0; i < objectCount; i++) {
			commands[i] = X.getCommandFactory().createRemoveObjectCommand(this.repoId, modelId,
			        objectIds[i], (callback.getEffect())[i].getResult(), true);
		}
		
		this.executeSucceedingCommands(commands, callback);
		
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
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		// create an object
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID objectId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback);
		
		// create an object
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID fieldId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true), callback);
		
		// check if the field was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback));
		
		BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		XReadableObject object = result2[0].getResult();
		assertEquals(object.getID(), objectId);
		assertTrue(object.hasField(fieldId));
		
		// remove the field again
		long revNr = (callback.getEffect())[0].getResult();
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId,
		        modelId, objectId, fieldId, revNr, true), callback);
		
		// check if the field was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		object = result2[0].getResult();
		assertEquals(object.getID(), objectId);
		assertFalse(object.hasField(fieldId));
	}
	
	@Test
	public void testExecuteCommandsIncorrectObjectCommands() {
		// create a model
		XID modelId = XX.createUniqueID();
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		// add an object
		XID objectId = XX.createUniqueID();
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback);
		
		// try to remove non-existing field
		XID fieldId = XX.createUniqueID();
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeFailingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
		        objectId, fieldId, 42, false), callback);
		
		// add a field
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true), callback);
		
		// try to remove the field but use the wrong revision number
		long revNr = (callback.getEffect())[0].getResult();
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createRemoveFieldCommand(this.repoId, modelId,
		        objectId, fieldId, revNr + 1, false), callback);
		
		// try to add the same field again with a not-forced command, should
		// fail
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false), callback);
		
	}
	
	@Test
	public void testExecuteCommandsMixedCorrectObjectCommands() {
		// create a model and object first
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID objectId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback);
		
		// create some field
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		int fieldCount = 5;
		XID[] fieldIds = new XID[fieldCount];
		XCommand[] commands = new XCommand[fieldCount];
		for(int i = 0; i < fieldIds.length; i++) {
			fieldIds[i] = XX.createUniqueID();
			commands[i] = X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
			        objectId, fieldIds[i], true);
		}
		
		this.executeSucceedingCommands(commands, callback);
		
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
			        objectId, fieldIds[i], (callback.getEffect())[i].getResult(), true);
		}
		
		this.executeSucceedingCommands(commands, callback);
		
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
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		// create an object
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID objectId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback);
		
		// create a field
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID fieldId = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true), callback);
		
		// add a value
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XValue testValue = XX.createUniqueID();
		long revNr = XCommand.NEW;
		
		executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, testValue, true), callback);
		
		revNr++;
		
		// check if the field was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback));
		
		BatchedResult<XReadableObject>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		XReadableObject object = result2[0].getResult();
		assertEquals(object.getID(), objectId);
		assertTrue(object.hasField(fieldId));
		
		XReadableField field = object.getField(fieldId);
		assertNotNull(field.getValue());
		assertEquals(field.getValue(), testValue);
		
		// change the value
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XValue testValue2 = XX.createUniqueID();
		
		executeSucceedingCommand(X.getCommandFactory().createChangeValueCommand(this.repoId,
		        modelId, objectId, fieldId, revNr, testValue2, true), callback);
		revNr++;
		
		// check if the value was changed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		object = result2[0].getResult();
		assertEquals(object.getID(), objectId);
		assertTrue(object.hasField(fieldId));
		
		field = object.getField(fieldId);
		assertNotNull(field.getValue());
		assertEquals(field.getValue(), testValue2);
		assertFalse(field.getValue().equals(testValue));
		
		// remove the value again
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId,
		        modelId, objectId, fieldId, revNr, true), callback);
		revNr++;
		
		// check if the value was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoId, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		object = result2[0].getResult();
		assertEquals(object.getID(), objectId);
		assertTrue(object.hasField(fieldId));
		
		field = object.getField(fieldId);
		assertNull(field.getValue());
	}
	
	@Test
	public void testExecuteCommandsIncorrectFieldCommands() {
		// create model, object and field
		XID modelId = XX.createUniqueID();
		XID objectId = XX.createUniqueID();
		XID fieldId = XX.createUniqueID();
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddModelCommand(this.repoId, modelId,
		        true), callback);
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddObjectCommand(this.repoId, modelId,
		        objectId, true), callback);
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, true), callback);
		
		// try to remove non-existing value
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeFailingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId, modelId,
		        objectId, fieldId, XCommand.NEW, false), callback);
		
		// try to change a non-existing value
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XValue testValue = X.getValueFactory().createStringValue("Test");
		
		executeFailingCommand(X.getCommandFactory().createChangeValueCommand(this.repoId, modelId,
		        objectId, fieldId, 0, testValue, false), callback);
		
		// add a value
		long revNr = (callback.getEffect())[0].getResult();
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		executeSucceedingCommand(X.getCommandFactory().createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, testValue, true), callback);
		
		// try to remove the value but use the wrong revision number
		revNr = (callback.getEffect())[0].getResult();
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		executeFailingCommand(X.getCommandFactory().createRemoveValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr + 1, false), callback);
		
	}
	
	/**
	 * Executes the given command (which is supposed to succeed) and checks if
	 * everything went as expected.
	 * 
	 * @param command The command which is to be executed
	 * @param callback The callback which is used to get the information about
	 *            the commands success
	 */
	protected long[] executeSucceedingCommands(XCommand[] commands,
	        SynchronousTestCallback<BatchedResult<Long>[]> callback) {
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == commands.length);
		assertNull(callback.getException());
		
		long[] revisions = new long[callback.getEffect().length];
		for(int i = 0; i < commands.length; i++) {
			assertTrue((callback.getEffect())[i].getResult() >= 0);
			assertNull((callback.getEffect())[i].getException());
			revisions[i] = callback.getEffect()[i].getResult();
		}
		return revisions;
	}
	
	protected long executeSucceedingCommand(XCommand command,
	        SynchronousTestCallback<BatchedResult<Long>[]> callback) {
		return executeSucceedingCommands(new XCommand[] { command }, callback)[0];
	}
	
	/**
	 * Executes the given command (which is supposed to fail) and checks if
	 * everything went as expected.
	 * 
	 * @param command The command which is to be executed
	 * @param callback The callback which is used to get the information about
	 *            the commands failure
	 */
	protected void executeFailingCommand(XCommand command,
	        SynchronousTestCallback<BatchedResult<Long>[]> callback) {
		this.store.executeCommands(this.correctUser, this.correctUserPass,
		        new XCommand[] { command }, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() == XCommand.FAILED);
		assertNull((callback.getEffect())[0].getException());
	}
}
