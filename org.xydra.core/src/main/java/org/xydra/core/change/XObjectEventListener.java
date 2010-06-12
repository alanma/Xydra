package org.xydra.core.change;

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
	 */
	void onChangeEvent(XObjectEvent event);
}
