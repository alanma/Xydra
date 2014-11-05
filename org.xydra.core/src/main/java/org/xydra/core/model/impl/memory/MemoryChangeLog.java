package org.xydra.core.model.impl.memory;

import org.xydra.annotations.CanBeNull;
import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.impl.memory.sync.XWritableChangeLog;
import org.xydra.sharedutils.XyAssert;


/**
 * Implementation of {@link XChangeLog} for use with {@link MemoryModel},
 * {@link MemoryObject} and {@link MemoryField}.
 * 
 * @author kaidel
 * @author xamde
 */
public class MemoryChangeLog extends AbstractChangeLog implements XWritableChangeLog {
    
    private static final long serialVersionUID = -3242936915355886858L;
    
    /** serialisable state */
    private XChangeLogState state;
    
    /**
     * @param state @NeverNull
     */
    MemoryChangeLog(XChangeLogState state) {
        if(state == null)
            throw new IllegalArgumentException("state may not be null");
        this.state = state;
    }
    
    @Override
	public synchronized void appendEvent(@CanBeNull XEvent event) {
        XyAssert.xyAssert(event == null || !event.inTransaction());
        /*
         * "else": event is part of a transaction and will therefore only be
         * recorded as part of the transaction
         */
        this.state.appendEvent(event);
    }
    
    @Override
    public XAddress getBaseAddress() {
        return this.state.getBaseAddress();
    }
    
    @Override
    synchronized public long getCurrentRevisionNumber() {
        return this.state.getCurrentRevisionNumber();
    }
    
    @Override
    synchronized public XEvent getEventAt(long revisionNumber) {
        if(revisionNumber < 0) {
            throw new IllegalArgumentException("revisionNumber may not be less than zero: "
                    + revisionNumber);
        }
        
        if(revisionNumber <= getBaseRevisionNumber()) {
            throw new IndexOutOfBoundsException("revisionNumber (" + revisionNumber
                    + ") may not be <= than the first revision number of this log ("
                    + getBaseRevisionNumber() + ")");
        }
        
        if(revisionNumber > this.state.getCurrentRevisionNumber()) {
            throw new IndexOutOfBoundsException(
                    "revisionNumber may not be greater than or equal to the current revision"
                            + "number of this log");
        }
        XEvent event = this.state.getEvent(revisionNumber);
        XyAssert.xyAssert(event == null || event.getRevisionNumber() == revisionNumber);
        return event;
    }
    
    @Override
    synchronized public long getBaseRevisionNumber() {
        return this.state.getBaseRevisionNumber();
    }
    
    @Override
    synchronized public String toString() {
        return this.state.toString();
    }
    
    @Override
	public synchronized boolean truncateToRevision(long revisionNumber) {
        return this.state.truncateToRevision(revisionNumber);
    }
    
    @Override
    public XChangeLogState getChangeLogState() {
        return this.state;
    }
    
    public static XWritableChangeLog create(XAddress baseAddress, long changeLogBaseRevision) {
        assert baseAddress.getRepository() != null;
        MemoryChangeLogState changeLogState = new MemoryChangeLogState(baseAddress);
        changeLogState.setBaseRevisionNumber(changeLogBaseRevision);
        return new MemoryChangeLog(changeLogState);
    }
    
    @Override
    public XEvent getLastEvent() {
        return this.state.getLastEvent();
    }
    
}
