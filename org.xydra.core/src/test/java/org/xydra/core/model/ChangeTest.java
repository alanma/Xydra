package org.xydra.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.value.impl.memory.MemoryStringValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


public class ChangeTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private static class DummyFieldChangeListener implements XFieldEventListener {
		private XID field;
		public boolean fired = false;
		private XID model;
		private XID object;
		private XID repo;
		private ChangeType type;
		
		public XID getFieldId() {
			return this.field;
		}
		
		public XID getModelId() {
			return this.model;
		}
		
		public XID getObjectId() {
			return this.object;
		}
		
		public XID getRepositoryId() {
			return this.repo;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void onChangeEvent(XFieldEvent event) {
			this.type = event.getChangeType();
			this.repo = event.getRepositoryId();
			this.model = event.getModelId();
			this.object = event.getObjectId();
			this.field = event.getFieldId();
			this.fired = true;
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
	
	private static class DummyModelChangeListener implements XModelEventListener {
		public boolean fired = false;
		private XID model;
		private XID repo;
		private ChangeType type;
		
		public XID getModel() {
			return this.model;
		}
		
		public XID getRepository() {
			return this.repo;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void onChangeEvent(XModelEvent event) {
			this.type = event.getChangeType();
			this.repo = event.getRepositoryId();
			this.model = event.getModelId();
			this.fired = true;
		}
		
		public void reset() {
			this.type = null;
			this.repo = null;
			this.model = null;
			this.fired = false;
		}
	}
	
	private static class DummyObjectChangeListener implements XObjectEventListener {
		public boolean fired = false;
		private XID modelId;
		private XID objectId;
		private XID repoID;
		private ChangeType type;
		
		public XID getModelId() {
			return this.modelId;
		}
		
		public XID getObject() {
			return this.objectId;
		}
		
		public XID getRepositoryId() {
			return this.repoID;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void onChangeEvent(XObjectEvent event) {
			this.type = event.getChangeType();
			this.repoID = event.getRepositoryId();
			this.modelId = event.getModelId();
			this.objectId = event.getObjectId();
			this.fired = true;
		}
		
		public void reset() {
			this.type = null;
			this.repoID = null;
			this.modelId = null;
			this.objectId = null;
			this.fired = false;
		}
	}
	
	private static class DummyRepositoryChangeListener implements XRepositoryEventListener {
		public boolean fired = false;
		private XID repo;
		private ChangeType type;
		
		public XID getRepository() {
			return this.repo;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		public void onChangeEvent(XRepositoryEvent event) {
			this.type = event.getChangeType();
			this.repo = event.getRepositoryId();
			this.fired = true;
		}
		
		public void reset() {
			this.type = null;
			this.repo = null;
			this.fired = false;
		}
	}
	
	private XID actorId = XX.toId("AbstractChangeTest");
	
	private String password = null; // TODO where to get this?
	
	{
		LoggerTestHelper.init();
	}
	
	@Test
	public void testFieldChangeListening() {
		// Prepare other objects for event propagating test later
		XRepository repo = X.createMemoryRepository(this.actorId);
		XModel model = repo.createModel(XX.createUniqueID());
		XObject object = model.createObject(XX.createUniqueID());
		XField field = object.createField(XX.createUniqueID());
		
		DummyFieldChangeListener fieldListener = new DummyFieldChangeListener();
		field.addListenerForFieldEvents(fieldListener);
		
		// Test add-event
		field.setValue(new MemoryStringValue("Test"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.ADD);
		assertEquals(fieldListener.getFieldId(), field.getID());
		assertEquals(fieldListener.getObjectId(), object.getID());
		assertEquals(fieldListener.getModelId(), model.getID());
		assertEquals(fieldListener.getRepositoryId(), repo.getID());
		fieldListener.reset();
		
		// Test change-event
		field.setValue(new MemoryStringValue("Test 2"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.CHANGE);
		fieldListener.reset();
		
		field.setValue(new MemoryStringValue("Test 2"));
		assertFalse(fieldListener.hasFired()); // no changes occurs, therefore
		// no event should be fired
		fieldListener.reset();
		
		// Test remove-event
		field.setValue(null);
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
		field.setValue(new MemoryStringValue("Test"));
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
		field.setValue(new MemoryStringValue("Test 2"));
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
		field.setValue(null);
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
	
	@Test
	public void testModelChangeListenening() {
		
		XModel model = new MemoryModel(this.actorId, this.password, XX.createUniqueID());
		DummyModelChangeListener modelListener = new DummyModelChangeListener();
		model.addListenerForModelEvents(modelListener);
		
		// Test add-event
		XObject addObject = model.createObject(XX.createUniqueID());
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
		
		model.createObject(XX.createUniqueID());
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		modelListener.reset();
		
		// Test remove-event
		model.removeObject(addObject.getID());
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		modelListener.reset();
		
	}
	
	// - - - - Dummy implementation of the listeners - - - -
	
	@Test
	public void testModelChangePropagation() {
		
		// Test if event-propagating works
		XRepository repo = X.createMemoryRepository(this.actorId);
		XModel model = repo.createModel(XX.createUniqueID());
		model.createObject(XX.createUniqueID());
		
		DummyModelChangeListener modelListener = new DummyModelChangeListener();
		DummyModelChangeListener repoListener = new DummyModelChangeListener();
		
		repo.addListenerForModelEvents(repoListener);
		model.addListenerForModelEvents(modelListener);
		
		// Test add-event
		XObject addObject = model.createObject(XX.createUniqueID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		modelListener.reset();
		
		// Test remove-event
		model.removeObject(addObject.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testObjectChangeListening() {
		
		MemoryObject object1 = new MemoryObject(this.actorId, this.password, XX.createUniqueID());
		DummyObjectChangeListener listener1 = new DummyObjectChangeListener();
		object1.addListenerForObjectEvents(listener1);
		
		// Test add-event
		// create field and fire event
		MemoryField field1 = object1.createField(XX.createUniqueID());
		
		assertTrue("check if the listener has fired", listener1.hasFired());
		assertEquals("check if the event has the right type", ChangeType.ADD, listener1.getType());
		assertEquals("check if the object of the event is the right one", object1.getID(),
		        listener1.getObject());
		assertEquals("listener1.getModel() is not null but " + listener1.getModelId(), listener1
		        .getModelId(), null);
		assertEquals(listener1.getRepositoryId(), null);
		listener1.reset();
		
		object1.createField(XX.createUniqueID());
		assertTrue(listener1.hasFired());
		assertEquals(ChangeType.ADD, listener1.getType());
		listener1.reset();
		
		// Test remove-event
		object1.removeField(field1.getID());
		assertTrue(listener1.hasFired());
		assertTrue(listener1.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testObjectChangePropagation() {
		
		// Test if event-propagating works
		XRepository repo = X.createMemoryRepository(this.actorId);
		XModel model = repo.createModel(XX.createUniqueID());
		XObject object2 = model.createObject(XX.createUniqueID());
		object2.createField(XX.createUniqueID());
		
		DummyObjectChangeListener repoListener = new DummyObjectChangeListener();
		repo.addListenerForObjectEvents(repoListener);
		
		DummyObjectChangeListener modelListener = new DummyObjectChangeListener();
		model.addListenerForObjectEvents(modelListener);
		
		DummyObjectChangeListener objectListener = new DummyObjectChangeListener();
		object2.addListenerForObjectEvents(objectListener);
		
		XField field3 = object2.createField(XX.createUniqueID());
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
		
		object2.removeField(field3.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		assertTrue(objectListener.hasFired());
		assertTrue(objectListener.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testRepositoryChangeListening() {
		
		XRepository repo = X.createMemoryRepository(this.actorId);
		DummyRepositoryChangeListener repoListener = new DummyRepositoryChangeListener();
		repo.addListenerForRepositoryEvents(repoListener);
		
		// Test add-event
		XModel addModel = repo.createModel(XX.createUniqueID());
		// check if the listener has fired
		assertTrue(repoListener.hasFired());
		// check if the event has the right type
		assertTrue(repoListener.getType() == ChangeType.ADD);
		// check if the repository of the event is the right one
		assertTrue(repo.getID().equals(repoListener.getRepository()));
		repoListener.reset();
		
		repo.createModel(XX.createUniqueID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		repoListener.reset();
		
		// Test remove-event
		repo.removeModel(addModel.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		repoListener.reset();
		
		// Test if event-propagating works
		repo.createModel(XX.createUniqueID());
		
		// Test add-event
		addModel = repo.createModel(XX.createUniqueID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		
		// Test remove-event
		repo.removeModel(addModel.getID());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
	}
	
}
