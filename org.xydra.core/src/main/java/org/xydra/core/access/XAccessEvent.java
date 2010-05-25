package org.xydra.core.access;

import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * An event to allow tracking changes to an XAccessManager.
 * 
 * @author dscharrer
 * 
 */
public interface XAccessEvent {
	
	ChangeType getChangeType();
	
	/**
	 * @return the actor for whom an access definition is being modified.
	 */
	XID getActor();
	
	/**
	 * @return the resource for which an access definition is being modified.
	 */
	XAddress getResource();
	
	/**
	 * @return the type of access being modified.
	 */
	XID getAccessType();
	
	/**
	 * @return get the previous value of the access definition. This is
	 *         undefined for events of type ADD.
	 */
	boolean getOldAllowed();
	
	/**
	 * @return get the new value of the access definition. This is undefined for
	 *         events of type REMOVE.
	 */
	boolean getNewAllowed();
	
}
