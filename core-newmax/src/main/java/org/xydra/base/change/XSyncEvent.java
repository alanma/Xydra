package org.xydra.base.change;

import org.xydra.base.XAddress;


public class XSyncEvent {
    
    /**
     * WHAT has been changed? The {@link XAddress} of the model, object or field
     * that was synced.
     */
    public final XAddress changedEntity;
    
    public XSyncEvent(XAddress changedEntity, boolean syncResult) {
        super();
        this.changedEntity = changedEntity;
        this.syncResult = syncResult;
    }
    
    public final boolean syncResult;
    
}
