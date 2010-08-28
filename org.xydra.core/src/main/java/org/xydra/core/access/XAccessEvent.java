package org.xydra.core.access;

import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * An event to allow tracking changes to an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public interface XAccessEvent {
	
	ChangeType getChangeType();
	
	/**
	 * Returns the {@link XID} of the actor whose access definition has been
	 * modified.
	 * 
	 * @return the {@link XID} of the actor whose access definition has been
	 *         modified
	 */
	XID getActor();
	
	/**
	 * Returns the {@link XAddress} of the resource which access definition has
	 * been modified.
	 * 
	 * @return the {@link XAddress} of the resource which access definition has
	 *         been modified
	 */
	XAddress getResource();
	
	/**
	 * Returns the type of access being modified. See {@link XA} for more
	 * information.
	 * 
	 * @return the type of access being modified.
	 */
	XID getAccessType();
	
	/**
	 * Returns the previous value of the access definition. This is undefined
	 * for events of the ADD-type.
	 * 
	 * @return get the previous value of the access definition.
	 */
	XAccessValue getOldAccessValue();
	
	/**
	 * Returns the new value of the access definition. This is undefined for
	 * events of the REMOVE-type.
	 * 
	 * @return get the new value of the access definition.
	 */
	XAccessValue getNewAccessValue();
	
}
