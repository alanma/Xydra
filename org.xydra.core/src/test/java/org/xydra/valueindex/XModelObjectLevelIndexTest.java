package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.xydra.index.query.Pair;


/**
 * An abstract test for testing {@link XModelObjectLevelIndex}. The phonebook
 * model from {@link DemoModelUtil} is used as the basic source of data.
 * 
 * @author Kaidel
 * 
 */

public abstract class XModelObjectLevelIndexTest {
	
	private enum TestType {
		XOBJECT, XFIELD, XEVENT;
	}
	
	/**
	 * The basic model. Most updateIndex methods need an old instance of a model
	 * or object and the newest instance to compute what exactly needs to be
	 * updated. This "oldModel" can be used to get the "old" objects etc. to
	 * test updateIndex methods.
	 * 
	 * Both oldModel and newModel will contain the same data during the tests,
	 * but newModel will be used to change values etc., whereas oldModel will
	 * remain in the old state, so that it can be used in the
	 * updateIndex-methods as described above.
	 */
	public XModel oldModel;
	public XModel newModel;
	
	/**
	 * oldIndex is the index used for indexing the oldModel, newIndex the index
	 * used for indexing the newModel.
	 */
	public XModelObjectLevelIndex oldIndex;
	public XModelObjectLevelIndex newIndex;
	
	/**
	 * oldIndexer is used by oldIndex, newIndexer by newIndex
	 */
	public XValueIndexer oldIndexer;
	public XValueIndexer newIndexer;
	
	/**
	 * Abstract method which initializes the indexes in the setup-method.
	 * 
	 * Needs to be overwritten! {@link XModelObjectLevelIndexTest#oldIndex}
	 * needs to be parameterized with
	 * {@link XModelObjectLevelIndexTest#oldModel} (as the model which it
	 * indexes, index(oldModel) needs to be executed!) and
	 * {@link XModelObjectLevelIndexTest#oldIndexer} (as the used indexer),
	 * {@link XModelObjectLevelIndexTest#newIndex} (as the model which it
	 * indexes, index(newModel) needs to be executed!) needs to be parameterized
	 * with {@link XModelObjectLevelIndexTest#newModel} (as the used indexer)
	 * and {@link XModelObjectLevelIndexTest#newIndexer}, otherwise the test
	 * won't work correctly.
	 */
	public abstract void initializeIndexes();
	
	/**
	 * Abstract method which initializes the indexers in the setup-method.
	 * 
	 * Needs to be overwritten, otherwise the test won't work correctly.
	 */
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
		
		initializeIndexes();
	}
	
	private static Set<XAddress> getAddressesFromSetOfPairs(Set<Pair<XAddress,XValue>> pairs) {
		HashSet<XAddress> addresses = new HashSet<XAddress>();
		
		for(Pair<XAddress,XValue> pair : pairs) {
			addresses.add(pair.getFirst());
		}
		
		return addresses;
	}
	
	/**
	 * Tests whether indexing an XModel works correctly.
	 * 
	 * Note: index(oldModel) and index(newModel) were already called in the
	 * setup() by initializing the indexes.
	 */
	@Test
	public void testIndexingXModel() {
		
		for(XID objectId : this.oldModel) {
			XObject object = this.oldModel.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				List<String> list = this.oldIndexer.getIndexStrings(value);
				
				for(String s : list) {
					Set<Pair<XAddress,XValue>> pairs = this.oldIndex.search(s);
					Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
					
					assertTrue(addresses.contains(object.getAddress()));
				}
			}
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a new field with a new value gets added to the
	 * object.
	 */
	@Test
	public void testUpdateIndexObjectAddNewFieldWithValue() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		Set<Pair<XAddress,XValue>> oldSet = this.oldIndex.search(valueString);
		assertTrue(oldSet.isEmpty());
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// add the new value, update index and check whether an entry exists or
		// not
		XID id = XX.createUniqueId();
		XField newField = newJohn.createField(id);
		newField.setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		Set<Pair<XAddress,XValue>> newList = this.newIndex.search(valueString);
		assertEquals(1, newList.size());
		assertEquals(newJohn.getAddress(), newList.iterator().next().getFirst());
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexObjectAddValue() {
		testUpdateIndexAddValue(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexFieldAddValue() {
		testUpdateIndexAddValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexEventAddValue() {
		testUpdateIndexAddValue(TestType.XEVENT);
	}
	
	private void testUpdateIndexAddValue(TestType type) {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		Set<Pair<XAddress,XValue>> oldSet = this.oldIndex.search(valueString);
		assertTrue(oldSet.isEmpty());
		
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
			this.newIndex.updateIndex(oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.ADD, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, null);
			break;
		}
		
		Set<Pair<XAddress,XValue>> newSet = this.newIndex.search(valueString);
		assertEquals(1, newSet.size());
		assertEquals(newJohn.getAddress(), newSet.iterator().next().getFirst());
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexObjectDeleteValue() {
		testUpdateIndexDeleteValue(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexFieldDeleteValue() {
		testUpdateIndexDeleteValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 */
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
			this.newIndex.updateIndex(oldField, newField);
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
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexObjectDeleteMultipleExistingValue() {
		testUpdateIndexDeleteMultipleExistingValue(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 */
	@Test
	public void testUpdateIndexFieldDeleteMultipleExistingValue() {
		testUpdateIndexDeleteMultipleExistingValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 */
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
			Set<Pair<XAddress,XValue>> pairs = this.newIndex.search(s);
			
			Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
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
			this.newIndex.updateIndex(oldField1, newField1);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField1.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, value);
		}
		
		for(String s : indexStrings) {
			Set<Pair<XAddress,XValue>> pairs = this.newIndex.search(s);
			Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
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
			this.newIndex.updateIndex(oldField2, newField2);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField2.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, value);
		}
		
		for(String s : indexStrings) {
			Set<Pair<XAddress,XValue>> pairs = this.newIndex.search(s);
			Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
			/*
			 * The value should be completely deindexed now
			 */
			
			assertTrue(addresses.isEmpty());
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 */
	@Test
	public void testUpdateIndexObjectChangeValue() {
		testUpdateIndexChangeValue(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 */
	@Test
	public void testUpdateIndexFieldChangeValue() {
		testUpdateIndexChangeValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 */
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
			this.newIndex.updateIndex(oldField, newField);
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
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : indexStrings2) {
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newJohn.getAddress(), found.iterator().next().getFirst());
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a field an object gets remove, which already existed
	 * in the old state.
	 */
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
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#deIndex(XReadableObject)} works
	 * correctly.
	 */
	@Test
	public void testDeIndexObject() {
		XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		this.newIndex.deIndex(newJohn);
		
		testIfObjectWasDeIndexed(newJohn);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#deIndex(XReadableField)} works
	 * correctly.
	 */
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
	
	private void testIfObjectWasDeIndexed(XReadableObject object) {
		XAddress objectAddress = object.getAddress();
		
		for(XID fieldId : object) {
			XValue value = object.getField(fieldId).getValue();
			List<String> indexStrings = this.newIndexer.getIndexStrings(value);
			
			for(String s : indexStrings) {
				Set<Pair<XAddress,XValue>> pairs = this.newIndex.search(s);
				Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
				
				assertFalse(addresses.contains(objectAddress));
			}
		}
	}
}
