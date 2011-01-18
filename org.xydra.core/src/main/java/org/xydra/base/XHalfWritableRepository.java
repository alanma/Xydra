package org.xydra.base;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;


/**
 * Allowing simple changes, but not to the revision number.
 * 
 * @author voelkel
 */
public interface XHalfWritableRepository extends XReadableRepository {
	
	/**
	 * Creates a new {@link XHalfWritableModel} with the given {@link XID} and
	 * adds it to this XWritableRepository or returns the already existing model
	 * if the given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XHalfWritableModel} which is to
	 *            be created
	 * 
	 * @return the newly created {@link XHalfWritableModel} or the already
	 *         existing {@link XHalfWritableModel} if the given {@link XID} was
	 *         already taken
	 */
	@ModificationOperation
	XHalfWritableModel createModel(XID modelId);
	
	/**
	 * Removes the specified {@link XHalfWritableModel} from this
	 * XWritableRepository.
	 * 
	 * @param baseRepository The {@link XID} of the {@link XHalfWritableModel}
	 *            which is to be removed
	 * 
	 * @return true, if the specified {@link XHalfWritableModel} could be
	 *         removed, false otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID modelId);
	
	@ReadOperation
	XHalfWritableModel getModel(XID modelId);
	
}
