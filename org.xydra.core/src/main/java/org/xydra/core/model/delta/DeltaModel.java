package org.xydra.core.model.delta;

import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;


/**
 * A {@link XBaseModel} that also allows direct changes.
 * 
 * @author dscharrer
 * 
 */
public interface DeltaModel extends XBaseModel {
	
	/**
	 * Create an object with the given {@link XID}
	 */
	public void createObject(XID objectId);
	
	/**
	 * Remove the object with the given {@link XID}
	 */
	public void removeObject(XID objectId);
	
	public DeltaObject getObject(XID objectId);
	
}
