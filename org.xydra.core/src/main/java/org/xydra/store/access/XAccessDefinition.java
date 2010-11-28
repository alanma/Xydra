package org.xydra.store.access;

import org.xydra.core.access.XAccessManagerWithListeners;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * An Access right definition as recorded in an {@link XAccessManagerWithListeners}. This is
 * used to allow iterating over all access definitions in an
 * {@link XAccessManagerWithListeners}.
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
	
	boolean isAllowed();
	
}
