package org.xydra.core.model.impl.memory;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XSyncLogState;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


public class MemorySyncLogState implements XSyncLogState {
	
	private class SimplePair implements Entry<XCommand,XEvent> {
		
		XCommand command;
		XEvent event;
		
		public SimplePair(XCommand command, XEvent event) {
			this.command = command;
			this.event = event;
		}
		
		@Override
		public XCommand getKey() {
			return this.command;
		}
		
		@Override
		public XEvent getValue() {
			return this.event;
		}
		
		@Override
		public XEvent setValue(XEvent value) {
			this.event = value;
			return value;
		}
		
		@Override
		public String toString() {
			return this.command.toString() + ", " + this.event.toString();
		}
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2293394396146852612L;
	
	/** the ID of the model this change log refers to **/
	private XAddress baseAddr;
	
	/**
	 * the revision number the model had when this synclog was created.
	 **/
	private long syncRevisionNumber = -1;
	
	/** the event map: revNR -> tuple<transaction,event> **/
	private SortedMap<Long,Entry<XCommand,XEvent>> eventMap = new TreeMap<Long,Entry<XCommand,XEvent>>();
	
	private static final Logger log = LoggerFactory.getLogger(MemoryChangeLogState.class);
	
	/**
	 * Creates a new MemorySyncLogState. Make sure to set the
	 * {@link #setSyncRevisionNumber(long)}.
	 * 
	 * @param baseAddr The {@link XAddress} of the entity holding the
	 *            MemorySyncLog which is represented by this MemorySyncLogState
	 */
	public MemorySyncLogState(XAddress baseAddr) {
		this.baseAddr = baseAddr;
	}
	
	@Override
	public void appendCommandEventPair(XCommand command, XEvent event) {
		if(command == null || event == null) {
			// TODO throw exception once all data is migrated; for now: do
			// nothing, just complain a little
			log.warn("Skipping null-event");
		} else {
			XyAssert.xyAssert(this.baseAddr.equalsOrContains(event.getChangedEntity()), "baseAddr="
			        + this.baseAddr + " does not contain " + event.getChangedEntity());
			XyAssert.xyAssert(event.getRevisionNumber() > getCurrentRevisionNumber());
			XyAssert.xyAssert(!event.inTransaction());
			this.eventMap.put(event.getRevisionNumber(), new SimplePair(command, event));
		}
	}
	
	@Override
	public XAddress getBaseAddress() {
		return this.baseAddr;
	}
	
	@Override
	public long getCurrentRevisionNumber() {
		if(this.eventMap.isEmpty()) {
			return this.syncRevisionNumber;
		} else {
			return getLastCommandEventPair().getValue().getRevisionNumber();
		}
		
	}
	
	@Override
	public long getSyncRevisionNumber() {
		return this.syncRevisionNumber;
	}
	
	@Override
	public void setSyncRevisionNumber(long rev) {
		if(!this.eventMap.isEmpty()) {
			throw new IllegalStateException(
			        "cannot set start revision number of non-empty change log");
		}
		this.syncRevisionNumber = rev;
		
	}
	
	@Override
	public String toString() {
		return "sync log for " + getBaseAddress() + ": baseRev=" + this.syncRevisionNumber
		        + " currentRev=" + getCurrentRevisionNumber() + " events="
		        + this.eventMap.toString();
	}
	
	@Override
	public boolean truncateToRevision(long revisionNumber) {
		if(revisionNumber > getCurrentRevisionNumber()) {
			return false;
		}
		
		if(revisionNumber < this.syncRevisionNumber) {
			return false;
		}
		
		this.eventMap = this.eventMap.headMap(revisionNumber);
		
		XyAssert.xyAssert(revisionNumber == getCurrentRevisionNumber());
		
		return true;
	}
	
	@Override
	public Entry<XCommand,XEvent> getCommandEventPair(long revisionNumber) {
		Entry<XCommand,XEvent> commandEventPair = this.eventMap.get(revisionNumber);
		XyAssert.xyAssert(commandEventPair.getValue() == null
		        || commandEventPair.getValue().getRevisionNumber() == revisionNumber, "event="
		        + commandEventPair.getValue());
		return commandEventPair;
	}
	
	@Override
	public Entry<XCommand,XEvent> getLastCommandEventPair() {
		if(this.eventMap.isEmpty()) {
			return null;
		} else {
			Long lastKey = this.eventMap.lastKey();
			Entry<XCommand,XEvent> commandEventPair = getCommandEventPair(lastKey);
			return commandEventPair;
		}
	}
	
}
