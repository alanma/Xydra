package org.xydra.store.access;

import org.xydra.annotations.NeverNull;
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
	 * @return ..
	 */
	@NeverNull
	XID getAccess();
	
	/**
	 * @return ..
	 */
	@NeverNull
	XID getActor();
	
	/**
	 * @return ..
	 */
	@NeverNull
	XAddress getResource();
	
	/**
	 * @return true if the actor has the right of type 'getAccess' on the
	 *         resource.
	 */
	boolean isAllowed();
	
}
