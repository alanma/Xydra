package org.xydra.base.change;

import org.xydra.base.XId;
import org.xydra.base.value.XValue;


/**
 * An {@link XEvent} representing changes of fields
 * 
 * @author Kaidel
 */
public interface XFieldEvent extends XAtomicEvent {
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the changed field
     */
    XId getFieldId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the Parent-model of the field where the change
     *         happened. It may be null.
     */
    XId getModelId();
    
    /**
     * HOW has the {@link XValue} of the field been changed?
     * 
     * To get the old value, use
     * {@link org.xydra.core.model.XModel#getChangeLog()} and get the event that
     * has the revision number of {@link #getOldFieldRevision()}.
     * 
     * @return the new {@link XValue}. null indicates a remove event.
     */
    XValue getNewValue();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the Parent-object of the field where the
     *         change happened. It may be null.
     */
    XId getObjectId();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XId} of the Parent-repository of the field where the
     *         change happened. It may be null.
     */
    XId getRepositoryId();
    
}
