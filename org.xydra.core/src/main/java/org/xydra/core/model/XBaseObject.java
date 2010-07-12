package org.xydra.core.model;

import java.util.Iterator;

import org.xydra.annotations.ReadOperation;


/**
 * A basic object that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseObject extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * Returns the {@link XBaseField} contained in this object with the given
	 * {@link XID}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XBaseField} which is to be
	 *            returned
	 * @return The {@link XBaseField} with the given {@link XID} or null, if no
	 *         corresponding {@link XBaseField} exists
	 */
	@ReadOperation
	XBaseField getField(XID fieldId);
	
	/**
	 * Checks whether this {@link XBaseObject} contains an {@link XBaseField}
	 * with the given {@link XID}
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if this {@link XBaseObject} contains an {@link XBaseField}
	 *         with the given {@link XID}, false otherwise
	 */
	@ReadOperation
	boolean hasField(XID fieldId);
	
	/**
	 * Returns the current revision number of this object.
	 * 
	 * @return The current revision number of this object.
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
	
	/**
	 * @return an iterator over the {@link XID XIDs} of the child-{@link XField
	 *         XFields} of this XBaseObject.
	 */
	Iterator<XID> iterator();
	
}
