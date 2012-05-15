package org.xydra.base.rmof;

import java.util.Iterator;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * A basic object that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XStateReadableObject extends XEntity, Iterable<XID> {
	
	/**
	 * Returns the {@link XReadableField} contained in this object with the
	 * given {@link XID}.
	 * 
	 * @param fieldId The {@link XID} of the {@link XReadableField} which is to
	 *            be returned
	 * @return The {@link XReadableField} with the given {@link XID} or null, if
	 *         no corresponding {@link XReadableField} exists
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	XStateReadableField getField(XID fieldId);
	
	/**
	 * Checks whether this {@link XReadableObject} contains an
	 * {@link XReadableField} with the given {@link XID}
	 * 
	 * @param fieldId The {@link XID} which is to be checked
	 * @return true, if this {@link XReadableObject} contains an
	 *         {@link XReadableField} with the given {@link XID}, false
	 *         otherwise
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	boolean hasField(XID fieldId);
	
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
	@Override
	@ReadOperation
	Iterator<XID> iterator();
	
}
