package org.xydra.core.model.impl.memory;

import java.util.List;

import org.xydra.base.change.XEvent;
import org.xydra.index.query.Pair;


public class EventSequenceMapper {
    
    /**
     * Find the first, longest sub-sequence of events from localChanges in the
     * sequence serverEvents. As many as possible events from localChanges
     * should appear. Additional events can appear between any two events from
     * localChanes. E.g. if the serverEvents is ABCDEFGHIJKLM and localChanges
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
    public static Result map(XEvent[] serverEvents, LocalChanges localChanges) {
        return null;
    }
    
    public static class Result {
        
        /**
         * List of true remote events = not seen yet on client
         */
        List<XEvent> nonMappedServerEvents;
        
        /**
         * List of local events that were not mapped = not executed on server
         */
        List<LocalChange> nonMappedLocalEvents;
        
        /**
         * Mapping between local changes and remove events = events originated
         * locally and successfully executed on server
         */
        List<Pair<XEvent,LocalChange>> mapped;
        
    }
    
}
