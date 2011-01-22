package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * Allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XWritableRepository extends XReadableRepository {
	
	/**
	 * Creates a new {@link XWritableModel} with the given {@link XID} and adds
	 * it to this XRevWritableRepository or returns the already existing model
	 * if the given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XWritableModel} which is to be
	 *            created
	 * 
	 * @return the newly created {@link XWritableModel} or the already existing
	 *         {@link XWritableModel} if the given {@link XID} was already taken
	 */
	@ModificationOperation
	XWritableModel createModel(XID modelId);
	
	@ReadOperation
	XWritableModel getModel(XID modelId);
	
	/**
	 * Removes the specified {@link XWritableModel} from this
	 * XRevWritableRepository.
	 * 
	 * @param baseRepository The {@link XID} of the {@link XWritableModel} which
	 *            is to be removed
	 * 
	 * @return true, if the specified {@link XWritableModel} could be removed,
	 *         false otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID modelId);
	
}
