package org.xydra.core.model.impl.memory.sync;

import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.impl.memory.MemoryChangeLogState;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


public class MemorySyncLogState implements ISyncLogState {
    
    private static final Logger log = LoggerFactory.getLogger(MemoryChangeLogState.class);
    
    private static final long serialVersionUID = 2293394396146852612L;
    
    /** the ID of the model this change log refers to **/
    private XAddress baseAddr;
    
    /** the event map: revNR -> tuple<transaction,event> **/
    private SortedMap<Long,ISyncLogEntry> eventMap = new TreeMap<Long,ISyncLogEntry>();
    
    /**
     * the revision number the model had when this synclog was created.
     **/
    private long syncRevisionNumber = -1;
    
    /**
     * Creates a new MemorySyncLogState. Make sure to set the
     * {@link #setSyncRevisionNumber(long)}.
     * 
     * @param baseAddr The {@link XAddress} of the entity holding the
     *            MemorySyncLog which is represented by this MemorySyncLogState
     */
    public MemorySyncLogState(XAddress baseAddr) {
        this.baseAddr = baseAddr;
    }
    
    /**
     * @param changeLogState
     */
    public MemorySyncLogState(XChangeLogState changeLogState) {
        // TODO Auto-generated constructor stub
        // TODO this should be possible
    }
    
    @Override
    public void appendEvent(XEvent event) {
        MemorySyncLogEntry syncLogEntry = new MemorySyncLogEntry(null, event);
        appendSyncLogEntry(syncLogEntry);
    }
    
    @Override
    public void appendSyncLogEntry(ISyncLogEntry syncLogEntry) {
        if(syncLogEntry == null || syncLogEntry.getEvent() == null) {
            // TODO throw exception once all data is migrated; for now: do
            // nothing, just complain a little
            log.warn("Skipping null-event");
        } else {
            XEvent event = syncLogEntry.getEvent();
            XyAssert.xyAssert(this.baseAddr.equalsOrContains(event.getChangedEntity()), "baseAddr="
                    + this.baseAddr + " does not contain " + event.getChangedEntity());
            XyAssert.xyAssert(event.getRevisionNumber() > getCurrentRevisionNumber());
            XyAssert.xyAssert(!event.inTransaction());
            this.eventMap.put(event.getRevisionNumber(), syncLogEntry);
        }
    }
    
    @Override
    public XAddress getBaseAddress() {
        return this.baseAddr;
    }
    
    @Override
    public long getBaseRevisionNumber() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public long getCurrentRevisionNumber() {
        if(this.eventMap.isEmpty()) {
            return this.syncRevisionNumber;
        } else {
            return getLastEvent().getRevisionNumber();
        }
        
    }
    
    @Override
    public XEvent getEvent(long revisionNumber) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public XEvent getLastEvent() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ISyncLogEntry getSyncLogEntry(long revisionNumber) {
        ISyncLogEntry syncLogEntry = this.eventMap.get(revisionNumber);
        XyAssert.xyAssert(syncLogEntry.getEvent() == null
                || syncLogEntry.getEvent().getRevisionNumber() == revisionNumber, "event="
                + syncLogEntry.getEvent());
        return syncLogEntry;
    }
    
    @Override
    public long getSyncRevisionNumber() {
        return this.syncRevisionNumber;
    }
    
    @Override
    public void setBaseRevisionNumber(long rev) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void setSyncRevisionNumber(long rev) {
        if(!this.eventMap.isEmpty()) {
            throw new IllegalStateException(
                    "cannot set start revision number of non-empty change log");
        }
        this.syncRevisionNumber = rev;
        
    }
    
    @Override
    public String toString() {
        return "sync log for " + getBaseAddress() + ": baseRev=" + this.syncRevisionNumber
                + " currentRev=" + getCurrentRevisionNumber() + " events="
                + this.eventMap.toString();
    }
    
    @Override
    public boolean truncateToRevision(long revisionNumber) {
        if(revisionNumber > getCurrentRevisionNumber()) {
            return false;
        }
        
        if(revisionNumber < this.syncRevisionNumber) {
            return false;
        }
        
        this.eventMap = this.eventMap.headMap(revisionNumber);
        
        XyAssert.xyAssert(revisionNumber == getCurrentRevisionNumber());
        
        return true;
    }
    
}
