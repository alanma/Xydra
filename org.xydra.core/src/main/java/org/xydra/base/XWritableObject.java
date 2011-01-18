package org.xydra.base;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.store.base.SimpleField;


public interface XWritableObject extends XHalfWritableObject {
	
	/**
	 * @param field
	 */
	void addField(SimpleField field);
	
	/**
	 * @param rev the new revision number
	 */
	void setRevisionNumber(long rev);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableField getField(XID fieldId);
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XWritableField createField(XID fieldId);
	
}
