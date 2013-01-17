package org.xydra.core.model.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.sharedutils.XyAssert;


/**
 * Implementation of {@link XChangeLog} for use with {@link MemoryModel},
 * {@link MemoryObject} and {@link MemoryField}.
 * 
 * @author Kaidel
 * 
 */
public class MemoryChangeLog extends AbstractChangeLog implements XChangeLog {
	
	private static final long serialVersionUID = -3242936915355886858L;
	
	private XChangeLogState state;
	
	public MemoryChangeLog(XChangeLogState state) {
		
		XyAssert.xyAssert(state != null); assert state != null;
		
		this.state = state;
		
	}
	
	/**
	 * Appends an {@link XEvent} to the end of this MemoryChangeLog
	 * 
	 * @param event the {@link XEvent} which is to be appended
	 */
	synchronized protected void appendEvent(XEvent event) {
		// TODO
		assert event == null || (event.getRevisionNumber() == getCurrentRevisionNumber() + 1) : "cannot append event with rev "
		        + event.getRevisionNumber()
		        + " to model change log at event "
		        + getCurrentRevisionNumber() + ": " + event;
		
		XyAssert.xyAssert(event == null || !event.inTransaction());
		// "else": event is part of a transaction and will therefore only be
		// recorded as part of the transaction
		
		this.state.appendEvent(event);
	}
	
	@Override
    public XAddress getBaseAddress() {
		return this.state.getBaseAddress();
	}
	
	@Override
    synchronized public long getCurrentRevisionNumber() {
		return this.state.getCurrentRevisionNumber();
	}
	
	@Override
    synchronized public XEvent getEventAt(long revisionNumber) {
		if(revisionNumber < 0) {
			throw new IllegalArgumentException("revisionNumber may not be less than zero");
		}
		
		if(revisionNumber < getFirstRevisionNumber()) {
			throw new IndexOutOfBoundsException(
			        "revisionNumber may not be less than the first revision" + "number of this log");
		}
		
		if(revisionNumber > this.state.getCurrentRevisionNumber()) {
			throw new IndexOutOfBoundsException(
			        "revisionNumber may not be greater than or equal to the current revision"
			                + "number of this log");
		}
		XEvent event = this.state.getEvent(revisionNumber);
		XyAssert.xyAssert(event == null || event.getRevisionNumber() == revisionNumber);
		return event;
	}
	
	@Override
    synchronized public long getFirstRevisionNumber() {
		return this.state.getFirstRevisionNumber();
	}
	
	@Override
	synchronized public String toString() {
		return this.state.toString();
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
	synchronized protected boolean truncateToRevision(long revisionNumber) {
		return this.state.truncateToRevision(revisionNumber);
	}
	
}