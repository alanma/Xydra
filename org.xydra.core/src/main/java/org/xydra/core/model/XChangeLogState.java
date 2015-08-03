package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XRevWritableModel;


/**
 * An {@link XChangeLogState} represents the inner state of an
 * {@link XChangeLog} for example for persistence/serialisation purposes.
 *
 * A change log contains all events that have happened on top of a given model
 * state ( {@link XRevWritableModel} or more complex types ) (which could have
 * come from a snapshot).
 *
 * Example:
 *
 * <pre>
 * ChangeLog:
 *
 * 17: event for a removed object
 * 16: event for a changed field
 * 15: event for an added field
 * 14: event for an added object
 *
 * (model state, last rev = 13)
 * </pre>
 *
 * Or for a new, just created model:
 *
 * <pre>
 * ChangeLog:
 *
 * (empty)
 *
 * (model state, last rev = 0)
 * </pre>
 *
 * Or for a model with a single object added:
 *
 * <pre>
 * ChangeLog:
 *
 * 1: event for add-object
 *
 * (model state, last rev = 0)
 * </pre>
 *
 * @author xamde
 */
public interface XChangeLogState extends Serializable {

	/**
	 * Appends the given {@link XEvent} to the log
	 *
	 * @param event The {@link XEvent} which is to be logged
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by {@link XObject},
	 *            {@link XModel} or {@link XRepository} containing the
	 *            XChangeLog represented by this XChangeLogState
	 */
	void appendEvent(XEvent event);

	/**
	 * @return the {@link XAddress} of the {@link XModel} or {@link XObject}
	 *         this change log refers to. All contained events have been
	 *         produced by this entity or a descendant.
	 */
	XAddress getBaseAddress();

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
	 * @return the revision number the logged {@link XModel} had at the time
	 *         when this change log began logging
	 */
	long getBaseRevisionNumber();

	/**
	 * Set the first revision number. This can only be done if the change log is
	 * empty.
	 *
	 * @param rev
	 */
	void setBaseRevisionNumber(long rev);

	/**
	 * Removes all {@link XEvent XEvents} from this XChangeLogState that
	 * occurred after the given revision number.
	 *
	 * @param revisionNumber the revision number from which on the
	 *            {@link XEvent XEvents} are to be removed
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by {@link XObject},
	 *            {@link XModel} or {@link XRepository} containing the
	 *            XChangeLog represented by this XChangeLogState
	 * @return true, if truncating was successful, false otherwise (may happen
	 *         if the given revision number was bigger than the current revision
	 *         number of this change log state or smaller than the revision
	 *         number when this change log began logging)
	 */

	boolean truncateToRevision(long revisionNumber);

	/**
	 * @return the last event or null if empty; convenience method
	 */
	XEvent getLastEvent();

}
