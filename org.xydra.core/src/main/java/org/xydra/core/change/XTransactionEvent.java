package org.xydra.core.change;

/**
 * The event-type that represents transactions.
 * 
 * @author Kaidel
 */

public interface XTransactionEvent extends XEvent, Iterable<XAtomicEvent> {
	
	/**
	 * Returns the event at the given index
	 * 
	 * @param index The index of the event that will be returned
	 * @return The event at the given index
	 */
	XAtomicEvent getEvent(int index);
	
	/**
	 * Returns the number of events of the transaction
	 * 
	 * @return the number of events of the transaction
	 */
	int size();
	
}
