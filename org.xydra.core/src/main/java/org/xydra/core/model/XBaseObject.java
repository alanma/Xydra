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
	 * @throws IllegalStateException if this object has already been removed
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
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	boolean hasField(XID fieldId);
	
	/**
	 * Returns the current revision number of this object.
	 * 
	 * @return The current revision number of this object.
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	long getRevisionNumber();
	
	/**
	 * Returns true, if this object has no child-fields
	 * 
	 * @return true, if this object has no child-fields
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	boolean isEmpty();
	
	/**
	 * @return an iterator over the {@link XID XIDs} of the child-{@link XField
	 *         XFields} of this XBaseObject.
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	Iterator<XID> iterator();
	
}
