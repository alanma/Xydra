package org.xydra.core.model.delta;

import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;


/**
 * A subtype of {@link XBaseModel} that also supports change operations.
 * 
 * @author dscharrer
 * 
 */
public interface DeltaModel extends XBaseModel {
	
	/**
	 * Creates a new {@link DeltaObject} with the given {@link XID} and adds it
	 * to this DeltaModel if the given {@link XID} was not already taken.
	 * 
	 * @param objectId The {@link XID} for the {@link DeltaObject} which is to
	 *            be created
	 */
	public void createObject(XID objectId);
	
	/**
	 * Removes the {@link DeltaObject} with the given {@link XID} from this
	 * DeltaModel.
	 * 
	 * @param objectId The {@link XID} of the {@link DeltaObject} which is to be
	 *            removed
	 */
	public void removeObject(XID objectId);
	
	/**
	 * Returns the {@link DeltaObject} contained in this model with the given
	 * {@link XID}
	 * 
	 * @param objectId The {@link XID} of the {@link DeltaObject} which is to be
	 *            returned
	 * @return The {@link DeltaObject} with the given {@link XID} or null, if no
	 *         corresponding {@link DeltaObject} exists
	 */
	public DeltaObject getObject(XID objectId);
	
}
