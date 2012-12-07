package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * Allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XWritableRepository extends XStateWritableRepository, XReadableRepository {
	
	/* More specific return type */
	@ModificationOperation
	XWritableModel createModel(XID modelId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableModel getModel(XID modelId);
	
}
