package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;


/**
 * Allowing simple changes.
 * 
 * @author voelkel
 */
public interface XWritableRepository extends XBaseRepository {
	
	/**
	 * Creates a new {@link XWritableModel} with the given {@link XID} and adds
	 * it to this XWritableRepository or returns the already existing
	 * {@link XModel} if the given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XWritableModel} which is to be
	 *            created
	 * 
	 * @return the newly created {@link XWritableModel} or the already existing
	 *         {@link XWritableModel} if the given {@link XID} was already taken
	 */
	@ModificationOperation
	XWritableModel createModel(XID id);
	
	/**
	 * Removes the specified {@link XWritableModel} from this
	 * XWritableRepository.
	 * 
	 * @param baseRepository The {@link XID} of the {@link XWritableModel} which is
	 *            to be removed
	 * 
	 * @return true, if the specified {@link XWritableModel} could be removed,
	 *         false otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID modelID);
	
	@ReadOperation
	XWritableModel getModel(XID id);
	
}
