package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XSyncEvent;
import org.xydra.core.change.XSendsSyncEvents;
import org.xydra.core.change.XSyncEventListener;


// FIXME MAX set to default access
public interface IMemoryMOFEntity extends XSendsSyncEvents {
    
    /**
     * @return true iff this entity exists, i.e. has been created and has not
     *         been removed yet
     */
    boolean exists();
    
    Root getRoot();
    
    long getFatherRevisionNumber();
    
    void setExists(boolean exists);
    
    /**
     * @param event if this entity receives a sync-event; look inside to find
     *            out if sync-error or sync-success
     */
    void fireSyncEvent(XSyncEvent event);
    
    boolean addListenerForSyncEvents(XSyncEventListener syncListener);
    
    boolean removeListenerForSyncEvents(XSyncEventListener syncListener);
    
}
