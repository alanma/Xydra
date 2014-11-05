package org.xydra.core.change;

import org.xydra.base.change.XTransactionEvent;


/**
 * A listener interested in {@link XTransactionEvent XTransactionEvents}. Can be
 * registered on classes implementing the {@link XSendsTransactionEvents}
 * interface.
 * 
 * @author kaidel
 */

public interface XTransactionEventListener {
	
	/**
	 * Invoked when an {@link XTransactionEvent} occurs on the entity this
	 * listener is registered on.
	 * 
	 * @param event
	 */
	void onChangeEvent(XTransactionEvent event);
}
