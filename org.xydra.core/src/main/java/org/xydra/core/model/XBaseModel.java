package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * A basic {@link XModel} that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseModel extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * @param id The XID of the wanted {@link XBaseObject}
	 * @return The {@link XBaseObject} with the given XID or null, if no
	 *         corresponding {@link XBaseObject} exists
	 */
	@ReadOperation
	XBaseObject getObject(XID objectId);
	
	/**
	 * Checks whether this {@link XBaseModel} already contains an
	 * {@link XBaseObject} with the given XID.
	 * 
	 * @param id The XID which is to be checked
	 * @return true, if this {@link XBaseModel} already contains an
	 *         {@link XBaseObject} with the given XID, false otherwise
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
