package org.xydra.core.change;

import org.xydra.base.change.XTransactionEvent;


/**
 * This interface indicates that it is possible to register
 * {@link XTransactionEventListener XTransactionEventListeners} to listen for
 * {@link XTransactionEvent XTransactionEvents}.
 * 
 * @author xamde
 * @author kaidel
 */
public interface XSendsTransactionEvents {
	
	/**
	 * Adds an {@link XTransactionEventListener}.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener);
	
	/**
	 * Removes the specified {@link XTransactionEventListener}.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener);
}
