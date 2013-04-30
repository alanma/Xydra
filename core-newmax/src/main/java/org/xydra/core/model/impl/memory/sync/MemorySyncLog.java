package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;

import com.google.gwt.dev.jjs.impl.gflow.Analysis;


/**
 * TODO andi implement
 * 
 * @author xamde
 */
public class MemorySyncLog extends AbstractSyncLog implements ISyncLog {
    
    private static final long serialVersionUID = -7008700708157729184L;
    
    /** serialisable state */
    private ISyncLogState state;
    
    /**
     * @param state @NeverNull
     */
    public MemorySyncLog(ISyncLogState state) {
        if(state == null)
            throw new IllegalArgumentException("state may not be null");
        this.state = state;
    }
    
    /**
     * Wrap an existing {@link XChangeLogState} into {@link Analysis}
     * {@link ISyncLog}
     * 
     * @param changeLogState
     */
    public MemorySyncLog(XChangeLogState changeLogState) {
        this(new MemorySyncLogState(changeLogState));
    }
    
    public MemorySyncLog(XAddress modelAddress) {
        this(new MemorySyncLogState(modelAddress));
    }
    
    // TODO handling of events in transactions???
    @Override
    public synchronized void appendSyncLogEntry(ISyncLogEntry syncLogEntry) {
        // TODO what to assert? XyAssert.xyAssert((event == null && command ==
        // null) || !event.inTransaction());
        /*
         * "else": event is part of a transaction and will therefore only be
         * recorded as part of the transaction
         */
        this.state.appendSyncLogEntry(syncLogEntry);
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
    synchronized public ISyncLogEntry getSyncLogEntryAt(long revisionNumber) {
        if(revisionNumber < 0) {
            throw new IllegalArgumentException("revisionNumber may not be less than zero: "
                    + revisionNumber);
        }
        
        if(revisionNumber <= getSynchronizedRevision()) {
            throw new IndexOutOfBoundsException("revisionNumber (" + revisionNumber
                    + ") may not be <= than the first revision number of this log ("
                    + getSynchronizedRevision() + ")");
        }
        
        if(revisionNumber > this.state.getCurrentRevisionNumber()) {
            throw new IndexOutOfBoundsException(
                    "revisionNumber may not be greater than or equal to the current revision"
                            + "number of this log");
        }
        ISyncLogEntry syncLogEntry = this.state.getSyncLogEntry(revisionNumber);
        // TODO what to assert? || syncLogEntry.getValue().getRevisionNumber()
        // == revisionNumber);
        return syncLogEntry;
    }
    
    @Override
    synchronized public long getSynchronizedRevision() {
        return this.state.getSyncRevisionNumber();
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
    public ISyncLogState getSyncLogState() {
        return this.state;
    }
    
    public static ISyncLog create(XAddress baseAddress, long syncRevision) {
        assert baseAddress.getRepository() != null;
        MemorySyncLogState syncLogState = new MemorySyncLogState(baseAddress);
        syncLogState.setSyncRevisionNumber(syncRevision);
        return new MemorySyncLog(syncLogState);
    }
    
    @Override
    public XEvent getEventAt(long revisionNumber) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Iterator<XEvent> getEventsSince(long revisionNumber) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Iterator<XEvent> getEventsUntil(long revisionNumber) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public long getBaseRevisionNumber() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public XChangeLogState getChangeLogState() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public XEvent getLastEvent() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int countUnappliedLocalChanges() {
        // TODO return number of local commands since syncRev
        return -666;
    }
    
    @Override
    public void appendSyncLogEntry(XCommand command, XEvent event) {
        ISyncLogEntry syncLogEntry = new MemorySyncLogEntry(command, event);
        appendSyncLogEntry(syncLogEntry);
    }
    
    @Override
    public void setSynchronizedRevision(long syncronizedRevision) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void appendEvent(XEvent event) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Iterator<ISyncLogEntry> getLocalChanges() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void clearLocalChanges() {
        // TODO Auto-generated method stub
        
    }
    
}
