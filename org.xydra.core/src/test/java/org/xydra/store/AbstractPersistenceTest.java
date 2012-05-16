package org.xydra.store;

import static org.junit.Assert.assertEquals;
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
import org.xydra.base.rmof.XWritableObject;
import org.xydra.store.impl.delegate.XydraPersistence;


public abstract class AbstractPersistenceTest {
	/*
	 * the following variables need to be instantiated in the @Before methd by
	 * implementations of this test
	 * 
	 * - persistence needs to be an empty XydraPersistence with this.repoId as
	 * its repository id.
	 * 
	 * - comFactory needs to be an implementation of XCommandFactory which
	 * creates commands that can be executed by persistence.
	 */
	public XydraPersistence persistence;
	public XCommandFactory comFactory;
	
	public XID repoId = X.getIDProvider().fromString("testRepo");
	public XID actorId = X.getIDProvider().fromString("testActor");
	
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
		 * RepositoryCommands of add type add new models to the repository.
		 */
		
		/*
		 * add a new model, should succeed
		 */
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Executing \"Adding a new model\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the model actually exists
		GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(addressRequest);
		assertNotNull(
		        "The model we tried to create with an \"Adding a new model\"-command actually wasn't correctly added.",
		        model);
		assertEquals("Returned model snapshot did not have the correct XAddress.", modelAddress,
		        model.getAddress());
		assertEquals("Returned model did not have the correct revision number.", revNr,
		        model.getRevisionNumber());
		
		/*
		 * try to add the same model again (with an unforced command), should
		 * fail
		 */
		
		long failRevNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue(
		        "Trying to add an already existing model with an unforced event succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
		
	}
	
	@Test
	public void testExecuteCommandRepositoryCommandRemoveType() {
		/*
		 * RepositoryCommands of the remove type remove models from the
		 * repository
		 */
		
		/*
		 * add a model, we'll delete afterwards
		 */
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);
		
		GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(addressRequest);
		
		assertNotNull("The model we tried to add doesn't exist, test cannot be executed.", model);
		assertEquals(
		        "The returned model has the wrong address, test cannot be executed, since it seems like adding models doesn't work correctly.",
		        modelAddress, model.getAddress());
		
		/*
		 * delete the model again
		 */
		XCommand deleteModelCom = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
		        model.getRevisionNumber(), false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteModelCom);
		
		assertTrue(
		        "Executing \"Deleting an existing model\"-command failed (should succeed), revNr was "
		                + revNr, revNr >= 0);
		// check that the model was actually removed
		model = this.persistence.getModelSnapshot(addressRequest);
		assertNull(
		        "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
		        model);
		
		/*
		 * try to remove a not existing model
		 * 
		 * TODO check how forced & unforced commands behave in this case
		 */
		
		modelId = XX.createUniqueId();
		XCommand deleteNotExistingModelCom = this.comFactory.createRemoveModelCommand(this.repoId,
		        modelId, 0, false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingModelCom);
		
		assertTrue(
		        "Removing a not existing model with an unforced command succeeded (should fail), revNr was "
		                + revNr, revNr == XCommand.FAILED);
		
		/*
		 * TODO also test that removing a model also removes the object
		 * (snapshots) and their fields
		 */
	}
	
	@Test
	public void testExecuteCommandModelCommandAddType() {
		/*
		 * ModelCommands of add type add new objects to models.
		 */
		
		// add a model on which the commands can be executed first
		
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object, should succeed
		 */
		
		XID objectId = XX.createUniqueId();
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("Executing \"Adding a new object\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the object actually exists
		GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
		
		assertNotNull(
		        "Model Snapshot Failure: The object we tried to create with an \"Adding a new object\"-command actually wasn't correctly added.",
		        model.getObject(objectId));
		
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		
		assertNotNull(
		        "Object Snapshot Failure: The object we tried to create with an \"Adding a new object\"-command actually wasn't correctly added.",
		        object);
		assertEquals("Returned object snapshot did not have the correct XAddress.", objectAddress,
		        object.getAddress());
		assertEquals("Returned object did not have the correct revision number.", revNr,
		        object.getRevisionNumber());
		
		/*
		 * try to add the same object again (with an unforced command), should
		 * fail
		 */
		
		long failRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue(
		        "Trying to add an already existing object  with an unforced event succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
		
	}
	
	@Test
	public void testExecuteCommandModelCommandRemoveType() {
		/*
		 * ModelCommands of the remove type remove objects from models
		 */
		
		/*
		 * add a model with an object which we'll delete afterwards
		 */
		XID modelId = XX.createUniqueId();
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);
		
		GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
		
		assertNotNull("The model we tried to add doesn't exist, test cannot be executed.", model);
		assertEquals(
		        "The returned model has the wrong address, test cannot be executed, since it seems like adding models doesn't work correctly.",
		        modelAddress, model.getAddress());
		
		XID objectId = XX.createUniqueId();
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("Adding an object failed, test cannot be executed.", revNr >= 0);
		
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		
		assertNotNull("The object we tried to add doesn't exist, test cannot be executed.", object);
		assertEquals(
		        "The returned object has the wrong address, test cannot be executed, since it seems like adding object doesn't work correctly.",
		        objectAddress, object.getAddress());
		
		/*
		 * delete the object again
		 */
		XCommand deleteObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId,
		        objectId, object.getRevisionNumber(), false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteObjectCom);
		
		assertTrue(
		        "Executing \"Deleting an existing object\"-command failed (should succeed), revNr was "
		                + revNr, revNr >= 0);
		// check that the object was actually removed
		model = this.persistence.getModelSnapshot(modelAdrRequest);
		assertNull(
		        "The object we tried to remove with an \"Removing an existing object\"-command actually wasn't correctly removed and still exists in the stored model.",
		        model.getObject(objectId));
		
		object = this.persistence.getObjectSnapshot(objectAdrRequest);
		assertNull(
		        "The object we tried to remove with an \"Removing an existing object \"-command actually wasn't correctly removed.",
		        object);
		
		/*
		 * try to remove a not existing object
		 * 
		 * TODO check how forced & unforced commands behave in this case
		 */
		
		objectId = XX.createUniqueId();
		XCommand deleteNotExistingObjectCom = this.comFactory.createRemoveObjectCommand(
		        this.repoId, modelId, objectId, 0, false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingObjectCom);
		
		assertTrue(
		        "Removing a not existing object with an unforced command succeeded (should fail), revNr was "
		                + revNr, revNr == XCommand.FAILED);
		
		/*
		 * TODO check that removing an object also removes all fields
		 */
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
		
		// TODO check what happens when the wrong types of XAddresses are given
		// as parameters
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
