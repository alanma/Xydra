package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public abstract class XModelObjectLevelIndexTest {
	
	private enum TestType {
		XOBJECT, XFIELD, XEVENT;
	}
	
	/**
	 * The basic model, can be used to get the "old" objects etc. to test
	 * updateIndex methods
	 */
	private XModel oldModel;
	
	private XModel newModel;
	
	/*
	 * TODO initialize the index and indexer!
	 */
	protected XModelObjectLevelIndex oldIndex;
	protected XModelObjectLevelIndex newIndex;
	protected XValueIndexer oldIndexer;
	protected XValueIndexer newIndexer;
	
	public abstract void initializeIndexes(XModel oldModel, XModel newModel,
	        XValueIndexer oldIndexer, XValueIndexer newIndexer);
	
	public abstract void initializeIndexers();
	
	@Before
	public void setup() {
		XID actorId = XX.createUniqueId();
		
		XRepository repo1 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo1);
		
		this.oldModel = repo1.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XRepository repo2 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo2);
		
		this.newModel = repo2.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		initializeIndexers();
		
		initializeIndexes(this.oldModel, this.newModel, this.oldIndexer, this.newIndexer);
	}
	
	@Test
	public void testIndexingXModel() {
		/*
		 * The model was already indexed during the setup() method
		 */

		for(XID objectId : this.oldModel) {
			XObject object = this.oldModel.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				List<String> list = this.oldIndexer.getIndexStrings(value);
				
				for(String s : list) {
					List<XAddress> adrList = this.oldIndex.search(s);
					
					assertTrue(adrList.contains(object.getAddress()));
				}
			}
		}
	}
	
	@Test
	public void testUpdateIndexObjectAddNewFieldWithValue() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		List<XAddress> oldList = this.oldIndex.search(valueString);
		assertTrue(oldList.isEmpty());
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// add the new value, update index and check whether an entry exists or
		// not
		XID id = XX.createUniqueId();
		XField newField = newJohn.createField(id);
		newField.setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		List<XAddress> newList = this.newIndex.search(valueString);
		assertEquals(1, newList.size());
		assertEquals(newJohn.getAddress(), newList.get(0));
	}
	
	@Test
	public void testUpdateIndexObjectAddValue() {
		testUpdateIndexAddValue(TestType.XOBJECT);
	}
	
	@Test
	public void testUpdateIndexFieldAddValue() {
		testUpdateIndexAddValue(TestType.XFIELD);
	}
	
	@Test
	public void testUpdateIndexEventAddValue() {
		testUpdateIndexAddValue(TestType.XEVENT);
	}
	
	private void testUpdateIndexAddValue(TestType type) {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		List<XAddress> oldList = this.oldIndex.search(valueString);
		assertTrue(oldList.isEmpty());
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// add the new value, update index and check whether an entry exists or
		// not
		XID id = XX.createUniqueId();
		
		XField oldField = oldJohn.createField(id);
		XField newField = newJohn.createField(id);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		newField.setValue(value);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldJohn, newJohn);
			break;
		case XFIELD:
			this.newIndex.updateIndex(newJohn.getAddress(), oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.ADD, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, null);
			break;
		}
		
		List<XAddress> newList = this.newIndex.search(valueString);
		assertEquals(1, newList.size());
		assertEquals(newJohn.getAddress(), newList.get(0));
	}
	
	@Test
	public void testUpdateIndexObjectDeleteValue() {
		testUpdateIndexDeleteValue(TestType.XOBJECT);
	}
	
	@Test
	public void testUpdateIndexFieldDeleteValue() {
		testUpdateIndexDeleteValue(TestType.XFIELD);
	}
	
	@Test
	public void testUpdateIndexEventDeleteValue() {
		testUpdateIndexDeleteValue(TestType.XEVENT);
	}
	
	private void testUpdateIndexDeleteValue(TestType type) {
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Add not yet existing value, update index, remove it again, update
		// index and check whether no entry for this value exists anymore or
		// not
		
		XID testId = X.getIDProvider().createUniqueId();
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XField oldField = oldJohn.createField(testId);
		XField newField = newJohn.createField(testId);
		
		newField.setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		oldField.setValue(value);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		newField.setValue(null);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldJohn, newJohn);
			break;
		case XFIELD:
			this.newIndex.updateIndex(newJohn.getAddress(), oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, value);
			break;
		}
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexObjectDeleteMultipleExistingValue() {
		testUpdateIndexDeleteMultipleExistingValue(TestType.XOBJECT);
	}
	
	@Test
	public void testUpdateIndexFieldDeleteMultipleExistingValue() {
		testUpdateIndexDeleteMultipleExistingValue(TestType.XFIELD);
	}
	
	@Test
	public void testUpdateIndexEventDeleteMultiplyExistingValue() {
		testUpdateIndexDeleteMultipleExistingValue(TestType.XEVENT);
	}
	
	private void testUpdateIndexDeleteMultipleExistingValue(TestType type) {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id1 = XX.createUniqueId();
		XID id2 = XX.createUniqueId();
		
		XField newField1 = newJohn.createField(id1);
		XField newField2 = newJohn.createField(id2);
		
		newField1.setValue(value);
		newField2.setValue(value);
		
		// update index and update the old model, so that it can be used again
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> addresses = this.newIndex.search(s);
			assertEquals(1, addresses.size());
			assertTrue(addresses.contains(newJohn.getAddress()));
		}
		
		XField oldField1 = oldJohn.createField(id1);
		XField oldField2 = oldJohn.createField(id2);
		
		oldField1.setValue(value);
		oldField2.setValue(value);
		
		// Remove value once, update, and check again
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		newField1.setValue(null);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldJohn, newJohn);
			break;
		case XFIELD:
			this.newIndex.updateIndex(newJohn.getAddress(), oldField1, newField1);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField1.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, value);
		}
		
		for(String s : indexStrings) {
			List<XAddress> addresses = this.newIndex.search(s);
			/*
			 * The value should still be indexed, since it existed multiple
			 * times
			 */

			assertEquals(1, addresses.size());
			assertTrue(addresses.contains(newJohn.getAddress()));
		}
		
		oldField1.setValue(null);
		
		// Remove value completely
		
		newField2.setValue(null);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldJohn, newJohn);
			break;
		case XFIELD:
			this.newIndex.updateIndex(newJohn.getAddress(), oldField2, newField2);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField2.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, value);
		}
		
		for(String s : indexStrings) {
			List<XAddress> addresses = this.newIndex.search(s);
			/*
			 * The value should be completely deindexed now
			 */

			assertTrue(addresses.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexObjectChangeValue() {
		testUpdateIndexChangeValue(TestType.XOBJECT);
	}
	
	@Test
	public void testUpdateIndexFieldChangeValue() {
		testUpdateIndexChangeValue(TestType.XFIELD);
	}
	
	@Test
	public void testUpdateIndexEventChangeValue() {
		testUpdateIndexChangeValue(TestType.XEVENT);
	}
	
	private void testUpdateIndexChangeValue(TestType type) {
		String valueString1 = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value1 = X.getValueFactory().createStringValue(valueString1);
		List<String> indexStrings1 = this.newIndexer.getIndexStrings(value1);
		
		String valueString2 = "Anothervaluestringwhichshouldntexistanywhere";
		XValue value2 = X.getValueFactory().createStringValue(valueString2);
		List<String> indexStrings2 = this.newIndexer.getIndexStrings(value2);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		XField oldField = oldJohn.createField(id);
		XField newField = newJohn.createField(id);
		
		newField.setValue(value1);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		oldField.setValue(value1);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		newField.setValue(value2);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldJohn, newJohn);
			break;
		case XFIELD:
			this.newIndex.updateIndex(newJohn.getAddress(), oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.CHANGE, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, value1);
		}
		
		// make sure the old value was deindexed
		for(String s : indexStrings1) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : indexStrings2) {
			List<XAddress> found = this.newIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newJohn.getAddress(), found.get(0));
		}
	}
	
	@Test
	public void testUpdateIndexObjectRemoveField() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		newJohn.createField(id).setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		oldJohn.createField(id).setValue(value);
		
		newJohn.removeField(id);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	@Test
	public void testDeIndexObject() {
		XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		this.newIndex.deIndex(newJohn);
		
		testIfObjectWasDeIndexed(newJohn);
	}
	
	@Test
	public void testDeIndexField() {
		XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		for(XID fieldId : newJohn) {
			XReadableField field = newJohn.getField(fieldId);
			this.newIndex.deIndex(field);
		}
		
		// deindexing all fields equals deindexing the whole object
		testIfObjectWasDeIndexed(newJohn);
	}
	
	@Test
	public void testDeIndexFieldWithAddress() {
		XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		XAddress johnAddress = newJohn.getAddress();
		
		for(XID fieldId : newJohn) {
			XReadableField field = newJohn.getField(fieldId);
			this.newIndex.deIndex(johnAddress, field);
		}
		
		// deindexing all fields equals deindexing the whole object
		testIfObjectWasDeIndexed(newJohn);
	}
	
	private void testIfObjectWasDeIndexed(XReadableObject object) {
		XAddress objectAddress = object.getAddress();
		
		for(XID fieldId : object) {
			XValue value = object.getField(fieldId).getValue();
			List<String> indexStrings = this.newIndexer.getIndexStrings(value);
			
			for(String s : indexStrings) {
				List<XAddress> addresses = this.newIndex.search(s);
				
				assertFalse(addresses.contains(objectAddress));
			}
		}
	}
}
