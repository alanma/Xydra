package org.xydra.core.change;

import org.xydra.base.change.XObjectEvent;



/**
 * 
 * 
 * @author alpha
 */

public interface XObjectSyncEventListener {
	
	/**
	 * Invoked when a synchronization occurs on the entity this listener is
	 * registered on.
	 * 
	 * @param event
	 */
	void onSynced(XObjectEvent event);
}
