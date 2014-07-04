package org.xydra.base;

import org.xydra.annotations.ReadOperation;


/**
 * Interface for entities that have an {@link XAddress}.
 * 
 * @author dscharrer
 * 
 */
public interface IHasXAddress {
	
	/**
	 * @return The {@link XAddress} of this entity
	 * @throws IllegalStateException if this entity has already been removed
	 */
	@ReadOperation
	XAddress getAddress();
	
}
