package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


public interface XRevWritableModel extends XWritableModel {
	
	/**
	 * @param object
	 */
	void addObject(XRevWritableObject object);
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XRevWritableObject createObject(XID id);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XRevWritableObject getObject(XID objectId);
	
	/**
	 * @param rev the new revision number
	 */
	void setRevisionNumber(long rev);
	
}
