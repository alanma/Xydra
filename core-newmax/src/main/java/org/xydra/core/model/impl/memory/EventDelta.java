package org.xydra.core.model.impl.memory;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
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
	
	/**
	 * Map: objectID -> Map: fieldId -> Set<events>
	 */
	IMapMapSetIndex<XId,XId,XFieldEvent> fieldEventMap = new MapMapSetIndex<XId,XId,XFieldEvent>(
	        new FastEntrySetFactory<XFieldEvent>());
	IMapSetIndex<XId,XObjectEvent> objectEventMap = new MapSetIndex<XId,XObjectEvent>(
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
		XType changedEntityType = changedEntityAddress.getAddressedType();
		XId objectId = changedEntityAddress.getObject();
		ChangeType changeType = rawEvent.getChangeType();
		ChangeType inverseChangeType = null;
		
		if(changeType == ChangeType.ADD) {
			inverseChangeType = ChangeType.REMOVE;
		} else
			inverseChangeType = ChangeType.ADD;
		
		Iterator<? extends XEvent> alreadyIndexedEventsForAddressSet = null;
		XEvent contradictoryEvent = null;
		if(changedEntityType.equals(XType.XOBJECT)) {
			XObjectEvent event = (XObjectEvent)rawEvent;
			
			alreadyIndexedEventsForAddressSet = this.objectEventMap
			        .constraintIterator(new EqualsConstraint<XId>(objectId));
			contradictoryEvent = isContradictoryEventExistingAndDeleted(inverseChangeType,
			        alreadyIndexedEventsForAddressSet);
			if(contradictoryEvent == null) {
				this.objectEventMap.index(objectId, event);
			}
		} else if(changedEntityType.equals(XType.XFIELD)) {
			XFieldEvent event = (XFieldEvent)rawEvent;
			XId fieldId = changedEntityAddress.getField();
			alreadyIndexedEventsForAddressSet = this.fieldEventMap.constraintIterator(
			        new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
			                changedEntityAddress.getField()));
			
			contradictoryEvent = isContradictoryEventExistingAndDeleted(inverseChangeType,
			        alreadyIndexedEventsForAddressSet);
			
			if(contradictoryEvent == null) {
				this.fieldEventMap.index(objectId, fieldId, event);
			}
		}
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
	private static XEvent isContradictoryEventExistingAndDeleted(ChangeType inverseChangeType,
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
		if(changedEntity.getAddressedType() == XType.XOBJECT) {
			this.objectEventMap.deIndex(changedEntity.getObject(), (XObjectEvent)event);
		} else if(changedEntity.getAddressedType() == XType.XFIELD) {
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
		XType changedEntityType = changedEntityAddress.getAddressedType();
		ChangeType changeType = event.getChangeType();
		
		if(changedEntityType.equals(XType.XOBJECT)) {
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
		} else if(changedEntityType == XType.XFIELD) {
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
		}
	}
	
	/**
	 * Applies the deltas to the given model without setting revision numbers.
	 * Throws runtime exceptions when encountering anomalies
	 * 
	 * @param model
	 */
	public void applyTo(XRevWritableModel model) {
		
		/* for all events concerning newly created objects: */
		Iterator<KeyEntryTuple<XId,XObjectEvent>> objectEventIterator = this.objectEventMap
		        .tupleIterator(null, null);
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XEvent currentEvent = keyEntryTuple.getSecond();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				XRevWritableObject newObject = model.createObject(currentEvent.getChangedEntity()
				        .getObject());
				
				model.addObject(newObject);
			}
			
		}
		
		/* for all events concerning newly created fields */
		Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> fieldEventIterator = this.fieldEventMap
		        .tupleIterator(null, null, null);
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XId fieldId = keyKeyEntryTuple.getKey2();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			XRevWritableObject currentObject = model.getObject(keyKeyEntryTuple.getKey1());
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				currentObject.createField(fieldId);
				currentObject.getField(fieldId).setValue(currentEvent.getNewValue());
			} else if(currentEvent.getChangeType() == ChangeType.CHANGE) {
				XRevWritableField field = currentObject.getField(fieldId);
				field.setValue(currentEvent.getNewValue());
			} else if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				currentObject.removeField(fieldId);
			}
		}
		
		/* for all events concerning objects to be removed */
		objectEventIterator = this.objectEventMap.tupleIterator(null, null);
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XEvent currentEvent = keyEntryTuple.getSecond();
			if(currentEvent.getChangeType() == ChangeType.REMOVE) {
				model.removeObject(currentEvent.getChangedEntity().getObject());
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
		
		/* remove objects */
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
		
		/* add objects */
		objectEventIterator = this.objectEventMap.tupleIterator(null, null);
		while(objectEventIterator.hasNext()) {
			KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent> keyEntryTuple = (KeyEntryTuple<org.xydra.base.XId,org.xydra.base.change.XObjectEvent>)objectEventIterator
			        .next();
			XObjectEvent currentEvent = keyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				root.fireObjectEvent(model, currentEvent);
			}
		}
		
		/* add fields */
		fieldEventIterator = this.fieldEventMap.tupleIterator(null, null, null);
		while(fieldEventIterator.hasNext()) {
			KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent> keyKeyEntryTuple = (KeyKeyEntryTuple<org.xydra.base.XId,org.xydra.base.XId,org.xydra.base.change.XFieldEvent>)fieldEventIterator
			        .next();
			XFieldEvent currentEvent = keyKeyEntryTuple.getEntry();
			if(currentEvent.getChangeType() == ChangeType.ADD) {
				root.fireFieldEvent(model, currentEvent);
			}
		}
	}
}
