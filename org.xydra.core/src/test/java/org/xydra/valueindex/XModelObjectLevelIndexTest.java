package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
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
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public abstract class XModelObjectLevelIndexTest {
	
	/**
	 * The basic model, can be used to get the "old" objects etc. to test
	 * updateIndex methods
	 */
	private XModel oldModel;
	
	private XModel newModel;
	
	/*
	 * TODO initialize the index and indexer!
	 */
	private XModelObjectLevelIndex oldIndex;
	private XModelObjectLevelIndex newIndex;
	private XValueIndexer oldIndexer;
	private XValueIndexer newIndexer;
	
	public abstract void initializeIndex(XModel model, XValueIndexer indexer);
	
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
		
		initializeIndex(this.oldModel, this.oldIndexer);
		initializeIndex(this.newModel, this.newIndexer);
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
	public void testUpdateIndexObjectAddValue() {
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
	public void testUpdateIndexObjectDeleteValue() {
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Remove an existing value, update index, and check whether no entry
		// exists any more or not
		XField titleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue value = titleField.getValue();
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		titleField.setValue(null);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexObjectDeleteMultiplyExistingValue() {
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
		newField1.setValue(null);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
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
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
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
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Change an existing value, update index, and check whether no entry
		// exists any more or not
		XField titleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue oldValue = titleField.getValue();
		List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);
		
		titleField.setValue(value);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		// make sure the old value was deindexed
		for(String s : oldIndexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newJohn.getAddress(), found.get(0));
		}
	}
	
	@Test
	public void testUpdateIndexObjectRemoveObject() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		newJohn.createField(id).setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		oldJohn.createField(id).setValue(value);
		
		this.newModel.removeObject(DemoModelUtil.JOHN_ID);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
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
	public void testUpdateIndexFieldAddValue() {
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
		newField.setValue(value);
		
		this.newIndex.updateIndex(newJohn.getAddress(), oldField, newField);
		
		List<XAddress> newList = this.newIndex.search(valueString);
		assertEquals(1, newList.size());
		assertEquals(newJohn.getAddress(), newList.get(0));
	}
	
	@Test
	public void testUpdateIndexFieldDeleteValue() {
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Remove an existing value, update index, and check whether no entry
		// exists any more or not
		XField oldTitleField = oldJohn.getField(DemoModelUtil.TITLE_ID);
		XField newTitleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue value = newTitleField.getValue();
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		newTitleField.setValue(null);
		this.newIndex.updateIndex(newJohn.getAddress(), oldTitleField, newTitleField);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexFieldDeleteMultiplyExistingValue() {
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
		newField1.setValue(null);
		
		this.newIndex.updateIndex(newJohn.getAddress(), oldField1, newField1);
		
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
		
		this.newIndex.updateIndex(newJohn.getAddress(), oldField2, newField2);
		
		for(String s : indexStrings) {
			List<XAddress> addresses = this.newIndex.search(s);
			/*
			 * The value should be completely deindexed now
			 */

			assertTrue(addresses.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexFieldChangeValue() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Change an existing value, update index, and check whether no entry
		// exists any more or not
		XField oldTitleField = oldJohn.getField(DemoModelUtil.TITLE_ID);
		XField newTitleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue oldValue = newTitleField.getValue();
		List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);
		
		newTitleField.setValue(value);
		this.newIndex.updateIndex(newJohn.getAddress(), oldTitleField, newTitleField);
		
		// make sure the old value was deindexed
		for(String s : oldIndexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newJohn.getAddress(), found.get(0));
		}
	}
	
	@Test
	public void testUpdateIndexEventAddValue() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		List<XAddress> oldList = this.oldIndex.search(valueString);
		assertTrue(oldList.isEmpty());
		
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// add the new value, update index and check whether an entry exists or
		// not
		XID id = XX.createUniqueId();
		XField newField = newJohn.createField(id);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		newField.setValue(value);
		
		XEvent event = listener.event;
		
		assertTrue(event instanceof XFieldEvent);
		assertEquals(ChangeType.ADD, event.getChangeType());
		assertEquals(newField.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(event);
		
		List<XAddress> newList = this.newIndex.search(valueString);
		assertEquals(1, newList.size());
		assertEquals(newJohn.getAddress(), newList.get(0));
	}
	
	@Test
	public void testUpdateIndexEventDeleteValue() {
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Remove an existing value, update index, and check whether no entry
		// exists any more or not
		XField titleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue value = titleField.getValue();
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		titleField.setValue(null);
		
		XEvent event = listener.event;
		
		assertTrue(event instanceof XFieldEvent);
		assertEquals(ChangeType.ADD, event.getChangeType());
		assertEquals(titleField.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(event);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexEventDeleteMultiplyExistingValue() {
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
		
		XEvent event = listener.event;
		assertTrue(event instanceof XFieldEvent);
		assertEquals(ChangeType.REMOVE, event.getChangeType());
		assertEquals(newField1.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(event);
		
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
		
		event = listener.event;
		assertTrue(event instanceof XFieldEvent);
		assertEquals(ChangeType.REMOVE, event.getChangeType());
		assertEquals(newField2.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> addresses = this.newIndex.search(s);
			/*
			 * The value should be completely deindexed now
			 */

			assertTrue(addresses.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexEventChangeValue() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Change an existing value, update index, and check whether no entry
		// exists any more or not
		XField titleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue oldValue = titleField.getValue();
		List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		titleField.setValue(value);
		
		XEvent event = listener.event;
		assertTrue(event instanceof XFieldEvent);
		assertEquals(ChangeType.CHANGE, event.getChangeType());
		assertEquals(titleField.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(event);
		
		// make sure the old value was deindexed
		for(String s : oldIndexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newJohn.getAddress(), found.get(0));
		}
	}
	
	@Test
	public void testUpdateIndexEventRemoveObject() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		newJohn.createField(id).setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		oldJohn.createField(id).setValue(value);
		
		DummyModelEventListener listener = new DummyModelEventListener();
		this.newModel.addListenerForModelEvents(listener);
		
		this.newModel.removeObject(DemoModelUtil.JOHN_ID);
		
		XEvent event = listener.event;
		assertTrue(event instanceof XModelEvent);
		assertEquals(ChangeType.REMOVE, event.getChangeType());
		assertEquals(newJohn.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(event);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	@Test
	public void testUpdateIndexEventRemoveField() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		XField newField = newJohn.createField(id);
		newField.setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		oldJohn.createField(id).setValue(value);
		
		DummyObjectEventListener listener = new DummyObjectEventListener();
		newJohn.addListenerForObjectEvents(listener);
		
		newJohn.removeField(id);
		
		XEvent event = listener.event;
		assertTrue(event instanceof XObjectEvent);
		assertEquals(ChangeType.REMOVE, event.getChangeType());
		assertEquals(newField.getAddress(), event.getChangedEntity());
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
}
