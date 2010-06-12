package org.xydra.core.change;

/**
 * A listener interested in {@link XTransactionEvent XTransactionEvents}. Can be
 * registered on classes implementing the {@link XSendsTransactionEvents}
 * interface.
 * 
 * @author Kaidel
 */

public interface XTransactionEventListener {
	
	/**
	 * Invoked when an {@link XTransactionEvent} occurs on the entity this
	 * listener is registered on.
	 */
	void onChangeEvent(XTransactionEvent event);
}
