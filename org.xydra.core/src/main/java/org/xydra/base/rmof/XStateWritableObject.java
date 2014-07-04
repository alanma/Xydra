package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * An XObject allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XStateWritableObject extends XStateReadableObject {
	
	@ModificationOperation
	XStateWritableField createField(XId fieldId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XStateWritableField getField(XId fieldId);
	
	/**
	 * Removes the {@link XWritableField} with the given {@link XId} from this
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
	boolean removeField(XId fieldId);
	
}
