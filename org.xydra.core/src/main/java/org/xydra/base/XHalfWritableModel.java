package org.xydra.base;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;


/**
 * An {@link XReadableModel} which allows also simple changes, but not to the
 * revision number.
 * 
 * @author voelkel
 */
public interface XHalfWritableModel extends XReadableModel {
	
	/**
	 * Creates a new {@link XHalfWritableObject} with the given {@link XID} and
	 * adds it to this {@link XHalfWritableModel} or returns the already
	 * existing {@link XHalfWritableObject} if the given {@link XID} was already
	 * taken.
	 * 
	 * @param id The {@link XID} for the {@link XHalfWritableObject} which is to
	 *            be created
	 * 
	 * @return the newly created {@link XHalfWritableObject} or the already
	 *         existing {@link XHalfWritableObject} if the given {@link XID} was
	 *         already taken
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	XHalfWritableObject createObject(XID id);
	
	/**
	 * Removes the {@link XHalfWritableObject} with the given {@link XID} from
	 * this {@link XHalfWritableModel}.
	 * 
	 * @param object The {@link XID} of the {@link XHalfWritableObject} which is
	 *            to be removed
	 * 
	 * @return true, if an {@link XHalfWritableObject} with the given
	 *         {@link XID} did exist in this {@link XHalfWritableModel} and
	 *         could be removed
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	boolean removeObject(XID objectID);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XHalfWritableObject getObject(XID objectId);
	
}
