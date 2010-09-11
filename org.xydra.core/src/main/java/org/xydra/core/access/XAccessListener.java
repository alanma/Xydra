package org.xydra.core.access;

/**
 * A listener that listens for {@link #XAccessEvent}s.
 * 
 * @author dscharrer
 * 
 */

public interface XAccessListener {
	
	/**
	 * Invoked when an {@link XAccessEvent} occurs.
	 */
	void onAccessEvent(XAccessEvent event);
	
}
