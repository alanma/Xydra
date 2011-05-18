package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public class TransactionObjectTest {
	private TransactionObject transObject;
	private XObject object;
	private XWritableField field;
	
	{
		LoggerTestHelper.init();
	}
	
	@Before
	public void setup() {
		XID modelId = XX.createUniqueId();
		XID objectId = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		
		XRepository repo = X.createMemoryRepository(null);
		XModel model = repo.createModel(modelId);
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
	
	@Test
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
		assertEquals(field, this.transObject.createField(fieldId));
		
		// make sure it exists in the transObject but not in object
		assertFalse(this.object.hasField(fieldId));
		assertTrue(this.transObject.hasField(fieldId));
	}
	
	@Test
	public void testRemoveField() {
		// try to remove a not existing field
		
	}
}
