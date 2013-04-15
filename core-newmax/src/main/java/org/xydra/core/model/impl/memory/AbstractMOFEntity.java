package org.xydra.core.model.impl.memory;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.ReadOperation;


public abstract class AbstractMOFEntity extends AbstractEntity implements IMemoryMOFEntity {
    
    /**
     * @return the father entity or null if this entity is the root
     */
    @CanBeNull
    protected abstract IMemoryEntity getFather();
    
    /**
     * @return true if the entity currently exists
     */
    public boolean exists() {
        return this.exists;
    }
    
    public AbstractMOFEntity(Root root, boolean exists) {
        this.root = root;
        this.exists = exists;
    }
    
    protected boolean exists;
    
    @Override
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    
    /**
     * Handles all registered event listeners. Also the lock for synchronising
     * change operations.
     */
    protected final Root root;
    
    @Override
    public Root getRoot() {
        return this.root;
    }
    
    public long getFatherRevisionNumber() {
        if(this.getFather() != null)
            return this.getFather().getRevisionNumber();
        return getRevisionNumber();
    }
    
    public abstract Object getStateLock();
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryModel was already removed
     */
    @ReadOperation
    public void assertThisEntityExists() throws IllegalStateException {
        synchronized(getStateLock()) {
            if(!exists()) {
                throw new IllegalStateException("this entity has been removed: " + getAddress());
            }
        }
    }
    
}
