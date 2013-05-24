package org.xydra.core.model.impl.memory.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.value.XValue;
import org.xydra.index.query.Pair;


/**
 * Examines the events from the server for changes this client has caused.
 * <p>
 * <b>Looks for events without regarding the safe/forced-attributes of the
 * underlying commands</b>; unpacks transactions to only compare atomic events.
 * 
 * @author Andi_Ka
 * 
 */
public class UnorderedEventMapper implements IEventMapper {
	
	/**
	 * Contains the results of the mapping.
	 * 
	 * @author Andi_Ka
	 * 
	 */
	private class MappingResult implements IMappingResult {
		
		private List<ISyncLogEntry> nonMappedLocalEvents;
		private List<XEvent> nonMappedRemoteEvents;
		private List<Pair<XEvent,ISyncLogEntry>> mapped;
		
		public MappingResult(List<Pair<XEvent,ISyncLogEntry>> mapped,
		        List<XEvent> nonMappedServerEvents, List<ISyncLogEntry> nonMappedLocalEvents) {
			this.mapped = mapped;
			this.nonMappedRemoteEvents = nonMappedServerEvents;
			this.nonMappedLocalEvents = nonMappedLocalEvents;
		}
		
		@Override
		public List<XEvent> getUnmappedRemoteEvents() {
			return this.nonMappedRemoteEvents;
		}
		
		@Override
		public List<ISyncLogEntry> getUnmappedLocalEvents() {
			return this.nonMappedLocalEvents;
		}
		
		@Override
		public List<Pair<XEvent,ISyncLogEntry>> getMapped() {
			return this.mapped;
		}
		
	}
	
	@Override
	public IMappingResult mapEvents(ISyncLog localSyncLog, XEvent[] remoteEvents) {
		
		List<ISyncLogEntry> nonMappedLocalEvents = new ArrayList<ISyncLogEntry>();
		List<XEvent> nonMappedServerEvents = new ArrayList<XEvent>();
		List<Pair<XEvent,ISyncLogEntry>> mapped = new ArrayList<Pair<XEvent,ISyncLogEntry>>();
		
		Iterator<ISyncLogEntry> localEventsSinceLastSynchronization = localSyncLog
		        .getSyncLogEntriesSince(localSyncLog.getSynchronizedRevision());
		
		Map<XEvent,ISyncLogEntry> atomicSyncLogEventsMap = unpackTransactionsToMap(localEventsSinceLastSynchronization);
		List<XEvent> atomicRemoteEvents = unpackTransactions(remoteEvents);
		
		/* for each server event: look for correspondent local event */
		ArrayList<XEvent> foundLocalEvents = new ArrayList<XEvent>();
		for(XEvent remoteEvent : atomicRemoteEvents) {
			boolean eventCouldBeMapped = false;
			Iterator<Entry<XEvent,ISyncLogEntry>> atomicSyncLogEntries = atomicSyncLogEventsMap
			        .entrySet().iterator();
			while(atomicSyncLogEntries.hasNext()) {
				Map.Entry<XEvent,ISyncLogEntry> atomicSyncLogEntry = (Map.Entry<XEvent,ISyncLogEntry>)atomicSyncLogEntries
				        .next();
				XEvent localEvent = atomicSyncLogEntry.getKey();
				if(isEqual(remoteEvent, localEvent)) {
					if(!foundLocalEvents.contains(localEvent)) {
						mapped.add(new Pair<XEvent,ISyncLogEntry>(remoteEvent, atomicSyncLogEntry
						        .getValue()));
						eventCouldBeMapped = true;
						foundLocalEvents.add(localEvent);
					}
					break;
				}
			}
			
			if(!eventCouldBeMapped) {
				nonMappedServerEvents.add(remoteEvent);
			}
		}
		
		/* look for not found local Events */
		Set<Entry<XEvent,ISyncLogEntry>> entries = atomicSyncLogEventsMap.entrySet();
		for(Entry<XEvent,ISyncLogEntry> entry : entries) {
			XEvent atomicEvent = entry.getKey();
			if(!foundLocalEvents.contains(atomicEvent)) {
				nonMappedLocalEvents.add(entry.getValue());
			}
		}
		
		return new MappingResult(mapped, nonMappedServerEvents, nonMappedLocalEvents);
	}
	
