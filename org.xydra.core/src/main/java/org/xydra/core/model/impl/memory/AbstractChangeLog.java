package org.xydra.core.model.impl.memory;

import java.util.Iterator;

import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.index.iterator.NoneIterator;


/**
 * Implementation of {@link XChangeLog} methods that don't vary between
 * implementations.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractChangeLog implements XChangeLog {
	
	class EventIterator implements Iterator<XEvent> {
		
		private final long end;
		private long i;
		private XEvent next;
		
		public EventIterator(long begin, long end) {
			this.i = begin;
			this.end = end;
		}
		
		private void getNext() {
			while(this.i < this.end && this.next == null) {
				this.next = getEventAt(this.i);
				this.i++;
			}
		}
		
		@Override
		public boolean hasNext() {
			getNext();
			return this.next != null;
		}
		
		@Override
		public XEvent next() {
			XEvent event = this.next;
			this.next = null;
			getNext();
			return event;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static final long serialVersionUID = -1916889722140082523L;
	
	@Override
	public synchronized Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		/*
		 * firstRev: the revision number the logged XModel had at the time when
		 * the first event was recorded by the change log
		 */
		
		long firstRev = getBaseRevisionNumber() + 1;
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
			return new NoneIterator<XEvent>();
		}
		
		long begin = beginRevision < firstRev ? firstRev : beginRevision;
		long end = endRevision > curRev ? curRev + 1 : endRevision;
		
		return new EventIterator(begin, end);
	}
	
	@Override
	public Iterator<XEvent> getEventsSince(long revisionNumber) {
		return getEventsBetween(revisionNumber, Long.MAX_VALUE);
	}
	
	@Override
	public Iterator<XEvent> getEventsUntil(long revisionNumber) {
		return getEventsBetween(0, revisionNumber);
	}
	
}
