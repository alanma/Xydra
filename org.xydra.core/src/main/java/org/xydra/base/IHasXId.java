package org.xydra.base;

import org.xydra.annotations.ReadOperation;


/**
 * Objects of classes that implement this interface have an {@link XId}.
 * 
 * @author voelkel
 */
public interface IHasXId {
	
	/**
	 * Returns the {@link XId} of this entity.
	 * 
	 * @return The {@link XId} of this entity.
	 * @throws IllegalStateException if this entity has already been removed
	 */
	@ReadOperation
	XId getId();
	
}