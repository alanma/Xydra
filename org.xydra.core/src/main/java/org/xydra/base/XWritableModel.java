package org.xydra.base;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;


public interface XWritableModel extends XHalfWritableModel {
	
	/**
	 * @param rev the new revision number
	 */
	void setRevisionNumber(long rev);
	
	/**
	 * @param object
	 */
	void addObject(XWritableObject object);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableObject getObject(XID objectId);
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XWritableObject createObject(XID id);
	
}
