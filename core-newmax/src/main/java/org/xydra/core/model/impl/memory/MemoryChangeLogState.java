package org.xydra.core.model.impl.memory;

import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
    
    /** the event map: revNR -> event **/
    private SortedMap<Long,XEvent> eventMap = new TreeMap<Long,XEvent>();
    
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
    
    private static final Logger log = LoggerFactory.getLogger(MemoryChangeLogState.class);
    
    @Override
    public void appendEvent(XEvent event) {
        if(event == null) {
            // TODO throw exception once all data is migrated; for now: do
            // nothing, just complain a little
            log.warn("Skipping null-event");
        } else {
            XyAssert.xyAssert(this.baseAddr.equalsOrContains(event.getChangedEntity()), "baseAddr="
                    + this.baseAddr + " does not contain " + event.getChangedEntity());
            XyAssert.xyAssert(event.getRevisionNumber() > getCurrentRevisionNumber());
            XyAssert.xyAssert(!event.inTransaction());
            this.eventMap.put(event.getRevisionNumber(), event);
        }
    }
    
    @Override
    public XAddress getBaseAddress() {
        return this.baseAddr;
    }
    
    @Override
    public long getCurrentRevisionNumber() {
        if(this.eventMap.isEmpty()) {
            return this.baseRevisionNumber;
        } else {
            return getEvent(this.eventMap.lastKey()).getRevisionNumber();
        }
    }
    
    @Override
    public XEvent getEvent(long revisionNumber) {
        XEvent event = this.eventMap.get(revisionNumber);
        XyAssert.xyAssert(event == null || event.getRevisionNumber() == revisionNumber, "event="
                + event);
        return event;
    }
    
    @Override
    public long getBaseRevisionNumber() {
        return this.baseRevisionNumber;
    }
    
    @Override
    public void setBaseRevisionNumber(long rev) {
        if(!this.eventMap.isEmpty()) {
            throw new IllegalStateException(
                    "cannot set start revision number of non-empty change log");
        }
        this.baseRevisionNumber = rev;
    }
    
    @Override
    public String toString() {
        return "change log for " + getBaseAddress() + ": baseRev=" + this.baseRevisionNumber
                + " currentRev=" + getCurrentRevisionNumber() + " events="
                + this.eventMap.toString();
    }
    
    @Override
    public boolean truncateToRevision(long revisionNumber) {
        if(revisionNumber > getCurrentRevisionNumber()) {
            return false;
        }
        
        if(revisionNumber < this.baseRevisionNumber) {
            return false;
        }
        
        this.eventMap = this.eventMap.headMap(revisionNumber);
        
        XyAssert.xyAssert(revisionNumber == getCurrentRevisionNumber());
        
        return true;
    }
}
