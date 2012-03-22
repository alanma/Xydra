package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
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
	
	/**
	 * see description of oldModel
	 */
	public XModel newModel;
	
	/**
	 * Has almost the same role as oldModel, but will be used to test an Index
	 * which excludes almost all fields from indexing. See description of
	 * oldModel for more information.
	 * 
	 * newExcludeAllModel has the same role for oldExcludeAllModel as newModel
	 * for oldModel.
	 */
	public XModel oldExcludeAllModel;
	
	/**
	 * see description of oldExcludeAllModel.
	 */
	public XModel newExcludeAllModel;
	
	/**
	 * oldIndex is the index used for indexing the oldModel, newIndex the index
	 * used for indexing the newModel.
	 * 
	 * defaultIncludeAll needs to be set to true and at least one ID in
	 * excludedFieldIds must be provided. includeFieldIds should be an empty
	 * set.
	 */
	public XModelObjectLevelIndex oldIndex;
	public XModelObjectLevelIndex newIndex;
	
	/**
	 * oldExcludeallIndex is the index used for indexing the oldExcludeAllModel,
	 * newExcludeAllIndex the index used for indexing the newExcludeAllModel.
	 * 
	 * defaultIncludeAll needs to be set to true and at least one ID in
	 * excludedFieldIds must be provided. includeFieldIds should be an empty
	 * set.
	 */
	public XModelObjectLevelIndex oldExcludeAllIndex;
	public XModelObjectLevelIndex newExcludeAllIndex;
	
	/**
	 * oldIndexer is used by oldIndex, newIndexer by newIndex
	 */
	public XValueIndexer oldIndexer;
	public XValueIndexer newIndexer;
	
	/**
	 * oldExcludeAllIndexer is used by oldExcludeAllIndex, newExcludeAllIndexer
	 * by newExcludeAllIndex
	 */
	public XValueIndexer oldExcludeAllIndexer;
	public XValueIndexer newExcludeAllIndexer;
	
	/**
	 * a set of Ids which are excluded from being indexed by oldIndex/newIndex
	 */
	public Set<XID> excludedIds;
	
	/**
	 * Id of an object holding fields with ids in excludedIds.
	 */
	public XID excludedObjectId;
	
	/**
	 * String which only exists as a value in fields which Id is in excludedIds.
	 */
	public String excludedValueString;
	
	/**
	 * a set of Ids which will be indexed by
	 * oldExcludedAllIndex/newExcludeAllIndex. Only fields which Id are in this
	 * set will be indexed by theses indexes.
	 */
	public Set<XID> includedIds;
	
	/**
	 * the id of an object holding fields with ids in includedIds.
	 */
	public XID includedObjectId;
	
	/**
	 * String which only exists as a value in fields which Id is in includedIds.
	 */
	public String includedValueString;
	
	/**
	 * Method which initializes the indexes in the setup-method.
	 * 
	 * You need to adhere to the following guidelines if you want to overwrite
	 * this method, otherwise the tests won't work correctly.
	 * 
	 * {@link XModelObjectLevelIndexTest#oldIndex} needs to be parameterized
	 * with {@link XModelObjectLevelIndexTest#oldModel} (as the model which it
	 * indexes, index(oldModel) needs to be executed!) and
	 * {@link XModelObjectLevelIndexTest#oldIndexer} (as the used indexer).
	 * {@link XModelObjectLevelIndexTest#newIndex} needs to be parameterized
	 * with {@link XModelObjectLevelIndexTest#newModel} (as the model which it
	 * indexes, index(newModel) needs to be executed!) and with
	 * {@link XModelObjectLevelIndexTest#newIndexer} (as the used indexer).
	 * defaultIncludeAll needs to be set to true, includeFieldIds should be an
	 * empty set. excludeFieldIds has to only include {@link XID XIDs} which are
	 * NOT used in the phonebook model of {@link DemoModelUtil} (if you include
	 * such {@link XID XIDs}, the test will not work correctly), it must not be
	 * empty, at least one such Id must be provided. Furthermore,
	 * {@link XModelObjectLevelIndexTest#excludedIds} needs to include the same
	 * {@link XID XIDs} as excludeFieldIds.
	 * 
	 * {@link XModelObjectLevelIndexTest#oldExcludeAllIndex} needs to be
	 * parameterized with {@link XModelObjectLevelIndexTest#oldExcludeAllModel}
	 * (as the model which it indexes, index(oldExcludeAllModel) needs to be
	 * executed!) and {@link XModelObjectLevelIndexTest#oldExcludeAllIndexer}
	 * (as the used indexer).
	 * {@link XModelObjectLevelIndexTest#newExcludeAllIndex} needs to be
	 * parameterized with {@link XModelObjectLevelIndexTest#newExcludeAllModel}
	 * (as the model which it indexes, index(newExcludeAllModel) needs to be
	 * executed!) and with {@link XModelObjectLevelIndexTest#newIndexer} (as the
	 * used indexer). defaultIncludeAll needs to be set to false,
	 * excludeFieldIds should be an empty set. includeFieldIds has to only
	 * include {@link XID XIDs} which are NOT used in the phonebook model of
	 * {@link DemoModelUtil} (if you include such {@link XID XIDs}, the test
	 * will not work correctly), it must not be empty, at least two such Ids
	 * must be provided. Furthermore,
	 * {@link XModelObjectLevelIndexTest#includedIds} needs to include the same
	 * {@link XID XIDs} as includeFieldIds.
	 */
	public void initializeIndexes() {
		HashSet<XID> emptySet = new HashSet<XID>();
		
		// oldModel, oldIndexer, newModel, newIndexer, excludedIds and
		// includedIds need to be set before calling all this!
		this.oldIndex = new XModelObjectLevelIndex(this.oldModel, this.oldIndexer, true, emptySet,
		        this.excludedIds);
		this.newIndex = new XModelObjectLevelIndex(this.newModel, this.newIndexer, true, emptySet,
		        this.excludedIds);
		
		this.oldExcludeAllIndex = new XModelObjectLevelIndex(this.oldExcludeAllModel,
		        this.oldExcludeAllIndexer, false, this.includedIds, emptySet);
		this.newExcludeAllIndex = new XModelObjectLevelIndex(this.newExcludeAllModel,
		        this.newExcludeAllIndexer, false, this.includedIds, emptySet);
	}
	
	/**
	 * Abstract method which initializes the indexers in the setup-method.
	 * 
	 * Needs to be overwritten, otherwise the test won't work correctly.
	 */
	public abstract void initializeIndexers();
	
	public void initializeExcludedAndIncludedIds() {
		this.excludedIds = new HashSet<XID>();
		this.includedIds = new HashSet<XID>();
		for(int i = 0; i < 13; i++) {
			this.excludedIds.add(XX.createUniqueId());
			this.includedIds.add(XX.createUniqueId());
		}
	}
	
	@Before
	public void setup() {
		XID actorId = XX.createUniqueId();
		
		XRepository repo1 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo1);
		
		this.oldModel = repo1.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XRepository repo2 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo2);
		
		this.newModel = repo2.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XRepository repo3 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo3);
		
		this.oldExcludeAllModel = repo3.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XRepository repo4 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo4);
		
		this.newExcludeAllModel = repo4.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		initializeExcludedAndIncludedIds();
		
		this.excludedObjectId = XX.createUniqueId();
		XObject oldExcludedObject = this.oldModel.createObject(this.excludedObjectId);
		XObject newExcludedObject = this.newModel.createObject(this.excludedObjectId);
		
		this.excludedValueString = "thisvalueisexcludedfromindexingbecauseitonlyexistsinexcludedfields";
		
		assertNotNull("excludedIds must not be null!", this.excludedIds);
		assertFalse("excludedIds needs to contain at least one Id!", this.excludedIds.isEmpty());
		
		for(XID id : this.excludedIds) {
			XField oldField = oldExcludedObject.createField(id);
			XField newField = newExcludedObject.createField(id);
			
			XValue value = X.getValueFactory().createStringValue(this.excludedValueString);
			
			oldField.setValue(value);
			newField.setValue(value);
		}
		
		this.includedObjectId = XX.createUniqueId();
		XObject oldIncludedObject = this.oldExcludeAllModel.createObject(this.includedObjectId);
		XObject newIncludedObject = this.newExcludeAllModel.createObject(this.includedObjectId);
		
		this.includedValueString = "thisvalueisincludedeventhoughweexcludealmosteverything";
		
		assertNotNull("includedIds must not be null", this.includedIds);
		assertFalse("includedIds needs to contain at least two Ids!", this.includedIds.size() < 2);
		
		for(XID id : this.includedIds) {
			XField oldField = oldIncludedObject.createField(id);
			XField newField = newIncludedObject.createField(id);
			
			XValue value = X.getValueFactory().createStringValue(this.includedValueString);
			
			oldField.setValue(value);
			newField.setValue(value);
		}
		
		initializeIndexers();
		
		initializeIndexes();
		
	}
	
	/**
	 * Returns the set of {@link XAddress XAddresses} implicitly stored in the
	 * given set of {@link Pair Pairs} of {@link XAddress} and {@link XValue}
	 * 
	 * @param pairs
	 * @return the set of {@link XAddress XAddresses} implicitly stored in the
	 *         given set of {@link Pair Pairs} of {@link XAddress} and
	 *         {@link XValue}
	 */
	private static Set<XAddress> getAddressesFromSetOfPairs(Set<Pair<XAddress,XValue>> pairs) {
		HashSet<XAddress> addresses = new HashSet<XAddress>();
		
		for(Pair<XAddress,XValue> pair : pairs) {
			addresses.add(pair.getFirst());
		}
		
		return addresses;
	}
	
	/**
	 * Tests whether indexing an XModel works correctly when defaultIncludeAll
	 * is set to true.
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
					
					if(this.excludedIds.contains(fieldId)) {
						/**
						 * the value of the current field was excluded from
						 * indexing. Since all theses fields have a the same
						 * value which should not exist anywhere else, the
						 * returned set of pairs should be empty.
						 */
						assertTrue(pairs.isEmpty());
					} else {
						Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
						assertTrue(addresses.contains(object.getAddress()));
					}
				}
			}
		}
	}
	
	/**
	 * Tests whether indexing an XModel works correctly when defaultIncludeAll
	 * is set to false.
	 * 
	 * Note: index(oldExcludeAllModel) and index(newExcludeAllModel) were
	 * already called in the setup() by initializing the indexes.
	 */
	@Test
	public void testIndexingExcludeAllModel() {
		
		for(XID objectId : this.oldExcludeAllModel) {
			XObject object = this.oldExcludeAllModel.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				List<String> list = this.oldExcludeAllIndexer.getIndexStrings(value);
				
				for(String s : list) {
					Set<Pair<XAddress,XValue>> pairs = this.oldExcludeAllIndex.search(s);
					
					if(this.includedIds.contains(fieldId)) {
						Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
						assertTrue(addresses.contains(object.getAddress()));
					} else {
						/**
						 * almost all fields were excluded from indexing.
						 */
						assertTrue(pairs.isEmpty());
					}
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
		
		Set<Pair<XAddress,XValue>> set = this.newIndex.search(valueString);
		assertEquals(1, set.size());
		assertEquals(newJohn.getAddress(), set.iterator().next().getFirst());
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a new field with a new value and gets added to the
	 * object, where the {@link XID} of the field is one of the Ids of fields
	 * the index will not index (i.e. the field is excluded from being indexed).
	 */
	@Test
	public void testUpdateIndexObjectAddNewFieldWithValueAndExcludedId() {
		// Tests adding a field with an ID in excludedIds (should not be
		// indexed)
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = this.excludedIds.iterator().next();
		XField newField = newJohn.createField(id);
		XValue value = X.getValueFactory().createStringValue(this.excludedValueString);
		newField.setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		Set<Pair<XAddress,XValue>> set = this.newIndex.search(this.excludedValueString);
		assertTrue(set.isEmpty());
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a new field with a new value gets added to the
	 * object, where the index excludes almost all fields from being indexed
	 * (i.e. defaultIncludeAll is set to false).
	 */
	@Test
	public void testUpdateIndexObjectAddNewFieldWithValueInExcludeAllModel() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		Set<Pair<XAddress,XValue>> oldSet = this.oldExcludeAllIndex.search(valueString);
		assertTrue(oldSet.isEmpty());
		
		XObject oldJohn = this.oldExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		
		// add the new value, update index and check whether an entry exists or
		// not
		XID fieldId;
		do {
			fieldId = XX.createUniqueId();
		} while(this.includedIds.contains(fieldId));
		
		XField newField = newJohn.createField(fieldId);
		newField.setValue(value);
		
		this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
		
		Set<Pair<XAddress,XValue>> set = this.newExcludeAllIndex.search(valueString);
		assertTrue(set.isEmpty());
		
		// Add a value to a field with ID in includedIds ( = should be indexed)
		
		XID objectId = XX.createUniqueId();
		XObject oldObject = this.oldExcludeAllModel.createObject(objectId);
		XObject newObject = this.newExcludeAllModel.createObject(objectId);
		
		fieldId = this.includedIds.iterator().next();
		newField = newObject.createField(fieldId);
		newField.setValue(value);
		
		this.newExcludeAllIndex.updateIndex(oldObject, newObject);
		
		set = this.newExcludeAllIndex.search(valueString);
		assertEquals(1, set.size());
		assertEquals(newObject.getAddress(), set.iterator().next().getFirst());
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testUpdateIndexFieldAddValue() {
		testUpdateIndexAddValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state and which is excluded from being
	 * indexed.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true, but
	 * has some fields which are excluded from being indexed.
	 */
	@Test
	public void testUpdateIndexObjectAddValueToExcludedField() {
		this.testUpdateIndexAddValueToExcludedField(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state and which is excluded from being
	 * indexed.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true, but
	 * has some fields which are excluded from being indexed.
	 */
	@Test
	public void testUpdateIndexFieldAddValueToExcludedField() {
		this.testUpdateIndexAddValueToExcludedField(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state and which is excluded from being
	 * indexed.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true, but
	 * has some fields which are excluded from being indexed.
	 */
	@Test
	public void testUpdateIndexEventAddValueToExcludedField() {
		this.testUpdateIndexAddValueToExcludedField(TestType.XEVENT);
	}
	
	private void testUpdateIndexAddValueToExcludedField(TestType type) {
		XID id = XX.createUniqueId();
		XID fieldId = this.excludedIds.iterator().next();
		
		XObject oldExcludedObject = this.oldModel.createObject(id);
		XField oldExcludedField = oldExcludedObject.createField(fieldId);
		
		XObject newExcludedObject = this.newModel.createObject(id);
		XField newExcludedField = newExcludedObject.createField(fieldId);
		
		XValue value = X.getValueFactory().createStringValue(this.excludedValueString);
		List<String> newIndexStrings = this.newIndexer.getIndexStrings(value);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newExcludedObject.addListenerForFieldEvents(listener);
		
		newExcludedField.setValue(value);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldExcludedObject, newExcludedObject);
			break;
		case XFIELD:
			this.newIndex.updateIndex(oldExcludedField, newExcludedField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.ADD, event.getChangeType());
			assertEquals(newExcludedField.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, null);
		}
		
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false,
	 * but has some fields which are still being indexed (i.e. the
	 * includedFieldIds set is not empty). Tests both the cases when a value of
	 * a field, which is not being indexed, is added and when a value of field
	 * which IS being index is added.
	 */
	@Test
	public void testUpdateIndexObjectAddValueToFieldOfExcludeAllModel() {
		this.testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false,
	 * but has some fields which are still being indexed (i.e. the
	 * includedFieldIds set is not empty). Tests both the cases when a value of
	 * a field, which is not being indexed, is added and when a value of field
	 * which IS being index is added.
	 */
	@Test
	public void testUpdateIndexFieldAddValueToFieldOfExcludeAllModel() {
		this.testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a new value gets added to a field of an object,
	 * which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false,
	 * but has some fields which are still being indexed (i.e. the
	 * includedFieldIds set is not empty). Tests both the cases when a value of
	 * a field, which is not being indexed, is added and when a value of field
	 * which IS being index is added.
	 */
	@Test
	public void testUpdateIndexEventAddValueToFieldOfExcludeAllModel() {
		this.testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType.XEVENT);
	}
	
	private void testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType type) {
		XID id = XX.createUniqueId();
		XID fieldId = XX.createUniqueId();
		
		XObject oldExcludedObject = this.oldExcludeAllModel.createObject(id);
		XField oldExcludedField = oldExcludedObject.createField(fieldId);
		
		XObject newExcludedObject = this.newExcludeAllModel.createObject(id);
		XField newExcludedField = newExcludedObject.createField(fieldId);
		
		XValue value = X.getValueFactory().createStringValue(this.excludedValueString);
		List<String> newIndexStrings = this.newIndexer.getIndexStrings(value);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newExcludedObject.addListenerForFieldEvents(listener);
		
		newExcludedField.setValue(value);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldExcludedObject, newExcludedObject);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldExcludedField, newExcludedField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.ADD, event.getChangeType());
			assertEquals(newExcludedField.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, null);
		}
		
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newExcludeAllIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// add value to field which is not exlude (i.e. which id is in
		// includedIds)
		XID includedId = this.includedIds.iterator().next();
		
		XField oldIncludedField = oldExcludedObject.createField(includedId);
		XField newIncludedField = newExcludedObject.createField(includedId);
		
		DummyFieldEventListener listener2 = new DummyFieldEventListener();
		newExcludedObject.addListenerForFieldEvents(listener2);
		
		newIncludedField.setValue(value);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldExcludedObject, newExcludedObject);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldIncludedField, newIncludedField);
			break;
		case XEVENT:
			XEvent event = listener2.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.ADD, event.getChangeType());
			assertEquals(newIncludedField.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, null);
		}
		
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newExcludeAllIndex.search(s);
			assertFalse(found.isEmpty());
			assertEquals(1, found.size());
			assertEquals(newExcludedObject.getAddress(), found.iterator().next().getFirst());
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testUpdateIndexFieldDeleteValue() {
		testUpdateIndexDeleteValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexObjectDeleteValueFromExcludeAllModel() {
		testUpdateIndexDeleteValueFromExcludeAllModel(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexFieldDeleteValueFromExcludeAllModel() {
		testUpdateIndexDeleteValueFromExcludeAllModel(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value, which only existed once, gets removed from
	 * a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexEventDeleteValueFromExcludeAllModel() {
		testUpdateIndexDeleteValueFromExcludeAllModel(TestType.XEVENT);
	}
	
	private void testUpdateIndexDeleteValueFromExcludeAllModel(TestType type) {
		XID objectId = XX.createUniqueId();
		XObject oldObject = this.oldExcludeAllModel.createObject(objectId);
		XObject newObject = this.newExcludeAllModel.createObject(objectId);
		
		// Add not yet existing value, update index, remove it again, update
		// index and check whether no entry for this value exists anymore or
		// not
		
		XID includedId = this.includedIds.iterator().next();
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XField oldField = oldObject.createField(includedId);
		XField newField = newObject.createField(includedId);
		
		newField.setValue(value);
		
		this.newExcludeAllIndex.updateIndex(oldObject, newObject);
		
		oldField.setValue(value);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newObject.addListenerForFieldEvents(listener);
		
		newField.setValue(null);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldObject, newObject);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, value);
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
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testUpdateIndexFieldDeleteMultipleExistingValue() {
		testUpdateIndexDeleteMultipleExistingValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexObjectDeleteMultipleExistingValueFromExcludeAllModel() {
		testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexFieldDeleteMultipleExistingValueFromExcludeAllModel() {
		testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value, which existed multiple times, gets removed
	 * from a field of an object, which already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexEventDeleteMultipleExistingValueFromExcludeAllModel() {
		testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType.XEVENT);
	}
	
	private void testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType type) {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		XID objectId = XX.createUniqueId();
		XObject oldObject = this.oldModel.createObject(objectId);
		XObject newObject = this.newModel.createObject(objectId);
		
		Iterator<XID> iterator = this.includedIds.iterator();
		XID id1 = iterator.next();
		XID id2 = iterator.next();
		
		XField newField1 = newObject.createField(id1);
		XField newField2 = newObject.createField(id2);
		
		newField1.setValue(value);
		newField2.setValue(value);
		
		// update index and update the old model, so that it can be used again
		this.newExcludeAllIndex.updateIndex(oldObject, newObject);
		
		for(String s : indexStrings) {
			Set<Pair<XAddress,XValue>> pairs = this.newExcludeAllIndex.search(s);
			
			Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
			assertEquals(1, addresses.size());
			assertTrue(addresses.contains(newObject.getAddress()));
		}
		
		XField oldField1 = oldObject.createField(id1);
		XField oldField2 = oldObject.createField(id2);
		
		oldField1.setValue(value);
		oldField2.setValue(value);
		
		// Remove value once, update, and check again
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newObject.addListenerForFieldEvents(listener);
		
		newField1.setValue(null);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldObject, newObject);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldField1, newField1);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField1.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, value);
		}
		
		for(String s : indexStrings) {
			Set<Pair<XAddress,XValue>> pairs = this.newExcludeAllIndex.search(s);
			Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
			/*
			 * The value should still be indexed, since it existed multiple
			 * times
			 */
			
			assertEquals(1, addresses.size());
			assertTrue(addresses.contains(newObject.getAddress()));
		}
		
		oldField1.setValue(null);
		
		// Remove value completely
		
		newField2.setValue(null);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldObject, newObject);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldField2, newField2);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.REMOVE, event.getChangeType());
			assertEquals(newField2.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, value);
		}
		
		for(String s : indexStrings) {
			Set<Pair<XAddress,XValue>> pairs = this.newExcludeAllIndex.search(s);
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
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testUpdateIndexFieldChangeValue() {
		testUpdateIndexChangeValue(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testUpdateIndexEventChangeValue() {
		testUpdateIndexChangeValue(TestType.XEVENT);
	}
	
	private void testUpdateIndexChangeValue(TestType type) {
		String valueString1 = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue oldValue = X.getValueFactory().createStringValue(valueString1);
		List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);
		
		String valueString2 = "Anothervaluestringwhichshouldntexistanywhere";
		XValue newValue = X.getValueFactory().createStringValue(valueString2);
		List<String> newIndexStrings = this.newIndexer.getIndexStrings(newValue);
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		XField oldField = oldJohn.createField(id);
		XField newField = newJohn.createField(id);
		
		newField.setValue(oldValue);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		oldField.setValue(oldValue);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		newField.setValue(newValue);
		
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
			
			this.newIndex.updateIndex((XFieldEvent)event, oldValue);
		}
		
		// make sure the old value was deindexed
		for(String s : oldIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newJohn.getAddress(), found.iterator().next().getFirst());
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true, but
	 * contains fields which are being excluded from indexing. This tests
	 * whether if the method behaves correctly if the value of such a field is
	 * changed.
	 */
	@Test
	public void testUpdateIndexObjectChangeValueOfExcludedField() {
		testUpdateIndexChangeValueOfExcludedField(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true, but
	 * contains fields which are being excluded from indexing. This tests
	 * whether if the method behaves correctly if the value of such a field is
	 * changed.
	 */
	@Test
	public void testUpdateIndexFieldChangeValueOfExcludedField() {
		testUpdateIndexChangeValueOfExcludedField(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true, but
	 * contains fields which are being excluded from indexing. This tests
	 * whether if the method behaves correctly if the value of such a field is
	 * changed.
	 */
	@Test
	public void testUpdateIndexEventChangeValueOfExcludedField() {
		testUpdateIndexChangeValueOfExcludedField(TestType.XEVENT);
	}
	
	private void testUpdateIndexChangeValueOfExcludedField(TestType type) {
		XID fieldId = this.excludedIds.iterator().next();
		
		XObject oldExcludedObject = this.oldModel.getObject(this.excludedObjectId);
		XField oldExcludedField = oldExcludedObject.getField(fieldId);
		
		XObject newExcludedObject = this.newModel.getObject(this.excludedObjectId);
		XField newExcludedField = newExcludedObject.getField(fieldId);
		
		XValue oldValue = X.getValueFactory().createStringValue(this.excludedValueString);
		XValue newValue = X.getValueFactory().createStringValue(
		        this.excludedValueString + "addingsomethingnew");
		List<String> newIndexStrings = this.newIndexer.getIndexStrings(newValue);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newExcludedObject.addListenerForFieldEvents(listener);
		
		newExcludedField.setValue(newValue);
		
		switch(type) {
		case XOBJECT:
			this.newIndex.updateIndex(oldExcludedObject, newExcludedObject);
			break;
		case XFIELD:
			this.newIndex.updateIndex(oldExcludedField, newExcludedField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.CHANGE, event.getChangeType());
			assertEquals(newExcludedField.getAddress(), event.getChangedEntity());
			
			this.newIndex.updateIndex((XFieldEvent)event, oldValue);
		}
		
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false,
	 * but has some fields which are still being indexed (i.e. the
	 * includedFieldIds set is not empty). Tests both the cases when a value of
	 * a field, which is not being indexed, is changed and when a value of field
	 * which IS being index is changed.
	 */
	@Test
	public void testUpdateIndexObjectChangeValueInExcludeAllModel() {
		testUpdateIndexChangeValueInExcludeAllModel(TestType.XOBJECT);
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableField, XReadableField)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false,
	 * but has some fields which are still being indexed (i.e. the
	 * includedFieldIds set is not empty). Tests both the cases when a value of
	 * a field, which is not being indexed, is changed and when a value of field
	 * which IS being index is changed.
	 */
	@Test
	public void testUpdateIndexFieldChangeValueInExcludeAllModel() {
		testUpdateIndexChangeValueInExcludeAllModel(TestType.XFIELD);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#updateIndex(XFieldEvent, XValue)}
	 * works correctly when a value of a field of an object gets changed, which
	 * already existed in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false,
	 * but has some fields which are still being indexed (i.e. the
	 * includedFieldIds set is not empty). Tests both the cases when a value of
	 * a field, which is not being indexed, is changed and when a value of field
	 * which IS being index is changed.
	 */
	@Test
	public void testUpdateIndexEventChangeValueInExcludeAllModel() {
		testUpdateIndexChangeValueInExcludeAllModel(TestType.XEVENT);
	}
	
	private void testUpdateIndexChangeValueInExcludeAllModel(TestType type) {
		// change value of field which is excluded from indexing
		
		String valueString1 = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue oldValue = X.getValueFactory().createStringValue(valueString1);
		List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);
		
		String valueString2 = "Anothervaluestringwhichshouldntexistanywhere";
		XValue newValue = X.getValueFactory().createStringValue(valueString2);
		List<String> newIndexStrings = this.newIndexer.getIndexStrings(newValue);
		
		XObject oldJohn = this.oldExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = X.getIDProvider().createUniqueId();
		XField oldField = oldJohn.createField(id);
		XField newField = newJohn.createField(id);
		
		newField.setValue(oldValue);
		this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
		
		oldField.setValue(oldValue);
		
		DummyFieldEventListener listener = new DummyFieldEventListener();
		newJohn.addListenerForFieldEvents(listener);
		
		newField.setValue(newValue);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.CHANGE, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, oldValue);
		}
		
		// make sure that nothing happened
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newExcludeAllIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// change value of field which is indexed
		
		XID objectId = XX.createUniqueId();
		XObject oldObject = this.oldExcludeAllModel.createObject(objectId);
		XObject newObject = this.newExcludeAllModel.createObject(objectId);
		
		id = this.includedIds.iterator().next();
		oldField = oldObject.createField(id);
		newField = newObject.createField(id);
		
		newField.setValue(oldValue);
		this.newExcludeAllIndex.updateIndex(oldObject, newObject);
		
		oldField.setValue(oldValue);
		
		listener = new DummyFieldEventListener();
		newObject.addListenerForFieldEvents(listener);
		
		newField.setValue(newValue);
		
		switch(type) {
		case XOBJECT:
			this.newExcludeAllIndex.updateIndex(oldObject, newObject);
			break;
		case XFIELD:
			this.newExcludeAllIndex.updateIndex(oldField, newField);
			break;
		case XEVENT:
			XEvent event = listener.event;
			assertTrue(event instanceof XFieldEvent);
			assertEquals(ChangeType.CHANGE, event.getChangeType());
			assertEquals(newField.getAddress(), event.getChangedEntity());
			
			this.newExcludeAllIndex.updateIndex((XFieldEvent)event, oldValue);
		}
		
		// make sure the old value was deindexed
		for(String s : oldIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newExcludeAllIndex.search(s);
			assertTrue(found.isEmpty());
		}
		
		// make sure the new value was indexed
		for(String s : newIndexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newExcludeAllIndex.search(s);
			assertEquals(1, found.size());
			assertEquals(newObject.getAddress(), found.iterator().next().getFirst());
		}
	}
	
	/**
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a field an object gets remove, which already existed
	 * in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
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
	 * Tests if
	 * {@link XModelObjectLevelIndex#updateIndex(XReadableObject, XReadableObject)}
	 * works correctly when a field an object gets remove, which already existed
	 * in the old state.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testUpdateIndexObjectRemoveFieldOfExcludeAllModel() {
		String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
		XValue value = X.getValueFactory().createStringValue(valueString);
		List<String> indexStrings = this.newExcludeAllIndexer.getIndexStrings(value);
		
		XObject oldJohn = this.oldExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		
		XID id = this.includedIds.iterator().next();
		newJohn.createField(id).setValue(value);
		
		this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
		oldJohn.createField(id).setValue(value);
		
		newJohn.removeField(id);
		this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			Set<Pair<XAddress,XValue>> found = this.newExcludeAllIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#deIndex(XReadableObject)} works
	 * correctly.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testDeIndexObject() {
		XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		this.newIndex.deIndex(newJohn);
		
		testIfObjectWasDeIndexed(newJohn, this.newIndex, this.newIndexer);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#deIndex(XReadableObject)} works
	 * correctly.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testDeIndexObjectOfExcludeAllModel() {
		XReadableObject includedObject = this.newExcludeAllModel.getObject(this.includedObjectId);
		this.newExcludeAllIndex.deIndex(includedObject);
		
		testIfObjectWasDeIndexed(includedObject, this.newExcludeAllIndex, this.newExcludeAllIndexer);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#deIndex(XReadableField)} works
	 * correctly.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to true.
	 */
	@Test
	public void testDeIndexField() {
		XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		for(XID fieldId : newJohn) {
			XReadableField field = newJohn.getField(fieldId);
			this.newIndex.deIndex(field);
		}
		
		// deindexing all fields equals deindexing the whole object
		testIfObjectWasDeIndexed(newJohn, this.newIndex, this.newIndexer);
	}
	
	/**
	 * Tests if {@link XModelObjectLevelIndex#deIndex(XReadableField)} works
	 * correctly.
	 * 
	 * This tests an index which defaultIncludeAll parameter is set to false.
	 */
	@Test
	public void testDeIndexFieldOfExcludeAllModel() {
		XReadableObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
		
		for(XID fieldId : newJohn) {
			XReadableField field = newJohn.getField(fieldId);
			this.newExcludeAllIndex.deIndex(field);
		}
		
		// deindexing all fields equals deindexing the whole object
		testIfObjectWasDeIndexed(newJohn, this.newExcludeAllIndex, this.newExcludeAllIndexer);
	}
	
	private void testIfObjectWasDeIndexed(XReadableObject object, XModelObjectLevelIndex index,
	        XValueIndexer indexer) {
		XAddress objectAddress = object.getAddress();
		
		for(XID fieldId : object) {
			XValue value = object.getField(fieldId).getValue();
			List<String> indexStrings = indexer.getIndexStrings(value);
			
			for(String s : indexStrings) {
				Set<Pair<XAddress,XValue>> pairs = index.search(s);
				Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);
				
				assertFalse(addresses.contains(objectAddress));
			}
		}
	}
}
