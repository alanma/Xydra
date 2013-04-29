package org.xydra.core.model.impl.memory;

import java.util.Iterator;
import java.util.Map.Entry;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XSyncLog;
import org.xydra.index.iterator.NoneIterator;


/**
 * Implementation of {@link XSyncLog} methods that don't vary between
 * implementations.
 * 
 * @author Andi K
 */
abstract public class AbstractSyncLog implements XSyncLog {
	
	class CommandEventPairIterator implements Iterator<Entry<XCommand,XEvent>> {
		
		private final long end;
		private long i;
		private Entry<XCommand,XEvent> next;
		
		public CommandEventPairIterator(long begin, long end) {
			this.i = begin;
			this.end = end;
		}
		
		private void getNext() {
			while(this.i < this.end && this.next == null) {
				this.next = getCommandEventPairAt(this.i);
				this.i++;
			}
		}
		
		@Override
		public boolean hasNext() {
			getNext();
			return this.next != null;
		}
		
		@Override
		public Entry<XCommand,XEvent> next() {
			Entry<XCommand,XEvent> commandEventPair = this.next;
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
	public synchronized Iterator<Entry<XCommand,XEvent>> getCommandEventPairsBetween(
	        long beginRevision, long endRevision) {
		/*
		 * firstRev: the revision number the logged XModel had at the time when
		 * the first event was recorded by the change log
		 */
		
		long firstRev = getSyncRevisionNumber() + 1;
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
			return new NoneIterator<Entry<XCommand,XEvent>>();
		}
		
		long begin = beginRevision < firstRev ? firstRev : beginRevision;
		long end = endRevision > curRev ? curRev + 1 : endRevision;
		
		return new CommandEventPairIterator(begin, end);
	}
	
	@Override
	public Iterator<Entry<XCommand,XEvent>> getCommandEventPairsSince(long revisionNumber) {
		return getCommandEventPairsBetween(revisionNumber, Long.MAX_VALUE);
	}
	
	@Override
	public Iterator<Entry<XCommand,XEvent>> getCommandEventPairsUntil(long revisionNumber) {
		return getCommandEventPairsBetween(0, revisionNumber);
	}
	
}
