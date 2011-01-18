package org.xydra.base;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;


/**
 * An XObject allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XHalfWritableObject extends XReadableObject {
	
	/**
	 * Creates a new {@link XHalfWritableField} and adds it to this
	 * {@link XHalfWritableObject} or returns the already existing
	 * {@link XHalfWritableField} if the given {@link XID} was already taken.
	 * 
	 * @param fieldId The {@link XID} for the {@link XHalfWritableField} which
	 *            is to be created.
	 * 
	 * @return the newly created {@link XHalfWritableField} or the already
	 *         existing {@link XHalfWritableField} with this {@link XID}
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ModificationOperation
	XHalfWritableField createField(XID fieldId);
	
	/**
	 * Removes the {@link XHalfWritableField} with the given {@link XID} from
	 * this XObject
	 * 
	 * @param fieldId The field which is to be removed
	 * 
	 * @return true, if the specified {@link XHalfWritableField} did exist and
	 *         could be removed
	 * @throws IllegalStateException if this object has already been removed
	 *             itself
	 */
	@ModificationOperation
	boolean removeField(XID fieldId);
	
	@ReadOperation
	XHalfWritableField getField(XID fieldId);
	
}
