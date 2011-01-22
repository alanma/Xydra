package org.xydra.store.access;

/**
 * An {@link org.xydra.store.access.XGroupDatabase} that allows also Listeners.
 * 
 * @author dscharrer
 */
public interface XGroupDatabaseWithListeners extends XGroupDatabase {
	
	/**
	 * Add a listener for {@link XGroupEvent XGroupEvents}. The listener will
	 * only receive events for defined group memberships, not for implied group
	 * memberships.
	 */
	void addListener(XGroupListener listener);
	
	/**
	 * Remove a listener for {@link XGroupEvent}s.
	 */
	void removeListener(XGroupListener listener);
	
}
