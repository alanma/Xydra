package org.xydra.core.change;

import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;


/**
 * An event observed by an XField (mostly happening within).
 * 
 * @author Kaidel
 * 
 */

public interface XFieldEvent extends XAtomicEvent {
	
	/**
	 * WHERE is the change?
	 * 
	 * @return the XID of the XRepository WHERE the change happens. It may be
	 *         null.
	 */
	XID getRepositoryID();
	
	/**
	 * WHERE is the change?
	 * 
	 * @return the XID of the XModel WHERE the change happens. It may be null.
	 */
	XID getModelID();
	
	/**
	 * WHERE is the change?
	 * 
	 * @return the XID of the XObject WHERE the change happens. It may be null.
	 */
	XID getObjectID();
	
	/**
	 * WHERE is the change?
	 * 
	 * @return the XField WHERE the change happens
	 */
	XID getFieldID();
	
	/**
	 * WHAT is changed?
	 * 
	 * @return the new value. null indicates a delete.
	 */
	XValue getNewValue();
	
	/**
	 * WHAT is changed?
	 * 
	 * @return the old value. null indicates an add.
	 */
	XValue getOldValue();
	
}
