package org.xydra.core.model.impl.memory.garbage;

import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLog;


public interface XWritableChangeLog extends XChangeLog {
    
    /**
     * Removes all {@link XEvent XEvents} that occurred after the given revision
     * number from this MemoryChangeLog, excluding the {@link XEvent} that
     * occurred at the given revision number
     * 
     * @param revisionNumber the revision number from which on the
     *            {@link XEvent XEvents} are to be removed
     * @return true, if the operation could be executed, i.e. the given revision
     *         number was smaller than the current revision number and greater
     *         than zero.
     */
    boolean truncateToRevision(long revisionNumber);
    
    /**
     * Appends an {@link XEvent} to the end of this MemoryChangeLog
     * 
     * @param event the {@link XEvent} which is to be appended @CanBeNull during
     *            play-back when synchronising
     */
    void appendEvent(XEvent event);
    
}