	/**
	 * adds events to list and unpacks all transactions so this list only
	 * contains atomic events
	 * 
	 * @param remoteEvents events that could contain TransactionEvents
	 */
	private static List<XEvent> unpackTransactions(XEvent[] remoteEvents) {
		
		Iterator<XEvent> rawEvents = Arrays.asList(remoteEvents).iterator();
		
		List<XEvent> unpackedEvents = new ArrayList<XEvent>();
		while(rawEvents.hasNext()) {
			XEvent currentEvent = rawEvents.next();
			if(currentEvent instanceof XTransactionEvent) {
				XTransactionEvent currentTransactionEvent = (XTransactionEvent)currentEvent;
				Iterator<XAtomicEvent> eventsInTransaction = currentTransactionEvent.iterator();
				while(eventsInTransaction.hasNext()) {
					XAtomicEvent currentEventInTransaction = eventsInTransaction.next();
					unpackedEvents.add(currentEventInTransaction);
				}
				
			} else {
				unpackedEvents.add(currentEvent);
			}
		}
		return unpackedEvents;
	}
	
	/**
	 * adds events to a map XEvent -> ISyncLogEntry and unpacks all transactions
	 * so this list only contains atomic events
	 * 
	 * @param entries events that could contain TransactionEvents
	 */
	private static Map<XEvent,ISyncLogEntry> unpackTransactionsToMap(Iterator<ISyncLogEntry> entries) {
		Map<XEvent,ISyncLogEntry> unpackedEventsMap = new HashMap<XEvent,ISyncLogEntry>();
		while(entries.hasNext()) {
			ISyncLogEntry currentEntry = entries.next();
			XEvent currentEntriesEvent = currentEntry.getEvent();
			if(currentEntriesEvent instanceof XTransactionEvent) {
				XTransactionEvent currentTransactionEvent = (XTransactionEvent)currentEntriesEvent;
				Iterator<XAtomicEvent> eventsInTransaction = currentTransactionEvent.iterator();
				while(eventsInTransaction.hasNext()) {
					XAtomicEvent currentEventInTransaction = eventsInTransaction.next();
					unpackedEventsMap.put(currentEventInTransaction, currentEntry);
				}
				
			} else {
				unpackedEventsMap.put(currentEntriesEvent, currentEntry);
			}
		}
		return unpackedEventsMap;
	}
	
	/**
	 * Critical method.
	 * <p>
	 * this implementation only checks whether the concerned entity and the
	 * change type are the same and as the case may be whether the value of the
	 * field event is the same
	 * 
	 * @param remoteEvent
	 * @param localEvent
	 * @return
	 */
	@SuppressWarnings("null")
	private static boolean isEqual(XEvent remoteEvent, XEvent localEvent) {
		
		if(remoteEvent.getChangeType().equals(localEvent.getChangeType())
		        && remoteEvent.getChangedEntity().equals(localEvent.getChangedEntity())
		        && remoteEvent.getTarget().equals(localEvent.getTarget())) {
			if(remoteEvent instanceof MemoryFieldEvent) {
				MemoryFieldEvent remoteFieldEvent = (MemoryFieldEvent)remoteEvent;
				MemoryFieldEvent localFieldEvent = (MemoryFieldEvent)localEvent;
				
				XValue newValueRemote = remoteFieldEvent.getNewValue();
				XValue newValueLocal = localFieldEvent.getNewValue();
				if(newValueRemote == null && newValueLocal == null
				        || newValueRemote.equals(newValueLocal)) {
					return true;
				} else
					return false;
				
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
}
