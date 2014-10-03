package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * An {@link XReadableModel} which allows also simple changes, but not to the
 * revision number.
 * 
 * @author xamde
 */
public interface XWritableModel extends XReadableModel, XStateWritableModel {
	
	/**
	 * Creates a new {@link XWritableObject} with the given {@link XId} and adds
	 * it to this {@link XWritableModel} or returns the already existing
	 * {@link XWritableObject} if the given {@link XId} was already taken.
	 * 
	 * @param objectId The {@link XId} for the {@link XWritableObject} which is
	 *            to be created
	 * 
	 * @return the newly created {@link XWritableObject} or the already existing
	 *         {@link XWritableObject} if the given {@link XId} was already
	 *         taken
	 * @throws IllegalStateException if this model has already been removed
	 */
	@Override
	@ModificationOperation
	XWritableObject createObject(@NeverNull XId objectId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableObject getObject(@NeverNull XId objectId);
	
}
