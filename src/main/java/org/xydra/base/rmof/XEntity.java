package org.xydra.base.rmof;

import org.xydra.base.IHasXAddress;
import org.xydra.base.IHasXID;
import org.xydra.base.XType;


/**
 * Basic entity for all entities in Xydra.
 * 
 * @author Kaidel
 * 
 */

public interface XEntity extends IHasXAddress, IHasXID {
	
	public XType getType();
	
}
