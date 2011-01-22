package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An {@link XReadableModel} which allows also simple changes, but not to the
 * revision number.
 * 
 * @author voelkel
 */
public interface XWritableModel extends XReadableModel {
	
	/**
	 * Creates a new {@link XWritableObject} with the given {@link XID} and adds
	 * it to this {@link XWritableModel} or returns the already existing
	 * {@link XWritableObject} if the given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XWritableObject} which is to be
	 *            created
	 * 
	 * @return the newly created {@link XWritableObject} or the already existing
	 *         {@link XWritableObject} if the given {@link XID} was already
	 *         taken
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	XWritableObject createObject(XID id);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableObject getObject(XID objectId);
	
	/**
	 * Removes the {@link XWritableObject} with the given {@link XID} from this
	 * {@link XWritableModel}.
	 * 
	 * @param object The {@link XID} of the {@link XWritableObject} which is to
	 *            be removed
	 * 
	 * @return true, if an {@link XWritableObject} with the given {@link XID}
	 *         did exist in this {@link XWritableModel} and could be removed
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	boolean removeObject(XID objectId);
	
}
