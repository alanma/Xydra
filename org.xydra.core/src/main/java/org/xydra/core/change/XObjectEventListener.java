package org.xydra.core.change;

import org.xydra.base.change.XObjectEvent;


/**
 * A listener interested in {@link XObjectEvent XObjectEvents}. Can be
 * registered on classes implementing the {@link XSendsObjectEvents} interface.
 * 
 * @author kaidel
 */

public interface XObjectEventListener extends XChangeEventListener {
    
    /**
     * Invoked when an {@link XObjectEvent} occurs on the entity this listener
     * is registered on. Invoked just after the event has happened.
     * 
     * @param event
     */
    void onChangeEvent(XObjectEvent event);
}
