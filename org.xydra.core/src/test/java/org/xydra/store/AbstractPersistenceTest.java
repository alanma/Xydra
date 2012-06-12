package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
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
		deleteModelCom = this.comFactory.createRemoveModelCommand(this.repoId, modelId, revNr,
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
		XCommand deleteNotExistingModelCom = this.comFactory.createRemoveModelCommand(this.repoId,
		        modelId, 0, forcedCommands);
		
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
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
		long objectRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("Adding an object failed, test cannot be executed.", objectRevNr >= 0);
		
		// remove model and check if object is also removed
		addressRequest = new GetWithAddressRequest(modelAddress);
		model = this.persistence.getModelSnapshot(addressRequest);
		deleteModelCom = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
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
		
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object, should succeed
		 */
		
		XID objectId = XX.toId("testExecuteCommandModelCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
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
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
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
		        objectId, object.getRevisionNumber(), forcedCommands);
		
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
		deleteObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId,
		        revNr, forcedCommands);
		
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
		XCommand deleteNotExistingObjectCom = this.comFactory.createRemoveObjectCommand(
		        this.repoId, modelId, objectId, 0, forcedCommands);
		
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandObjectCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandObjectCommandAddTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, forcedCommands);
		
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandObjectCommandRemoveTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandObjectCommandRemoveTypeField");
		
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * remove field, should succeed
		 */
		
		XCommand removeFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, forcedCommands);
		
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
		
		removeFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId,
		        fieldId, revNr, forcedCommands);
		
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
		XCommand deleteNotExistingFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId,
		        modelId, objectId, fieldId, 0, forcedCommands);
		
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandAddTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * add a new value to the field, should succeed
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
		        forcedCommands);
		
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
		XCommand addValueCom2 = this.comFactory.createAddValueCommand(fieldAddress, revNr, value2,
		        forcedCommands);
		
		revNr2 = this.persistence.executeCommand(this.actorId, addValueCom2);
		
		if(forcedCommands) {
			// TODO check if this is correct
			assertTrue(
			        "A forced add-command should succeed, even though the value is already set.",
			        revNr2 >= 0);
			
			// TODO check that the value was changed
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandRemoveTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandRemoveTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * add a new value to the field, should succeed
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
		        forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * remove the value again, should succeed
		 */
		
		XCommand removeValueCom = this.comFactory.createRemoveValueCommand(fieldAddress, revNr,
		        forcedCommands);
		
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
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        forcedCommands);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandChangeTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandChangeTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, forcedCommands);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * try to change a value which does not exist, should fail
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand changeValueCom = this.comFactory.createChangeValueCommand(fieldAddress, revNr,
		        value, forcedCommands);
		
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
		
		XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
		        forcedCommands);
		
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
		changeValueCom = this.comFactory.createChangeValueCommand(fieldAddress, revNr, value2,
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
	public void testExecuteCommandSucceedingTransaction() {
		int nrOfTxns = 20;
		
		for(int i = 0; i <= nrOfTxns; i++) {
			long seed = System.currentTimeMillis();
			
			XID modelId = X.getIDProvider().fromString(
			        "testExecuteCommandSimpleTransactionModel" + i);
			XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
			
			GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
			XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
			        false);
			// add a model on which an object can be created first
			long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			
			assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
			
			XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
			
			Pair<ChangedModel,XTransaction> pair = createRandomSucceedingModelTransaction(
			        modelSnapshot, seed);
			ChangedModel changedModel = pair.getFirst();
			XTransaction txn = pair.getSecond();
			
			revNr = this.persistence.executeCommand(this.actorId, txn);
			assertTrue("Transaction failed, should succeed, seed was: " + seed, revNr >= 0);
			
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
			        "The transaction wasn't correctly executed, the stored model does not store the correct amount of objects it should be storing after execution of the transaction.",
			        nrOfObjectsInChangedModel, nrOfObjectsInModelSnapshot);
			
			for(XID objectId : changedModel) {
				assertTrue(
				        "The stored model does not contain an object it should contain after the transaction was executed.",
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
				        "The transaction wasn't correctly executed, one of the stored objects does not store the correct amount of fields it should be storing after execution of the transaction.",
				        nrOfFieldsInChangedObject, nrOfFieldsInObjectSnapshot);
				
				for(XID fieldId : changedObject) {
					assertTrue(
					        "One of the stored objects does not contain a field it should contain after the transaction was executed.",
					        objectSnapshot.hasField(fieldId));
					
					XReadableField changedField = changedObject.getField(fieldId);
					XReadableField fieldSnapshot = objectSnapshot.getField(fieldId);
					
					assertEquals(
					        "One of the stored fields does not contain the value it should contain after the transaction was executed.",
					        changedField.getValue(), fieldSnapshot.getValue());
				}
			}
		}
	}
	
	/**
	 * Pseudorandomly generates a transaction which should succeed on the given
	 * model. The seed determines how the random number generator generates its
	 * pseudorandom output. Using the same seed twice will result in
	 * deterministically generating the same transaction again, which can be
	 * useful when executing a transaction in a test fails and the failed
	 * transaction needs to be reconstructed.
	 */
	private Pair<ChangedModel,XTransaction> createRandomSucceedingModelTransaction(
	        XWritableModel model, long seed) {
		Random rand = new Random(seed);
		
		XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());
		ChangedModel changedModel = new ChangedModel(model);
		
		// create random amount of objects
		int nrOfObjects = 0;
		
		do {
			nrOfObjects = rand.nextInt(50);
		} while(nrOfObjects <= 0); // add at least one object
		
		for(int i = 0; i < nrOfObjects; i++) {
			XID objectId = X.getIDProvider().fromString("randomObject" + i);
			
			changedModel.createObject(objectId);
		}
		
		// add fields and values to the object
		for(XID objectId : changedModel) {
			XWritableObject object = changedModel.getObject(objectId);
			
			int nrOfFields = rand.nextInt(50);
			for(int i = 0; i < nrOfFields; i++) {
				XID fieldId = X.getIDProvider().fromString(objectId + "randomField" + i);
				
				XWritableField field = object.createField(fieldId);
				
				boolean hasValue = rand.nextBoolean();
				
				if(hasValue) {
					/*
					 * TODO add different types of values
					 */
					XValue value = X.getValueFactory().createStringValue("randomValue" + fieldId);
					
					field.setValue(value);
				}
			}
			
		}
		
		txBuilder.applyChanges(changedModel);
		XTransaction txn = txBuilder.build();
		
		return new Pair<ChangedModel,XTransaction>(changedModel, txn);
	}
	
	// @Test (TODO test not yet ready)
	public void testExecuteCommandFailingTransaction() {
		// TODO write this test
		// TODO write multiple tests for Transactions, since they are pretty
		// complex
		
		int nrOfTxns = 20;
		
		for(int i = 0; i <= nrOfTxns; i++) {
			long seed = System.currentTimeMillis();
			
			XID modelId = X.getIDProvider().fromString(
			        "testExecuteCommandSimpleTransactionModel" + i);
			XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
			
			GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
			XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
			        false);
			// add a model on which an object can be created first
			long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
			
			assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
			
			XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
			
			Pair<ChangedModel,XTransaction> pair = createRandomFailingModelTransaction(
			        modelSnapshot, seed);
			// ChangedModel changedModel = pair.getFirst();
			XTransaction txn = pair.getSecond();
			
			revNr = this.persistence.executeCommand(this.actorId, txn);
			assertEquals("Transaction succeed, should fail, seed was: " + seed, XCommand.FAILED,
			        revNr);
		}
	}
	
	/*
	 * TODO write a method which randomly creates Transactions that should fail
	 * and assert that they fail!
	 */
	
	private Pair<ChangedModel,XTransaction> createRandomFailingModelTransaction(
	        XWritableModel model, long seed) {
		Random rand = new Random(seed);
		
		XTransactionBuilder txBuilder = new XTransactionBuilder(model.getAddress());
		ChangedModel changedModel = new ChangedModel(model);
		
		// create random amount of objects
		int nrOfObjects = 0;
		
		do {
			nrOfObjects = rand.nextInt(50);
		} while(nrOfObjects <= 0); // add at least one object
		
		boolean objectCommandFail = rand.nextBoolean();
		int faultyObjectCommand = -1;
		
		if(objectCommandFail) {
			faultyObjectCommand = rand.nextInt();
		}
		
		/*
		 * TODO ChangedModel might not be able to create faulty transactions -
		 * check this and write test accordingly
		 */
		
		for(int i = 0; i < nrOfObjects; i++) {
			XID objectId = X.getIDProvider().fromString("randomObject" + i);
			
			if(i == faultyObjectCommand) {
				changedModel.removeObject(objectId);
			} else {
				changedModel.createObject(objectId);
			}
			
		}
		
		// add fields and values to the object
		for(XID objectId : changedModel) {
			XWritableObject object = changedModel.getObject(objectId);
			
			int nrOfFields = rand.nextInt(50);
			for(int i = 0; i < nrOfFields; i++) {
				XID fieldId = X.getIDProvider().fromString(objectId + "randomField" + i);
				
				XWritableField field = object.createField(fieldId);
				
				boolean hasValue = rand.nextBoolean();
				
				if(hasValue) {
					/*
					 * TODO add different types of values
					 */
					XValue value = X.getValueFactory().createStringValue("randomValue" + fieldId);
					
					field.setValue(value);
				}
			}
		}
		
		txBuilder.applyChanges(changedModel);
		XTransaction txn = txBuilder.build();
		return new Pair<ChangedModel,XTransaction>(changedModel, txn);
	}
	
	/*
	 * TODO also test object transactions
	 */
	
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
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
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
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false);
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
		XCommand addValueCom = this.comFactory.createAddValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, value, false);
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
		XCommand changeValueCom = this.comFactory.createChangeValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, value2, false);
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
		
		XCommand removeValueCom = this.comFactory.createRemoveValueCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, false);
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
		
		XCommand removeObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId,
		        objectId, revNr, false);
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
		 * TODO remove the model
		 */
	}
	
	// @Test (TODO test not yet ready)
	public void testGetEventsTransactions() {
		/*
		 * TODO write this test
		 */
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
		 * TODO what are tentative revision numbers?
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
		int nrOfModels = 50;
		for(int i = 2; i < nrOfModels; i++) {
			XID objectId = XX.toId("testGetModelRevisionModel" + i);
			XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
			        objectId, false);
			this.persistence.executeCommand(this.actorId, addObjectCom);
		}
		
		model = this.persistence.getModelSnapshot(modelAdrRequest);
		storedRevision = this.persistence.getModelRevision(modelAdrRequest);
		
		assertEquals("Revision number does not match.", model.getRevisionNumber(),
		        storedRevision.revision());
		assertTrue("The model still exists, but exists() returns false.",
		        storedRevision.modelExists());
		
		// remove the model
		XCommand removeModelCom = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
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
		
		XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		assertTrue("Model could not be created, test cannot be executed.", revNr >= 0);
		
		int numberOfObjects = 15;
		int numberOfObjectsWithFields = 10;
		
		assert numberOfObjects >= numberOfObjectsWithFields;
		
		int numberOfFields = 5;
		
		List<XID> objectIds = new ArrayList<XID>();
		for(int i = 0; i < numberOfObjects; i++) {
			XID objectId = X.getIDProvider().fromString("testGetModelSnapshotObject" + i);
			objectIds.add(objectId);
			
			model.createObject(objectId);
			
			XCommand addObjectCommand = this.comFactory.createAddObjectCommand(this.repoId,
			        modelId, objectId, false);
			revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
			assertTrue("The " + i + ". Object could not be created, test cannot be executed.",
			        revNr > 0);
		}
		
		List<XID> fieldIds = new ArrayList<XID>();
		for(int i = 0; i < numberOfFields; i++) {
			fieldIds.add(X.getIDProvider().fromString("testGetModelSnapshotField" + i));
		}
		
		for(int i = 0; i < numberOfObjectsWithFields; i++) {
			XID objectId = objectIds.get(i);
			XObject object = model.getObject(objectId);
			
			for(XID fieldId : fieldIds) {
				object.createField(fieldId);
				
				XCommand addFieldCommand = this.comFactory.createAddFieldCommand(this.repoId,
				        modelId, objectId, fieldId, false);
				revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
				assertTrue("The " + i + ". field could not be created in the Object with id "
				        + objectId + ", test cannot be executed.", revNr > 0);
			}
		}
		
		/*
		 * TODO also add values and remove some things
		 */
		
		// get the model snapshot
		GetWithAddressRequest modelReq = new GetWithAddressRequest(modelAddress);
		XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelReq);
		
		int objectsInSnapshot = 0;
		
		for(XID objectId : modelSnapshot) {
			assert objectId != null;
			objectsInSnapshot++;
		}
		
		assertEquals("The snapshot does not have the correct amount of objects.", numberOfObjects,
		        objectsInSnapshot);
		
		/*
		 * compare the managed model with the snapshot. "equals" might not work
		 * here, since the returned java objects might be of different types, so
		 * we'll have to test the equality manually.
		 */
		
		assertEquals(modelId, modelSnapshot.getId());
		assertEquals(model.getRevisionNumber(), modelSnapshot.getRevisionNumber());
		
		int objectSnapshotsWithoutFields = 0;
		
		for(XID objectId : model) {
			assertTrue("Snapshot does not contain an object which it should contain.",
			        modelSnapshot.hasObject(objectId));
			
			XObject object = model.getObject(objectId);
			XWritableObject objectSnapshot = modelSnapshot.getObject(objectId);
			
			int fieldsInSnapshot = 0;
			
			for(XID fieldId : objectSnapshot) {
				assert fieldId != null;
				fieldsInSnapshot++;
			}
			
			if(fieldsInSnapshot != 0) {
				assertEquals("The snapshot does not have the correct amount of fields.",
				        numberOfFields, fieldsInSnapshot);
				
				for(XID fieldId : object) {
					assertTrue("Snapshot does not contain a field which it should contain.",
					        objectSnapshot.hasField(fieldId));
				}
			} else {
				objectSnapshotsWithoutFields++;
			}
			
		}
		
		assertEquals("The snapshot does not have the correct amount of objects without fields.",
		        numberOfObjects - numberOfObjectsWithFields, objectSnapshotsWithoutFields);
		
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
	public void testGetLargeModelSnapshot() {
		/*
		 * Test that such a large snapshot at least is computed without throwing
		 * an error
		 */
		WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(
		        this.persistence, this.actorId);
		XID model1 = XX.toId("model1");
		XWritableModel model = repo.createModel(model1);
		for(int i = 0; i < 600; i++) {
			model.createObject(XX.toId("object" + i));
		}
		
		log.info("Getting snapshot");
		XWritableModel snapshot = this.persistence.getModelSnapshot(new GetWithAddressRequest(XX
		        .resolveModel(this.repoId, model1), true));
		assertNotNull(snapshot);
	}
	
	// @Test (TODO test not yet ready)
	public void testGetObjectSnapshot() {
		/*
		 * TODO write this test - just like testgetModelSnapshot, but with
		 * Objects
		 */
	}
	
	@Test
	public void testGetRepositoryId() {
		assertEquals(
		        "The repositor id of the XydraPersistence wasn't set correctly - might also cause other tests to fail.",
		        this.repoId, this.persistence.getRepositoryId());
	}
	
	@Test
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
			
			XCommand deleteModelCom = this.comFactory.createRemoveModelCommand(this.repoId, id,
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
		XFieldCommand fieldChangeValueCommand = MemoryFieldCommand.createAddCommand(
		        field.getAddress(), XCommand.FORCED, valueSecond);
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
