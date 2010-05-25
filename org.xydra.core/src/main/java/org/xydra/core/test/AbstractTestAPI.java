package org.xydra.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xydra.core.URIFormatException;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.XY;
import org.xydra.core.model.MissingPieceException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;
import org.xydra.core.value.impl.memory.MemoryIDListValue;



/**
 * Sub-classes need this code <code>
 * 
 * @BeforeClass public static void init() {
 * 
 *              XSPI.setStateStore(new MemoryStateStore()); }
 * 
 *              </code>
 * @author voelkel
 */
public abstract class AbstractTestAPI {
	
	public static final XID ACTOR_ID = X.getIDProvider().fromString("urn-test-actorID");
	
	@Test
	public void testRepository() {
		// create a repository
		XRepository repo = new MemoryRepository(X.getIDProvider().createUniqueID());
		
		// add a model to the repository
		XID modelID = X.getIDProvider().createUniqueID(); // create an ID for
		// the model
		XModel model = repo.createModel(ACTOR_ID, modelID);
		
		// check whether it was really added
		assertEquals(model, repo.getModel(modelID));
		assertTrue(repo.hasModel(modelID));
		
		// remove model again
		repo.removeModel(ACTOR_ID, model.getID());
		
		// check whether it was really removed
		assertEquals(null, repo.getModel(modelID));
		assertFalse(repo.hasModel(modelID));
	}
	
	@Test
	public void testModel() {
		// create a model
		XModel model = X.createMemoryRepository().createModel(ACTOR_ID,
		        X.getIDProvider().createUniqueID());
		assertTrue(model.getRevisionNumber() >= 0);
		
		// add an object to the model
		XID objectID = X.getIDProvider().createUniqueID(); // create an ID for
		// the object
		XObject object = model.createObject(ACTOR_ID, objectID);
		
		// check whether it was really added
		assertEquals(object, model.getObject(objectID));
		assertTrue(model.hasObject(objectID));
		
		// remove object again
		model.removeObject(ACTOR_ID, object.getID());
		
		// check whether it was really removed
		assertEquals(null, model.getObject(objectID));
		assertFalse(model.hasObject(objectID));
		
		// - do the same with a model that was created by a repository -
		
		XRepository repo = new MemoryRepository(X.getIDProvider().createUniqueID());
		XID modelID = X.getIDProvider().createUniqueID();
		model = repo.createModel(ACTOR_ID, modelID);
		
		// add an object to the model
		XID object2ID = X.getIDProvider().createUniqueID(); // create an ID for
		// the object
		XObject object2 = model.createObject(ACTOR_ID, object2ID);
		
		// check whether it was really added
		assertEquals(object2, model.getObject(object2ID));
		assertTrue(model.hasObject(object2ID));
		
		// remove object again
		model.removeObject(ACTOR_ID, object2.getID());
		
		// check whether it was really removed
		assertEquals(null, model.getObject(object2ID));
		assertFalse(model.hasObject(object2ID));
		
	}
	
	@Test
	public void testObject() {
		// create an object
		XObject object = new MemoryObject(X.getIDProvider().createUniqueID());
		
		// add a field to the object
		XID fieldID = X.getIDProvider().createUniqueID(); // create an ID for
		// the field
		XField field = object.createField(ACTOR_ID, fieldID);
		
		// check whether it was really added
		assertEquals(field, object.getField(fieldID));
		assertTrue(object.hasField(fieldID));
		
		// remove field again
		object.removeField(ACTOR_ID, field.getID());
		
		// check whether it was really removed
		XField fieldAgain = object.getField(fieldID);
		assertNull(fieldAgain);
		assertFalse(object.hasField(fieldID));
		
		// - do the same with an object that was created by a model -
		XRepository repo = X.createMemoryRepository();
		XID modelID = X.getIDProvider().createUniqueID();
		XModel model = repo.createModel(ACTOR_ID, modelID);
		object = model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		
		// add a field to the object
		XID field2ID = X.getIDProvider().createUniqueID(); // create an ID for
		// the field
		XField field2 = object.createField(ACTOR_ID, field2ID);
		
		// check whether it was really added
		assertEquals(field2, object.getField(field2ID));
		assertTrue(object.hasField(field2ID));
		
		// remove field again
		object.removeField(ACTOR_ID, field2.getID());
		
		// check whether it was really removed
		assertEquals(null, object.getField(field2ID));
		assertFalse(object.hasField(field2ID));
	}
	
