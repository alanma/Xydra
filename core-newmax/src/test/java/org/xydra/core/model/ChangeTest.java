package org.xydra.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.value.impl.memory.MemoryStringValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


public class ChangeTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private static class DummyFieldChangeListener implements XFieldEventListener {
		private XId field;
		public boolean fired = false;
		private XId model;
		private XId object;
		private XId repo;
		private ChangeType type;
		
		public XId getFieldId() {
			return this.field;
		}
		
		public XId getModelId() {
			return this.model;
		}
		
		public XId getObjectId() {
			return this.object;
		}
		
		public XId getRepositoryId() {
			return this.repo;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		@Override
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
		private XId model;
		private XId repo;
		private ChangeType type;
		
		public XId getModel() {
			return this.model;
		}
		
		public XId getRepository() {
			return this.repo;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		@Override
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
		private XId modelId;
		private XId objectId;
		private XId repoID;
		private ChangeType type;
		
		public XId getModelId() {
			return this.modelId;
		}
		
		public XId getObject() {
			return this.objectId;
		}
		
		public XId getRepositoryId() {
			return this.repoID;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		@Override
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
		private XId repo;
		private ChangeType type;
		
		public XId getRepository() {
			return this.repo;
		}
		
		public ChangeType getType() {
			return this.type;
		}
		
		public boolean hasFired() {
			return this.fired;
		}
		
		@Override
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
	
	private XId actorId = XX.toId("AbstractChangeTest");
	
	private String password = null;
	
	{
		LoggerTestHelper.init();
	}
	
	@Test
	public void testFieldChangeListening() {
		// Prepare other objects for event propagating test later
		XRepository repo = X.createMemoryRepository(this.actorId);
		XModel model = repo.createModel(XX.createUniqueId());
		XObject object = model.createObject(XX.createUniqueId());
		XField field = object.createField(XX.createUniqueId());
		
		DummyFieldChangeListener fieldListener = new DummyFieldChangeListener();
		field.addListenerForFieldEvents(fieldListener);
		
		// Test add-event
		field.setValue(new MemoryStringValue("Test"));
		assertTrue(fieldListener.hasFired());
		assertTrue(fieldListener.getType() == ChangeType.ADD);
		assertEquals(fieldListener.getFieldId(), field.getId());
		assertEquals(fieldListener.getObjectId(), object.getId());
		assertEquals(fieldListener.getModelId(), model.getId());
		assertEquals(fieldListener.getRepositoryId(), repo.getId());
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
		
		XModel model = new MemoryModel(this.actorId, this.password, XX.createUniqueId());
		DummyModelChangeListener modelListener = new DummyModelChangeListener();
		model.addListenerForModelEvents(modelListener);
		
		// Test add-event
		XObject addObject = model.createObject(XX.createUniqueId());
		assertTrue("check if the listener has fired", modelListener.hasFired());
		assertTrue("check if the event has the right type",
		        modelListener.getType() == ChangeType.ADD);
		assertEquals("check if the model of the event is the right one", model.getId(),
		        modelListener.getModel());
		assertNotNull(modelListener.getRepository());
		
		modelListener.reset();
		
		model.createObject(XX.createUniqueId());
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		modelListener.reset();
		
		// Test remove-event
		model.removeObject(addObject.getId());
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		modelListener.reset();
		
	}
	
	// - - - - Dummy implementation of the listeners - - - -
	
	@Test
	public void testModelChangePropagation() {
		
		// Test if event-propagating works
		XRepository repo = X.createMemoryRepository(this.actorId);
		XModel model = repo.createModel(XX.createUniqueId());
		model.createObject(XX.createUniqueId());
		
		DummyModelChangeListener modelListener = new DummyModelChangeListener();
		DummyModelChangeListener repoListener = new DummyModelChangeListener();
		
		repo.addListenerForModelEvents(repoListener);
		model.addListenerForModelEvents(modelListener);
		
		// Test add-event
		XObject addObject = model.createObject(XX.createUniqueId());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		modelListener.reset();
		
		// Test remove-event
		model.removeObject(addObject.getId());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
		assertTrue(modelListener.hasFired());
		assertTrue(modelListener.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testObjectChangeListening() {
		
		MemoryObject object1 = new MemoryObject(this.actorId, this.password, XX.createUniqueId());
		assertTrue(object1.exists());
		assertTrue("rev=" + object1.getRevisionNumber(), object1.getRevisionNumber() >= 0);
		
		DummyObjectChangeListener listener1 = new DummyObjectChangeListener();
		object1.addListenerForObjectEvents(listener1);
		
		// Test add-event
		// create field and fire event
		XField field1 = object1.createField(XX.createUniqueId());
		
		assertTrue("check if the listener has fired", listener1.hasFired());
		assertEquals("check if the event has the right type", ChangeType.ADD, listener1.getType());
		assertEquals("check if the object of the event is the right one", object1.getId(),
		        listener1.getObject());
		assertEquals("listener1.getModel() is not null but " + listener1.getModelId(), XId.DEFAULT,
		        listener1.getModelId());
		assertEquals(XId.DEFAULT, listener1.getRepositoryId());
		listener1.reset();
		
		object1.createField(XX.createUniqueId());
		assertTrue(listener1.hasFired());
		assertEquals(ChangeType.ADD, listener1.getType());
		listener1.reset();
		
		// Test remove-event
		object1.removeField(field1.getId());
		assertTrue(listener1.hasFired());
		assertTrue(listener1.getType() == ChangeType.REMOVE);
		
	}
	
	@Test
	public void testObjectChangePropagation() {
		
		// Test if event-propagating works
		XRepository repo = X.createMemoryRepository(this.actorId);
		XModel model = repo.createModel(XX.createUniqueId());
		XObject object2 = model.createObject(XX.createUniqueId());
		object2.createField(XX.createUniqueId());
		
		DummyObjectChangeListener repoListener = new DummyObjectChangeListener();
		repo.addListenerForObjectEvents(repoListener);
		
		DummyObjectChangeListener modelListener = new DummyObjectChangeListener();
		model.addListenerForObjectEvents(modelListener);
		
		DummyObjectChangeListener objectListener = new DummyObjectChangeListener();
		object2.addListenerForObjectEvents(objectListener);
		
		XField field3 = object2.createField(XX.createUniqueId());
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
		
		object2.removeField(field3.getId());
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
		XModel addModel = repo.createModel(XX.createUniqueId());
		// check if the listener has fired
		assertTrue(repoListener.hasFired());
		// check if the event has the right type
		assertTrue(repoListener.getType() == ChangeType.ADD);
		// check if the repository of the event is the right one
		assertTrue(repo.getId().equals(repoListener.getRepository()));
		repoListener.reset();
		
		repo.createModel(XX.createUniqueId());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		repoListener.reset();
		
		// Test remove-event
		repo.removeModel(addModel.getId());
		assertTrue(repoListener.hasFired());
		assertTrue("repoListener.Type=" + repoListener.getType(),
		        repoListener.getType() == ChangeType.REMOVE);
		repoListener.reset();
		
		// Test if event-propagating works
		repo.createModel(XX.createUniqueId());
		
		// Test add-event
		addModel = repo.createModel(XX.createUniqueId());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.ADD);
		
		repoListener.reset();
		
		// Test remove-event
		repo.removeModel(addModel.getId());
		assertTrue(repoListener.hasFired());
		assertTrue(repoListener.getType() == ChangeType.REMOVE);
	}
	
}
