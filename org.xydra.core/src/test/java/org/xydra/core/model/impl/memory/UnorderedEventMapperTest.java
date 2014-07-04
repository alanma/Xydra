package org.xydra.core.model.impl.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryCommandFactory;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryTransaction;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.sync.IEventMapper.IMappingResult;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.ISyncLogEntry;
import org.xydra.core.model.impl.memory.sync.UnorderedEventMapper;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class UnorderedEventMapperTest {
    
    private static final Logger log = LoggerFactory.getLogger(UnorderedEventMapperTest.class);
    
    private static final XId ACTOR_ID = XX.toId("actor1");
    
    private static final XId REPO_ID = XX.toId("repo1");
    private static final XId MODEL_ID = XX.toId("model1");
    private static final XId OBJECT_ID = XX.toId("object1");
    private static final XAddress OBJECT_ADDRESS = XX.toAddress(REPO_ID, MODEL_ID, OBJECT_ID, null);
    private static final XId FIELD_A_ID = XX.toId("A");
    private static final XId FIELD_B_ID = XX.toId("B");
    private static final XId FIELD_C_ID = XX.toId("C");
    
    private static XEvent createFieldAddedEvent(XAddress objectAddress, XId fieldId) {
        // set an arbitrary revision number 1000 for testing
        return MemoryObjectEvent.createAddEvent(ACTOR_ID, objectAddress, fieldId, 1000, false);
    }
    
    @SuppressWarnings("unused")
    private static XCommand createFieldAddedCommand(XAddress objectAddress, XId fieldId) {
        return MemoryObjectCommand.createAddCommand(objectAddress, true, fieldId);
    }
    
    /**
     * Three local changes which shall all be found from server events; event1
     * and event 2 are from one transaction
     */
    @Test
    public void testAllServerMappedAllLocalMapped() {
        
        XEvent addFieldA = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_A_ID);
        XEvent addFieldB = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_B_ID);
        XEvent addFieldC = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_C_ID);
        
        XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC };
        
        MemoryRepository memoryRepository = new MemoryRepository(ACTOR_ID, null, REPO_ID);
        IMemoryModel memoryModel = memoryRepository.createModel(MODEL_ID);
        XObject object2 = memoryModel.createObject(OBJECT_ID);
        object2.createField(FIELD_A_ID);
        object2.createField(FIELD_B_ID);
        object2.createField(FIELD_C_ID);
        
        ISyncLog syncLog = memoryModel.getRoot().getSyncLog();
        assertEquals(5, syncLog.getSize());
        // set syncRev to ignore ADD-model and ADD-object
        syncLog.setSynchronizedRevision(1);
        
        ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
        Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(2);
        while(iterator.hasNext()) {
            ISyncLogEntry entry = iterator.next();
            log.info("SyncLogEntry=" + entry);
            syncLogEntryList.add(entry);
        }
        
        UnorderedEventMapper mapper = new UnorderedEventMapper();
        IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
        
        assertTrue("unmapped=" + result.getUnmappedLocalEvents(), result.getUnmappedLocalEvents()
                .isEmpty());
        assertTrue(result.getUnmappedRemoteEvents().isEmpty());
        assertEquals(result.getMapped().size(), serverEvents.length);
        assertEquals(result.getMapped().size(), syncLogEntryList.size());
    }
    
    /**
     * Three local changes which shall all be found from server events; event1
     * and event 2 are from one transaction
     */
    @Test
    public void testAllServerMappedAllLocalMappedWithTransaction() {
        
        XEvent addFieldA = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_A_ID);
        XEvent addFieldB = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_B_ID);
        XEvent addFieldC = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_C_ID);
        
        XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC };
        
        MemoryRepository memoryRepository = new MemoryRepository(ACTOR_ID, "", REPO_ID);
        memoryRepository.createModel(MODEL_ID);
        XModel memoryModel = memoryRepository.getModel(MODEL_ID);
        memoryModel.createObject(OBJECT_ID);
        XObject object2 = memoryModel.getObject(OBJECT_ID);
        
        MemoryCommandFactory factory = new MemoryCommandFactory();
        XObjectCommand commandA = factory.createAddFieldCommand(OBJECT_ADDRESS, FIELD_A_ID, true);
        XObjectCommand commandB = factory.createAddFieldCommand(OBJECT_ADDRESS, FIELD_B_ID, true);
        XTransaction transaction = MemoryTransaction.createTransaction(OBJECT_ADDRESS,
                new XAtomicCommand[] { commandA, commandB });
        memoryModel.executeCommand(transaction);
        
        object2.createField(FIELD_C_ID);
        
        ISyncLog syncLog = memoryRepository.getModel(MODEL_ID).getRoot().getSyncLog();
        syncLog.setSynchronizedRevision(1);
        
        ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
        Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(1);
        while(iterator.hasNext()) {
            ISyncLogEntry entry = iterator.next();
            syncLogEntryList.add(entry);
        }
        
        UnorderedEventMapper mapper = new UnorderedEventMapper();
        IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
        
        assert result.getUnmappedLocalEvents().isEmpty();
        assert result.getUnmappedRemoteEvents().isEmpty();
        assert result.getMapped().size() == serverEvents.length
                && result.getMapped().size() == syncLogEntryList.size();
    }
    
    /**
     * One local change ["A"] which shall be found from server events, two
     * server events ["B", "C"] which shall not be found
     * 
     */
    @Test
    public void testSomeServerMappedAllLocalMapped() {
        
        XEvent addFieldA = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_A_ID);
        XEvent addFieldB = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_B_ID);
        XEvent addFieldC = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_C_ID);
        
        XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC };
        
        MemoryRepository memoryRepository = new MemoryRepository(ACTOR_ID, "", REPO_ID);
        memoryRepository.createModel(MODEL_ID);
        XModel memoryModel = memoryRepository.getModel(MODEL_ID);
        memoryModel.createObject(OBJECT_ID);
        XObject object2 = memoryModel.getObject(OBJECT_ID);
        object2.createField(FIELD_A_ID);
        
        ISyncLog syncLog = memoryRepository.getModel(MODEL_ID).getRoot().getSyncLog();
        syncLog.setSynchronizedRevision(1);
        
        ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
        Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(2);
        while(iterator.hasNext()) {
            ISyncLogEntry entry = iterator.next();
            syncLogEntryList.add(entry);
        }
        
        UnorderedEventMapper mapper = new UnorderedEventMapper();
        IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
        
        assertTrue(result.getUnmappedLocalEvents().isEmpty());
        assertEquals(2, result.getUnmappedRemoteEvents().size());
        assertEquals(result.getMapped().size(), syncLogEntryList.size());
    }
    
    /**
     * Three local changes ["A", "B", "C"], ["A" and "B" ] shall be found from
     * server events, one shall not be found
     * 
     */
    @Test
    public void testAllServerMappedSomeLocalNotMapped() {
        
        XEvent addFieldA = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_A_ID);
        XEvent addFieldB = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_B_ID);
        
        XEvent[] serverEvents = { addFieldA, addFieldB };
        
        MemoryRepository memoryRepository = new MemoryRepository(ACTOR_ID, "", REPO_ID);
        memoryRepository.createModel(MODEL_ID);
        XModel memoryModel = memoryRepository.getModel(MODEL_ID);
        memoryModel.createObject(OBJECT_ID);
        XObject object2 = memoryModel.getObject(OBJECT_ID);
        
        object2.createField(FIELD_A_ID);
        object2.createField(FIELD_B_ID);
        object2.createField(FIELD_C_ID);
        
        ISyncLog syncLog = memoryRepository.getModel(MODEL_ID).getRoot().getSyncLog();
        syncLog.setSynchronizedRevision(1);
        
        ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
        Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(1);
        while(iterator.hasNext()) {
            ISyncLogEntry entry = iterator.next();
            syncLogEntryList.add(entry);
        }
        
        UnorderedEventMapper mapper = new UnorderedEventMapper();
        IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
        
        assertEquals(1, result.getUnmappedLocalEvents().size());
        assertEquals(0, result.getUnmappedRemoteEvents().size());
        assertEquals(result.getMapped().size(), serverEvents.length);
    }
    
    /**
     * One local change ["A"], which shall be found; two more server events
     * which delete and re-add this entity
     * 
     */
    @Test
    public void testDuplicateEvents() {
        
        XEvent addFieldA = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_A_ID);
        XEvent removeFieldA = MemoryObjectEvent.createRemoveEvent(ACTOR_ID, OBJECT_ADDRESS,
                FIELD_A_ID, 2, 2, false, false);
        XEvent addFieldAAgain = createFieldAddedEvent(OBJECT_ADDRESS, FIELD_A_ID);
        
        XEvent[] serverEvents = { addFieldA, removeFieldA, addFieldAAgain };
        
        MemoryRepository memoryRepository = new MemoryRepository(ACTOR_ID, "", REPO_ID);
        memoryRepository.createModel(MODEL_ID);
        XModel memoryModel = memoryRepository.getModel(MODEL_ID);
        memoryModel.createObject(OBJECT_ID);
        XObject object2 = memoryModel.getObject(OBJECT_ID);
        object2.createField(FIELD_A_ID);
        
        ISyncLog syncLog = memoryRepository.getModel(MODEL_ID).getRoot().getSyncLog();
        syncLog.setSynchronizedRevision(1);
        
        ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
        Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(2);
        while(iterator.hasNext()) {
            ISyncLogEntry entry = iterator.next();
            syncLogEntryList.add(entry);
        }
        
        UnorderedEventMapper mapper = new UnorderedEventMapper();
        IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
        
        assertEquals(0, result.getUnmappedLocalEvents().size());
        assertEquals(2, result.getUnmappedRemoteEvents().size());
        assertEquals(result.getMapped().size(), syncLogEntryList.size());
    }
    
    @Test
    public void testEventMapperWithBigTestDataSet() {
        
        /* get server events */
        ArrayList<XEvent> serverEventList = new ArrayList<XEvent>();
        
        XRepository serverRepo = new MemoryRepository(ACTOR_ID, "", REPO_ID);
        DemoModelUtil.addPhonebookModel(serverRepo);
        Iterator<XEvent> serverEvents = DemoLocalChangesAndServerEvents
                .applyAndGetServerChanges(serverRepo);
        while(serverEvents.hasNext()) {
            XEvent serverEvent = (XEvent)serverEvents.next();
            serverEventList.add(serverEvent);
        }
        
        /* get local sync log */
        XRepository localRepo = new MemoryRepository(ACTOR_ID, "", REPO_ID);
        localRepo.createModel(XX.toId("trial"));
        XModel secondModel = localRepo.getModel(XX.toId("trial"));
        DemoModelUtil.addPhonebookModel(localRepo);
        XModel localModel = localRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
        XCopyUtils.copyData(localModel, secondModel);
        DemoLocalChangesAndServerEvents.addLocalChangesToModel(localModel);
        ISyncLog localChangeLog = (ISyncLog)localModel.getChangeLog();
        localChangeLog.setSynchronizedRevision(46);
        
        /* add both to event mapper */
        UnorderedEventMapper mapper = new UnorderedEventMapper();
        IMappingResult result = mapper.mapEvents(localChangeLog,
                serverEventList.toArray(new XEvent[serverEventList.size()]));
        
        /* verify result */
        ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
        Iterator<ISyncLogEntry> iterator = localChangeLog.getSyncLogEntriesSince(1);
        while(iterator.hasNext()) {
            ISyncLogEntry entry = iterator.next();
            syncLogEntryList.add(entry);
        }
        
        int numberUnmappedRemoteEvents = result.getUnmappedRemoteEvents().size();
        int numberMappedEvents = result.getMapped().size();
        int numberUnmappedLocalEvents = result.getUnmappedLocalEvents().size();
        assertEquals("wrong number of mapped events: was " + numberMappedEvents
                + ", but should have been 11", 11, numberMappedEvents);
        assertEquals("wrong number of unmapped remote events: was " + numberUnmappedRemoteEvents
                + ", but should have been 10", 10, numberUnmappedRemoteEvents);
        assertEquals("wrong number of unmapped local events: was " + numberUnmappedLocalEvents
                + ", but should have been 5", 5, numberUnmappedLocalEvents);
    }
}
