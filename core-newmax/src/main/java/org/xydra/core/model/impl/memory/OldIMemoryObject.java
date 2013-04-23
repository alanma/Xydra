package org.xydra.core.model.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.model.XObject;


// FIXME MAX set to default access
public interface OldIMemoryObject extends XObject, IMemoryEntity, IMemoryMOFEntity {
    
    void fireObjectEvent(XObjectEvent event);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireTransactionEvent(XTransactionEvent event);
    
    SynchronisationState getSyncState();
    
    IMemoryModel getFather();
    
    IMemoryField getField(XId fieldId);
    
    void enqueueFieldRemoveEvents(XId actor, IMemoryField field, boolean inTrans, boolean implied);
    
    void delete();
    
    @Deprecated
    IMemoryField createFieldInternal(XId fieldId);
    
    @Deprecated
    void removeFieldInternal(XId fieldId);
    
    void setRevisionNumber(long newRevision);
    
    XRevWritableObject getState();
    
    void incrementRevision();
    
    void removeInternal();
    
    long getModelRevisionNumber();
}