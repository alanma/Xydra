package org.xydra.core.model.impl.memory;

import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.store.sync.NewSyncer;


/**
 * Test for {@link EventDelta}
 * 
 * @author Andi K.
 */
public class EventDeltaTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private XId actorId = XX.toId("EventDeltaTest");
	
	private String password = null; // TODO auth: where to get this?
	
	private XModel dummyModel;
	
	XId repo = XX.toId("remoteRepo");
	XId dummyModelId = XX.toId("dummyModel");
	XId o1Id = XX.toId("object1");
	XId o2Id = XX.toId("object2");
	XId o3Id = XX.toId("object3");
	XId o4Id = XX.toId("object4");
	XId o5Id = XX.toId("object5");
	XId f1Id = XX.toId("f1");
	XId f2Id = XX.toId("f2");
	XId f3Id = XX.toId("f3");
	XId f4Id = XX.toId("f4");
	
	private void setUp() {
		
		XRepository remoteRepo = new MemoryRepository(this.actorId, this.password, this.repo);
		this.dummyModel = remoteRepo.createModel(this.dummyModelId);
		this.dummyModel.createObject(this.o1Id);
		this.dummyModel.createObject(this.o2Id);
		this.dummyModel.getObject(this.o2Id).createField(this.f2Id);
		this.dummyModel.getObject(this.o2Id).createField(this.f3Id);
		this.dummyModel.getObject(this.o2Id).createField(this.f4Id).setValue(XV.toValue(false));
	}
	
	@After
	public void tearDown() {
	}
	
	/**
	 * add 4 events: 2 object- and 2 field-events. Then add 2 events that are
	 * inverse to both one object - and one field-event. Then apply everything
	 * to an dummy model [2 objects and one has 3 fields] and check the results
	 * 
	 * add 4 object-events: remove object 3, add object 4, add object 5 and
	 * remove object 5
	 * 
	 * basis dummyModel: object1: 0 fields; object2: fields f2, f3, f4; f4 has
	 * value "false"; object3
	 * 
	 * Expected result:
	 * 
	 * object1: 0 fields object2: fields f3, f4; f3 has value "true"; F4 has
	 * value "false";; object4
	 * 
	 * TODO currently doesn't test for repository events
	 */
	@Test
	public void testAddEvent() {
		
		setUp();
		
		XAddress modelAddress = this.dummyModel.getAddress();
		XAddress o1Ad = XX.toAddress(this.repo, this.dummyModelId, this.o1Id, null);
		XAddress o2Ad = XX.toAddress(this.repo, this.dummyModelId, this.o2Id, null);
		
		XObjectEvent object1Event = MemoryObjectEvent.createAddEvent(this.actorId, o1Ad, this.f1Id,
		        0, false);
		XObjectEvent object2Event = MemoryObjectEvent.createRemoveEvent(this.actorId, o2Ad,
		        this.f2Id, 0, 0, false, false);
		XFieldEvent field3Event = MemoryFieldEvent.createAddEvent(this.actorId,
		        XX.resolveField(o2Ad, this.f3Id), XV.toValue(true), 0, 0, false);
		XFieldEvent field4Event = MemoryFieldEvent.createRemoveEvent(this.actorId,
		        XX.resolveField(o2Ad, this.f4Id), 0, 0, false, false);
		
		XObjectEvent object1InverseEvent = MemoryObjectEvent.createRemoveEvent(this.actorId, o1Ad,
		        this.f1Id, 0, 0, false, false);
		XFieldEvent field4InverseEvent = MemoryFieldEvent.createAddEvent(this.actorId,
		        XX.resolveField(o2Ad, this.f4Id), XV.toValue(false), 0, 0, false);
		
		XModelEvent object3Event = MemoryModelEvent.createRemoveEvent(this.actorId, modelAddress,
		        this.o3Id, 0, 0, false, false);
		XModelEvent object4Event = MemoryModelEvent.createAddEvent(this.actorId, modelAddress,
		        this.o4Id, 0, false);
		XModelEvent object5Event = MemoryModelEvent.createAddEvent(this.actorId, modelAddress,
		        this.o5Id, 0, false);
		
		XModelEvent object5InverseEvent = MemoryModelEvent.createRemoveEvent(this.actorId,
		        modelAddress, this.o5Id, 0, 0, false, false);
		
		EventDelta eventDelta = new EventDelta();
		eventDelta.addEvent(object1Event);
		eventDelta.addEvent(object2Event);
		eventDelta.addEvent(field3Event);
		eventDelta.addEvent(field4Event);
		eventDelta.addEvent(object1InverseEvent);
		eventDelta.addEvent(field4InverseEvent);
		eventDelta.addEvent(object3Event);
		eventDelta.addEvent(object4Event);
		eventDelta.addEvent(object5Event);
		eventDelta.addEvent(object5InverseEvent);
		
		XRevWritableModel revWritableDummyModel = XCopyUtils.createSnapshot(this.dummyModel);
		
		eventDelta.applyTo(revWritableDummyModel);
		
		System.out.println(revWritableDummyModel);
		
		/* check, if the model contains the right entities */
		
		Iterator<XId> modelsObjectsIterator = revWritableDummyModel.iterator();
		HashSet<XId> modelsObjects = new HashSet<XId>();
		while(modelsObjectsIterator.hasNext()) {
			XId xId = (XId)modelsObjectsIterator.next();
			modelsObjects.add(xId);
		}
		
		Assert.assertTrue(modelsObjects.contains(this.o1Id));
		Assert.assertTrue(modelsObjects.contains(this.o2Id));
		
		Assert.assertTrue(revWritableDummyModel.getObject(this.o1Id).isEmpty());
		
		HashSet<XId> o2Fields = new HashSet<XId>();
		Iterator<XId> o2Iterator = revWritableDummyModel.getObject(this.o2Id).iterator();
		while(o2Iterator.hasNext()) {
			XId fieldId = o2Iterator.next();
			o2Fields.add(fieldId);
		}
		
		Assert.assertTrue(o2Fields.contains(this.f3Id));
		Assert.assertTrue(o2Fields.contains(this.f4Id));
		
		XValue f3Value = revWritableDummyModel.getObject(this.o2Id).getField(this.f3Id).getValue();
		Assert.assertTrue(f3Value.equals(XV.toValue(true)));
		XValue f4Value = revWritableDummyModel.getObject(this.o2Id).getField(this.f4Id).getValue();
		Assert.assertTrue(f4Value.equals(XV.toValue(false)));
		
		Assert.assertTrue(!revWritableDummyModel.hasObject(this.o3Id));
		Assert.assertTrue(revWritableDummyModel.hasObject(this.o4Id));
		Assert.assertTrue(!revWritableDummyModel.hasObject(this.o5Id));
		
	}
	
	/**
	 * add 3 fieldChange-Events to field 4: true,false,true and make sure, the
	 * latest one's (Nr.1's) revisions are taken with the value of the newest
	 * one's value
	 * 
	 * basis dummyModel: object1: 0 fields; object2: fields f2, f3, f4; f4 has
	 * value "false"; object3
	 * 
	 * Expected result:
	 * 
	 * object1: 0 fields object2: fields f3, f4; f3 has value "true"; F4 has
	 * value "true" and revision numbers
	 * 
	 * TODO currently doesn't test for repository events
	 */
	@Test
	public void testAddEvent_MultipleFieldChanges() {
		
		setUp();
		
		XAddress o2Ad = XX.toAddress(this.repo, this.dummyModelId, this.o2Id, null);
		
		XFieldEvent changeEvent1 = MemoryFieldEvent.createChangeEvent(this.actorId,
		        XX.resolveField(o2Ad, this.f4Id), XV.toValue(true), 4, 4, 4, false);
		XFieldEvent changeEvent2 = MemoryFieldEvent.createChangeEvent(this.actorId,
		        XX.resolveField(o2Ad, this.f4Id), XV.toValue(false), 5, 5, 5, false);
		XFieldEvent changeEvent3 = MemoryFieldEvent.createChangeEvent(this.actorId,
		        XX.resolveField(o2Ad, this.f4Id), XV.toValue(true), 6, 6, 6, false);
		
		EventDelta eventDelta = new EventDelta();
		eventDelta.addEvent(changeEvent1);
		eventDelta.addEvent(changeEvent2);
		eventDelta.addEvent(changeEvent3);
		
		XRevWritableModel revWritableDummyModel = XCopyUtils.createSnapshot(this.dummyModel);
		eventDelta.applyTo(revWritableDummyModel);
		
		Assert.assertTrue(revWritableDummyModel.getObject(this.o2Id).getField(this.f4Id).getValue()
		        .equals(XV.toValue(true)));
	}
	
	@Test
	public void testWithNewDemoData() {
		EventDelta eventDelta = new EventDelta();
		
		/* add server changes to the EventDelta */
		XRepository serverRepo = new MemoryRepository(this.actorId, this.password, this.repo);
		DemoModelUtil.addPhonebookModel(serverRepo);
		Iterator<XEvent> serverEvents = DemoLocalChangesAndServerEvents
		        .getServerChanges(serverRepo);
		int count = 0;
		while(serverEvents.hasNext()) {
			XEvent serverEvent = (XEvent)serverEvents.next();
			eventDelta.addEvent(serverEvent);
			count++;
		}
		serverRepo.removeModel(DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.addPhonebookModel(serverRepo);
		XEvent[] serverEventArray = new XEvent[count];
		
		serverEvents = DemoLocalChangesAndServerEvents.getServerChanges(serverRepo);
		count = 0;
		while(serverEvents.hasNext()) {
			XEvent xEvent = (XEvent)serverEvents.next();
			serverEventArray[count] = xEvent;
			count++;
		}
		
		/* add local changes to the EventDelta */
		XRepository localRepo = new MemoryRepository(this.actorId, this.password, this.repo);
		localRepo.createModel(XX.toId("trial"));
		XModel secondModel = localRepo.getModel(XX.toId("trial"));
		DemoModelUtil.addPhonebookModel(localRepo);
		XModel localModel = localRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XCopyUtils.copyData(localModel, secondModel);
		DemoLocalChangesAndServerEvents.addLocalChangesToModel(localModel);
		XChangeLog localChangeLog = localModel.getChangeLog();
		
		Iterator<XEvent> localEventIterator = localChangeLog
		        .getEventsSince(DemoLocalChangesAndServerEvents.SYNCREVISION);
		while(localEventIterator.hasNext()) {
			XEvent localEvent = (XEvent)localEventIterator.next();
			eventDelta.addInverseEvent(localEvent, DemoLocalChangesAndServerEvents.SYNCREVISION,
			        localChangeLog);
		}
		
		/* check, if the Delta is just as it should be */
		
		XRepository referenceRepo = new MemoryRepository(this.actorId, this.password, this.repo);
		DemoModelUtil.addPhonebookModel(referenceRepo);
		XRevWritableModel referenceModel = DemoLocalChangesAndServerEvents
		        .getResultingClientState(referenceRepo);
		XExistsRevWritableModel localModel2 = XCopyUtils.createSnapshot(localModel);
		eventDelta.applyTo(localModel2);
		XRevWritableModel localModelWithRevisions = XCopyUtils.createSnapshot(localModel2);
		NewSyncer.applyEntityRevisionsToModel(serverEventArray, localModelWithRevisions);
		
		Assert.assertTrue(XCompareUtils.equalState(localModelWithRevisions, referenceModel));
	}
}
