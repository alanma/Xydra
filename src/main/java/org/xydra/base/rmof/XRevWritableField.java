package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;


/**
 * An {@link XRevWritableField} whose revision can be set.
 */
public interface XRevWritableField extends XWritableField {
	
	/**
	 * Set the revision number of this field. Revision number any parent object
	 * and/or model are not changed.
	 * 
	 * @param rev the new revision number
	 */
	@ModificationOperation
	void setRevisionNumber(long rev);
	
}
