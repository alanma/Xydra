package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


public interface XRevWritableRepository extends XWritableRepository {
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XRevWritableModel createModel(XID modelId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XRevWritableModel getModel(XID modelId);
	
	@ModificationOperation
	boolean removeModel(XID modelId);
	
}
