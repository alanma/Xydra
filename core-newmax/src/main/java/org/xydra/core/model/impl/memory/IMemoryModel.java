package org.xydra.core.model.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;


// FIXME MAX set to default access
public interface IMemoryModel extends XModel, IMemoryEntity, IMemoryMOFEntity {
    
    XRevWritableModel getState();
    
    // TODO kill
    SynchronisationState getSyncState();
    
    long executeCommandWithActor(XCommand command, XId sessionActor, String sessionPasswordHash,
            XLocalChangeCallback callback);
    
    void incrementRevision();
    
    IMemoryRepository getFather();
    
    boolean enqueueModelRemoveEvents(XId sessionActor);
    
    void removeInternal();
    
    void fireModelEvent(XModelEvent event);
    
    void fireObjectEvent(XObjectEvent event);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireTransactionEvent(XTransactionEvent event);
    
    void fireModelSyncEvent(XModelEvent event);
    
    void fireObjectSyncEvent(XObjectEvent event);
    
    void fireFieldSyncEvent(XFieldEvent event);
    
}
