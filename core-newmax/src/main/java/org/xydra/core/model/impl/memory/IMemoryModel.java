package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.core.model.XModel;


// FIXME MAX set to default access
public interface IMemoryModel extends XModel, IMemoryEntity, IMemoryMOFEntity {
    
    XExistsRevWritableModel getState();
    
    IMemoryRepository getFather();
    
    void fireModelEvent(XModelEvent event);
    
    void fireObjectEvent(XObjectEvent event);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireTransactionEvent(XTransactionEvent event);
    
}
