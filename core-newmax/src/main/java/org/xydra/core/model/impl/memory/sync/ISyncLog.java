package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


/**
 * A log that is used to retrieve information about the properties of the change
 * of entities
 * 
 * @author Andi K.
 * @author xamde
 */
public interface ISyncLog extends XWritableChangeLog {
    
    // from XWritableChangeLog
    void appendEvent(XEvent event);
    
    /**
     * Appends an {@link XEvent} to the end of this MemoryChangeLog
     * 
     * @param syncLogEntry the {@link XEvent} which is to be appended @CanBeNull
     *            during play-back when synchronising
     */
    void appendSyncLogEntry(ISyncLogEntry syncLogEntry);
    
    /**
     * @param command
     * @param event
     */
    void appendSyncLogEntry(XCommand command, XEvent event);
    
    /**
     * remove all local commands from syncLog
     */
    void clearLocalChanges();
    
    /**
     * @return number of locally executed changes that are not yet stored on the
     *         server
     */
    int countUnappliedLocalChanges();
    
    /**
     * Convenience method
     * 
     * @return all {@link ISyncLogEntry} >= syncRevision
     */
    Iterator<ISyncLogEntry> getLocalChanges();
    
    /**
     * @return the revision number up until which all changes have been
     *         synchronized successfully with the server
     */
    long getSynchronizedRevision();
    
    /**
     * Returns an iterator over all {@link ISyncLogEntry} that were logged after
     * (and including) beginRevision and before (but not including) endRevision.
     * 
     * @param beginRevision
     * @param endRevision
     * @return an iterator over all matching {@link ISyncLogEntry}
     * @throws IndexOutOfBoundsException if beginRevision or endRevision are
     *             negative
     * @throws IllegalArgumentException if beginRevision is greater than
     *             endRevision
     */
    Iterator<ISyncLogEntry> getSyncLogEntriesBetween(long beginRevision, long endRevision);
    
    /**
     * Returns a list of all {@link ISyncLogEntry} that were logged after (and
     * including) the given revision number.
     * 
     * @param revisionNumber
     * @return all {@link ISyncLogEntry} until (but not including) the given
     *         revision number.
     * @throws IndexOutOfBoundsException if revisionNumber is negative
     */
    Iterator<ISyncLogEntry> getSyncLogEntriesSince(long revisionNumber);
    
    /**
     * Returns a list of all {@link ISyncLogEntry} that occurred until (but not
     * including) the given revision number.
     * 
     * @param revisionNumber
     * @return all {@link ISyncLogEntry} until (but not including) the given
     *         revision number.
     * @throws IndexOutOfBoundsException if revisionNumber is negative
     */
    Iterator<ISyncLogEntry> getSyncLogEntriesUntil(long revisionNumber);
    
    /**
     * Returns the ISyncLogEntry (basically a pair of {@link XCommand} and
     * {@link XEvent}) this sync log logged at the given revision number
     * 
     * @param revisionNumber the revision number which corresponding
     *            {@link ISyncLogEntry} logged by this sync log is to be
     *            returned
     * @return the {@link ISyncLogEntry} that was logged at the given revision
     *         number or null if the {@link ISyncLogEntry} cannot be accessed.
     * @throws IndexOutOfBoundsException if the given revision number is less
     *             than the first revision number or greater than or equal to
     *             the current revision number of this sync log
     */
    ISyncLogEntry getSyncLogEntryAt(long revisionNumber);
    
    /**
     * TODO why expose?
     * 
     * @return the internal, serialisable state holder
     */
    ISyncLogState getSyncLogState();
    
    void setSynchronizedRevision(long synchronizedRevision);
    
    /**
     * Removes all {@link ISyncLogEntry} that occurred after the given revision
     * number from this sync log, excluding the {@link ISyncLogEntry} that has
     * been loggerd at the given revision number
     * 
     * @param revisionNumber
     * @return true, if the operation could be executed, i.e. the given revision
     *         number was smaller than the current revision number and greater
     *         than zero.
     */
    @Override
    boolean truncateToRevision(long revisionNumber);
    
}
