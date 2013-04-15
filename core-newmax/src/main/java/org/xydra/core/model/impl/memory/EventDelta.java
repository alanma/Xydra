package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.index.IMapMapSetIndex;


/**
 * Different from a {@link ChangedModel}, this class e.g., can keep fields even
 * if its object is removed.
 * 
 * Revision numbers of incoming events are used.
 * 
 * @author xamde
 * 
 *         IMPROVE Do same for objects?
 */
public class EventDelta {
    
    /**
     * Map: objectID -> Map: fieldId -> Set<events>
     */
    // TODO ....
    IMapMapSetIndex<?,?,?> i;
    
    /**
     * Add event to internal state and maintain a redundancy-free state
     * 
     * @param event
     */
    public void addEvent(XEvent event) {
    }
    
    /**
     * Add the inverse of the given event to the internal state.
     * 
     * TODO use existing utility for calculating the inverse event
     * 
     * @param event
     */
    public void addInverseEvent(XEvent event) {
        
    }
    
    /**
     * Perform few plausibility-checks, just apply changes.
     * 
     * @param model
     */
    public void applyTo(XRevWritableModel model) {
        
    }
    
    /**
     * Send first removeFields, removeObject, removeModel, addModel, addObject,
     * addField, changeField.
     * 
     * No txn events are sent.
     * 
     * @param root for sending events; if better, {@link MemoryEventBus} could
     *            also be used
     * @param model
     */
    public void sendChangeEvents(Root root, XModel model) {
        
    }
    
}
