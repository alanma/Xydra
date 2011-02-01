package org.xydra.base.change;

/**
 * An {@link XEvent} representing changes made by {@link XTransaction
 * XTransactions}
 * 
 * TODO is it always true that this.{@link #getChangeType()} ==
 * {@link ChangeType#TRANSACTION} ?
 * 
 * @author Kaidel
 */

public interface XTransactionEvent extends XEvent, Iterable<XAtomicEvent> {
	
	/**
	 * Returns the {@link XEvent} at the given index of the {@link XTransaction}
	 * which caused this event.
	 * 
	 * @param index The index of the {@link XEvent} which will be returned
	 * @return The event at the given index
	 */
	XAtomicEvent getEvent(int index);
	
	/**
	 * @return the number of events of the {@link XTransaction} which caused
	 *         this event.
	 */
	int size();
	
}
