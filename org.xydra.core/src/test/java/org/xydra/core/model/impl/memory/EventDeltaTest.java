package org.xydra.core.model.impl.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.Base;
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
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.sync.NewSyncer;


/**
 * Test for {@link EventDelta}
 *
 * @author kahmann
 */
public class EventDeltaTest {

    private static final Logger log = LoggerFactory.getLogger(EventDeltaTest.class);

    @BeforeClass
    public static void init() {
        LoggerTestHelper.init();
    }

    private final XId actorId = XX.toId("EventDeltaTest");

    private final String password = null;

    private IMemoryModel model;

    XId repo = XX.toId("remoteRepo");
    XId modelId = XX.toId("model1");
    XId o1Id = XX.toId("object1");
    XId o2Id = XX.toId("object2");
    XId o3Id = XX.toId("object3");
    XId o4Id = XX.toId("object4");
    XId o5Id = XX.toId("object5");
    XId f1Id = XX.toId("f1");
    XId f2Id = XX.toId("f2");
    XId f3Id = XX.toId("f3");
    XId f4Id = XX.toId("f4");
    XId f5Id = XX.toId("f5");

    @Before
    public void setUp() {
        final IMemoryRepository remoteRepo = new MemoryRepository(this.actorId, this.password, this.repo);
        this.model = (IMemoryModel)remoteRepo.createModel(this.modelId);
        this.model.createObject(this.o1Id);
        final XObject object2 = this.model.createObject(this.o2Id);
        object2.createField(this.f2Id);
        object2.createField(this.f3Id);
        object2.createField(this.f4Id).setValue(XV.toValue(false));
        log.info("Created model: \n" + DumpUtilsBase.toStringBuffer(this.model));
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
     */
    @Test
    public void testAddEvent() {

        final XAddress modelAddress = this.model.getAddress();
        final XAddress o1Addr = Base.toAddress(this.repo, this.modelId, this.o1Id, null);
        final XAddress o2Addr = Base.toAddress(this.repo, this.modelId, this.o2Id, null);

        /* ADD o1.f1 */
        final XObjectEvent object1Event = MemoryObjectEvent.createAddEvent(this.actorId, o1Addr,
                this.f1Id, 0, false);
        /* REMOVE o2.f2 */
        final XObjectEvent object2Event = MemoryObjectEvent.createRemoveEvent(this.actorId, o2Addr,
                this.f2Id, 0, 0, false, false);
        /* ADD o2.f3.value="true" */
        final XFieldEvent field3Event = MemoryFieldEvent.createAddEvent(this.actorId,
                Base.resolveField(o2Addr, this.f3Id), XV.toValue(true), 0, 0, false);
        /* REMOVE o2.f4.value */
        final XFieldEvent field4Event = MemoryFieldEvent.createRemoveEvent(this.actorId,
                Base.resolveField(o2Addr, this.f4Id), 0, 0, false, false);

        /* REMOVE o1.f1 */
        final XObjectEvent object1InverseEvent = MemoryObjectEvent.createRemoveEvent(this.actorId,
                o1Addr, this.f1Id, 0, 0, false, false);
        /* ADD o2.f4.value="false" */
        final XFieldEvent field4InverseEvent = MemoryFieldEvent.createAddEvent(this.actorId,
                Base.resolveField(o2Addr, this.f4Id), XV.toValue(false), 0, 0, false);
        /* REMOVE o3 */
        final XModelEvent object3Event = MemoryModelEvent.createRemoveEvent(this.actorId, modelAddress,
                this.o3Id, 0, 0, false, false);
        /* ADD o4 */
        final XModelEvent object4Event = MemoryModelEvent.createAddEvent(this.actorId, modelAddress,
                this.o4Id, 0, false);
        /* ADD o5 */
        final XModelEvent object5Event = MemoryModelEvent.createAddEvent(this.actorId, modelAddress,
                this.o5Id, 0, false);
        /* REMOVE o5 */
        final XModelEvent object5InverseEvent = MemoryModelEvent.createRemoveEvent(this.actorId,
                modelAddress, this.o5Id, 0, 0, false, false);

        final EventDelta eventDelta = new EventDelta();
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
        /*
         * Expected RESULTING EFFECTS:
         *
         * o1: ADD f1,REMOVE f1 =>> --
         *
         * o2: REMOVE f2, f3=true, f4=false =>> REMOVE f2, f3=true, f4=false
         *
         * o3: REMOVE =>> REMOVE
         *
         * o4: f4=null, ADD =>> f4=null
         *
         * o5: ADD, REMOVE =>> --
         */

        /*
         * Initial Model * Model /remoteRepo/model1/-/- [6]
         *
         * ** Object /remoteRepo/model1/object1/- [1]
         *
         * ** Object /remoteRepo/model1/object2/- [6]
         *
         * *** Field /remoteRepo/model1/object2/f2 = 'null' X-type=NoValue [3]
         *
         * *** Field /remoteRepo/model1/object2/f3 = 'null' X-type=NoValue [4]
         *
         * *** Field /remoteRepo/model1/object2/f4 = 'false' X-type=Boolean [6]
         */

        /*
         * Expected model state
         *
         * * Model /remoteRepo/model1/-/-
         *
         * ** Object /remoteRepo/model1/object1/-
         *
         * ** Object /remoteRepo/model1/object2/-
         *
         * *** Field /remoteRepo/model1/object2/f3 = 'true' X-type=Boolean
         *
         * *** Field /remoteRepo/model1/object2/f4 = 'false' X-type=Boolean
         *
         * ** Object /remoteRepo/model1/object4/-
         */

        // verify event delta
        final XRevWritableModel model_t2 = XCopyUtils.createSnapshot(this.model);
        eventDelta.applyTo(model_t2);

        System.out.println("Changed model:\n" + model_t2);

        /* check, if the model contains the right entities */
        assertTrue(model_t2.hasObject(this.o1Id));
        assertTrue(model_t2.getObject(this.o1Id).isEmpty());

        assertTrue(model_t2.hasObject(this.o2Id));
        final XRevWritableObject object2 = model_t2.getObject(this.o2Id);
        assertFalse(object2.hasField(this.f1Id));
        assertFalse(object2.hasField(this.f2Id));
        assertTrue(object2.hasField(this.f3Id));
        assertTrue(object2.hasField(this.f4Id));
        final XRevWritableField object2_f3 = object2.getField(this.f3Id);
        final XRevWritableField object2_f4 = object2.getField(this.f4Id);
        final XValue f3Value = object2_f3.getValue();
        assertTrue(f3Value.equals(XV.toValue(true)));
        final XValue f4Value = object2_f4.getValue();
        assertNotNull(f4Value);
        assertTrue(f4Value.equals(XV.toValue(false)));

        assertFalse(model_t2.hasObject(this.o3Id));
        assertTrue(model_t2.hasObject(this.o4Id));
        assertFalse(model_t2.hasObject(this.o5Id));
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
     */
    @Test
    public void testAddEvent_MultipleFieldChanges() {

        final XAddress o2Ad = Base.toAddress(this.repo, this.modelId, this.o2Id, null);

        final XFieldEvent changeEvent1 = MemoryFieldEvent.createChangeEvent(this.actorId,
                Base.resolveField(o2Ad, this.f4Id), XV.toValue(true), 4, 4, 4, false);
        final XFieldEvent changeEvent2 = MemoryFieldEvent.createChangeEvent(this.actorId,
                Base.resolveField(o2Ad, this.f4Id), XV.toValue(false), 5, 5, 5, false);
        final XFieldEvent changeEvent3 = MemoryFieldEvent.createChangeEvent(this.actorId,
                Base.resolveField(o2Ad, this.f4Id), XV.toValue(true), 6, 6, 6, false);

        final EventDelta eventDelta = new EventDelta();
        eventDelta.addEvent(changeEvent1);
        eventDelta.addEvent(changeEvent2);
        eventDelta.addEvent(changeEvent3);

        final XRevWritableModel revWritableDummyModel = XCopyUtils.createSnapshot(this.model);
        eventDelta.applyTo(revWritableDummyModel);

        assertTrue(revWritableDummyModel.getObject(this.o2Id).getField(this.f4Id).getValue()
                .equals(XV.toValue(true)));
    }

    /**
     *
     */
    @Test
    public void testWithNewDemoData() {
        final EventDelta eventDelta = new EventDelta();
        // S1 = phonebook + server changes
        final XEvent[] serverEvents = applyPhonebookPlusSimulatedServerChangesToEventDelta(eventDelta);
        System.out.println("--- server = phonebook + simulated serverChanges");
        eventDelta.dump();
        System.out.println("--- /server = phonebook + simulated serverChanges");

        /*
         * reverse local changes in EventDelta.
         */
        final XModel localModel = applyInverseSimulatedLocalChangesToEventDelta(eventDelta);
        System.out.println("--- phonebook + local");
        // assert localModel has claudias car = "911"
        System.out.println(DumpUtilsBase.toStringBuffer(localModel));
        System.out.println("--- /phonebook + local");
        System.out.println("--- phonebook + server - local");
        eventDelta.dump();
        System.out.println("--- /phonebook + server - local");

        // P0
        /* check, if the Delta is just as it should be */
        final XRepository referenceRepo = new MemoryRepository(this.actorId, this.password, this.repo);
        // P1
        DemoModelUtil.addPhonebookModel(referenceRepo);
        // P2
        final XRevWritableModel referenceModel = DemoLocalChangesAndServerEvents
                .getResultingClientState(referenceRepo);
        System.out.println("--- referenceModel with 911S");
        System.out.println(DumpUtilsBase.toStringBuffer(referenceModel));
        System.out.println("--- /referenceModel");

        /* copy localPhonebook, add changes from 'server' in two steps */
        final XExistsRevWritableModel localModel2 = XCopyUtils.createSnapshot(localModel);

        // 911 --> 911S is still in the eventDelta
        eventDelta.applyTo(localModel2);
        final XRevWritableModel localModelWithRevisions = XCopyUtils.createSnapshot(localModel2);
        // simply copy revs over, so they match for sure
        NewSyncer.applyEntityRevisionsToModel(serverEvents, localModelWithRevisions);

        System.out.println("--- localModelWithRevisions");
        System.out.println(DumpUtilsBase.toStringBuffer(localModelWithRevisions));
        System.out.println("--- /localModelWithRevisions");

        assertTrue(XCompareUtils.equalState(localModelWithRevisions, referenceModel));
    }

    /**
     * Add phoneBookModel + additional changes to eventDelta
     *
     * @param eventDelta
     * @return
     */
    private XEvent[] applyPhonebookPlusSimulatedServerChangesToEventDelta(final EventDelta eventDelta) {
        /* Simulate changes on remote repo */
        final XRepository remoteRepo = new MemoryRepository(this.actorId, this.password, this.repo);
        DemoModelUtil.addPhonebookModel(remoteRepo);
        Iterator<XEvent> remoteEvents = DemoLocalChangesAndServerEvents
                .applyAndGetServerChanges(remoteRepo);

        /* add server changes to the EventDelta */
        int count = 0;
        while(remoteEvents.hasNext()) {
            final XEvent serverEvent = remoteEvents.next();
            eventDelta.addEvent(serverEvent);
            count++;
        }

        final XEvent[] serverEvents = new XEvent[count];
        remoteEvents = DemoLocalChangesAndServerEvents.applyAndGetServerChanges(remoteRepo);
        count = 0;
        while(remoteEvents.hasNext()) {
            final XEvent xEvent = remoteEvents.next();
            serverEvents[count] = xEvent;
            count++;
        }
        return serverEvents;
    }

    /**
     * Sets among other things Claudias car to "911"
     *
     * @param eventDelta reverse all local changes here
     * @return phonebook + local changes (claudis car = "911")
     */
    private XModel applyInverseSimulatedLocalChangesToEventDelta(final EventDelta eventDelta) {
        final XRepository localRepo = new MemoryRepository(this.actorId, this.password, this.repo);
        DemoModelUtil.addPhonebookModel(localRepo);
        final XModel localPhonebookModel = localRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
        final ISyncLog syncLog = (ISyncLog)localPhonebookModel.getChangeLog();
        syncLog.setSynchronizedRevision(DemoModelUtil.REVISION_AFTER_ADDING_INCLUDING_MODEL_ITSELF);

        // model = phonebook + local changes
        DemoLocalChangesAndServerEvents.addLocalChangesToModel(localPhonebookModel);
        final XChangeLog localChangeLog = localPhonebookModel.getChangeLog();
        assert localChangeLog instanceof ISyncLog;
        final ISyncLog localSyncLog = (ISyncLog)localChangeLog;

        // undo all changes after SYNC_REV in eventDelta
        final Iterator<XEvent> localEventIterator = localChangeLog
                .getEventsSince(DemoLocalChangesAndServerEvents.SYNC_REVISION + 1);
        while(localEventIterator.hasNext()) {
            final XEvent localEvent = localEventIterator.next();
            eventDelta.addInverseEvent(localEvent, localSyncLog);
        }
        return localPhonebookModel;
    }

    /**
     * change field value two times. Then nothing [no positive server
     * responses]. EventDelta should be empty. Remote revision numbers are
     * completely different here.
     */
    @Test
    public void testAddMultipleFieldEvent() {

        assert this.model.getObject(this.o2Id).hasField(this.f2Id);

        final EventDelta eventDelta = new EventDelta();

        final XField field = this.model.getObject(this.o2Id).getField(this.f2Id);
        field.setValue(XV.toValue("A"));
        field.setValue(XV.toValue("B"));
        final ISyncLog syncLog = this.model.getRoot().getSyncLog();

        Iterator<XEvent> it = syncLog.getEventsSince(7);
        int count = 0;
        while(it.hasNext()) {
            final XEvent e = it.next();
            final MemoryFieldEvent m = (MemoryFieldEvent)e;
            /* simulate different remote revision */
            final XFieldEvent newEvent = MemoryFieldEvent.createChangeEvent(m.getActor(), m.getTarget(),
                    m.getNewValue(), 999 + count, 999 + count, false);
            System.out.println("REMOTE=" + e);
            eventDelta.addEvent(newEvent);
            count++;
        }

        // add same events inverted
        it = syncLog.getEventsSince(7);
        while(it.hasNext()) {
            final XEvent e = it.next();
            System.out.println("LOCAL =" + e);
            eventDelta.addInverseEvent(e, syncLog);
        }

        System.out.println("EVENTDELTA=" + eventDelta);

        assertEquals(0, eventDelta.getEventCount());
    }

    /**
     * change field value two times. Then nothing [no positive server
     * responses]. EventDelta should be empty. Remote revision numbers differ
     * only slightly here
     */
    @Test
    public void testAddMultipleFieldEvent2() {

        assert this.model.getObject(this.o2Id).hasField(this.f2Id);

        final EventDelta eventDelta = new EventDelta();

        final XField field = this.model.getObject(this.o2Id).getField(this.f2Id);
        field.setValue(XV.toValue("A"));
        field.setValue(XV.toValue("B"));
        final ISyncLog syncLog = this.model.getRoot().getSyncLog();

        /* add some events in between */
        this.model.createObject(Base.toId("dummy1"));
        this.model.createObject(Base.toId("dummy2"));
        Iterator<XEvent> it = syncLog.getEventsSince(7);
        int count = 0;
        while(it.hasNext()) {
            final XEvent e = it.next();
            if(e instanceof MemoryFieldEvent) {
                final MemoryFieldEvent m = (MemoryFieldEvent)e;
                /* simulate different remote revision */
                final XFieldEvent newEvent = MemoryFieldEvent.createChangeEvent(m.getActor(),
                        m.getTarget(), m.getNewValue(), 8 + count, 8 + count, false);
                System.out.println("REMOTE=" + e);
                eventDelta.addEvent(newEvent);
                count++;
            }
        }

        // add same events inverted
        it = syncLog.getEventsSince(7);
        while(it.hasNext()) {
            final XEvent e = it.next();
            System.out.println("LOCAL =" + e);
            eventDelta.addInverseEvent(e, syncLog);
        }

        System.out.println("EVENTDELTA=" + eventDelta);

        assertEquals(2, eventDelta.getEventCount());
    }

    /**
     * change field value two times. Then nothing [no positive server
     * responses]. EventDelta should be empty. Remote revision numbers differ
     * only slightly here
     */
    @Test
    public void testAddMultipleFieldEventAllRemoteChangesFailing() {

        assert this.model.getObject(this.o2Id).hasField(this.f2Id);

        final EventDelta eventDelta = new EventDelta();

        final XField field = this.model.getObject(this.o2Id).getField(this.f2Id);
        field.setValue(XV.toValue("A"));
        field.setValue(XV.toValue("B"));
        final ISyncLog syncLog = this.model.getRoot().getSyncLog();

        /* add some events in between */
        Iterator<XEvent> it = syncLog.getEventsSince(7);

        // add same events inverted
        it = syncLog.getEventsSince(7);
        while(it.hasNext()) {
            final XEvent e = it.next();
            System.out.println("LOCAL =" + e);
            eventDelta.addInverseEvent(e, syncLog);
        }

        System.out.println("EVENTDELTA=" + eventDelta);

        assertEquals(1, eventDelta.getEventCount());
    }

    /**
     * create all kinds of entity changes and test, if the right revision
     * numbers were restored afterwards
     */
    @Test
    public void testRevisionnumberRestoration() {

        assert this.model.getObject(this.o2Id).hasField(this.f2Id);

        final EventDelta eventDelta = new EventDelta();
        this.model.getObject(this.o2Id).createField(this.f5Id).setValue(XV.toValue("X"));
        this.model.getRoot().getSyncLog().setSynchronizedRevision(8);
        this.model.getRoot().getSyncLog().clearLocalChanges();

        final XExistsRevWritableModel model2 = XCopyUtils.createSnapshot(this.model);
        // create and remove object
        this.model.createObject(this.o3Id);
        this.model.removeObject(this.o1Id);

        // create and remove field
        final XObject o2 = this.model.getObject(this.o2Id);
        o2.createField(this.f1Id);
        o2.removeField(this.f2Id);

        // create and remove value
        o2.getField(this.f3Id).setValue(XV.toValue(true));
        o2.getField(this.f4Id).setValue(null);

        // change value 3 times
        final XField f5 = o2.getField(this.f5Id);
        f5.setValue(XV.toValue("A"));
        f5.setValue(XV.toValue("B"));
        f5.setValue(XV.toValue("C"));

        final ISyncLog syncLog = this.model.getRoot().getSyncLog();

        final Iterator<XEvent> it = syncLog.getEventsSince(9);

        // add same events inverted
        while(it.hasNext()) {
            final XEvent e = it.next();
            System.out.println("LOCAL =" + e);
            eventDelta.addInverseEvent(e, syncLog);
        }

        System.out.println("EVENTDELTA=" + eventDelta);

        final XExistsRevWritableModel model3 = XCopyUtils.createSnapshot(this.model);
        eventDelta.applyTo(model3);

        assertTrue(XCompareUtils.equalState(model2, model3));
    }
}
