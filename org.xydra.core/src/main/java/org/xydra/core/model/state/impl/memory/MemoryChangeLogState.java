package org.xydra.core.model.state.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;


public class MemoryChangeLogState implements XChangeLogState {
	
	/** the ID of the model this change log refers to **/
	private XAddress baseAddr;
	
	/**
	 * the revision number the model had while this changelog was created the
	 * event at index i equals the event that occurred at the time of the
	 * revision number which equals this.revisionNumber + this.events.length
	 **/
	private final long revisionNumber;
	
	/** the event list **/
	private List<XEvent> events = new ArrayList<XEvent>();
	
	private static final long serialVersionUID = 4745987477215964499L;
	
	public MemoryChangeLogState(XAddress baseAddr, long revisionNumber) {
		this.baseAddr = baseAddr;
		this.revisionNumber = revisionNumber;
	}
	
	public void save() {
		// memory change log cannot be saved, ignore
	}
	
	public void delete() {
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
		long rev = this.baseAddr.getObject() == null ? event.getModelRevisionNumber() : event
		        .getObjectRevisionNumber();
		assert rev >= 0;
		return rev;
	}
	
	public void appendEvent(XEvent event) {
		
		if(this.baseAddr.getObject() != null) {
			long rev = getRevisionForEvent(event);
			while(getCurrentRevisionNumber() < rev) {
				this.events.add(null);
			}
		} else {
			assert getRevisionForEvent(event) == getCurrentRevisionNumber();
		}
		
		this.events.add(event);
	}
	
	public XAddress getBaseAddress() {
		return this.baseAddr;
	}
	
	public boolean truncateToRevision(long revisionNumber) {
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
	
}
