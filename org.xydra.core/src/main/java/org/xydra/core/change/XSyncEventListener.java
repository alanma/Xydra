package org.xydra.core.change;

import org.xydra.base.change.XSyncEvent;


/**
 * @author xamde
 */
public interface XSyncEventListener {

    /**
     * Invoked when a synchronization occurs on the entity this listener is
     * registered on.
     *
     * @param event look inside to find if success or failure
     */
    void onSynced(XSyncEvent event);
}
