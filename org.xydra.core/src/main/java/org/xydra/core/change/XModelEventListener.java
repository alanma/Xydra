package org.xydra.core.change;

/**
 * A listener interested in {@link XModelEvent XModelEvents}. Can be registered
 * on classes implementing the {@link XSendsModelEvents} interface.
 * 
 * @author Kaidel
 */

public interface XModelEventListener {
	
	/**
	 * Invoked when an {@link XModelEvent} occurs on the entity this listener is
	 * registered on.
	 */
	void onChangeEvent(XModelEvent event);
}
