package org.xydra.core.model.impl.memory;

// FIXME MAX set to default access
public interface IMemoryMOFEntity {
    
    /**
     * @return true iff this entity exists, i.e. has been created and has not
     *         been removed yet
     */
    boolean exists();
    
    Root getRoot();
    
    long getFatherRevisionNumber();
    
    void setExists(boolean exists);
    
}
