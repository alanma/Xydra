package org.xydra.core.model.impl.memory;

/**
 * Indicates that this entity is synchronizable, i.e. it can be synchronized and
 * persisted on a server using the {@link org.xydra.store.sync.NewSyncer}.
 * 
 * @author alpha
 * 
 */
public interface Synchronizable {
    
    /**
     * @return true if the entity has been synchronized AKA persisted, false if
     *         it has pending local changes.
     */
    boolean isSynchronized();
}
