package org.xydra.core.model.impl.memory.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        
        private List<XEvent> nonMappedLocalEvents;
        private List<XEvent> nonMappedRemoteEvents;
        private List<Pair<XEvent,XEvent>> mapped;
        
        public MappingResult(List<Pair<XEvent,XEvent>> mapped, List<XEvent> nonMappedServerEvents,
                List<XEvent> nonMappedLocalEvents) {
            this.mapped = mapped;
            this.nonMappedRemoteEvents = nonMappedServerEvents;
            this.nonMappedLocalEvents = nonMappedLocalEvents;
        }
        
        @Override
        public List<XEvent> getUnmappedRemoteEvents() {
            return this.nonMappedRemoteEvents;
        }
        
        @Override
        public List<XEvent> getUnmappedLocalEvents() {
            return this.nonMappedLocalEvents;
        }
        
        @Override
        public List<Pair<XEvent,XEvent>> getMapped() {
            return this.mapped;
        }
        
    }
    
    @Override
    public IMappingResult mapEvents(ISyncLog localSyncLog, XEvent[] remoteEvents) {
        
        List<XEvent> nonMappedLocalEvents = new ArrayList<XEvent>();
        
        List<XEvent> nonMappedServerEvents = new ArrayList<XEvent>();
        
        List<Pair<XEvent,XEvent>> mapped = new ArrayList<Pair<XEvent,XEvent>>();
        
        Iterator<ISyncLogEntry> localEventsSinceLastSynchronization = localSyncLog
                .getSyncLogEntriesSince(
                
                Math.max(0, localSyncLog.getSynchronizedRevision() + 1)
                
                );
        
        List<XAtomicEvent> atomicLocalEventsList = unpackTransactionsToList(localEventsSinceLastSynchronization);
        List<XEvent> atomicRemoteEvents = unpackTransactions(remoteEvents);
        
        /* for each server event: look for corresponding local event */
        ArrayList<XEvent> foundLocalEvents = new ArrayList<XEvent>();
        for(XEvent remoteEvent : atomicRemoteEvents) {
            assert !(remoteEvent instanceof XTransactionEvent);
            boolean eventCouldBeMapped = false;
            Iterator<XAtomicEvent> atomicLocalEventsIt = atomicLocalEventsList.iterator();
            while(atomicLocalEventsIt.hasNext()) {
                XAtomicEvent atomicLocalEvent = atomicLocalEventsIt.next();
                if(isEqual(remoteEvent, atomicLocalEvent)) {
                    if(!foundLocalEvents.contains(atomicLocalEvent)) {
                        mapped.add(new Pair<XEvent,XEvent>(remoteEvent, atomicLocalEvent));
                        eventCouldBeMapped = true;
                        foundLocalEvents.add(atomicLocalEvent);
                    }
                    break;
                }
            }
            
            if(!eventCouldBeMapped) {
                nonMappedServerEvents.add(remoteEvent);
            }
        }
        
        /* look for not found local Events */
        for(XEvent atomicLocalEvent : atomicLocalEventsList) {
            if(!foundLocalEvents.contains(atomicLocalEvent)) {
                nonMappedLocalEvents.add(atomicLocalEvent);
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
     * @param syncLogEntries events that could contain TransactionEvents
     */
    private static List<XAtomicEvent> unpackTransactionsToList(
            Iterator<ISyncLogEntry> syncLogEntries) {
        List<XAtomicEvent> unpackedEventsList = new ArrayList<XAtomicEvent>(8);
        while(syncLogEntries.hasNext()) {
            ISyncLogEntry syncLogEntry = syncLogEntries.next();
            XEvent event = syncLogEntry.getEvent();
            if(event instanceof XTransactionEvent) {
                XTransactionEvent transactionEvent = (XTransactionEvent)event;
                Iterator<XAtomicEvent> eventsInTransaction = transactionEvent.iterator();
                while(eventsInTransaction.hasNext()) {
                    XAtomicEvent atomicEvent = eventsInTransaction.next();
                    unpackedEventsList.add(atomicEvent);
                }
                
            } else {
                unpackedEventsList.add((XAtomicEvent)event);
            }
        }
        return unpackedEventsList;
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
    private static boolean isEqual(XEvent remoteEvent, XEvent localEvent) {
        
        if(remoteEvent.getChangeType().equals(localEvent.getChangeType())
                && remoteEvent.getChangedEntity().equals(localEvent.getChangedEntity())
                && remoteEvent.getTarget().equals(localEvent.getTarget())) {
            if(remoteEvent instanceof MemoryFieldEvent) {
                MemoryFieldEvent remoteFieldEvent = (MemoryFieldEvent)remoteEvent;
                MemoryFieldEvent localFieldEvent = (MemoryFieldEvent)localEvent;
                
                XValue newValueRemote = remoteFieldEvent.getNewValue();
                XValue newValueLocal = localFieldEvent.getNewValue();
                
                if(newValueRemote == null) {
                	return newValueLocal == null;
                } else {
                	return newValueRemote.equals(newValueLocal);
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
