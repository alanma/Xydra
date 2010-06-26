package org.xydra.core.model;

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
	 */
	@ReadOperation
	XID getID();
	
}
