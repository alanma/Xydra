package org.xydra.core.change;

import org.xydra.core.model.XID;


/**
 * A change happening within a repository
 * 
 * @author voelkel
 * 
 */
public interface XRepositoryEvent extends XAtomicEvent {
	
	/**
	 * WHERE is the change?
	 * 
	 * @return the XID of the XRepository WHERE the change happens
	 */
	XID getRepositoryID();
	
	/**
	 * WHAT is changed?
	 * 
	 * @return the XID of the model that was added/deleted.
	 */
	XID getModelID();
	
	/**
	 * @return The revision number of the model at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not a model or has no father-model)
	 */
	long getModelRevisionNumber();
	
}
