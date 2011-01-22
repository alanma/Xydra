package org.xydra.core.model.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XStateTransaction;


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
		
		assert state != null;
		
		this.state = state;
		
	}
	
	/**
	 * Appends an {@link XEvent} to the end of this MemoryChangeLog
	 * 
	 * @param event the {@link XEvent} which is to be appended
	 */
	protected void appendEvent(XEvent event, XStateTransaction transaction) {
		// TODO
		assert event == null || (event.getRevisionNumber() == getCurrentRevisionNumber() + 1) : "cannot append event with rev "
		        + event.getRevisionNumber()
		        + " to model change log at event "
		        + getCurrentRevisionNumber() + ": " + event;
		
		assert event == null || !event.inTransaction();
		// "else": event is part of a transaction and will therefore only be
		// recorded as part of the transaction
		
		this.state.appendEvent(event, transaction);
	}
	
	protected void delete(XStateTransaction transaction) {
		this.state.delete(transaction);
	}
	
	public XAddress getBaseAddress() {
		return this.state.getBaseAddress();
	}
	
	synchronized public long getCurrentRevisionNumber() {
		return this.state.getCurrentRevisionNumber();
	}
	
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
		assert event == null || event.getRevisionNumber() == revisionNumber;
		return event;
	}
	
	synchronized public long getFirstRevisionNumber() {
		return this.state.getFirstRevisionNumber();
	}
	
	protected void save(XStateTransaction transaction) {
		this.state.save(transaction);
	}
	
	@Override
	public String toString() {
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
	protected boolean truncateToRevision(long revisionNumber, XStateTransaction transaction) {
		return this.state.truncateToRevision(revisionNumber, transaction);
	}
	
}
