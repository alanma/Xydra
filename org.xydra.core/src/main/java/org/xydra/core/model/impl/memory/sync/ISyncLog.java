package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;

/**
 * A log that is used to retrieve information about the properties of the change
 * of entities
 * 
 * @author kahmann
 * @author xamde
 */
public interface ISyncLog extends XWritableChangeLog, IBrowsableSyncLog {
	
	// from XWritableChangeLog
	@Override
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
	
	/**
	 * Internal use
	 * 
	 * @return number of sync log entries managed
	 */
	long getSize();
	
}
