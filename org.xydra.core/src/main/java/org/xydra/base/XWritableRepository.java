package org.xydra.base;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;


public interface XWritableRepository extends XHalfWritableRepository {
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XWritableModel createModel(XID modelId);
	
	@ModificationOperation
	boolean removeModel(XID modelId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableModel getModel(XID modelId);
	
}
