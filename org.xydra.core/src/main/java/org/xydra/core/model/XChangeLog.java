package org.xydra.core.model;

import java.util.Iterator;
import java.util.List;

import org.xydra.core.change.XEvent;


/**
 * A change log that {@link XModel XModels} use to log changes on themselves,
 * their {@link XObjects}, their {@link XFields} and their {@link XValues}.
 * 
 * @author Kaidel
 */

public interface XChangeLog {
	
	/**
	 * Returns the {@link XAddress} of the {@link XModel} this change log refers
	 * to.
	 * 
	 * @return the {@link XAddress} of the {@link XModel} this change log refers
	 *         to.
	 */
	public XAddress getModelAddress();
	
	/**
	 * @return the revision number the logged {@link XModel} had at the time
	 *         when this change log began logging
	 */
	// TODO Is this still necessary? (currently every model is logged from the
	// beginning and this method should therefore always return 0)
	long getFirstRevisionNumber();
	
	/**
	 * @return the current revision number of the logged {@link XModel} as seen
	 *         from this log
	 */
	long getCurrentRevisionNumber();
	
	/**
	 * Returns the {@link XEvent} this change log logged at the given revision
	 * number
	 * 
	 * @param revisionNumber the revision number which corresponding
	 *            {@link XEvent} logged by this change log is to be returned
	 * @return the {@link XEvent} that was logged at the given revision number
	 *         or null if the {@link XEvent} cannot be accessed.
	 * @throws IndexOutOfBoundsException if the given revision number is less
	 *             than the first revision number or greater than or equal to
	 *             the current revision number of this change log
	 */
	public XEvent getEventAt(long revisionNumber);
	
	/**
	 * Returns a list of all {@link XEvent XEvents} that occurred until (but not
	 * including) the given revision number.
	 * 
	 * @param revisionNumber the revision number which corresponding
	 *            {@link XEvent} logged by this change log is to be returned
	 * @return a list of all {@link XEvent XEvents} that occurred until (but not
	 *         including) the given revision number.
	 * @throws IndexOutOfBoundsException if the given revision number is less
	 *             than the first revision number or greater than or equal to
	 *             the current revision number of this change log
	 */
	
	public List<XEvent> getAllEventsUntil(long revisionNumber);
	
	/**
	 * Returns a list of all {@link XEvent XEvents} that occurred after (and
	 * including) the given revision number.
	 * 
	 * @param revisionNumber the revision number which corresponding
	 *            {@link XEvent} logged by this change log is to be returned
	 * @return a list of all {@link XEvent XEvents} that occurred after (and
	 *         including) the given revision number.
	 * @throws IndexOutOfBoundsException if the given revision number is less
	 *             than the first revision number or greater than or equal to
	 *             the current revision number of this change log
	 */
	
	public List<XEvent> getAllEventsAfter(long revisionNumber);
	
	/**
	 * Returns an iterator oer all {@link XEvent XEvents} that occurred after
	 * (and including) beginRevision and before (but not including) endRevision.
	 * 
	 * @param beginRevision the beginning revision number of the interval from
	 *            which all {@link XEvent XEvents} are to be returned - can be
	 *            less than {@link #getFirstRevisionNumber()} to get all
	 *            {@link XEvent XEvents} up to endRevision
	 * @param endRevision the end revision number of the interval from which all
	 *            {@link XEvent XEvents} are to be returned - can be greater
	 *            than {@link #getCurrentRevisionNumber()} to get all
	 *            {@link XEvent XEvents} since beginRevision
	 * @return an iterator over all {@link XEvent XEvents} that occurred during
	 *         the specified interval of revision numbers
	 * @throws IndexOutOfBoundsException if beginRevision is greater than or
	 *             equal to the current revision number or if endRevision is
	 *             less than the first revision number of this changelog
	 * @throws IllegalArgumentException if beginRevision is greater than
	 *             endRevision
	 */
	public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision);
	
}
