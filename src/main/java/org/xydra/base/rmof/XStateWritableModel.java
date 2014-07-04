package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * An {@link XStateReadableModel} which allows just to read and write the state.
 * No support for revision numbers at all.
 * 
 * @author voelkel
 */
public interface XStateWritableModel extends XStateReadableModel {
	
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
	@ModificationOperation
	XStateWritableObject createObject(@NeverNull XId objectId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XStateWritableObject getObject(@NeverNull XId objectId);
	
	/**
	 * Removes the {@link XWritableObject} with the given {@link XId} from this
	 * {@link XWritableModel}.
	 * 
	 * @param objectId The {@link XId} of the {@link XWritableObject} which is
	 *            to be removed
	 * 
	 * @return true, if an {@link XWritableObject} with the given {@link XId}
	 *         did exist in this {@link XWritableModel} and could be removed
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	boolean removeObject(@NeverNull XId objectId);
	
}
