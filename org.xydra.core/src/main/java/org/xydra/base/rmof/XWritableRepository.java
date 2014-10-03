package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * Allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XWritableRepository extends XStateWritableRepository, XReadableRepository {
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XWritableModel createModel(XId modelId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableModel getModel(XId modelId);
	
}
