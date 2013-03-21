package org.xydra.core.change;

import org.xydra.base.change.XFieldEvent;


/**
 * 
 * 
 * @author alpha
 */

public interface XFieldSyncEventListener {
	
	/**
	 * Invoked when a synchronization occurs on the entity this listener is
	 * registered on.
	 * 
	 * @param event
	 */
	void onSynced(XFieldEvent event);
}
