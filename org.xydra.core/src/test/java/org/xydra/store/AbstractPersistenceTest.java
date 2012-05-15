package org.xydra.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.store.impl.delegate.XydraPersistence;


public abstract class AbstractPersistenceTest {
	/*
	 * the following variables need to be instantiated in the @Before methd by
	 * implementations of this test
	 * 
	 * - persistence needs to be an empty XydraPersistence
	 * 
	 * - comFactory needs to be an implementation of XCommandFactory which
	 * creates commands that can be executed by persistence.
	 */
	public XydraPersistence persistence;
	public XCommandFactory comFactory;
	
	public XID actorId = X.getIDProvider().fromString("actorId");
	
	/*
	 * TODO also test forced commands! (for example by adding a model, executing
	 * some changes on it, and then adding a new model with the same Id -> this
	 * way we can actually check if the managed model is changed by the forced
	 * command (i.e. in this case a new model needs to be created)
	 * 
	 * TODO is the above description correct? How are forced commands handled
	 * exactly? Check this!
	 */
	
	@Test
	public void testExecuteCommandRepositoryCommandAddType() {
		/*
		 * add a new model, should succeed
		 */
		XID repoId = XX.createUniqueId();
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.resolveModel(repoId, modelId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(repoId, modelId, false);
		
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Executing \"Adding a new model\"-command failed (should succeed), revNr was "
		        + revNr, revNr > 0);
		
		// check that the model actually exists
		GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(addressRequest);
		assertNotNull(
		        "The model we tried to create with an \"Adding a new model\"-command actually wasn't correctly added.",
		        model);
		
		/*
		 * try to add the same model again (with an unforced command), should
		 * fail
		 */
		
		long failRevNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue(
		        "Trying to add an already existing model with an unforced event succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
		
		/*
		 * delete the model again
		 */
		XCommand deleteModelCom = this.comFactory.createRemoveModelCommand(repoId, modelId,
		        model.getRevisionNumber(), false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteModelCom);
		
		assertTrue(
		        "Executing \"Deleting an existing model\"-command failed (should succeed), revNr was "
		                + revNr, revNr > 0);
		// check that the model was actually removed
		addressRequest = new GetWithAddressRequest(modelAddress);
		model = this.persistence.getModelSnapshot(addressRequest);
		assertNull(
		        "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
		        model);
		
		/*
		 * try to remove a not existing model
		 * 
		 * TODO check how forced & unforced commands behave in this case
		 */
		repoId = XX.createUniqueId();
		modelId = XX.createUniqueId();
		XCommand deleteNotExistingModelCom = this.comFactory.createRemoveModelCommand(repoId,
		        modelId, 0, false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingModelCom);
		
		assertTrue(
		        "Removing a not existing command with an unforced command succeeded (should fail), revNr was "
		                + revNr, revNr == XCommand.FAILED);
		
	}
	
	@Test
	public void testExecuteCommandRepositoryCommandAddRemove() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandModelCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandModelCommandRemoveType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandObjectCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandObjectCommandRemoveType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandFieldCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandFieldCommandRemoveType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandFieldCommandChangeType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandSimpleTransaction() {
		// TODO write this test
		// TODO write multiple tests for Transactions, since they are pretty
		// complex
	}
	
	@Test
	public void testGetEvents() {
		/*
		 * TODO write this test - execute some simple (and maybe some complex)
		 * commands & transactions and check if the returned events match
		 */
	}
	
	@Test
	public void testGetManagedModelIds() {
		/*
		 * TODO write this test - add some models and check whether the corret
		 * Ids are returned or not. (take into account that deleting a model
		 * does not necessarily remove its Id!)
		 */
	}
	
	@Test
	public void testGetModelRevision() {
		/*
		 * TODO write this test - add & change some models and check their
		 * revision numbers
		 */
	}
	
	@Test
	public void testGetModelSnapshot() {
		/*
		 * TODO write this test - create a model and execute some commands on
		 * it, manage a separate model in parallel, execute the same commands on
		 * that one and compare the returned model to the self-managed model
		 */
		// TODO check what a "tentative revision" is and write test accordingly
	}
	
	@Test
	public void testGetObjectSnapshot() {
		/*
		 * TODO write this test - just like testgetModelSnapshot, but with
		 * Objects
		 */
	}
	
	@Test
	public void testGetRepositoryId() {
		/*
		 * TODO write this test (is there anything that needs to be tested
		 * here?)
		 */
	}
	
	@Test
	public void testHasManagedModel() {
		// TODO write this test
	}
}
