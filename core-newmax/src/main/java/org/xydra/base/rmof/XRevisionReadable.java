package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;


public interface XRevisionReadable {
    
    /**
     * Gets the current revision number of this entity
     * 
     * @return The current revision number of this entity
     * @throws IllegalStateException if this object has already been removed
     */
    @ReadOperation
    long getRevisionNumber();
    
    /**
     * @return at runtime if the entity currently exists or not. This flag is
     *         not persisted.
     * @throws IllegalStateException if this object has already been removed
     */
    @ReadOperation
    boolean exists();
    
}
