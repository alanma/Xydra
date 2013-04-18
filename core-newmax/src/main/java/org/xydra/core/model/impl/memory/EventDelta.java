package org.xydra.core.model.impl.memory;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
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
 *         TODO Max fragen: Methoden zu lang? FIXME auch für model-events?
 */
public class EventDelta {
	
	/**
	 * Map: objectID -> Map: fieldId -> Set<events>
	 */
	private IMapMapSetIndex<XId,XId,XFieldEvent> fieldEventMap = new MapMapSetIndex<XId,XId,XFieldEvent>(
	        new FastEntrySetFactory<XFieldEvent>());
	private IMapSetIndex<XId,XObjectEvent> objectEventMap = new MapSetIndex<XId,XObjectEvent>(
	        new FastEntrySetFactory<XObjectEvent>());
	
	/**
	 * Add event to internal state and maintain a redundancy-free state. If to
	 * an existing event contradictory event should be added, event gets removed
	 * 
	 * @param rawEvent event that comes from the server and could not be mapped
	 *            to any self-committed change
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
		// TODO what, if 2 change-events?
		
		Iterator<? extends XEvent> alreadyIndexedEventsForAddressSet = null;
		XEvent contradictoryEvent = null;
		if(rawEvent instanceof XObjectEvent) {
			XObjectEvent event = (XObjectEvent)rawEvent;
			
			alreadyIndexedEventsForAddressSet = this.objectEventMap
			        .constraintIterator(new EqualsConstraint<XId>(objectId));
			contradictoryEvent = lookForContradictoryEvents(inverseChangeType,
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
			
			contradictoryEvent = lookForContradictoryEvents(inverseChangeType,
			        alreadyIndexedEventsForAddressSet);
			
			if(contradictoryEvent == null) {
				this.fieldEventMap.index(objectId, fieldId, event);
				// if(!this.fieldEventMap.contains(new
				// EqualsConstraint<XId>(objectId),
				// new EqualsConstraint<XId>(fieldId),
				// new EqualsConstraint<XFieldEvent>(event))) {
				// throw new RuntimeException("IMapMapSetIndex doesn't work!");
				// }
				// TODO why does this not work?
			}
		} else
			throw new RuntimeException("event could not be casted!");
		if(contradictoryEvent != null) {
			deleteContradictoryEvent(contradictoryEvent);
		}
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
				// TODO Max fragen, was hier...
			}
		}
		
		return eventToBeRemoved;
	}
	
	private void deleteContradictoryEvent(XEvent event) {
		XAddress changedEntity = event.getChangedEntity();
		if(event instanceof XObjectEvent) {
			this.objectEventMap.deIndex(changedEntity.getObject(), (XObjectEvent)event);
		} else if(event instanceof XFieldEvent) {
			this.fieldEventMap.deIndex(changedEntity.getObject(), changedEntity.getField(),
			        (XFieldEvent)event);
		}
	}
	
	/**
	 * Add the inverse of the given event to the internal state. Mostly a not
	 * found committed change. The resulting events get the sync revision number
	 * 
	 * @param event to be inversed
	 * @param syncRevision of the last synchronized state
	 * @param changeLog of this client
	 */
	public void addInverseEvent(XEvent event, long syncRevision, XChangeLog changeLog) {
		XAddress changedEntityAddress = event.getChangedEntity();
		XId objectId = changedEntityAddress.getObject();
		ChangeType changeType = event.getChangeType();
		
		if(event instanceof XObjectEvent) {
			XObjectEvent resultingEvent = null;
			switch(changeType) {
			case ADD:
				resultingEvent = MemoryObjectEvent.createRemoveEvent(event.getActor(),
				        event.getTarget(), objectId, syncRevision, syncRevision,
				        event.inTransaction(), event.isImplied());
				break;
			case REMOVE:
				resultingEvent = MemoryObjectEvent.createAddEvent(event.getActor(),
				        event.getTarget(), objectId, syncRevision, event.inTransaction());
				break;
			default:
				break;
			}
			
			this.objectEventMap.index(objectId, resultingEvent);
		} else if(event instanceof XObjectEvent) {
			XFieldEvent resultingEvent = null;
			
			XEvent oldRevisionsEvent = changeLog.getEventAt(event.getOldFieldRevision());
			XValue oldValue = null;
			if(oldRevisionsEvent instanceof MemoryFieldEvent) {
				MemoryFieldEvent fieldEvent = (MemoryFieldEvent)oldRevisionsEvent;
				oldValue = fieldEvent.getNewValue();
			}
			
			switch(changeType) {
			case ADD:
				resultingEvent = MemoryFieldEvent.createRemoveEvent(event.getActor(),
				        event.getTarget(), syncRevision, syncRevision, event.inTransaction(),
				        event.isImplied());
				break;
			case REMOVE:
				if(oldValue == null) {
					throw new RuntimeException("old value for fieldChangedEvent "
					        + event.toString() + " could not be restored!");
				}
				resultingEvent = MemoryFieldEvent.createAddEvent(event.getActor(),
				        event.getTarget(), oldValue, syncRevision, syncRevision,
				        event.inTransaction());
				break;
			case CHANGE:
				if(oldValue == null) {
					throw new RuntimeException("old value for fieldChangedEvent "
					        + event.toString() + " could not be restored!");
				}
				resultingEvent = MemoryFieldEvent.createChangeEvent(event.getActor(),
				        event.getTarget(), oldValue, event.getOldObjectRevision(),
				        event.getOldFieldRevision(), event.inTransaction());
				break;
			default:
				break;
			}
			if(resultingEvent == null) {
				throw new RuntimeException("unable to inverse event " + event.toString());
			}
			
			this.fieldEventMap.index(objectId, changedEntityAddress.getField(), resultingEvent);
		} else
			throw new RuntimeException("event could not be casted!");
	}
	
