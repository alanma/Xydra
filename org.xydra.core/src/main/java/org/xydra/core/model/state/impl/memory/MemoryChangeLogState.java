package org.xydra.core.model.state.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;


public class MemoryChangeLogState implements XChangeLogState {
	/** the ID of the model this change log refers to **/
	private XAddress modelAddr;
	
	/**
	 * the revision number the model had while this changelog was created the
	 * event at index i equals the event that occurred at the time of the
	 * revision number which equals this.revisionNumber + this.events.length
	 **/
	private final long revisionNumber;
	
	/** the event list **/
	private List<XEvent> events = new ArrayList<XEvent>();
	
	private static final long serialVersionUID = 4745987477215964499L;
	
	public MemoryChangeLogState(XAddress modelAddr, long revisionNumber) {
		this.modelAddr = modelAddr;
		this.revisionNumber = revisionNumber;
	}
	
	public void save() {
		// TODO implement
		
	}
	
	public void delete() {
		// TODO implement
		
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
	
	public void appendEvent(XEvent event) {
		assert event.getModelRevisionNumber() == getCurrentRevisionNumber();
		
		this.events.add(event);
	}
	
	public XAddress getModelAddress() {
		return this.modelAddr;
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
