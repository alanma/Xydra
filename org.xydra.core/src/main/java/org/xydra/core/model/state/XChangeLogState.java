package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;


/**
 * An {@link XChangeLogState} represents the inner state of an
 * {@link XChangeLog} for example for persistence purposes.
 */

public interface XChangeLogState extends Serializable {
	
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created the containing
	 *            {@link XObjectState}, {@link XModelState} or
	 *            {@link XRepositoryState}
	 */
	void delete(Object transaction);
	
	/**
	 * Store the data of this object in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created the containing
	 *            {@link XObjectState}, {@link XModelState} or
	 *            {@link XRepositoryState}
	 */
	void save(Object transaction);
	
	/**
	 * @return the revision number the logged {@link XModel} had at the time
	 *         when this change log began logging
	 */
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
	XEvent getEvent(long revisionNumber);
	
	/**
	 * Appends the given {@link XEvent} to the log
	 * 
	 * @param event The {@link XEvent} which is to be logged
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created the containing
	 *            {@link XObjectState}, {@link XModelState} or
	 *            {@link XRepositoryState}
	 */
	void appendEvent(XEvent event, Object transaction);
	
	/**
	 * Removes all {@link XEvent XEvents} from this XChangeLogState that
	 * occurred after the given revision number.
	 * 
	 * @param revisionNumber the revision number from which on the
	 *            {@link XEvent XEvents} are to be removed
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created the containing
	 *            {@link XObjectState}, {@link XModelState} or
	 *            {@link XRepositoryState}
	 * @return true, if truncating was successful, false otherwise (may happen
	 *         if the given revision number was bigger than the current revision
	 *         number of this change log state or smaller than the revision
	 *         number when this change log began logging)
	 */
	
	boolean truncateToRevision(long revisionNumber, Object transaction);
	
	/**
	 * @return the {@link XAddress} of the {@link XModelState} or
	 *         {@link XObjectState} this changelog refers to. All contained
	 *         events have been produced by this entity or a descendant.
	 */
	XAddress getBaseAddress();
	
}
