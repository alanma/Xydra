package org.xydra.base.rmof.impl;



/**
 * Entities that maintain an internal 'exists' boolean flag. They make
 * implementing executing transactions much easier.
 * 
 * @author xamde
 * 
 */
public interface XExists extends XExistsReadable {
    
    /**
     * Set if this entity exists or not. This flag is not persisted.
     * 
     * @param entityExists
     */
    void setExists(boolean entityExists);
    
}
