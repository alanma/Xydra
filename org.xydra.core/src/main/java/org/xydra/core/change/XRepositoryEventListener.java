package org.xydra.core.change;

import org.xydra.base.change.XRepositoryEvent;


/**
 * A listener interested in {@link XRepositoryEvent XRepositoryEvents}. Can be
 * registered on classes implementing the {@link XSendsRepositoryEvents}
 * interface.
 * 
 * @author Kaidel
 */

public interface XRepositoryEventListener extends XChangeEventListener {
    
    /**
     * Invoked when an {@link XRepositoryEvent} occurs on the entity this
     * listener is registered on. Invoked just after the event has happened.
     * 
     * @param event
     */
    void onChangeEvent(XRepositoryEvent event);
}
