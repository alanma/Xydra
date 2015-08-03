package org.xydra.core.model.impl.memory.sync;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLogState;
import org.xydra.index.iterator.Iterators;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

public class MemorySyncLogState implements XSyncLogState {

	private static final Logger log = LoggerFactory.getLogger(MemorySyncLogState.class);

	private static final long serialVersionUID = 2293394396146852612L;

	/** the ID of the model this change log refers to **/
	private XAddress baseAddr;

	/** the event map: revNR -> tuple<transaction,event> **/
	private SortedMap<Long, ISyncLogEntry> eventMap = new TreeMap<Long, ISyncLogEntry>();

	/**
	 * the revision number the model had when this sync log was created.
	 **/
	private long syncRevisionNumber = XCommand.NEW;

	private long baseRevisionNumber;

	/**
	 * Creates a new MemorySyncLogState. Make sure to set the
	 * {@link #setSyncRevisionNumber(long)}.
	 *
	 * @param baseAddr The {@link XAddress} of the entity holding the
	 *            MemorySyncLog which is represented by this MemorySyncLogState
	 */
	public MemorySyncLogState(final XAddress baseAddr) {
		this.baseAddr = baseAddr;
	}

	/**
	 * @param changeLogState
	 */
	public MemorySyncLogState(final XChangeLogState changeLogState) {
		if (changeLogState instanceof MemorySyncLogState) {
			final MemorySyncLogState msl = (MemorySyncLogState) changeLogState;
			this.baseAddr = msl.baseAddr;
			this.baseRevisionNumber = msl.baseRevisionNumber;
			this.syncRevisionNumber = msl.syncRevisionNumber;
			this.eventMap = msl.eventMap;
		} else {
			final long baseRev = changeLogState.getBaseRevisionNumber();
			final long currentRev = changeLogState.getCurrentRevisionNumber();
			this.baseAddr = changeLogState.getBaseAddress();
			this.baseRevisionNumber = baseRev;
			this.syncRevisionNumber = baseRev;
			for (long i = baseRev + 1; i <= currentRev; i++) {
				final XEvent event = changeLogState.getEvent(i);
				appendEvent(event);
			}
		}
	}

	@Override
	public void appendEvent(final XEvent event) {
		final MemorySyncLogEntry syncLogEntry = new MemorySyncLogEntry(null, event);
		appendSyncLogEntry(syncLogEntry);
	}

	@Override
	public void appendSyncLogEntry(final ISyncLogEntry syncLogEntry) {
		if (syncLogEntry == null || syncLogEntry.getEvent() == null) {
			log.warn("Skipping null-event");
		} else {
			final XEvent event = syncLogEntry.getEvent();
			XyAssert.xyAssert(this.baseAddr.equalsOrContains(event.getChangedEntity()),
					"baseAddr=%s does not contain %s", this.baseAddr, event.getChangedEntity());
			XyAssert.xyAssert(event.getRevisionNumber() > getCurrentRevisionNumber(),
					"eventRev=%s, currentRev=%s event=%s", +event.getRevisionNumber(),
					getCurrentRevisionNumber(), event);
			XyAssert.xyAssert(!event.inTransaction(), "event not in txn? %s", event);
			assert !this.eventMap.containsKey(event.getRevisionNumber());
			addEntry(syncLogEntry);
		}
	}

	private void addEntry(final ISyncLogEntry syncLogEntry) {
		this.eventMap.put(syncLogEntry.getEvent().getRevisionNumber(), syncLogEntry);
	}

	@Override
	public XAddress getBaseAddress() {
		return this.baseAddr;
	}

	@Override
	public long getBaseRevisionNumber() {
		return this.baseRevisionNumber;
	}

	@Override
	public long getCurrentRevisionNumber() {
		if (this.eventMap.isEmpty()) {
			return this.baseRevisionNumber;
		} else {
			return getLastEvent().getRevisionNumber();
		}

	}

	@Override
	public XEvent getEvent(final long revisionNumber) {
		final ISyncLogEntry entry = this.eventMap.get(revisionNumber);
		if (entry == null) {
			return null;
		}
		assert entry.getEvent().getRevisionNumber() == revisionNumber;
		return entry.getEvent();
	}

	@Override
	public XEvent getLastEvent() {
		if (this.eventMap.isEmpty()) {
			return null;
		}
		final long last = getLast();
		return getEvent(last);
	}

	private long getLast() {
		final Long lastKey = this.eventMap.lastKey();
		return lastKey;
	}

	private long getFirst() {
		final Long firstKey = this.eventMap.firstKey();
		return firstKey;
	}

	@Override
	public ISyncLogEntry getSyncLogEntry(final long revisionNumber) {
		final ISyncLogEntry syncLogEntry = this.eventMap.get(revisionNumber);
		XyAssert.xyAssert(syncLogEntry.getEvent() == null
				|| syncLogEntry.getEvent().getRevisionNumber() == revisionNumber, "event="
				+ syncLogEntry.getEvent());
		return syncLogEntry;
	}

	@Override
	public long getSyncRevisionNumber() {
		return this.syncRevisionNumber;
	}

	@Override
	public void setBaseRevisionNumber(final long baseRevisionNumber) {
		this.baseRevisionNumber = baseRevisionNumber;
	}

	@Override
	public void setSyncRevisionNumber(final long rev) {
		// if(!this.eventMap.isEmpty()) {
		// throw new IllegalStateException(
		// "cannot set start revision number of non-empty change log");
		// }
		this.syncRevisionNumber = rev;

	}

