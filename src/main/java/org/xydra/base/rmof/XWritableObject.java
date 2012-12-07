package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An XObject allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XWritableObject extends XReadableObject, XStateWritableObject {
	
	/**
	 * Creates a new {@link XWritableField} and adds it to this
	 * {@link XWritableObject} or returns the already existing
	 * {@link XWritableField} if the given {@link XID} was already taken.
	 * 
	 * @param fieldId The {@link XID} for the {@link XWritableField} which is to
	 *            be created.
	 * 
	 * @return the newly created {@link XWritableField} or the already existing
	 *         {@link XWritableField} with this {@link XID}
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ModificationOperation
	XWritableField createField(XID fieldId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableField getField(XID fieldId);
	
}
