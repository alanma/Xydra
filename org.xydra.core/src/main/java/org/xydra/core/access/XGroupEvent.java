package org.xydra.core.access;

import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XID;


/**
 * An event to allow tracking changes to an XGroupDatabase.
 * 
 * @author dscharrer
 * 
 */
public interface XGroupEvent {
	
	/**
	 * @return ADD or REMOVE if the actor is being added to / removed from the
	 *         group
	 */
	ChangeType getChangeType();
	
	/**
	 * @return the actor that is being added to / removed from a group
	 */
	XID getActor();
	
	/**
	 * @return the group to which an actor is being added / removed
	 */
	XID getGroup();
	
}
