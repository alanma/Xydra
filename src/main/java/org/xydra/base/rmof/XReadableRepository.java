package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


public interface XReadableRepository extends XStateReadableRepository {
	
	/* More specific return type */
	@ReadOperation
	XReadableModel getModel(XID id);
}
