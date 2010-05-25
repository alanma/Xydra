package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * Objects of classes that implement this interface have an XID.
 * 
 * @author voelkel
 */
public interface IHasXID {
	
	/**
	 * Returns the XID of the class.
	 * 
	 * @return The XID of the class
	 */
	@ReadOperation
	XID getID();
	
}
