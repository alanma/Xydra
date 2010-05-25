package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * A basic {@link XObject} that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseObject extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * Returns the field corresponding to the given XID in this object.
	 * 
	 * @param fieldID The XID of the wanted {@link XBaseField}
	 * @return The {@link XBaseField} with the given XID or null, if no
	 *         corresponding {@link XBaseField} exists
	 */
	@ReadOperation
	XBaseField getField(XID fieldId);
	
	/**
	 * Checks whether this {@link XBaseObject} contains an {@link XBaseField}
	 * with the given XID
	 * 
	 * @param id The XID which is to be checked
	 * @return true, if this {@link XBaseObject} contains an {@link XBaseField}
	 *         with the given XID, false otherwise
	 */
	@ReadOperation
	boolean hasField(XID fieldId);
	
	/**
	 * Returns the current revision number of this {@link XBaseObject}.
	 * 
	 * @return The current revision number of this {@link XBaseObject}.
	 */
	@ReadOperation
	long getRevisionNumber();
	
	/**
	 * Returns true, if this object has no child-fields
	 * 
	 * @return true, if this object has no child-fields
	 */
	@ReadOperation
	boolean isEmpty();
	
}