	/**
	 * Applies the deltas to the given model without setting revision numbers.
	 * Throws runtime exceptions when encountering anomalies
	 * 
	 * @param model
	 */
	public void applyTo(XRevWritableModel model) {
		
		// TODO what do to with double entitys??? (when the model / object
		// already contained an entity...)
		
		/* for all events concerning newly created fields: */
		Iterator<KeyEntryTuple<XId,XObjectEvent>> objectEventIterator = this.objectEventMap
		        .tupleIterator(new Wildcard<XId>(), new Wildcard<XObjectEvent>());
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XEvent currentEvent = keyEntryTuple.getSecond();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				@SuppressWarnings("unused")
				XRevWritableField newField = model.getObject(keyEntryTuple.getFirst()).createField(
				        currentEvent.getChangedEntity().getField());
				
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
			XRevWritableField currentField = model.getObject(keyKeyEntryTuple.getKey1()).getField(
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
	}
	
	/**
	 * Send first removeFields, removeObject, removeModel, addModel, addObject,
	 * addField, changeField.
	 * 
	 * No txn events are sent.
	 * 
	 * @param root for sending events; if better, {@link MemoryEventBus} could
	 *            also be used
	 * @param model
	 */
	public void sendChangeEvents(Root root, XModel model) {
		
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldEventIterator = this.fieldEventMap
		        .tupleIterator(null, null, null);
		/* remove fields */
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireFieldEvent(model, currentEvent);
			}
		}
		
		/* remove fields */
		Iterator<KeyEntryTuple<XId,XObjectEvent>> objectEventIterator = this.objectEventMap
		        .tupleIterator(null, null);
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XObjectEvent currentEvent = keyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				root.fireObjectEvent(model, currentEvent);
			}
		}
		
		/* add fields */
		objectEventIterator = this.objectEventMap.tupleIterator(null, null);
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XObjectEvent currentEvent = keyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				root.fireObjectEvent(model, currentEvent);
			}
		}
		
		// change values
		fieldEventIterator = this.fieldEventMap.tupleIterator(null, null, null);
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			root.fireFieldEvent(model, currentEvent);
		}
	}
}
