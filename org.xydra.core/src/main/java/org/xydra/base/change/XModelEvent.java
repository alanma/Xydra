package org.xydra.base.change;

import org.xydra.base.XId;


/**
 * An {@link XEvent} representing changes of models.
 * 
 * @author Kaidel
 * 
 */
public interface XModelEvent extends XAtomicEvent {
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the changed model
     */
    XId getModelId();
    
    /**
     * WHAT has been changed?
     * 
     * @return the {@link XId} of the added/removed object.
     */
    XId getObjectId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the Parent-repository of the model where the
     *         change happened. It may be null.
     */
    XId getRepositoryId();
}
