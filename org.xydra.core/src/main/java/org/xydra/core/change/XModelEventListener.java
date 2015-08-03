package org.xydra.core.change;

import org.xydra.base.change.XModelEvent;


/**
 * A listener interested in {@link XModelEvent XModelEvents}. Can be registered
 * on classes implementing the {@link XSendsModelEvents} interface.
 *
 * @author kaidel
 */

public interface XModelEventListener extends XChangeEventListener {

    /**
     * Invoked when an {@link XModelEvent} occurs on the entity this listener is
     * registered on. Invoked just after the event has happened.
     *
     * @param event
     */
    void onChangeEvent(XModelEvent event);
}
