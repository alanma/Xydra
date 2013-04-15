package org.xydra.core.change;

import org.xydra.base.change.XFieldEvent;


/**
 * A listener interested in {@link XFieldEvent XFieldEvents}. Can be registered
 * on classes implementing the {@link XSendsFieldEvents} interface.
 * 
 * TODO what events are listened to exactly? do I get transactions in the model
 * that make changes to this field? (same question for
 * {@link XObjectEventListener})
 * 
 * @author Kaidel
 */

public interface XFieldEventListener {
	
	/**
	 * Invoked when an {@link XFieldEvent} occurs on the entity this listener is
	 * registered on.
	 * 
	 * @param event
	 */
	void onChangeEvent(XFieldEvent event);
}
