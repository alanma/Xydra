package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;
import org.xydra.index.iterator.AbstractFilteringIterator;

import com.google.gwt.dev.jjs.impl.gflow.Analysis;


/**
 * TODO andi implement
 * 
 * @author xamde
 */
public class MemorySyncLog extends AbstractSyncLog implements ISyncLog {
	
	private static final long serialVersionUID = -7008700708157729184L;
	
	/** serialisable state */
	private ISyncLogState state;
	
	/**
	 * @param state @NeverNull
	 */
	public MemorySyncLog(ISyncLogState state) {
		if(state == null)
			throw new IllegalArgumentException("state may not be null");
		this.state = state;
	}
	
	/**
	 * Wrap an existing {@link XChangeLogState} into {@link Analysis}
	 * {@link ISyncLog}
	 * 
	 * @param changeLogState
	 */
	public MemorySyncLog(XChangeLogState changeLogState) {
		this(new MemorySyncLogState(changeLogState));
	}
	
	public MemorySyncLog(XAddress modelAddress) {
		this(new MemorySyncLogState(modelAddress));
	}
	
	// TODO handling of events in transactions???
	@Override
	public synchronized void appendSyncLogEntry(ISyncLogEntry syncLogEntry) {
		// TODO what to assert? XyAssert.xyAssert((event == null && command ==
		// null) || !event.inTransaction());
		/*
		 * "else": event is part of a transaction and will therefore only be
		 * recorded as part of the transaction
		 */
		this.state.appendSyncLogEntry(syncLogEntry);
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
	synchronized public ISyncLogEntry getSyncLogEntryAt(long revisionNumber) {
		return this.state.getSyncLogEntryAt(revisionNumber);
	}
	
	@Override
	synchronized public long getSynchronizedRevision() {
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
	public ISyncLogState getSyncLogState() {
		return this.state;
	}
	
	public static ISyncLog create(XAddress baseAddress, long syncRevision) {
		assert baseAddress.getRepository() != null;
		MemorySyncLogState syncLogState = new MemorySyncLogState(baseAddress);
		syncLogState.setSyncRevisionNumber(syncRevision);
		syncLogState.setBaseRevisionNumber(syncRevision);
		return new MemorySyncLog(syncLogState);
	}
	
	@Override
	public XEvent getEventAt(long revisionNumber) {
		return this.state.getEvent(revisionNumber);
	}
	
	@Override
	public long getBaseRevisionNumber() {
		return this.state.getBaseRevisionNumber();
	}
	
	@Override
	public XChangeLogState getChangeLogState() {
		return this.state;
	}
	
	@Override
	public XEvent getLastEvent() {
		return this.state.getLastEvent();
	}
	
	@Override
	public int countUnappliedLocalChanges() {
		// TODO return number of local commands since syncRev
		return -666;
	}
	
	@Override
	public void appendSyncLogEntry(XCommand command, XEvent event) {
		ISyncLogEntry syncLogEntry = new MemorySyncLogEntry(command, event);
		appendSyncLogEntry(syncLogEntry);
	}
	
	@Override
	public void setSynchronizedRevision(long syncronizedRevision) {
		this.state.setSyncRevisionNumber(syncronizedRevision);
	}
	
	@Override
	public void appendEvent(XEvent event) {
		this.state.appendEvent(event);
	}
	
	class LocalChangesIterator extends AbstractFilteringIterator<ISyncLogEntry> {
		
		public LocalChangesIterator(Iterator<ISyncLogEntry> base) {
			super(base);
		}
		
		@Override
		protected boolean matchesFilter(ISyncLogEntry entry) {
			return entry.getCommand() != null;
		}
		
	}
	
	@Override
	public Iterator<ISyncLogEntry> getLocalChanges() {
		return new LocalChangesIterator(getSyncLogEntriesSince(getSynchronizedRevision()));
	}
	
	@Override
	public void clearLocalChanges() {
		List<Long> toRemove = new LinkedList<Long>();
		Iterator<ISyncLogEntry> it = getLocalChanges();
		while(it.hasNext()) {
			ISyncLogEntry sle = it.next();
			toRemove.add(sle.getEvent().getRevisionNumber());
		}
		for(Long l : toRemove) {
			this.state.removeSyncLogEntryAt(l);
		}
	}
	
	@Override
	public boolean equals(Object b) {
		
		boolean equal = true;
		MemorySyncLog syncLogB = (MemorySyncLog)b;
		
		if(!this.getBaseAddress().equals(syncLogB.getBaseAddress())
		        || !(this.getBaseRevisionNumber() == syncLogB.getBaseRevisionNumber())
		        || !(this.getSynchronizedRevision() == syncLogB.getSynchronizedRevision())) {
			equal = false;
			return equal;
		}
		
		if(this.getCurrentRevisionNumber() < 0)
			return true;
		
		Iterator<ISyncLogEntry> iteratorA = this.getSyncLogEntriesBetween(
		        getBaseRevisionNumber() + 1, getCurrentRevisionNumber());
		Iterator<ISyncLogEntry> iteratorB = syncLogB.getSyncLogEntriesBetween(
		        getBaseRevisionNumber() + 1, getCurrentRevisionNumber());
		
		try {
			while(iteratorA.hasNext() && iteratorB.hasNext()) {
				ISyncLogEntry itemA = iteratorA.next();
				ISyncLogEntry itemB = iteratorB.next();
				
				XEvent eventA = itemA.getEvent();
				XEvent eventB = itemB.getEvent();
				
				XCommand commandA = itemA.getCommand();
				XCommand commandB = itemB.getCommand();
				equal = (eventA.equals(eventB) && commandA.equals(commandB));
				if(!equal)
					return false;
				
			}
			
			if(iteratorA.hasNext() || iteratorA.hasNext()) {
				equal = false;
			}
		} catch(NullPointerException e) {
			equal = false;
		}
		
		return equal;
	}
	
	@Override
	public Iterator<ISyncLogEntry> getSyncLogEntriesBetween(long beginRevision, long endRevision) {
		return this.state.getSyncLogEntriesBetween(beginRevision, endRevision);
	}
	
	@Override
	public Iterator<ISyncLogEntry> getSyncLogEntriesSince(long revisionNumber) {
		return this.state.getSyncLogEntriesSince(revisionNumber);
	}
	
	@Override
	public Iterator<ISyncLogEntry> getSyncLogEntriesUntil(long revisionNumber) {
		return this.state.getSyncLogEntriesUntil(revisionNumber);
	}
}
