package org.xydra.core.model.impl.memory;

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
import org.xydra.base.id.MemoryStringIDProvider;
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


public class UnorderedEventMapperTest {
	XId repo = XX.toId("repo");
	XId model = XX.toId("model");
	XId object = XX.toId("object");
	private static XId actor = XX.toId("actor");
	String fieldA = "A";
	String fieldB = "B";
	String fieldC = "C";
	
	private static XEvent createFieldAddedEvent(XAddress objectAddress, String fieldId) {
		return MemoryObjectEvent.createAddEvent(actor, objectAddress, XX.toId(fieldId), 1, false);
	}
	
	private static XCommand createFieldAddedCommand(XAddress objectAddress, String field) {
		return MemoryObjectCommand.createAddCommand(objectAddress, true, XX.toId(field));
	}
	
	/**
	 * Three local changes which shall all be found from server events; event1
	 * and event 2 are from one transaction
	 */
	@Test
	public void testAllServerMappedAllLocalMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, this.fieldA);
		XEvent addFieldB = createFieldAddedEvent(objectAddress, this.fieldB);
		XEvent addFieldC = createFieldAddedEvent(objectAddress, this.fieldC);
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC };
		
		MemoryRepository memoryRepository = new MemoryRepository(actor, "", this.repo);
		memoryRepository.createModel(this.model);
		XModel memoryModel = memoryRepository.getModel(this.model);
		memoryModel.createObject(this.object);
		XObject object2 = memoryModel.getObject(this.object);
		object2.createField(XX.toId(this.fieldA));
		object2.createField(XX.toId(this.fieldB));
		object2.createField(XX.toId(this.fieldC));
		
		ISyncLog syncLog = memoryRepository.getModel(this.model).getRoot().getSyncLog();
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
		
		// FIXME whiteboxtest double event functionality
		
	}
	
	/**
	 * Three local changes which shall all be found from server events; event1
	 * and event 2 are from one transaction
	 * 
	 * FIXME currently not working
	 */
	// @Test
	public void testAllServerMappedAllLocalMappedWithTransaction() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, this.fieldA);
		XEvent addFieldB = createFieldAddedEvent(objectAddress, this.fieldB);
		XEvent addFieldC = createFieldAddedEvent(objectAddress, this.fieldC);
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC };
		
		MemoryRepository memoryRepository = new MemoryRepository(actor, "", this.repo);
		memoryRepository.createModel(this.model);
		XModel memoryModel = memoryRepository.getModel(this.model);
		memoryModel.createObject(this.object);
		XObject object2 = memoryModel.getObject(this.object);
		
		MemoryCommandFactory factory = new MemoryCommandFactory();
		XObjectCommand commandA = factory.createAddFieldCommand(objectAddress,
		        XX.toId(this.fieldA), true);
		XObjectCommand commandB = factory.createAddFieldCommand(objectAddress,
		        XX.toId(this.fieldB), true);
		XTransaction transaction = MemoryTransaction.createTransaction(objectAddress,
		        new XAtomicCommand[] { commandA, commandB });
		memoryModel.executeCommand(transaction);
		
		object2.createField(XX.toId(this.fieldC));
		
		ISyncLog syncLog = memoryRepository.getModel(this.model).getRoot().getSyncLog();
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
		
		// FIXME whiteboxtest double event functionality
		
	}
	
	/**
	 * One local change ["A"] which shall be found from server events, two
	 * server events ["B", "C"] which shall not be found
	 * 
	 */
	@Test
	public void testSomeServerMappedAllLocalMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, this.fieldA);
		XEvent addFieldB = createFieldAddedEvent(objectAddress, this.fieldB);
		XEvent addFieldC = createFieldAddedEvent(objectAddress, this.fieldC);
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC };
		
		MemoryRepository memoryRepository = new MemoryRepository(actor, "", this.repo);
		memoryRepository.createModel(this.model);
		XModel memoryModel = memoryRepository.getModel(this.model);
		memoryModel.createObject(this.object);
		XObject object2 = memoryModel.getObject(this.object);
		object2.createField(XX.toId(this.fieldA));
		
		ISyncLog syncLog = memoryRepository.getModel(this.model).getRoot().getSyncLog();
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
		assert result.getUnmappedRemoteEvents().size() == 2;
		assert result.getMapped().size() == syncLogEntryList.size();
		
	}
	
	/**
	 * Three local changes ["A", "B", "C"], ["A" and "B" ] shall be found from
	 * server events, one shall not be found
	 * 
	 */
	@Test
	public void testAllServerMappedSomeLocalNotMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, this.fieldA);
		XEvent addFieldB = createFieldAddedEvent(objectAddress, this.fieldB);
		
		XEvent[] serverEvents = { addFieldA, addFieldB };
		
		MemoryRepository memoryRepository = new MemoryRepository(actor, "", this.repo);
		memoryRepository.createModel(this.model);
		XModel memoryModel = memoryRepository.getModel(this.model);
		memoryModel.createObject(this.object);
		XObject object2 = memoryModel.getObject(this.object);
		object2.createField(XX.toId(this.fieldA));
		object2.createField(XX.toId(this.fieldB));
		object2.createField(XX.toId(this.fieldC));
		
		ISyncLog syncLog = memoryRepository.getModel(this.model).getRoot().getSyncLog();
		syncLog.setSynchronizedRevision(1);
		
		ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
		Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(1);
		while(iterator.hasNext()) {
			ISyncLogEntry entry = iterator.next();
			syncLogEntryList.add(entry);
		}
		
		UnorderedEventMapper mapper = new UnorderedEventMapper();
		IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
		
		assert result.getUnmappedLocalEvents().size() == 1;
		assert result.getUnmappedRemoteEvents().size() == 0;
		assert result.getMapped().size() == serverEvents.length;
		
	}
	
	/**
	 * One local change ["A"], which shall be found; two more server events
	 * which delete and re-add this entity
	 * 
	 */
	@Test
	public void testDouplicateEvents() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, this.fieldA);
		XEvent removeFieldA = MemoryObjectEvent.createRemoveEvent(actor, objectAddress,
		        XX.toId(this.fieldA), 2, 2, false, false);
		XEvent addFieldAAgain = createFieldAddedEvent(objectAddress, this.fieldA);
		
		XEvent[] serverEvents = { addFieldA, removeFieldA, addFieldAAgain };
		
		MemoryRepository memoryRepository = new MemoryRepository(actor, "", this.repo);
		memoryRepository.createModel(this.model);
		XModel memoryModel = memoryRepository.getModel(this.model);
		memoryModel.createObject(this.object);
		XObject object2 = memoryModel.getObject(this.object);
		object2.createField(XX.toId(this.fieldA));
		
		ISyncLog syncLog = memoryRepository.getModel(this.model).getRoot().getSyncLog();
		syncLog.setSynchronizedRevision(1);
		
		ArrayList<ISyncLogEntry> syncLogEntryList = new ArrayList<ISyncLogEntry>();
		Iterator<ISyncLogEntry> iterator = syncLog.getSyncLogEntriesSince(1);
		while(iterator.hasNext()) {
			ISyncLogEntry entry = iterator.next();
			syncLogEntryList.add(entry);
		}
		
		UnorderedEventMapper mapper = new UnorderedEventMapper();
		IMappingResult result = mapper.mapEvents(syncLog, serverEvents);
		
		assert result.getUnmappedLocalEvents().size() == 0;
		assert result.getUnmappedRemoteEvents().size() == 2;
		assert result.getMapped().size() == syncLogEntryList.size();
		
	}
	
	@Test
	public void testEventMapperWithBigTestDataSet() {
		
		/* get server events */
		ArrayList<XEvent> serverEventList = new ArrayList<XEvent>();
		
		XRepository serverRepo = new MemoryRepository(actor, "", this.repo);
		DemoModelUtil.addPhonebookModel(serverRepo);
		Iterator<XEvent> serverEvents = DemoLocalChangesAndServerEvents
		        .getServerChanges(serverRepo);
		while(serverEvents.hasNext()) {
			XEvent serverEvent = (XEvent)serverEvents.next();
			serverEventList.add(serverEvent);
		}
		
		/* get local sync log */
		XRepository localRepo = new MemoryRepository(actor, "", this.repo);
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
		assert numberMappedEvents == 11 : "wrong number of mapped events: was "
		        + numberMappedEvents + ", but should have been 11";
		assert numberUnmappedRemoteEvents == 10 : "wrong number of unmapped remote events: was "
		        + numberUnmappedRemoteEvents + ", but should have been 10";
		assert numberUnmappedLocalEvents == 5 : "wrong number of unmapped local events: was "
		        + numberUnmappedLocalEvents + ", but should have been 5";
	}
}
