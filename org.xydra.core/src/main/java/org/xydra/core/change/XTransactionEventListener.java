package org.xydra.core.change;

/**
 * The basic interface for ChangeListeneres that listens for transactions in an
 * XModel/XObject.
 * 
 * @author Kaidel
 */

public interface XTransactionEventListener {
	
	/**
	 * A single transaction
	 * 
	 * @param event
	 */
	void onChangeEvent(XTransactionEvent event);
}
