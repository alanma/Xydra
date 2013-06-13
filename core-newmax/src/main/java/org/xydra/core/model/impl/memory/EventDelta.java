package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.value.XValue;
import org.xydra.core.change.RevisionConstants;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapMapSetIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Different from a {@link ChangedModel}, this class e.g., can keep fields even
 * if its object is removed. The state is stored as an event storage.
 * 
 * Revision numbers of incoming events are used.
 * 
 * @author xamde
 * 
 */
public class EventDelta {
	
	private static final Logger log = LoggerFactory.getLogger(EventDelta.class);
	
	private XRepositoryEvent repoEvent = null;
	
	/** objectId -> event */
	private Map<XId,XModelEvent> modelEvents = new HashMap<XId,XModelEvent>();
	
	/** Map: objectId -> (fieldId -> {events}) */
	private IMapMapSetIndex<XId,XId,XObjectEvent> objectEvents = new MapMapSetIndex<XId,XId,XObjectEvent>(
	        new FastEntrySetFactory<XObjectEvent>());
	
	/** Map: objectId -> (fieldId -> {events}) */
	private IMapMapSetIndex<XId,XId,XFieldEvent> fieldEvents = new MapMapSetIndex<XId,XId,XFieldEvent>(
	        new FastEntrySetFactory<XFieldEvent>());
	
	/**
	 * Set XAddress: fields, for which we have known server responses (so we
	 * know they were processed)
	 */
	private Set<XAddress> fieldsWithProcessedChanges = new HashSet<XAddress>();
	
	/** List for all local events, which have failed */
	private List<XEvent> failedLocalEvents = new ArrayList<XEvent>();
	
	private int eventCount;
	private boolean serverEventsWereAdded;
	
	/**
	 * Add event to internal state and maintain a redundancy-free state. I.e. if
	 * an event gets added that contradicts an existing event, the existing
	 * event gets removed. I.e. both events cancel each other out.
	 * 
	 * @param anyEvent event that comes from the server. Expects that the latest
	 *            FieldEvent with ChangeType=CHANGE is the newest
	 */
	public void addEvent(XEvent anyEvent) {
		addEvent(anyEvent, null);
	}
	
	private void addEvent(XEvent anyEvent, XChangeLog changeLog) {
		if(anyEvent instanceof XTransactionEvent) {
			XTransactionEvent transactionEvent = (XTransactionEvent)anyEvent;
			for(XAtomicEvent atomicEvent : transactionEvent) {
				this.addAtomicEvent(atomicEvent, changeLog);
			}
		} else {
			addAtomicEvent((XAtomicEvent)anyEvent, changeLog);
		}
	}
	
	private void addAtomicEvent(XAtomicEvent atomicEvent, XChangeLog changeLog) {
		boolean isLocalEvent = changeLog != null;
		if(atomicEvent instanceof XRepositoryEvent) {
			addRepositoryEvent((XRepositoryEvent)atomicEvent, isLocalEvent);
		} else if(atomicEvent instanceof XModelEvent) {
			addModelEvent((XModelEvent)atomicEvent, isLocalEvent);
		} else if(atomicEvent instanceof XObjectEvent) {
			addObjectEvent((XObjectEvent)atomicEvent, isLocalEvent);
		} else if(atomicEvent instanceof XFieldEvent) {
			addFieldEvent((XFieldEvent)atomicEvent, changeLog);
		} else
			throw new AssertionError("unknown event type");
	}
	
	/**
	 * Tricky part. There can be multiple local Change-Events, which would
	 * falsify the result, if they were simply added, because the resulting
	 * events from the server are always aggregated.
	 * 
	 * So now we only store the resulting server event (we know that by checking
	 * whether the sync log is existing here). Local events can now only either
	 * cancel this event out, so there would be no local change, or simply be
	 * skipped - since they get included at the server in the process of
	 * aggregation.
	 * 
	 * @param fieldEvent
	 * @param changeLog
	 */
	private void addFieldEvent(XFieldEvent fieldEvent, XChangeLog changeLog) {
		XId objectId = fieldEvent.getTarget().getObject();
		XId fieldId = fieldEvent.getTarget().getField();
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> indexedEventIt = this.fieldEvents
		        .tupleIterator(new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
		                fieldId), new Wildcard<XFieldEvent>());
		
