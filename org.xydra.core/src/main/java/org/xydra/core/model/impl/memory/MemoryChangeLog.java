package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.state.XChangeLogState;


/**
 * Implementation of {@link XChangeLog} for use in MemoryModel, MemoryObject and
 * MemoryField.
 * 
 * @author Kaidel
 * 
 */
public class MemoryChangeLog implements XChangeLog {
	
	private XChangeLogState state;
	
	public MemoryChangeLog(XChangeLogState state) {
		
		assert state != null;
		
		this.state = state;
		
	}
	
	/**
	 * Appends an event to the end of the change log
	 * 
	 * @param event the event which is to be appended
	 */
	protected void appendEvent(XEvent event) {
		assert event.getModelRevisionNumber() == this.getCurrentRevisionNumber();
		
		assert !event.inTransaction();
		// "else": event is part of a transaction and will therefore only be
		// recorded as part of the transaction (by using a
		// ChangeLogTransactionListener)
		
		this.state.appendEvent(event);
	}
	
	/**
	 * Removes all events that occurred after the given revision number,
	 * excluding the event that occurred at the given revision number
	 * 
	 * @param revisionNumber
	 * @return true, if the operation could be executed, i.e. the given revision
	 *         number was smaller than the current revision number and greater
	 *         than zero.
	 */
	protected boolean truncateToRevision(long revisionNumber) {
		return this.state.truncateToRevision(revisionNumber);
	}
	
	public XAddress getModelAddress() {
		return this.state.getModelAddress();
	}
	
	public List<XEvent> getAllEventsAfter(long revisionNumber) {
		if(revisionNumber < 0) {
			throw new IllegalArgumentException(
			        "MemoryChangeLog: revisionNumber may not be less than zero");
		}
		
		if(revisionNumber > this.state.getCurrentRevisionNumber()) {
			throw new IllegalArgumentException(
			        "MemoryChangeLog: revisionNumber may not greater than the current revision number of this log");
			// TODO Exception or return null?
		}
		
		ArrayList<XEvent> list = new ArrayList<XEvent>();
		
		for(long i = revisionNumber; i < this.state.getCurrentRevisionNumber(); i++) {
			list.add(this.state.getEvent(i));
		}
		
		return list;
	}
	
	public List<XEvent> getAllEventsUntil(long revisionNumber) {
		if(revisionNumber < 0) {
			throw new IllegalArgumentException(
			        "MemoryChangeLog: revisionNumber may not be less than zero");
		}
		
		if(revisionNumber > this.state.getCurrentRevisionNumber()) {
			throw new IllegalArgumentException(
			        "MemoryChangeLog: revisionNumber may not greater than the current revision number of this log");
			// TODO Exception or return null?
		}
		
		ArrayList<XEvent> list = new ArrayList<XEvent>();
		
		for(long i = 0; i < revisionNumber; i++) {
			list.add(this.state.getEvent(i));
		}
		
		return list;
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
	
	public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		
		long firstRev = getFirstRevisionNumber();
		long curRev = getCurrentRevisionNumber();
		
		if(beginRevision > curRev) {
			throw new IndexOutOfBoundsException(
			        "beginRevision may not be greater than the current revision, was "
			                + beginRevision);
		}
		
		if(endRevision < firstRev) {
			throw new IndexOutOfBoundsException(
			        "endRevision may not be less than the first revision, was " + endRevision);
		}
		
		if(beginRevision > endRevision) {
			throw new IndexOutOfBoundsException("beginRevision may not be greater than endRevision");
		}
		
		long begin = beginRevision < firstRev ? firstRev : beginRevision;
		long end = endRevision > curRev ? curRev : endRevision;
		
		return new EventIterator(begin, end);
	}
	
	public XEvent getEventAt(long revisionNumber) {
		if(revisionNumber < 0) {
			throw new IllegalArgumentException(
			        "MemoryChangeLog: revisionNumber may not be less than zero");
		}
		
		if(revisionNumber < getFirstRevisionNumber()
		        || revisionNumber >= this.state.getCurrentRevisionNumber()) {
			return null;
		}
		return this.state.getEvent(revisionNumber);
	}
	
	public long getCurrentRevisionNumber() {
		return this.state.getCurrentRevisionNumber();
	}
	
	public long getFirstRevisionNumber() {
		return this.state.getFirstRevisionNumber();
	}
	
}
