package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;
import org.xydra.sharedutils.XyAssert;


// TODO merge this into memory change log?
public class MemoryChangeLogState implements XChangeLogState {
	
	private static final long serialVersionUID = 4745987477215964499L;
	
	/** the ID of the model this change log refers to **/
	private XAddress baseAddr;
	
	/** the event list **/
	private List<XEvent> events = new ArrayList<XEvent>();
	
	/**
	 * the revision number the model had while this changelog was created the
	 * event at index i equals the event that occurred at the time of the
	 * revision number which equals this.revisionNumber + this.events.length
	 **/
	private long revisionNumber = 0L;
	
	/**
	 * Creates a new MemoryChangeLogState.
	 * 
	 * @param baseAddr The {@link XAddress} of the entity holding the
	 *            MemoryChangeLog which is represented by this
	 *            MemoryChangeLogState
	 */
	public MemoryChangeLogState(XAddress baseAddr) {
		this.baseAddr = baseAddr;
	}
	
	@Override
    public void appendEvent(XEvent event) {
		
		if(event == null) {
			this.events.add(null);
		} else {
			
			XyAssert.xyAssert(this.baseAddr.equalsOrContains(event.getChangedEntity()));
			XyAssert.xyAssert(event.getRevisionNumber() == getCurrentRevisionNumber() + 1);
			XyAssert.xyAssert(!event.inTransaction());
			
			this.events.add(event);
		}
	}
	
	@Override
    public XAddress getBaseAddress() {
		return this.baseAddr;
	}
	
	@Override
    public long getCurrentRevisionNumber() {
		return this.revisionNumber + this.events.size() - 1;
	}
	
	@Override
    public XEvent getEvent(long revisionNumber) {
		XEvent event = this.events.get((int)(revisionNumber - this.revisionNumber));
		XyAssert.xyAssert(event == null || event.getRevisionNumber() == revisionNumber);
		return event;
	}
	
	@Override
    public long getFirstRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
    public void setFirstRevisionNumber(long rev) {
		if(!this.events.isEmpty()) {
			throw new IllegalStateException(
			        "cannot set start revision number of non-empty change log");
		}
		this.revisionNumber = rev;
	}
	
	@Override
	public String toString() {
		return "change log for " + getBaseAddress() + ": baseRev=" + this.revisionNumber
		        + " currentRev=" + getCurrentRevisionNumber() + " events=" + this.events.toString();
	}
	
	@Override
    public boolean truncateToRevision(long revisionNumber) {
		if(revisionNumber > getCurrentRevisionNumber()) {
			return false;
		}
		
		if(revisionNumber < this.revisionNumber - 1) {
			return false;
		}
		
		while(revisionNumber < getCurrentRevisionNumber()) {
			this.events.remove(this.events.size() - 1); // remove last element
		}
		
		XyAssert.xyAssert(revisionNumber == getCurrentRevisionNumber());
		
		return true;
	}
}