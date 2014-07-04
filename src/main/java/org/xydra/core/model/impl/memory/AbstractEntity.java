package org.xydra.core.model.impl.memory;

import org.xydra.base.rmof.XEntity;


/**
 * Abstract super entity type for XModel, XObject and XField types containing
 * central implementations of methods, for example equals()
 * 
 * @author Kaidel
 * 
 */

public abstract class AbstractEntity implements XEntity {
    
    protected abstract long getRevisionNumber();
    
    /**
     * Compares address and revision number. Does not compare potential father
     * objects. Thus equality works well only within the same entity.
     * 
     * @see java.lang.Object#equals(java.lang.Object) for full contract.
     */
    @Override
    public boolean equals(Object object) {
        if(object instanceof AbstractEntity) {
            AbstractEntity entity = (AbstractEntity)object;
            if(this.getAddress().equals(entity.getAddress())
                    && this.getRevisionNumber() == entity.getRevisionNumber()) {
                
                return true;
            }
        }
        return false;
    }
    
    protected abstract AbstractEntity getFather();
    
    /**
     * Looks into address and revision number. Does not use potential father
     * objects. Thus equality works well only within the same entity.
     * 
     * @see java.lang.Object#hashCode() for full contract.
     */
    @Override
    public int hashCode() {
        int hashCode = this.getId().hashCode() + (int)this.getRevisionNumber();
        return hashCode;
    }
    
}