	@Test
	public void testField() {
		// create a field
		XField field = new MemoryField(X.getIDProvider().createUniqueID());
		
		// check that the value isn't set
		assertEquals(null, field.getValue());
		
		// add a value to the object
		XValue testValue1 = X.getValueFactory().createStringValue("Test");
		field.setValue(ACTOR_ID, testValue1);
		
		// check whether it was really added
		assertEquals(testValue1, field.getValue());
		
		// change the value
		XValue testValue2 = X.getValueFactory().createStringValue("Another test");
		field.setValue(ACTOR_ID, testValue2);
		
		// check whether it was really changed
		assertEquals(testValue2, field.getValue());
		
		// remove the value
		field.setValue(ACTOR_ID, null);
		
		// check whether it was really removed
		assertEquals(null, field.getValue());
		
		// - do the same with a field that was created by an object -
		XRepository repo = X.createMemoryRepository();
		XID modelID = X.getIDProvider().createUniqueID();
		XModel model = repo.createModel(ACTOR_ID, modelID);
		XObject object = model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		field = object.createField(ACTOR_ID, X.getIDProvider().createUniqueID());
		
		// check that the value isn't set
		assertEquals(null, field.getValue());
		
		// add a value to the object
		XValue testValue3 = X.getValueFactory().createStringValue("Testing again");
		field.setValue(ACTOR_ID, testValue3);
		
		// check whether it was really added
		assertEquals(testValue3, field.getValue());
		
		// change the value
		XValue testValue4 = X.getValueFactory().createStringValue("AND AGAIN!");
		field.setValue(ACTOR_ID, testValue4);
		
		// check whether it was really changed
		assertEquals(testValue4, field.getValue());
		
		// remove the value
		field.setValue(ACTOR_ID, null);
		
		// check whether it was really removed
		assertEquals(null, field.getValue());
	}
	
	@Test
	public void testXX() {
		// This test shows how XX can be used
		
		// - - Method: XField setValue(XID actorID, XObject object, XID fieldID,
		// XValue value) - -
		XRepository repo = X.createMemoryRepository();
		XModel model = repo.createModel(ACTOR_ID, X.getIDProvider().createUniqueID());
		XObject object = model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		XField field1 = object.createField(ACTOR_ID, X.getIDProvider().createUniqueID());
		XValue value1 = X.getValueFactory().createStringValue("Test value");
		
		// Add value to an existing field
		XField field2 = XX.setValue(ACTOR_ID, object, field1.getID(), value1);
		
		// check if the method works correctly
		assertEquals("the value of field should be set", field1, field2);
		assertTrue("the method should return the field1-object, and not create a new one",
		        field1 == field2); // 
		
		// Change value of the existing field
		XValue value2 = X.getValueFactory().createStringValue("Another test value");
		field2 = XX.setValue(ACTOR_ID, object, field1.getID(), value2);
		assertEquals(field1, field2);
		assertTrue(field1 == field2);
		
		// Remove value of the existing field
		field2 = XX.setValue(ACTOR_ID, object, field1.getID(), null);
		assertEquals(field1, field2);
		assertTrue(field1 == field2);
		assertNull(field1.getValue());
		
		// Add value to a not existing field (should create a fitting field)
		XID newID = X.getIDProvider().createUniqueID();
		field2 = XX.setValue(ACTOR_ID, object, newID, value1);
		assertTrue(object.hasField(newID)); // did it create a new field?
		assertEquals(object.getField(newID), field2);
		assertTrue(object.getField(newID) == field2);
		assertEquals(value1, object.getField(newID).getValue());
		
		// Remove a value from a not existing field (should create a new field
		// which value isn't set)
		newID = X.getIDProvider().createUniqueID();
		field2 = XX.setValue(ACTOR_ID, object, newID, null);
		assertTrue(object.hasField(newID));
		assertEquals(object.getField(newID), field2);
		assertTrue(object.getField(newID) == field2);
		assertNull(object.getField(newID).getValue());
		
		// - - Method: void copy(XID actorID, XModel sourceModel, XModel
		// targetModel) - -
		model = repo.createModel(ACTOR_ID, X.getIDProvider().createUniqueID());
		// add some content
		model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test"));
		
		model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test2"));
		
		model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test3"));
		
		model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test4"));
		
		model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID());
		
		model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		
		// copy it!
		XModel copyModel = new MemoryModel(model.getID());
		
		XX.copy(ACTOR_ID, model, copyModel);
		
		// do both models have the same content? (revision numbers may differ)
		assertTrue(XX.equalTree(model, copyModel));
		
