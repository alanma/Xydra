package org.xydra.store.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.XydraStore;


/**
 * Abstract test for the write methods of {@link XydraStore}.
 * 
 * @author Björn
 */

/*
 * TODO Comments in {@link XydraStore} state, that the changes made by methods
 * like executeCommand might not be avaiable directly after the execution of the
 * commands. if this is the case, how exactly can I effectively test if the
 * changes are made? Idea: Introduce a parameter "average waiting time", which
 * describe how long it takes on averages after the changes are avaiable on the
 * store
 */

public abstract class AbstractStoreWriteMethodsTest extends AbstractStoreTest {
	
	protected XydraStore store;
	protected XCommandFactory factory;
	
	protected XID correctUser, incorrectUser, repoID;
	
	protected String correctUserPass, incorrectUserPass;
	protected long timeout;
	protected long bfQuota;
	protected boolean setUpDone = false, incorrectActorExists = true;
	
	@Before
	public void setUp() {
		
		if(this.setUpDone) {
			return;
			
			/*
			 * This code segment guarantees that the following set-up code will
			 * only be run once. This basically works like an @BeforeClass
			 * method and it not really the most beautiful solution, but
			 * unfortunately we cannot actually use a @BeforeClass method here,
			 * because this is an abstract test and we need to call abstract
			 * methods... but abstract methods cannot be static. There might be
			 * some other kind of workout around this problem, but all I could
			 * think of was/could find on the Internet were even uglier...
			 * ~Bjoern
			 */
		}
		
		this.store = this.getStore();
		this.factory = this.getCommandFactory();
		
		if(this.store == null) {
			throw new RuntimeException(
			        "XydraStore could not be initalized in the setUpClass method!");
		}
		if(this.factory == null) {
			throw new RuntimeException(
			        "XCommandFactory could not be initalized in the setUpClass method!");
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
		
		if(this.bfQuota <= 0) {
			throw new IllegalArgumentException("Quota for Login attempts must be greater than 0!");
		}
		
		// get the repository ID of the store
		this.repoID = getRepositoryId();
		
		this.setUpDone = true;
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
	
	// Test if model Commands work
	@Test
	public void testExecuteCommandsModelCommands() {
		// create a model
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XID modelID = XX.createUniqueID();
		
		// first, let's make sure that the model we want to create doesn't exist
		// check if the model was created
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback2 = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] modelAddress = new XAddress[] { XX.toAddress(this.repoID, modelID, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		BatchedResult<XBaseModel>[] result2 = callback2.getEffect();
		assertNotNull(result2);
		assertNull(result2[0].getResult());
		
		// create the model
		XCommand[] commands = new XCommand[] { X.getCommandFactory().createAddModelCommand(
		        this.repoID, modelID, true) };
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertTrue((callback.getEffect())[0].getResult() > 0);
		assertNull(callback.getException());
		
		// check if the model was created
		callback2 = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		modelAddress = new XAddress[] { XX.toAddress(this.repoID, modelID, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		assertEquals(result2[0].getResult().getID(), modelID);
		
		// remove the model again
		commands = new XCommand[] { X.getCommandFactory().createRemoveModelCommand(this.repoID,
		        modelID, (callback.getEffect())[0].getResult(), true) };
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertTrue(callback.getEffect().length == 1);
		assertTrue((callback.getEffect())[0].getResult() > 0);
		assertNull(callback.getException());
		
		// check if the model was removed
		callback2 = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		modelAddress = new XAddress[] { XX.toAddress(this.repoID, modelID, null, null) };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddress,
		        callback2);
		assertTrue(this.waitOnCallback(callback2));
		
		result2 = callback2.getEffect();
		assertNotNull(result2);
		assertNull(result2[0].getResult());
		
		// TODO Maybe split all these cases into smaller tests
	}
}
