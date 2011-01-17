package org.xydra.core.access;

import org.xydra.store.MAXTodo;


/**
 * A listener that listens for {@link XAccessEvent XAccessEvents}.
 * 
 * @author dscharrer
 * 
 */
@MAXTodo
public interface XAccessListener {
	
	/**
	 * Invoked when an {@link XAccessEvent} occurs.
	 */
	void onAccessEvent(XAccessEvent event);
	
}
