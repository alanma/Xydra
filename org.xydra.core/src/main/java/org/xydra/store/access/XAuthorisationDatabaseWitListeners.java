package org.xydra.store.access;

/**
 * An {@link XAuthorisationDatabase} with change events.
 * 
 * @author xamde
 */
public interface XAuthorisationDatabaseWitListeners extends XAuthorisationDatabase {
	
	/**
	 * Add a listener for access events.
	 * 
	 * @param listener
	 */
	void addListener(XAccessListener listener);
	
	/**
	 * Remove a listener for access events.
	 * 
	 * @param listener
	 */
	void removeListener(XAccessListener listener);
	
}
