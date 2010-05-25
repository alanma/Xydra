package org.xydra.core.change;

/**
 * This interface indicated that you can register or remove listeners to listen
 * for {@link XTransactions}s
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsTransactionEvents {
	
	/**
	 * Adds an XTransactionEventListener.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener);
	
	/**
	 * Removes the specified XTransactionEventListener.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener);
}
