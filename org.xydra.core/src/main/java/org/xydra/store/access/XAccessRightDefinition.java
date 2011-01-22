package org.xydra.store.access;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


/**
 * An access right definition as recorded in an {@link XAuthorisationDatabase}.
 * This is used to allow iterating over all access definitions in an
 * {@link XAuthorisationDatabase}.
 * 
 * @author dscharrer
 * 
 */
public interface XAccessRightDefinition {
	
	/**
	 * @return never null
	 */
	XID getAccess();
	
	/**
	 * @return never null
	 */
	XID getActor();
	
	/**
	 * @return never null
	 */
	XAddress getResource();
	
	/**
	 * @return true if the actor has the right of type 'getAccess' on the
	 *         resource.
	 */
	boolean isAllowed();
	
}
