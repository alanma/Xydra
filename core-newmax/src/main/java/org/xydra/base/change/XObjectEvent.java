package org.xydra.base.change;

import org.xydra.base.XId;


/**
 * An {@link XEvent} representing changes of objects
 * 
 * 
 * @author voelkel
 */
public interface XObjectEvent extends XAtomicEvent {
    
    /**
     * WHAT was changed?
     * 
     * @return the {@link XId} of the added/removed field
     */
    XId getFieldId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the Parent-model of the object where the
     *         change happened. It may be null.
     */
    XId getModelId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the changed object
     */
    XId getObjectId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the Parent-repository of the object where the
     *         change happened. It may be null.
     */
    XId getRepositoryId();
}
