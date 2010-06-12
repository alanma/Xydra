package org.xydra.core.change;

/**
 * A listener interested in {@link XFieldEvent XFieldEvents}. Can be registered
 * on classes implementing the {@link XSendsFieldEvents} interface.
 * 
 * @author Kaidel
 */

public interface XFieldEventListener {
	
	/**
	 * Invoked when an {@link XFieldEvent} occurs on the entity this listener is
	 * registered on.
	 */
	void onChangeEvent(XFieldEvent event);
}
