package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * A basic repository that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseRepository extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * Returns the {@link XBaseModel} contained in this repository with the
	 * given {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XBaseModel} which is to be
	 *            returned
	 * @return the {@link XBaseModel} with the given {@link XID} or null if no
	 *         such {@link XBaseModel} exists in this repository.
	 */
	@ReadOperation
	XBaseModel getModel(XID id);
	
	/**
	 * Checks whether this repository contains an {@link XBaseModel} with the
	 * given {@link XID}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if this repository contains an {@link XBaseModel} with the
	 *         given {@link XID}, false otherwise
	 */
	@ReadOperation
	boolean hasModel(XID id);
	
	/**
	 * Returns true, if this repository has no child-models
	 * 
	 * @return true, if this repository has no child-models
	 */
	@ReadOperation
	boolean isEmpty();
	
}
