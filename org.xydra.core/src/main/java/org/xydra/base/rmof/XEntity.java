package org.xydra.base.rmof;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;


/**
 * Basic entity for all entities in Xydra.
 * 
 * @author Kaidel
 * 
 */

public interface XEntity {
	
	public XType getType();
	
	public XID getID();
	
	public XAddress getAddress();
}
