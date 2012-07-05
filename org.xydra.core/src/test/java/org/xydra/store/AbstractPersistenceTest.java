package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * the following variables need to be instantiated in the @Before method by
 * implementations of this test
 * 
 * - persistence needs to be an empty XydraPersistence with this.repoId as its
 * repository id.
 * 
 * - comFactory needs to be an implementation of XCommandFactory which creates
 * commands that can be executed by persistence.
 */
public abstract class AbstractPersistenceTest {
	
	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
	}
	
	private static final Logger log = LoggerFactory.getLogger(AbstractPersistenceTest.class);
	
	public XydraPersistence persistence;
	
	public XCommandFactory comFactory;
	
	public XID repoId = X.getIDProvider().fromString("testRepo");
	public XAddress repoAddress = XX.resolveRepository(this.repoId);
	public XID actorId = X.getIDProvider().fromString("testActor");
	
	/**
	 * most tests that deal with transactions built transactions pseudorandomly,
	 * so it is recommended to execute them multiple times. This parameter
	 * determines how many times these tests will be executed.
	 */
	public int nrOfIterationsForTxnTests = 2;
	
	@Test
	public void testExecuteCommandRepositorySafeCommandAddType() {
		testExecuteCommandRepositoryCommandAddType(false);
	}
	
	@Test
	public void testExecuteCommandRepositoryForcedCommandAddType() {
		testExecuteCommandRepositoryCommandAddType(true);
		
	}
	
	public void testExecuteCommandRepositoryCommandAddType(boolean forcedCommands) {
		/*
		 * RepositoryCommands of add type add new models to the repository.
		 */
		
		/*
		 * add a new model, should succeed
		 */
		XID modelId = XX.toId("testExecuteCommandRepositoryCommandAddType");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		
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
		 * try to add the same model again.
		 */
		
		revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		if(forcedCommands) {
			
			assertEquals(
			        "Executing \"Adding an existing model\"-command did not return \"No Change\", although the command was forced and the model we tried to add already exists.",
			        XCommand.NOCHANGE, revNr);
		} else {
			
			assertEquals(
			        "Trying to add an already existing model with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
	}
	
	@Test
	public void testExecuteCommandRepositorySafeCommandRemoveType() {
		testExecuteCommandRepositoryCommandRemoveType(false);
	}
	
	@Test
	public void testExecuteCommandRepositoryForcedCommandRemoveType() {
		testExecuteCommandRepositoryCommandRemoveType(true);
	}
	
	public void testExecuteCommandRepositoryCommandRemoveType(boolean forcedCommands) {
		/*
		 * RepositoryCommands of the remove type remove models from the
		 * repository
		 */
		
		/*
		 * add a model, we'll delete afterwards
		 */
		XID modelId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeModel1");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
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
		XCommand deleteModelCom =
		        this.comFactory.createRemoveModelCommand(this.repoId, modelId,
		                model.getRevisionNumber(), forcedCommands);
		
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
		 * try to delete the model again
		 */
		deleteModelCom =
		        this.comFactory.createRemoveModelCommand(this.repoId, modelId, revNr,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteModelCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to remove and already removed model with a forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Trying to remove and already removed model with a safe command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
		// check that the model was actually removed
		model = this.persistence.getModelSnapshot(addressRequest);
		assertNull(
		        "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
		        model);
		
		/*
		 * try to remove a not existing model
		 */
		
		modelId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeModel2");
		XCommand deleteNotExistingModelCom =
		        this.comFactory.createRemoveModelCommand(this.repoId, modelId, 0, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingModelCom);
		
		if(forcedCommands) {
			/*
			 * Should suceed/return "No Change" since the command was forced.
			 */
			
			assertEquals(
			        "Trying to remove a not existing model should return \"No change\" when the command is forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertTrue(
			        "Removing a not existing model with an unforced command succeeded (should fail), revNr was "
			                + revNr, revNr == XCommand.FAILED);
		}
		
		/*
		 * test if removing a model also removes the object (snapshots) and
		 * their fields
		 */
		
		modelId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeModel3");
		modelAddress = XX.resolveModel(this.repoId, modelId);
		addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);
		
		XID objectId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeObject1");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		long objectRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("Adding an object failed, test cannot be executed.", objectRevNr >= 0);
		
		// remove model and check if object is also removed
		addressRequest = new GetWithAddressRequest(modelAddress);
		model = this.persistence.getModelSnapshot(addressRequest);
		deleteModelCom =
		        this.comFactory.createRemoveModelCommand(this.repoId, modelId,
		                model.getRevisionNumber(), forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteModelCom);
		
		assertTrue(
		        "Executing \"Deleting an existing model\"-command failed (should succeed), revNr was "
		                + revNr, revNr >= 0);
		// check that the model was actually removed
		model = this.persistence.getModelSnapshot(addressRequest);
		assertNull(
		        "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
		        model);
		
		GetWithAddressRequest objectAddressRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAddressRequest);
		
		assertNull("The model was removed but its objects weren't removed.", object);
	}
	
	@Test
	public void testExecuteCommandModelSafeCommandAddType() {
		testExecuteCommandModelCommandAddType(false);
	}
	
	@Test
	public void testExecuteCommandModelForcedCommandAddType() {
		testExecuteCommandModelCommandAddType(true);
	}
	
	public void testExecuteCommandModelCommandAddType(boolean forcedCommands) {
		/*
		 * ModelCommands of add type add new objects to models.
		 */
		
		// add a model on which the commands can be executed first
		
		XID modelId = XX.toId("testExecuteCommandModelCommandAddTypeModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object, should succeed
		 */
		
		XID objectId = XX.toId("testExecuteCommandModelCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("Executing \"Adding a new object\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the object actually exists
		GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
		XWritableObject object = model.getObject(objectId);
		
		assertNotNull(
		        "Model Snapshot Failure: The object we tried to create with an \"Adding a new object\"-command actually wasn't correctly added.",
		        object);
		
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);
		
		assertNotNull(
		        "Object Snapshot Failure: The object we tried to create with an \"Adding a new object\"-command actually wasn't correctly added.",
		        objectSnapshot);
		assertEquals("Returned object snapshot did not have the correct XAddress.", objectAddress,
		        objectSnapshot.getAddress());
		assertEquals("Returned object snapshot did not have the correct revision number.", revNr,
		        objectSnapshot.getRevisionNumber());
		
		/*
		 * TODO implement equals() methods and add this to all add-tests
		 * 
		 * This fails because the equals() method is not
		 * overwritten/implemented.
		 * 
		 * assertEquals(
		 * "Returned object snapshot is not equal to the object stored in the model."
		 * , object, objectSnapshot);
		 */
		
		/*
		 * try to add the same object again
		 */
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		if(forcedCommands) {
			assertEquals(
			        "Executing \"Adding an existing object\"-command did not return \"No Change\", although the command was forced and the object we tried to add already exists.",
			        XCommand.NOCHANGE, revNr);
			
		} else {
			assertEquals(
			        "Trying to add an already existing object  with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
			
		}
		
	}
	
	@Test
	public void testExecuteCommandModelSafeCommandRemoveType() {
		testExecuteCommandModelCommandRemoveType(false);
	}
	
	@Test
	public void testExecuteCommandModelForcedCommandRemoveType() {
		testExecuteCommandModelCommandRemoveType(true);
	}
	
	public void testExecuteCommandModelCommandRemoveType(boolean forcedCommands) {
		/*
		 * ModelCommands of the remove type remove objects from models
		 */
		
		/*
		 * add a model with an object which we'll delete afterwards
		 */
		XID modelId = XX.toId("testExecuteCommandModelCommandRemoveTypeModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);
		
		GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
		
		assertNotNull("The model we tried to add doesn't exist, test cannot be executed.", model);
		assertEquals(
		        "The returned model has the wrong address, test cannot be executed, since it seems like adding models doesn't work correctly.",
		        modelAddress, model.getAddress());
		
		XID objectId = XX.toId("testExecuteCommandModelCommandRemoveTypeObject1");
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
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
		XCommand deleteObjectCom =
		        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId,
		                object.getRevisionNumber(), forcedCommands);
		
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
		 * try to delete the same object again
		 */
		deleteObjectCom =
		        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId, revNr,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteObjectCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to remove and already removed object with a forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Trying to remove and already removed object with a safe command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
		/*
		 * try to remove a not existing object
		 */
		
		objectId = XX.toId("testExecuteCommandModelCommandRemoveTypeObject2");
		XCommand deleteNotExistingObjectCom =
		        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId, 0,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingObjectCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to remove a not existing object should return \"No change\" when the command is forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Removing a not existing object with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
	}
	
	@Test
	public void testExecuteCommandObjectSafeCommandAddType() {
		testExecuteCommandObjectCommandAddType(false);
	}
	
	@Test
	public void testExecuteCommandObjectForcedCommandAddType() {
		testExecuteCommandObjectCommandAddType(true);
	}
	
	public void testExecuteCommandObjectCommandAddType(boolean forcedCommands) {
		/*
		 * ObjectCommands of add type add new fields to objects.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandObjectCommandAddTypeModel");
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandObjectCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandObjectCommandAddTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the field actually exists
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		XWritableField field = object.getField(fieldId);
		
		assertNotNull(
		        "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
		        field);
		assertEquals("Returned field did not have the correct XAddress.", fieldAddress,
		        field.getAddress());
		assertEquals("Returned field did not have the correct revision number.", revNr,
		        field.getRevisionNumber());
		
		/*
		 * try to add the same field again (with an unforced command), should
		 * fail
		 */
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		if(forcedCommands) {
			assertEquals(
			        "Trying to add an already existing field with a forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Trying to add an already existing field with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
	}
	
	@Test
	public void testExecuteCommandObjectSafeCommandRemoveType() {
		testExecuteCommandObjectCommandRemoveType(false);
	}
	
	@Test
	public void testExecuteCommandObjectForcedCommandRemoveType() {
		testExecuteCommandObjectCommandRemoveType(true);
	}
	
	public void testExecuteCommandObjectCommandRemoveType(boolean forcedCommands) {
		/*
		 * FieldCommands of remove type remove fields from objects.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandObjectCommandRemoveTypeModel");
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandObjectCommandRemoveTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandObjectCommandRemoveTypeField");
		
		XCommand addFieldCom =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * remove field, should succeed
		 */
		
		XCommand removeFieldCom =
		        this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, removeFieldCom);
		
		assertTrue("Executing \"Removing a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the field was actually removed
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		XWritableField field = object.getField(fieldId);
		
		assertNull(
		        "The field we tried to create with an \"Remove an existing field\"-command actually wasn't correctly removed.",
		        field);
		
		/*
		 * try to remove the same field again
		 */
		
		removeFieldCom =
		        this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, removeFieldCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to remove a not existing field with an forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Trying to remove a not existing field with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
		/*
		 * try to remove a not existing field
		 */
		
		fieldId = XX.toId("testExecuteCommandObjectCommandRemoveTypeNotExistingField");
		XCommand deleteNotExistingFieldCom =
		        this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId, fieldId,
		                0, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingFieldCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to remove a not existing field should return \"No change\" when the command is forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Removing a not existing field with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
	}
	
	@Test
	public void testExecuteCommandFieldSafeCommandAddType() {
		testExecuteCommandFieldCommandAddType(false);
	}
	
	@Test
	public void testExecuteCommandFieldForcedCommandAddType() {
		testExecuteCommandFieldCommandAddType(true);
	}
	
	public void testExecuteCommandFieldCommandAddType(boolean forcedCommands) {
		/*
		 * FieldCommands of add type add new value to fields.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandFieldCommandAddTypeModel");
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandAddTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * add a new value to the field, should succeed
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCom =
		        this.comFactory.createAddValueCommand(fieldAddress, revNr, value, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		assertTrue("Executing \"Adding a new value\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the value actually exists
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		XWritableField field = object.getField(fieldId);
		
		assertNotNull(
		        "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
		        field);
		
		XValue storedValue = field.getValue();
		
		assertNotNull(
		        "The value we tried to add with an \"Add a new value to a field without a value\"-command actually wasn't correctly added.",
		        storedValue);
		assertEquals("The stored value is not equal to the value we wanted to add.", value,
		        storedValue);
		
		/*
		 * try to add a value to the same field again (with an unforced
		 * command), should fail, since the value is now set and adding new
		 * values no longer works
		 */
		
		long revNr2 = this.persistence.executeCommand(this.actorId, addValueCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to add the same value to a field which value is already set with a forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr2);
		} else {
			assertEquals(
			        "Trying to add the same value to a field which value is already set with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr2);
		}
		
		// try to add a different value
		XValue value2 = X.getValueFactory().createIntegerValue(42);
		XCommand addValueCom2 =
		        this.comFactory.createAddValueCommand(fieldAddress, revNr, value2, forcedCommands);
		
		revNr2 = this.persistence.executeCommand(this.actorId, addValueCom2);
		
		if(forcedCommands) {
			assertTrue(
			        "A forced add-command should succeed, even though the value is already set.",
			        revNr2 >= 0);
			
			object = this.persistence.getObjectSnapshot(objectAdrRequest);
			field = object.getField(fieldId);
			
			assertEquals(
			        "Value wasn't changed, although the command was forced and its execution reported \"success\".",
			        value2, field.getValue());
		} else {
			assertEquals(
			        "Trying to add a new value to a field which value is already set succeeded (should fail).",
			        XCommand.FAILED, revNr2);
			
			// check that the value wasn't changed
			object = this.persistence.getObjectSnapshot(objectAdrRequest);
			field = object.getField(fieldId);
			
			storedValue = field.getValue();
			
			assertEquals("The stored value was changed, although the add-command failed.", value,
			        storedValue);
		}
		
	}
	
	@Test
	public void testExecuteCommandFieldSafeCommandRemoveType() {
		testExecuteCommandFieldCommandRemoveType(false);
	}
	
	@Test
	public void testExecuteCommandFieldForcedCommandRemoveType() {
		testExecuteCommandFieldCommandRemoveType(true);
	}
	
	public void testExecuteCommandFieldCommandRemoveType(boolean forcedCommands) {
		/*
		 * FieldCommands of remove type remove values from fields.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandFieldCommandRemoveTypeModel");
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandRemoveTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandRemoveTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * add a new value to the field, should succeed
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCom =
		        this.comFactory.createAddValueCommand(fieldAddress, revNr, value, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * remove the value again, should succeed
		 */
		
		XCommand removeValueCom =
		        this.comFactory.createRemoveValueCommand(fieldAddress, revNr, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, removeValueCom);
		
		assertTrue("Executing \"Remove a value \"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the value actually was removed
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		XWritableField field = object.getField(fieldId);
		
		assertNotNull(
		        "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
		        field);
		
		XValue storedValue = field.getValue();
		
		assertNull(
		        "The value we tried to remove with an \"Remove a value from a field with a value\"-command actually wasn't correctly removed.",
		        storedValue);
		
		/*
		 * try to remove a value from a field with no value (should fail)
		 */
		revNr = this.persistence.executeCommand(this.actorId, removeValueCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to remove a value from a field which has no value with a forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals(
			        "Removing a value from a field with no value with an unforced command succeeded (should fail).",
			        XCommand.FAILED, revNr);
		}
		
	}
	
	@Test
	public void testExecuteCommandFieldSafeCommandChangeType() {
		testExecuteCommandFieldCommandChangeType(false);
	}
	
	@Test
	public void testExecuteCommandFieldForcedCommandChangeType() {
		testExecuteCommandFieldCommandChangeType(true);
	}
	
	public void testExecuteCommandFieldCommandChangeType(boolean forcedCommands) {
		/*
		 * FieldCommands of change type change values of fields with values.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandFieldCommandChangeTypeModel");
		XCommand addModelCom =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandChangeTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandChangeTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * try to change a value which does not exist, should fail
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand changeValueCom =
		        this.comFactory
		                .createChangeValueCommand(fieldAddress, revNr, value, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, changeValueCom);
		
		if(forcedCommands) {
			assertTrue(
			        "Executing a forced \"Change a value\"-command failed on a field with no value (should succeed).",
			        revNr >= 0);
		} else {
			assertEquals(
			        "Executing a safe \"Change a value\"-command succeeded on a field with no value (should fail).",
			        XCommand.FAILED, revNr);
		}
		
		/*
		 * add a value to the field, should succeed
		 * 
		 * Note: if forcedCommands is set to false, the value of the field will
		 * not be set. In contrast, if forcedCommands is set to true, the value
		 * of the field will already be set, but since all commands will be
		 * forced, the following add command also has to succeed and the test
		 * can continue without any problems.
		 */
		// get the correct revision number
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		XWritableField field = object.getField(fieldId);
		revNr = field.getRevisionNumber();
		
		XCommand addValueCom =
		        this.comFactory.createAddValueCommand(fieldAddress, revNr, value, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		if(forcedCommands) {
			assertEquals(
			        "Trying to add the same value again with a forced command should return \"No change\".",
			        XCommand.NOCHANGE, revNr);
			/*
			 * Although we need the revNr for the following events, it is not
			 * problematic that it is now set to XCommand.NOCHANGE, since the
			 * following events will also be forced events and will therefore
			 * not care whether the revision number is correct or not.
			 */
			
		} else {
			assertTrue(
			        "Executing \"Adding a new value\"-command failed (should succeed), revNr was "
			                + revNr, revNr >= 0);
		}
		
		/*
		 * change the value, should succeed
		 */
		XValue value2 = X.getValueFactory().createIntegerValue(42);
		changeValueCom =
		        this.comFactory.createChangeValueCommand(fieldAddress, revNr, value2,
		                forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, changeValueCom);
		
		assertTrue("Executing \"Change a value\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		// check that the value actually was changed
		objectAdrRequest = new GetWithAddressRequest(objectAddress);
		object = this.persistence.getObjectSnapshot(objectAdrRequest);
		field = object.getField(fieldId);
		
		assertNotNull(
		        "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
		        field);
		
		XValue storedValue = field.getValue();
		
		assertNotNull(
		        "The value we tried to add with an \"Add a new value to a field without a value\"-command actually wasn't correctly added.",
		        storedValue);
		assertEquals("The stored value is not equal to the value that we tried to change it to.",
		        value2, storedValue);
		
		/*
		 * try to change the value to itself, should return
		 * "nothing was changed".
		 */
		
		/*
		 * TODO Unsure if this should really return XCommand.NOCHANGE
		 * 
		 * long failRevNr = this.persistence.executeCommand(this.actorId,
		 * changeValueCom); assertTrue(
		 * "Trying to change the value to itself with an unforced command succeeded or failed (should return \"nothing was changed\"), revNr was "
		 * + failRevNr, failRevNr == XCommand.NOCHANGE);
		 */
		
		/*
		 * trying to change the value to "null" doesn't need to be tested, since
		 * we cannot create CHANGE-commands which change a value to "null"
		 * (changing an existing value to "null" is done by a REMOVE-command)
		 */
		
	}
	
	/*
	 * TODO check if all types of forced commands work correctly with arbitrary
	 * revision numbers (as they should)
	 */
	
	@Test
	public void testExecuteTransactionAddObjectWithForcedCmd() {
		testExecuteTransactionAddObject(true);
	}
	
	@Test
	public void testExecuteTransactionAddObjectWithSafeCmd() {
		testExecuteTransactionAddObject(false);
	}
	
	private void testExecuteTransactionAddObject(boolean forced) {
		XID modelId = X.getIDProvider().fromString("executeTransactionAddObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId = X.getIDProvider().fromString("executeTransactionAddObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addObjectCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Object wasn't added correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
	}
	
	@Test
	public void testExecuteTransactionAddAlreadyExistingObjectWithForcedCmd() {
		testExecuteTransactionAddAlreadyExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionAddAlreadyExistingObjectWithSafeCmd() {
		testExecuteTransactionAddAlreadyExistingObject(false);
	}
	
	private void testExecuteTransactionAddAlreadyExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionAddAlreadyExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionAddAlreadyExistingObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		
		XCommand addObjectAgainCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		txnBuilder.addCommand(addObjectAgainCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertEquals("Execution should return \"No Change\", since the command was forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
			        XCommand.FAILED, revNr);
		}
	}
	
	@Test
	public void testExecuteTransactionRemoveExistingObjectWithForcedCmd() {
		testExecuteTransactionRemoveExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveExistingObjectWithSafeCmd() {
		testExecuteTransactionRemoveExistingObject(false);
	}
	
	private void testExecuteTransactionRemoveExistingObject(boolean forced) {
		XID modelId = X.getIDProvider().fromString("executeTransactionRemoveExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionRemoveNotExistingObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		
		XCommand removeObjectCommand =
		        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId, revNr,
		                forced);
		txnBuilder.addCommand(removeObjectCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Object wasn't correclty removed/Transaction failed.", revNr >= 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertEquals("The persistence should not contain the specified object at this point.",
		        null, object);
	}
	
	@Test
	public void testExecuteTransactionRemoveNotExistingObjectWithForcedCmd() {
		testExecuteTransactionRemoveNotExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveNotExistingObjectWithSafeCmd() {
		testExecuteTransactionRemoveNotExistingObject(false);
	}
	
	private void testExecuteTransactionRemoveNotExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionRemoveNotExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionRemoveNotExistingObject-Object");
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		
		XCommand removeObjectCommand =
		        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId, revNr,
		                forced);
		txnBuilder.addCommand(removeObjectCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertEquals("Execution should return \"No Change\", since the command was forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
			        XCommand.FAILED, revNr);
		}
	}
	
	@Test
	public void testExecuteTransactionAddObjectAndFieldWithForcedCmd() {
		testExecuteTransactionAddObjectAndField(true);
	}
	
	@Test
	public void testExecuteTransactionAddObjectAndFieldWithSafeCmd() {
		testExecuteTransactionAddObjectAndField(false);
	}
	
	private void testExecuteTransactionAddObjectAndField(boolean forced) {
		XID modelId = X.getIDProvider().fromString("executeTransactionAddObjectAndField-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId = X.getIDProvider().fromString("executeTransactionAddObjectAndField-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		XID fieldId = X.getIDProvider().fromString("executeTransactionAddObjectAndField-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addObjectCommand);
		txnBuilder.addCommand(addFieldCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		assertTrue("Object does not contain the field the transaction should've added.",
		        object.hasField(fieldId));
	}
	
	@Test
	public void testExecuteTransactionAddFieldToExistingObjectWithForcedCmd() {
		testExecuteTransactionAddFieldToExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionAddFieldToExistingObjectWithSafeCmd() {
		testExecuteTransactionAddFieldToExistingObject(false);
	}
	
	private void testExecuteTransactionAddFieldToExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionAddFieldToExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionAddFieldToExistingObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionAddAlreadyExistingFieldToExistingObject-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                false);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addFieldCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		assertTrue("Object does not contain the field the transaction should've added.",
		        object.hasField(fieldId));
	}
	
	@Test
	public void testExecuteTransactionAddAlreadyExistingFieldToExistingObjectWithForcedCmd() {
		testExecuteTransactionAddAlreadyExistingFieldToExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionAddAlreadyExistingFieldToExistingObjectWithSafeCmd() {
		testExecuteTransactionAddAlreadyExistingFieldToExistingObject(false);
	}
	
	private void testExecuteTransactionAddAlreadyExistingFieldToExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionAddAlreadyExistingFieldToExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionAddFieldToExistingObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString("executeTransactionAddFieldToExistingObject-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                false);
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XCommand addFieldAgainCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addFieldAgainCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertEquals("Execution should return \"No Change\", since the command was forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
			        XCommand.FAILED, revNr);
		}
	}
	
	@Test
	public void testExecuteTransactionTryToAddFieldToNotExistingObjectWithForcedCmd() {
		testExecuteTransactionTryToAddFieldToNotExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionTryToAddFieldToNotExistingObjectWithSafeCmd() {
		testExecuteTransactionTryToAddFieldToNotExistingObject(false);
	}
	
	private void testExecuteTransactionTryToAddFieldToNotExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionTryToRemoveFieldFromNotExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactioRemoveTryToRemoveFieldFromNotExistingObject-Object");
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionTryToRemoveFieldFromNotExistingObject-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addFieldCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertEquals("Execution should return \"Failed\".", XCommand.FAILED, revNr);
		
	}
	
	@Test
	public void testExecuteTransactionRemoveExistingFieldFromExistingObjectWithForcedCmd() {
		testExecuteTransactionRemoveExistingFieldFromExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveExistingFieldFromExistingObjectWithSafeCmd() {
		testExecuteTransactionRemoveExistingFieldFromExistingObject(false);
	}
	
	private void testExecuteTransactionRemoveExistingFieldFromExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveExistingFieldFromExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactioRemoveExistingFieldFromExistingObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveExistingFieldFromExistingObject-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addFieldCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		assertTrue("Object does not contain the field the transaction should've added.",
		        object.hasField(fieldId));
	}
	
	@Test
	public void testExecuteTransactionRemoveNotExistingFieldFromExistingObjectWithForcedCmd() {
		testExecuteTransactionRemoveNotExistingFieldFromExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveNotExistingFieldFromExistingObjectWithSafeCmd() {
		testExecuteTransactionRemoveNotExistingFieldFromExistingObject(false);
	}
	
	private void testExecuteTransactionRemoveNotExistingFieldFromExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveNotExistingFieldFromExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactioRemoveNotExistingFieldFromExistingObject-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveNotExistingFieldFromExistingObject-Field");
		XCommand removeFieldCommand =
		        this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(removeFieldCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertEquals("Execution should return \"No Change\", since the command was forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
			        XCommand.FAILED, revNr);
		}
	}
	
	@Test
	public void testExecuteTransactionTryToRemoveFieldFromNotExistingObjectWithForcedCmd() {
		testExecuteTransactionTryToRemoveFieldFromNotExistingObject(true);
	}
	
	@Test
	public void testExecuteTransactionTryToRemoveFieldFromNotExistingObjectWithSafeCmd() {
		testExecuteTransactionTryToRemoveFieldFromNotExistingObject(false);
	}
	
	private void testExecuteTransactionTryToRemoveFieldFromNotExistingObject(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionTryToRemoveFieldFromNotExistingObject-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactioRemoveTryToRemoveFieldFromNotExistingObject-Object");
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionTryToRemoveFieldFromNotExistingObject-Field");
		XCommand removeFieldCommand =
		        this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(removeFieldCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertEquals("Execution should return \"Failed\".", XCommand.FAILED, revNr);
		
	}
	
	@Test
	public void testExecuteTransactionAddObjectFieldAndValueWithForcedCmd() {
		testExecuteTransactionAddObjectFieldAndValue(true);
	}
	
	@Test
	public void testExecuteTransactionAddObjectFieldAndValueWithSafeCmd() {
		testExecuteTransactionAddObjectFieldAndValue(false);
	}
	
	private void testExecuteTransactionAddObjectFieldAndValue(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionAddObjectFieldAndValue-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionAddObjectFieldAndValue-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		XID fieldId =
		        X.getIDProvider().fromString("executeTransactionAddObjectFieldAndValue-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addObjectCommand);
		txnBuilder.addCommand(addFieldCommand);
		txnBuilder.addCommand(addValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		XReadableField field = object.getField(fieldId);
		assertNotNull("Object does not contain the field the transaction should've added.", field);
		
		assertEquals("Field doesn't have the right value.", value, field.getValue());
	}
	
	@Test
	public void testExecuteTransactionAddValueToExistingFieldWithForcedCmd() {
		testExecuteTransactionAddValueToExistingField(true);
	}
	
	@Test
	public void testExecuteTransactionAddValueToExistingFieldWithSafeCmd() {
		testExecuteTransactionAddValueToExistingField(false);
	}
	
	private void testExecuteTransactionAddValueToExistingField(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionAddValueToExistingField-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionAddValueToExistingField-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString("executeTransactionAddValueToExistingField-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		XReadableField field = object.getField(fieldId);
		assertNotNull("Object does not contain the field the transaction should've added.", field);
		
		assertEquals("Field doesn't have the right value.", value, field.getValue());
	}
	
	@Test
	public void testExecuteTransactionAddValueToNotExistingFieldWithForcedCmd() {
		testExecuteTransactionAddValueToNotExistingField(true);
	}
	
	@Test
	public void testExecuteTransactionAddValueToNotExistingFieldWithSafeCmd() {
		testExecuteTransactionAddValueToExistingField(false);
	}
	
	private void testExecuteTransactionAddValueToNotExistingField(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionAddValueToNotExistingField-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionAddValueToNotExistingField-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString("executeTransactionAddValueToNotExistingField-Field");
		
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertEquals("Transaction should've failed, since we never added the needed field.",
		        XCommand.FAILED, revNr);
	}
	
	@Test
	public void testExecuteTransactionAddValueToExistingFieldWithValueWithForcedCmd() {
		testExecuteTransactionAddValueToExistingFieldWithValue(true);
	}
	
	@Test
	public void testExecuteTransactionAddValueToExistingFieldWithValueWithSafeCmd() {
		testExecuteTransactionAddValueToExistingFieldWithValue(false);
	}
	
	private void testExecuteTransactionAddValueToExistingFieldWithValue(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionAddValueToExistingFieldWithValue-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactionAddValueToExistingFieldWithValue-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionAddValueToExistingFieldWithValue-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
		
		assertTrue("Value wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value2 = X.getValueFactory().createStringValue("test2");
		XCommand addValueCommand2 =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value2, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(addValueCommand2);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertTrue("Transaction wasn't executed correctly.", revNr > 0);
			
			GetWithAddressRequest addressRequest =
			        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
			XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
			
			assertNotNull(
			        "Object does not exist, but the transactions execution reported a success.",
			        object);
			
			XReadableField field = object.getField(fieldId);
			assertNotNull("Object does not contain the field the transaction should've added.",
			        field);
			
			assertEquals("Field doesn't have the right value.", value2, field.getValue());
		} else {
			assertEquals(
			        "Execution should return \"Failed\", since the command wasn't forced and the value was already set.",
			        XCommand.FAILED, revNr);
		}
	}
	
	@Test
	public void testExecuteTransactionRemoveValueFromExistingFieldWithForcedCmd() {
		testExecuteTransactionRemoveValueFromExistingField(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveValueFromExistingFieldWithSafeCmd() {
		testExecuteTransactionRemoveValueFromExistingField(false);
	}
	
	private void testExecuteTransactionRemoveValueFromExistingField(boolean forced) {
		XID modelId =
		        X.getIDProvider()
		                .fromString("executeTransactionRemoveValueFromExistingField-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromExistingField-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider()
		                .fromString("executeTransactionRemoveValueFromExistingField-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
		
		assertTrue("Value wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XCommand removeValueCommand =
		        this.comFactory.createRemoveValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(removeValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		XReadableField field = object.getField(fieldId);
		assertNotNull("Object does not contain the field the transaction should've added.", field);
		
		assertEquals("Field should have no value.", null, field.getValue());
	}
	
	@Test
	public void testExecuteTransactionRemoveValueFromExistingFieldWithoutValueForcedCmd() {
		testExecuteTransactionRemoveValueFromExistingFieldWithoutValue(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveValueFromExistingFieldWithoutValueSafeCmd() {
		testExecuteTransactionRemoveValueFromExistingFieldWithoutValue(false);
	}
	
	private void testExecuteTransactionRemoveValueFromExistingFieldWithoutValue(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromExistingFieldWithoutValue-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromExistingFieldWithoutValue-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromExistingFieldWithoutValue-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XCommand removeValueCommand =
		        this.comFactory.createRemoveValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(removeValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertEquals("Execution should return \"No Change\", since the command was forced.",
			        XCommand.NOCHANGE, revNr);
		} else {
			assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
			        XCommand.FAILED, revNr);
		}
	}
	
	@Test
	public void testExecuteTransactionRemoveValueFromNotExistingFieldWithForcedCmd() {
		testExecuteTransactionRemoveValueFromNotExistingField(true);
	}
	
	@Test
	public void testExecuteTransactionRemoveValueFromNotExistingFieldWithSafeCmd() {
		testExecuteTransactionRemoveValueFromNotExistingField(false);
	}
	
	private void testExecuteTransactionRemoveValueFromNotExistingField(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromNotExistingField-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromNotExistingField-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionRemoveValueFromNotExistingField-Field");
		
		XCommand removeValueCommand =
		        this.comFactory.createRemoveValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(removeValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertEquals("Execution should return \"Failed\".", XCommand.FAILED, revNr);
		
	}
	
	@Test
	public void testExecuteTransactionChangeValueOfExistingFieldWithForcedCmd() {
		testExecuteTransactionChangeValueOfExistingField(true);
	}
	
	@Test
	public void testExecuteTransactionChangeValueOfExistingFieldWithSafeCmd() {
		testExecuteTransactionChangeValueOfExistingField(false);
	}
	
	private void testExecuteTransactionChangeValueOfExistingField(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString("executeTransactionChangeValueOfExistingField-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString("executeTransactionChangeValueOfExistingField-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString("executeTransactionChangeValueOfExistingField-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value1 = X.getValueFactory().createStringValue("test1");
		
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value1, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
		
		assertTrue("Value wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value2 = X.getValueFactory().createStringValue("test2");
		
		XCommand changeValueCommand =
		        this.comFactory.createChangeValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value2, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(changeValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction wasn't executed correctly.", revNr > 0);
		
		GetWithAddressRequest addressRequest =
		        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
		XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
		
		assertNotNull("Object does not exist, but the transactions execution reported a success.",
		        object);
		
		XReadableField field = object.getField(fieldId);
		assertNotNull("Object does not contain the field the transaction should've added.", field);
		
		assertEquals("Field doesn't have the right value.", value2, field.getValue());
		
	}
	
	@Test
	public void testExecuteTransactionChangeValueOfExistingFieldWithoutValueForcedCmd() {
		testExecuteTransactionChangeValueOfExistingFieldWithoutValue(true);
	}
	
	@Test
	public void testExecuteTransactionChangeValueOfExistingFieldWithoutValueSafeCmd() {
		testExecuteTransactionChangeValueOfExistingFieldWithoutValue(false);
	}
	
	private void testExecuteTransactionChangeValueOfExistingFieldWithoutValue(boolean forced) {
		XID modelId =
		        X.getIDProvider().fromString(
		                "executeTransactionChangeValueOfExistingFieldWithoutValue-Model");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID objectId =
		        X.getIDProvider().fromString(
		                "executeTransactionChangeValueOfExistingFieldWithoutValue-Object");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		
		assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XID fieldId =
		        X.getIDProvider().fromString(
		                "executeTransactionChangeValueOfExistingFieldWithoutValue-Field");
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                forced);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
		
		assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		XCommand changeValueCommand =
		        this.comFactory.createChangeValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, forced);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		txnBuilder.addCommand(changeValueCommand);
		
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		if(forced) {
			assertTrue("Transaction wasn't executed correctly.", revNr > 0);
			
			GetWithAddressRequest addressRequest =
			        new GetWithAddressRequest(XX.resolveObject(this.repoId, modelId, objectId));
			XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
			
			assertNotNull(
			        "Object does not exist, but the transactions execution reported a success.",
			        object);
			
			XReadableField field = object.getField(fieldId);
			assertNotNull("Object does not contain the field the transaction should've added.",
			        field);
			
			assertEquals("Field doesn't have the right value.", value, field.getValue());
		} else {
			assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
			        XCommand.FAILED, revNr);
		}
	}
	
	/**
	 * This test pseudorandomly creates model transactions which execution is
	 * supposed to succeed. It uses {@link java.util.Random} to create random
	 * transactions. We set the seed manually and always print it on the screen.
	 * 
	 * If the test fails, simply copy the seed which created the transaction
	 * which was the cause of the failure and set the seed to this value
	 * (instead of using random seeds, as the test normally does). This makes
	 * the test deterministic and enables debugging.
	 */
	
	@Test
	public void testExecuteCommandSucceedingModelTransaction() {
		SecureRandom seedGen = new SecureRandom();
		
		for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
			
			XID modelId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandSucceedingModelTransactionModel" + i);
			XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
			
			GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
			XCommand addModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
			// add a model on which an object can be created first
			long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			
			assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
			
			XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
			
			/*
			 * Info: if the test fails, do the following to enable deterministic
			 * debugging: Set the seed to the value which caused the test to
			 * fail. This makes the test deterministic .
			 */
			long seed = seedGen.nextLong();
			log.info("Creating transaction " + i + " with seed " + seed + ".");
			Pair<ChangedModel,XTransaction> pair =
			        createRandomSucceedingModelTransaction(modelSnapshot, seed);
			ChangedModel changedModel = pair.getFirst();
			XTransaction txn = pair.getSecond();
			
			revNr = this.persistence.executeCommand(this.actorId, txn);
			assertTrue(
			        "Transaction failed, should succeed, seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        revNr >= 0);
			
			modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
			
			int nrOfObjectsInModelSnapshot = 0;
			int nrOfObjectsInChangedModel = 0;
			for(@SuppressWarnings("unused")
			XID objectId : changedModel) {
				nrOfObjectsInChangedModel++;
			}
			
			for(@SuppressWarnings("unused")
			XID objectId : modelSnapshot) {
				nrOfObjectsInModelSnapshot++;
			}
			
			assertEquals(
			        "The transaction wasn't correctly executed, the stored model does not store the correct amount of objects it should be storing after execution of the transaction, seed was "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        nrOfObjectsInChangedModel, nrOfObjectsInModelSnapshot);
			
			for(XID objectId : changedModel) {
				assertTrue(
				        "The stored model does not contain an object it should contain after the transaction was executed, seed was "
				                + seed
				                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
				        modelSnapshot.hasObject(objectId));
				
				XReadableObject changedObject = changedModel.getObject(objectId);
				XReadableObject objectSnapshot = modelSnapshot.getObject(objectId);
				
				int nrOfFieldsInObjectSnapshot = 0;
				int nrOfFieldsInChangedObject = 0;
				for(@SuppressWarnings("unused")
				XID id : changedObject) {
					nrOfFieldsInChangedObject++;
				}
				
				for(@SuppressWarnings("unused")
				XID id : objectSnapshot) {
					nrOfFieldsInObjectSnapshot++;
				}
				
				assertEquals(
				        "The transaction wasn't correctly executed, one of the stored objects does not store the correct amount of fields it should be storing after execution of the transaction, seed was "
				                + seed
				                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
				        nrOfFieldsInChangedObject, nrOfFieldsInObjectSnapshot);
				
				for(XID fieldId : changedObject) {
					assertTrue(
					        "One of the stored objects does not contain a field it should contain after the transaction was executed, seed was "
					                + seed
					                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
					        objectSnapshot.hasField(fieldId));
					
					XReadableField changedField = changedObject.getField(fieldId);
					XReadableField fieldSnapshot = objectSnapshot.getField(fieldId);
					
					assertEquals(
					        "One of the stored fields does not contain the value it should contain after the transaction was executed, seed was "
					                + seed
					                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
					        changedField.getValue(), fieldSnapshot.getValue());
				}
			}
		}
	}
	
	/**
	 * This test pseudorandomly creates object transactions which execution is
	 * supposed to succeed. It uses {@link java.util.Random} to create random
	 * transactions. We set the seed manually and always print it on the screen.
	 * 
	 * If the test fails, simply copy the seed which created the transaction
	 * which was the cause of the failure and set the seed to this value
	 * (instead of using random seeds, as the test normally does). This makes
	 * the test deterministic and enables debugging.
	 */
	@Test
	public void testExecuteCommandSucceedingObjectTransaction() {
		SecureRandom seedGen = new SecureRandom();
		
		for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
			
			XID modelId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandSucceedingObjectTransactionModel" + i);
			
			XCommand addModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
			// add a model on which an object can be created first
			long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			
			assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
			
			XID objectId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandSucceedingObjectTransactionObject" + i);
			XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
			
			GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
			XCommand addObjectCom =
			        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
			// add a model on which an object can be created first
			revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
			
			assertTrue("Object could not be added, test cannot be executed.", revNr >= 0);
			
			XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);
			
			/*
			 * Info: if the test fails, do the following to enable deterministic
			 * debugging: Set the seed to the value which caused the test to
			 * fail. This makes the test deterministic .
			 */
			long seed = seedGen.nextLong();
			log.info("Creating transaction " + i + " with seed " + seed + ".");
			Pair<ChangedObject,XTransaction> pair =
			        createRandomSucceedingObjectTransaction(objectSnapshot, seed);
			ChangedObject changedObject = pair.getFirst();
			XTransaction txn = pair.getSecond();
			
			revNr = this.persistence.executeCommand(this.actorId, txn);
			assertTrue(
			        "Transaction failed, should succeed, seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        revNr >= 0);
			
			objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);
			
			int nrOfFieldsInObjectSnapshot = 0;
			int nrOfFieldsInChangedObject = 0;
			for(@SuppressWarnings("unused")
			XID id : changedObject) {
				nrOfFieldsInChangedObject++;
			}
			
			for(@SuppressWarnings("unused")
			XID id : objectSnapshot) {
				nrOfFieldsInObjectSnapshot++;
			}
			
			assertEquals(
			        "The transaction wasn't correctly executed, the stored objects does not store the correct amount of fields it should be storing after execution of the transaction, seed was "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        nrOfFieldsInChangedObject, nrOfFieldsInObjectSnapshot);
			
			for(XID fieldId : changedObject) {
				assertTrue(
				        "The stored object does not contain a field it should contain after the transaction was executed, seed was "
				                + seed
				                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
				        objectSnapshot.hasField(fieldId));
				
				XReadableField changedField = changedObject.getField(fieldId);
				XReadableField fieldSnapshot = objectSnapshot.getField(fieldId);
				
				assertEquals(
				        "One of the stored fields does not contain the value it should contain after the transaction was executed, seed was "
				                + seed
				                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
				        changedField.getValue(), fieldSnapshot.getValue());
			}
		}
		
	}
	
	/**
	 * Pseudorandomly generates a transaction which should succeed on the given
	 * empty model. The seed determines how the random number generator
	 * generates its pseudorandom output.
	 * 
	 * Using the same seed twice will result in deterministically generating the
	 * same transaction again, which can be useful when executing a transaction
	 * in a test fails and the failed transaction needs to be reconstructed.
	 */
	private Pair<ChangedModel,XTransaction> createRandomSucceedingModelTransaction(
	        XWritableModel model, long seed) {
		assertTrue("This method only works with empty models.", model.isEmpty());
		
		Random rand = new Random(seed);
		XAddress modelAddress = model.getAddress();
		
		XTransactionBuilder txBuilder = new XTransactionBuilder(modelAddress);
		ChangedModel changedModel = new ChangedModel(model);
		
		// create random amount of objects
		int nrOfObjects = 0;
		
		nrOfObjects = 1 + rand.nextInt(10);
		// add at least one object
		
		for(int i = 0; i < nrOfObjects; i++) {
			XID objectId = X.getIDProvider().fromString("randomObject" + i);
			
			changedModel.createObject(objectId);
			
			XCommand addObjectCommand =
			        this.comFactory.createAddObjectCommand(modelAddress, objectId, false);
			
			txBuilder.addCommand(addObjectCommand);
		}
		
		List<XID> toBeRemovedObjects = new LinkedList<XID>();
		
		// add fields and values to the object
		for(XID objectId : changedModel) {
			XWritableObject changedObject = changedModel.getObject(objectId);
			XAddress objectAddress = XX.resolveObject(modelAddress, objectId);
			
			int nrOfFields = rand.nextInt(10);
			for(int i = 0; i < nrOfFields; i++) {
				XID fieldId = X.getIDProvider().fromString(objectId + "randomField" + i);
				XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
				
				XWritableField field = changedObject.createField(fieldId);
				long fieldRevNr = field.getRevisionNumber();
				
				XCommand addFieldCommand =
				        this.comFactory.createAddFieldCommand(objectAddress, fieldId, false);
				
				txBuilder.addCommand(addFieldCommand);
				
				boolean hasValue = rand.nextBoolean();
				
				if(hasValue) {
					
					XValue value = createRandomValue(rand);
					
					field.setValue(value);
					
					XCommand addValueCommand =
					        this.comFactory.createAddValueCommand(fieldAddress, fieldRevNr, value,
					                false);
					
					txBuilder.addCommand(addValueCommand);
				}
			}
			
			/*
			 * randomly change fields, i.e. randomly remove or change some
			 * values and randomly remove some fields
			 */
			randomlyChangeFields(rand, changedObject, txBuilder);
			
			XID firstObjectId = X.getIDProvider().fromString("randomObject" + 0);
			XID secondObjectId = X.getIDProvider().fromString("randomObject" + 1);
			if(!objectId.equals(firstObjectId) && !objectId.equals(secondObjectId)) {
				
				/*
				 * randomly determine if the object should be removed in the
				 * transaction.
				 * 
				 * Never remove the first and second object that was added so
				 * that at least two objects will be added and we're actually
				 * building a transaction (and not just a single command or a
				 * transaction which execution would result in NOCHANGE)
				 */
				boolean removeObject = rand.nextBoolean();
				if(removeObject) {
					
					toBeRemovedObjects.add(objectId);
					
					/*
					 * object was added in the transaction, so its revision
					 * number should be 0.
					 */
					XCommand removeObjectCommand =
					        this.comFactory.createRemoveObjectCommand(objectAddress, 0, false);
					
					txBuilder.addCommand(removeObjectCommand);
				}
			}
			
		}
		
		/*
		 * we need to remove the objects in a separate loop because modifying
		 * the set of objects of the changedModel while we iterate over it
		 * results in ConcurrentModificationExceptions
		 */
		for(XID objectId : toBeRemovedObjects) {
			changedModel.removeObject(objectId);
		}
		
		XTransaction txn = txBuilder.build();
		
		return new Pair<ChangedModel,XTransaction>(changedModel, txn);
	}
	
	/**
	 * Pseudorandomly generates a transaction which should succeed on the given
	 * empty object. The seed determines how the random number generator
	 * generates its pseudorandom output.
	 * 
	 * Using the same seed twice will result in deterministically generating the
	 * same transaction again, which can be useful when executing a transaction
	 * in a test fails and the failed transaction needs to be reconstructed.
	 */
	private Pair<ChangedObject,XTransaction> createRandomSucceedingObjectTransaction(
	        XWritableObject object, long seed) {
		assertTrue("This method only works with empty objects.", object.isEmpty());
		
		Random rand = new Random(seed);
		XID objectId = object.getId();
		XAddress objectAddress = object.getAddress();
		
		XTransactionBuilder txBuilder = new XTransactionBuilder(object.getAddress());
		ChangedObject changedObject = new ChangedObject(object);
		
		int nrOfFields = 1 + rand.nextInt(10); // add at least one field.
		for(int i = 0; i < nrOfFields; i++) {
			XID fieldId = X.getIDProvider().fromString(objectId + "randomField" + i);
			XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
			
			XWritableField field = changedObject.createField(fieldId);
			long fieldRevNr = field.getRevisionNumber();
			XCommand addFieldCommand =
			        this.comFactory.createAddFieldCommand(objectAddress, fieldId, false);
			txBuilder.addCommand(addFieldCommand);
			
			boolean hasValue = rand.nextBoolean();
			
			if(hasValue) {
				
				XValue value = createRandomValue(rand);
				
				field.setValue(value);
				
				XCommand addValueCommand =
				        this.comFactory.createAddValueCommand(fieldAddress, fieldRevNr, value,
				                false);
				txBuilder.addCommand(addValueCommand);
			}
		}
		
		/*
		 * randomly change fields, i.e. randomly remove or change some values
		 * and randomly remove some fields
		 */
		randomlyChangeFields(rand, changedObject, txBuilder);
		
		/*
		 * The code above might construct transactions which add some fields,
		 * but immediately removes these fields again, so the execution of this
		 * transaction would return "No change" or it might create a transaction
		 * which only contains one command which actually changes one thing (for
		 * example when nrOfFields equals 1, but we don't add a value to the
		 * field or we add a value, but remove it again). But we want to create
		 * a succeeding transaction, which actually changes something, which is
		 * the reason why another field is added here to be on the safe side. We
		 * also need to add a value, since a transaction should be made up of at
		 * least 2 commands which actually change something.
		 */
		
		XID fieldId = X.getIDProvider().fromString(objectId + "randomField" + nrOfFields + 1);
		XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
		
		XWritableField field = changedObject.createField(fieldId);
		long fieldRevNr = field.getRevisionNumber();
		XCommand addFieldCommand =
		        this.comFactory.createAddFieldCommand(objectAddress, fieldId, false);
		txBuilder.addCommand(addFieldCommand);
		
		XValue value = createRandomValue(rand);
		
		field.setValue(value);
		
		XCommand addValueCommand =
		        this.comFactory.createAddValueCommand(fieldAddress, fieldRevNr, value, false);
		txBuilder.addCommand(addValueCommand);
		
		XTransaction txn = txBuilder.build();
		
		return new Pair<ChangedObject,XTransaction>(changedObject, txn);
	}
	
	/**
	 * Pseudorandomly manipulates the fields of the given object (removes and
	 * changes values of fields which already have a value or remove fields)
	 * using the given pseudorandom generator and adds the appropriate commands
	 * to the given transaction builder.
	 */
	private void randomlyChangeFields(Random rand, XWritableObject changedObject,
	        XTransactionBuilder txnBuilder) {
		XAddress objectAddress = changedObject.getAddress();
		
		// randomly determine if some of the fields should be removed
		List<XID> toBeRemovedFields = new LinkedList<XID>();
		for(XID fieldId : changedObject) {
			boolean removeField = rand.nextBoolean();
			XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
			XWritableField field = changedObject.getField(fieldId);
			long fieldRevNr = field.getRevisionNumber();
			
			if(removeField) {
				toBeRemovedFields.add(fieldId);
				XCommand removeFieldCommand =
				        this.comFactory.createRemoveFieldCommand(fieldAddress, fieldRevNr, false);
				
				txnBuilder.addCommand(removeFieldCommand);
				
			} else {
				// randomly determine if its value should be changed/removed
				XValue currentValue = field.getValue();
				
				if(currentValue != null) {
					boolean removeValue = rand.nextBoolean();
					if(removeValue) {
						field.setValue(null);
						
						XCommand removeValueCommand =
						        this.comFactory.createRemoveValueCommand(fieldAddress, fieldRevNr,
						                false);
						
						txnBuilder.addCommand(removeValueCommand);
						
					} else {
						boolean changeValue = rand.nextBoolean();
						
						if(changeValue) {
							XValue newValue = null;
							do {
								newValue = createRandomValue(rand);
							} while(newValue.equals(currentValue));
							
							field.setValue(newValue);
							
							XCommand changeValueCommand =
							        this.comFactory.createChangeValueCommand(fieldAddress,
							                fieldRevNr, newValue, false);
							
							txnBuilder.addCommand(changeValueCommand);
						}
					}
				}
			}
		}
		
		/*
		 * we need to remove the fields in a separate loop because modifying the
		 * set of fields of the changedObject while we iterate over it results
		 * in ConcurrentModificationExceptions
		 */
		for(XID fieldId : toBeRemovedFields) {
			changedObject.removeField(fieldId);
		}
	}
	
	/**
	 * This test randomly creates model transactions which execution is supposed
	 * to fail. It uses {@link java.util.Random} to create random transactions.
	 * We set the seed manually and always print it on the screen.
	 * 
	 * If the test fails, simply copy the seed which created the transaction
	 * which was the cause of the failure and set the seed to this value
	 * (instead of using a random seed, as the test normally does). This makes
	 * the test deterministic and enables debugging.
	 */
	@Test
	public void testExecuteCommandFailingModelTransaction() {
		SecureRandom seedGen = new SecureRandom();
		
		for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
			XID failModelId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandFailingModelTransactionFailModel" + i);
			XAddress failModelAddress = XX.resolveModel(this.repoId, failModelId);
			
			GetWithAddressRequest failModelAdrRequest = new GetWithAddressRequest(failModelAddress);
			XCommand addFailModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, failModelId, false);
			
			XID succModelId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandFailingModelTranscationSuccModel" + i);
			XAddress succModelAddress = XX.resolveModel(this.repoId, succModelId);
			
			GetWithAddressRequest succModelAdrRequest = new GetWithAddressRequest(succModelAddress);
			XCommand addSuccModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, succModelId, false);
			
			/*
			 * We use two model instance, which basically represent the same
			 * model. One will be used to execute the succeeding transaction and
			 * the other one for the transaction which is supposed to fail. This
			 * makes testing easier and more flexible.
			 */
			
			// add a model on which an object can be created first
			long failRevNr = this.persistence.executeCommand(this.actorId, addFailModelCom);
			long succRevNr = this.persistence.executeCommand(this.actorId, addSuccModelCom);
			
			assertTrue(
			        "Model for the failing transaction could not be added, test cannot be executed",
			        failRevNr >= 0);
			assertTrue(
			        "Model for the succeeding transaction could not be added, test cannot be executed",
			        succRevNr >= 0);
			
			XWritableModel failModelSnapshot =
			        this.persistence.getModelSnapshot(failModelAdrRequest);
			XWritableModel succModelSnapshot =
			        this.persistence.getModelSnapshot(succModelAdrRequest);
			
			/*
			 * Info: if the test fails, do the following for deterministic
			 * debugging: Set the seed to the value which caused the test to
			 * fail. This makes the test deterministic.
			 */
			long seed = seedGen.nextLong();
			log.info("Creating transaction pair " + i + " with seed " + seed + ".");
			Pair<XTransaction,XTransaction> pair =
			        createRandomFailingModelTransaction(failModelSnapshot, succModelSnapshot, seed);
			
			XTransaction failTxn = pair.getFirst();
			XTransaction succTxn = pair.getSecond();
			
			succRevNr = this.persistence.executeCommand(this.actorId, succTxn);
			assertTrue(
			        "Model Transaction failed, should succeed, since this was the transaction that does not contain the command which should cause the transaction to fail, seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        succRevNr >= 0);
			
			failRevNr = this.persistence.executeCommand(this.actorId, failTxn);
			assertEquals(
			        "Model Transaction succeeded, should fail, seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        XCommand.FAILED, failRevNr);
			
			/*
			 * Make sure that the changes actually weren't executed. Since the
			 * model was empty before the execution of the faulty transaction,
			 * we just need to check if it's still empty. If this is not the
			 * case, this implies that some of the commands in the faulty
			 * transaction were executed, although the transaction's execution
			 * failed.
			 */
			
			failModelSnapshot = this.persistence.getModelSnapshot(failModelAdrRequest);
			assertTrue(
			        "Since the model was empty before the execution of the faulty transaction, it  should be empty. Since it is not empty, some commands of the transaction must've been executed, although the transaction failed."
			                + " Seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        failModelSnapshot.isEmpty());
		}
	}
	
	/**
	 * This test randomly creates object transactions which execution is
	 * supposed to fail. It uses {@link java.util.Random} to create random
	 * transactions. We set the seed manually and always print it on the screen.
	 * 
	 * If the test fails, simply copy the seed which created the transaction
	 * which was the cause of the failure and set the seed to this value
	 * (instead of using random seeds, as the test normally does). This makes
	 * the test deterministic and enables debugging.
	 */
	@Test
	public void testExecuteCommandFailingObjectTransaction() {
		SecureRandom seedGen = new SecureRandom();
		
		for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
			XID failModelId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandFailingObjectTransactionFailModel" + i);
			
			XCommand addFailModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, failModelId, false);
			
			XID succModelId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandFailingObjectTransactionSuccModel" + i);
			
			XCommand addSuccModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, succModelId, false);
			
			/*
			 * We use two model instances, which basically represent the same
			 * model. One will be used to hold the object on which we'll execute
			 * the succeeding transaction and the other one for the object on
			 * which we'll execute the transaction which is supposed to fail.
			 * This makes testing easier and more flexible.
			 */
			
			// add a model on which an object can be created first
			long failRevNr = this.persistence.executeCommand(this.actorId, addFailModelCom);
			long succRevNr = this.persistence.executeCommand(this.actorId, addSuccModelCom);
			
			assertTrue("One of the models could not be added, test cannot be executed.",
			        failRevNr >= 0 && succRevNr >= 0);
			
			XID failObjectId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandFailingObjectTransactionFailObject" + i);
			XAddress failObjectAddress = XX.resolveObject(this.repoId, failModelId, failObjectId);
			
			GetWithAddressRequest failObjectAdrRequest =
			        new GetWithAddressRequest(failObjectAddress);
			XCommand addFailObjectCom =
			        this.comFactory.createAddObjectCommand(this.repoId, failModelId, failObjectId,
			                false);
			
			XID succObjectId =
			        X.getIDProvider().fromString(
			                "testExecuteCommandFailingObjectTransactionSuccObject" + i);
			XAddress succObjectAddress = XX.resolveObject(this.repoId, succModelId, succObjectId);
			
			GetWithAddressRequest succObjectAdrRequest =
			        new GetWithAddressRequest(succObjectAddress);
			XCommand addSuccObjectCom =
			        this.comFactory.createAddObjectCommand(this.repoId, succModelId, succObjectId,
			                false);
			
			/*
			 * We use two object instances, which basically represent the same
			 * object. One will be used to execute the succeeding transaction
			 * and the other one for the transaction which is supposed to fail.
			 * This makes testing easier and more flexible.
			 */
			
			// create the objects on which the transactions will be executed
			failRevNr = this.persistence.executeCommand(this.actorId, addFailObjectCom);
			succRevNr = this.persistence.executeCommand(this.actorId, addSuccObjectCom);
			
			assertTrue("The object for the failing transaction could not be added.", failRevNr >= 0);
			assertTrue("The object for the succeeding transaction could not be added.",
			        succRevNr >= 0);
			
			XWritableObject failObjectSnapshot =
			        this.persistence.getObjectSnapshot(failObjectAdrRequest);
			XWritableObject succObjectSnapshot =
			        this.persistence.getObjectSnapshot(succObjectAdrRequest);
			
			/*
			 * Info: if the test fails, do the following for deterministic
			 * debugging: Set the seed to the value which caused the test to
			 * fail. This makes the test deterministic.
			 */
			long seed = seedGen.nextLong();
			System.out
			        .println("Creating object transaction pair " + i + " with seed " + seed + ".");
			Pair<XTransaction,XTransaction> pair =
			        createRandomFailingObjectTransaction(failObjectSnapshot, succObjectSnapshot,
			                seed);
			
			XTransaction failTxn = pair.getFirst();
			XTransaction succTxn = pair.getSecond();
			
			succRevNr = this.persistence.executeCommand(this.actorId, succTxn);
			assertTrue(
			        "Object Transaction failed, should succeed, since this was the transaction that does not contain the command which should cause the transaction to fail, seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        succRevNr >= 0);
			
			failRevNr = this.persistence.executeCommand(this.actorId, failTxn);
			assertEquals(
			        "Object Transaction succeeded, should fail, seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        XCommand.FAILED, failRevNr);
			
			/*
			 * Make sure that the changes actually weren't executed. Since the
			 * object was empty before we tried to execute the faulty
			 * transaction, we just have to assert that it is still empty. This
			 * implies that no commands of the transaction were executed.
			 */
			failObjectSnapshot = this.persistence.getObjectSnapshot(failObjectAdrRequest);
			assertTrue(
			        "Object was empty before we tried to execute the faulty transaction, but has fields now, which means that some of the commands of the faulty transaction must've been executed, although its execution failed."
			                + " Seed was: "
			                + seed
			                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
			        failObjectSnapshot.isEmpty());
			
		}
	}
	
	/**
	 * Pseudorandomly generates a transaction which should fail on the given
	 * empty model (failModel). Also creates a transaction which does not
	 * contain the event which makes the transaction fail (i.e. this transaction
	 * is supposed to succeed) for the given empty model succModel.
	 * 
	 * The seed determines how the random number generator generates its
	 * pseudorandom output. Using the same seed twice will result in
	 * deterministically generating the same transaction again, which can be
	 * useful when executing a transaction in a test fails and the failed
	 * transaction needs to be reconstructed.
	 */
	private Pair<XTransaction,XTransaction> createRandomFailingModelTransaction(
	        XWritableModel failModel, XWritableModel succModel, long seed) {
		assertTrue("This test only works with empty models.", failModel.isEmpty());
		assertTrue("This test only works with empty models.", succModel.isEmpty());
		
		Random rand = new Random(seed);
		
		/*
		 * we create two transactions that basically do the same, but one of
		 * them does not contain the command which is supposed to let the
		 * execution of the transaction fail. By executing both transactions on
		 * different models (which are in the same state), we'll be able to
		 * ensure that the failing transaction actually fails because of the
		 * specific command we added to let it fail and not because of another
		 * event which execution should succeed.
		 */
		XTransactionBuilder failTxnBuilder = new XTransactionBuilder(failModel.getAddress());
		XTransactionBuilder succTxnBuilder = new XTransactionBuilder(succModel.getAddress());
		
		// create random amount of objects
		int nrOfObjects = 0;
		
		nrOfObjects = 2 + rand.nextInt(10);
		
		/*
		 * determines whether the constructed transaction's execution should
		 * fail because of a command which tries to add or remove an object, but
		 * fails.
		 */
		boolean failBecauseOfAddOrRemoveObjectCommand = rand.nextBoolean();
		int faultyAddOrRemoveObjectCommand = -1;
		
		if(failBecauseOfAddOrRemoveObjectCommand) {
			
			faultyAddOrRemoveObjectCommand = 1 + rand.nextInt(nrOfObjects - 1);
			
			/*
			 * add at least one succeeding command so that succTxnBuilder does
			 * not throw an exception because of an empty command list. The
			 * range is from 0 to nrOfOBjects - 1 because of this too (this
			 * ensures that the random value is between 1 and nrOfObjects-1, if
			 * we wouldn't do this, the variable might be set to nrOfObjects,
			 * which obviously can never be reached, because i only iterates
			 * from 0 to nrOfObject-1).
			 */
		}
		
		/*
		 * begin adding commands until the command which should let the
		 * execution fail is added.
		 */
		boolean faultyCommandAdded = false;
		
		for(int i = 0; i < nrOfObjects && !faultyCommandAdded; i++) {
			XID objectId = X.getIDProvider().fromString("randomObject" + i);
			XAddress failObjectAddress = XX.resolveObject(failModel.getAddress(), objectId);
			XAddress succObjectAddress = XX.resolveObject(succModel.getAddress(), objectId);
			
			if(i == faultyAddOrRemoveObjectCommand) {
				/*
				 * The transaction's execution is supposed to fail because of a
				 * ModelCommand. The next random boolean determines whether it
				 * will fail because we'll try to add an already existing object
				 * or because we'll try to remove a not existing object.
				 */
				boolean failBecauseOfFalseRemove = rand.nextBoolean();
				
				if(failBecauseOfFalseRemove) {
					// fail because we try to remove a not existing object
					XCommand removeCom =
					        this.comFactory.createRemoveObjectCommand(failObjectAddress,
					                failModel.getRevisionNumber(), false);
					
					failTxnBuilder.addCommand(removeCom);
					System.out
					        .println("Transaction will fail because of a faulty ModelCommand of remove type.");
					
				} else {
					// fail because we try to add an already existing object
					XCommand addCom =
					        this.comFactory.createAddObjectCommand(failModel.getAddress(),
					                objectId, false);
					
					// we need to add it twice for this use-case
					failTxnBuilder.addCommand(addCom);
					failTxnBuilder.addCommand(addCom);
					
					System.out
					        .println("Transaction will fail because of a faulty ModelCommand of add type.");
				}
				
				/*
				 * faulty command was added, stop adding more elements
				 */
				faultyCommandAdded = true;
				
			} else {
				XCommand failAddCom =
				        this.comFactory.createAddObjectCommand(failModel.getAddress(), objectId,
				                false);
				
				XCommand succAddCom =
				        this.comFactory.createAddObjectCommand(succModel.getAddress(), objectId,
				                false);
				
				failTxnBuilder.addCommand(failAddCom);
				succTxnBuilder.addCommand(succAddCom);
			}
			
			// add fields
			
			int nrOfFields = 0;
			
			nrOfFields = 1 + rand.nextInt(10);
			// add at least one field
			
			/*
			 * if the transaction isn't supposed to fail because of a faulty
			 * ModelCommand, randomly determine whether it should fail because
			 * of a faulty ObjectCommand.
			 */
			boolean failBecauseOfAddOrRemoveFieldCommand = false;
			int faultyAddOrRemoveFieldCommand = -1;
			if(!failBecauseOfAddOrRemoveObjectCommand) {
				failBecauseOfAddOrRemoveFieldCommand = rand.nextBoolean();
				
				if(!failBecauseOfAddOrRemoveFieldCommand && i + 1 == nrOfObjects) {
					/*
					 * if the transaction wasn't supposed to fail because of a
					 * ModelCommand and the current object is the last, make
					 * sure that the transaction will actually fail by enforcing
					 * that it will fail because of an ObjectCommand.
					 */
					failBecauseOfAddOrRemoveFieldCommand = true;
				}
				
				if(failBecauseOfAddOrRemoveFieldCommand) {
					faultyAddOrRemoveFieldCommand = rand.nextInt(nrOfFields);
				}
			}
			
			for(int j = 0; j < nrOfFields && !faultyCommandAdded; j++) {
				XID fieldId = X.getIDProvider().fromString("randomField" + j);
				
				if(j == faultyAddOrRemoveFieldCommand) {
					/*
					 * The transaction's execution is supposed to fail because
					 * of an ObjectCommand. The next random boolean determines
					 * whether it will fail because we'll try to add an already
					 * existing field or because we'll try to remove a not
					 * existing field.
					 */
					
					boolean failBecauseOfFalseRemove = rand.nextBoolean();
					
					if(failBecauseOfFalseRemove) {
						// fail because we try to remove a not existing field
						XAddress fieldAddress =
						        XX.resolveField(failModel.getAddress(), objectId, fieldId);
						
						XCommand removeCom =
						        this.comFactory.createRemoveFieldCommand(fieldAddress,
						                failModel.getRevisionNumber(), false);
						
						failTxnBuilder.addCommand(removeCom);
						System.out
						        .println("Transaction will fail because of a faulty ObjectCommand of remove type.");
						
					} else {
						// fail because we try to add an already existing field
						XCommand addCom =
						        this.comFactory.createAddFieldCommand(failObjectAddress, fieldId,
						                false);
						
						// we need to add it twice for this use-case
						failTxnBuilder.addCommand(addCom);
						failTxnBuilder.addCommand(addCom);
						System.out
						        .println("Transaction will fail because of a faulty ObjectCommand of add type.");
					}
					
					/*
					 * faulty command was added, stop the construction of the
					 * transaction
					 */
					faultyCommandAdded = true;
					
				} else {
					XCommand failAddCom =
					        this.comFactory
					                .createAddFieldCommand(failObjectAddress, fieldId, false);
					
					XCommand succAddCom =
					        this.comFactory
					                .createAddFieldCommand(succObjectAddress, fieldId, false);
					
					failTxnBuilder.addCommand(failAddCom);
					succTxnBuilder.addCommand(succAddCom);
					
					/*
					 * randomly decide whether the field has a value or not
					 */
					boolean addValue = rand.nextBoolean();
					if(addValue) {
						/*
						 * the field didn't exist before the transaction, so its
						 * revision number is 0.
						 */
						long fieldRevNr = 0;
						
						boolean failBecauseOfFaultyFieldCommand = false;
						if(!failBecauseOfAddOrRemoveFieldCommand
						        && !failBecauseOfAddOrRemoveObjectCommand) {
							failBecauseOfFaultyFieldCommand = rand.nextBoolean();
						}
						
						XAddress failFieldAddress =
						        XX.resolveField(failModel.getAddress(), objectId, fieldId);
						XAddress succFieldAddress =
						        XX.resolveField(succModel.getAddress(), objectId, fieldId);
						
						boolean temp =
						        constructFieldCommandForFaultyTransaction(failFieldAddress,
						                succFieldAddress, failTxnBuilder, succTxnBuilder,
						                failBecauseOfFaultyFieldCommand, fieldRevNr, rand);
						
						if(temp) {
							/*
							 * we're using a temporary variable here because
							 * faultyCommandAdded might already be set to true
							 * and the method might return false, thereby
							 * wrongly setting the faultyCommandAdd to false if
							 * we'd just assign the return value of the method
							 * to faultyCommandAdded.
							 */
							
							faultyCommandAdded = true;
						}
						
					}
				}
			}
		}
		
		/*
		 * TODO add some commands after the failed command to check that this is
		 * no problem
		 */
		
		XTransaction failTxn = failTxnBuilder.build();
		XTransaction succTxn = succTxnBuilder.build();
		
		assertTrue(
		        "There seems to be a bug in the code of the test, since no faulty command was added to the transaction which' execution is supposed to fail, which results in a transaction which' execution would succeed.",
		        faultyCommandAdded);
		Pair<XTransaction,XTransaction> pair =
		        new Pair<XTransaction,XTransaction>(failTxn, succTxn);
		return pair;
		
	}
	
	/**
	 * Pseudorandomly generates a transaction which should fail on the given
	 * empty object (failObject). Also creates a transaction which does not
	 * contain the event which makes the transaction fail (i.e. this transaction
	 * is supposed to succeed) for the given empty object (succObject).
	 * 
	 * The seed determines how the random number generator generates its
	 * pseudorandom output. Using the same seed twice will result in
	 * deterministically generating the same transaction again, which can be
	 * useful when executing a transaction in a test fails and the failed
	 * transaction needs to be reconstructed.
	 */
	private Pair<XTransaction,XTransaction> createRandomFailingObjectTransaction(
	        XWritableObject failObject, XWritableObject succObject, long seed) {
		assertTrue("This test only works with empty objects.", failObject.isEmpty());
		assertTrue("This test only works with empty objects", succObject.isEmpty());
		
		Random rand = new Random(seed);
		
		/*
		 * we create two transactions that basically do the same, but one of
		 * them does not contain the command which is supposed to let the
		 * execution of the transaction fail. By executing both transactions on
		 * different models (which are in the same state), we'll be able to
		 * ensure that the failing transaction actually fails because of the
		 * specific command we added to let it fail and not because of another
		 * event which execution should succeed.
		 */
		XTransactionBuilder failTxnBuilder = new XTransactionBuilder(failObject.getAddress());
		XTransactionBuilder succTxnBuilder = new XTransactionBuilder(succObject.getAddress());
		
		/*
		 * begin adding commands until the command which should let the
		 * execution fail is added.
		 */
		boolean faultyCommandAdded = false;
		
		// add fields
		
		int nrOfFields = 0;
		
		nrOfFields = 2 + rand.nextInt(10);
		// add at least two fields
		
		/*
		 * randomly determine whether the transaction should fail because of a
		 * faulty ObjectCommand.
		 */
		boolean failBecauseOfAddOrRemoveFieldCommand = false;
		int faultyAddOrRemoveFieldCommand = -1;
		
		failBecauseOfAddOrRemoveFieldCommand = rand.nextBoolean();
		
		if(failBecauseOfAddOrRemoveFieldCommand) {
			faultyAddOrRemoveFieldCommand = 1 + rand.nextInt(nrOfFields - 1);
		}
		
		for(int j = 0; j < nrOfFields && !faultyCommandAdded; j++) {
			XID fieldId = X.getIDProvider().fromString("randomField" + j);
			
			if(j == faultyAddOrRemoveFieldCommand) {
				/*
				 * The transaction's execution is supposed to fail because of an
				 * ObjectCommand. The next random boolean determines whether it
				 * will fail because we'll try to add an already existing field
				 * or because we'll try to remove a not existing field.
				 */
				
				boolean failBecauseOfFalseRemove = rand.nextBoolean();
				
				if(failBecauseOfFalseRemove) {
					// fail because we try to remove a not existing field
					XAddress fieldAddress = XX.resolveField(failObject.getAddress(), fieldId);
					
					XCommand removeCom =
					        this.comFactory.createRemoveFieldCommand(fieldAddress,
					                failObject.getRevisionNumber(), false);
					
					failTxnBuilder.addCommand(removeCom);
					System.out
					        .println("Transaction will fail because of a faulty ObjectCommand of remove type.");
					
				} else {
					// fail because we try to add an already existing field
					XCommand addCom =
					        this.comFactory.createAddFieldCommand(failObject.getAddress(), fieldId,
					                false);
					
					// we need to add it twice for this use-case
					failTxnBuilder.addCommand(addCom);
					failTxnBuilder.addCommand(addCom);
					System.out
					        .println("Transaction will fail because of a faulty ObjectCommand of add type.");
				}
				
				/*
				 * faulty command was added, stop the construction of the
				 * transaction
				 */
				faultyCommandAdded = true;
				
			} else {
				XCommand failAddCom =
				        this.comFactory.createAddFieldCommand(failObject.getAddress(), fieldId,
				                false);
				
				XCommand succAddCom =
				        this.comFactory.createAddFieldCommand(succObject.getAddress(), fieldId,
				                false);
				
				failTxnBuilder.addCommand(failAddCom);
				succTxnBuilder.addCommand(succAddCom);
				
				/*
				 * randomly decide whether the field has a value or not
				 */
				
				boolean addValue = rand.nextBoolean();
				if(!faultyCommandAdded && j + 1 == nrOfFields) {
					/*
					 * assures that a faulty field command will be added if no
					 * faulty command was added up till now and this is the last
					 * iteration through the loop which creates commands.
					 * 
					 * If we wouldn't set addValue to true here, the following
					 * might occur: No faulty command was added until the last
					 * iteration, addValue is set to false and therefore no
					 * faulty field command might be added, which will result in
					 * a transaction without a faulty command, i.e. a
					 * transaction which' execution should succeed. If we set
					 * addValue to true in this case, the code after
					 * "if(addValue)..." will ensure that a faulty field command
					 * will be added.
					 */
					addValue = true;
				}
				if(addValue) {
					/*
					 * the field didn't exist before the transaction, so its
					 * revision number is 0.
					 */
					long fieldRevNr = 0;
					
					boolean failBecauseOfFaultyFieldCommand = false;
					if(!failBecauseOfAddOrRemoveFieldCommand) {
						failBecauseOfFaultyFieldCommand = rand.nextBoolean();
						
						if(!failBecauseOfFaultyFieldCommand && j + 1 == nrOfFields) {
							/*
							 * make sure that the transaction will fail if this
							 * is the last command which will be added but no
							 * faulty command was added until now
							 */
							failBecauseOfFaultyFieldCommand = true;
						}
					}
					
					XAddress failFieldAddress = XX.resolveField(failObject.getAddress(), fieldId);
					XAddress succFieldAddress = XX.resolveField(succObject.getAddress(), fieldId);
					
					boolean temp =
					        constructFieldCommandForFaultyTransaction(failFieldAddress,
					                succFieldAddress, failTxnBuilder, succTxnBuilder,
					                failBecauseOfFaultyFieldCommand, fieldRevNr, rand);
					
					if(temp) {
						/*
						 * we're using a temporary variable here because
						 * faultyCommandAdded might already be set to true and
						 * the method might return false, thereby wrongly
						 * setting the faultyCommandAdd to false if we'd just
						 * assign the return value of the method to
						 * faultyCommandAdded.
						 */
						
						faultyCommandAdded = true;
					}
				}
			}
		}
		
		/*
		 * TODO add some commands after the failed command to check that this is
		 * no problem
		 */
		
		XTransaction failTxn = failTxnBuilder.build();
		XTransaction succTxn = succTxnBuilder.build();
		
		assertTrue(
		        "There seems to be a bug in the code of the test, since no faulty command was added to the transaction which' execution is supposed to fail, which results in a transaction which' execution would succeed.",
		        faultyCommandAdded);
		Pair<XTransaction,XTransaction> pair =
		        new Pair<XTransaction,XTransaction>(failTxn, succTxn);
		return pair;
		
	}
	
	private boolean constructFieldCommandForFaultyTransaction(XAddress failFieldAddress,
	        XAddress succFieldAddress, XTransactionBuilder failTxnBuilder,
	        XTransactionBuilder succTxnBuilder, boolean failBecauseOfFaultyFieldCommand,
	        long fieldRevNr, Random rand) {
		
		if(failBecauseOfFaultyFieldCommand) {
			int reason = rand.nextInt(3);
			
			if(reason == 0) {
				// fail because we try to remove a not existing
				// value
				XCommand failRemoveCom =
				        this.comFactory.createRemoveValueCommand(failFieldAddress, fieldRevNr,
				                false);
				
				failTxnBuilder.addCommand(failRemoveCom);
				
				System.out
				        .println("Transaction will fail because of a faulty FieldCommand of remove type.");
			} else if(reason == 1) {
				// fail because we try to add a value to a field
				// which value is already set
				
				XValue value1 = createRandomValue(rand);
				XValue value2 = createRandomValue(rand);
				XCommand failAddCom1 =
				        this.comFactory.createAddValueCommand(failFieldAddress, fieldRevNr, value1,
				                false);
				XCommand failAddCom2 =
				        this.comFactory.createAddValueCommand(failFieldAddress, fieldRevNr, value2,
				                false);
				
				failTxnBuilder.addCommand(failAddCom1);
				failTxnBuilder.addCommand(failAddCom2);
				
				System.out
				        .println("Transaction will fail because of a faulty FieldCommand of add type.");
			} else {
				assert reason == 2;
				// fail because we try to change the value of a
				// field which value isn't set
				XValue value = createRandomValue(rand);
				XCommand failChangeCom =
				        this.comFactory.createChangeValueCommand(failFieldAddress, fieldRevNr,
				                value, false);
				
				failTxnBuilder.addCommand(failChangeCom);
				
				System.out
				        .println("Transaction will fail because of a faulty FieldCommand of change type.");
			}
			
			/*
			 * faulty command was added, stop construction
			 */
			return true;
			
		} else {
			XValue value = X.getValueFactory().createStringValue("" + rand.nextInt());
			
			XCommand failAddValueCom =
			        this.comFactory.createAddValueCommand(failFieldAddress, fieldRevNr, value,
			                false);
			XCommand succAddValueCom =
			        this.comFactory.createAddValueCommand(succFieldAddress, fieldRevNr, value,
			                false);
			
			failTxnBuilder.addCommand(failAddValueCom);
			succTxnBuilder.addCommand(succAddValueCom);
			
			return false;
		}
	}
	
	private static XValue createRandomValue(Random rand) {
		int type = rand.nextInt(6);
		
		XValue value = null;
		
		switch(type) {
		case 0:
			// random integer value
			value = X.getValueFactory().createIntegerValue(rand.nextInt());
			
			break;
		case 1:
			// random long value
			value = X.getValueFactory().createLongValue(rand.nextLong());
			
			break;
		case 2:
			// random double value
			value = X.getValueFactory().createDoubleValue(rand.nextDouble());
			
			break;
		case 3:
			// random boolean value
			value = X.getValueFactory().createBooleanValue(rand.nextBoolean());
			
			break;
		case 4:
			// random String value
			value = X.getValueFactory().createStringValue("" + rand.nextInt());
			
			break;
		case 5:
			// random XID
			value = XX.toId("RandomXID" + rand.nextInt());
			
			break;
		case 6:
			// random XAddress
			XID repoId = XX.toId("RandomRepoID" + rand.nextInt());
			XID modelId = XX.toId("RandomModelID" + rand.nextInt());
			XID objectId = XX.toId("RandomObjectID" + rand.nextInt());
			XID fieldId = XX.toId("RandomFieldID" + rand.nextInt());
			
			value = XX.toAddress(repoId, modelId, objectId, fieldId);
			
			break;
		}
		
		return value;
	}
	
	@Test
	public void testGetEvents() {
		/*
		 * This testcase is unfortunately pretty large, but since it's better to
		 * test the events in context with multiple commands there's really no
		 * good way around it.
		 */
		
		XID modelId = XX.toId("testGetEventsModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);
		
		/*
		 * check if the returned list (only) contains the "model was added"
		 * event
		 */
		assertEquals(
		        "List of events should only contain one event (the \"create model\"-event), but actually contains zero or multiple events.",
		        1, events.size());
		
		XEvent modelAddEvent = events.get(0);
		assertTrue(
		        "The returned event is not an XRepositoryEvent, but \"model was created\"-events are XRepositoryEvents",
		        modelAddEvent instanceof XRepositoryEvent);
		assertEquals("The returned event was not of add-type.", ChangeType.ADD,
		        modelAddEvent.getChangeType());
		assertEquals("The event didn't refer to the correct old revision number.",
		        XEvent.RevisionOfEntityNotSet, modelAddEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        modelAddEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", this.repoAddress,
		        modelAddEvent.getTarget());
		assertEquals("Event doesn't refer to the correct model.", modelAddress,
		        modelAddEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        modelAddEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", modelAddEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        modelAddEvent.inTransaction());
		
		/*
		 * add an object and check if the correct events are returned. Check
		 * getEvents(objectAddress...) and getEvents(modelAddress...) since they
		 * might behave differently.
		 */
		
		XID objectId = XX.toId("testGetEventsObject");
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		long oldModelRev = revNr;
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		assertTrue("The object wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		// get events from the object first and check them
		
		events = this.persistence.getEvents(objectAddress, revNr, revNr);
		
		/*
		 * check if the returned list (only) contains the "object was added"
		 * event
		 */
		assertEquals(
		        "List of events should only contain one event (the \"create object\"-event), but contains 0 or multiple events.",
		        1, events.size());
		
		XEvent objectAddEvent = events.get(0);
		assertTrue(
		        "The returned event is not an XModelEvent, but \"object was created\"-events are XModelEvents",
		        objectAddEvent instanceof XModelEvent);
		assertEquals("The returned event was not of add-type.", ChangeType.ADD,
		        objectAddEvent.getChangeType());
		assertEquals("The event didn't refer to the correct old revision number.", oldModelRev,
		        objectAddEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        objectAddEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", modelAddress,
		        objectAddEvent.getTarget());
		assertEquals("Event doesn't refer to the correct object.", objectAddress,
		        objectAddEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        objectAddEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", objectAddEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        objectAddEvent.inTransaction());
		
		/*
		 * check if the list returned by the model contains the correct events.
		 */
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		
		events = this.persistence.getEvents(modelAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add model\"-event.",
		        events.contains(modelAddEvent));
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		
		/*
		 * add a field and check if the correct events are returned. Check
		 * getEvents(fieldAddress...), getEvents(objectAddress...) and
		 * getEvents(modelAddress...) since they might behave differently.
		 */
		
		XID fieldId = XX.toId("testGetEventsField");
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom =
		        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
		                false);
		long oldObjectRev = revNr;
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		assertTrue("The field  wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		// get events from the field first and check them
		events = this.persistence.getEvents(fieldAddress, revNr, revNr);
		
		assertEquals(
		        "List of events should only contain one event (the \"create field\"-event), but actually contains zero or multiple events.",
		        1, events.size());
		
		XEvent fieldAddEvent = events.get(0);
		assertTrue(
		        "The returned event is not an XObjectEvent, but \"field was created\"-events are XObjectEvents",
		        fieldAddEvent instanceof XObjectEvent);
		assertEquals("The returned event was not of add-type.", ChangeType.ADD,
		        fieldAddEvent.getChangeType());
		
		// oldObjectRev is also the old revision number of the model (before the
		// field was added)
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldObjectRev, fieldAddEvent.getOldModelRevision());
		
		assertEquals("The event didn't refer to the correct old object revision number.",
		        oldObjectRev, fieldAddEvent.getOldObjectRevision());
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        fieldAddEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", objectAddress,
		        fieldAddEvent.getTarget());
		assertEquals("Event doesn't refer to the correct field.", fieldAddress,
		        fieldAddEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        fieldAddEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", fieldAddEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        fieldAddEvent.inTransaction());
		
		/*
		 * check if the list returned by the object contains the correct events.
		 */
		events = this.persistence.getEvents(objectAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		
		events = this.persistence.getEvents(objectAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		
		/*
		 * check if the list returned by the model contains the correct events.
		 */
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		
		// oldObjectRev is also the old revision number of the model (before the
		// field was added)
		events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		
		events = this.persistence.getEvents(modelAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, although only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add model\"-event.",
		        events.contains(modelAddEvent));
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		
		/*
		 * add a value and check if the correct events are returned. Check
		 * getEvents(fieldAddress...), getEvents(objectAddress...) and
		 * getEvents(modelAddress...) since they might behave differently.
		 */
		
		XValue value = X.getValueFactory().createStringValue("testValue");
		XCommand addValueCom =
		        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value, false);
		long oldFieldRev = revNr;
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		assertTrue("The value  wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		// get events from the field first and check them
		events = this.persistence.getEvents(fieldAddress, revNr, revNr);
		
		assertEquals(
		        "List of events should only contain one event (the \"add value\"-event), but actually contains zero or multiple events.",
		        1, events.size());
		
		XEvent valueAddEvent = events.get(0);
		assertTrue(
		        "The returned event is not an XFieldEvent, but \"value was added\"-events are XFieldEvents",
		        valueAddEvent instanceof XFieldEvent);
		assertEquals("The returned event was not of add-type.", ChangeType.ADD,
		        valueAddEvent.getChangeType());
		
		// oldFieldRev is also the old revision number of the model & object
		// (before the
		// value was added)
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldFieldRev, valueAddEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct old object revision number.",
		        oldFieldRev, valueAddEvent.getOldObjectRevision());
		
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        valueAddEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", fieldAddress,
		        valueAddEvent.getTarget());
		assertEquals("Event doesn't refer to the correct field.", fieldAddress,
		        valueAddEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        valueAddEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", valueAddEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        valueAddEvent.inTransaction());
		
		events = this.persistence.getEvents(fieldAddress, oldFieldRev, revNr);
		assertEquals(
		        "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(fieldAddress, 0, revNr);
		assertEquals(
		        "The list does not contain exactly 2 elements, even though only 2 commands were executed which affected the field.",
		        2, events.size());
		
		/*
		 * check if the list returned by the object contains the correct events.
		 */
		events = this.persistence.getEvents(objectAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(objectAddress, oldFieldRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(objectAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(objectAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, even though only 3 commands were executed which affected the object.",
		        3, events.size());
		
		/*
		 * check if the list returned by the model contains the correct events.
		 */
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(modelAddress, oldFieldRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, although only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		events = this.persistence.getEvents(modelAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 4 elements, although only 4 commands were executed.",
		        4, events.size());
		assertTrue("The list does not contain the \"add model\"-event.",
		        events.contains(modelAddEvent));
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		
		/*
		 * change the value and check if the correct events are returned. Check
		 * getEvents(fieldAddress...), getEvents(objectAddress...) and
		 * getEvents(modelAddress...) since they might behave differently.
		 */
		
		XValue value2 = X.getValueFactory().createStringValue("testValue2");
		XCommand changeValueCom =
		        this.comFactory.createChangeValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, value2, false);
		long oldFieldRev2 = revNr;
		revNr = this.persistence.executeCommand(this.actorId, changeValueCom);
		assertTrue("The value  wasn't correctly changed, test cannot be executed.", revNr >= 0);
		
		// get events from the field first and check them
		events = this.persistence.getEvents(fieldAddress, revNr, revNr);
		
		assertEquals(
		        "List of events should only contain one event (the \"change value\"-event), but actually contains zero or multiple events.",
		        1, events.size());
		
		XEvent valueChangeEvent = events.get(0);
		assertTrue(
		        "The returned event is not an XFieldEvent, but \"value was changed\"-events are XFieldEvents",
		        valueChangeEvent instanceof XFieldEvent);
		assertEquals("The returned event was not of change-type.", ChangeType.CHANGE,
		        valueChangeEvent.getChangeType());
		
		/*
		 * oldFieldRev2 is also the old revision number of the model & object
		 * (before the value was changed)
		 */
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldFieldRev2, valueChangeEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct old object revision number.",
		        oldFieldRev2, valueChangeEvent.getOldObjectRevision());
		
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        valueChangeEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", fieldAddress,
		        valueChangeEvent.getTarget());
		assertEquals("Event doesn't refer to the correct field.", fieldAddress,
		        valueChangeEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        valueChangeEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", valueChangeEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        valueChangeEvent.inTransaction());
		
		events = this.persistence.getEvents(fieldAddress, oldFieldRev2, revNr);
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 new command was executed since the last revision.",
		        2, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(fieldAddress, oldFieldRev, revNr);
		assertEquals(
		        "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(fieldAddress, 0, revNr);
		assertEquals(
		        "The list contains more than 3 elements, even though only 3 commands were executed which affected the field.",
		        3, events.size());
		
		/*
		 * check if the list returned by the object contains the correct events.
		 */
		events = this.persistence.getEvents(objectAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"change value\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(objectAddress, oldFieldRev2, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(objectAddress, oldFieldRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(objectAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 4 elements, even though only 4 commands were executed.",
		        4, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(objectAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 4 elements, even though only 4 commands were executed which affected the object.",
		        4, events.size());
		
		/*
		 * check if the list returned by the model contains the correct events.
		 */
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(modelAddress, oldFieldRev2, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(modelAddress, oldFieldRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, although only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 4 elements, although only 4 commands were executed.",
		        4, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		events = this.persistence.getEvents(modelAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 5 elements, although only 5 commands were executed.",
		        5, events.size());
		assertTrue("The list does not contain the \"add model\"-event.",
		        events.contains(modelAddEvent));
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		
		/*
		 * remove the value and check if the correct events are returned. Check
		 * getEvents(fieldAddress...), getEvents(objectAddress...) and
		 * getEvents(modelAddress...) since they might behave differently.
		 */
		
		XCommand removeValueCom =
		        this.comFactory.createRemoveValueCommand(this.repoId, modelId, objectId, fieldId,
		                revNr, false);
		long oldFieldRev3 = revNr;
		revNr = this.persistence.executeCommand(this.actorId, removeValueCom);
		assertTrue("The value  wasn't correctly removed, test cannot be executed.", revNr >= 0);
		
		// get events from the field first and check them
		events = this.persistence.getEvents(fieldAddress, revNr, revNr);
		
		assertEquals(
		        "List of events should only contain one event (the \"change value\"-event), but actually contains zero or multiple events.",
		        1, events.size());
		
		XEvent valueRemoveEvent = events.get(0);
		assertTrue(
		        "The returned event is not an XFieldEvent, but \"value was removed\"-events are XFieldEvents",
		        valueRemoveEvent instanceof XFieldEvent);
		assertEquals("The returned event was not of remove-type.", ChangeType.REMOVE,
		        valueRemoveEvent.getChangeType());
		
		/*
		 * oldFieldRev3 is also the old revision number of the model & object
		 * (before the value was changed)
		 */
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldFieldRev3, valueRemoveEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct old object revision number.",
		        oldFieldRev3, valueRemoveEvent.getOldObjectRevision());
		
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        valueRemoveEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", fieldAddress,
		        valueRemoveEvent.getTarget());
		assertEquals("Event doesn't refer to the correct field.", fieldAddress,
		        valueRemoveEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        valueRemoveEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", valueRemoveEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        valueRemoveEvent.inTransaction());
		
		events = this.persistence.getEvents(fieldAddress, oldFieldRev3, revNr);
		assertEquals(
		        "The list does not contain exactly 2 element, although only 2 new command was executed since the last revision.",
		        2, events.size());
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(fieldAddress, oldFieldRev2, revNr);
		assertEquals(
		        "The list does not contain exactly 3 element, although only 3 new command was executed since the last revision.",
		        3, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(fieldAddress, oldFieldRev, revNr);
		assertEquals(
		        "The list does not contain exactly 4 elements, even though only 4 commands were executed.",
		        4, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(fieldAddress, 0, revNr);
		assertEquals(
		        "The list does not contain exactly 4 elements, even though only 4 commands were executed which affected the field.",
		        4, events.size());
		
		/*
		 * check if the list returned by the object contains the correct events.
		 */
		events = this.persistence.getEvents(objectAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(objectAddress, oldFieldRev3, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(objectAddress, oldFieldRev2, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(objectAddress, oldFieldRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 4 elements, even though only 4 commands were executed.",
		        4, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(objectAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 5 elements, even though only 5 commands were executed.",
		        5, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(objectAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 5 elements, even though only 5 commands were executed which affected the object.",
		        5, events.size());
		
		/*
		 * check if the list returned by the model contains the correct events.
		 */
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		assertEquals(
		        "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
		        1, events.size());
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(modelAddress, oldFieldRev3, revNr);
		
		assertEquals(
		        "The list does not contain exactly 2 elements, although only 2 commands were executed.",
		        2, events.size());
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(modelAddress, oldFieldRev2, revNr);
		
		assertEquals(
		        "The list does not contain exactly 3 elements, although only 3 commands were executed.",
		        3, events.size());
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(modelAddress, oldFieldRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 4 elements, although only 4 commands were executed.",
		        4, events.size());
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);
		
		assertEquals(
		        "The list does not contain exactly 5 elements, although only 5 commands were executed.",
		        5, events.size());
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		events = this.persistence.getEvents(modelAddress, 0, revNr);
		
		assertEquals(
		        "The list does not contain exactly 6 elements, although only 6 commands were executed.",
		        6, events.size());
		assertTrue("The list does not contain the \"add model\"-event.",
		        events.contains(modelAddEvent));
		assertTrue("The list does not contain the \"add object\"-event.",
		        events.contains(objectAddEvent));
		assertTrue("The list does not contain the \"add field\"-event.",
		        events.contains(fieldAddEvent));
		assertTrue("The list does not contain the \"add value\"-event.",
		        events.contains(valueAddEvent));
		assertTrue("The list does not contain the \"change field\"-event.",
		        events.contains(valueChangeEvent));
		assertTrue("The list does not contain the \"remove field\"-event.",
		        events.contains(valueRemoveEvent));
		
		/*
		 * Remove the object and check if the correct events are returned.
		 * 
		 * The returned event should be an XTransactionEvent since removing the
		 * object also removes the field. Check that the \"field was
		 * removed\"-event is implicit.
		 */
		
		XCommand removeObjectCom =
		        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId, revNr,
		                false);
		long oldObjectRev2 = revNr;
		revNr = this.persistence.executeCommand(this.actorId, removeObjectCom);
		assertTrue("The object  wasn't correctly removed, test cannot be executed.", revNr >= 0);
		
		// get events from the model first and check them
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		
		assertEquals(
		        "List of events should contain one transaction event (which represents the \"object was removed\"- and the implicit \"field was removed\"-event), but actually contains zero or more than 2 events.",
		        1, events.size());
		
		assertTrue(
		        "The returned event is not an XTransactionEvent, but it should be an XTransactionEvent, since we removed the object and thereby also implicitly removed the field.",
		        events.get(0) instanceof XTransactionEvent);
		XTransactionEvent transEvent = (XTransactionEvent)events.get(0);
		
		/*
		 * check the transaction event
		 */
		assertEquals("The transaction event is not of the transaction-type.",
		        ChangeType.TRANSACTION, transEvent.getChangeType());
		assertEquals(
		        "The transcation event should contain 2 events, but actually contains zero or more than 2 events.",
		        2, transEvent.size());
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldObjectRev2, transEvent.getOldModelRevision());
		
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        transEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", modelAddress,
		        transEvent.getTarget());
		assertEquals("Event doesn't refer to the correct changed entity.", modelAddress,
		        transEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId, transEvent.getActor());
		assertFalse("The event is wrongly marked as bein implied implied.", transEvent.isImplied());
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        transEvent.inTransaction());
		
		XEvent fieldRemoveEvent, objectRemoveEvent;
		
		/*
		 * check that the transaction event consists of one XModelEvent (=
		 * Object was removed) and one XObjectEvent (= Field was removed)
		 */
		if(transEvent.getEvent(0) instanceof XObjectEvent) {
			fieldRemoveEvent = transEvent.getEvent(0);
			objectRemoveEvent = transEvent.getEvent(1);
			
			assertTrue("There was an XObjectEvent, but no XModelEvent.",
			        objectRemoveEvent instanceof XModelEvent);
		} else {
			fieldRemoveEvent = transEvent.getEvent(1);
			objectRemoveEvent = transEvent.getEvent(0);
			
			assertTrue("The first event is neither an XObject- nor an XModelEvent.",
			        objectRemoveEvent instanceof XModelEvent);
			assertTrue("The second event is not an XModelEvent",
			        fieldRemoveEvent instanceof XObjectEvent);
		}
		
		assertEquals("The returned event was not of remove-type.", ChangeType.REMOVE,
		        objectRemoveEvent.getChangeType());
		assertEquals("The returned event was not of remove-type.", ChangeType.REMOVE,
		        fieldRemoveEvent.getChangeType());
		
		/*
		 * oldObjectRev2 is also the old revision number of the model & object
		 * (before the value was changed)
		 */
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldObjectRev2, fieldRemoveEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct old object revision number.",
		        oldObjectRev2, fieldRemoveEvent.getOldObjectRevision());
		
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        fieldRemoveEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", objectAddress,
		        fieldRemoveEvent.getTarget());
		assertEquals("Event doesn't refer to the correct field.", fieldAddress,
		        fieldRemoveEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        fieldRemoveEvent.getActor());
		assertTrue("The event is wrongly marked as not implied.", fieldRemoveEvent.isImplied());
		assertTrue("the event is wrongly marked as not being part of a transaction.",
		        fieldRemoveEvent.inTransaction());
		
		assertEquals("The event didn't refer to the correct old model revision number.",
		        oldObjectRev2, objectRemoveEvent.getOldModelRevision());
		assertEquals("The event didn't refer to the correct old object revision number.",
		        oldObjectRev2, objectRemoveEvent.getOldObjectRevision());
		
		assertEquals("The event didn't refer to the correct revision number.", revNr,
		        objectRemoveEvent.getRevisionNumber());
		assertEquals("Event doesn't refer to the correct target.", modelAddress,
		        objectRemoveEvent.getTarget());
		assertEquals("Event doesn't refer to the correct field.", objectAddress,
		        objectRemoveEvent.getChangedEntity());
		assertEquals("The actor of the event is not correct.", this.actorId,
		        objectRemoveEvent.getActor());
		assertFalse("The event is wrongly marked as implied.", objectRemoveEvent.isImplied());
		assertTrue("the event is wrongly marked as not being part of a transaction.",
		        objectRemoveEvent.inTransaction());
		
		/*
		 * removing the model doesn't need to be checked, since the getEvents
		 * method only gets events from models, objects and fields, but not from
		 * the repository.
		 */
		
	}
	
	@Test
	public void testGetEventsModelTransactions() {
		SecureRandom seedGen = new SecureRandom();
		
		for(int i = 0; i < this.nrOfIterationsForTxnTests; i++) {
			
			XID modelId = XX.toId("testGetEventsTransactionsModel" + i);
			XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
			XCommand addModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
			long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
			
			GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
			XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
			
			/*
			 * Info: if the test fails, do the following for deterministic
			 * debugging: Set the seed to the value which caused the test to
			 * fail. This makes the test deterministic.
			 */
			long seed = seedGen.nextLong();
			log.info("Used seed: " + seed + ".");
			Pair<ChangedModel,XTransaction> pair =
			        createRandomSucceedingModelTransaction(model, seed);
			XTransaction txn = pair.getSecond();
			
			revNr = this.persistence.executeCommand(this.actorId, txn);
			assertTrue("Transaction did not succeed.", revNr >= 0);
			
			List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);
			assertEquals(
			        "The list of events should contain one Transaction Event, but actually contains multiple events.",
			        1, events.size());
			
			XEvent event = events.get(0);
			assertTrue("The returned event should be a TransactionEvent.",
			        event instanceof XTransactionEvent);
			
			XTransactionEvent txnEvent = (XTransactionEvent)event;
			
			assertEquals("The event didn't refer to the correct old revision number.", 0,
			        txnEvent.getOldModelRevision());
			assertEquals("The event didn't refer to the correct revision number.", revNr,
			        txnEvent.getRevisionNumber());
			assertEquals("Event doesn't refer to the correct target.", modelAddress,
			        txnEvent.getTarget());
			assertEquals("Event doesn't refer to the correct changed entity.", modelAddress,
			        txnEvent.getChangedEntity());
			assertEquals("The actor of the event is not correct.", this.actorId,
			        txnEvent.getActor());
			assertFalse("The event is wrongly marked as implied.", txnEvent.isImplied());
			assertFalse("the event is wrongly marked as being part of a transaction.",
			        txnEvent.inTransaction());
			
			/*
			 * TODO check the events that make up the transaction!
			 */
			
			/*
			 * TODO document why there are no "removedObjectEvents" (etc.) lists
			 */
			Map<XAddress,XEvent> addedObjectEvents = new HashMap<XAddress,XEvent>();
			Map<XAddress,XEvent> addedFieldEvents = new HashMap<XAddress,XEvent>();
			Map<XAddress,XEvent> addedValueEvents = new HashMap<XAddress,XEvent>();
			
			for(XEvent ev : txnEvent) {
				/*
				 * TODO Notice that this might change in the future and the
				 * randomly constructed succeeding transaction might also
				 * contain events of other types.
				 * 
				 * Check why the transaction events only contain ADD type
				 * events, although the transactions themselves also contain
				 * changes and removes. Does the TransactionBuilder or the
				 * system which creates the transaction event already fine tune
				 * the result so that changes and removes which occur on
				 * objects/fields which were added during the transaction aren't
				 * even shown?
				 */
				assertTrue("The transaction should only contain events of the Add-type.",
				        ev.getChangeType() == ChangeType.ADD);
				assertTrue("Event is wrongly marked as not being part of a transaction.",
				        ev.inTransaction());
				assertEquals("The event doesn't refer to the correct model.", modelId, ev
				        .getTarget().getModel());
				assertFalse("The event is wrongly marked as being implied.", event.isImplied());
				assertEquals("The actor of the event is not correct.", this.actorId,
				        event.getActor());
				assertEquals("The event didn't refer to the correct revision number.", revNr,
				        ev.getRevisionNumber());
				
				if(ev.getChangedEntity().getAddressedType() == XType.XOBJECT) {
					
					addedObjectEvents.put(ev.getChangedEntity(), ev);
				} else {
					assertEquals(
					        "A model transaction should only contain commands that target objects or fields.",
					        ev.getChangedEntity().getAddressedType(), XType.XFIELD);
					
					if(ev instanceof XObjectEvent) {
						addedFieldEvents.put(ev.getChangedEntity(), ev);
					} else {
						assertTrue(ev instanceof XFieldEvent);
						addedValueEvents.put(ev.getChangedEntity(), ev);
					}
				}
			}
			
			model = this.persistence.getModelSnapshot(modelAdrRequest);
			for(XID objectId : model) {
				XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
				
				assertTrue(
				        "Since the model was emtpy before the transaction, there should be a fitting add-event for the object with XID "
				                + objectId + ".", addedObjectEvents.containsKey(objectAddress));
				XReadableObject object = model.getObject(objectId);
				
				for(XID fieldId : object) {
					XAddress fieldAddress =
					        XX.resolveField(this.repoId, modelId, objectId, fieldId);
					
					assertTrue(
					        "Since the model was emtpy before the transaction, there should be a fitting add-event for the field with XID "
					                + fieldId + ".", addedFieldEvents.containsKey(fieldAddress));
					XReadableField field = object.getField(fieldId);
					if(field.getValue() != null) {
						assertTrue(
						        "Since the model was emtpy before the transaction, there should be a fitting add-event for the value in the field with XID "
						                + fieldId + ".", addedValueEvents.containsKey(fieldAddress));
					}
				}
			}
		}
	}
	
	@Test
	public void testGetEventsObjectTransactions() {
		SecureRandom seedGen = new SecureRandom();
		
		for(int i = 0; i < this.nrOfIterationsForTxnTests; i++) {
			
			XID modelId = XX.toId("testGetEventsObjectTransactionsModel" + i);
			XCommand addModelCom =
			        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
			long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
			
			XID objectId = XX.toId("testGetEventsObjectTransactionsObject" + i);
			XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
			XCommand addObjectCom =
			        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
			revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
			assertTrue("The object wasn't correctly added, test cannot be executed.", revNr >= 0);
			
			GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
			XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
			
			/*
			 * Info: if the test fails, do the following for deterministic
			 * debugging: Set the seed to the value which caused the test to
			 * fail. This makes the test deterministic.
			 */
			long seed = seedGen.nextLong();
			log.info("Used seed: " + seed + ".");
			Pair<ChangedObject,XTransaction> pair =
			        createRandomSucceedingObjectTransaction(object, seed);
			XTransaction txn = pair.getSecond();
			
			revNr = this.persistence.executeCommand(this.actorId, txn);
			assertTrue("Transaction did not succeed.", revNr >= 0);
			
			List<XEvent> events = this.persistence.getEvents(objectAddress, revNr, revNr);
			assertEquals(
			        "The list of events should contain one Transaction Event, but actually contains zero or multiple events.",
			        1, events.size());
			
			XEvent event = events.get(0);
			assertTrue("The returned event should be a TransactionEvent.",
			        event instanceof XTransactionEvent);
			
			XTransactionEvent txnEvent = (XTransactionEvent)event;
			
			assertEquals("The event didn't refer to the correct old revision number.", 1,
			        txnEvent.getOldObjectRevision());
			assertEquals("The event didn't refer to the correct revision number.", revNr,
			        txnEvent.getRevisionNumber());
			assertEquals("Event doesn't refer to the correct target.", objectAddress,
			        txnEvent.getTarget());
			assertEquals("Event doesn't refer to the correct changed entity.", objectAddress,
			        txnEvent.getChangedEntity());
			assertEquals("The actor of the event is not correct.", this.actorId,
			        txnEvent.getActor());
			assertFalse("The event is wrongly marked as implied.", txnEvent.isImplied());
			assertFalse("the event is wrongly marked as being part of a transaction.",
			        txnEvent.inTransaction());
			
			/*
			 * TODO check the events that make up the transaction!
			 */
			
			/*
			 * TODO document why there are no "removedFieldEvents" (etc.) lists
			 */
			Map<XAddress,XEvent> addedFieldEvents = new HashMap<XAddress,XEvent>();
			Map<XAddress,XEvent> addedValueEvents = new HashMap<XAddress,XEvent>();
			
			for(XEvent ev : txnEvent) {
				/*
				 * TODO Notice that this might change in the future and the
				 * randomly constructed succeeding transaction might also
				 * contain events of other types.
				 */
				assertTrue("The transaction should only contain events of the Add-type.",
				        ev.getChangeType() == ChangeType.ADD);
				assertTrue("Event is wrongly marked as not being part of a transaction.",
				        ev.inTransaction());
				assertEquals("The event doesn't refer to the correct model.", modelId, ev
				        .getTarget().getModel());
				assertEquals("The event doesn't refer to the correct object.", objectId, ev
				        .getTarget().getObject());
				assertFalse("The event is wrongly marked as being implied.", event.isImplied());
				assertEquals("The actor of the event is not correct.", this.actorId,
				        event.getActor());
				assertEquals("The event didn't refer to the correct revision number.", revNr,
				        ev.getRevisionNumber());
				
				assertEquals(
				        "A object transaction should only contain commands that target fields.", ev
				                .getChangedEntity().getAddressedType(), XType.XFIELD);
				
				if(ev instanceof XObjectEvent) {
					addedFieldEvents.put(ev.getChangedEntity(), ev);
				} else {
					assertTrue(ev instanceof XFieldEvent);
					addedValueEvents.put(ev.getChangedEntity(), ev);
				}
			}
			
			object = this.persistence.getObjectSnapshot(objectAdrRequest);
			for(XID fieldId : object) {
				XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
				
				assertTrue(
				        "Since the object was emtpy before the transaction, there should be a fitting add-event for the field with XID "
				                + fieldId + ".", addedFieldEvents.containsKey(fieldAddress));
				XReadableField field = object.getField(fieldId);
				if(field.getValue() != null) {
					assertTrue(
					        "Since the object was emtpy before the transaction, there should be a fitting add-event for the value in the field with XID "
					                + fieldId + ".", addedValueEvents.containsKey(fieldAddress));
				}
			}
		}
		
	}
	
	@Test
	public void testGetManagedModelIds() {
		
		Set<XID> managedIds = this.persistence.getManagedModelIds();
		
		assertTrue("The persistence already has some managed IDs, although no models were added. "
		        + managedIds, managedIds.isEmpty());
		
		// add a model
		XID modelId = XX.toId("testGetManagedModelsIdModel1");
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		managedIds = this.persistence.getManagedModelIds();
		
		assertTrue("The set of managed ids does not contain the XID of the newly added model.",
		        managedIds.contains(modelId));
		assertEquals(
		        "The set of managed ids contains more than one XID, although we only added one model.",
		        1, managedIds.size());
		
		// add some more models
		Set<XID> addedModelIds = new HashSet<XID>();
		addedModelIds.add(modelId);
		for(int i = 2; i < 32; i++) {
			modelId = XX.toId("testGetManagedModelsIdModel" + i);
			addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
			revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
			
			addedModelIds.add(modelId);
		}
		
		managedIds = this.persistence.getManagedModelIds();
		
		assertEquals("The amount of managed XIDs does not match the amount of added models.",
		        addedModelIds.size(), managedIds.size());
		
		for(XID id : addedModelIds) {
			assertTrue(
			        "The set of managed XIDs doesn't contain one of the XIDs of a model we've added.",
			        managedIds.contains(id));
		}
	}
	
	@Test
	public void testGetModelRevision() {
		
		/*
		 * TODO what are tentative revision numbers? These need to be tested,
		 * too.
		 */
		
		XID modelId = XX.toId("testGetModelRevisionModel1");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
		XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
		
		ModelRevision storedRevision = this.persistence.getModelRevision(modelAdrRequest);
		
		assertEquals("Revision number does not match.", model.getRevisionNumber(),
		        storedRevision.revision());
		assertTrue("The model still exists, but exists() returns false.",
		        storedRevision.modelExists());
		
		// add some objects, to increase the revision number
		int nrOfModels = 10;
		for(int i = 2; i < nrOfModels; i++) {
			XID objectId = XX.toId("testGetModelRevisionModel" + i);
			XCommand addObjectCom =
			        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
			this.persistence.executeCommand(this.actorId, addObjectCom);
		}
		
		model = this.persistence.getModelSnapshot(modelAdrRequest);
		storedRevision = this.persistence.getModelRevision(modelAdrRequest);
		
		assertEquals("Revision number does not match.", model.getRevisionNumber(),
		        storedRevision.revision());
		assertTrue("The model still exists, but exists() returns false.",
		        storedRevision.modelExists());
		
		// remove the model
		XCommand removeModelCom =
		        this.comFactory.createRemoveModelCommand(this.repoId, modelId,
		                model.getRevisionNumber(), false);
		revNr = this.persistence.executeCommand(this.actorId, removeModelCom);
		assertTrue("The model wasn't correctly removed, test cannot be executed.", revNr >= 0);
		
		storedRevision = this.persistence.getModelRevision(modelAdrRequest);
		
		/*
		 * according to the documentation of {@link ModelRevision}, a
		 * ModelRevision stores the old revision number after the model was
		 * removed.
		 */
		assertEquals("Model was removed, the stored revision should be XCommand.FAILED.", revNr,
		        storedRevision.revision());
		assertFalse("The model does no longer exist, but exists() returns true.",
		        storedRevision.modelExists());
		
		/*
		 * try to get the model revision of a not existing model
		 * 
		 * According to the documentation of {@link XydraPersistence} null is
		 * returned, if the state is not known.
		 * 
		 * TODO It is unclear what is meant with that, but I suppose
		 * "state is not known" means that no such model ever existed. ~ Kaidel
		 */
		XID modelId2 = XX.toId("testGetModelRevisionModel" + (nrOfModels + 1));
		XAddress modelAddress2 = XX.resolveModel(this.repoId, modelId2);
		GetWithAddressRequest modelAdr2Request = new GetWithAddressRequest(modelAddress2);
		
		storedRevision = this.persistence.getModelRevision(modelAdr2Request);
		
		assertEquals(
		        "Returned model revision should be ModelRevision.MODEL_DOES_NOT_EXIST_YET, since the model never existed.",
		        ModelRevision.MODEL_DOES_NOT_EXIST_YET, storedRevision);
	}
	
	@Test
	public void testGetModelSnapshot() {
		
		/*
		 * create a model and execute some commands on it, manage a separate
		 * model in parallel, execute the same commands on that one and compare
		 * the returned model to the self-managed model
		 */
		
		XID modelId = X.getIDProvider().fromString("testGetModelSnapshotModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XRepository repo = X.createMemoryRepository(this.repoId);
		XModel model = repo.createModel(modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		assertTrue("Model could not be created, test cannot be executed.", revNr >= 0);
		
		int numberOfObjects = 15;
		int numberOfObjectsWithFields = 10;
		int numberOfObjectsToRemove = 5;
		
		assert numberOfObjects >= numberOfObjectsWithFields;
		assert numberOfObjectsWithFields >= numberOfObjectsToRemove;
		
		int numberOfFields = 15;
		int numberOfFieldsWithValue = 10;
		int numberOfFieldsToRemove = 5;
		
		assert numberOfFields >= numberOfFieldsWithValue;
		assert numberOfFieldsWithValue >= numberOfFieldsToRemove;
		
		List<XID> objectIds = new ArrayList<XID>();
		for(int i = 0; i < numberOfObjects; i++) {
			XID objectId = X.getIDProvider().fromString("testGetModelSnapshotObject" + i);
			objectIds.add(objectId);
			
			model.createObject(objectId);
			
			XCommand addObjectCommand =
			        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
			revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
			assertTrue("The " + i + ". Object could not be created, test cannot be executed.",
			        revNr > 0);
		}
		
		for(int i = 0; i < numberOfObjectsWithFields; i++) {
			XID objectId = objectIds.get(i);
			XObject object = model.getObject(objectId);
			
			for(int j = 0; j < numberOfFields; j++) {
				XID fieldId = X.getIDProvider().fromString("testGetModelSnapshotField" + j);
				
				XField field = object.createField(fieldId);
				
				XCommand addFieldCommand =
				        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId,
				                fieldId, false);
				revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
				assertTrue("The " + j + ". field could not be created in the Object with id "
				        + objectId + ", test cannot be executed.", revNr > 0);
				
				if(j < numberOfFieldsWithValue) {
					XValue value =
					        X.getValueFactory().createStringValue("testGetModelSnapshotValue" + j);
					field.setValue(value);
					
					XCommand addValueCommand =
					        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId,
					                fieldId, revNr, value, false);
					revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
					assertTrue("The value for the " + j
					        + ". field could not be created in the Object with id " + objectId
					        + ", test cannot be executed.", revNr >= 0);
					
					if(j < numberOfFieldsToRemove) {
						object.removeField(fieldId);
						
						XCommand removeFieldCommand =
						        this.comFactory.createRemoveFieldCommand(this.repoId, modelId,
						                objectId, fieldId, revNr, false);
						revNr = this.persistence.executeCommand(this.actorId, removeFieldCommand);
						assertTrue("The " + j
						        + ". field could not be removed in the object with id " + objectId
						        + ", test cannot be executed.", revNr >= 0);
					}
				}
			}
			
			if(i < numberOfObjectsToRemove) {
				model.removeObject(objectId);
				
				XCommand removeObjectCommand =
				        this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId,
				                revNr, false);
				revNr = this.persistence.executeCommand(this.actorId, removeObjectCommand);
				assertTrue("The " + i + ". object could not be removed, test cannot be executed.",
				        revNr >= 0);
			}
		}
		
		// get the model snapshot
		GetWithAddressRequest modelReq = new GetWithAddressRequest(modelAddress);
		XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelReq);
		
		int objectsInManagedModel = 0;
		int objectsInSnapshot = 0;
		
		for(@SuppressWarnings("unused")
		XID objectId : model) {
			objectsInManagedModel++;
		}
		
		for(@SuppressWarnings("unused")
		XID objectId : modelSnapshot) {
			objectsInSnapshot++;
		}
		
		assertEquals("The snapshot does not have the correct amount of objects.",
		        objectsInManagedModel, objectsInSnapshot);
		
		/*
		 * compare the managed model with the snapshot. "equals" might not work
		 * here, since the returned java objects might be of different types, so
		 * we'll have to test the equality manually.
		 */
		
		assertEquals(modelId, modelSnapshot.getId());
		assertEquals(model.getRevisionNumber(), modelSnapshot.getRevisionNumber());
		
		int nrOfObjectsInManagedModel = 0;
		int nrOfObjectsInModelSnapshot = 0;
		
		for(@SuppressWarnings("unused")
		XID objectId : model) {
			nrOfObjectsInManagedModel++;
		}
		
		for(@SuppressWarnings("unused")
		XID objectId : modelSnapshot) {
			nrOfObjectsInModelSnapshot++;
		}
		
		assertEquals("The snapshot does not have the correct amount of objects.",
		        nrOfObjectsInManagedModel, nrOfObjectsInModelSnapshot);
		
		for(XID objectId : model) {
			assertTrue("Snapshot does not contain an object which it should contain.",
			        modelSnapshot.hasObject(objectId));
			
			XObject object = model.getObject(objectId);
			XWritableObject objectSnapshot = modelSnapshot.getObject(objectId);
			
			int nrOfFieldsInManagedObject = 0;
			int nrOfFieldsInObjectSnapshot = 0;
			
			for(@SuppressWarnings("unused")
			XID fieldId : object) {
				nrOfFieldsInManagedObject++;
			}
			
			for(@SuppressWarnings("unused")
			XID fieldId : objectSnapshot) {
				nrOfFieldsInObjectSnapshot++;
			}
			
			assertEquals("The object snapshot does not have the correct amount of fields.",
			        nrOfFieldsInManagedObject, nrOfFieldsInObjectSnapshot);
			
			for(XID fieldId : object) {
				assertTrue("Snapshot does not contain a field which it should contain.",
				        objectSnapshot.hasField(fieldId));
				
				XField field = object.getField(fieldId);
				XWritableField fieldSnapshot = objectSnapshot.getField(fieldId);
				
				assertEquals("The field snapshot does not contain the correct value.",
				        field.getValue(), fieldSnapshot.getValue());
			}
			
		}
		
	}
	
	@Test
	public void testGetModelSnapshotWrongAddressType() {
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		
		XAddress repoAddress = XX.resolveRepository(this.repoId);
		XAddress objectAddress = XX.resolveObject(this.repoId, objectId, fieldId);
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		
		try {
			this.persistence.getModelSnapshot(new GetWithAddressRequest(repoAddress));
			assertTrue("The method should've thrown an execption", false);
		} catch(Exception e) {
			assertTrue(true);
		}
		
		try {
			this.persistence.getModelSnapshot(new GetWithAddressRequest(objectAddress));
			assertTrue("The method should've thrown an execption", false);
		} catch(Exception e) {
			assertTrue(true);
		}
		
		try {
			this.persistence.getModelSnapshot(new GetWithAddressRequest(fieldAddress));
			assertTrue("The method should've thrown an execption", false);
		} catch(Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testGetLargeModelSnapshot() {
		/*
		 * Test that such a large snapshot at least is computed without throwing
		 * an error
		 */
		WritableRepositoryOnPersistence repo =
		        new WritableRepositoryOnPersistence(this.persistence, this.actorId);
		XID model1 = XX.toId("model1");
		XWritableModel model = repo.createModel(model1);
		for(int i = 0; i < 600; i++) {
			model.createObject(XX.toId("object" + i));
		}
		
		log.info("Getting snapshot");
		XWritableModel snapshot =
		        this.persistence.getModelSnapshot(new GetWithAddressRequest(XX.resolveModel(
		                this.repoId, model1), true));
		assertNotNull(snapshot);
	}
	
	@Test
	public void testGetObjectSnapshot() {
		
		/*
		 * create an object and execute some commands on it, manage a separate
		 * object in parallel, execute the same commands on that one and compare
		 * the returned object to the self-managed object
		 */
		
		// create appropriate objects first
		XID modelId = X.getIDProvider().fromString("testGetObjectSnapshotModel");
		
		XRepository repo = X.createMemoryRepository(this.repoId);
		XModel model = repo.createModel(modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		assertTrue("Model could not be created, test cannot be executed.", revNr >= 0);
		
		int numberOfFields = 15;
		int numberOfFieldsWithValue = 10;
		int numberOfFieldsToRemove = 5;
		
		assert numberOfFields >= numberOfFieldsWithValue;
		assert numberOfFieldsWithValue >= numberOfFieldsToRemove;
		
		XID objectId = X.getIDProvider().fromString("testGetObjectSnapshotObject");
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XObject object = model.createObject(objectId);
		
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
		assertTrue("The object could not be created, test cannot be executed.", revNr >= 0);
		
		List<XID> fieldIds = new ArrayList<XID>();
		for(int i = 0; i < numberOfFields; i++) {
			XID fieldId = X.getIDProvider().fromString("testGetObjectSnapshotField" + i);
			fieldIds.add(fieldId);
			
			XField field = object.createField(fieldId);
			
			XCommand addFieldCommand =
			        this.comFactory.createAddFieldCommand(this.repoId, modelId, objectId, fieldId,
			                false);
			revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
			assertTrue("The " + i + ". field could not be created in the Object with id "
			        + objectId + ", test cannot be executed.", revNr >= 0);
			
			if(i < numberOfFieldsWithValue) {
				XValue value =
				        X.getValueFactory().createStringValue("testGetObjectSnapshotValue" + i);
				field.setValue(value);
				
				XCommand addValueCommand =
				        this.comFactory.createAddValueCommand(this.repoId, modelId, objectId,
				                fieldId, revNr, value, false);
				revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
				assertTrue("The value for the " + i
				        + ". field could not be created in the Object with id " + objectId
				        + ", test cannot be executed.", revNr >= 0);
				
				if(i < numberOfFieldsToRemove) {
					object.removeField(fieldId);
					
					XCommand removeFieldCommand =
					        this.comFactory.createRemoveFieldCommand(this.repoId, modelId,
					                objectId, fieldId, revNr, false);
					revNr = this.persistence.executeCommand(this.actorId, removeFieldCommand);
					assertTrue("The " + i + ". field could not be removed in the object with id "
					        + objectId + ", test cannot be executed.", revNr >= 0);
				}
			}
		}
		
		// get the object snapshot
		GetWithAddressRequest objectReq = new GetWithAddressRequest(objectAddress);
		XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectReq);
		
		/*
		 * compare the managed object with the snapshot. "equals" might not work
		 * here, since the returned java objects might be of different types, so
		 * we'll have to test the equality manually.
		 */
		
		assertEquals(objectId, objectSnapshot.getId());
		assertEquals(object.getRevisionNumber(), objectSnapshot.getRevisionNumber());
		
		int nrOfFields = 0;
		int nrOfFieldsInSnapshot = 0;
		
		for(@SuppressWarnings("unused")
		XID fieldId : objectSnapshot) {
			nrOfFieldsInSnapshot++;
		}
		
		for(XID fieldId : object) {
			nrOfFields++;
			
			assertTrue("Snapshot does not contain a field which it should contain.",
			        objectSnapshot.hasField(fieldId));
			
			XField field = object.getField(fieldId);
			XWritableField fieldSnapshot = objectSnapshot.getField(fieldId);
			
			assertEquals("Field snapshot does not contain the correct value.", field.getValue(),
			        fieldSnapshot.getValue());
		}
		
		assertEquals("Object snapshot does not contain the correct amount of fields.", nrOfFields,
		        nrOfFieldsInSnapshot);
	}
	
	@Test
	public void testGetObjectSnapshotWrongAddressType() {
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		
		XAddress repoAddress = XX.resolveRepository(this.repoId);
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		
		try {
			this.persistence.getModelSnapshot(new GetWithAddressRequest(repoAddress));
			assertTrue("The method should've thrown an execption", false);
		} catch(Exception e) {
			assertTrue(true);
		}
		
		try {
			this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
			assertTrue("The method should've thrown an execption", false);
		} catch(Exception e) {
			assertTrue(true);
		}
		
		try {
			this.persistence.getModelSnapshot(new GetWithAddressRequest(fieldAddress));
			assertTrue("The method should've thrown an execption", false);
		} catch(Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testGetRepositoryId() {
		assertEquals(
		        "The repositor id of the XydraPersistence wasn't set correctly - might also cause other tests to fail.",
		        this.repoId, this.persistence.getRepositoryId());
	}
	
	// @Test
	public void testHasManagedModel() {
		XID modelId = XX.toId("testHasManagedModelModel1");
		assertFalse(
		        "hasManagedModels() returns true, although the persistence has no managed models yet.",
		        this.persistence.hasManagedModel(modelId));
		
		// add a model
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		assertTrue(
		        "hasManagedModels(modelId) returns false, although we correctly added a model with the given modelId.",
		        this.persistence.hasManagedModel(modelId));
		
		// add some more models
		List<XID> addedModelIds = new ArrayList<XID>();
		addedModelIds.add(modelId);
		for(int i = 2; i < 32; i++) {
			modelId = XX.toId("testHasManagedModelModel" + i);
			addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
			revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
			
			addedModelIds.add(modelId);
		}
		
		for(XID id : addedModelIds) {
			assertTrue(
			        "hasManagedModels(id) returns false, although we correctly added a model with the given id.",
			        this.persistence.hasManagedModel(id));
		}
		
		// delete some models and check if hasManagedModel still returns true
		
		int nrOfModels = 12;
		
		for(int i = 0; i < nrOfModels; i++) {
			XID id = addedModelIds.get(i);
			XAddress modelAddress = XX.resolveModel(this.repoId, id);
			GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
			XWritableModel model = this.persistence.getModelSnapshot(addressRequest);
			
			XCommand deleteModelCom =
			        this.comFactory.createRemoveModelCommand(this.repoId, id,
			                model.getRevisionNumber(), false);
			
			revNr = this.persistence.executeCommand(this.actorId, deleteModelCom);
			assertTrue("The model wasn't correctly removed, test cannot be executed.", revNr >= 0);
		}
		
		for(int i = 0; i < nrOfModels; i++) {
			XID id = addedModelIds.get(i);
			assertTrue(
			        "hasManagedModels(id) returns false after we removed the model with the given id, although the persistence once managed a model with this id.",
			        this.persistence.hasManagedModel(id));
		}
	}
	
	/**
	 * Create Transaction of a simple MOF structure on a ChangedModel and check
	 * revision numbers after a subsequent fieldCommand
	 * 
	 * Check in particular, if after an update of a field field.getOldFieldRev()
	 * actually returns the former field rev (currently it seems to return 0).
	 * 
	 * This is causing an error right now as seen in FieldProperty.setValue,
	 * where subsequent forced add cmd are used to set a value opposed to change
	 * cmds.
	 * 
	 * This code is a mess, only commited so Max can test this special case.
	 * 
	 * @author xamde
	 */
	@Test
	public void testExecuteCommandTransaction() {
		// constants
		XID modelId = XX.toId("testExecuteCommandTransaction");
		XID objectId = XX.toId("objectId");
		XID fieldId = XX.toId("fiedlId");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XStringValue valueFirst = X.getValueFactory().createStringValue(new String("first"));
		XStringValue valueSecond = X.getValueFactory().createStringValue(new String("second"));
		
		// add a model on which an object can be created first
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
		
		XTransactionBuilder txBuilder = new XTransactionBuilder(modelAddress);
		ChangedModel cm = new ChangedModel(model);
		XWritableObject xo = cm.createObject(objectId);
		XWritableField field = xo.createField(fieldId);
		
		field.setValue(valueFirst);
		txBuilder.applyChanges(cm);
		XTransaction txn = txBuilder.build();
		// create object and field with value as tx
		revNr = this.persistence.executeCommand(this.actorId, txn);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/* get object from persistence ; check that the field actually exists */
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		field = object.getField(fieldId);
		
		assertNotNull("The field we tried to create actually wasn't correctly added.", field);
		assertEquals("Returned field did not have the correct revision number.", revNr,
		        field.getRevisionNumber());
		
		long fieldRevNrBeforeUpdate = field.getRevisionNumber();
		
		/* overwrite field */
		XFieldCommand fieldChangeValueCommand =
		        MemoryFieldCommand.createAddCommand(field.getAddress(), XCommand.FORCED,
		                valueSecond);
		long newRevNr = this.persistence.executeCommand(this.actorId, fieldChangeValueCommand);
		assertTrue("The field value wasn't correctly added, test cannot be executed.",
		        newRevNr >= 0);
		
		List<XEvent> events = this.persistence.getEvents(modelAddress, revNr + 1, newRevNr);
		assertEquals("Got more than one event from field cmd", 1, events.size());
		XEvent event = events.get(0);
		assertTrue("event is not a FieldEvent", event instanceof XFieldEvent);
		long oldRevNr = event.getOldFieldRevision();
		
		// System.out.println("========================================");
		// List<XEvent> events1 = this.persistence.getEvents(modelAddress, 0,
		// 1000);
		// for(XEvent e : events1) {
		// System.out.println("========= " + e.getRevisionNumber() + " " + e);
		// if(e instanceof XTransactionEvent) {
		// XTransactionEvent te = (XTransactionEvent)e;
		// for(int i = 0; i < te.size(); i++) {
		// XAtomicEvent sube = te.getEvent(i);
		// System.out.println("========= " + sube.getRevisionNumber() + " --- "
		// + sube);
		// }
		// }
		// }
		
		assertEquals("Old field rev number and rev nr before field cmd did not match",
		        fieldRevNrBeforeUpdate, oldRevNr);
	}
}
