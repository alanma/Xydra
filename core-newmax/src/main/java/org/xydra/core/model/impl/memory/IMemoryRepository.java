package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.core.model.XRepository;


public interface IMemoryRepository extends XRepository, IMemoryEntity {
    
    void fireRepositoryEvent(XRepositoryEvent event);
    
    void fireModelEvent(XModelEvent event);
    
    void fireObjectEvent(XObjectEvent event);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireTransactionEvent(XTransactionEvent event);
    
    // TODO add this to other IMem also?
    void addModel(IMemoryModel memoryModel);
    
    XExistsRevWritableRepository getState();
    
}
