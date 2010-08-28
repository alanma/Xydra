package org.xydra.core.model.impl.memory;

import java.util.Iterator;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.index.iterator.NoneIterator;


/**
 * Implementation of {@link XChangeLog} for use with {@link MemoryModel},
 * {@link MemoryObject} and {@link MemoryField}.
 * 
 * @author Kaidel
 * 
 */
public class MemoryChangeLog implements XChangeLog {
	
	private static final long serialVersionUID = -3242936915355886858L;
	
	private XChangeLogState state;
	
	public MemoryChangeLog(XChangeLogState state) {
		
		assert state != null;
		
		this.state = state;
		
	}
	
	/**
	 * Appends an {@link XEvent} to the end of this MemoryChangeLog
	 * 
	 * @param event the {@link XEvent} which is to be appended
	 */
	protected void appendEvent(XEvent event, Object transaction) {
		assert this.state.getBaseAddress().getObject() != null || event != null : "cannot add null events to model change log";
		assert this.state.getBaseAddress().getObject() != null
		        || (event.getModelRevisionNumber() == getCurrentRevisionNumber()) : "cannot append event with rev "
		        + event.getModelRevisionNumber()
		        + " to model change log at event "
		        + getCurrentRevisionNumber();
		
		assert event == null || !event.inTransaction();
		// "else": event is part of a transaction and will therefore only be
		// recorded as part of the transaction (by using a
		// ChangeLogTransactionListener)
		
		this.state.appendEvent(event, transaction);
	}
	
	/**
	 * Removes all {@link XEvent XEvents} that occurred after the given revision
	 * number from this MemoryChangeLog, excluding the {@link XEvent} that
	 * occurred at the given revision number
	 * 
	 * @param revisionNumber the revision number from which on the
	 *            {@link XEvent XEvents} are to be removed
	 * @return true, if the operation could be executed, i.e. the given revision
	 *         number was smaller than the current revision number and greater
	 *         than zero.
	 */
	protected boolean truncateToRevision(long revisionNumber, Object transaction) {
		return this.state.truncateToRevision(revisionNumber, transaction);
	}
	
	public XAddress getBaseAddress() {
		return this.state.getBaseAddress();
	}
	
	private class EventIterator implements Iterator<XEvent> {
		
		private final long end;
		private long i;
		private XEvent next;
		
		public EventIterator(long begin, long end) {
			this.i = begin;
			this.end = end;
		}
		
		public boolean hasNext() {
			getNext();
			return this.next != null;
		}
		
		public XEvent next() {
			XEvent event = this.next;
			this.next = null;
			getNext();
			return event;
		}
		
		private void getNext() {
			while(this.i < this.end && this.next == null) {
				this.next = getEventAt(this.i);
				this.i++;
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	synchronized public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		
		long firstRev = getFirstRevisionNumber();
		long curRev = getCurrentRevisionNumber();
		
		if(beginRevision < 0) {
			throw new IndexOutOfBoundsException(
			        "beginRevision is not a valid revision number, was " + beginRevision);
		}
		
		if(endRevision < 0) {
			throw new IndexOutOfBoundsException(
			        "beginRevision is not a valid revision number, was " + beginRevision);
		}
		
		if(beginRevision > endRevision) {
			throw new IllegalArgumentException("beginRevision may not be greater than endRevision");
		}
		
		if(beginRevision >= endRevision || endRevision < firstRev) {
			return new NoneIterator<XEvent>();
		}
		
		long begin = beginRevision < firstRev ? firstRev : beginRevision;
		long end = endRevision > curRev ? curRev : endRevision;
		
		return new EventIterator(begin, end);
	}
	
	synchronized public XEvent getEventAt(long revisionNumber) {
		if(revisionNumber < 0) {
			throw new IllegalArgumentException("revisionNumber may not be less than zero");
		}
		
		if(revisionNumber < getFirstRevisionNumber()) {
			throw new IndexOutOfBoundsException(
			        "revisionNumber may not be less than the first revision" + "number of this log");
		}
		
		if(revisionNumber >= this.state.getCurrentRevisionNumber()) {
			throw new IndexOutOfBoundsException(
			        "revisionNumber may not be greater than or equal to the current revision"
			                + "number of this log");
		}
		return this.state.getEvent(revisionNumber);
	}
	
	synchronized public long getCurrentRevisionNumber() {
		return this.state.getCurrentRevisionNumber();
	}
	
	synchronized public long getFirstRevisionNumber() {
		return this.state.getFirstRevisionNumber();
	}
	
	public Iterator<XEvent> getEventsAfter(long revisionNumber) {
		return getEventsBetween(revisionNumber, Long.MAX_VALUE);
	}
	
	public Iterator<XEvent> getEventsUntil(long revisionNumber) {
		return getEventsBetween(0, revisionNumber);
	}
	
	protected void save(Object transaction) {
		this.state.save(transaction);
	}
	
	protected void delete(Object transaction) {
		this.state.delete(transaction);
	}
	
}
