package org.xydra.core.model.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
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
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapMapSetIndex;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


/**
 * Different from a {@link ChangedModel}, this class e.g., can keep fields even
 * if its object is removed. The state is stored as an event storage.
 * 
 * Revision numbers of incoming events are used.
 * 
 * @author xamde
 * 
 *         IMPROVE Do same for objects?
 * 
 *         TODO Max fragen: Methoden zu lang?
 */
public class EventDelta {
	
	private Set<XRepositoryEvent> repoEvents = new HashSet<XRepositoryEvent>();
	private Set<XModelEvent> modelEvents = new HashSet<XModelEvent>();
	private IMapSetIndex<XId,XObjectEvent> objectEventMap = new MapSetIndex<XId,XObjectEvent>(
	        new FastEntrySetFactory<XObjectEvent>());
	private IMapMapSetIndex<XId,XId,XFieldEvent> fieldEventMap = new MapMapSetIndex<XId,XId,XFieldEvent>(
	        new FastEntrySetFactory<XFieldEvent>());
	
	/**
	 * Add event to internal state and maintain a redundancy-free state. I.e. if
	 * a to an existing event contradictory event is supposed to be added, the
	 * existing event gets removed
	 * 
	 * 
	 * @param rawEvent event that comes from the server. Expects that the latest
	 *            FieldEvent with ChangeType: Change is the newest
	 */
	public void addEvent(XEvent rawEvent) {
		XAddress changedEntityAddress = rawEvent.getChangedEntity();
		XId objectId = changedEntityAddress.getObject();
		ChangeType changeType = rawEvent.getChangeType();
		ChangeType inverseChangeType = null;
		
		if(changeType == ChangeType.ADD) {
			inverseChangeType = ChangeType.REMOVE;
		} else
			inverseChangeType = ChangeType.ADD;
		
		Iterator<? extends XEvent> alreadyIndexedEventsForAddressSet = null;
		XEvent contradictoryEvent = null;
		
		if(rawEvent instanceof XTransactionEvent) {
			XTransactionEvent transactionEvent = (XTransactionEvent)rawEvent;
			for(XAtomicEvent xAtomicEvent : transactionEvent) {
				this.addEvent(xAtomicEvent);
			}
		} else {
			
			if(rawEvent instanceof XRepositoryEvent) {
				XRepositoryEvent event = (XRepositoryEvent)rawEvent;
				alreadyIndexedEventsForAddressSet = this.repoEvents.iterator();
				contradictoryEvent = lookForContradictoryEvents(
				        event.getChangedEntity().getModel(), inverseChangeType,
				        alreadyIndexedEventsForAddressSet);
				if(contradictoryEvent == null) {
					this.repoEvents.add(event);
				}
			} else if(rawEvent instanceof XModelEvent) {
				XModelEvent event = (XModelEvent)rawEvent;
				alreadyIndexedEventsForAddressSet = this.modelEvents.iterator();
				contradictoryEvent = lookForContradictoryEvents(event.getObjectId(),
				        inverseChangeType, alreadyIndexedEventsForAddressSet);
				if(contradictoryEvent == null) {
					this.modelEvents.add(event);
				}
			}
			
			else if(rawEvent instanceof XObjectEvent) {
				XObjectEvent event = (XObjectEvent)rawEvent;
				
				alreadyIndexedEventsForAddressSet = this.objectEventMap
				        .constraintIterator(new EqualsConstraint<XId>(objectId));
				contradictoryEvent = lookForContradictoryEvents(
				        event.getChangedEntity().getField(), inverseChangeType,
				        alreadyIndexedEventsForAddressSet);
				if(contradictoryEvent == null) {
					this.objectEventMap.index(objectId, event);
				}
			} else if(rawEvent instanceof XFieldEvent) {
				XFieldEvent event = (XFieldEvent)rawEvent;
				XId fieldId = changedEntityAddress.getField();
				alreadyIndexedEventsForAddressSet = this.fieldEventMap.constraintIterator(
				        new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
				                changedEntityAddress.getField()));
				if(event.getChangeType() == ChangeType.CHANGE) {
					contradictoryEvent = inspectFieldChangeEventsAndCombineAsWellAsAdd(
					        changedEntityAddress, objectId, event);
				} else {
					contradictoryEvent = lookForContradictoryEvents(inverseChangeType,
					        alreadyIndexedEventsForAddressSet);
					XFieldEvent contradictoryFieldEvent = (XFieldEvent)contradictoryEvent;
					
					if(contradictoryFieldEvent != null) {
						/*
						 * TODO there is a thing here: if the server and the
						 * client added different values, you cannot find this
						 * out here. So you have to find out which one is newer
						 * and fire a Change Event, even though it could be
						 * unnecessary
						 */
						XFieldEvent newEvent = event;
						if(event.getRevisionNumber() < contradictoryEvent.getRevisionNumber()) {
							
							newEvent = contradictoryFieldEvent;
						}
						if(newEvent.getNewValue() != null) {
							XFieldEvent resultingEvent = MemoryFieldEvent.createChangeEvent(
							        newEvent.getActor(), newEvent.getTarget(),
							        newEvent.getNewValue(), newEvent.getOldModelRevision(),
							        newEvent.getOldObjectRevision(),
							        newEvent.getOldFieldRevision(), newEvent.inTransaction());
							this.fieldEventMap.index(objectId, fieldId, resultingEvent);
						}
					}
				}
				if(contradictoryEvent == null) {
					this.fieldEventMap.index(objectId, fieldId, event);
				}
			} else
				throw new RuntimeException("event could not be casted!");
			if(contradictoryEvent != null) {
				deleteContradictoryEvent(contradictoryEvent);
			}
		}
	}
	
	private XFieldEvent inspectFieldChangeEventsAndCombineAsWellAsAdd(
	        XAddress changedEntityAddress, XId objectId, XFieldEvent event) {
		XFieldEvent eventToBeRemoved = null;
		XFieldEvent eventToBeAdded;
		/*
		 * step 1: get existing fieldEvent with the changeType "change"
		 */
		Iterator<XFieldEvent> fieldEvents = this.fieldEventMap.constraintIterator(
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
			
			this.fieldEventMap.index(objectId, eventToBeAdded.getFieldId(), eventToBeAdded);
		}
		return eventToBeRemoved;
	}
	
	@SuppressWarnings("null")
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
	
	private void deleteContradictoryEvent(XEvent event) {
		XAddress changedEntity = event.getChangedEntity();
		if(event instanceof XRepositoryEvent) {
			this.repoEvents.remove(event);
		} else if(event instanceof XModelEvent) {
			this.modelEvents.remove(event);
		} else if(event instanceof XObjectEvent) {
			this.objectEventMap.deIndex(changedEntity.getObject(), (XObjectEvent)event);
		} else if(event instanceof XFieldEvent) {
			this.fieldEventMap.deIndex(changedEntity.getObject(), changedEntity.getField(),
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
	public void addAdverseEvent(XEvent event, long syncRevision, XChangeLog changeLog) {
		XAddress changedEntityAddress = event.getChangedEntity();
		ChangeType changeType = event.getChangeType();
		
		if(event instanceof XTransactionEvent) {
			XTransactionEvent transactionEvent = (XTransactionEvent)event;
			for(XAtomicEvent xAtomicEvent : transactionEvent) {
				this.addAdverseEvent(xAtomicEvent, syncRevision, changeLog);
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
					        syncRevision, event.inTransaction());
					break;
				default:
					break;
				}
			} else if(event instanceof XModelEvent) {
				
				switch(changeType) {
				case ADD:
					resultingEvent = MemoryModelEvent.createRemoveEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getObject(), syncRevision,
					        syncRevision, event.inTransaction(), event.isImplied());
					break;
				case REMOVE:
					resultingEvent = MemoryModelEvent.createAddEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getObject(), syncRevision,
					        event.inTransaction());
					break;
				default:
					break;
				}
				
			} else if(event instanceof XObjectEvent) {
				switch(changeType) {
				case ADD:
					resultingEvent = MemoryObjectEvent.createRemoveEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getField(), syncRevision,
					        syncRevision, event.inTransaction(), event.isImplied());
					break;
				case REMOVE:
					resultingEvent = MemoryObjectEvent.createAddEvent(event.getActor(),
					        event.getTarget(), changedEntityAddress.getField(), syncRevision,
					        event.inTransaction());
					break;
				default:
					break;
				}
				
			} else if(event instanceof XFieldEvent) {
				
				XEvent oldRevisionsEvent = changeLog.getEventAt(event.getOldFieldRevision());
				XValue oldValue = null;
				if(oldRevisionsEvent instanceof MemoryFieldEvent) {
					MemoryFieldEvent fieldEvent = (MemoryFieldEvent)oldRevisionsEvent;
					oldValue = fieldEvent.getNewValue();
				}
				
				switch(changeType) {
				case ADD:
					resultingEvent = MemoryFieldEvent.createRemoveEvent(event.getActor(),
					        event.getTarget(), syncRevision, syncRevision, syncRevision,
					        event.inTransaction(), event.isImplied());
					break;
				case REMOVE:
					if(oldValue == null) {
						throw new RuntimeException("old value for fieldChangedEvent "
						        + event.toString() + " could not be restored!");
					}
					resultingEvent = MemoryFieldEvent.createAddEvent(event.getActor(),
					        event.getTarget(), oldValue, syncRevision, syncRevision, syncRevision,
					        event.inTransaction());
					break;
				case CHANGE:
					if(oldValue == null) {
						throw new RuntimeException("old value for fieldChangedEvent "
						        + event.toString() + " could not be restored!");
					}
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
				
			} else {
				throw new RuntimeException("event could not be casted!");
			}
			this.addEvent(resultingEvent);
		}
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
	public void applyTo(XWritableModel model) {
		
		/* for all newly created objects */
		Iterator<XModelEvent> modelEventIterator = this.modelEvents.iterator();
		while(modelEventIterator.hasNext()) {
			XModelEvent currentEvent = (XModelEvent)modelEventIterator.next();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				XId objectId = currentEvent.getObjectId();
				XWritableObject existingObject = model.getObject(objectId);
				if(existingObject != null)
					throw new RuntimeException("object " + objectId + " already existed!");
				model.createObject(objectId);
			}
		}
		/* for all events concerning newly created fields: */
		Iterator<KeyEntryTuple<XId,XObjectEvent>> objectEventIterator = this.objectEventMap
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XObjectEvent currentEvent = keyEntryTuple.getSecond();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				XId fieldId = currentEvent.getChangedEntity().getField();
				XWritableField existingField = model.getObject(keyEntryTuple.getFirst()).getField(
				        fieldId);
				if(existingField != null)
					throw new RuntimeException("field " + fieldId + " already existed!");
				model.getObject(keyEntryTuple.getFirst()).createField(fieldId);
				
			}
			
		}
		
		/* for all events concerning newly created values */
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldEventIterator = this.fieldEventMap
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XFieldEvent>());
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XId fieldId = keyKeyEntryTuple.getKey2();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			XWritableField currentField = model.getObject(keyKeyEntryTuple.getKey1()).getField(
			        fieldId);
			if(currentEvent.getChangeType() == ChangeType.ADD
			        || currentEvent.getChangeType() == ChangeType.CHANGE) {
				currentField.setValue(currentEvent.getNewValue());
			} else if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				currentField.setValue(null);
			}
		}
		
		/* for all events concerning fields to be removed */
		objectEventIterator = this.objectEventMap.tupleIterator(new Wildcard<XId>(),
		        new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XEvent currentEvent = keyEntryTuple.getSecond();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				model.getObject(keyEntryTuple.getFirst()).removeField(
				        (currentEvent.getChangedEntity().getField()));
			}
			
		}
		
		/* for all events concerning objects to be removed */
		modelEventIterator = this.modelEvents.iterator();
		while(modelEventIterator.hasNext()) {
			XModelEvent currentEvent = (XModelEvent)modelEventIterator.next();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				model.removeObject(currentEvent.getObjectId());
			}
		}
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
		Iterator<KeyEntryTuple<XId,XObjectEvent>> objectEventIterator = this.objectEventMap
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XObjectEvent currentEvent = keyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireObjectEvent(modelAddress, currentEvent);
			}
		}
		
		/* remove objects */
		Iterator<XModelEvent> modelEventIterator = this.modelEvents.iterator();
		while(modelEventIterator.hasNext()) {
			XModelEvent modelEvent = (XModelEvent)modelEventIterator.next();
			if(modelEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireModelEvent(modelAddress, modelEvent);
			}
		}
		
		/* remove & add models */
		Iterator<XRepositoryEvent> repoEventIterator = this.repoEvents.iterator();
		while(repoEventIterator.hasNext()) {
			XRepositoryEvent repoEvent = (XRepositoryEvent)repoEventIterator.next();
			if(repoEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireRepositoryEvent(repositoryAddress, repoEvent);
			} else if(repoEvent.getChangeType() == ChangeType.ADD) {
				root.fireRepositoryEvent(repositoryAddress, repoEvent);
			}
		}
		
		/* add objects */
		modelEventIterator = this.modelEvents.iterator();
		while(modelEventIterator.hasNext()) {
			XModelEvent modelEvent = (XModelEvent)modelEventIterator.next();
			if(modelEvent.getChangeType() == ChangeType.ADD) {
				root.fireModelEvent(modelAddress, modelEvent);
			}
		}
		
		/* add fields */
		objectEventIterator = this.objectEventMap.tupleIterator(new Wildcard<XId>(),
		        new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XObjectEvent currentEvent = keyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				root.fireObjectEvent(modelAddress, currentEvent);
			}
		}
		
		// change values
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldEventIterator = this.fieldEventMap
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XId>(),
		                new Wildcard<XFieldEvent>());
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			root.fireFieldEvent(modelAddress, currentEvent);
		}
	}
}
