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
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;


/*
 * TODO Maybe test executing transactions more thoroughly
 */

public class TransactionObjectTest {
	private TransactionObject transObject;
	private MemoryObject object;
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
		MemoryModel model = repo.createModel(modelId);
		this.object = model.createObject(objectId);
		
		// add two fields
		this.field = this.object.createField(fieldId);
		this.fieldWithValue = this.object.createField(fieldWithValueId);
		
		// set its value
		XValue value = X.getValueFactory().createStringValue("test value");
		this.fieldWithValue.setValue(value);
		
		this.transObject = new TransactionObject(this.object);
	}
	
	// Tests for commit() {
	
	@Test
	public void testCommitSingleCommands() {
		// add a new XField
		XID fieldId = X.getIDProvider().createUniqueId();
		assertFalse(this.transObject.hasField(fieldId) || this.object.hasField(fieldId));
		
		this.transObject.createField(fieldId);
		assertEquals(1, this.transObject.size());
		
		assertTrue(this.transObject.isChanged());
		
		long oldRevNr = this.object.getRevisionNumber();
		long revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the field was added to the real object
		assertTrue(this.object.hasField(fieldId));
		
		// remove an XField
		this.transObject.removeField(fieldId);
		assertEquals(1, this.transObject.size());
		
		assertTrue(this.transObject.isChanged());
		
		oldRevNr = this.object.getRevisionNumber();
		revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the field was removed from the real object
		assertFalse(this.object.hasField(fieldId));
		
		// add a value
		XValue value = X.getValueFactory().createStringValue("testValue");
		XWritableField field = this.transObject.getField(this.field.getID());
		
		field.setValue(value);
		
		assertTrue(this.transObject.isChanged());
		
		oldRevNr = this.object.getRevisionNumber();
		revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the value was added to the field
		assertEquals(value, this.field.getValue());
		
		// change a value
		value = X.getValueFactory().createStringValue("testValue2");
		field = this.transObject.getField(this.field.getID());
		
		field.setValue(value);
		
		assertTrue(this.transObject.isChanged());
		
		oldRevNr = this.object.getRevisionNumber();
		revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the value of the field was changed
		assertEquals(value, this.field.getValue());
		
		// remove a value
		field = this.transObject.getField(this.field.getID());
		
		field.setValue(null);
		
		assertTrue(this.transObject.isChanged());
		
		oldRevNr = this.object.getRevisionNumber();
		revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the value was removed from the field
		assertEquals(null, this.field.getValue());
	}
	
	@Test
	public void testCommitTransaction() {
		// add some fields
		XID fieldId1 = X.getIDProvider().createUniqueId();
		XID fieldId2 = X.getIDProvider().createUniqueId();
		XID fieldId3 = X.getIDProvider().createUniqueId();
		
		this.transObject.createField(fieldId1);
		this.transObject.createField(fieldId2);
		this.transObject.createField(fieldId3);
		
		// remove some fields
		this.transObject.removeField(this.field.getID());
		this.transObject.removeField(fieldId3);
		
		// add some values
		XValue value = X.getValueFactory().createStringValue("testValue");
		XWritableField field1 = this.transObject.getField(fieldId1);
		XWritableField field2 = this.transObject.getField(fieldId2);
		
		field1.setValue(value);
		field2.setValue(value);
		
		// remove a value
		field2.setValue(null);
		
		// change a value
		XWritableField temp = this.transObject.getField(this.fieldWithValue.getID());
		temp.setValue(value);
		
		// commit the transaction
		assertTrue(this.transObject.isChanged());
		
		long oldRevNr = this.object.getRevisionNumber();
		long revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the changes were actually executed
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

		this.transObject.createField(fieldId3);
		XWritableField field3 = this.transObject.getField(fieldId3);
		
		field3.setValue(value);
		
		temp = this.transObject.getField(this.fieldWithValue.getID());
		temp.setValue(null);
		
		this.transObject.removeField(fieldId1);
		
		// commit the transaction
		assertTrue(this.transObject.isChanged());
		
		oldRevNr = this.object.getRevisionNumber();
		revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the changes were actually executed
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
		assertEquals(this.object.getAddress(), this.transObject.getAddress());
	}
	
	// Tests for getId()
	@Test
	public void testGetId() {
		assertEquals(this.object.getID(), this.transObject.getID());
	}
	
	/*
	 * Tests for executeCommand(XCommand command, XLocalCallback callback)
	 * 
	 * Note: the other executeCommand without the callback-parameter needs no
	 * extra tests, since the only thing it does is call this method
	 */

	@Test
	public void testExecuteCommandsWrongXAddress() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		XAddress randomAddress = XX.toAddress(XX.createUniqueId(), XX.createUniqueId(),
		        XX.createUniqueId(), XX.createUniqueId());
		
		XCommand fieldCommand = factory.createAddFieldCommand(randomAddress, newFieldId, false);
		
		long result = this.transObject.executeCommand(fieldCommand, callback);
		assertEquals(XCommand.FAILED, result);
		assertFalse(this.transObject.isChanged());
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		XCommand valueCommand = factory.createAddValueCommand(randomAddress, 0,
		        XX.createUniqueId(), false);
		
		result = this.transObject.executeCommand(valueCommand, callback);
		assertEquals(XCommand.FAILED, result);
		assertFalse(this.transObject.isChanged());
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
	}
	
	private void testExecuteCommandsAddFieldCommands(boolean isForced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertNull(this.transObject.getField(newFieldId));
		
		// add the field
		XCommand addCommand = factory.createAddFieldCommand(this.transObject.getAddress(),
		        newFieldId, isForced);
		
		long sizeBefore = this.transObject.size();
		long result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result != XCommand.FAILED);
		assertTrue(this.transObject.isChanged());
		assertEquals(sizeBefore + 1, this.transObject.size());
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was added correctly
		assertTrue(this.transObject.hasField(newFieldId));
		
		XWritableField field = this.transObject.getField(newFieldId);
		assertTrue(field.getRevisionNumber() >= 0);
		
		assertFalse(this.object.hasField(newFieldId));
		
		// try to add a field that already exists
		addCommand = factory.createAddFieldCommand(this.transObject.getAddress(),
		        this.field.getID(), isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(addCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeAddFieldCommands() {
		testExecuteCommandsAddFieldCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedAddFieldCommands() {
		testExecuteCommandsAddFieldCommands(true);
	}
	
	private void testExecuteCommandsRemoveFieldCommands(boolean isForced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertNull(this.transObject.getField(newFieldId));
		
		// try to remove not existing field
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		
		XCommand removeCommand = factory.createRemoveFieldCommand(address, 0, isForced);
		
		long sizeBefore = this.transObject.size();
		long result = this.transObject.executeCommand(removeCommand, callback);
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// try to remove a field that already exists & use wrong revNr
		removeCommand = factory.createRemoveFieldCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber() + 1, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(removeCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// try to remove a field that already exists, should succeed
		removeCommand = factory.createRemoveFieldCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(removeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		assertEquals(sizeBefore + 1, this.transObject.size());
		
		// check whether the field was removed correctly
		assertFalse(this.transObject.hasField(this.field.getID()));
		assertTrue(this.object.hasField(this.field.getID()));
	}
	
	@Test
	public void testExecuteCommandsSafeRemoveFieldCommands() {
		testExecuteCommandsRemoveFieldCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedRemoveFieldCommands() {
		testExecuteCommandsRemoveFieldCommands(true);
	}
	
	private void testExecuteCommandsAddValueCommands(boolean isForced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// add a value to a not existing field, should fail
		assertFalse(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand addCommand = factory.createAddValueCommand(address, 0, value, isForced);
		
		long sizeBefore = this.transObject.size();
		long result = this.transObject.executeCommand(addCommand, callback);
		assertEquals(XCommand.FAILED, result);
		assertEquals(sizeBefore, this.transObject.size());
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// add a value to an existing field, use wrong revNr
		addCommand = factory.createAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber() + 1, value, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(addCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// value was set, remove it again or the following case won't test
			// what it's supposed to test
			this.transObject.getField(this.field.getID()).setValue(null);
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// add a value to an existing field, should succeed
		addCommand = factory.createAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result != XCommand.FAILED);
		assertEquals(sizeBefore + 1, this.transObject.size());
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = this.transObject.getField(this.field.getID());
		
		assertEquals(value, changedField.getValue());
		assertFalse(value.equals(this.field.getValue()));
		
		// try to add a value to a field which value is already set
		XValue value2 = XX.createUniqueId();
		addCommand = factory.createAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value2, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(addCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			changedField = this.transObject.getField(this.field.getID());
			
			assertEquals(value2, changedField.getValue());
			assertFalse(value2.equals(this.field.getValue()));
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
	}
	
	@Test
	public void testExecuteCommandsSafeAddValueCommands() {
		testExecuteCommandsAddValueCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedAddValueCommands() {
		testExecuteCommandsAddValueCommands(true);
	}
	
	private void testExecuteCommandsChangeValueCommands(boolean isForced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// change a value of a not existing field, should fail
		assertFalse(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand changeCommand = factory.createChangeValueCommand(address, 0, value, isForced);
		
		long sizeBefore = this.transObject.size();
		long result = this.transObject.executeCommand(changeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		assertEquals(sizeBefore, this.transObject.size());
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// change the value of a field, which value is not set
		changeCommand = factory.createChangeValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(changeCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			XWritableField changedField = this.transObject.getField(this.field.getID());
			
			assertEquals(value, changedField.getValue());
			assertFalse(value.equals(this.fieldWithValue.getValue()));
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
			
			// check that nothing was changed
			XWritableField simulatedField = this.transObject.getField(this.field.getID());
			assertNull(simulatedField.getValue());
			assertNull(this.field.getValue());
		}
		
		// change the value of a field, which value is already set, but use
		// wrong revNr
		changeCommand = factory.createChangeValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber() + 1, value, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(changeCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			XWritableField changedField = this.transObject.getField(this.fieldWithValue.getID());
			
			assertEquals(value, changedField.getValue());
			assertFalse(value.equals(this.fieldWithValue.getValue()));
		} else {
			assertEquals(XCommand.FAILED, result);
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
			
			// check that nothing was changed
			XWritableField changedField = this.transObject.getField(this.fieldWithValue.getID());
			
			assertFalse(value.equals(changedField.getValue()));
			assertFalse(value.equals(this.fieldWithValue.getValue()));
		}
		
		// change the value of a field, which value is already set - should
		// succeed
		XValue value2 = XX.createUniqueId();
		changeCommand = factory.createChangeValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber(), value2, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(changeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		assertEquals(sizeBefore + 1, this.transObject.size());
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = this.transObject.getField(this.fieldWithValue.getID());
		
		assertEquals(value2, changedField.getValue());
		assertFalse(value2.equals(this.fieldWithValue.getValue()));
	}
	
	@Test
	public void testExecuteCommandsSafeChangeValueCommands() {
		testExecuteCommandsChangeValueCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedChangeValueCommands() {
		testExecuteCommandsChangeValueCommands(true);
	}
	
	private void testExecuteCommandsRemoveValueCommands(boolean isForced) {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// remove a value from a not existing field, should fail
		assertFalse(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand removeCommand = factory.createRemoveValueCommand(address, 0, isForced);
		
		long sizeBefore = this.transObject.size();
		long result = this.transObject.executeCommand(removeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		assertEquals(sizeBefore, this.transObject.size());
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// remove a value from an existing field, without a set value
		removeCommand = factory.createRemoveValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(removeCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// remove a value from an existing field with set value, use wrong revNr
		removeCommand = factory.createRemoveValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber() + 1, isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(removeCommand, callback);
		
		if(isForced) {
			assertTrue(result != XCommand.FAILED);
			assertEquals(sizeBefore + 1, this.transObject.size());
			
			// check callback
			assertFalse(callback.failed);
			assertEquals((Long)result, callback.revision);
			
			// check whether the simulated field was changed and the real field
			// wasn't
			XWritableField changedField = this.transObject.getField(this.fieldWithValue.getID());
			
			assertNull(changedField.getValue());
			assertNotNull(this.fieldWithValue.getValue());
			
			// reset value or the following case will not test what it is
			// supposed to test
			changedField.setValue(XX.createUniqueId());
		} else {
			assertEquals(XCommand.FAILED, result);
			assertEquals(sizeBefore, this.transObject.size());
			
			// check callback
			assertTrue(callback.failed);
			assertNull(callback.revision);
		}
		
		// remove a value from an existing field with set value - should succeed
		removeCommand = factory.createRemoveValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber(), isForced);
		callback = new TestCallback();
		
		sizeBefore = this.transObject.size();
		result = this.transObject.executeCommand(removeCommand, callback);
		assertTrue(result != XCommand.FAILED);
		assertEquals(sizeBefore + 1, this.transObject.size());
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = this.transObject.getField(this.fieldWithValue.getID());
		
		assertNull(changedField.getValue());
		assertNotNull(this.fieldWithValue.getValue());
	}
	
	@Test
	public void testExecuteCommandsSafeRemoveValueCommands() {
		testExecuteCommandsRemoveValueCommands(false);
	}
	
	@Test
	public void testExecuteCommandsForcedRemoveValueCommands() {
		testExecuteCommandsRemoveValueCommands(true);
	}
	
	@Test
	public void testExecuteCommandSucceedingTransaction() {
		// manually build a transaction
		XTransactionBuilder builder = new XTransactionBuilder(this.object.getAddress());
		
		// add some fields
		XID fieldId1 = X.getIDProvider().createUniqueId();
		XID fieldId2 = X.getIDProvider().createUniqueId();
		
		builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId1);
		builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId2);
		
		// add something, remove it again
		XID temp = X.getIDProvider().createUniqueId();
		builder.addField(this.object.getAddress(), XCommand.SAFE, temp);
		builder.removeField(this.object.getAddress(), XCommand.NEW, temp);
		
		// remove some fields
		builder.removeField(this.object.getAddress(), this.field.getRevisionNumber(),
		        this.field.getID());
		
		// add some values
		XValue value = X.getValueFactory().createStringValue("testValue");
		XAddress fieldAddress1 = XX.toAddress(this.object.getAddress().getRepository(), this.object
		        .getAddress().getModel(), this.object.getAddress().getObject(), fieldId1);
		XAddress fieldAddress2 = XX.toAddress(this.object.getAddress().getRepository(), this.object
		        .getAddress().getModel(), this.object.getAddress().getObject(), fieldId2);
		
		builder.addValue(fieldAddress1, XCommand.NEW, value);
		builder.addValue(fieldAddress2, XCommand.NEW, value);
		
		// remove values
		builder.removeValue(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber());
		builder.removeValue(fieldAddress2, XCommand.NEW);
		
		// execute the transaction - should succeed
		XTransaction transaction = builder.build();
		TestCallback callback = new TestCallback();
		
		long result = this.transObject.executeCommand(transaction, callback);
		
		assertTrue(result != XCommand.FAILED);
		assertTrue(this.transObject.isChanged());
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		assertEquals(transaction.size(), this.transObject.size());
		
		/*
		 * we do not need to check whether the single commands in the
		 * transaction were correctly executed, since they all are executed by
		 * separately calling the executeCommand() method, which was already
		 * thoroughly tested above
		 */

		// commit the transaction
		
		long oldRevNr = this.object.getRevisionNumber();
		long revNr = this.transObject.commit();
		
		assertFalse(this.transObject.isChanged());
		
		assertTrue(revNr != XCommand.FAILED);
		assertEquals(oldRevNr + 1, revNr);
		assertEquals(0, this.transObject.size());
		assertEquals(revNr, this.transObject.getRevisionNumber());
		
		// check that the changes were actually executed
		assertTrue(this.object.hasField(fieldId1));
		assertTrue(this.object.hasField(fieldId2));
		
		assertFalse(this.object.hasField(this.field.getID()));
		assertFalse(this.object.hasField(temp));
		
		XField field1 = this.object.getField(fieldId1);
		assertEquals(value, field1.getValue());
		
		XField field2 = this.object.getField(fieldId2);
		assertNull(field2.getValue());
		
		assertEquals(null, this.fieldWithValue.getValue());
	}
	
	private void testExecuteCommandsFailingTransaction(XType type, ChangeType changeType) {
		// manually build a transaction
		XTransactionBuilder builder = new XTransactionBuilder(this.object.getAddress());
		
		// add some fields
		XID fieldId1 = X.getIDProvider().createUniqueId();
		XID fieldId2 = X.getIDProvider().createUniqueId();
		
		builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId1);
		builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId2);
		
		// add some values
		XValue value = X.getValueFactory().createStringValue("testValue");
		XAddress fieldAddress1 = XX.toAddress(this.object.getAddress().getRepository(), this.object
		        .getAddress().getModel(), this.object.getAddress().getObject(), fieldId1);
		XAddress fieldAddress2 = XX.toAddress(this.object.getAddress().getRepository(), this.object
		        .getAddress().getModel(), this.object.getAddress().getObject(), fieldId2);
		
		builder.addValue(fieldAddress1, XCommand.NEW, value);
		builder.addValue(fieldAddress2, XCommand.NEW, value);
		
		// add failing command
		assert type == XType.XOBJECT || type == XType.XFIELD;
		
		if(type == XType.XOBJECT) {
			assert changeType != ChangeType.CHANGE;
			
			switch(changeType) {
			case ADD:
				// try to add a field which already exists
				builder.addField(this.object.getAddress(), XCommand.SAFE, this.field.getID());
				break;
			
			case REMOVE:
				// try to remove a field which doesn't exist
				builder.removeField(this.object.getAddress(), XCommand.SAFE, XX.createUniqueId());
				break;
			case CHANGE:
			case TRANSACTION:
				assert false : "unexpected change type " + changeType + " for xtype " + type;
			}
		} else {
			switch(changeType) {
			case ADD:
				// try to add a value to a field which value is already set
				builder.addValue(this.fieldWithValue.getAddress(), XCommand.SAFE,
				        XX.createUniqueId());
				break;
			
			case REMOVE:
				// try to remove the vale from a field which value isn't set
				builder.removeValue(this.field.getAddress(), XCommand.SAFE);
				break;
			case CHANGE:
				// try to change the value of a field which value is set, but
				// use wrong revision number
				builder.changeValue(this.fieldWithValue.getAddress(),
				        this.fieldWithValue.getRevisionNumber() + 1, XX.createUniqueId());
				break;
			case TRANSACTION:
				assert false : "unexpected change type " + changeType + " for xtype " + type;
			}
			
		}
		
		// add some more commands after the command which should fail
		builder.addField(this.object.getAddress(), XCommand.SAFE, XX.createUniqueId());
		builder.addField(this.object.getAddress(), XCommand.SAFE, XX.createUniqueId());
		builder.addValue(this.field.getAddress(), XCommand.FORCED, XX.createUniqueId());
		
		// execute something before the transaction
		this.transObject.createField(XX.createUniqueId());
		
		// execute the transaction - should fail
		XTransaction transaction = builder.build();
		TestCallback callback = new TestCallback();
		
		int sizeBefore = this.transObject.size();
		long result = this.transObject.executeCommand(transaction, callback);
		
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// check that nothing was changed by the transaction
		assertEquals(sizeBefore, this.transObject.size());
		
		assertFalse(this.transObject.hasField(fieldId1));
		assertFalse(this.transObject.hasField(fieldId2));
	}
	
	/*
	 * Tests for failing transaction that fail because of an XObjectCommand
	 */

	@Test
	public void testExecuteCommandsFailingTransactionAddObjectCommand() {
		testExecuteCommandsFailingTransaction(XType.XOBJECT, ChangeType.ADD);
	}
	
	@Test
	public void testExecuteCommandsFailingTransactionRemoveObjectCommand() {
		testExecuteCommandsFailingTransaction(XType.XOBJECT, ChangeType.REMOVE);
	}
	
	/*
	 * Tests for transactions which fail because of an XFieldCommand
	 */

	@Test
	public void testExecuteCommandsFailingTransactionAddFieldCommand() {
		testExecuteCommandsFailingTransaction(XType.XFIELD, ChangeType.ADD);
	}
	
	@Test
	public void testExecuteCommandsFailingTransactionRemoveFieldCommand() {
		testExecuteCommandsFailingTransaction(XType.XFIELD, ChangeType.REMOVE);
	}
	
	@Test
	public void testExecuteCommandsFailingTransactionChangeFieldCommand() {
		testExecuteCommandsFailingTransaction(XType.XFIELD, ChangeType.CHANGE);
	}
	
	// Tests for getRevisionNumber
	@Test
	public void testGetRevisionNumber() {
		assertEquals(this.object.getRevisionNumber(), this.transObject.getRevisionNumber());
	}
	
	// Tests for isEmpty
	@Test
	public void testIsEmpty() {
		// At first, the value should be the same as that of object.isEmpty()
		assertEquals(this.object.isEmpty(), this.transObject.isEmpty());
		
		// remove all fields from transObject
		assertTrue(this.transObject.removeField(this.field.getID()));
		assertTrue(this.transObject.removeField(this.fieldWithValue.getID()));
		
		assertTrue(this.transObject.isEmpty());
		assertFalse(this.object.isEmpty());
		
		// add a new field, remove it, check again
		XID newFieldId = X.getIDProvider().createUniqueId();
		this.transObject.createField(newFieldId);
		
		assertTrue(this.transObject.hasField(newFieldId));
		assertFalse(this.object.hasField(newFieldId));
		
		this.transObject.removeField(newFieldId);
		assertTrue(this.transObject.isEmpty());
		assertFalse(this.object.isEmpty());
	}
	
	// Tests for hasField()
	@Test
	public void testHasField() {
		assertTrue(this.transObject.hasField(this.field.getID()));
		assertTrue(this.transObject.hasField(this.fieldWithValue.getID()));
		
		// add a field
		XID newFieldId = XX.createUniqueId();
		assertFalse(this.transObject.hasField(newFieldId));
		
		this.transObject.createField(newFieldId);
		assertTrue(this.transObject.hasField(newFieldId));
		
		// remove a field
		this.transObject.removeField(newFieldId);
		assertFalse(this.transObject.hasField(newFieldId));
		
		// do the same with commands
		XCommand addCommand = X.getCommandFactory().createSafeAddFieldCommand(
		        this.transObject.getAddress(), newFieldId);
		this.transObject.executeCommand(addCommand);
		assertTrue(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.object.getAddress();
		XAddress fieldAddress = XX.toAddress(temp.getRepository(), temp.getModel(),
		        temp.getObject(), newFieldId);
		XCommand removeCommand = X.getCommandFactory().createSafeRemoveFieldCommand(fieldAddress,
		        XCommand.NEW);
		this.transObject.executeCommand(removeCommand);
		assertFalse(this.transObject.hasField(newFieldId));
	}
	
	// Tests for createField()
	@Test
	public void testCreateField() {
		XID fieldId = XX.createUniqueId();
		XWritableField field = this.transObject.createField(fieldId);
		
		assertNotNull(field);
		
		// make sure it exists in the transObject but not in object
		assertFalse(this.object.hasField(fieldId));
		assertTrue(this.transObject.hasField(fieldId));
		
		// try to add the same field again
		XWritableField field2 = this.transObject.createField(fieldId);
		assertEquals(field, field2);
		
		// make sure it exists in the transObject but not in object
		assertFalse(this.object.hasField(fieldId));
		assertTrue(this.transObject.hasField(fieldId));
	}
	
	// Tests for removeField()
	@Test
	public void testRemoveField() {
		// try to remove a not existing field
		XID fieldId = XX.createUniqueId();
		
		assertFalse(this.transObject.removeField(fieldId));
		
		// try to remove an existing field
		boolean removed = this.transObject.removeField(this.field.getID());
		assertTrue(removed);
		assertFalse(this.transObject.hasField(this.field.getID()));
		
		// make sure it wasn't removed from the underlying object
		assertTrue(this.object.hasField(this.field.getID()));
		
		// add a field an remove it again
		this.transObject.createField(fieldId);
		assertTrue(this.transObject.hasField(fieldId));
		
		assertTrue(this.transObject.removeField(fieldId));
		assertFalse(this.transObject.hasField(fieldId));
		assertFalse(this.object.hasField(fieldId));
	}
	
	// Tests for getField()
	@Test
	public void testGetField() {
		// try to get an already existing field
		XWritableField field2 = this.transObject.getField(this.field.getID());
		
		assertEquals(this.field, field2);
		
		// try to get a not existing field
		assertNull(this.transObject.getField(X.getIDProvider().createUniqueId()));
		
		// change the existing field and get it again
		XCommandFactory factory = X.getCommandFactory();
		XValue value = X.getValueFactory().createStringValue("test");
		XCommand command = factory.createSafeAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value);
		
		this.transObject.executeCommand(command);
		
		field2 = this.transObject.getField(this.field.getID());
		
		// revision numbers are not increased/managed by the TransactionObject,
		// therefore this should succeed
		assertTrue(this.field.equals(field2));
	}
	
	/*
	 * Tests for the methods of {@link InObjectTransactionField}
	 */

	@Test
	public void testInObjectTransactionField() {
		XWritableField temp = this.transObject.getField(this.field.getID());
		assertTrue(temp instanceof InObjectTransactionField);
		
		InObjectTransactionField transField = (InObjectTransactionField)temp;
		
		assertEquals(this.field.getRevisionNumber(), transField.getRevisionNumber());
		assertEquals(this.field.getID(), transField.getID());
		assertEquals(this.field.isEmpty(), transField.isEmpty());
		assertEquals(this.field.getAddress(), transField.getAddress());
		assertEquals(this.field.getValue(), transField.getValue());
		assertEquals(this.field, transField);
	}
	
	@Test
	public void testInObjectTransactionFieldSetValueCorrectUsage() {
		XValue value = X.getValueFactory().createStringValue("42");
		XValue value2 = X.getValueFactory().createStringValue("test");
		
		XWritableField temp = this.transObject.getField(this.field.getID());
		assertTrue(temp instanceof InObjectTransactionField);
		
		InObjectTransactionField transField = (InObjectTransactionField)temp;
		
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
	public void testInObjectTransactionFieldSetValueIncorrectUsage() {
		XWritableField temp = this.transObject.getField(this.field.getID());
		assertTrue(temp instanceof InObjectTransactionField);
		
		InObjectTransactionField transField = (InObjectTransactionField)temp;
		
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
