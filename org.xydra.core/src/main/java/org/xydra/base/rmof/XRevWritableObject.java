package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An {@link XWritableObject} whose revision can be set and to which existing
 * {@link XRevWritableField XRevWritableFields} can be added.
 */
public interface XRevWritableObject extends XWritableObject {
	
	/**
	 * Add an existing field to this object. Fields created using
	 * {@link #createField(XID)} are automatically added.
	 * 
	 * This overwrites any existing field in this object with the same
	 * {@link XID}.
	 */
	@ModificationOperation
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
	 * Set the revision number of this object. Revision number of contained
	 * fields or any parent model are not changed.
	 * 
	 * @param rev the new revision number
	 */
	@ModificationOperation
	void setRevisionNumber(long rev);
	
}
