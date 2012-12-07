package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An XObject allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XStateWritableObject extends XStateReadableObject {
	
	@ModificationOperation
	XStateWritableField createField(XID fieldId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XStateWritableField getField(XID fieldId);
	
	/**
	 * Removes the {@link XWritableField} with the given {@link XID} from this
	 * XObject
	 * 
	 * @param fieldId The field which is to be removed
	 * 
	 * @return true, if the specified {@link XWritableField} did exist and could
	 *         be removed
	 * @throws IllegalStateException if this object has already been removed
	 *             itself
	 */
	@ModificationOperation
	boolean removeField(XID fieldId);
	
}
