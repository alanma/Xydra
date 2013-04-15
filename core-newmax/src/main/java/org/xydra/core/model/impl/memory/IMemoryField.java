package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;


// FIXME MAX set to default access
public interface IMemoryField extends XField, IMemoryEntity, IMemoryMOFEntity {
    
    void delete();
    
    long executeFieldCommand(XFieldCommand command, XLocalChangeCallback callback);
    
    void fireFieldEvent(XFieldEvent event);
    
    void fireFieldSyncEvent(XFieldEvent event);
    
    IMemoryObject getObject();
    
    XRevWritableField getState();
    
    void incrementRevision();
    
    boolean isSynchronized();
    
    void setRevisionNumber(long newRevision);
    
    void setValueInternal(XValue value);
    
    /**
     * Current snapshot-like state is handled locally, change log and event
     * listening is handled by Root.
     * 
     * Executing commands (and transactions) is handled by Root to respect
     * locking correctly.
     */
    
}
