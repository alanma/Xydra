package org.xydra.core.model.state.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XStateTransaction;


public class MemoryChangeLogState implements XChangeLogState {
	
	/** the ID of the model this change log refers to **/
	private XAddress baseAddr;
	
	/**
	 * the revision number the model had while this changelog was created the
	 * event at index i equals the event that occurred at the time of the
	 * revision number which equals this.revisionNumber + this.events.length
	 **/
	private long revisionNumber = 0L;
	
	/** the event list **/
	private List<XEvent> events = new ArrayList<XEvent>();
	
	private static final long serialVersionUID = 4745987477215964499L;
	
	/**
	 * Creates a new MemoryChangeLogState.
	 * 
	 * @param baseAddr The {@link XAddress} of the entity holding the
	 *            MemoryChangeLog which is represented by this
	 *            MemoryChangeLogState
	 * @param revisionNumber The current revision number of the entity holding
	 *            the MemoryChangeLog which is represented by this
	 *            MemoryChangeLogState
	 */
	public MemoryChangeLogState(XAddress baseAddr) {
		this.baseAddr = baseAddr;
	}
	
	public void save(XStateTransaction transaction) {
		// memory change log cannot be saved, ignore
	}
	
	public void delete(XStateTransaction transaction) {
		// automatically deleted by garbage collector
	}
	
	public long getCurrentRevisionNumber() {
		return this.revisionNumber + this.events.size();
	}
	
	public XEvent getEvent(long revisionNumber) {
		return this.events.get((int)(revisionNumber - getFirstRevisionNumber()));
	}
	
	public long getFirstRevisionNumber() {
		return this.revisionNumber;
	}
	
	long getRevisionForEvent(XEvent event) {
		if(event == null) {
			return getCurrentRevisionNumber();
		}
		long rev = this.baseAddr.getObject() == null ? event.getModelRevisionNumber() : event
		        .getObjectRevisionNumber();
		assert rev >= 0;
		return rev;
	}
	
	public void appendEvent(XEvent event, XStateTransaction transaction) {
		
		assert getRevisionForEvent(event) == getCurrentRevisionNumber();
		
		this.events.add(event);
	}
	
	public XAddress getBaseAddress() {
		return this.baseAddr;
	}
	
	public boolean truncateToRevision(long revisionNumber, XStateTransaction transaction) {
		if(revisionNumber > getCurrentRevisionNumber()) {
			return false;
		}
		
		if(revisionNumber < this.revisionNumber) {
			return false;
		}
		
		while(revisionNumber < getCurrentRevisionNumber()) {
			this.events.remove(this.events.size() - 1); // remove last element
		}
		
		assert revisionNumber == getCurrentRevisionNumber();
		
		return true;
	}
	
	public void setFirstRevisionNumber(long rev) {
		if(!this.events.isEmpty()) {
			throw new IllegalStateException(
			        "cannot set start revision number of non-empty change log");
		}
		this.revisionNumber = rev;
	}
	
	@Override
	public String toString() {
		return "change log for " + getBaseAddress() + ": baseRev=" + getFirstRevisionNumber()
		        + " currentRev=" + getCurrentRevisionNumber() + " events=" + this.events.toString();
	}
}
