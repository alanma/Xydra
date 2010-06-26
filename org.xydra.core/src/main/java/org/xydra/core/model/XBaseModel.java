package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * A basic model that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseModel extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * Returns the {@link XBaseObject} contained in this model with the given
	 * {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XBaseObject} which is to be
	 *            returned
	 * @return The {@link XBaseObject} with the given {@link XID} or null, if no
	 *         corresponding {@link XBaseObject} exists
	 */
	@ReadOperation
	XBaseObject getObject(XID objectId);
	
	/**
	 * Checks whether this {@link XBaseModel} already contains an
	 * {@link XBaseObject} with the given {@link XID}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if this {@link XBaseModel} already contains an
	 *         {@link XBaseObject} with the given {@link XID}, false otherwise
	 */
	@ReadOperation
	boolean hasObject(XID objectId);
	
	/**
	 * Returns the current revision number of this {@link XBaseModel}.
	 * 
	 * @return The current revision number of this {@link XBaseModel}.
	 */
	@ReadOperation
	long getRevisionNumber();
	
	/**
	 * Returns true, if this model has no child-objects
	 * 
	 * @return true, if this model has no child-objects
	 */
	@ReadOperation
	boolean isEmpty();
	
}
