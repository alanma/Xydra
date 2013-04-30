package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;

import org.xydra.base.change.XEvent;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;


/**
 * Implementation of {@link ISyncLog} methods that don't vary between
 * implementations.
 * 
 * @author Andi K
 */
abstract public class AbstractSyncLog implements ISyncLog {
    
    class SyncLogEntryIterator implements Iterator<ISyncLogEntry> {
        
        private final long end;
        
        private long i;
        
        private ISyncLogEntry next;
        
        public SyncLogEntryIterator(long begin, long end) {
            this.i = begin;
            this.end = end;
        }
        
        private void getNext() {
            while(this.i < this.end && this.next == null) {
                this.next = getSyncLogEntryAt(this.i);
                this.i++;
            }
        }
        
        @Override
        public boolean hasNext() {
            getNext();
            return this.next != null;
        }
        
        @Override
        public ISyncLogEntry next() {
            ISyncLogEntry syncLogEntry = this.next;
            this.next = null;
            getNext();
            return syncLogEntry;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static final long serialVersionUID = -1916889722140082523L;
    
    @Override
    public synchronized Iterator<ISyncLogEntry> getSyncLogEntriesBetween(long beginRevision,
            long endRevision) {
        /*
         * firstRev: the revision number the logged XModel had at the time when
         * the first event was recorded by the change log
         */
        
        long firstRev = getSynchronizedRevision() + 1;
        long curRev = getCurrentRevisionNumber();
        
        if(beginRevision < 0) {
            throw new IndexOutOfBoundsException(
                    "beginRevision is not a valid revision number, was " + beginRevision);
        }
        
        if(endRevision < 0) {
            throw new IndexOutOfBoundsException("endRevision is not a valid revision number, was "
                    + endRevision);
        }
        
        if(beginRevision > endRevision) {
            throw new IllegalArgumentException("beginRevision may not be greater than endRevision");
        }
        
        if(beginRevision >= endRevision || endRevision <= firstRev) {
            return new NoneIterator<ISyncLogEntry>();
        }
        
        long begin = beginRevision < firstRev ? firstRev : beginRevision;
        long end = endRevision > curRev ? curRev + 1 : endRevision;
        
        return new SyncLogEntryIterator(begin, end);
    }
    
    @Override
    public Iterator<ISyncLogEntry> getSyncLogEntriesSince(long revisionNumber) {
        return getSyncLogEntriesBetween(revisionNumber, Long.MAX_VALUE);
    }
    
    @Override
    public Iterator<ISyncLogEntry> getSyncLogEntriesUntil(long revisionNumber) {
        return getSyncLogEntriesBetween(0, revisionNumber);
    }
    
    class ExtractEventTransformer extends AbstractTransformingIterator<ISyncLogEntry,XEvent> {
        
        public ExtractEventTransformer(Iterator<? extends ISyncLogEntry> base) {
            super(base);
        }
        
        @Override
        public XEvent transform(ISyncLogEntry in) {
            if(in == null)
                return null;
            return in.getEvent();
        }
        
    }
    
    @Override
    public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
        return new ExtractEventTransformer(getSyncLogEntriesBetween(beginRevision, endRevision));
    }
    
    @Override
    public Iterator<XEvent> getEventsSince(long revisionNumber) {
        return new ExtractEventTransformer(getSyncLogEntriesSince(revisionNumber));
    }
    
    @Override
    public Iterator<XEvent> getEventsUntil(long revisionNumber) {
        return new ExtractEventTransformer(getSyncLogEntriesUntil(revisionNumber));
    }
    
}
