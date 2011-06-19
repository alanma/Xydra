package org.xydra.core.change;

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
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryRepository;


/*
 * TODO Add tests for all cases in which executing commands and transactions
 * would fail - some important cases are not covered at the moment
 */

public class TransactionModelTest {
	private TransactionModel transModel;
	private MemoryModel model;
	private XObject object;
	private XWritableField field, fieldWithValue;
	
	{
		LoggerTestHelper.init();
	}
	
	@Before
	public void setup() {
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		XID fieldWithValueId = XX.createUniqueId();
		
		MemoryRepository repo = (MemoryRepository)X.createMemoryRepository(XX.toId("testActor"));
		this.model = repo.createModel(modelId);
		this.object = this.model.createObject(objectId);
		
		// add two fields
		this.field = this.object.createField(fieldId);
		this.fieldWithValue = this.object.createField(fieldWithValueId);
		
		// set its value
		XValue value = X.getValueFactory().createStringValue("test value");
		this.fieldWithValue.setValue(value);
		
		this.transModel = new TransactionModel(this.model);
	}
	
	// Tests for commit() {
	
	/*
	 * for commit():
	 * 
	 * TODO check cases were something that already existed gets deleted and
	 * something new with the same address gets added (see if the old thing is
	 * actually replaced by something new)
	 * 
	 * TODO maybe check forced commands in transactions
	 */

	@Test
	public void testCommitSingleModelCommands() {
		
		// add a new XObject
		XID objectId = X.getIDProvider().createUniqueId();
		assertFalse(this.transModel.hasObject(objectId) || this.model.hasObject(objectId));
		
		this.transModel.createObject(objectId);
		assertEquals(1, this.transModel.size());
		
		assertTrue(this.transModel.isChanged());
		
		long oldRevNr = this.model.getRevisionNumber();
		long revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the object was added to the real model
		assertTrue(this.model.hasObject(objectId));
		
		// remove an XObject
		
		this.transModel.removeObject(objectId);
		assertEquals(1, this.transModel.size());
		
		assertTrue(this.transModel.isChanged());
		
		oldRevNr = this.model.getRevisionNumber();
		revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the object was removed from the real model
		assertFalse(this.model.hasObject(objectId));
	}
	
	@Test
	public void testCommitSingleObjectCommands() {
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		// add a new XField
		XID fieldId = X.getIDProvider().createUniqueId();
		assertFalse(transObject.hasField(fieldId) || this.object.hasField(fieldId));
		
		transObject.createField(fieldId);
		assertEquals(1, this.transModel.size());
		
		assertTrue(this.transModel.isChanged());
		
		long oldRevNr = this.model.getRevisionNumber();
		long revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the field was added to the real object
		assertTrue(this.object.hasField(fieldId));
		
		// remove an XField
		transObject = (InModelTransactionObject)this.transModel.getObject(this.object.getID());
		transObject.removeField(fieldId);
		assertEquals(1, this.transModel.size());
		
		assertTrue(this.transModel.isChanged());
		
		oldRevNr = this.model.getRevisionNumber();
		revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the field was removed from the real object
		assertFalse(this.object.hasField(fieldId));
	}
	
	@Test
	public void testSingeFieldCommands() {
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		// add a value
		transObject = (InModelTransactionObject)this.transModel.getObject(this.object.getID());
		XValue value = X.getValueFactory().createStringValue("testValue");
		XWritableField field = transObject.getField(this.field.getID());
		
		field.setValue(value);
		
		assertTrue(this.transModel.isChanged());
		
		long oldRevNr = this.model.getRevisionNumber();
		long revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the value was added to the field
		assertEquals(value, this.field.getValue());
		
		// change a value
		transObject = (InModelTransactionObject)this.transModel.getObject(this.object.getID());
		value = X.getValueFactory().createStringValue("testValue2");
		field = transObject.getField(this.field.getID());
		
		field.setValue(value);
		
		assertTrue(this.transModel.isChanged());
		
		oldRevNr = this.model.getRevisionNumber();
		revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the value of the field was changed
		assertEquals(value, this.field.getValue());
		
		// remove a value
		transObject = (InModelTransactionObject)this.transModel.getObject(this.object.getID());
		field = transObject.getField(this.field.getID());
		
		field.setValue(null);
		
		assertTrue(this.transModel.isChanged());
		
		oldRevNr = this.model.getRevisionNumber();
		revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the value was removed from the field
		assertEquals(null, this.field.getValue());
	}
	
