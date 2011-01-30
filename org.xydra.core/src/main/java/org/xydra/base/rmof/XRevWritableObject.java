package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


public interface XRevWritableObject extends XWritableObject {
	
	/**
	 * @param field
	 */
	void addField(XRevWritableField field);
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XRevWritableField createField(XID fieldId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XRevWritableField getField(XID fieldId);
	
	/**
	 * @param rev the new revision number
	 */
	void setRevisionNumber(long rev);
	
}
