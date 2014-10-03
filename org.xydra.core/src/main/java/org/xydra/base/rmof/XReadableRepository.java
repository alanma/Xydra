package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


public interface XReadableRepository extends XStateReadableRepository {
	
	/* More specific return type */
	@Override
	@ReadOperation
	XReadableModel getModel(XId id);
}
