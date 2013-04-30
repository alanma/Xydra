package org.xydra.core.model.impl.memory.sync;

import java.util.List;

import org.xydra.base.change.XEvent;
import org.xydra.index.query.Pair;


/**
 * @author xamde
 */
public interface IEventMapper {
    
    /**
     * @param localSyncLog
     * @param remoteEvents
     * @return ...
     */
    IMappingResult mapEvents(ISyncLog localSyncLog, XEvent[] remoteEvents);
    
    public static interface IMappingResult {
        
        /**
         * @return List of true remote events = not seen yet on client
         */
        List<XEvent> getUnmappedRemoteEvents();
        
        /**
         * @return List of local events that were not mapped = not executed on
         *         server
         */
        List<ISyncLogEntry> getUnmappedLocalEvents();
        
        /**
         * @return Mapping between local changes and remove events = events
         *         originated locally and successfully executed on server
         */
        List<Pair<XEvent,ISyncLogEntry>> getMapped();
        
    }
    
}
