package org.xydra.core.model.impl.memory;

import java.util.Map.Entry;

import org.xydra.annotations.CanBeNull;
import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XSyncLog;
import org.xydra.core.model.XSyncLogState;
import org.xydra.sharedutils.XyAssert;


public class MemorySyncLog extends AbstractSyncLog implements XSyncLog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7008700708157729184L;
	
	/** serialisable state */
	private XSyncLogState state;
	
	/**
	 * @param state @NeverNull
	 */
	MemorySyncLog(XSyncLogState state) {
		if(state == null)
			throw new IllegalArgumentException("state may not be null");
		this.state = state;
	}
	
	// TODO handling of events in transactions???
	@Override
	public synchronized void appendCommandEventPair(@CanBeNull XCommand command,
	        @CanBeNull XEvent event) {
		XyAssert.xyAssert((event == null && command == null) || !event.inTransaction());
		/*
		 * "else": event is part of a transaction and will therefore only be
		 * recorded as part of the transaction
		 */
		this.state.appendCommandEventPair(command, event);
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
	synchronized public Entry<XCommand,XEvent> getCommandEventPairAt(long revisionNumber) {
		if(revisionNumber < 0) {
			throw new IllegalArgumentException("revisionNumber may not be less than zero: "
			        + revisionNumber);
		}
		
		if(revisionNumber <= getSyncRevisionNumber()) {
			throw new IndexOutOfBoundsException("revisionNumber (" + revisionNumber
			        + ") may not be <= than the first revision number of this log ("
			        + getSyncRevisionNumber() + ")");
		}
		
		if(revisionNumber > this.state.getCurrentRevisionNumber()) {
			throw new IndexOutOfBoundsException(
			        "revisionNumber may not be greater than or equal to the current revision"
			                + "number of this log");
		}
		Entry<XCommand,XEvent> commandEventPair = this.state.getCommandEventPair(revisionNumber);
		XyAssert.xyAssert((commandEventPair.getKey() == null && commandEventPair.getValue() == null)
		        || commandEventPair.getValue().getRevisionNumber() == revisionNumber);
		return commandEventPair;
	}
	
	@Override
	synchronized public long getSyncRevisionNumber() {
		return this.state.getSyncRevisionNumber();
	}
	
	@Override
	synchronized public String toString() {
		return this.state.toString();
	}
	
	@Override
	public synchronized boolean truncateToRevision(long revisionNumber) {
		return this.state.truncateToRevision(revisionNumber);
	}
	
	@Override
	public XSyncLogState getSyncLogState() {
		return this.state;
	}
	
	public static XSyncLog create(XAddress baseAddress, long syncRevision) {
		assert baseAddress.getRepository() != null;
		MemorySyncLogState syncLogState = new MemorySyncLogState(baseAddress);
		syncLogState.setSyncRevisionNumber(syncRevision);
		return new MemorySyncLog(syncLogState);
	}
	
	@Override
	public Entry<XCommand,XEvent> getLastCommandEventPairs() {
		return this.state.getLastCommandEventPair();
	}
	
}
