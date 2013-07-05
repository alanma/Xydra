package org.xydra.base.change;

import org.xydra.base.XAddress;


public class XSyncEvent {
    
    private final XAddress changedEntity;
    
    private final boolean syncResult;
    
    public boolean isSyncSuccess() {
        return this.syncResult;
    }
    
    public XSyncEvent(XAddress changedEntity, boolean syncResult) {
        super();
        this.changedEntity = changedEntity;
        this.syncResult = syncResult;
    }
    
    /**
     * @return WHAT has been changed? The {@link XAddress} of the model, object
     *         or field that was synced.
     */
    public XAddress getChangedEntity() {
        return this.changedEntity;
    }
    
}
