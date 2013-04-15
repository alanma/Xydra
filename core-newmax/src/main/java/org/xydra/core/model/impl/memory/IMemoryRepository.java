package org.xydra.core.model.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.model.XRepository;


// FIXME MAX set to default access
public interface IMemoryRepository extends XRepository, IMemoryEntity {
    
    void fireRepositoryEvent(XRepositoryEvent event);
    
    void fireModelEvent(XModelEvent event);
    
    void fireObjectEvent(XObjectEvent event);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireTransactionEvent(XTransactionEvent event);
    
    void addModel(IMemoryModel memoryModel);
    
    boolean removeModelInternal(XId id);
    
    void updateRemoved(IMemoryModel model);
    
}
