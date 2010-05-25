package org.xydra.core.change;

import org.xydra.core.model.XID;


/**
 * An event observed by an XModel (mostly happening within).
 * 
 * @author Kaidel
 * 
 */

public interface XModelEvent extends XAtomicEvent {
	
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
	 * @return the XModel WHERE the change happens
	 */
	XID getModelID();
	
	/**
	 * WHAT is changed?
	 * 
	 * @return the XID of the added/deleted object.
	 */
	XID getObjectID();
	
	/**
	 * @return The revision number of the model at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not a model or has no father-model)
	 */
	long getModelRevisionNumber();
	
	/**
	 * @return The revision number of the object at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not an object or has no father-object)
	 */
	long getObjectRevisionNumber();
	
}
