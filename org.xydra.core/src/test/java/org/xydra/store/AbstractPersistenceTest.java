package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
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
		XID modelId = XX.toId("testExecuteCommandRepositoryCommandAddType");
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
		        "Trying to add an already existing model with an unforced command succeeded (should fail), revNr was "
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
		XID modelId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeModel1");
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
		
		modelId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeModel2");
		XCommand deleteNotExistingModelCom = this.comFactory.createRemoveModelCommand(this.repoId,
		        modelId, 0, false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingModelCom);
		
		assertTrue(
		        "Removing a not existing model with an unforced command succeeded (should fail), revNr was "
		                + revNr, revNr == XCommand.FAILED);
		
		/*
		 * test if removing a model also removes the object (snapshots) and
		 * their fields
		 */
		
		modelId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeModel3");
		modelAddress = XX.resolveModel(this.repoId, modelId);
		addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		
		assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);
		
		XID objectId = XX.toId("testExecuteCommandRepositoryCommandRemoveTypeObject1");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		long objectRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("Adding an object failed, test cannot be executed.", objectRevNr >= 0);
		
		// remove model and check if object is also removed
		addressRequest = new GetWithAddressRequest(modelAddress);
		model = this.persistence.getModelSnapshot(addressRequest);
		deleteModelCom = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
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
		
		GetWithAddressRequest objectAddressRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAddressRequest);
		
		assertNull("The model was removed but its objects weren't removed.", object);
	}
	
	@Test
	public void testExecuteCommandModelCommandAddType() {
		/*
		 * ModelCommands of add type add new objects to models.
		 */
		
		// add a model on which the commands can be executed first
		
		XID modelId = XX.toId("testExecuteCommandModelCommandAddTypeModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object, should succeed
		 */
		
		XID objectId = XX.toId("testExecuteCommandModelCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
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
		 * try to add the same object again (with an unforced command), should
		 * fail
		 */
		
		long failRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue(
		        "Trying to add an already existing object  with an unforced command succeeded (should fail), revNr was "
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
		XID modelId = XX.toId("testExecuteCommandModelCommandRemoveTypeModel");
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
		
		XID objectId = XX.toId("testExecuteCommandModelCommandRemoveTypeObject1");
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
		
		objectId = XX.toId("testExecuteCommandModelCommandRemoveTypeObject2");
		XCommand deleteNotExistingObjectCom = this.comFactory.createRemoveObjectCommand(
		        this.repoId, modelId, objectId, 0, false);
		
		revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingObjectCom);
		
		assertTrue(
		        "Removing a not existing object with an unforced command succeeded (should fail), revNr was "
		                + revNr, revNr == XCommand.FAILED);
	}
	
	@Test
	public void testExecuteCommandObjectCommandAddType() {
		/*
		 * ObjectCommands of add type add new fields to objects.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandObjectCommandAddTypeModel");
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandObjectCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandObjectCommandAddTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false);
		
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
		
		long failRevNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue(
		        "Trying to add an already existing field with an unforced command succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
	}
	
	@Test
	public void testExecuteCommandObjectCommandRemoveType() {
		/*
		 * FieldCommands of remove type remove fields from objects.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandObjectCommandRemoveTypeModel");
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandObjectCommandRemoveTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandObjectCommandRemoveTypeField");
		
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * remove field, should succeed
		 */
		
		XCommand removeFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId, modelId,
		        objectId, fieldId, revNr, false);
		
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
		 * try to remove the same field again (with an unforced command), should
		 * fail (since no such field exists)
		 */
		
		removeFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId,
		        fieldId, revNr, false);
		long failRevNr = this.persistence.executeCommand(this.actorId, removeFieldCom);
		
		assertTrue(
		        "Trying to remove a not existing field with an unforced command succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
	}
	
	@Test
	public void testExecuteCommandFieldCommandAddType() {
		/*
		 * FieldCommands of add type add new value to fields.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandFieldCommandAddTypeModel");
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandAddTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandAddTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * add a new value to the field, should succeed
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
		        false);
		
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
		
		long failRevNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		assertTrue(
		        "Trying to add the same value to a field which value is already set with an unforced command succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
		
		// try to add a different value
		XValue value2 = X.getValueFactory().createIntegerValue(42);
		XCommand addValueCom2 = this.comFactory.createAddValueCommand(fieldAddress, revNr, value2,
		        false);
		
		failRevNr = this.persistence.executeCommand(this.actorId, addValueCom2);
		
		assertTrue(
		        "Trying to add a new value to a field which value is already set with an unforced command succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
		
		// check that the value wasn't changed
		object = this.persistence.getObjectSnapshot(objectAdrRequest);
		field = object.getField(fieldId);
		
		storedValue = field.getValue();
		
		assertEquals("The stored value was changed, although the add-command failed.", value,
		        storedValue);
		
	}
	
	@Test
	public void testExecuteCommandFieldCommandRemoveType() {
		/*
		 * FieldCommands of remove type remove values from fields.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandFieldCommandRemoveTypeModel");
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandRemoveTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandRemoveTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * add a new value to the field, should succeed
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
		        false);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * remove the value again, should succeed
		 */
		
		XCommand removeValueCom = this.comFactory.createRemoveValueCommand(fieldAddress, revNr,
		        false);
		
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
		 * 
		 * TODO check how forced commands work here
		 */
		
		long failRevNr = this.persistence.executeCommand(this.actorId, removeValueCom);
		
		assertTrue(
		        "Removing a value from a field with no value with an unforced command succeeded (should fail), revNr was "
		                + failRevNr, failRevNr == XCommand.FAILED);
	}
	
	@Test
	public void testExecuteCommandFieldCommandChangeType() {
		/*
		 * FieldCommands of change type change values of fields with values.
		 */
		
		// add a model on which an object can be created first
		
		XID modelId = XX.toId("testExecuteCommandFieldCommandChangeTypeModel");
		XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new object on which a new field can be created
		 */
		
		XID objectId = XX.toId("testExecuteCommandFieldCommandChangeTypeObject");
		
		XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
		XCommand addObjectCom = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
		
		assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
		
		/*
		 * add a new field, should succeed
		 */
		
		XID fieldId = XX.toId("testExecuteCommandFieldCommandChangeTypeField");
		
		XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
		XCommand addFieldCom = this.comFactory.createAddFieldCommand(this.repoId, modelId,
		        objectId, fieldId, false);
		
		revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
		
		assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * try to change a value which does not exist, should fail
		 */
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand changeValueCom = this.comFactory.createChangeValueCommand(fieldAddress, revNr,
		        value, false);
		
		revNr = this.persistence.executeCommand(this.actorId, changeValueCom);
		
		assertTrue(
		        "Executing \"Change a value\"-command succeeded on a field with no value (should fail), revNr was "
		                + revNr, revNr == XCommand.FAILED);
		
		/*
		 * add a value to the field, should succeed
		 */
		// get the correct revision number
		GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
		XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
		XWritableField field = object.getField(fieldId);
		revNr = field.getRevisionNumber();
		
		XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
		        false);
		
		revNr = this.persistence.executeCommand(this.actorId, addValueCom);
		
		assertTrue("Executing \"Adding a new value\"-command failed (should succeed), revNr was "
		        + revNr, revNr >= 0);
		
		/*
		 * change the value, should succeed
		 */
		XValue value2 = X.getValueFactory().createIntegerValue(42);
		changeValueCom = this.comFactory.createChangeValueCommand(fieldAddress, revNr, value2,
		        false);
		
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
	 * TODO don't forget to write tests for forced commands
	 */
	
	@Test
	public void testExecuteCommandSimpleTransaction() {
		// TODO write this test
		// TODO write multiple tests for Transactions, since they are pretty
		// complex
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
		 * remove the object and check if the correct events are returned. Check
		 * getEvents(fieldAddress...), getEvents(objectAddress...) and
		 * getEvents(modelAddress...) since they might behave differently.
		 * 
		 * Check that the \"field was removed\"-event is implicit.
		 */
		
		XCommand removeObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId,
		        objectId, revNr, false);
		long oldObjectRev2 = revNr;
		revNr = this.persistence.executeCommand(this.actorId, removeObjectCom);
		assertTrue("The object  wasn't correctly removed, test cannot be executed.", revNr >= 0);
		
		// get events from the model first and check them
		events = this.persistence.getEvents(modelAddress, revNr, revNr);
		
		assertEquals(
		        "List of events should contain two events (the \"object was removed\"- and the implicit \"field was removed\"-event), but actually contains zero or more than 2 events.",
		        2, events.size());
		
		XEvent fieldRemoveEvent, objectRemoveEvent;
		
		if(events.get(0) instanceof XObjectEvent) {
			fieldRemoveEvent = events.get(0);
			objectRemoveEvent = events.get(1);
		} else {
			fieldRemoveEvent = events.get(1);
			objectRemoveEvent = events.get(0);
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
		assertFalse("the event is wrongly marked as being part of a transaction.",
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
		assertFalse("the event is wrongly marked as being part of a transaction.",
		        objectRemoveEvent.inTransaction());
		
		/*
		 * TODO remove the model
		 */
	}
	
	@Test
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
	
	@Test
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
}
