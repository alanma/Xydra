package org.xydra.core.model;

import java.util.Iterator;
import java.util.Map.Entry;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


/**
 * A log that is used to retrieve information about the properties of the change
 * of entities
 * 
 * @author Andi K.
 */
public interface XSyncLog extends XLog {
	
	/**
	 * Returns the a pair of {@link XCommand} and {@link XEvent} this change log
	 * logged at the given revision number
	 * 
	 * @param revisionNumber the revision number which corresponding
	 *            {@link XEvent} logged by this change log is to be returned
	 * @return the {@link XEvent} that was logged at the given revision number
	 *         or null if the {@link XEvent} cannot be accessed.
	 * @throws IndexOutOfBoundsException if the given revision number is less
	 *             than the first revision number or greater than or equal to
	 *             the current revision number of this change log
	 */
	Entry<XCommand,XEvent> getCommandEventPairAt(long revisionNumber);
	
	/**
	 * Returns an iterator over all {@link XEvent XEvents} that occurred after
	 * (and including) beginRevision and before (but not including) endRevision.
	 * 
	 * @param beginRevision the beginning revision number of the interval from
	 *            which all {@link XEvent XEvents} are to be returned - can be
	 *            less than {@link #getBaseRevisionNumber()} to get all
	 *            {@link XEvent XEvents} up to endRevision
	 * @param endRevision the end revision number of the interval from which all
	 *            {@link XEvent XEvents} are to be returned - can be greater
	 *            than {@link #getCurrentRevisionNumber()} to get all
	 *            {@link XEvent XEvents} since beginRevision
	 * @return an iterator over all {@link XEvent XEvents} that occurred during
	 *         the specified interval of revision numbers
	 * @throws IndexOutOfBoundsException if beginRevision or endRevision are
	 *             negative
	 * @throws IllegalArgumentException if beginRevision is greater than
	 *             endRevision
	 */
	Iterator<Entry<XCommand,XEvent>> getCommandEventPairsBetween(long beginRevision,
	        long endRevision);
	
	/**
	 * Returns a list of all {@link XEvent XEvents} that occurred after (and
	 * including) the given revision number.
	 * 
	 * @param revisionNumber the revision number determining which
	 *            {@link XEvent XEvents} logged by this change log are to be
	 *            returned
	 * @return a list of all {@link XEvent XEvents} that occurred after (and
	 *         including) the given revision number.
	 * @throws IndexOutOfBoundsException if revisionNumber is negative
	 */
	Iterator<Entry<XCommand,XEvent>> getCommandEventPairsSince(long revisionNumber);
	
	/**
	 * Returns a list of all {@link XEvent XEvents} that occurred until (but not
	 * including) the given revision number.
	 * 
	 * @param revisionNumber the revision number determining which
	 *            {@link XEvent XEvents} logged by this change log are to be
	 *            returned
	 * @return a list of all {@link XEvent XEvents} that occurred until (but not
	 *         including) the given revision number.
	 * @throws IndexOutOfBoundsException if revisionNumber is negative
	 */
	Iterator<Entry<XCommand,XEvent>> getCommandEventPairsUntil(long revisionNumber);
	
	/**
	 * @return the revision number the logged {@link XModel} had at the time
	 *         just before this change log began logging.
	 */
	long getSyncRevisionNumber();
	
	/**
	 * @return the internal, serialisable state holder
	 */
	XSyncLogState getSyncLogState();
	
	/**
	 * @return the last event or null; convenience method
	 */
	Entry<XCommand,XEvent> getLastCommandEventPairs();
	
}
