package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.impl.memory.MemoryModel;



public interface XChangeLogState extends Serializable {
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 */
	void delete();
	
	/**
	 * Store the data of this object in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 */
	void save();
	
	/**
	 * @return the revision number the logged model had at the time when this
	 *         log began to log
	 */
	long getFirstRevisionNumber();
	
	/**
	 * @return the current revision number of the logged model as seen from this
	 *         log
	 */
	long getCurrentRevisionNumber();
	
	/**
	 * @param revisionNumber the revision number corresponding to the event that
	 *            is saved in the log
	 * @return the XEvent with the corresponding revision number, if there is no
	 *         such element, null will be returned
	 */
	
	XEvent getEvent(long revisionNumber);
	
	/**
	 * Adds the given event to the log
	 * 
	 * @param event The event which is to be logged
	 */
	void appendEvent(XEvent event);
	
	/**
	 * Removes all events from this change log state that occurred after the
	 * given revision number.
	 * 
	 * @param revisionNumber
	 * @return true, if truncating was successful, false otherwise (may happen
	 *         if the given revision number was bigger than the current revision
	 *         number of this change log state or smaller than the revision
	 *         number when this change log began logging)
	 */
	
	boolean truncateToRevision(long revisionNumber);
	
	/**
	 * Returns the {@link XAddress} of the {@link MemoryModel} this changelog
	 * refers to.
	 * 
	 * @return the {@link XAddress} of the {@link MemoryModel} this changelog
	 *         refers to.
	 */
	// TODO not sure, if this is necessary
	XAddress getModelAddress();
	
}
