package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;


public interface XRevisionWritable {
    
    /**
     * Set the revision number of this entity. Revision number of contained
     * entities or any parent entity are not changed.
     * 
     * @param rev the new revision number
     */
    @ModificationOperation
    void setRevisionNumber(long rev);
    
    /**
     * Set if this entity exists or not. This flag is not persisted.
     * 
     * @param entityExists
     */
    void setExists(boolean entityExists);
    
}