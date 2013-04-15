package org.xydra.store.access;

/**
 * A listener that listens for {@link XAuthorisationEvent XAccessEvents}.
 * 
 * @author dscharrer
 * 
 */

public interface XAccessListener {
	
	/**
	 * Invoked when an {@link XAuthorisationEvent} occurs.
	 * 
	 * @param event
	 */
	void onAccessEvent(XAuthorisationEvent event);
	
}
