package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/*
 * TODO Should the enqueue-Methods check whether the given XEntities actually
 * fit to the XEntities specified by the XIDs in the event? - only though
 * assertions (if at all) as this class is completely internal to XModel ~Daniel
 */

public class MemoryEventQueue {
	
	private final List<EventQueueEntry> eventQueue;
	private boolean sending;
	private final MemoryChangeLog changeLog;
	private boolean logging;
	
	public MemoryEventQueue(MemoryChangeLog log) {
		// array list to allow indexed access for creating transaction events
		this.eventQueue = new ArrayList<EventQueueEntry>();
		this.changeLog = log;
		this.logging = this.changeLog != null;
	}
	
	/**
	 * Enqueues the given entry.
	 * 
	 * @param entry The entry to be enqueued.
	 */
	private void enqueueEvent(EventQueueEntry entry) {
		
		if(this.logging && !entry.event.inTransaction()) {
			this.changeLog.appendEvent(entry.event);
		}
		
		this.eventQueue.add(entry);
	}
	
	/**
	 * Propagates the events in the enqueued entries.
	 */
	public void sendEvents() {
		
		if(this.sending)
			return;
		
		this.sending = true;
		
		while(!this.eventQueue.isEmpty()) {
			EventQueueEntry entry = this.eventQueue.remove(0);
			
			assert entry.event != null;
			
			assert entry.event instanceof XModelEvent || entry.event instanceof XObjectEvent
			        || entry.event instanceof XFieldEvent
			        || entry.event instanceof XTransactionEvent;
			
			if(entry.event instanceof XModelEvent) {
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
	 * Enqueues the given {@link XModelEvent}.
	 * 
	 * @param model The {@link MemoryModel} in which this event occurred.
	 * @param event The event.
	 */
	public void enqueueModelEvent(MemoryModel model, XModelEvent event) {
		assert model != null && event != null : "Neither model nor event may be null!";
		
		enqueueEvent(new EventQueueEntry(model.getFather(), model, null, null, event));
	}
	
	/**
	 * Enqueues the given {@link XObjectEvent}.
	 * 
	 * @param object The {@link MemoryObject} in which this event occurred.
	 * @param event The event.
	 */
	public void enqueueObjectEvent(MemoryObject object, XObjectEvent event) {
		assert object != null && event != null : "Neither object nor event may be null!";
		
		MemoryModel model = object.getFather();
		MemoryRepository repo = model == null ? null : model.getFather();
		
		enqueueEvent(new EventQueueEntry(repo, model, object, null, event));
	}
	
	/**
	 * Enqueues the given {@link XFieldEvent}.
	 * 
	 * @param field The {@link MemoryField} in which this event occurred.
	 * @param event The event.
	 */
	public void enqueueFieldEvent(MemoryField field, XFieldEvent event) {
		assert field != null && event != null : "Neither field nor event may be null!";
		
		MemoryObject object = field.getFather();
		MemoryModel model = object == null ? null : object.getFather();
		MemoryRepository repo = model == null ? null : model.getFather();
		
		enqueueEvent(new EventQueueEntry(repo, model, object, field, event));
	}
	
	/**
	 * Enqueues the given {@link XTransactionEvent}.
	 * 
	 * @param field The {@link MemoryField} in which this event occurred.
	 * @param event The event.
	 * @Param since The transaction will contain all events after this (value
	 *        retrieved from getNextPosition())
	 */
	@SuppressWarnings("null")
	public void createTransactionEvent(XID actor, MemoryModel model, MemoryObject object, int since) {
		
		assert since >= 0 && since < this.eventQueue.size() : "Invalid since, have events been sent?";
		
		assert since < this.eventQueue.size() - 1 : "Transactions should have more than one event.";
		
		assert model != null || object != null : "either model or object must not be null";
		
		XAddress target = object != null ? object.getAddress() : model.getAddress();
		
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
	 * Get the position to use for the since parameter of
	 * createTransactionEvent() or cleanEvents() for using all following events.
	 * 
	 * @return
	 */
	int getNextPosition() {
		return this.eventQueue.size();
	}
	
	/**
	 * A container for a given event and the affected XEntities
	 */
	private static class EventQueueEntry {
		
		MemoryRepository repo;
		MemoryModel model;
		MemoryObject object;
		MemoryField field;
		XEvent event;
		
		public EventQueueEntry(MemoryRepository repo, MemoryModel model, MemoryObject object,
		        MemoryField field, XEvent event) {
			this.repo = repo;
			this.model = model;
			this.object = object;
			this.field = field;
			this.event = event;
		}
		
	}
	
	/**
	 * Suspend and resume logging.
	 * 
	 * @param logging True if events should be logged.
	 * @return True if events were logged before.
	 */
	public boolean setLogging(boolean logging) {
		
		assert !logging || this.changeLog != null;
		
		boolean oldLogging = this.logging;
		this.logging = logging;
		return oldLogging;
	}
	
	/**
	 * Remove events that cancel each other out from the queue.
	 * 
	 * @Param since Clean all events after this (value retrieved from
	 *        getNextPosition())
	 */
	public void cleanEvents(int since) {
		// TODO implement
	}
	
	/**
	 * @return the {@link MemoryChangeLog} being used for logging.
	 */
	public MemoryChangeLog getChangeLog() {
		return this.changeLog;
	}
	
}
