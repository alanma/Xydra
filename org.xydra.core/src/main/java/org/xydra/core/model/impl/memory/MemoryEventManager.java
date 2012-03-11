package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl.Orphans;
import org.xydra.index.XI;


/**
 * A queue for {@link XModelEvent XModelEvents}, {@link XObjectEvent
 * XObjectEvents} and {@link XFieldEvent XFieldEvents} which are to be
 * dispatched after all current change operations are completed.
 * 
 */
public class MemoryEventManager implements Serializable {
	
	private static class EventCollision {
		
		protected int first;
		protected int last;
		
		public EventCollision(int first) {
			this.first = first;
			this.last = -1;
		}
		
	}
	
	/**
	 * A container for a given {@link XEvent} and the affected entities
	 */
	protected static class EventQueueEntry {
		
		XEvent event;
		MemoryField field;
		MemoryModel model;
		MemoryObject object;
		MemoryRepository repo;
		
		public EventQueueEntry(MemoryRepository repo, MemoryModel model, MemoryObject object,
		        MemoryField field, XEvent event) {
			this.repo = repo;
			this.model = model;
			this.object = object;
			this.field = field;
			this.event = event;
		}
		
	}
	
	private static final long serialVersionUID = -4839276542320739074L;
	private final MemoryChangeLog changeLog;
	
	private final List<EventQueueEntry> eventQueue = new ArrayList<EventQueueEntry>();
	private final List<MemoryLocalChange> localChanges = new ArrayList<MemoryLocalChange>();
	/**
	 * Should events be logged right now?
	 */
	private boolean logging;
	/**
	 * A list of temporarily removed orphans.
	 */
	protected Orphans orphans;
	
	private boolean sending;
	
	private XID sessionActor;
	
	private String sessionPasswordHash;
	
	private long syncRevision;
	
	/**
	 * Is there currently a transaction running?
	 */
	protected boolean transactionInProgess;
	
	/**
	 * Creates a new MemoryEventQueue
	 * 
	 * @param log The {@link XChangeLog} this MemoryEventQueue will use for
	 *            logging (may be null)
	 */
	public MemoryEventManager(XID actorId, String passwordHash, MemoryChangeLog log, long syncRev) {
		assert actorId != null;
		this.sessionActor = actorId;
		this.sessionPasswordHash = passwordHash;
		this.changeLog = log;
		this.logging = this.changeLog != null;
		this.syncRevision = syncRev;
	}
	
