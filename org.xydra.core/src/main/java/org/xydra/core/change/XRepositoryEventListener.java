package org.xydra.core.change;

/**
 * A listener interested in {@link XRepositoryEvent XRepositoryEvents}. Can be
 * registered on classes implementing the {@link XSendsRepositoryEvents}
 * interface.
 * 
 * @author Kaidel
 */

public interface XRepositoryEventListener {
	
	/**
	 * Invoked when an {@link XRepositoryEvent} occurs on the entity this
	 * listener is registered on.
	 */
	void onChangeEvent(XRepositoryEvent event);
}
