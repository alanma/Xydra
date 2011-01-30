package org.xydra.core.model;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.base.value.XValue;


/**
 * A change log that {@link XModel XModels} use to log changes on themselves,
 * their {@link XObject XObjects}, their {@link XField XFields} and their
 * {@link XValue XValues}.
 * 
 * @author Kaidel
 */

public interface XChangeLog extends Serializable {
	
	/**
	 * @return the {@link XAddress} of the {@link XModel} or {@link XObject}
	 *         this change log refers to. All contained events have been
	 *         produced by this entity or a descendant.
	 */
	public XAddress getBaseAddress();
	
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
	 * Returns an iterator over all {@link XEvent XEvents} that occurred after
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
	 * @throws IndexOutOfBoundsException if beginRevision or endRevision are
	 *             negative
	 * @throws IllegalArgumentException if beginRevision is greater than
	 *             endRevision
	 */
	public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision);
	
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
	public Iterator<XEvent> getEventsSince(long revisionNumber);
	
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
	public Iterator<XEvent> getEventsUntil(long revisionNumber);
	
	/**
	 * @return the revision number the logged {@link XModel} had at the time
	 *         when this change log began logging
	 */
	long getFirstRevisionNumber();
	
}
