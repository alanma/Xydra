package org.xydra.base;

import java.util.Iterator;

import org.xydra.annotations.ReadOperation;


/**
 * A basic object that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XReadableObject extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * Returns the {@link XReadableField} contained in this object with the given
	 * {@link XID}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XReadableField} which is to be
	 *            returned
	 * @return The {@link XReadableField} with the given {@link XID} or null, if no
	 *         corresponding {@link XReadableField} exists
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	XReadableField getField(XID fieldId);
	
	/**
	 * Checks whether this {@link XReadableObject} contains an {@link XReadableField}
	 * with the given {@link XID}
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if this {@link XReadableObject} contains an {@link XReadableField}
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
	 * @return an iterator over the {@link XID XIDs} of the child-fields of this
	 *         XBaseObject.
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	Iterator<XID> iterator();
	
}
