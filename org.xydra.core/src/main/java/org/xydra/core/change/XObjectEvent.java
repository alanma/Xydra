package org.xydra.core.change;

import org.xydra.core.model.XID;


/**
 * An event observed by an XObject (mostly happening within).
 * 
 * 
 * @author voelkel
 * 
 */
public interface XObjectEvent extends XAtomicEvent {
	
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
	 * @return the XObject WHERE the change happens
	 */
	XID getObjectID();
	
	/**
	 * WHAT is changed?
	 * 
	 * @return the XID of the added/deleted field
	 */
	XID getFieldID();
	
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
	
	/**
	 * @return The revision number of the field at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not a field)
	 */
	long getFieldRevisionNumber();
	
}
