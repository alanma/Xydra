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
	 * Create a field with the given {@link XID}
	 */
	public void createField(XID fieldId);
	
	/**
	 * Remove the field with the given {@link XID}
	 */
	public void removeField(XID fieldId);
	
	public DeltaField getField(XID fieldId);
	
}
