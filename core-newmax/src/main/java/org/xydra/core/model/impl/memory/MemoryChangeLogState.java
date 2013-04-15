package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;
import org.xydra.sharedutils.XyAssert;


/**
 * A small, serializable change log without any business logic.
 * 
 * @author xamde
 */
public class MemoryChangeLogState implements XChangeLogState {
    
    private static final long serialVersionUID = 4745987477215964499L;
    
    /** the ID of the model this change log refers to **/
    private XAddress baseAddr;
    
    /** the event list **/
    private List<XEvent> events = new ArrayList<XEvent>();
    
    /**
     * the revision number the model had when this changelog was created.
     **/
    private long baseRevisionNumber = -1;
    
    /**
     * Creates a new MemoryChangeLogState. Make sure to set the
     * {@link #setBaseRevisionNumber(long)}.
     * 
     * @param baseAddr The {@link XAddress} of the entity holding the
     *            MemoryChangeLog which is represented by this
     *            MemoryChangeLogState
     */
    public MemoryChangeLogState(XAddress baseAddr) {
        this.baseAddr = baseAddr;
    }
    
    @Override
    public void appendEvent(XEvent event) {
        if(event == null) {
            this.events.add(null);
        } else {
            XyAssert.xyAssert(this.baseAddr.equalsOrContains(event.getChangedEntity()));
            XyAssert.xyAssert(event.getRevisionNumber() == getCurrentRevisionNumber() + 1);
            XyAssert.xyAssert(!event.inTransaction());
            
            this.events.add(event);
        }
    }
    
    @Override
    public XAddress getBaseAddress() {
        return this.baseAddr;
    }
    
    @Override
    public long getCurrentRevisionNumber() {
        return this.baseRevisionNumber + this.events.size();
    }
    
    @Override
    public XEvent getEvent(long revisionNumber) {
        XEvent event = this.events.get((int)(revisionNumber - this.baseRevisionNumber - 1));
        XyAssert.xyAssert(event == null || event.getRevisionNumber() == revisionNumber);
        return event;
    }
    
    @Override
    public long getBaseRevisionNumber() {
        return this.baseRevisionNumber;
    }
    
    @Override
    public void setBaseRevisionNumber(long rev) {
        if(!this.events.isEmpty()) {
            throw new IllegalStateException(
                    "cannot set start revision number of non-empty change log");
        }
        this.baseRevisionNumber = rev;
    }
    
    @Override
    public String toString() {
        return "change log for " + getBaseAddress() + ": baseRev=" + this.baseRevisionNumber
                + " currentRev=" + getCurrentRevisionNumber() + " events=" + this.events.toString();
    }
    
    @Override
    public boolean truncateToRevision(long revisionNumber) {
        if(revisionNumber > getCurrentRevisionNumber()) {
            return false;
        }
        
        if(revisionNumber < this.baseRevisionNumber) {
            return false;
        }
        
        while(revisionNumber < getCurrentRevisionNumber()) {
            this.events.remove(this.events.size() - 1); // remove last element
        }
        
        XyAssert.xyAssert(revisionNumber == getCurrentRevisionNumber());
        
        return true;
    }
}
