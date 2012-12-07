package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An {@link XStateReadableModel} which allows just to read and write the state.
 * No support for revision numbers at all.
 * 
 * @author voelkel
 */
public interface XStateWritableModel extends XStateReadableModel {
	
	/**
	 * Creates a new {@link XWritableObject} with the given {@link XID} and adds
	 * it to this {@link XWritableModel} or returns the already existing
	 * {@link XWritableObject} if the given {@link XID} was already taken.
	 * 
	 * @param objectId The {@link XID} for the {@link XWritableObject} which is
	 *            to be created
	 * 
	 * @return the newly created {@link XWritableObject} or the already existing
	 *         {@link XWritableObject} if the given {@link XID} was already
	 *         taken
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	XStateWritableObject createObject(@NeverNull XID objectId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XStateWritableObject getObject(@NeverNull XID objectId);
	
	/**
	 * Removes the {@link XWritableObject} with the given {@link XID} from this
	 * {@link XWritableModel}.
	 * 
	 * @param objectId The {@link XID} of the {@link XWritableObject} which is
	 *            to be removed
	 * 
	 * @return true, if an {@link XWritableObject} with the given {@link XID}
	 *         did exist in this {@link XWritableModel} and could be removed
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	boolean removeObject(@NeverNull XID objectId);
	
}
