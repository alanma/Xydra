package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;


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
	
	@Test
	public void testGetAddress() {
		assertEquals(this.object.getAddress(), this.transObject.getAddress());
	}
	
	@Test
	public void testGetId() {
		assertEquals(this.object.getID(), this.transObject.getID());
	}
	
	// TODO the isEmpty method currently doesn't work at all, so ignore this
	// test at the moment - I need to fix this!
	@Test
	@Ignore
	public void testIsEmpty() {
		// At first, the value should be the same as that of object.isEmpty()
		assertEquals(this.object.isEmpty(), this.transObject.isEmpty());
		
		// remove the field from transObject
		assertTrue(this.transObject.removeField(this.field.getID()));
		
		assertTrue(this.transObject.isEmpty());
		assertFalse(this.object.isEmpty());
	}
	
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
	
	@Test
	public void testRemoveField() {
		// try to remove a not existing field
		XID fieldId = XX.createUniqueId();
		
		assertFalse(this.transObject.removeField(fieldId));
		
		// try to remove an existing field
		assertTrue(this.transObject.removeField(this.field.getID()));
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
		assertFalse(this.field.equals(field2));
	}
	
	/*
	 * Tests for executeCommand(XCommand command, XLocalCallback callback)
	 * 
	 * Note: the other executeCommand without the callback-parameter needs no
	 * extra tests, since the only thing it does is call this method
	 */

	/*
	 * TODO Check that failing operations do not change old revisionnumbers
	 */
	@Test
	public void testExecuteCommandsSafeAddFieldCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertNull(this.transObject.getField(newFieldId));
		
		// add the field
		XCommand addCommand = factory.createSafeAddFieldCommand(this.transObject.getAddress(),
		        newFieldId);
		
		long result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was added correctly
		assertTrue(this.transObject.hasField(newFieldId));
		
		XWritableField field = this.transObject.getField(newFieldId);
		assertEquals((Long)field.getRevisionNumber(), callback.revision);
		assertEquals(field.getRevisionNumber(), result);
		
		assertFalse(this.object.hasField(newFieldId));
		
		// try to add a field that already exists, should fail
		addCommand = factory.createSafeAddFieldCommand(this.transObject.getAddress(),
		        this.field.getID());
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
	}
	
	@Test
	public void testExecuteCommandsForcedAddFieldCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertNull(this.transObject.getField(newFieldId));
		
		// add the field
		XCommand addCommand = factory.createForcedAddFieldCommand(this.transObject.getAddress(),
		        newFieldId);
		
		long result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was added correctly
		assertTrue(this.transObject.hasField(newFieldId));
		
		XWritableField field = this.transObject.getField(newFieldId);
		assertEquals((Long)field.getRevisionNumber(), callback.revision);
		assertEquals(field.getRevisionNumber(), result);
		
		assertFalse(this.object.hasField(newFieldId));
		
		// try to add a field that already exists, should succeed
		addCommand = factory.createForcedAddFieldCommand(this.transObject.getAddress(),
		        this.field.getID());
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
	}
	
	@Test
	public void testExecuteCommandsSafeRemoveFieldCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertNull(this.transObject.getField(newFieldId));
		
		// try to remove not existing field - should fail
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		
		XCommand removeCommand = factory.createSafeRemoveFieldCommand(address, 0);
		
		long result = this.transObject.executeCommand(removeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// try to remove a field that already exists, should succeed
		removeCommand = factory.createSafeRemoveFieldCommand(this.field.getAddress(),
		        this.field.getRevisionNumber());
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(removeCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was removed correctly
		assertFalse(this.transObject.hasField(this.field.getID()));
		assertTrue(this.object.hasField(this.field.getID()));
	}
	
	@Test
	public void testExecuteCommandsForcedRemoveFieldCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		// make sure there is no field with this ID
		assertNull(this.transObject.getField(newFieldId));
		
		// try to remove not existing field - should succeed
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		
		XCommand removeCommand = factory.createForcedRemoveFieldCommand(address);
		
		long result = this.transObject.executeCommand(removeCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// try to remove a field that already exists, should succeed
		removeCommand = factory.createForcedRemoveFieldCommand(this.field.getAddress());
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(removeCommand, callback);
		assertTrue(result > 0);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the field was removed correctly
		assertFalse(this.transObject.hasField(this.field.getID()));
		assertTrue(this.object.hasField(this.field.getID()));
	}
	
	@Test
	public void testExecuteCommandsSafeAddValueCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// add a value to a not existing field, should fail
		assertFalse(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand addCommand = factory.createSafeAddValueCommand(address, 0, value);
		
		long result = this.transObject.executeCommand(addCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// add a value to an existing field, should succeed
		addCommand = factory.createSafeAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = this.transObject.getField(this.field.getID());
		assertEquals((Long)result, (Long)changedField.getRevisionNumber());
		assertFalse(result == this.field.getRevisionNumber());
		
		assertEquals(value, changedField.getValue());
		assertFalse(value.equals(this.field.getValue()));
		
		// try to add a value to a field which value is already set, should fail
		addCommand = factory.createSafeAddValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
	}
	
	@Test
	public void testExecuteCommandsForcedAddValueCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// add a value to a not existing field, should fail
		assertFalse(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand addCommand = factory.createForcedAddValueCommand(address, value);
		
		long result = this.transObject.executeCommand(addCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// add a value to an existing field, should succeed
		addCommand = factory.createForcedAddValueCommand(this.field.getAddress(), value);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = this.transObject.getField(this.field.getID());
		assertEquals((Long)result, (Long)changedField.getRevisionNumber());
		assertFalse(result == this.field.getRevisionNumber());
		
		assertEquals(value, changedField.getValue());
		assertFalse(value.equals(this.field.getValue()));
		
		// try to add value to a field which value is already set (use the same
		// value), should succeed
		addCommand = factory.createForcedAddValueCommand(this.field.getAddress(), value);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// try to add value to a field which value is already set (use another
		// same value), should succeed
		XValue value2 = X.getValueFactory().createStringValue("test2");
		addCommand = factory.createForcedAddValueCommand(this.field.getAddress(), value2);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(addCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		changedField = this.transObject.getField(this.field.getID());
		assertEquals((Long)result, (Long)changedField.getRevisionNumber());
		assertFalse(result == this.field.getRevisionNumber());
		
		assertEquals(value2, changedField.getValue());
		assertFalse(value2.equals(this.field.getValue()));
	}
	
	@Test
	public void testExecuteCommandsSafeChangeValueCommands() {
		XCommandFactory factory = X.getCommandFactory();
		XID newFieldId = X.getIDProvider().createUniqueId();
		TestCallback callback = new TestCallback();
		
		XValue value = X.getValueFactory().createStringValue("test");
		
		// change a value of a not existing field, should fail
		assertFalse(this.transObject.hasField(newFieldId));
		
		XAddress temp = this.transObject.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        newFieldId);
		XCommand changeCommand = factory.createSafeChangeValueCommand(address, 0, value);
		
		long result = this.transObject.executeCommand(changeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// change the value of a field, which value is not set - should fail
		changeCommand = factory.createSafeChangeValueCommand(this.field.getAddress(),
		        this.field.getRevisionNumber(), value);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(changeCommand, callback);
		assertEquals(XCommand.FAILED, result);
		
		// check callback
		assertTrue(callback.failed);
		assertNull(callback.revision);
		
		// check that nothing was changed
		XWritableField simulatedField = this.transObject.getField(this.field.getID());
		assertNull(simulatedField.getValue());
		assertNull(this.field.getValue());
		
		// change the value of a field, which value is already set - should
		// succeed
		changeCommand = factory.createSafeChangeValueCommand(this.fieldWithValue.getAddress(),
		        this.fieldWithValue.getRevisionNumber(), value);
		callback = new TestCallback();
		
		result = this.transObject.executeCommand(changeCommand, callback);
		assertTrue(result > 0);
		assertEquals(this.transObject.getRevisionNumber(), result);
		assertFalse(this.object.getRevisionNumber() == result);
		
		// check callback
		assertFalse(callback.failed);
		assertEquals((Long)result, callback.revision);
		
		// check whether the simulated field was changed and the real field
		// wasn't
		XWritableField changedField = this.transObject.getField(this.fieldWithValue.getID());
		assertEquals((Long)result, (Long)changedField.getRevisionNumber());
		assertFalse(result == this.field.getRevisionNumber());
		
		assertEquals(value, changedField.getValue());
		assertFalse(value.equals(this.fieldWithValue.getValue()));
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