package org.xydra.core.model.impl.memory;

import org.xydra.core.change.XObjectSyncEventListener;


/**
 * This interface indicates that it is possible to register
 * {@link XObjectSyncEventListener XSyncEventListeners}
 * 
 * @author alpha
 */
public interface XSendsObjectSyncEvents {
	
	/**
	 * Adds an {@link XObjectSyncEventListener}.
	 * 
	 * @param syncListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	public boolean addListenerForObjectSyncEvents(XObjectSyncEventListener syncListener);
	
	/**
	 * Removes the specified {@link XObjectSyncEventListener}.
	 * 
	 * @param syncListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForObjectSyncEvents(XObjectSyncEventListener syncListener);
}
