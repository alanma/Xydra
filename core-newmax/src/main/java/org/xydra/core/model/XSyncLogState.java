package org.xydra.core.model;

import java.util.Map.Entry;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


public interface XSyncLogState extends XLogState {
	
	/**
	 * TODO adapt documentation Appends the given {@link XEvent} to the log
	 * 
	 * @param event The {@link XEvent} which is to be logged
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by {@link XObject},
	 *            {@link XModel} or {@link XRepository} containing the
	 *            XChangeLog represented by this XChangeLogState
	 */
	void appendCommandEventPair(XCommand command, XEvent event);
	
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
	Entry<XCommand,XEvent> getCommandEventPair(long revisionNumber);
	
	/**
	 * @return the last event or null if empty; convenience method
	 */
	Entry<XCommand,XEvent> getLastCommandEventPair();
	
	/**
	 * @return the revision number the logged {@link XModel} had at the time
	 *         when this change log began logging
	 */
	long getSyncRevisionNumber();
	
	/**
	 * Set the first revision number. This can only be done if the change log is
	 * empty.
	 * 
	 * @param rev
	 */
	void setSyncRevisionNumber(long rev);
	
}
