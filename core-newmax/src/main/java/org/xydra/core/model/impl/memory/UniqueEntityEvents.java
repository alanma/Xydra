package org.xydra.core.model.impl.memory;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.change.XEvent;
import org.xydra.index.impl.MapMapIndex;


/**
 * Attempt 2 to create an alternative to {@link EventDelta}
 * 
 * @author xamde
 */
@Deprecated
public class UniqueEntityEvents {
    
    /* EntityType -> ChangedEntityAddress -> Event */
    private MapMapIndex<XType,XAddress,XEvent> index = new MapMapIndex<XType,XAddress,XEvent>();
    
    /**
     * Add an event, overwriting any potentially existing event with the same
     * {@link XEvent#getChangedEntity()}
     * 
     * @param event
     */
    public void putEvent(XEvent event) {
        XAddress address = event.getChangedEntity();
        this.index.index(address.getAddressedType(), address, event);
    }
    
    /**
     * all last local events that have no corresponding last remote event, need
     * to be added, in inverse order
     * 
     * @param local
     * @param remote
     * @return merged
     */
    public static UniqueEntityEvents merge(UniqueEntityEvents local, UniqueEntityEvents remote) {
        Iterator<XEvent> it = local.iteratorOverAllEvents();
        
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * @return an iterator with unknown ordering
     */
    private Iterator<XEvent> iteratorOverAllEvents() {
        return this.index.entryIterator();
    }
}
