package org.xydra.core.change;

import org.xydra.base.change.XObjectEvent;


/**
 * A listener interested in {@link XObjectEvent XObjectEvents}. Can be
 * registered on classes implementing the {@link XSendsObjectEvents} interface.
 * 
 * @author Kaidel
 */

public interface XObjectEventListener {
	
	/**
	 * Invoked when an {@link XObjectEvent} occurs on the entity this listener
	 * is registered on.
	 * 
	 * @param event
	 */
	void onChangeEvent(XObjectEvent event);
}
