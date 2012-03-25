package org.xydra.base;

import org.xydra.annotations.ReadOperation;


/**
 * Objects of classes that implement this interface have an {@link XID}.
 * 
 * @author voelkel
 */
public interface IHasXID {
	
	/**
	 * Returns the {@link XID} of this entity.
	 * 
	 * @return The {@link XID} of this entity.
	 * @throws IllegalStateException if this entity has already been removed
	 */
	@ReadOperation
	XID getId();
	
}