		if(indexedEventIt.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XFieldEvent> indexedEventTuple = indexedEventIt.next();
			XFieldEvent indexedEvent = indexedEventTuple.getEntry();
			if(b_cancels_a(indexedEvent, fieldEvent, changeLog)) {
				this.eventCount--;
				this.fieldEvents.deIndex(objectId, fieldId, indexedEvent);
			} else {
				// only server events can be used here
				if(!this.serverEventsWereAdded) {
					this.fieldEvents.deIndex(objectId, fieldId, indexedEvent);
					this.fieldEvents.index(objectId, fieldId, fieldEvent);
				}
			}
		} else {
			// there were no events indexed
			
			if(!this.serverEventsWereAdded) {
				// this is a server event
				this.eventCount++;
				this.fieldEvents.index(objectId, fieldId, fieldEvent);
				this.fieldsWithProcessedChanges.add(fieldEvent.getChangedEntity());
			} else {
				// this is a local event and there was no server event added
				// before
				if(!this.fieldsWithProcessedChanges.contains(fieldEvent.getChangedEntity())) {
					this.eventCount++;
					this.fieldEvents.index(objectId, fieldId, fieldEvent);
					this.fieldsWithProcessedChanges.add(fieldEvent.getChangedEntity());
					addFailedLocalEvent(fieldEvent);
				} else {
					// there has been a server event added before
				}
			}
		}
		
