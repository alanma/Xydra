package org.xydra.core.test.model;

import junit.framework.TestCase;

import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.value.impl.memory.MemoryStringValue;


public abstract class AbstractChangeTest extends TestCase {
	
	@Test
	public void testRepositoryChangeListening() {
		XRepository repo = X.createMemoryRepository();
		DummyRepositoryChangeListener repoListener = new DummyRepositoryChangeListener();
		repo.addListenerForRepositoryEvents(repoListener);
		
		// Test add-event
		XModel addModel = repo.createModel(null, X.getIDProvider().createUniqueID());
		// check if the listener has fired
		assertTrue(repoListener.hasFired());
		// check if the event has the right type
		assertTrue(repoListener.getType() == ChangeType.ADD);
		// check if the repository of the event is the right one
		assertTrue(repo.getID().equals(repoListener.getRepository()));
		repoListener.reset();
		
		repo.createModel(null, X.getIDProvider().createUniqueID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		repoListener.reset();
		
		// Test remove-event
		repo.removeModel(null, addModel.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		repoListener.reset();
		
		// Test if event-propagating works
		repo.createModel(null, X.getIDProvider().createUniqueID());
		
		// Test add-event
		addModel = repo.createModel(null, X.getIDProvider().createUniqueID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		
		// Test remove-event
		repo.removeModel(null, addModel.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
	}
	
	@Test
	public void testModelChangeListenening() {
		
		XModel model = new MemoryModel(X.getIDProvider().createUniqueID());
		DummyModelChangeListener modelListener = new DummyModelChangeListener();
		model.addListenerForModelEvents(modelListener);
		
		// Test add-event
		XObject addObject = model.createObject(null, X.getIDProvider().createUniqueID());
		assertTrue(modelListener.hasFired()); // check if the listener has fired
		assertTrue(modelListener.getType() == ChangeType.ADD); // check if the
		// event
		// has the right
		// type
		assertEquals(model.getID(), modelListener.getModel()); // check if the
		// model of
		// the event is the
		// right one
		assertNull(modelListener.getRepository());
		modelListener.reset();
		
		model.createObject(null, X.getIDProvider().createUniqueID());
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		modelListener.reset();
		
		// Test remove-event
		model.removeObject(null, addObject.getID());
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		modelListener.reset();
		
	}
	
	@Test
	public void testModelChangePropagation() {
		
		// Test if event-propagating works
		XRepository repo = X.createMemoryRepository();
		XModel model = repo.createModel(null, X.getIDProvider().createUniqueID());
		model.createObject(null, X.getIDProvider().createUniqueID());
		
		DummyModelChangeListener modelListener = new DummyModelChangeListener();
		DummyModelChangeListener repoListener = new DummyModelChangeListener();
		
		repo.addListenerForModelEvents(repoListener);
		model.addListenerForModelEvents(modelListener);
		
		// Test add-event
		XObject addObject = model.createObject(null, X.getIDProvider().createUniqueID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		modelListener.reset();
		
		// Test remove-event
		model.removeObject(null, addObject.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testObjectChangeListening() {
		
		MemoryObject object1 = new MemoryObject(X.getIDProvider().createUniqueID());
		DummyObjectChangeListener listener1 = new DummyObjectChangeListener();
		object1.addListenerForObjectEvents(listener1);
		
		// Test add-event
		// create field and fire event
		MemoryField field1 = object1.createField(null, X.getIDProvider().createUniqueID());
		
		assertTrue("check if the listener has fired", listener1.hasFired());
		assertEquals("check if the event has the right type", ChangeType.ADD, listener1.getType());
		assertEquals("check if the object of the event is the right one", object1.getID(),
		        listener1.getObject());
		assertEquals("listener1.getModel() is not null but " + listener1.getModelID(), listener1
		        .getModelID(), null);
		assertEquals(listener1.getRepositoryID(), null);
		listener1.reset();
		
		object1.createField(null, X.getIDProvider().createUniqueID());
		assertTrue(listener1.hasFired());
		assertEquals(ChangeType.ADD, listener1.getType());
		listener1.reset();
		
		// Test remove-event
		object1.removeField(null, field1.getID());
		assertTrue(listener1.hasFired());
		assertTrue(listener1.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testObjectChangePropagation() {
		
		// Test if event-propagating works
		XRepository repo = X.createMemoryRepository();
		XModel model = repo.createModel(null, X.getIDProvider().createUniqueID());
		XObject object2 = model.createObject(null, X.getIDProvider().createUniqueID());
		object2.createField(null, X.getIDProvider().createUniqueID());
		
		DummyObjectChangeListener repoListener = new DummyObjectChangeListener();
		repo.addListenerForObjectEvents(repoListener);
		
		DummyObjectChangeListener modelListener = new DummyObjectChangeListener();
		model.addListenerForObjectEvents(modelListener);
		
		DummyObjectChangeListener objectListener = new DummyObjectChangeListener();
		object2.addListenerForObjectEvents(objectListener);
		
		XField field3 = object2.createField(null, X.getIDProvider().createUniqueID());
		assertTrue(repoListener.hasFired()); // event was propagated to the
		// father repository
		assertTrue(repoListener.getType() == ChangeType.ADD);
		assertTrue(modelListener.hasFired()); // event was propagated to the
		// father model
		assertTrue(modelListener.getType() == ChangeType.ADD);
		assertTrue(objectListener.hasFired());
		assertTrue(objectListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		modelListener.reset();
		objectListener.reset();
		
		object2.removeField(null, field3.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		assertTrue(objectListener.hasFired());
		assertTrue(objectListener.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testFieldChangeListening() {
		// Prepare other objects for event propagating test later
		XRepository repo = X.createMemoryRepository();
		XModel model = repo.createModel(null, X.getIDProvider().createUniqueID());
		XObject object = model.createObject(null, X.getIDProvider().createUniqueID());
		XField field = object.createField(null, X.getIDProvider().createUniqueID());
		
		DummyFieldChangeListener fieldListener = new DummyFieldChangeListener();
		field.addListenerForFieldEvents(fieldListener);
		
		// Test add-event
		field.setValue(field.getID(), new MemoryStringValue("Test"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.ADD);
		assertEquals(fieldListener.getFieldID(), field.getID());
		assertEquals(fieldListener.getObjectID(), object.getID());
		assertEquals(fieldListener.getModelID(), model.getID());
		assertEquals(fieldListener.getRepositoryID(), repo.getID());
		fieldListener.reset();
		
		// Test change-event
		field.setValue(field.getID(), new MemoryStringValue("Test 2"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.CHANGE);
		fieldListener.reset();
		
		field.setValue(field.getID(), new MemoryStringValue("Test 2"));
		assertFalse(fieldListener.hasFired()); // no changes occurs, therefore
		// no event should be fired
		fieldListener.reset();
		
		// Test remove-event
		field.setValue(field.getID(), null);
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.REMOVE);
		assertTrue(field.getValue() == null);
		fieldListener.reset();
		
		// Test if event-propagating works
		DummyFieldChangeListener repoListener = new DummyFieldChangeListener();
		repo.addListenerForFieldEvents(repoListener);
		
		DummyFieldChangeListener modelListener = new DummyFieldChangeListener();
		model.addListenerForFieldEvents(modelListener);
		
		DummyFieldChangeListener objectListener = new DummyFieldChangeListener();
		object.addListenerForFieldEvents(objectListener);
		
		// Add
		field.setValue(field.getID(), new MemoryStringValue("Test"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.ADD);
		assertTrue(objectListener.hasFired());
		assertTrue(objectListener.getType() == ChangeType.ADD);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		fieldListener.reset();
		objectListener.reset();
		modelListener.reset();
		repoListener.reset();
		
		// Change
		field.setValue(field.getID(), new MemoryStringValue("Test 2"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.CHANGE);
		assertTrue(objectListener.hasFired());
		assertTrue(objectListener.getType() == ChangeType.CHANGE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.CHANGE);
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.CHANGE);
		fieldListener.reset();
		objectListener.reset();
		modelListener.reset();
		repoListener.reset();
		
		// Change
		field.setValue(field.getID(), null);
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.REMOVE);
		assertTrue(objectListener.hasFired());
		assertTrue(objectListener.getType() == ChangeType.REMOVE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		fieldListener.reset();
		objectListener.reset();
		modelListener.reset();
		repoListener.reset();
	}
	
	// - - - - Dummy implementation of the listeners - - - -
	
	private class DummyRepositoryChangeListener implements XRepositoryEventListener {
		private ChangeType type;
		private XID repo;
		public boolean fired = false;
		
		public void onChangeEvent(XRepositoryEvent event) {
			this.type = event.getChangeType();
			this.repo = event.getRepositoryID();
			this.fired = true;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public XID getRepository() {
			return this.repo;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void reset() {
			this.type = null;
			this.repo = null;
			this.fired = false;
		}
	}
	
	private class DummyModelChangeListener implements XModelEventListener {
		private ChangeType type;
		private XID repo;
		private XID model;
		public boolean fired = false;
		
		public void onChangeEvent(XModelEvent event) {
			this.type = event.getChangeType();
			this.repo = event.getRepositoryID();
			this.model = event.getModelID();
			this.fired = true;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public XID getRepository() {
			return this.repo;
		}
		
		public XID getModel() {
			return this.model;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void reset() {
			this.type = null;
			this.repo = null;
			this.model = null;
			this.fired = false;
		}
	}
	
	private class DummyObjectChangeListener implements XObjectEventListener {
		private ChangeType type;
		private XID repoID;
		private XID modelID;
		private XID objectID;
		public boolean fired = false;
		
		public void onChangeEvent(XObjectEvent event) {
			this.type = event.getChangeType();
			this.repoID = event.getRepositoryID();
			this.modelID = event.getModelID();
			this.objectID = event.getObjectID();
			this.fired = true;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public XID getRepositoryID() {
			return this.repoID;
		}
		
		public XID getModelID() {
			return this.modelID;
		}
		
		public XID getObject() {
			return this.objectID;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void reset() {
			this.type = null;
			this.repoID = null;
			this.modelID = null;
			this.objectID = null;
			this.fired = false;
		}
	}
	
	private class DummyFieldChangeListener implements XFieldEventListener {
		private ChangeType type;
		private XID repo;
		private XID model;
		private XID object;
		private XID field;
		public boolean fired = false;
		
		public void onChangeEvent(XFieldEvent event) {
			this.type = event.getChangeType();
			this.repo = event.getRepositoryID();
			this.model = event.getModelID();
			this.object = event.getObjectID();
			this.field = event.getFieldID();
			this.fired = true;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public XID getRepositoryID() {
			return this.repo;
		}
		
		public XID getModelID() {
			return this.model;
		}
		
		public XID getObjectID() {
			return this.object;
		}
		
		public XID getFieldID() {
			return this.field;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void reset() {
			this.type = null;
			this.repo = null;
			this.model = null;
			this.object = null;
			this.field = null;
			this.fired = false;
		}
	}
	
}
