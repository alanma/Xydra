package org.xydra.store.access;

import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;


/**
 * An event to allow tracking changes to an {@link XGroupDatabaseWithListeners}.
 * 
 * @author dscharrer
 * 
 */
public interface XGroupEvent {
	
	/**
	 * @return the {@link XId} of the actor that is being added to / removed
	 *         from a group
	 */
	XId getActor();
	
	/**
	 * Returns the type of this event.
	 * 
	 * @return ADD or REMOVE if the actor is being added to / removed from the
	 *         group
	 */
	ChangeType getChangeType();
	
	/**
	 * @return the {@link XId} of the group to which an actor is being added /
	 *         removed
	 */
	XId getGroup();
	
}
