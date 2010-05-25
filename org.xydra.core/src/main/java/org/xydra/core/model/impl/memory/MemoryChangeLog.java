package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;



/**
 * Implementation of {@link XChangeLog}.
 * 
 * @author Kaidel
 * 
 */

public class MemoryChangeLog implements XChangeLog {
	private XChangeLogState state;
	
	private ChangeLogEventListener eventListener;
	private ChangeLogTransactionListener transactionListener;
	
	public MemoryChangeLog(MemoryModel model) {
		this(model, new MemoryChangeLogState(model.getAddress(), model.getRevisionNumber()));
	}
	
	public MemoryChangeLog(MemoryModel model, XChangeLogState state) {
		
		if(!model.getAddress().equals(state.getModelAddress())) {
			throw new RuntimeException("MemoryChangeLog: Given Model doesn't fit to given state");
		}
		
		this.state = state;
		
		this.eventListener = new ChangeLogEventListener();
		this.transactionListener = new ChangeLogTransactionListener();
		
		startLogging(model);
	}
	
	protected void stopLogging(MemoryModel model) {
		model.removeListenerForFieldEvents(this.eventListener);
		model.removeListenerForModelEvents(this.eventListener);
		model.removeListenerForObjectEvents(this.eventListener);
		model.removeListenerForTransactionEvents(this.transactionListener);
	}
	
	protected void startLogging(MemoryModel model) {
		model.addListenerForFieldEvents(this.eventListener);
		model.addListenerForModelEvents(this.eventListener);
		model.addListenerForObjectEvents(this.eventListener);
		model.addListenerForTransactionEvents(this.transactionListener);
	}
	
	/**
	 * Appends an event to the end of the change log
	 * 
	 * @param event the event which is to be appended
	 */
	
	private void appendEvent(XEvent event) {
		assert event.getModelRevisionNumber() == this.getCurrentRevisionNumber();
		
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
	
	/**
	 * Returns the {@link XID} of the {@link MemoryModel} this changelog refers
	 * to.
	 * 
	 * @return the {@link XID} of the {@link MemoryModel} this changelog refers
	 *         to.
	 */
	
	public XAddress getModelAddress() {
		return this.state.getModelAddress();
	}
	
	/*
	 * This class is used for logging Model-, Object- and FieldEvents by the
	 * MemoryChangeLogs
	 */

	private class ChangeLogEventListener implements XModelEventListener, XFieldEventListener,
	        XObjectEventListener {
		
		private void onChangeEvent(XEvent event) {
			if(!event.inTransaction()) {
				appendEvent(event);
			}
			// "else": event is part of a transaction and will therefore only be
			// recorded as part of the transaction (by using a
			// ChangeLogTransactionListener)
		}
		
		public void onChangeEvent(XFieldEvent event) {
			onChangeEvent((XEvent)event);
		}
		
		public void onChangeEvent(XObjectEvent event) {
			onChangeEvent((XEvent)event);
		}
		
		public void onChangeEvent(XModelEvent event) {
			onChangeEvent((XEvent)event);
		}
		
	}
	
	/*
	 * This class is used for TransactionEvents by the MemoryChangeLogs
	 */

	private class ChangeLogTransactionListener implements XTransactionEventListener {
		
		public void onChangeEvent(XTransactionEvent event) {
			appendEvent(event);
		}
		
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
		
		for(long i = revisionNumber; i <= this.state.getCurrentRevisionNumber(); i++) {
			list.add(this.state.getEvent(revisionNumber));
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
		
		for(long i = 0; i <= revisionNumber; i++) {
			list.add(this.state.getEvent(revisionNumber));
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
		
		if(revisionNumber > this.state.getCurrentRevisionNumber()) {
			throw new IllegalArgumentException(
			        "MemoryChangeLog: revisionNumber may not greater than the current revision number of this log");
			// TODO Exception or return null?
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
