package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.model.impl.memory.garbage.LocalChange;
import org.xydra.core.model.impl.memory.garbage.XLocalChanges;
import org.xydra.index.query.Pair;


public class EventSequenceMapper {
    
    /**
     * Find the first, longest sub-sequence of events from localChanges in the
     * sequence serverEvents. As many as possible events from localChanges
     * should appear. Additional events can appear between any two events from
     * localChanges. E.g. if the serverEvents is ABCDEFGHIJKLM and localChanges
     * is CEGXL the longest sequence is CEGL.
     * 
     * Transactions are compared as-is, i.e. they are different from the list of
     * their atomic events. This holds from both sides. I.e. a transaction on
     * the server-side can only ever be mapped to an equivalent transaction on
     * the client side.
     * 
     * @param serverEvents
     * @param localChanges
     * @return the {@link Result}
     */
    public static Result map(XEvent[] serverEvents, XLocalChanges localChanges) {
        List<XEvent> unpackedServerEvents = unpackImpliedTxEvents(serverEvents);
        return mapServerEventsToLocalChanges(unpackedServerEvents, localChanges);
    }
    
    private static Result mapServerEventsToLocalChanges(List<XEvent> unpackedServerEvents,
            XLocalChanges localChanges) {
        
        List<LocalChange> nonMappedLocalEvents = new ArrayList<LocalChange>();
        List<XEvent> nonMappedServerEvents = new ArrayList<XEvent>();
        List<Pair<XEvent,LocalChange>> mapped = new ArrayList<Pair<XEvent,LocalChange>>();
        
        int[][] matches = calcMatches(unpackedServerEvents, localChanges);
        
        int numServerEvents = unpackedServerEvents.size();
        int numLocalChanges = localChanges.getList().size();
        
        for(int x = numServerEvents, y = numLocalChanges; x > 0 || y > 0;) {
            if(x > 0 && matches[x][y] == matches[x - 1][y]) {
                XEvent serverEvent = unpackedServerEvents.get(x - 1);
                nonMappedServerEvents.add(serverEvent);
                x--;
            } else if(y > 0 && matches[x][y] == matches[x][y - 1]) {
                nonMappedLocalEvents.add(localChanges.getList().get(y - 1));
                y--;
            } else {
                if(x > 0 && y > 0) {
                    XEvent serverEvent = unpackedServerEvents.get(x - 1);
                    XEvent localEvent = localChanges.getList().get(y - 1).getEvent();
                    assert isEqual(serverEvent, localEvent);
                    mapped.add(new Pair<XEvent,LocalChange>(serverEvent, localChanges.getList()
                            .get(y - 1)));
                    x--;
                    y--;
                }
            }
        }
        
        Collections.reverse(nonMappedLocalEvents);
        Collections.reverse(nonMappedServerEvents);
        Collections.reverse(mapped);
        
        assert numServerEvents == (nonMappedServerEvents.size() + mapped.size());
        assert numLocalChanges == (nonMappedLocalEvents.size() + mapped.size());
        
        return new Result(nonMappedServerEvents, nonMappedLocalEvents, mapped);
    }
    
    private static int[][] calcMatches(List<XEvent> unpackedServerEvents, XLocalChanges localChanges) {
        
        int numServerEvents = unpackedServerEvents.size();
        int numLocalChanges = localChanges.getList().size();
        int[][] matches = new int[numServerEvents + 1][numLocalChanges + 1];
        for(int x = 0; x < numServerEvents; x++) {
            XEvent serverEvent = unpackedServerEvents.get(x);
            for(int y = 0; y < numLocalChanges; y++) {
                XEvent localEvent = localChanges.getList().get(y).getEvent();
                if(isEqual(serverEvent, localEvent)) {
                    matches[x + 1][y + 1] = matches[x][y] + 1;
                } else {
                    matches[x + 1][y + 1] = Math.max(matches[x + 1][y], matches[x][y + 1]);
                }
            }
        }
        return matches;
    }
    
