package org.xydra.store.access;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;


/**
 * An event to allow tracking changes to an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */

public interface XAuthorisationEvent {
	
	/**
	 * Returns the type of access being modified. See {@link XA} for more
	 * information.
	 * 
	 * @return the type of access being modified.
	 */
	XID getAccessType();
	
	/**
	 * Returns the {@link XID} of the actor whose access definition has been
	 * modified.
	 * 
	 * @return the {@link XID} of the actor whose access definition has been
	 *         modified
	 */
	XID getActor();
	
	ChangeType getChangeType();
	
	/**
	 * Returns the new value of the access definition. This is undefined for
	 * events of the REMOVE-type.
	 * 
	 * @return get the new value of the access definition.
	 */
	XAccessRightValue getNewAccessValue();
	
	/**
	 * Returns the previous value of the access definition. This is undefined
	 * for events of the ADD-type.
	 * 
	 * @return get the previous value of the access definition.
	 */
	XAccessRightValue getOldAccessValue();
	
	/**
	 * Returns the {@link XAddress} of the resource which access definition has
	 * been modified.
	 * 
	 * @return the {@link XAddress} of the resource which access definition has
	 *         been modified
	 */
	XAddress getResource();
	
}
