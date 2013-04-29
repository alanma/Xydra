package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;

import org.xydra.index.iterator.NoneIterator;


/**
 * Implementation of {@link ISyncLog} methods that don't vary between
 * implementations.
 * 
 * @author Andi K
 */
abstract public class AbstractSyncLog implements ISyncLog {
    
    class CommandEventPairIterator implements Iterator<ISyncLogEntry> {
        
        private final long end;
        private long i;
        private ISyncLogEntry next;
        
        public CommandEventPairIterator(long begin, long end) {
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
            ISyncLogEntry commandEventPair = this.next;
            this.next = null;
            getNext();
            return commandEventPair;
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
        
        return new CommandEventPairIterator(begin, end);
    }
    
    @Override
    public Iterator<ISyncLogEntry> getSyncLogEntriesSince(long revisionNumber) {
        return getSyncLogEntriesBetween(revisionNumber, Long.MAX_VALUE);
    }
    
    @Override
    public Iterator<ISyncLogEntry> getSyncLogEntriesUntil(long revisionNumber) {
        return getSyncLogEntriesBetween(0, revisionNumber);
    }
    
}
