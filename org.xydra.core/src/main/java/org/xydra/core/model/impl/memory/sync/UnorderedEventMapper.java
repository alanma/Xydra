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
 * @author kahmann
 *
 */
public class UnorderedEventMapper implements IEventMapper {

    /**
     * Contains the results of the mapping.
     *
     * @author kahmann
     *
     */
    private class MappingResult implements IMappingResult {

        private final List<XEvent> nonMappedLocalEvents;
        private final List<XEvent> nonMappedRemoteEvents;
        private final List<Pair<XEvent,XEvent>> mapped;

        public MappingResult(final List<Pair<XEvent,XEvent>> mapped, final List<XEvent> nonMappedServerEvents,
                final List<XEvent> nonMappedLocalEvents) {
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
    public IMappingResult mapEvents(final ISyncLog localSyncLog, final XEvent[] remoteEvents) {

        final List<XEvent> nonMappedLocalEvents = new ArrayList<XEvent>();

        final List<XEvent> nonMappedServerEvents = new ArrayList<XEvent>();

        final List<Pair<XEvent,XEvent>> mapped = new ArrayList<Pair<XEvent,XEvent>>();

        final Iterator<ISyncLogEntry> localEventsSinceLastSynchronization = localSyncLog
                .getSyncLogEntriesSince(

                Math.max(0, localSyncLog.getSynchronizedRevision() + 1)

                );

        final List<XAtomicEvent> atomicLocalEventsList = unpackTransactionsToList(localEventsSinceLastSynchronization);
        final List<XEvent> atomicRemoteEvents = unpackTransactions(remoteEvents);

        /* for each server event: look for corresponding local event */
        final ArrayList<XEvent> foundLocalEvents = new ArrayList<XEvent>();
        for(final XEvent remoteEvent : atomicRemoteEvents) {
            assert !(remoteEvent instanceof XTransactionEvent);
            boolean eventCouldBeMapped = false;
            final Iterator<XAtomicEvent> atomicLocalEventsIt = atomicLocalEventsList.iterator();
            while(atomicLocalEventsIt.hasNext()) {
                final XAtomicEvent atomicLocalEvent = atomicLocalEventsIt.next();
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
        for(final XEvent atomicLocalEvent : atomicLocalEventsList) {
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
    private static List<XEvent> unpackTransactions(final XEvent[] remoteEvents) {

        final Iterator<XEvent> rawEvents = Arrays.asList(remoteEvents).iterator();

        final List<XEvent> unpackedEvents = new ArrayList<XEvent>();
        while(rawEvents.hasNext()) {
            final XEvent currentEvent = rawEvents.next();
            if(currentEvent instanceof XTransactionEvent) {
                final XTransactionEvent currentTransactionEvent = (XTransactionEvent)currentEvent;
                final Iterator<XAtomicEvent> eventsInTransaction = currentTransactionEvent.iterator();
                while(eventsInTransaction.hasNext()) {
                    final XAtomicEvent currentEventInTransaction = eventsInTransaction.next();
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
            final Iterator<ISyncLogEntry> syncLogEntries) {
        final List<XAtomicEvent> unpackedEventsList = new ArrayList<XAtomicEvent>(8);
        while(syncLogEntries.hasNext()) {
            final ISyncLogEntry syncLogEntry = syncLogEntries.next();
            final XEvent event = syncLogEntry.getEvent();
            if(event instanceof XTransactionEvent) {
                final XTransactionEvent transactionEvent = (XTransactionEvent)event;
                final Iterator<XAtomicEvent> eventsInTransaction = transactionEvent.iterator();
                while(eventsInTransaction.hasNext()) {
                    final XAtomicEvent atomicEvent = eventsInTransaction.next();
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
    private static boolean isEqual(final XEvent remoteEvent, final XEvent localEvent) {

        if(remoteEvent.getChangeType().equals(localEvent.getChangeType())
                && remoteEvent.getChangedEntity().equals(localEvent.getChangedEntity())
                && remoteEvent.getTarget().equals(localEvent.getTarget())) {
            if(remoteEvent instanceof MemoryFieldEvent) {
                final MemoryFieldEvent remoteFieldEvent = (MemoryFieldEvent)remoteEvent;
                final MemoryFieldEvent localFieldEvent = (MemoryFieldEvent)localEvent;

                final XValue newValueRemote = remoteFieldEvent.getNewValue();
                final XValue newValueLocal = localFieldEvent.getNewValue();

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
