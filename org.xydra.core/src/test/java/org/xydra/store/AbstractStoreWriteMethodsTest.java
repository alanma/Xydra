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
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;


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

public abstract class AbstractStoreWriteMethodsTest extends AbstractStoreTest {
	
	protected long bfQuota;
	protected XID correctUser, incorrectUser, repoID;
	
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
		
		this.bfQuota = getQuotaForBruteForce();
		
		// get the repository ID of the store
		this.repoID = getRepositoryId();
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
		        this.repoID, XX.createUniqueID(), true) };
		
		this.store.executeCommands(this.incorrectUser, this.incorrectUserPass, commands, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	/*
	 * Tests for modelCommands
	 */
	@Test
	public void testExecuteCommandsCorrectModelCommands() {
		// create a model
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddModelCommand(
		        this.repoID, modelId, true) };
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() > 0);
		assertNull((callback.getEffect())[0].getException());
		
		// check if the model was created
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		XAddress[] modelAddress = new XAddress[] { XX.toAddress(this.repoID, modelId, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableModel>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		assertEquals(result2[0].getResult().getID(), modelId);
		
		// remove the model again
		commands = new XCommand[] { X.getCommandFactory().createRemoveModelCommand(this.repoID,
		        modelId, (callback.getEffect())[0].getResult(), true) };
		
		callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() > 0);
		
		// check if the model was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		modelAddress = new XAddress[] { XX.toAddress(this.repoID, modelId, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		assertNull(result2[0].getResult());
		assertNull(result2[0].getException());
	}
	
	@Test
	public void testExecuteCommandsIncorrectModelCommands() {
		// try to remove non-existing model
		XID modelId = XX.createUniqueID();
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createRemoveModelCommand(
		        this.repoID, modelId, (callback.getEffect())[0].getResult(), true) };
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() == XCommand.FAILED);
		assertNull((callback.getEffect())[0].getException());
	}
	
	@Test
	public void testExecuteCommandsMixedCorrectModelCommands() {
		// create a model
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		int modelCount = 5;
		XID[] modelIds = new XID[modelCount];
		XCommand[] commands = new XCommand[modelCount];
		for(int i = 0; i < modelIds.length; i++) {
			commands[i] = X.getCommandFactory().createAddModelCommand(this.repoID, modelIds[i],
			        true);
		}
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == modelCount);
		assertNull(callback.getException());
		
		for(int i = 0; i < modelCount; i++) {
			assertTrue((callback.getEffect())[i].getResult() > 0);
			assertNull((callback.getEffect())[i].getException());
		}
		
		// check if the models were created
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		XAddress[] modelAddresses = new XAddress[modelCount];
		for(int i = 0; i < modelCount; i++) {
			modelAddresses[i] = XX.toAddress(this.repoID, modelIds[i], null, null);
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
			commands[i] = X.getCommandFactory().createRemoveModelCommand(this.repoID,
			        modelIds[modelCount], (callback.getEffect())[0].getResult(), true);
		}
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == modelCount);
		assertNull(callback.getException());
		
		for(int i = 0; i < modelCount; i++) {
			assertTrue((callback.getEffect())[i].getResult() > 0);
			assertNull((callback.getEffect())[i].getException());
		}
		
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
	 * Tests for objectCommands
	 */
	@Test
	public void testExecuteCommandsCorrectObjectCommands() {
		// create a model first
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelId = XX.createUniqueID();
		
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddModelCommand(
		        this.repoID, modelId, true) };
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertNull(callback.getException());
		assertTrue((callback.getEffect())[0].getResult() > 0);
		assertNull((callback.getEffect())[0].getException());
		
		// create an object
		SynchronousTestCallback<BatchedResult<Long>[]> callback2 = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID objectId = XX.createUniqueID();
		
		XCommand[] commands2 = new XCommand[] { X.getCommandFactory().createAddObjectCommand(
		        this.repoID, modelId, objectId, true) };
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands2, callback2);
		
		assertTrue(this.waitOnCallback(callback2));
		assertNotNull(callback2.getEffect());
		assertTrue(callback2.getEffect().length == 1);
		assertNull(callback2.getException());
		assertTrue((callback2.getEffect())[0].getResult() > 0);
		assertNull((callback2.getEffect())[0].getException());
		
		// check if the object was created
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback3 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] objectAddress = new XAddress[] { XX.toAddress(this.repoID, modelId, objectId,
		        null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback3);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XReadableObject>[] result2 = callback3.getEffect();
		assertNotNull(result2);
		assertEquals(result2[0].getResult().getID(), objectId);
		
		// remove the object again
		commands2 = new XCommand[] { X.getCommandFactory().createRemoveObjectCommand(this.repoID,
		        modelId, objectId, (callback.getEffect())[0].getResult(), true) };
		callback2 = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands2, callback2);
		
		assertTrue(this.waitOnCallback(callback2));
		assertNotNull(callback2.getEffect());
		assertTrue(callback2.getEffect().length == 1);
		assertNull(callback2.getException());
		assertTrue((callback2.getEffect())[0].getResult() > 0);
		
		// check if the model was removed
		callback3 = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		objectAddress = new XAddress[] { XX.toAddress(this.repoID, modelId, objectId, null) };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddress,
		        callback3);
		assertTrue(this.waitOnCallback(callback3));
		
		result2 = callback3.getEffect();
		assertNotNull(result2);
		assertNull(result2[0].getResult());
		assertNull(result2[0].getException());
	}
}