	/**
	 * Remove all {@link XEvent XEvents} after 'since' from this
	 * MemoryEventQueue that cancel each other out. (for example, remove
	 * {@link XEvent XEvents} that one after another add and remove the same
	 * entity)
	 * 
	 * @Param since Clean all {@link XEvent XEvents} after the {@link XEvent}
	 *        with this index in the current MemoryEventQueue (value can be
	 *        retrieved from getNextPosition())
	 */
	protected void cleanEvents(int since) {
		
		// TODO the actorId is when merging events, is this OK?
		
		assert this.eventQueue instanceof RandomAccess;
		
		if(since + 1 >= this.eventQueue.size()) {
			// we need more than one event to clean
			return;
		}
		
		Map<XAddress,EventCollision> fields = new HashMap<XAddress,EventCollision>();
		Map<XAddress,EventCollision> coll = new HashMap<XAddress,EventCollision>();
		
		// Find the first and last event for each entity, set everything else to
		// null.
		int size = this.eventQueue.size();
		for(int i = since; i < size; i++) {
			
			XEvent event = this.eventQueue.get(i).event;
			
			assert event != null;
			
			if(event instanceof XTransactionEvent) {
				this.eventQueue.set(i, null);
				continue;
			}
			assert !(event instanceof XTransactionEvent);
			
			XAddress changed = event.getChangedEntity();
			
			Map<XAddress,EventCollision> map = (event instanceof XFieldEvent) ? fields : coll;
			
			EventCollision ec = map.get(changed);
			
			if(ec == null) {
				map.put(changed, new EventCollision(i));
			} else {
				if(ec.last >= 0) {
					this.eventQueue.set(ec.last, null);
				}
				ec.last = i;
			}
			
		}
		
		// Merge colliding field events.
		for(EventCollision ec : fields.values()) {
			
			assert this.eventQueue.get(ec.first) != null;
			if(ec.last < 0) {
				// no collision
				continue;
			}
			XEvent first = this.eventQueue.get(ec.first).event;
			
			EventQueueEntry e = this.eventQueue.get(ec.last);
			assert e != null;
			XEvent last = e.event;
			
			assert first instanceof XReversibleFieldEvent;
			assert last instanceof XReversibleFieldEvent;
			
			XEvent merged = mergeFieldEvents((XReversibleFieldEvent)first,
			        (XReversibleFieldEvent)last);
			
			this.eventQueue.set(ec.first, null);
			
			if(merged == null) {
				this.eventQueue.set(ec.last, null);
			} else if(merged != last) {
				EventQueueEntry qe = new EventQueueEntry(e.repo, e.model, e.object, e.field, merged);
				this.eventQueue.set(ec.last, qe);
			}
			
		}
		
		// Merge colliding non-field events.
		for(EventCollision ec : coll.values()) {
			
			assert this.eventQueue.get(ec.first) != null;
			if(ec.last < 0) {
				// no collision
				continue;
			}
			assert this.eventQueue.get(ec.last) != null;
			XEvent first = this.eventQueue.get(ec.first).event;
			XEvent last = this.eventQueue.get(ec.last).event;
			
			this.eventQueue.set(ec.first, null);
			if(first.getChangeType() != last.getChangeType()) {
				// ADD and REMOVE cancel each other out
				this.eventQueue.set(ec.last, null);
			}
			
		}
		
		// Now remove all non-null entries. This is optimized for an ArrayList.
		
		// Find the first null entry.
		int i = since;
		while(i < size && this.eventQueue.get(i) != null) {
			i++;
		}
		
		// Compact all non-null entries.
		for(int j = i + 1; j < size; j++) {
			EventQueueEntry e = this.eventQueue.get(j);
			if(e != null) {
				this.eventQueue.set(i, e);
				i++;
			}
		}
		
		// Remove all null entries from the end of the queue.
		for(int j = size - 1; j >= i; j--) {
			this.eventQueue.remove(j);
		}
		
		assert !containsNullEntries();
	}
	
