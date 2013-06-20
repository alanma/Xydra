package org.xydra.core.model.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.model.XObject;


public interface IMemoryObject extends XObject, IMemoryEntity, IMemoryMOFEntity {
    
    void fireObjectEvent(XObjectEvent event);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireTransactionEvent(XTransactionEvent event);
    
    IMemoryModel getFather();
    
    IMemoryField getField(XId fieldId);
    
    XRevWritableObject getState();
    
}