	@Test
	public void testCommitTransaction() {
		// add some objects
		XID objectId1 = X.getIDProvider().createUniqueId();
		XID objectId2 = X.getIDProvider().createUniqueId();
		
		this.transModel.createObject(objectId1);
		this.transModel.createObject(objectId2);
		
		// remove an object
		
		this.transModel.removeObject(objectId2);
		
		// add some fields
		XID fieldId1 = X.getIDProvider().createUniqueId();
		XID fieldId2 = X.getIDProvider().createUniqueId();
		XID fieldId3 = X.getIDProvider().createUniqueId();
		
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		transObject.createField(fieldId1);
		transObject.createField(fieldId2);
		transObject.createField(fieldId3);
		
		// remove some fields
		transObject.removeField(this.field.getID());
		transObject.removeField(fieldId3);
		
		// add some values
		XValue value = X.getValueFactory().createStringValue("testValue");
		XWritableField field1 = transObject.getField(fieldId1);
		XWritableField field2 = transObject.getField(fieldId2);
		
		field1.setValue(value);
		field2.setValue(value);
		
		// remove a value
		field2.setValue(null);
		
		// change a value
		XWritableField temp = transObject.getField(this.fieldWithValue.getID());
		temp.setValue(value);
		
		// commit the transaction
		assertTrue(this.transModel.isChanged());
		
		long oldRevNr = this.model.getRevisionNumber();
		long revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the changes were actually executed
		assertTrue(this.model.hasObject(objectId1));
		
		assertFalse(this.model.hasObject(objectId2));
		
		assertTrue(this.object.hasField(fieldId1));
		assertTrue(this.object.hasField(fieldId2));
		
		assertFalse(this.object.hasField(this.field.getID()));
		assertFalse(this.object.hasField(fieldId3));
		
		field1 = this.object.getField(fieldId1);
		assertEquals(value, field1.getValue());
		
		field2 = this.object.getField(fieldId2);
		assertEquals(null, field2.getValue());
		
		assertEquals(value, this.fieldWithValue.getValue());
		
		/*
		 * execute another transaction to test whether cleared
		 * TransactionObjects can be reused
		 */
		this.transModel.removeObject(objectId1);
		
		transObject = (InModelTransactionObject)this.transModel.getObject(this.object.getID());
		transObject.createField(fieldId3);
		XWritableField field3 = transObject.getField(fieldId3);
		
		field3.setValue(value);
		
		temp = transObject.getField(this.fieldWithValue.getID());
		temp.setValue(null);
		
		transObject.removeField(fieldId1);
		
		// commit the transaction
		assertTrue(this.transModel.isChanged());
		
		oldRevNr = this.model.getRevisionNumber();
		revNr = this.transModel.commit();
		
		assertFalse(this.transModel.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transModel.size());
		assertEquals(revNr, this.transModel.getRevisionNumber());
		
		// check that the changes were actually executed
		assertFalse(this.model.hasObject(objectId1));
		
		assertTrue(this.object.hasField(fieldId3));
		
		assertFalse(this.object.hasField(fieldId1));
		
		field3 = this.object.getField(fieldId3);
		assertNotNull(field3);
		assertEquals(value, field3.getValue());
		
		assertEquals(null, this.fieldWithValue.getValue());
	}
	
	// Tests for getAddress()
	@Test
	public void testGetAddress() {
		assertEquals(this.model.getAddress(), this.transModel.getAddress());
	}
	
	// Tests for getId()
	@Test
	public void testGetId() {
		assertEquals(this.model.getID(), this.transModel.getID());
	}
	