	/**
	 * @return true, if this MemoryEventQueue contains null values or
	 *         {@link EventQueueEntry EventQueueEntries} containing null values
	 *         as their {@link XEvent}
	 */
	private boolean containsNullEntries() {
		
		for(EventQueueEntry entry : this.eventQueue) {
			if(entry == null || entry.event == null) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Creates an {@link XTransactionEvent} containing the {@link XEvent
	 * XEvents} in this MemoryEventQueue since the given index 'since' as
	 * specified by the given parameters. The created transaction is enqueued
	 * and logged if logging is enabled.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param model The {@link MemoryModel} in which the {@link XEvent XEvents}
	 *            occurred (may be null, if 'object' is not null)
	 * @param object The {@link MemoryObject} in which the {@link XEvent
	 *            XEvents} occurred (may be null, if 'model' is not null)
	 * @Param since The created {@link XTransactionEvent} will contain all
	 *        {@link XEvent XEvents} after the {@link XEvent} with this index in
	 *        the current MemoryEventQueue (value can be retrieved from
	 *        getNextPosition())
	 */
	protected void createTransactionEvent(XID actor, MemoryModel model, MemoryObject object,
	        int since) {
		
		assert this.eventQueue instanceof RandomAccess;
		
		assert since >= 0 && since < this.eventQueue.size() : "Invalid argument 'since', have events been sent?";
		
		assert since < this.eventQueue.size() - 1 : "Transactions should have more than one event.";
		
		assert model != null || object != null : "either model or object must not be null";
		
		@SuppressWarnings("null")
		XAddress target = object == null ? model.getAddress() : object.getAddress();
		
		XAtomicEvent[] events = new XAtomicEvent[this.eventQueue.size() - since];
		for(int i = since; i < this.eventQueue.size(); ++i) {
			events[i - since] = (XAtomicEvent)this.eventQueue.get(i).event;
		}
		
		long modelRev = model == null ? XEvent.RevisionOfEntityNotSet : model.getRevisionNumber();
		long objectRev = object == null ? XEvent.RevisionOfEntityNotSet : object
		        .getRevisionNumber();
		
		XTransactionEvent trans = MemoryTransactionEvent.createTransactionEvent(actor, target,
		        events, modelRev, objectRev);
		
		MemoryRepository repo = model == null ? null : model.getFather();
		
		enqueueEvent(new EventQueueEntry(repo, model, object, null, trans));
	}
	
	/**
	 * Enqueues the given {@link EventQueueEntry}.
	 * 
	 * @param entry The {@link EventQueueEntry} to be enqueued.
	 */
	private void enqueueEvent(EventQueueEntry entry) {
		
		if(this.logging && !entry.event.inTransaction()) {
			this.changeLog.appendEvent(entry.event);
		}
		
		this.eventQueue.add(entry);
	}
	
	/**
	 * Enqueues the given {@link XFieldEvent}.
	 * 
	 * TODO check whether the given XEntities actually fit to the XEntities
	 * specified by the XIDs in the event
	 * 
	 * @param field The {@link MemoryField} in which this event occurred.
	 * @param event The {@link XFieldEvent}.
	 */
	protected void enqueueFieldEvent(MemoryField field, XReversibleFieldEvent event) {
		assert field != null && event != null : "Neither field nor event may be null!";
		
		MemoryObject object = field.getObject();
		MemoryModel model = object == null ? null : object.getModel();
		MemoryRepository repo = model == null ? null : model.getFather();
		
		enqueueEvent(new EventQueueEntry(repo, model, object, field, event));
	}
	
	/**
	 * Enqueues the given {@link XModelEvent}.
	 * 
	 * TODO check whether the given XEntities actually fit to the XEntities
	 * specified by the XIDs in the event
	 * 
	 * @param model The {@link MemoryModel} in which this event occurred.
	 * @param event The {@link XModelEvent}.
	 */
	protected void enqueueModelEvent(MemoryModel model, XModelEvent event) {
		assert model != null && event != null : "Neither model nor event may be null!";
		
		enqueueEvent(new EventQueueEntry(model.getFather(), model, null, null, event));
	}
	
	/**
	 * Enqueues the given {@link XObjectEvent}.
	 * 
	 * TODO check whether the given XEntities actually fit to the XEntities
	 * specified by the XIDs in the event
	 * 
	 * @param object The {@link MemoryObject} in which this event occurred.
	 * @param event The {@link XObjectEvent}.
	 */
	protected void enqueueObjectEvent(MemoryObject object, XObjectEvent event) {
		assert object != null && event != null : "Neither object nor event may be null!";
		
		MemoryModel model = object.getModel();
		MemoryRepository repo = model == null ? null : model.getFather();
		
		enqueueEvent(new EventQueueEntry(repo, model, object, null, event));
	}
	
	/**
	 * Enqueues the given {@link XRepositoryEvent}.
	 * 
	 * TODO check whether the given XEntities actually fit to the XEntities
	 * specified by the XIDs in the event
	 * 
	 * @param model The {@link MemoryModel} in which this event occurred.
	 * @param event The {@link XModelEvent}.
	 */
	protected void enqueueRepositoryEvent(MemoryRepository repo, XRepositoryEvent event) {
		assert event != null : "Event must not be null!";
		
		enqueueEvent(new EventQueueEntry(repo, null, null, null, event));
	}
	
	protected XID getActor() {
		return this.sessionActor;
	}
	
	/**
	 * @return the {@link MemoryChangeLog} being used for logging.
	 */
	protected XChangeLog getChangeLog() {
		return this.changeLog;
	}
	
	protected List<MemoryLocalChange> getLocalChanges() {
		return this.localChanges;
	}
	
	/**
	 * Get the position to use for the 'since' parameter of
	 * {@link #createTransactionEvent(XID, MemoryModel, MemoryObject, int)} or
	 * {@link #cleanEvents(int)} for using all {@link XEvent XEvents} that will
	 * be enqueued after the returned value.
	 * 
	 * Note: This position equals the current size of this MemoryEventQueue (the
	 * amount of enqueued {@link XEvent XEvents})
	 * 
	 * @return the position to use for the 'since' parameter, as described above
	 */
	protected int getNextPosition() {
		return this.eventQueue.size();
	}
	
	protected String getPasswordHash() {
		return this.sessionPasswordHash;
	}
	
	protected long getSyncRevision() {
		return this.syncRevision;
	}
	
	protected void logNullEvent() {
		this.changeLog.appendEvent(null);
	}
	
	/**
	 * Merges the two given {@link XFieldEvent XFieldEvents} to a single
	 * equivalent {@link XFieldEvent} representing both events, if possible.
	 * (for example an 'first' = REMOVE event, 'last' = ADD Event will result in
	 * a new event of the CHANGE-type)
	 * 
	 * @return The merged {@link XFieldEvent} (which may be 'last') or null if
	 *         the given {@link XFieldEvent XFieldEvents} cancel each other out.
	 */
	private static XReversibleFieldEvent mergeFieldEvents(XReversibleFieldEvent first,
	        XReversibleFieldEvent last) {
		
		assert first.getTarget().equals(last.getTarget());
		
		// Matching ADD->REMOVE or CHANGE->CHANGE or REMOVE->ADD
		// where the value is reset to the old state
		if(XI.equals(first.getOldValue(), last.getNewValue())) {
			return null;
		}
		
		switch(last.getChangeType()) {
		case ADD:
			if(first.getChangeType() == ChangeType.ADD) {
				return last;
			}
			assert first.getChangeType() == ChangeType.REMOVE;
			// non matching REMOVE -> ADD => merge to CHANGE
			return MemoryReversibleFieldEvent.createChangeEvent(last.getActor(), last.getTarget(),
			        first.getOldValue(), last.getNewValue(), last.getOldModelRevision(),
			        last.getOldObjectRevision(), last.getOldFieldRevision(), false);
		case REMOVE:
			if(first.getChangeType() == ChangeType.REMOVE) {
				return last;
			}
			assert first.getChangeType() == ChangeType.CHANGE;
			// (non matching) CHANGE->REMOVE => merge to REMOVE
			return MemoryReversibleFieldEvent.createRemoveEvent(last.getActor(), last.getTarget(),
			        first.getOldValue(), last.getOldModelRevision(), last.getOldObjectRevision(),
			        last.getOldFieldRevision(), false, false);
		case CHANGE:
			assert first.getChangeType() != ChangeType.REMOVE;
			if(first.getChangeType() == ChangeType.CHANGE) {
				// non-matching CHANGE->CHANGE => merge to CHANGE
				return MemoryReversibleFieldEvent.createChangeEvent(last.getActor(),
				        last.getTarget(), first.getOldValue(), last.getNewValue(),
				        last.getOldModelRevision(), last.getOldObjectRevision(),
				        last.getOldFieldRevision(), false);
			} else {
				assert first.getChangeType() == ChangeType.ADD;
				// non-matching ADD->CHANGE => merge to ADD
				return MemoryReversibleFieldEvent.createAddEvent(last.getActor(), last.getTarget(),
				        last.getNewValue(), last.getOldModelRevision(),
				        last.getOldObjectRevision(), last.getOldFieldRevision(), false);
			}
		default:
			throw new AssertionError("invalid event: " + last);
		}
		
	}
	
	protected void newLocalChange(XCommand command, XLocalChangeCallback callback) {
		if(this.orphans == null) {
			this.localChanges.add(new MemoryLocalChange(this.sessionActor,
			        this.sessionPasswordHash, command, callback));
		}
	}
	
	/**
	 * Propagates the {@link XEvent XEvents} in the enqueued
	 * {@link EventQueueEntry EventQueueEntries}.
	 */
	protected void sendEvents() {
		
		if(this.sending)
			return;
		
		this.sending = true;
		
		while(!this.eventQueue.isEmpty()) {
			EventQueueEntry entry = this.eventQueue.remove(0);
			
			assert entry != null;
			assert entry.event != null;
			
			assert entry.event instanceof XRepositoryEvent || entry.event instanceof XModelEvent
			        || entry.event instanceof XObjectEvent || entry.event instanceof XFieldEvent
			        || entry.event instanceof XTransactionEvent;
			
			if(entry.event instanceof XRepositoryEvent) {
				
				// fire repo event
				
				if(entry.repo != null) {
					entry.repo.fireRepositoryEvent((XRepositoryEvent)entry.event);
				}
				
			} else if(entry.event instanceof XModelEvent) {
				assert entry.model != null;
				
				// fire model event and propagate to fathers if necessary.
				
				entry.model.fireModelEvent((XModelEvent)entry.event);
				
				if(entry.repo != null) {
					entry.repo.fireModelEvent((XModelEvent)entry.event);
				}
				
			} else if(entry.event instanceof XObjectEvent) {
				assert entry.object != null;
				
				// fire object event and propagate to fathers if necessary.
				
				entry.object.fireObjectEvent((XObjectEvent)entry.event);
				
				if(entry.model != null) {
					entry.model.fireObjectEvent((XObjectEvent)entry.event);
					
					if(entry.repo != null) {
						entry.repo.fireObjectEvent((XObjectEvent)entry.event);
					}
				}
			} else if(entry.event instanceof XFieldEvent) {
				assert entry.field != null;
				
				// fire field event and propagate to fathers if necessary.
				
				entry.field.fireFieldEvent((XFieldEvent)entry.event);
				
				if(entry.object != null) {
					entry.object.fireFieldEvent((XFieldEvent)entry.event);
					
					if(entry.model != null) {
						entry.model.fireFieldEvent((XFieldEvent)entry.event);
						
						if(entry.repo != null) {
							entry.repo.fireFieldEvent((XFieldEvent)entry.event);
						}
					}
				}
			} else if(entry.event instanceof XTransactionEvent) {
				assert entry.model != null || entry.object != null;
				
				// fire transaction event and propagate to fathers if necessary.
				
				if(entry.object != null) {
					entry.object.fireTransactionEvent((XTransactionEvent)entry.event);
				}
				
				if(entry.model != null) {
					entry.model.fireTransactionEvent((XTransactionEvent)entry.event);
					
					if(entry.repo != null) {
						entry.repo.fireTransactionEvent((XTransactionEvent)entry.event);
					}
				}
			} else {
				throw new AssertionError("unknown event type queued: " + entry);
			}
			
		}
		
		this.sending = false;
	}
	
	/**
	 * Suspends and resumes logging.
	 * 
	 * @param logging True, to suspend logging, false for resuming.
	 * @return True, if logging was suspended before this method was called.
	 */
	protected boolean setBlockSending(boolean block) {
		boolean oldBlock = this.sending;
		this.sending = block;
		return oldBlock;
	}
	
	/**
	 * Suspend and resume logging.
	 * 
	 * @param logging Set to true if events should be logged.
	 * @return True, if logging was already activated before.
	 * @throws IllegalArgumentException if logging is set to true, but the
	 *             {@link XChangeLog} if this MemoryEventQueue is not set
	 */
	protected boolean setLogging(boolean logging) {
		
		if(logging && this.changeLog == null) {
			throw new IllegalArgumentException(
			        "Logging was set to true, but the MemoryChangeLog is not set");
		}
		
		boolean oldLogging = this.logging;
		this.logging = logging;
		return oldLogging;
	}
	
	protected void setSessionActor(XID actorId, String passwordHash) {
		assert actorId != null;
		this.sessionActor = actorId;
		this.sessionPasswordHash = passwordHash;
	}
	
	protected void setSyncRevision(long syncRevision) {
		this.syncRevision = syncRevision;
	}
	
	public void truncateLog(long revision) {
		boolean truncated = this.changeLog.truncateToRevision(revision);
		assert truncated;
		assert this.changeLog.getCurrentRevisionNumber() == revision;
	}
	
}
