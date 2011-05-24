package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;


public class TransactionObjectTest {
	private TransactionObject transObject;
	private MemoryObject object;
	private XWritableField field;
	
	{
		LoggerTestHelper.init();
	}
	
	@Before
	public void setup() {
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		
		MemoryRepository repo = (MemoryRepository)X.createMemoryRepository(XX.toId("testActor"));
		MemoryModel model = repo.createModel(modelId);
		this.object = model.createObject(objectId);
		
		// add a single field
		this.field = this.object.createField(fieldId);
		
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
		// try to get an already existing object
		XWritableField field2 = this.transObject.getField(this.field.getID());
		
		assertTrue(field2.equals(this.field));
		assertTrue(this.field.equals(field2));
		
		assertEquals(field2, this.field);
		assertEquals(this.field, field2);
	}
}
