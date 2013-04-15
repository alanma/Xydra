package org.xydra.core.model.impl.memory;

import org.xydra.base.rmof.XEntity;


// FIXME MAX set to default access
public interface IMemoryEntity extends XEntity {
    
    /**
     * @return the current revision number of this entity. Is 0 if the entity
     *         has been created. Can be -1 if this instance represents an entity
     *         that has not been created.
     */
    public long getRevisionNumber();
    
}
