package org.xydra.core.access;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


public interface XAccessDefinition {
	
	XID getActor();
	
	XAddress getResource();
	
	XID getAccess();
	
	boolean isAllowed();
	
}
