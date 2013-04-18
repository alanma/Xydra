package org.xydra.core.model.impl.memory;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.value.XV;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


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
	private XModel localModel;
	
	private String password = null; // TODO auth: where to get this?
	private XModel remoteModel;
	
	private XModel dummyModel;
	
	XId repo = XX.toId("remoteRepo");
	XId dummyModelId = XX.toId("dummyModel");
	XId o1Id = XX.toId("object1");
	XId o2Id = XX.toId("object2");
	XId f1Id = XX.toId("f1");
	XId f2Id = XX.toId("f2");
	XId f3Id = XX.toId("f3");
	XId f4Id = XX.toId("f4");
	
	@Before
	public void setUp() {
		
		// create two identical phonebook models
		XRepository remoteRepo = new MemoryRepository(this.actorId, this.password, this.repo);
		DemoModelUtil.addPhonebookModel(remoteRepo);
		this.remoteModel = remoteRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		this.localModel = XCopyUtils.copyModel(this.actorId, this.password, this.remoteModel);
		
		assertTrue(XCompareUtils.equalState(this.localModel, this.remoteModel));
		
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
	 * object1: 0 fields object2: fields f2, f3, f4; f4 has value "false"
	 * 
	 * Expected result:
	 * 
	 * object1: 0 fields object 2: fields f3, f4; f3 has value "false"; F4 has
	 * value "false"
	 */
	@Test
	public void testAddEvent() {
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
		// XX.resolveField(o1Ad, this.f1Id)
		EventDelta eventDelta = new EventDelta();
		eventDelta.addEvent(object1Event);
		eventDelta.addEvent(object2Event);
		eventDelta.addEvent(field3Event);
		eventDelta.addEvent(field4Event);
		eventDelta.addEvent(object1InverseEvent);
		eventDelta.addEvent(field4InverseEvent);
		
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
		
		Assert.assertTrue(revWritableDummyModel.getObject(this.o2Id).getField(this.f3Id).getValue()
		        .equals(XV.toValue(false)));
		Assert.assertTrue(revWritableDummyModel.getObject(this.o2Id).getField(this.f4Id).getValue()
		        .equals(XV.toValue(false)));
		
	}
}