	@Override
	public String toString() {
		return "sync log for " + getBaseAddress() + ": baseRev=" + this.baseRevisionNumber
				+ ", syncRev=" + this.syncRevisionNumber + " currentRev="
				+ getCurrentRevisionNumber() + " events=" + this.eventMap.toString();
	}

	@Override
	public boolean truncateToRevision(final long revisionNumber) {
		if (revisionNumber > getCurrentRevisionNumber()) {
			return false;
		}

		if (revisionNumber < this.syncRevisionNumber) {
			return false;
		}

		if (log.isDebugEnabled()) {
			log.debug("Truncating local syncLog down to rev="
					+ revisionNumber
					+ "; highest was "
					+ (getLastEvent() == null ? "none" : getLastEvent()
							.getRevisionNumber()));
		}

		/* Delete all events LATER than syncRev */
		final SortedMap<Long, ISyncLogEntry> toBeDeleted = this.eventMap.tailMap(revisionNumber + 1);
		while (toBeDeleted.size() > 0) {
			final Long key = toBeDeleted.lastKey();
			assert key != null;
			this.eventMap.remove(key);
		}

		XyAssert.xyAssert(getCurrentRevisionNumber() <= revisionNumber, "revNr=" + revisionNumber
				+ " currRev=" + getCurrentRevisionNumber());

		return true;
	}

	@Override
	public void removeSyncLogEntryAt(final Long l) {
		this.eventMap.remove(l);
	}

	class SyncLogEntryIterator implements Iterator<ISyncLogEntry> {

		private final long end;

		private long i;

		private ISyncLogEntry next;

		public SyncLogEntryIterator(final long begin, final long end) {
			this.i = begin;
			this.end = end;
		}

		private void getNext() {
			while (this.i < this.end && this.next == null) {
				this.next = getSyncLogEntryAt(this.i);
				this.i++;
			}
		}

		@Override
		public boolean hasNext() {
			getNext();
			return this.next != null;
		}

		@Override
		public ISyncLogEntry next() {
			final ISyncLogEntry syncLogEntry = this.next;
			this.next = null;
			getNext();
			return syncLogEntry;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public synchronized Iterator<ISyncLogEntry> getSyncLogEntriesBetween(final long beginRevision,
			final long endRevision) {
		final SortedMap<Long, ISyncLogEntry> subMap = getSyncLogEntriesBetween_asMap(beginRevision,
				endRevision);
		if (subMap == null) {
			return Iterators.none();
		} else {
			return subMap.values().iterator();
		}
	}

	private SortedMap<Long, ISyncLogEntry> getSyncLogEntriesBetween_asMap(final long beginRevision,
			final long endRevision) {
		/*
		 * firstRev: the revision number the logged XModel had at the time when
		 * the first event was recorded by the change log
		 */
		long firstRev;
		final XEvent firstEvent = getFirstEvent();
		if (firstEvent != null) {
			firstRev = getFirstEvent().getRevisionNumber();
		} else {
			// empty syncLog
			firstRev = -1;
		}
		final long curRev = getCurrentRevisionNumber();

		if (beginRevision < 0) {
			throw new IndexOutOfBoundsException(
					"beginRevision is not a valid revision number, was " + beginRevision);
		}

		if (endRevision < 0) {
			throw new IndexOutOfBoundsException("endRevision is not a valid revision number, was "
					+ endRevision);
		}

		if (beginRevision > endRevision) {
			return null;
			// throw new IllegalArgumentException("beginRevision (" +
			// beginRevision
			// + ") may not be greater than endRevision (" + endRevision + ")");
		}

		if (beginRevision >= endRevision || endRevision <= firstRev) {
			return null;
		}

		final long begin = beginRevision < firstRev ? firstRev : beginRevision;
		final long end = endRevision > curRev ? curRev + 1 : endRevision;

		if (begin > end) {
			return null;
		}

		return this.eventMap.subMap(begin, end);
	}

	private XEvent getFirstEvent() {
		if (this.eventMap.isEmpty()) {
			return null;
		}
		final long first = getFirst();
		return getEvent(first);
	}

	@Override
	public Iterator<ISyncLogEntry> getSyncLogEntriesSince(final long revisionNumber) {
		return getSyncLogEntriesBetween(revisionNumber, Long.MAX_VALUE);
	}

	@Override
	public Iterator<ISyncLogEntry> getSyncLogEntriesUntil(final long revisionNumber) {
		return getSyncLogEntriesBetween(0, revisionNumber);
	}

	@Override
	public ISyncLogEntry getSyncLogEntryAt(final long revisionNumber) {
		if (revisionNumber < 0) {
			throw new IllegalArgumentException("revisionNumber may not be less than zero: "
					+ revisionNumber);
		}

		if (revisionNumber <= getSyncRevisionNumber()) {
			throw new IndexOutOfBoundsException("revisionNumber (" + revisionNumber
					+ ") may not be <= than the first revision number of this log ("
					+ getSyncRevisionNumber() + ")");
		}

		if (revisionNumber > getCurrentRevisionNumber()) {
			throw new IndexOutOfBoundsException(
					"revisionNumber may not be greater than or equal to the current revision"
							+ "number of this log");
		}
		final ISyncLogEntry syncLogEntry = getSyncLogEntry(revisionNumber);
		assert syncLogEntry.getEvent() == null
				|| syncLogEntry.getEvent().getRevisionNumber() == revisionNumber : "event="
				+ syncLogEntry.getEvent();
		return syncLogEntry;
	}

	@Override
	public long getSize() {
		return this.eventMap.size();
	}

}
