package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * Interface for entities that have a full XAddress.
 * 
 * @author dscharrer
 * 
 */
public interface IHasXAddress {
	
	/**
	 * @return The {@link XAddress} of this entity
	 */
	@ReadOperation
	XAddress getAddress();
	
}