		// if(event.getChangeType() == ChangeType.CHANGE) {
		// contradictoryEvent = inspectFieldChangeEventsAndCombineAsWellAsAdd(
		// changedEntityAddress, objectId, event);
		// } else {
		// contradictoryEvent = lookForContradictoryEvents(inverseChangeType,
		// alreadyIndexedEventsForAddressIt);
		// XFieldEvent contradictoryFieldEvent =
		// (XFieldEvent)contradictoryEvent;
		//
		// if(contradictoryFieldEvent != null) {
		// /*
		// * TODO there is a thing here: if the server and the client
		// * added different values, you cannot find this out here. So you
		// * have to find out which one is newer and fire a Change Event,
		// * even though it could be unnecessary
		// */
		// XFieldEvent newEvent = event;
		// if(event.getRevisionNumber() <
		// contradictoryEvent.getRevisionNumber()) {
		//
		// newEvent = contradictoryFieldEvent;
		// }
		// if(newEvent.getNewValue() != null) {
		// XFieldEvent resultingEvent = MemoryFieldEvent.createChangeEvent(
		// newEvent.getActor(), newEvent.getTarget(), newEvent.getNewValue(),
		// newEvent.getOldModelRevision(), newEvent.getOldObjectRevision(),
		// newEvent.getOldFieldRevision(), newEvent.inTransaction());
		// this.fieldEvents.index(objectId, fieldId, resultingEvent);
		// }
		// }
		// }
		// if(contradictoryEvent == null) {
		// this.fieldEvents.index(objectId, fieldId, event);
		// }
	}
	
	/**
	 * add failed local event so you can later restore the revision numbers of
	 * the whole model
	 * 
	 * @param event failed event
	 */
	private void addFailedLocalEvent(XEvent event) {
		this.failedLocalEvents.add(event);
		
	}
	
	/**
	 * look in the whole change log for the last XFieldEvent before sync
	 * revision for this address.
	 * 
	 * @param fieldsAddress
	 * @param changeLog
	 * @return
	 */
	@SuppressWarnings("null")
	private static XFieldEvent buildInvertedLastFieldEvent(XAddress fieldsAddress,
	        XChangeLog changeLog) {
		ISyncLog syncLog = null;
		if(changeLog instanceof ISyncLog) {
			syncLog = (ISyncLog)changeLog;
		} else {
			// WE NEED A SYNC LOG HERE!
			assert false;
		}
		Iterator<XEvent> iterator = syncLog.getEventsBetween(syncLog.getBaseRevisionNumber() + 1,
		        syncLog.getSynchronizedRevision());
		XFieldEvent lastEventBeforeSyncRev = null;
		while(iterator.hasNext()) {
			XEvent currentEvent = (XEvent)iterator.next();
			XAddress changedEntity = currentEvent.getChangedEntity();
			if(changedEntity.equals(fieldsAddress)) {
				if(currentEvent instanceof XFieldEvent) {
					lastEventBeforeSyncRev = (XFieldEvent)currentEvent;
				}
				
			}
		}
		ChangeType changeType = ChangeType.CHANGE;
		if(lastEventBeforeSyncRev != null)
			changeType = lastEventBeforeSyncRev.getChangeType();
		
		return (XFieldEvent)invertFieldEvent(lastEventBeforeSyncRev, changeLog, changeType);
	}
	
	private void addObjectEvent(XObjectEvent objectEvent, boolean isLocalEvent) {
		XId objectId = objectEvent.getTarget().getObject();
		XId fieldId = objectEvent.getChangedEntity().getField();
		Iterator<KeyKeyEntryTuple<XId,XId,XObjectEvent>> indexedEventIt = this.objectEvents
		        .tupleIterator(new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
		                fieldId), new Wildcard<XObjectEvent>());
		
		if(indexedEventIt.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XObjectEvent> indexedEventTuple = indexedEventIt.next();
			XObjectEvent indexedEvent = indexedEventTuple.getEntry();
			if(cancelEachOtherOut(objectEvent, indexedEvent)) {
				this.eventCount--;
				this.objectEvents.deIndex(objectId, fieldId, indexedEvent);
			} else {
				assert equalEffect(objectEvent, indexedEvent);
			}
		} else {
			this.eventCount++;
			this.objectEvents.index(objectId, fieldId, objectEvent);
			if(isLocalEvent) {
				addFailedLocalEvent(objectEvent);
			}
		}
	}
	
	private void addModelEvent(XModelEvent modelEvent, boolean isLocalEvent) {
		XModelEvent indexedEvent = this.modelEvents.get(modelEvent.getObjectId());
		if(indexedEvent == null) {
			this.eventCount++;
			this.modelEvents.put(modelEvent.getObjectId(), modelEvent);
			if(isLocalEvent) {
				addFailedLocalEvent(modelEvent);
			}
		} else if(cancelEachOtherOut(modelEvent, indexedEvent)) {
			this.eventCount--;
			this.modelEvents.remove(modelEvent.getObjectId());
		} else {
			assert equalEffect(modelEvent, indexedEvent);
		}
	}
	
	private void addRepositoryEvent(XRepositoryEvent repositoryEvent, boolean isLocalEvent) {
		XRepositoryEvent indexedEvent = this.repoEvent;
		if(indexedEvent == null) {
			this.eventCount++;
			this.repoEvent = repositoryEvent;
			if(isLocalEvent) {
				addFailedLocalEvent(repositoryEvent);
			}
		} else if(cancelEachOtherOut(repositoryEvent, indexedEvent)) {
			this.eventCount--;
			this.repoEvent = null;
		} else {
			assert equalEffect(repositoryEvent, indexedEvent);
		}
	}
	
	private static boolean equalEffect(XEvent a, XEvent b) {
		return a.getTarget().equals(b.getTarget())
		        && a.getChangedEntity().equals(b.getChangedEntity())
		        && a.getChangeType() == b.getChangeType();
	}
	
	private static boolean cancelEachOtherOut(XEvent a, XEvent b) {
		return a.getTarget().equals(b.getTarget())
		        && a.getChangedEntity().equals(b.getChangedEntity())
		        && cancelEachOtherOut(a.getChangeType(), b.getChangeType());
	}
	
	private static boolean equalEffect(XFieldEvent a, XFieldEvent b) {
		if(!equalEffect((XEvent)a, (XEvent)b)) {
			return false;
		}
		if((a.getChangeType() == ChangeType.CHANGE || a.getChangeType() == ChangeType.ADD)
		
		&&
		
		(b.getChangeType() == ChangeType.CHANGE || b.getChangeType() == ChangeType.ADD)) {
			// additional test required
			return a.getNewValue().equals(b.getNewValue());
		}
		return true;
	}
	
	/**
	 * Temporal dimension relevant.
	 * 
	 * @param a
	 * @param b
	 * @param changeLog
	 * @return
	 */
	private static boolean b_cancels_a(XFieldEvent a, XFieldEvent b, XChangeLog changeLog) {
		if(!a.getTarget().equals(b.getTarget())) {
			return false;
		}
		if(!a.getChangedEntity().equals(b.getChangedEntity())) {
			return false;
		}
		
		ChangeType ac = a.getChangeType();
		ChangeType bc = b.getChangeType();
		
		if(ac == ChangeType.ADD && bc == ChangeType.REMOVE) {
			return true;
		}
		if(ac == ChangeType.CHANGE && bc == ChangeType.REMOVE) {
			return true;
		}
		if(ac == ChangeType.CHANGE && bc == ChangeType.CHANGE || ac == ChangeType.REMOVE
		        && bc == ChangeType.ADD) {
			if(changeLog == null) {
				// we cannot compare the old values, so we're conservative
				return false;
			}
			assert changeLog != null;
			
			/*
			 * true if the old value of one event is the new value of the other
			 * one
			 */
			XValue aOldValue = getOldValue(a, changeLog);
			return b.getNewValue().equals(aOldValue);
		}
		return false;
	}
	
	private static XValue getOldValue(XFieldEvent fieldEvent, XChangeLog changeLog) {
		if(fieldEvent == null) {
			return null;
		}
		assert changeLog != null;
		long syncRev = ((ISyncLog)changeLog).getSynchronizedRevision();
		long currentFieldRev = fieldEvent.getRevisionNumber();
		
		XEvent oldEvent = fieldEvent;
		while(currentFieldRev > syncRev) {
			// so we have the latest synchronized value (which has to be before
			// or exactly the synchronized revision)
			long oldFieldRevision = oldEvent.getOldFieldRevision();
			if(oldFieldRevision == RevisionConstants.REVISION_OF_ENTITY_NOT_SET) {
				// this was an object command
				oldFieldRevision = oldEvent.getOldObjectRevision();
				oldEvent = changeLog.getEventAt(oldFieldRevision);
				// there cannot have been any field commands if there was an
				// object command later
				break;
			}
			oldEvent = changeLog.getEventAt(oldFieldRevision);
			currentFieldRev = oldEvent.getRevisionNumber();
		}
		XValue oldValue = null;
		if(oldEvent instanceof XFieldEvent) {
			XFieldEvent oldFieldEvent = (XFieldEvent)oldEvent;
			oldValue = oldFieldEvent.getNewValue();
		} else if(oldEvent instanceof XTransactionEvent) {
			XTransactionEvent oldTxnEvent = (XTransactionEvent)oldEvent;
			for(XAtomicEvent ae : oldTxnEvent) {
				if(ae.getTarget().equals(fieldEvent.getTarget())
				        && ae.getChangedEntity().equals(fieldEvent.getChangedEntity())) {
					assert ae instanceof XFieldEvent;
					XFieldEvent oldFieldEvent = (XFieldEvent)ae;
					oldValue = oldFieldEvent.getNewValue();
				}
			}
		}
		
		return oldValue;
	}
	
	private static boolean cancelEachOtherOut(ChangeType a, ChangeType b) {
		return
		
		a == ChangeType.ADD && b == ChangeType.REMOVE ||
		
		a == ChangeType.REMOVE && b == ChangeType.ADD;
	}
	
	@Deprecated
	private static XEvent lookForEventWithChangeTyoe(XId entityId, ChangeType inverseChangeType,
	        Iterator<? extends XEvent> alreadyIndexedEventsForAddressSet) {
		
		Set<XEvent> affectedEvents = new HashSet<XEvent>();
		
		while(alreadyIndexedEventsForAddressSet.hasNext()) {
			XId idToBeCompared = null;
			XEvent xEvent = (XEvent)alreadyIndexedEventsForAddressSet.next();
			XAddress changedEntityAddress = xEvent.getChangedEntity();
			if(xEvent instanceof XRepositoryEvent) {
				idToBeCompared = changedEntityAddress.getModel();
			} else if(xEvent instanceof XModelEvent) {
				idToBeCompared = changedEntityAddress.getObject();
			} else if(xEvent instanceof XObjectEvent) {
				idToBeCompared = changedEntityAddress.getField();
			}
			if(idToBeCompared.equals(entityId)) {
				affectedEvents.add(xEvent);
				break;
			}
			
		}
		
		if(affectedEvents.size() > 1)
			throw new RuntimeException("multiple events are concerned!");
		
		return lookForContradictoryEvents(inverseChangeType, affectedEvents.iterator());
	}
	
	private XFieldEvent inspectFieldChangeEventsAndCombineAsWellAsAdd(
	        XAddress changedEntityAddress, XId objectId, XFieldEvent event) {
		XFieldEvent eventToBeRemoved = null;
		XFieldEvent eventToBeAdded;
		/*
		 * step 1: get existing fieldEvent with the changeType "change"
		 */
		Iterator<XFieldEvent> fieldEvents = this.fieldEvents.constraintIterator(
		        new EqualsConstraint<XId>(objectId),
		        new EqualsConstraint<XId>(changedEntityAddress.getField()));
		while(fieldEvents.hasNext()) {
			XFieldEvent indexedFieldEvent = (XFieldEvent)fieldEvents.next();
			if(indexedFieldEvent.getChangeType() == ChangeType.CHANGE
			        && indexedFieldEvent.getFieldId().equals(event.getFieldId())) {
				eventToBeRemoved = indexedFieldEvent;
				break;
			}
		}
		
		/*
		 * step 2: check if event was found and if so build new event with old
		 * revisionNumbers and new value
		 */
		if(eventToBeRemoved == null) {
			// no events found
			eventToBeAdded = event;
		} else if(eventToBeRemoved != null) {
			// an event was found
			
			eventToBeAdded = MemoryFieldEvent.createChangeEvent(event.getActor(),
			        event.getTarget(), event.getNewValue(), eventToBeRemoved.getOldModelRevision(),
			        eventToBeRemoved.getOldObjectRevision(),
			        eventToBeRemoved.getOldFieldRevision(), false);
			
			this.fieldEvents.index(objectId, eventToBeAdded.getFieldId(), eventToBeAdded);
		}
		return eventToBeRemoved;
	}
	
	@SuppressWarnings("null")
	@Deprecated
	private static XEvent lookForContradictoryEvents(XId entityId, ChangeType inverseChangeType,
	        Iterator<? extends XEvent> alreadyIndexedEventsForAddressSet) {
		
		Set<XEvent> affectedEvents = new HashSet<XEvent>();
		
		while(alreadyIndexedEventsForAddressSet.hasNext()) {
			XId idToBeCompared = null;
			XEvent xEvent = (XEvent)alreadyIndexedEventsForAddressSet.next();
			XAddress changedEntityAddress = xEvent.getChangedEntity();
			if(xEvent instanceof XRepositoryEvent) {
				idToBeCompared = changedEntityAddress.getModel();
			} else if(xEvent instanceof XModelEvent) {
				idToBeCompared = changedEntityAddress.getObject();
			} else if(xEvent instanceof XObjectEvent) {
				idToBeCompared = changedEntityAddress.getField();
			}
			if(idToBeCompared.equals(entityId)) {
				affectedEvents.add(xEvent);
				break;
			}
			
		}
		
		if(affectedEvents.size() > 1)
			throw new RuntimeException("multiple events are concerned!");
		
		return lookForContradictoryEvents(inverseChangeType, affectedEvents.iterator());
	}
	
	/**
	 * check the internal state if contradictory event exists and if so: remove
	 * it
	 * 
	 * @param inverseChangeType
	 * @param alreadyIndexedEventsForAddressSet
	 * @return the contradictory event
	 */
	@Deprecated
	private static XEvent lookForContradictoryEvents(ChangeType inverseChangeType,
	        Iterator<? extends XEvent> alreadyIndexedEventsForAddressSet) {
		XEvent eventToBeRemoved = null;
		
		while(alreadyIndexedEventsForAddressSet.hasNext()) {
			XEvent alreadyIndexedEvent = (XEvent)alreadyIndexedEventsForAddressSet.next();
			
			if(alreadyIndexedEvent.getChangeType() == inverseChangeType) {
				eventToBeRemoved = alreadyIndexedEvent;
			} else {
				// // should not happen: throw exception
				// throw new
				// RuntimeException(alreadyIndexedEvent.getChangedEntity()
				// + " was already caused to perfom the "
				// + alreadyIndexedEvent.getChangeType().toString() +
				// "-action before!");
				
				// regular case: server accepted a change
			}
		}
		
		return eventToBeRemoved;
	}
	
	@Deprecated
	private void deleteContradictoryEvent(XEvent event) {
		XAddress changedEntity = event.getChangedEntity();
		if(event instanceof XRepositoryEvent) {
			// this.repoEvents.remove(event);
		} else if(event instanceof XModelEvent) {
			this.modelEvents.remove(event);
		} else if(event instanceof XObjectEvent) {
			// this.objectEvents.deIndex(changedEntity.getObject(),
			// (XObjectEvent)event);
		} else if(event instanceof XFieldEvent) {
			this.fieldEvents.deIndex(changedEntity.getObject(), changedEntity.getField(),
			        (XFieldEvent)event);
		}
	}
	
	/**
	 * Add the inverse of the given event to the internal state. The resulting
	 * events get the sync revision number
	 * 
	 * @param event to be inversed
	 * @param syncRevision of the last synchronized state
	 * @param changeLog of this client
	 */
	public void addInverseEvent(XEvent event, long syncRevision, XChangeLog changeLog) {
		this.serverEventsWereAdded = true;
		
		XAddress changedEntityAddress = event.getChangedEntity();
		ChangeType changeType = event.getChangeType();
		
		if(event instanceof XTransactionEvent) {
			XTransactionEvent transactionEvent = (XTransactionEvent)event;
			for(XAtomicEvent xAtomicEvent : transactionEvent) {
				this.addInverseEvent(xAtomicEvent, syncRevision, changeLog);
			}
		} else {
			XEvent resultingEvent = null;
			if(event instanceof XRepositoryEvent) {
				switch(changeType) {
				case ADD:
					resultingEvent = MemoryRepositoryEvent.createRemoveEvent(event.getActor(),
					        event.getTarget(), ((XRepositoryEvent)event).getModelId(),
					        syncRevision, event.inTransaction());
					break;
				case REMOVE:
					resultingEvent = MemoryRepositoryEvent.createAddEvent(event.getActor(),
					        event.getTarget(), ((XRepositoryEvent)event).getModelId(),
					        event.getOldModelRevision(), event.inTransaction());
					break;
				default:
					break;
				}
			} else if(event instanceof XModelEvent) {
				
				switch(changeType) {
				case ADD:
					resultingEvent = MemoryModelEvent.createRemoveEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getObject(),
					        event.getOldModelRevision(), syncRevision, event.inTransaction(),
					        event.isImplied());
					break;
				case REMOVE:
					/*
					 * this is necessary because elsewise we couldn't restore
					 * the right revision to this formerly locally deleted
					 * entity
					 */
					resultingEvent = MemoryModelEvent.createInternalAddEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getObject(),
					        event.getOldModelRevision(), event.getOldObjectRevision(),
					        event.inTransaction());
					
					break;
				default:
					break;
				}
				
			} else if(event instanceof XObjectEvent) {
				switch(changeType) {
				case ADD:
					resultingEvent = MemoryObjectEvent.createRemoveEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getField(),
					        event.getOldModelRevision(), event.getOldObjectRevision(),
					        syncRevision, event.inTransaction(), event.isImplied());
					break;
				case REMOVE:
					resultingEvent = MemoryObjectEvent.createAddEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getField(),
					        event.getOldModelRevision(), event.getOldObjectRevision(),
					        event.getOldFieldRevision(), event.inTransaction());
					break;
				default:
					break;
				}
				
			} else if(event instanceof XFieldEvent) {
				for(XEvent failedFieldEvent : this.failedLocalEvents) {
					if(failedFieldEvent.getChangedEntity().equals(event.getChangedEntity())) {
						/*
						 * skip this event: we already know that no change for
						 * this event was successful
						 */
						return;
					}
				}
				resultingEvent = invertFieldEvent(event, changeLog, changeType);
				
			} else {
				throw new RuntimeException("event could not be casted!");
			}
			this.addEvent(resultingEvent, changeLog);
		}
	}
	
	private static XEvent invertFieldEvent(XEvent event, XChangeLog changeLog, ChangeType changeType) {
		XEvent resultingEvent = null;
		XFieldEvent fieldEvent = (XFieldEvent)event;
		XValue oldValue = getOldValue(fieldEvent, changeLog);
		
		switch(changeType) {
		case ADD:
			resultingEvent = MemoryFieldEvent.createRemoveEvent(event.getActor(),
			        event.getTarget(), event.getOldModelRevision(), event.getOldObjectRevision(),
			        event.getOldFieldRevision(), event.inTransaction(), event.isImplied());
			break;
		case REMOVE:
			if(oldValue == null) {
				throw new RuntimeException("old value could not be restored for fieldChangedEvent "
				        + event.toString());
			}
			resultingEvent = MemoryFieldEvent.createAddEvent(event.getActor(), event.getTarget(),
			        oldValue, event.getOldModelRevision(), event.getOldObjectRevision(),
			        event.getOldFieldRevision(), event.inTransaction());
			break;
		case CHANGE:
			// if(oldValue == null) {
			// throw new
			// RuntimeException("old value could not be restored for fieldChangedEvent "
			// + event.toString());
			// }
			resultingEvent = MemoryFieldEvent.createChangeEvent(event.getActor(),
			        event.getTarget(), oldValue, event.getOldModelRevision(),
			        event.getOldObjectRevision(), event.getOldFieldRevision(),
			        event.inTransaction());
			break;
		default:
			break;
		}
		if(resultingEvent == null) {
			throw new RuntimeException("unable to inverse event " + event.toString());
		}
		return resultingEvent;
	}
	
	/**
	 * Applies the deltas to the given model without setting revision numbers.
	 * Throws runtime exceptions when encountering anomalies
	 * 
	 * @param model
	 * 
	 *            TODO handle peter: object was locally removed, but commit
	 *            failed - now the again added object has the wrong revision
	 *            number (since we cannot take one from the server change log)
	 */
	public void applyTo(XRevWritableModel model) {
		log.debug("Apply EventDelta=\n" + toString() + "\n***");
		
		/* for a newly created model */
		if(this.repoEvent != null) {
			if(this.repoEvent.getChangeType() == ChangeType.ADD) {
				if(model instanceof XExistsRevWritableModel) {
					XExistsRevWritableModel model2 = (XExistsRevWritableModel)model;
					model2.setExists(true);
				}
			}
		}
		
		/* for all newly created objects */
		for(XModelEvent modelEvent : this.modelEvents.values()) {
			if(modelEvent.getChangeType() == ChangeType.ADD) {
				XId objectId = modelEvent.getObjectId();
				XRevWritableObject object = model.getObject(objectId);
				if(object != null)
					throw new RuntimeException("object " + objectId + " already existed!");
				log.debug("Creating object " + objectId);
				object = model.createObject(objectId);
				object.setRevisionNumber(modelEvent.getRevisionNumber());
			}
		}
		/* for all events concerning newly created fields: */
		Iterator<KeyKeyEntryTuple<XId,XId,XObjectEvent>> objectEventIterator = this.objectEvents
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XObjectEvent> tuple = objectEventIterator.next();
			XObjectEvent currentEvent = tuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				XId objectId = tuple.getKey1();
				XId fieldId = tuple.getKey2();
				assert model.hasObject(objectId);
				assert !model.getObject(objectId).hasField(fieldId) : "field " + fieldId
				        + " already existed in " + model.getAddress() + "." + objectId;
				XRevWritableField field = model.getObject(objectId).createField(fieldId);
				field.setRevisionNumber(currentEvent.getRevisionNumber());
			}
		}
		
		/* for all events concerning newly created values */
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldEventIterator = this.fieldEvents
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XFieldEvent>());
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XId fieldId = keyKeyEntryTuple.getKey2();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			assert model != null;
			XId objectId = keyKeyEntryTuple.getKey1();
			XRevWritableObject currentObject = model.getObject(objectId);
			assert currentObject != null : "missing object '" + objectId + "' in model "
			        + model.getAddress() + " model=" + model;
			XWritableField currentField = currentObject.getField(fieldId);
			if(currentEvent.getChangeType() == ChangeType.ADD
			        || currentEvent.getChangeType() == ChangeType.CHANGE) {
				if(currentField != null) {
					currentField.setValue(currentEvent.getNewValue());
					
				}
			} else if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				currentField.setValue(null);
			}
		}
		
		/* for all events concerning fields to be removed */
		objectEventIterator = this.objectEvents.tupleIterator(new Wildcard<XId>(),
		        new Wildcard<XId>(), new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XObjectEvent> tuple = objectEventIterator.next();
			XObjectEvent currentEvent = tuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				XId objectId = tuple.getKey1();
				XId fieldId = tuple.getKey2();
				assert model.hasObject(objectId);
				assert model.getObject(objectId).hasField(fieldId) : "field " + fieldId
				        + " not existing";
				model.getObject(objectId).removeField((fieldId));
			}
		}
		
		/* for all events concerning objects to be removed */
		for(XModelEvent modelEvent : this.modelEvents.values()) {
			if(modelEvent.getChangeType() == ChangeType.REMOVE) {
				model.removeObject(modelEvent.getObjectId());
			}
		}
		
		/* for a model that is to be removed */
		if(this.repoEvent != null) {
			if(this.repoEvent.getChangeType() == ChangeType.REMOVE) {
				if(model instanceof XExistsRevWritableModel) {
					XExistsRevWritableModel model2 = (XExistsRevWritableModel)model;
					model2.setExists(false);
				}
			}
		}
		
		/* now apply revision numbers of all failed local events */
		for(int i = this.failedLocalEvents.size() - 1; i >= 0; i--) {
			long modelRevision = -999;
			long objectRevision = -999;
			long fieldRevision = -999;
			
			XEvent currentEvent = this.failedLocalEvents.get(i);
			ChangeType changeType = currentEvent.getChangeType();
			modelRevision = currentEvent.getOldModelRevision();
			
			model.setRevisionNumber(modelRevision);
			
			if(currentEvent instanceof XModelEvent) {
				if(changeType == ChangeType.ADD) {
					objectRevision = currentEvent.getOldObjectRevision();
					XModelEvent modelEvent = (XModelEvent)currentEvent;
					model.getObject(modelEvent.getObjectId()).setRevisionNumber(objectRevision);
				}
			} else if(currentEvent instanceof XObjectEvent) {
				objectRevision = currentEvent.getOldObjectRevision();
				XObjectEvent objectEvent = (XObjectEvent)currentEvent;
				model.getObject(objectEvent.getObjectId()).setRevisionNumber(objectRevision);
				
				if(changeType == ChangeType.ADD) {
					fieldRevision = currentEvent.getOldFieldRevision();
					model.getObject(objectEvent.getObjectId()).getField(objectEvent.getFieldId())
					        .setRevisionNumber(fieldRevision);
				}
			} else if(currentEvent instanceof XFieldEvent) {
				objectRevision = currentEvent.getOldObjectRevision();
				fieldRevision = currentEvent.getOldFieldRevision();
				
				XFieldEvent fieldEvent = (XFieldEvent)currentEvent;
				model.getObject(fieldEvent.getObjectId()).setRevisionNumber(objectRevision);
				model.getObject(fieldEvent.getObjectId()).getField(fieldEvent.getFieldId())
				        .setRevisionNumber(fieldRevision);
			}
		}
		
		log.debug("Done applying eventDelta to " + model.getAddress());
	}
	
	/**
	 * Send first removeFields, removeObject, removeModel, addModel, addObject,
	 * addField, changeField.
	 * 
	 * No txn events are sent.
	 * 
	 * @param root for sending events; if better, {@link MemoryEventBus} could
	 *            also be used
	 * @param modelAddress through which the events will be fired
	 * @param repositoryAddress can be null
	 */
	public void sendChangeEvents(Root root, XAddress modelAddress, XAddress repositoryAddress) {
		
		/* remove fields */
		Iterator<KeyKeyEntryTuple<XId,XId,XObjectEvent>> objectEventIterator = this.objectEvents
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XObjectEvent> tuple = objectEventIterator.next();
			XObjectEvent objectEvent = tuple.getEntry();
			if(objectEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireObjectEvent(objectEvent.getTarget(), objectEvent);
			}
		}
		
		/* remove objects */
		for(XModelEvent modelEvent : this.modelEvents.values()) {
			if(modelEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireModelEvent(modelAddress, modelEvent);
			}
		}
		
		/* remove & add models */
		if(this.repoEvent != null) {
			if(this.repoEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireRepositoryEvent(repositoryAddress, this.repoEvent);
			} else if(this.repoEvent.getChangeType() == ChangeType.ADD) {
				root.fireRepositoryEvent(repositoryAddress, this.repoEvent);
			}
		}
		
		/* add objects */
		for(XModelEvent modelEvent : this.modelEvents.values()) {
			if(modelEvent.getChangeType() == ChangeType.ADD) {
				root.fireModelEvent(modelEvent.getTarget(), modelEvent);
			}
		}
		
		/* add fields */
		objectEventIterator = this.objectEvents.tupleIterator(new Wildcard<XId>(),
		        new Wildcard<XId>(), new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XObjectEvent> tuple = objectEventIterator.next();
			XObjectEvent objectEvent = tuple.getEntry();
			if(objectEvent.getChangeType() == ChangeType.ADD) {
				root.fireObjectEvent(objectEvent.getTarget(), objectEvent);
			}
		}
		
		// change values
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldEventIterator = this.fieldEvents
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XFieldEvent>());
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XFieldEvent fieldEvent = keyKeyEntryTuple.getEntry();
			root.fireFieldEvent(fieldEvent.getTarget(), fieldEvent);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(this.repoEvent != null) {
			sb.append("Repository-EVENT ").append(this.repoEvent).append("\n");
		}
		for(XEvent e : this.modelEvents.values()) {
			sb.append("     Model-EVENT ").append(e).append("\n");
		}
		Iterator<KeyKeyEntryTuple<XId,XId,XObjectEvent>> objectEventIterator = this.objectEvents
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XObjectEvent> tuple = objectEventIterator.next();
			XObjectEvent objectEvent = tuple.getEntry();
			sb.append("    Object-EVENT ").append(objectEvent).append("\n");
		}
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldIt = this.fieldEvents.tupleIterator(
		        new Wildcard<XId>(), new Wildcard<XId>(), new Wildcard<XFieldEvent>());
		while(fieldIt.hasNext()) {
			KeyKeyEntryTuple<XId,XId,XFieldEvent> keyEntryTuple = fieldIt.next();
			sb.append("     Field-EVENT ").append(keyEntryTuple.getEntry()).append("\n");
		}
		
		return sb.toString();
	}
	
	public int getEventCount() {
		return this.eventCount;
	}
	
}
