package org.xydra.core.model.delta;

import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;


/**
 * A {@link XBaseObject} that also allows direct changes.
 * 
 * @author dscharrer
 * 
 */
public interface DeltaObject extends XBaseObject {
	
	/**
	 * Creates a new {@link DeltaField} with the given {@link XID} and adds it
	 * to this DeltaObject if the given {@link XID} was not already taken.
	 * 
	 * @param fieldId The {@link XID} for the {@link DeltaField} which is to be
	 *            created
	 */
	public void createField(XID fieldId);
	
	/**
	 * Removes the {@link DeltaField} with the given {@link XID} from this
	 * DeltaObject.
	 * 
	 * @param fieldId The {@link XID} of the {@link DeltaField} which is to be
	 *            removed
	 */
	public void removeField(XID fieldId);
	
	/**
	 * Returns the {@link DeltaField} contained in this object with the given
	 * {@link XID}
	 * 
	 * @param objectId The {@link XID} of the {@link DeltaField} which is to be
	 *            returned
	 * @return The {@link DeltaField} with the given {@link XID} or null, if no
	 *         corresponding {@link DeltaField} exists
	 */
	public DeltaField getField(XID fieldId);
	
}