    /**
     * <p>
     * Compares a local command-event Pair with a remote server event.
     * 
     * They are considered equal if:
     * <p>
     * 1) localCommand is an atomicCommand, resulting in an atomicEvent and on
     * the server is also an atomicEvent.
     * 
     * Target address and changedEntityAddress must match. ChangeType must
     * match.
     * 
     * Additionally, the command mode of the local command is checked:
     * 
     * mode == forced or mode == state-bound: isEqual=true;
     * 
     * mode == revision-bound: check if oldXXXrevision matches the command
     * condition.
     * </p>
     * <p>
     * 2) localCommand is an atomicCommand, resulting in an transactionEvent and
     * on the server is an atomicEvent or transactionEvent.
     * 
     * If mode == forced OR mode == state-bound, match is true even if server
     * event contains additional or less atomicEvents.
     * 
     * If mode == revision-bound is true if revision-condition matches.
     * 
     * </p>
     * <p>
     * 3) localCommand is transaction, resulting in an transactionEvent and on
     * the server is an atomicEvent or transactionEvent.
     * 
     * Match if all localAtomicCommands within the localTransaction match to one
     * or several events (1:n can happen for REMOVE commands only) from the
     * serverTransactionEvent or map to the single serverAtomicEvent if there
     * was just one.
     * </p>
     * <p>
     * 
     * 4) In other cases: No match.
     * </p>
     * 
     * <h3>Mapping commands to events:</h3>
     * 
     * <ul>
     * <li>ADD-command => ADD-event</li>
     * <li>REMOVE-command => REMOVE-event + ( implied REMOVE-events of children)
     * </li>
     * <li>CHANGE-command => CHANGE-event</li>
     * </ul>
     * 
     * @param serverEvent
     * 
     * @param localEvent
     * 
     * @return
     */
    private static boolean isEqual(XEvent serverEvent, XEvent localEvent) {
        if(serverEvent.getChangeType().equals(localEvent.getChangeType())
                && serverEvent.getChangedEntity().equals(localEvent.getChangedEntity())
                && serverEvent.getTarget().equals(localEvent.getTarget())) {
            return true;
        } else {
            // TODO Transactions
            return false;
        }
        
    }
    
    private static List<XEvent> unpackImpliedTxEvents(XEvent[] serverEvents) {
        List<XEvent> unpackedServerEvents = new ArrayList<XEvent>();
        for(XEvent event : serverEvents) {
            assert event != null;
            if(event instanceof XAtomicEvent) {
                unpackedServerEvents.add(event);
            } else if(event instanceof XTransactionEvent) {
                XTransactionEvent txEvent = (XTransactionEvent)event;
                unpackedServerEvents.addAll(unpackTxEvents(txEvent));
            }
        }
        
        return unpackedServerEvents;
    }
    
    private static List<XEvent> unpackTxEvents(XTransactionEvent txEvent) {
        List<XEvent> unpackedTxEvents = new ArrayList<XEvent>();
        if(doesOnlyContainImpliedEvents(txEvent)) {
            List<XEvent> containedEvents = getContainedEventsFromTx(txEvent);
            unpackedTxEvents.addAll(containedEvents);
        } else {
            unpackedTxEvents.add(txEvent);
        }
        return unpackedTxEvents;
    }
    
    private static boolean doesOnlyContainImpliedEvents(XTransactionEvent txEvent) {
        for(int i = 0; i < txEvent.size(); i++) {
            if(!txEvent.getEvent(i).isImplied())
                return false;
        }
        return true;
    }
    
    private static List<XEvent> getContainedEventsFromTx(XTransactionEvent txEvent) {
        List<XEvent> containedEvents = new ArrayList<XEvent>();
        for(int i = 0; i < txEvent.size(); i++) {
            containedEvents.add(txEvent.getEvent(i));
        }
        return containedEvents;
    }
    
    public static class Result {
        
        public Result(List<XEvent> nonMappedServerEvents, List<LocalChange> nonMappedLocalEvents,
                List<Pair<XEvent,LocalChange>> mapped) {
            this.nonMappedServerEvents = nonMappedServerEvents;
            this.nonMappedLocalEvents = nonMappedLocalEvents;
            this.mapped = mapped;
        }
        
        /**
         * List of true remote events = not seen yet on client
         */
        public List<XEvent> nonMappedServerEvents;
        
        /**
         * List of local events that were not mapped = not executed on server
         */
        public List<LocalChange> nonMappedLocalEvents;
        
        /**
         * Mapping between local changes and remove events = events originated
         * locally and successfully executed on server
         */
        public List<Pair<XEvent,LocalChange>> mapped;
        
    }
    
}