	/*
	 * Tests for executeCommand(XCommand command, XLocalCallback callback)
	 * 
	 * Note: the other executeCommand without the callback-parameter needs no
	 * extra tests, since the only thing it does is call this method
	 */

	// Tests for model commands
	
	public void executeCommandsAddObjectCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newObjectId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no object with this ID
		assertFalse(this.transModel.hasObject(newObjectId));
		
		// add the object
		XCommand addCommand = factory.createAddObjectCommand(this.transModel.getAddress(),
		        newObjectId, forced);
		
		long result = this.transModel.executeCommand(addCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the object was added correctly
		assertTrue(this.transModel.hasObject(newObjectId));
		
		XWritableObject object = this.transModel.getObject(newObjectId);
		assertTrue(object.getRevisionNumber() >= 0);
		
		assertFalse(this.model.hasObject(newObjectId));
		
		// try to add an object that already exists
		addCommand = factory.createAddObjectCommand(this.transModel.getAddress(),
		        this.object.getID(), forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(addCommand, callback);
		if(forced) {
			// forced -> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			// not forced -> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeAddObjectCommands() {
		executeCommandsAddObjectCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedAddObjectCommands() {
		executeCommandsAddObjectCommands(true);
	}
	
	public void executeCommandsRemoveObjectCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newObjectId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertFalse(this.transModel.hasObject(newObjectId));
		
		// try to remove not existing object
		XAddress temp = this.transModel.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), newObjectId, null);
		
		XCommand removeCommand = factory.createRemoveObjectCommand(address, 0, forced);
		
		long result = this.transModel.executeCommand(removeCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// try to remove an object that already exists, should succeed
		removeCommand = factory.createRemoveObjectCommand(this.object.getAddress(),
		        this.object.getRevisionNumber(), forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(removeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the object was removed correctly
		assertFalse(this.transModel.hasObject(this.object.getID()));
		assertTrue(this.model.hasObject(this.object.getID()));
		
		/*
		 * object was removed, add it again (otherwise the next case doesn't
		 * test what it's supposed to test
		 */
		this.transModel.createObject(this.object.getID());
		
		/*
		 * try to remove an object that already exists & use wrong revNr (use
		 * revision number of the old object, not the new one)
		 */
		removeCommand = factory.createRemoveObjectCommand(this.object.getAddress(),
		        this.object.getRevisionNumber() + 1, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(removeCommand, callback);
		
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the object was removed correctly
			assertFalse(this.transModel.hasObject(this.object.getID()));
			assertTrue(this.model.hasObject(this.object.getID()));
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
	}
	
	@Test
	public void testExecuteCommandsSafeRemoveObjectCommands() {
		executeCommandsRemoveObjectCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedRemoveObjectCommands() {
		executeCommandsRemoveObjectCommands(true);
	}
	
	// tests for object commands
	
	public void executeCommandsAddFieldCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		// make sure there is no field with this ID
		assert !transObject.hasField(newFieldId);
		
		// add the field
		XCommand addCommand = factory.createAddFieldCommand(transObject.getAddress(), newFieldId,
		        forced);
		
		long result = this.transModel.executeCommand(addCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was added correctly
		assertTrue(transObject.hasField(newFieldId));
		
		XWritableField field = transObject.getField(newFieldId);
		assertTrue(field.getRevisionNumber() >= 0);
		
		assertFalse(this.object.hasField(newFieldId));
		
		// try to add a field that already exists
		addCommand = factory.createAddFieldCommand(transObject.getAddress(), this.field.getID(),
		        forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(addCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeAddFieldCommands() {
		executeCommandsAddFieldCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedAddFieldCommands() {
		executeCommandsAddFieldCommands(true);
	}
	
	public void executeCommandsRemoveFieldCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		// make sure there is no field with this ID
		assert !transObject.hasField(newFieldId);
		
		// try to remove not existing field
		XAddress temp = transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		
		XCommand removeCommand = factory.createRemoveFieldCommand(address, 0, forced);
		
		long result = this.transModel.executeCommand(removeCommand, callback);
		if(forced) {
			// forced --> should suceed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// try to remove a field that already exists, should succeed
		removeCommand = factory.createRemoveFieldCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(removeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was removed correctly
		assertFalse(transObject.hasField(this.field.getID()));
		assertTrue(this.object.hasField(this.field.getID()));
		
		/*
		 * field was removed, add it again (otherwise the next case doesn't test
		 * what it's supposed to test
		 */
		assertNotNull(transObject.createField(this.field.getID()));
		
		/*
		 * try to remove a field that already exists & use wrong revNr (using
		 * the revision number of the old field which was removed, not the one
		 * of the current field)
		 */

		removeCommand = factory.createRemoveFieldCommand(this.field.getAddress(),
		        this.field.getRevisionNumber() + 1, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(removeCommand, callback);
		if(forced) {
			// forced --> should suceed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the field was removed correctly
			assertFalse(transObject.hasField(this.field.getID()));
			assertTrue(this.object.hasField(this.field.getID()));
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeRemoveFieldCommands() {
		executeCommandsRemoveFieldCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedRemoveFieldCommands() {
		executeCommandsRemoveFieldCommands(true);
	}
	
	// Tests for field commands
	
	public void executeCommandsAddValueCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// add a value to a not existing field, should fail
		assert !transObject.hasField(newFieldId);
		
		XAddress temp = transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand addCommand = factory.createAddValueCommand(address, 0, value, forced);
		
		long result = this.transModel.executeCommand(addCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// add a value to an existing field, use wrong revNr
		addCommand = factory.createAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber() + 1, value, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(addCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			XWritableField changedField = transObject.getField(this.field.getID());
			
			assertEquals(value, changedField.getValue());
			assertFalse(value.equals(this.field.getValue()));
			
			/*
			 * value was added, remove it again or the next cases won't test
			 * what they're suppossed to test
			 */
			changedField.setValue(null);
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// add a value to an existing field, should succeed
		addCommand = factory.createAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(addCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = transObject.getField(this.field.getID());
		
		assertEquals(value, changedField.getValue());
		assertFalse(value.equals(this.field.getValue()));
		
		// try to add a value to a field which value is already set
		XValue value2 = X.getValueFactory().createStringValue("test2");
		addCommand = factory.createAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value2, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(addCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			changedField = transObject.getField(this.field.getID());
			
			assertEquals(value2, changedField.getValue());
			assertFalse(value2.equals(this.field.getValue()));
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeAddValueCommands() {
		executeCommandsAddValueCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedAddValueCommands() {
		executeCommandsAddValueCommands(true);
	}
	
	public void executeCommandsChangeValueCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// change a value of a not existing field, should fail
		assert !transObject.hasField(newFieldId);
		
		XAddress temp = transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand changeCommand = factory.createChangeValueCommand(address, 0, value, forced);
		
		long result = this.transModel.executeCommand(changeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// change the value of a field, which value is not set
		changeCommand = factory.createChangeValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(changeCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check the changes
			XWritableField simulatedField = transObject.getField(this.field.getID());
			assertEquals(value, simulatedField.getValue());
			assertNull(this.field.getValue());
			
		} else {
			// not forced --> should fail
			
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
			
			// check that nothing was changed
			XWritableField simulatedField = transObject.getField(this.field.getID());
			assertNull(simulatedField.getValue());
			assertNull(this.field.getValue());
		}
		
		// change the value of a field, which value is already set, but use
		// wrong revNr
		changeCommand = factory.createChangeValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber() + 1, value, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(changeCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			XWritableField changedField = transObject.getField(this.fieldWithValue.getID());
			
			assertEquals(value, changedField.getValue());
			assertFalse(value.equals(this.fieldWithValue.getValue()));
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
			
			// check that nothing was changed
			XWritableField changedField = transObject.getField(this.fieldWithValue.getID());
			
			assertFalse(value.equals(changedField.getValue()));
			assertFalse(value.equals(this.fieldWithValue.getValue()));
		}
		
		// change the value of a field, which value is already set - should
		// succeed
		XValue value2 = X.getValueFactory().createStringValue("test2");
		changeCommand = factory.createChangeValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber(), value2, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(changeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = transObject.getField(this.fieldWithValue.getID());
		changedField = transObject.getField(this.fieldWithValue.getID());
		
		assertEquals(value2, changedField.getValue());
		assertFalse(value2.equals(this.fieldWithValue.getValue()));
	}
	
	@Test
	public void testExecuteCommandsSafeChangeValueCommands() {
		executeCommandsChangeValueCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedChangeValueCommands() {
		executeCommandsChangeValueCommands(true);
	}
	
	public void executeCommandsRemoveValueCommands(boolean forced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		// remove a value from a not existing field, should fail
		assert !transObject.hasField(newFieldId);
		
		XAddress temp = transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand removeCommand = factory.createRemoveValueCommand(address, 0, forced);
		
		long result = this.transModel.executeCommand(removeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// remove a value from an existing field, without a set value
		removeCommand = factory.createRemoveValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), forced);
		callback = new TestCallback();
		result = this.transModel.executeCommand(removeCommand, callback);
		
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// remove a value from an existing field with set value - should succeed
		removeCommand = factory.createSafeRemoveValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber());
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(removeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = transObject.getField(this.fieldWithValue.getID());
		
		assertNull(changedField.getValue());
		assertNotNull(this.fieldWithValue.getValue());
		
		/*
		 * value was removed, add a new one or the next case won't test what
		 * it's suppossed to test
		 */
		changedField.setValue(X.getValueFactory().createStringValue("test"));
		
		// remove a value from an existing field with set value, use wrong revNr
		removeCommand = factory.createRemoveValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber() + 1, forced);
		callback = new TestCallback();
		
		result = this.transModel.executeCommand(removeCommand, callback);
		if(forced) {
			// forced --> should succeed
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			changedField = transObject.getField(this.fieldWithValue.getID());
			
			assertNull(changedField.getValue());
			assertNotNull(this.fieldWithValue.getValue());
		} else {
			// not forced --> should fail
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeRemoveValueCommands() {
		executeCommandsRemoveValueCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedRemoveValueCommands() {
		executeCommandsRemoveValueCommands(true);
	}
	
	// Tests for getRevisionNumber
	@Test
	public void testGetRevisionNumber() {
		assertEquals(this.model.getRevisionNumber(), this.transModel.getRevisionNumber());
	}
	
	// Tests for isEmpty
	@Test
	public void testIsEmpty() {
		// At first, the value should be the same as that of object.isEmpty()
		assertEquals(this.model.isEmpty(), this.transModel.isEmpty());
		
		// remove object
		assertTrue(this.transModel.removeObject(this.object.getID()));
		
		assertTrue(this.transModel.isEmpty());
		assertFalse(this.model.isEmpty());
		
		// add a new field, remove it, check again
		XID newObjectId = X.getIDProvider().createUniqueId();
		this.transModel.createObject(newObjectId);
		
		assertTrue(this.transModel.hasObject(newObjectId));
		assertFalse(this.model.hasObject(newObjectId));
		
		this.transModel.removeObject(newObjectId);
		assertTrue(this.transModel.isEmpty());
		assertFalse(this.model.isEmpty());
	}
	
	// Tests for hasObject()
	@Test
	public void testHasObject() {
		assertTrue(this.transModel.hasObject(this.object.getID()));
		
		// add an object
		XID newObjectId = XX.createUniqueId();
		assertFalse(this.transModel.hasObject(newObjectId));
		
		this.transModel.createObject(newObjectId);
		assertTrue(this.transModel.hasObject(newObjectId));
		
		// remove an object
		this.transModel.removeObject(newObjectId);
		assertFalse(this.transModel.hasObject(newObjectId));
		
		// do the same with commands
		XCommand addCommand = X.getCommandFactory().createSafeAddObjectCommand(
		        this.transModel.getAddress(), newObjectId);
		this.transModel.executeCommand(addCommand);
		assertTrue(this.transModel.hasObject(newObjectId));
		
		XAddress temp = this.model.getAddress();
		XAddress objectAddress = XX.toAddress(temp.getRepository(), temp.getModel(), newObjectId,
		        null);
		XCommand removeCommand = X.getCommandFactory().createSafeRemoveObjectCommand(objectAddress,
		        XCommand.NEW);
		this.transModel.executeCommand(removeCommand);
		assertFalse(this.transModel.hasObject(newObjectId));
	}
	
	// Tests for createObject()
	@Test
	public void testCreateObject() {
		XID objectId = XX.createUniqueId();
		XWritableObject object = this.transModel.createObject(objectId);
		
		assertNotNull(object);
		
		// make sure it exists in the transObject but not in object
		assertFalse(this.model.hasObject(objectId));
		assertTrue(this.transModel.hasObject(objectId));
		
		// try to add the same object again
		XWritableObject object2 = this.transModel.createObject(objectId);
		assertEquals(object, object2);
		
		// make sure it exists in the transModel but not in model
		assertFalse(this.model.hasObject(objectId));
		assertTrue(this.transModel.hasObject(objectId));
	}
	
	// Tests for removeObject()
	@Test
	public void testRemoveField() {
		// try to remove a not existing object
		XID objectId = XX.createUniqueId();
		
		assertFalse(this.transModel.removeObject(objectId));
		
		// try to remove an existing object
		boolean removed = this.transModel.removeObject(this.object.getID());
		assertTrue(removed);
		assertFalse(this.transModel.hasObject(this.object.getID()));
		
		// make sure it wasn't removed from the underlying model
		assertTrue(this.model.hasObject(this.object.getID()));
		
		// add an object an remove it again
		this.transModel.createObject(objectId);
		assertTrue(this.transModel.hasObject(objectId));
		
		assertTrue(this.transModel.removeObject(objectId));
		assertFalse(this.transModel.hasObject(objectId));
		assertFalse(this.model.hasObject(objectId));
	}
	
	// Tests for getObject()
	@Test
	public void testGetObject() {
		// try to get an already existing object
		XWritableObject object2 = this.transModel.getObject(this.object.getID());
		
		assertEquals(this.object, object2);
		
		// try to get a not existing object
		assertNull(this.transModel.getObject(X.getIDProvider().createUniqueId()));
		
		// change the existing object and get it again
		XCommandFactory factory = X.getCommandFactory();
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand command = factory.createSafeAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value);
		
		this.transModel.executeCommand(command);
		
		object2 = this.transModel.getObject(this.object.getID());
		
		// revision numbers are not increased/managed by the TransactionModel,
		// therefore this should succeed
		assertTrue(this.object.equals(object2));
	}
	
	/*
	 * Tests for the methods of {@link InModelTransactionObject}
	 */

	@Test
	public void testInModelTransactionObject() {
		XWritableObject temp = this.transModel.getObject(this.object.getID());
		assertTrue(temp instanceof InModelTransactionObject);
		
		InModelTransactionObject transObject = (InModelTransactionObject)temp;
		
		assertEquals(this.object.getAddress(), transObject.getAddress());
		assertEquals(this.object.getID(), transObject.getID());
		assertEquals(this.object.getRevisionNumber(), transObject.getRevisionNumber());
		assertEquals(this.object.isEmpty(), transObject.isEmpty());
		assertEquals(this.object, transObject);
		
		assertTrue(transObject.hasField(this.field.getID()));
	}
	
	@Test
	public void testInModelTransactionObjectAddField() {
		XWritableObject temp = this.transModel.getObject(this.object.getID());
		assertTrue(temp instanceof InModelTransactionObject);
		
		InModelTransactionObject transObject = (InModelTransactionObject)temp;
		
		XID fieldId = X.getIDProvider().createUniqueId();
		
		XWritableField field = transObject.createField(fieldId);
		assertNotNull(field);
		assertTrue(field instanceof InModelTransactionField);
		
		// make sure it exists in the transObject but not in object
		assertFalse(this.object.hasField(fieldId));
		assertTrue(transObject.hasField(fieldId));
		
		// try to add the same field again
		XWritableField field2 = transObject.createField(fieldId);
		assertEquals(field, field2);
		
		// make sure it exists in the transObject but not in object
		assertFalse(this.object.hasField(fieldId));
		assertTrue(transObject.hasField(fieldId));
	}
	
	@Test
	public void testInModelTransactionObjectRemoveField() {
		XWritableObject temp = this.transModel.getObject(this.object.getID());
		assertTrue(temp instanceof InModelTransactionObject);
		
		InModelTransactionObject transObject = (InModelTransactionObject)temp;
		
		// try to remove a not existing field
		XID fieldId = XX.createUniqueId();
		
		assertFalse(transObject.removeField(fieldId));
		
		// try to remove an existing field
		assertTrue(transObject.removeField(this.field.getID()));
		assertFalse(transObject.hasField(this.field.getID()));
		
		// make sure it wasn't removed from the underlying object
		assertTrue(this.object.hasField(this.field.getID()));
		
		// add a field and remove it again
		transObject.createField(fieldId);
		assertTrue(transObject.hasField(fieldId));
		
		assertTrue(transObject.removeField(fieldId));
		assertFalse(transObject.hasField(fieldId));
		assertFalse(this.object.hasField(fieldId));
	}
	
	/*
	 * Note: there is no need to test the "executeCommand" methods of {@link
	 * InModelTransactionObject}, since they only pass the command to their
	 * TransactionModel and do nothing else
	 */

	/*
	 * Tests for the methods of {@link InModelTransactionField}
	 */

	@Test
	public void testInModelTransactionField() {
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		XWritableField temp = transObject.getField(this.field.getID());
		assertTrue(temp instanceof InModelTransactionField);
		
		InModelTransactionField transField = (InModelTransactionField)temp;
		
		assertEquals(this.field.getRevisionNumber(), transField.getRevisionNumber());
		assertEquals(this.field.getID(), transField.getID());
		assertEquals(this.field.isEmpty(), transField.isEmpty());
		assertEquals(this.field.getAddress(), transField.getAddress());
		assertEquals(this.field.getValue(), transField.getValue());
		assertEquals(this.field, transField);
	}
	
	@Test
	public void testInModelTransactionFieldSetValueCorrectUsage() {
		XValue value = X.getValueFactory().createStringValue("42");
		XValue value2 = X.getValueFactory().createStringValue("test");
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		XWritableField temp = transObject.getField(this.field.getID());
		assertTrue(temp instanceof InModelTransactionField);
		
		InModelTransactionField transField = (InModelTransactionField)temp;
		
		// add value
		assertTrue(transField.setValue(value));
		
		assertEquals(value, transField.getValue());
		assertEquals(null, this.field.getValue());
		
		// change value
		assertTrue(transField.setValue(value2));
		
		assertEquals(value2, transField.getValue());
		assertEquals(null, this.field.getValue());
		
		// remove value
		assertTrue(transField.setValue(null));
		
		assertEquals(null, transField.getValue());
	}
	
	@Test
	public void testInModelTransactionFieldSetValueIncorrectUsage() {
		InModelTransactionObject transObject = (InModelTransactionObject)this.transModel
		        .getObject(this.object.getID());
		
		XWritableField temp = transObject.getField(this.field.getID());
		assertTrue(temp instanceof InModelTransactionField);
		
		InModelTransactionField transField = (InModelTransactionField)temp;
		
		// try to remove not existing value
		assertFalse(transField.setValue(null));
	}
	
	private class TestCallback implements XLocalChangeCallback {
		boolean failed = false;
		Long revision;
		
		@Override
		public void onFailure() {
			this.failed = true;
		}
		
		@Override
		public void onSuccess(long revision) {
			this.revision = revision;
		}
		
	}
}
