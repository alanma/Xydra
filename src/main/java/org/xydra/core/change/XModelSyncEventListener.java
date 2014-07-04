package org.xydra.core.change;

import org.xydra.base.change.XModelEvent;


/**
 * 
 * 
 * @author alpha
 */

public interface XModelSyncEventListener {
	
	/**
	 * Invoked when a synchronization occurs on the entity this listener is
	 * registered on.
	 * 
	 * @param event
	 */
	void onSynced(XModelEvent event);
}
