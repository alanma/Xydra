package org.xydra.core.model.impl.memory.sync;

import org.xydra.core.model.XChangeLogState;


/**
 * Internal sync log state for serialisation/deserialisation
 * 
 * @author xamde
 * @author andreask
 */
public interface XSyncLogState extends XChangeLogState, IReadableSyncLog {
    
    /**
     * @param syncLogEntry The {@link ISyncLogEntry} which is to be logged
     */
    void appendSyncLogEntry(ISyncLogEntry syncLogEntry);
    
    /**
     * Returns the {@link ISyncLogEntry} this sync log logged at the given
     * revision number
     * 
     * @param revisionNumber for which to return an {@link ISyncLogEntry}
     * @return the {@link ISyncLogEntry} that was logged at the given revision
     *         number or null if the {@link ISyncLogEntry} cannot be accessed.
     * @throws IndexOutOfBoundsException if the given revision number is less
     *             than the first revision number or greater than or equal to
     *             the current revision number of this change log
     */
    ISyncLogEntry getSyncLogEntry(long revisionNumber);
    
    /**
     * @return the revision number up to which the log has been synchronised
     *         successfully with the server
     */
    long getSyncRevisionNumber();
    
    /**
     * Set the sync revision number.
     * 
     * @param rev
     */
    void setSyncRevisionNumber(long rev);
    
    void removeSyncLogEntryAt(Long l);
    
    /**
     * Internal use
     * 
     * @return number of sync log entries managed
     */
    long getSize();
    
}
