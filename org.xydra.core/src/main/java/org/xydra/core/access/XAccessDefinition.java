package org.xydra.core.access;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * An Access right definition as recorded in an {@link XAccessManager}. This is
 * used to allow iterating over all access definitions in an
 * {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public interface XAccessDefinition {
	
	XID getActor();
	
	XAddress getResource();
	
	XID getAccess();
	
	boolean isAllowed();
	
}
