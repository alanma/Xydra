package org.xydra.core.model.impl.memory;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.change.XSyncEvent;
import org.xydra.core.change.XSyncEventListener;


public abstract class AbstractMOFEntity extends AbstractEntity implements IMemoryMOFEntity {
    
    /**
     * @return true if the entity currently exists
     */
    public boolean exists() {
        return this.exists;
    }
    
    public AbstractMOFEntity(Root root, boolean exists) {
        assert root != null;
        
        this.root = root;
        this.exists = exists;
    }
    
    private boolean exists;
    
    @Override
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    
    /**
     * Handles all registered event listeners. Also the lock for synchronising
     * change operations.
     */
    private final Root root;
    
    @Override
    public Root getRoot() {
        return this.root;
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryModel was already removed
     */
    @ReadOperation
    public void assertThisEntityExists() throws IllegalStateException {
        // TODO synchronize?
        if(!exists()) {
            throw new IllegalStateException("this entity has been removed: " + getAddress());
        }
    }
    
    @Override
    public boolean addListenerForSyncEvents(XSyncEventListener syncListener) {
        return this.root.addListenerForSyncEvents(getAddress(), syncListener);
    }
    
    @Override
    public boolean removeListenerForSyncEvents(XSyncEventListener syncListener) {
        return this.root.removeListenerForSyncEvents(getAddress(), syncListener);
    }
    
    @Override
    public void fireSyncEvent(XSyncEvent event) {
        this.root.fireSyncEvent(getAddress(), event);
    }
    
}
