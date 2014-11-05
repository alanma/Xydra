package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * Allowing simple changes, but not to the revision number.
 * 
 * @author xamde
 */
public interface XStateWritableRepository extends XStateReadableRepository {
	
	/**
	 * Creates a new {@link XWritableModel} with the given {@link XId} and adds
	 * it to this XRevWritableRepository or returns the already existing model
	 * if the given {@link XId} was already taken.
	 * 
	 * @param modelId The {@link XId} for the {@link XWritableModel} which is to
	 *            be created
	 * 
	 * @return the newly created {@link XWritableModel} or the already existing
	 *         {@link XWritableModel} if the given {@link XId} was already taken
	 */
	@ModificationOperation
	XStateWritableModel createModel(XId modelId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XStateWritableModel getModel(XId modelId);
	
	/**
	 * Removes the specified {@link XWritableModel} from this
	 * XRevWritableRepository.
	 * 
	 * @param modelId The {@link XId} of the {@link XWritableModel} which is to
	 *            be removed
	 * 
	 * @return true, if the specified {@link XWritableModel} could be removed,
	 *         false otherwise
	 */
	@ModificationOperation
	boolean removeModel(XId modelId);
	
}
