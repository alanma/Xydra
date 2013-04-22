package org.xydra.core.model.impl.memory;

import org.xydra.core.model.XField;


// FIXME MAX set to default access
public interface IMemoryField extends XField, IMemoryEntity, IMemoryMOFEntity {
    
    // boolean isSynchronized();
    
    /**
     * Current snapshot-like state is handled locally, change log and event
     * listening is handled by Root.
     * 
     * Executing commands (and transactions) is handled by Root to respect
     * locking correctly.
     */
    
}
