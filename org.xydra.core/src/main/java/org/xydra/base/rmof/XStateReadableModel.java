package org.xydra.base.rmof;

import java.util.Iterator;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * A readable model that provides only state information and no revison numbers.
 */
public interface XStateReadableModel extends XEntity, Iterable<XId> {

	/**
	 * Returns the {@link XReadableObject} contained in this model with the
	 * given {@link XId}
	 *
	 * @param objectId The {@link XId} of the {@link XReadableObject} which is
	 *            to be returned
	 * @return The {@link XReadableObject} with the given {@link XId} or null,
	 *         if no corresponding {@link XReadableObject} exists
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ReadOperation
	XStateReadableObject getObject(@NeverNull XId objectId);

	/**
	 * Checks whether this {@link XReadableModel} already contains an
	 * {@link XReadableObject} with the given {@link XId}.
	 *
	 * @param objectId The {@link XId} which is to be checked
	 * @return true, if this {@link XReadableModel} already contains an
	 *         {@link XReadableObject} with the given {@link XId}, false
	 *         otherwise
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ReadOperation
	boolean hasObject(@NeverNull XId objectId);

	/**
	 * Returns true, if this model has no child-objects
	 *
	 * @return true, if this model has no child-objects
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ReadOperation
	boolean isEmpty();

	/**
	 * @return an iterator over the {@link XId XIds} of the child-objects of
	 *         this XBaseModel.
	 * @throws IllegalStateException if this model has already been removed
	 */
	@Override
	@ReadOperation
	Iterator<XId> iterator();

}
