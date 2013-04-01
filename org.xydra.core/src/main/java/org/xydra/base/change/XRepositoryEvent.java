package org.xydra.base.change;

import org.xydra.base.XId;


/**
 * An {@link XEvent} representing changes of repositories.
 * 
 * @author voelkel
 */
public interface XRepositoryEvent extends XAtomicEvent {
    
    /**
     * WHAT was changed?
     * 
     * @return the {@link XId} of the model that was added/removed.
     */
    XId getModelId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the repository where the change happened
     */
    XId getRepositoryId();
}
