package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An {@link XReadableModel} which allows also simple changes, but not to the
 * revision number.
 * 
 * @author xamde
 */
public interface XWritableModel extends XReadableModel, XStateWritableModel {
	
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
	XWritableObject createObject(@NeverNull XID objectId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XWritableObject getObject(@NeverNull XID objectId);
	
}
