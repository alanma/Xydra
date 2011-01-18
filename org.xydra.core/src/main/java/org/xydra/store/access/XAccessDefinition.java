package org.xydra.store.access;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


/**
 * An access right definition as recorded in an {@link XAccessDatabase}. This is
 * used to allow iterating over all access definitions in an
 * {@link XAccessDatabase}.
 * 
 * @author dscharrer
 * 
 */
public interface XAccessDefinition {
	
	/**
	 * @return never null
	 */
	XID getActor();
	
	/**
	 * @return never null
	 */
	XAddress getResource();
	
	/**
	 * @return never null
	 */
	XID getAccess();
	
	/**
	 * @return true if the actor has the right of type 'getAccess' on the
	 *         resource.
	 */
	boolean isAllowed();
	
}