		// - - Method: void copy(XID actorID, XObject sourceObject, XObject
		// targetObject) - -
		model = repo.createModel(ACTOR_ID, X.getIDProvider().createUniqueID());
		object = model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		
		// add some content
		object.createField(ACTOR_ID, X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test"));
		
		object.createField(ACTOR_ID, X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test 2"));
		
		object.createField(ACTOR_ID, X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test 3"));
		
		object.createField(ACTOR_ID, X.getIDProvider().createUniqueID()).setValue(ACTOR_ID,
		        X.getValueFactory().createStringValue("Test 4"));
		
		object.createField(ACTOR_ID, X.getIDProvider().createUniqueID());
		
		// copy it!
		XObject copyObject = new MemoryObject(object.getID());
		
		XX.copy(ACTOR_ID, object, copyObject);
		
		// do both objects have the same content? (revision numbers may differ)
		assertTrue(XX.equalTree(object, copyObject));
		
		// - - Method: XValue safeGetValue(XObject object, XID fieldID) - -
		object = model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		XID fieldID = X.getIDProvider().createUniqueID();
		field1 = object.createField(ACTOR_ID, fieldID);
		field1.setValue(ACTOR_ID, X.getValueFactory().createStringValue("Test"));
		XValue value = field1.getValue();
		
		// get the value of an existing field
		assertEquals(value, XX.safeGetValue(object, fieldID));
		assertTrue(value == XX.safeGetValue(object, fieldID));
		
		// remove value and try to get it (should throw an exception)
		field1.setValue(ACTOR_ID, null);
		try {
			XX.safeGetValue(object, fieldID); // safeGetValue should throw a
			// MissingPieceException
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get the value of a not existing field
		try {
			// safeGetValue should throw a MissingPieceException
			XX.safeGetValue(object, X.getIDProvider().createUniqueID());
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// - - Method: XValue safeGetValue(XModel model, XID objectID, XID
		// fieldID - -
		model = repo.createModel(ACTOR_ID, X.getIDProvider().createUniqueID());
		XID objectID = X.getIDProvider().createUniqueID();
		object = model.createObject(ACTOR_ID, objectID);
		fieldID = X.getIDProvider().createUniqueID();
		field1 = object.createField(ACTOR_ID, fieldID);
		field1.setValue(ACTOR_ID, X.getValueFactory().createStringValue("Test"));
		value = field1.getValue();
		
		// get the value of an existing field
		assertEquals(value, XX.safeGetValue(model, objectID, fieldID));
		assertTrue(value == XX.safeGetValue(model, objectID, fieldID));
		
		// remove value and try to get it (should throw an exception)
		field1.setValue(ACTOR_ID, null);
		try {
			XX.safeGetValue(model, objectID, fieldID); // safeGetValue should
			// throw a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get the value of a not existing field
		try {
			// safeGetValue should throw a MissingPieceException here
			XX.safeGetValue(model, objectID, X.getIDProvider().createUniqueID());
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get the value of a field in an not existing object
		try {
			// safeGetValue should throw a MissingPieceException here
			XX.safeGetValue(model, X.getIDProvider().createUniqueID(), X.getIDProvider()
			        .createUniqueID());
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// - - Method: XField safeGetField(XObject object, XID fieldID) - -
		model = repo.createModel(ACTOR_ID, X.getIDProvider().createUniqueID());
		object = model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID());
		fieldID = X.getIDProvider().createUniqueID();
		field1 = object.createField(ACTOR_ID, fieldID);
		
		// get an existing field of an existing object
		assertEquals(field1, XX.safeGetField(object, fieldID));
		assertTrue(field1 == XX.safeGetField(object, fieldID));
		
		// remove the field and try to get it (should throw an exception)
		object.removeField(ACTOR_ID, field1.getID());
		
		try {
			XX.safeGetField(object, fieldID); // safeGetField should throw a
			// MissingPieceException here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get a not existing field
		try {
			XX.safeGetField(object, X.getIDProvider().createUniqueID()); // safeGetField
			// should
			// throw
			// a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// - - Method: XModel safeGetModel(XRepository repository, XID modelID)
		// - -
		XID modelID = X.getIDProvider().createUniqueID();
		model = repo.createModel(ACTOR_ID, modelID);
		
		// get an existing model of an existing object
		assertEquals(model, XX.safeGetModel(repo, modelID));
		assertTrue(model == XX.safeGetModel(repo, modelID));
		
		// remove model and try to get it (should throw an exception)
		repo.removeModel(ACTOR_ID, model.getID());
		
		try {
			XX.safeGetModel(repo, modelID); // safeGetModel should throw a
			// MissingPieceException here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get a not existing model
		try {
			XX.safeGetModel(repo, X.getIDProvider().createUniqueID()); // safeGetModel
			// should
			// throw a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// - - Method: XObject safeGetObject(XModel model, XID objectID) - -
		model = repo.createModel(ACTOR_ID, X.getIDProvider().createUniqueID());
		objectID = X.getIDProvider().createUniqueID();
		object = model.createObject(ACTOR_ID, objectID);
		
		// get existing object
		assertEquals(object, XX.safeGetObject(model, objectID));
		assertTrue(object == XX.safeGetObject(model, objectID));
		
		// remove object and try to get it (should throw an exception)
		model.removeObject(ACTOR_ID, object.getID());
		try {
			XX.safeGetObject(model, objectID); // safeGetObject should throw a
			// MissingPieceException here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get a not existing object
		try {
			XX.safeGetObject(model, X.getIDProvider().createUniqueID()); // safeGetObject
			// should
			// throw
			// a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// - - Method: XObject safeGetObject(XRepository repository, XID
		// modelID, XID objectID) - -
		repo = X.createMemoryRepository();
		modelID = X.getIDProvider().createUniqueID();
		model = repo.createModel(ACTOR_ID, modelID);
		objectID = X.getIDProvider().createUniqueID();
		object = model.createObject(ACTOR_ID, objectID);
		
		// get existing object
		assertEquals(object, XX.safeGetObject(repo, modelID, objectID));
		assertTrue(object == XX.safeGetObject(repo, modelID, objectID));
		
		// remove object and try to get it (should throw an exception)
		model.removeObject(ACTOR_ID, object.getID());
		try {
			XX.safeGetObject(repo, modelID, objectID); // safeGetObject should
			// throw a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get a not existing object
		try {
			XX.safeGetObject(repo, modelID, X.getIDProvider().createUniqueID()); // safeGetObject
			// should
			// throw
			// a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// try to get a not existing object of a not existing model
		try {
			XX.safeGetObject(repo, X.getIDProvider().createUniqueID(), X.getIDProvider()
			        .createUniqueID()); // safeGetObject should
			// throw a
			// MissingPieceException
			// here
			assertTrue(false);
		} catch(MissingPieceException mpe) {
			assertTrue(true);
		}
		
		// - - Method: void safeSetStringValue(XID actorID, XObject object, XID
		// fieldID, String stringValue) - -
		object = new MemoryObject(X.getIDProvider().createUniqueID());
		fieldID = X.getIDProvider().createUniqueID();
		field1 = object.createField(ACTOR_ID, fieldID);
		
		// set the value of an existing field
		XX.safeSetStringValue(ACTOR_ID, object, fieldID, "Test");
		assertTrue(field1.getValue() instanceof XStringValue);
		assertEquals("Test", ((XStringValue)field1.getValue()).contents());
		
		// set the value of a not existing field
		newID = X.getIDProvider().createUniqueID();
		assertFalse(object.hasField(newID));
		XX.safeSetStringValue(ACTOR_ID, object, newID, "Test");
		assertTrue(object.hasField(newID));
		assertTrue(object.getField(newID).getValue() instanceof XStringValue);
		assertEquals("Test", ((XStringValue)object.getField(newID).getValue()).contents());
		
		// - - Method: XIDListValue addIDToList(XID actorID, XField field, XID
		// id) - -
		// - - Method: XIDListValue removeIDFromList(XID actorID, XField field,
		// XID id) - -
		field1 = new MemoryField(X.getIDProvider().createUniqueID());
		field1.setValue(ACTOR_ID, new MemoryIDListValue(new XID[] { X.getIDProvider()
		        .createUniqueID() }));
		newID = X.getIDProvider().createUniqueID();
		
		XIDListValue listValue = (XIDListValue)field1.getValue();
		assertFalse(XX.asList(listValue).contains(newID));
		// add the new id
		XX.addIDToList(ACTOR_ID, field1, newID);
		
		// check that the id was added
		listValue = (XIDListValue)field1.getValue();
		assertEquals(XX.asList(listValue).size(), 2);
		assertTrue(XX.asList(listValue).contains(newID));
		
		// remove it
		XX.removeIDFromList(ACTOR_ID, field1, newID);
		
		// check that it was removed
		listValue = (XIDListValue)field1.getValue();
		assertEquals(XX.asList(listValue).size(), 1);
		assertFalse(XX.asList(listValue).contains(newID));
		
		// Some tests for the getXXfromURI Methods
		XRepository testRepository = X.createMemoryRepository();
		String modelIDString = "TestModel";
		XModel testModel = testRepository.createModel(ACTOR_ID, X.getIDProvider().fromString(
		        modelIDString));
		String objectIDString = "Object";
		XObject testObject = testModel.createObject(ACTOR_ID, X.getIDProvider().fromString(
		        objectIDString));
		String fieldIDString = "Field";
		XField testField = testObject.createField(ACTOR_ID, X.getIDProvider().fromString(
		        fieldIDString));
		XValue testValue = X.getValueFactory().createStringValue("TestValue");
		testField.setValue(ACTOR_ID, testValue);
		
		// - - Method: XModel getModelFromURI(XRepository repository, String
		// uri) - -
		assertEquals(testModel, XX.getModelFromURI(testRepository, modelIDString));
		assertEquals(testModel, XX.getModelFromURI(testRepository, modelIDString + "/"
		        + objectIDString));
		assertEquals(testModel, XX.getModelFromURI(testRepository, modelIDString + "/"
		        + objectIDString + "/" + fieldIDString));
		assertEquals(null, XX.getModelFromURI(testRepository, "NonsenseURI"));
		
		// - - Method: XObject getObjectFromURI(XRepository repository, String
		// uri) - -
		assertEquals(testObject, XX.getObjectFromURI(testRepository, modelIDString + "/"
		        + objectIDString));
		assertEquals(testObject, XX.getObjectFromURI(testRepository, modelIDString + "/"
		        + objectIDString + "/" + fieldIDString));
		assertEquals(null, XX.getObjectFromURI(testRepository, "NonsenseURI"));
		
		try {
			XX.getObjectFromURI(testRepository, modelIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		// - - Method: XObject getObjectFromURI(Model model, String uri) - -
		assertEquals(testObject, XX.getObjectFromURI(testModel, objectIDString));
		assertEquals(testObject, XX.getObjectFromURI(testModel, objectIDString + "/"
		        + fieldIDString));
		assertEquals(null, XX.getObjectFromURI(testModel, "NonsenseURI"));
		
		// - - XField getFieldFromURI(XRepository repository, String uri) - -
		assertEquals(testField, XX.getFieldFromURI(testRepository, modelIDString + "/"
		        + objectIDString + "/" + fieldIDString));
		assertEquals(null, XX.getFieldFromURI(testModel, "NonsenseURI"));
		
		try {
			XX.getFieldFromURI(testRepository, modelIDString + "/" + objectIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		try {
			XX.getFieldFromURI(testRepository, modelIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		// - - XField getFieldFromURI(XModel model, String uri) - -
		assertEquals(testField, XX.getFieldFromURI(testModel, objectIDString + "/" + fieldIDString));
		assertEquals(null, XX.getFieldFromURI(testModel, "NonsenseURI"));
		
		try {
			XX.getFieldFromURI(testModel, objectIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		// - - XField getFieldFromURI(XObject model, String uri) - -
		assertEquals(testField, XX.getFieldFromURI(testObject, fieldIDString));
		assertEquals(null, XX.getFieldFromURI(testObject, "NonsenseURI"));
		
		// - - XField getValueFromURI(XRepository repository, String uri) - -
		assertEquals(testValue, XX.getValueFromURI(testRepository, modelIDString + "/"
		        + objectIDString + "/" + fieldIDString));
		assertEquals(null, XX.getValueFromURI(testModel, "NonsenseURI"));
		
		try {
			XX.getValueFromURI(testRepository, modelIDString + "/" + objectIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		try {
			XX.getValueFromURI(testRepository, modelIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		// - - XField getValueFromURI(XModel model, String uri) - -
		assertEquals(testValue, XX.getValueFromURI(testModel, objectIDString + "/" + fieldIDString));
		assertEquals(null, XX.getValueFromURI(testModel, "NonsenseURI"));
		
		try {
			XX.getValueFromURI(testModel, objectIDString);
			fail(); // fail if no exception is thrown
		} catch(URIFormatException ufe) {
			// success
		}
		
		// - - XField getValueFromURI(XObject model, String uri) - -
		assertEquals(testField, XX.getValueFromURI(testObject, fieldIDString));
		assertEquals(null, XX.getValueFromURI(testObject, "NonsenseURI"));
		
		// TODO test nonsense-URI cases more thoroughly
	}
	
	/*
	 * The following is a big test illustrating how to use XModel to build a
	 * model and how to actually use the model we built. The sample model will
	 * model a library in the following style:
	 * 
	 * - A Library has: Books, BookCopies (the actual books that can be
	 * borrowed), Users - A Book has: An ID, A title, an author and copies - A
	 * BookCopy has: An ID, A field telling whether its borrowed or not, a field
	 * telling which Book it's a copy of, a field telling who borrowed it Book
	 * copies can be borrowed and returned
	 * 
	 * The first thing we'll do is building some wrappers that'll make it easier
	 * to use XModel
	 * 
	 * Attention: We will not catch errors in using the models in the wrappers
	 * here. For example if the user tries to set the titleField on an XObject
	 * which has no such field, the JVM will throw an exception, because we're
	 * not paying attention to that here. It's generally a good idea to use the
	 * wrappers to catch possible errors like that
	 */

	// A wrapper for the books - A book can be modeled as an XObject
	static class Book {
		// we'll use XFields and XValues to store the informations about the
		// book, so we need some XIDs for that
		public static final XID titleID = X.getIDProvider().fromString("title");
		public static final XID authorID = X.getIDProvider().fromString("author");
		public static final XID copiesID = X.getIDProvider().fromString("copies");
		
		private XObject book;
		
		public Book(XObject book) {
			this.book = book;
		}
		
		// a little method to set up the object structure
		public void setUp(XID actorID) {
			this.book.createField(actorID, titleID); // create a field for the
			// name
			this.book.createField(actorID, authorID); // create a field for the
			// author
			XField copiesField = this.book.createField(actorID, copiesID); // create
			// a
			// field
			// for
			// the
			// copies
			
			// we'll set up the value here too, because wed take it for granted
			// that setUp will only be called on "new" objects
			// otherwise it might overwrite the already existing value
			copiesField.setValue(actorID, X.getValueFactory().createIDListValue(new XID[] {})); // the
			// copies
			// have
			// an
			// own
			// XID,
			// so
			// we'll
			// store those
		}
		
		// the following methods will assume that the book-XObject is correctly
		// set up
		
		// a method for setting the title
		public void setTitle(XID actorID, String name) {
			this.book.getField(titleID).setValue(actorID,
			        X.getValueFactory().createStringValue(name));
		}
		
		// a method for getting the title
		public String getTitle() {
			XValue value = this.book.getField(titleID).getValue();
			
			if(value instanceof XStringValue) {
				return ((XStringValue)value).contents();
			} else {
				return null; // the titleField should hold a String
			}
		}
		
		// a method for setting the author
		public void setAuthor(XID actorID, String author) {
			this.book.getField(authorID).setValue(actorID,
			        X.getValueFactory().createStringValue(author));
		}
		
		// a method for getting the author
		public String getAuthor() {
			XValue value = this.book.getField(authorID).getValue();
			
			if(value instanceof XStringValue) {
				return ((XStringValue)value).contents();
			} else {
				return null; // the authorField should hold a String
			}
		}
		
		// a method for adding a copy
		public void addCopy(XID actorID, XID copyID) {
			XX.addIDToList(actorID, this.book.getField(copiesID), copyID);
		}
		
		// a method for getting the XIDs of the copies of this book
		public List<XID> getCopies() {
			return XX.asList((XIDListValue)this.book.getField(copiesID).getValue());
		}
	}
	
	// A wrapper for a book copy - a book copy can be modeled as an XObject
	static class BookCopy {
		// we'll use XFields and XValues to store the informations about the
		// book copy, so we need some XIDs for that
		public static final XID copyOfID = X.getIDProvider().fromString("copyOf");
		public static final XID isBorrowedID = X.getIDProvider().fromString("isBorrowed");
		
		private XObject bookCopy;
		
		public BookCopy(XObject bookCopy) {
			this.bookCopy = bookCopy;
		}
		
		// a little method to set up the object structure
		public void setUp(XID actorID) {
			this.bookCopy.createField(actorID, copyOfID); // create a field for
			// the ID of the book
			// this object is a
			// copy of
			this.bookCopy.createField(actorID, isBorrowedID); // create a field
			// for the
			// isBorrowed-attribute
		}
		
		// the following methods will assume that the bookCopy-XObject is
		// correctly set up
		
		// a method for setting the XID this bookCopy is a copy of
		public void setCopyOf(XID actorID, XID bookID) {
			this.bookCopy.getField(copyOfID).setValue(actorID,
			        X.getValueFactory().createIDValue(bookID));
		}
		
		// a method for getting the XID this bookCopy is a copy of
		public XID getCopyOf() {
			XValue value = this.bookCopy.getField(copyOfID).getValue();
			
			if(value instanceof XIDValue) {
				return ((XIDValue)value).contents();
			} else {
				return null; // the copyOf-Field should hold an XID
			}
		}
		
		// a method for setting whether this bookCopy is borrowed or not
		public void setIsBorrowed(XID actorID, boolean isBorrowed) {
			this.bookCopy.getField(isBorrowedID).setValue(actorID,
			        X.getValueFactory().createBooleanValue(isBorrowed));
		}
		
		// a method for checking whether this bookCopy is borrowed or not
		public boolean isBorrowed() {
			XValue value = this.bookCopy.getField(isBorrowedID).getValue();
			
			if(value instanceof XBooleanValue) {
				return ((XBooleanValue)value).contents();
			} else {
				return true; // the copyOf-Field should hold a boolean
			}
		}
	}
	
	// A wrapper for the library - a library can be modeled as an XRepository
	static class Library {
		// we'll use 2 models for books and bookCopies, so we'll need 2 unique
		// IDs:
		public static final XID booksID = X.getIDProvider().fromString("books");
		public static final XID bookCopiesID = X.getIDProvider().fromString("bookCopies");
		
		// we'll interpret the library itself as an XRepository
		private XRepository library;
		
		public Library(XRepository library) {
			this.library = library;
		}
		
		// a little method to set up the model structure
		public void setUp(XID actorID) {
			this.library.createModel(actorID, booksID); // create model for the
			// books
			this.library.createModel(actorID, bookCopiesID); // create model for
			// the book copies
		}
		
		// the following methods will assume that the library-XRepository is
		// correctly set up
		
		// a method to add a new book to the library
		public XID addBook(XID actorID, String title, String author) {
			XID bookID = X.getIDProvider().createUniqueID();
			XObject book = this.library.getModel(booksID).createObject(actorID, bookID);
			
			Book bookWrapper = new Book(book); // we'll use the book wrapper to
			// set the title & author
			bookWrapper.setUp(actorID);
			bookWrapper.setTitle(actorID, title);
			bookWrapper.setAuthor(actorID, author);
			
			return bookID;
		}
		
		// a method to add a copy of a specified book to the library
		public XID addBookyCopy(XID actorID, XID bookID) {
			XObject book = this.library.getModel(booksID).getObject(bookID);
			if(book != null) {
				Book bookWrapper = new Book(book);
				XID bookCopyID = X.getIDProvider().createUniqueID();
				XObject bookCopy = this.library.getModel(bookCopiesID).createObject(actorID,
				        bookCopyID);
				
				BookCopy bookCopyWrapper = new BookCopy(bookCopy); // we'll use
				// the
				// bookCopy-Wrapper
				// to set the
				// fields
				bookCopyWrapper.setUp(actorID);
				bookCopyWrapper.setCopyOf(actorID, bookID);
				bookCopyWrapper.setIsBorrowed(actorID, false);
				
				// add a reference to this copy to the book
				bookWrapper.addCopy(actorID, bookCopyID);
				
				return bookCopyID;
			} else {
				return null; // return null, if there is no book with the ID
				// bookID
			}
		}
		
		// a method for checking whether a book has unborrowed copies
		public boolean hasUnborrowedCopies(XID bookID) {
			XObject book = this.library.getModel(booksID).getObject(bookID);
			
			if(book != null) {
				Book bookWrapper = new Book(book);
				
				// find an unborrowed copy
				for(XID copyID : bookWrapper.getCopies()) {
					XObject unborrowedCopy = this.library.getModel(bookCopiesID).getObject(copyID);
					
					if(unborrowedCopy != null) {
						BookCopy copyWrapper = new BookCopy(unborrowedCopy);
						
						// check whether this copy is borrowed or not
						if(!copyWrapper.isBorrowed()) {
							return true;
						}
					}
				}
				
				return false; // no unborrowed copy found
			} else {
				return false;
			}
		}
		
		// a method for borrowing a book (will return null if no copy of this
		// book is available)
		public XID borrow(XID actorID, XID bookID) {
			XObject book = this.library.getModel(booksID).getObject(bookID);
			
			if(book != null) {
				Book bookWrapper = new Book(book);
				
				// find an unborrowed copy
				for(XID copyID : bookWrapper.getCopies()) {
					XObject unborrowedCopy = this.library.getModel(bookCopiesID).getObject(copyID);
					
					if(unborrowedCopy != null) {
						BookCopy copyWrapper = new BookCopy(unborrowedCopy);
						
						// check whether this copy is borrowed or not
						if(!copyWrapper.isBorrowed()) {
							// found an unborrowed copy
							copyWrapper.setIsBorrowed(actorID, true); // borrow
							// it
							
							return unborrowedCopy.getID();
						}
					}
				}
				
				return null; // no unborrowed copy found
			} else {
				return null;
			}
		}
		
		// a method for returning a copy of a book
		public void returnCopy(XID actorID, XID copyID) {
			XObject copy = this.library.getModel(bookCopiesID).getObject(copyID);
			
			if(copy != null) {
				BookCopy copyWrapper = new BookCopy(copy);
				copyWrapper.setIsBorrowed(actorID, false);
			}
		}
	}
	
	/*
	 * Here is a little test showing a normal user would use the XModel API +
	 * our wrappers to model a library
	 */

	@Test
	public void testLibrary() {
		// Use the omnipotent X to get a repository
		XRepository libraryRepo = X.createMemoryRepository();
		XID actorID = X.getIDProvider().createUniqueID();
		
		// Wrap it with the library class and set it up
		Library library = new Library(libraryRepo);
		library.setUp(actorID); // set the repository up so that it can be used
		// as a library
		
		// add some books
		XID hitchhikerID = library.addBook(actorID, "The Hitchhiker's Guide To The Galaxy",
		        "Douglas Adams");
		XID guardsguardsID = library.addBook(actorID, "Guards! Guards!", "Terry Pratchett");
		XID daVinciID = library.addBook(actorID, "The Da Vinci Code", "Dan Brown");
		
		// add some copies for the books
		List<XID> hitchhikerCopies = new ArrayList<XID>();
		List<XID> guardsguardsCopies = new ArrayList<XID>();
		List<XID> daVinciCopies = new ArrayList<XID>();
		
		// Add 5 copies for the Hitchhiker's Guide
		for(int i = 0; i < 5; i++) {
			hitchhikerCopies.add(library.addBookyCopy(actorID, hitchhikerID));
		}
		
		// Add 2 copies for Guards! Guards!
		for(int i = 0; i < 2; i++) {
			guardsguardsCopies.add(library.addBookyCopy(actorID, guardsguardsID));
		}
		
		// Add 1 copy for The Da Vinci Code
		daVinciCopies.add(library.addBookyCopy(actorID, daVinciID));
		
		// Create some userIDs
		XID user1 = X.getIDProvider().createUniqueID();
		XID user2 = X.getIDProvider().createUniqueID();
		
		// borrow some books
		assertTrue(library.hasUnborrowedCopies(hitchhikerID));
		assertTrue(library.hasUnborrowedCopies(guardsguardsID));
		assertTrue(library.hasUnborrowedCopies(daVinciID));
		
		// borrow all copies of The Hitchhiker's Guide To The Galaxy
		for(int i = 0; i < 5; i++) {
			library.borrow(user1, hitchhikerID);
		}
		// try to borrow another copy of The Hitchhiker's Guide To The Galaxy
		// (should'nt work, all 5 copies should be borrowed)
		assertEquals(null, library.borrow(user1, hitchhikerID));
		
		// borrow the only copy of The Da Vinci Code
		XID borrowedDaVinciID = library.borrow(user1, daVinciID);
		// check that all copies of The Da Vinci Code are now borrowed
		assertFalse(library.hasUnborrowedCopies(daVinciID));
		
		// return the borrowed copy of The Da Vinci Code
		library.returnCopy(user1, borrowedDaVinciID);
		assertTrue(library.hasUnborrowedCopies(daVinciID));
		
		// borrow 1 copy of the Guards! Guards!
		XID borrowedGuardsID1 = library.borrow(user1, guardsguardsID);
		assertTrue(library.hasUnborrowedCopies(guardsguardsID)); // ...there's
		// still
		// another copy
		XID borrowedGuardsID2 = library.borrow(user2, guardsguardsID); // borrow
		// that
		// too
		assertFalse(library.hasUnborrowedCopies(guardsguardsID));
		
		// return both copies
		library.returnCopy(user1, borrowedGuardsID1);
		library.returnCopy(user2, borrowedGuardsID2);
		
		assertTrue(library.hasUnborrowedCopies(guardsguardsID));
	}
	
	@Test
	public void testSaveAndLoadRepository() throws IOException {
		// we'll use our library classes to create a repository with content
		XRepository repo = X.createMemoryRepository();
		XID actorID = X.getIDProvider().createUniqueID();
		
		// add some books and copies
		Library library = new Library(repo);
		library.setUp(actorID);
		XID hitchhikerID = library.addBook(actorID, "The Hitchhiker's Guide To The Galaxy",
		        "Douglas Adams");
		XID guardsguardsID = library.addBook(actorID, "Guards! Guards!", "Terry Pratchett");
		XID daVinciID = library.addBook(actorID, "The Da Vinci Code", "Dan Brown");
		
		library.borrow(actorID, hitchhikerID);
		library.borrow(actorID, hitchhikerID);
		library.borrow(actorID, hitchhikerID);
		library.borrow(actorID, guardsguardsID);
		library.borrow(actorID, guardsguardsID);
		library.borrow(actorID, daVinciID);
		
		// Add 5 copies for the Hitchhiker's Guide
		for(int i = 0; i < 5; i++) {
			library.addBookyCopy(actorID, hitchhikerID);
		}
		
		// Add 2 copies for Guards! Guards!
		for(int i = 0; i < 2; i++) {
			library.addBookyCopy(actorID, guardsguardsID);
		}
		
		// Add 1 copy for The Da Vinci Code
		library.addBookyCopy(actorID, daVinciID);
		
		// We now created a little repository with some content, so saving makes
		// sense
		String filename = "TestAPI-SaveAndLoadRepositoryTest" + XY.fileSuffix;
		XY.saveRepository(repo, filename);
		
		// try to load it
		XRepository loadedRepo = XY.loadRepository(filename);
		assertTrue(loadedRepo != null); // if loadedRepo == null, saving wasn't
		// successful
		assertEquals(loadedRepo, repo); // assert that the saving process really
		// saved our repo
		new File(filename).delete();
	}
	
	@Test
	public void testSaveAndLoadModel() throws IOException {
		XModel model = new MemoryModel(X.getIDProvider().createUniqueID());
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createBooleanValue(true));
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createStringValue("Test!"));
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createBooleanValue(false));
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createIntegerValue(42));
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createLongValue(0l));
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createDoubleValue(3.14159265));
		model.createObject(null, X.getIDProvider().createUniqueID()).createField(null,
		        X.getIDProvider().createUniqueID()).setValue(null,
		        X.getValueFactory().createStringValue("Another Test!"));
		
		String filename = "TestAPI-SaveAndLoadModelTest" + XY.fileSuffix;
		XY.saveModel(model, filename);
		
		XModel loadedModel = XY.loadModel(filename);
		assertTrue(loadedModel != null);
		assertEquals(loadedModel, model);
		
		new File(filename).delete();
	}
}
