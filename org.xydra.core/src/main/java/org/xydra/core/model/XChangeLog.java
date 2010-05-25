package org.xydra.core.model;

import java.util.Iterator;
import java.util.List;

import org.xydra.core.change.XEvent;



/**
 * A change log that {@link XModels} can use to log changes on themselves, their
 * objects, their fields and their values.
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
	 * @return the revision number the logged model had at the time when this
	 *         log began to log
	 */
	long getFirstRevisionNumber();
	
	/**
	 * @return the current revision number of the logged model as seen from this
	 *         log
	 */
	long getCurrentRevisionNumber();
	
	/**
	 * Returns the event this log logged at the given revision number
	 * 
	 * @param revisionNumber
	 * @return the event that was logged at the given revision number or null if
	 *         the event cannot be accessed.
	 */
	public XEvent getEventAt(long revisionNumber);
	
	/**
	 * Returns a list of all events that occurred until (but not including) the
	 * given revision number.
	 * 
	 * @param revisionNumber
	 * @return a list of all events that occured until (but not including) the
	 *         given revision number.
	 */
	
	public List<XEvent> getAllEventsUntil(long revisionNumber);
	
	/**
	 * Returns a list of all events that occurred after (and including) the
	 * given revision number.
	 * 
	 * @param revisionNumber
	 * @return a list of all events that occurred after (and including) the
	 *         given revision number.
	 */
	
	public List<XEvent> getAllEventsAfter(long revisionNumber);
	
	/**
	 * Returns a list of all events that occurred after (and including)
	 * beginRevision and before (but not including) endRevision.
	 * 
	 * @param beginRevision can be less than {@link #getFirstRevisionNumber()}
	 *            to get all events up to endRveision
	 * @param endRevision can be greater than
	 *            {@link #getCurrentRevisionNumber()} to get all events since
	 *            beginRevision
	 */
	public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision);
	
}
