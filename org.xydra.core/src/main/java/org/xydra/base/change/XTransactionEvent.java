package org.xydra.base.change;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;


/**
 * An {@link XEvent} representing changes made by {@link XTransaction
 * XTransactions}
 * 
 * It is always true that this.{@link #getChangeType()} ==
 * {@link ChangeType#TRANSACTION},
 * 
 * @author kaidel
 */
public interface XTransactionEvent extends XEvent, Iterable<XAtomicEvent> {
    
    /**
     * Returns the {@link XEvent} at the given index of the {@link XTransaction}
     * which caused this event.
     * 
     * @param index The index of the {@link XEvent} which will be returned
     * @return The event at the given index
     * @throws IndexOutOfBoundsException if index does not match
     */
    @NeverNull
    XAtomicEvent getEvent(int index) throws IndexOutOfBoundsException;
    
    /**
     * @return the number of events of the {@link XTransaction} which caused
     *         this event.
     */
    int size();
    
    /**
     * @return the last event on the event list. Can be null, if the transaction
     *         contains no events.
     */
    @CanBeNull
    XAtomicEvent getLastEvent();
    
}
