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
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.impl.ISyncableState;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableObject;
import org.xydra.base.value.XValue;
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
    
    private void addEvent(XEvent anyEvent, ISyncLog syncLog) {
        if(anyEvent instanceof XTransactionEvent) {
            XTransactionEvent transactionEvent = (XTransactionEvent)anyEvent;
            for(XAtomicEvent atomicEvent : transactionEvent) {
                this.addAtomicEvent(atomicEvent, syncLog);
            }
        } else {
            addAtomicEvent((XAtomicEvent)anyEvent, syncLog);
        }
    }
    
    private void addAtomicEvent(XAtomicEvent atomicEvent, ISyncLog syncLog) {
        boolean isLocalEvent = syncLog != null;
        if(atomicEvent instanceof XRepositoryEvent) {
            addRepositoryEvent((XRepositoryEvent)atomicEvent, isLocalEvent);
        } else if(atomicEvent instanceof XModelEvent) {
            addModelEvent((XModelEvent)atomicEvent, isLocalEvent);
        } else if(atomicEvent instanceof XObjectEvent) {
            addObjectEvent((XObjectEvent)atomicEvent, isLocalEvent);
        } else if(atomicEvent instanceof XFieldEvent) {
            addFieldEvent((XFieldEvent)atomicEvent, syncLog);
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
     * @param syncLog
     */
    private void addFieldEvent(XFieldEvent fieldEvent, ISyncLog syncLog) {
        XId objectId = fieldEvent.getTarget().getObject();
        XId fieldId = fieldEvent.getTarget().getField();
        Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> indexedEventIt = this.fieldEvents
                .tupleIterator(new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
                        fieldId), new Wildcard<XFieldEvent>());
        
        if(indexedEventIt.hasNext()) {
            KeyKeyEntryTuple<XId,XId,XFieldEvent> indexedEventTuple = indexedEventIt.next();
            XFieldEvent indexedEvent = indexedEventTuple.getEntry();
            if(b_cancels_a(indexedEvent, fieldEvent, syncLog)) {
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
    
    /**
     * Temporal dimension relevant.
     * 
     * @param a
     * @param b
     * @param syncLog
     * @return
     */
    private static boolean b_cancels_a(XFieldEvent a, XFieldEvent b, ISyncLog syncLog) {
        if(!a.getTarget().equals(b.getTarget())) {
            return false;
        }
        if(!a.getChangedEntity().equals(b.getChangedEntity())) {
            return false;
        }
        
        ChangeType ac = a.getChangeType();
        ChangeType bc = b.getChangeType();
        
        if(ac == ChangeType.ADD && bc == ChangeType.REMOVE) {
            XValue removedValue = getOldValueForRemovedValue(b, syncLog);
            if(removedValue == null || a.getNewValue().equals(removedValue)) {
                return true;
            }
        }
        if(ac == ChangeType.CHANGE && bc == ChangeType.REMOVE) {
            return true;
        }
        if(ac == ChangeType.CHANGE && bc == ChangeType.CHANGE || ac == ChangeType.REMOVE
                && bc == ChangeType.ADD) {
            if(syncLog == null) {
                // we cannot compare the old values, so we're conservative
                return false;
            }
            assert syncLog != null;
            
            /*
             * true if the old value of one event is the new value of the other
             * one
             */
            XValue aOldValue = getOldValue(a, syncLog);
            return b.getNewValue().equals(aOldValue);
        }
        return false;
    }
    
    /**
     * Extracts the value a field had at the time just before the given
     * fieldEvent changed it.
     * 
     * @param fieldEvent
     * @param syncLog
     * @return
     */
    private static XValue getOldValue(XFieldEvent fieldEvent, ISyncLog syncLog) {
        if(fieldEvent == null) {
            return null;
        }
        assert syncLog != null;
        
        if(fieldEvent instanceof MemoryReversibleFieldEvent) {
            MemoryReversibleFieldEvent reversibleFieldEvent = (MemoryReversibleFieldEvent)fieldEvent;
            return reversibleFieldEvent.getOldValue();
        } else {
            /*
             * change happened after syncRev, so can be found in current local
             * syncLog
             */
            XEvent oldEvent = syncLog.getEventAt(fieldEvent.getOldFieldRevision());
            assert oldEvent.getChangeType() == ChangeType.ADD
                    || oldEvent.getChangeType() == ChangeType.CHANGE
                    || oldEvent.getChangeType() == ChangeType.TRANSACTION;
            if(oldEvent.getChangeType() == ChangeType.TRANSACTION) {
                XTransactionEvent oldTxnEvent = (XTransactionEvent)oldEvent;
                for(XAtomicEvent ae : oldTxnEvent) {
                    if(ae.getTarget().equals(fieldEvent.getTarget())
                            && ae.getChangedEntity().equals(fieldEvent.getChangedEntity())) {
                        assert ae instanceof XFieldEvent;
                        XFieldEvent oldFieldEvent = (XFieldEvent)ae;
                        return oldFieldEvent.getNewValue();
                    }
                }
                throw new AssertionError(
                        "oldEvent (a txn) did not contain a field event for given field");
            } else {
                if(oldEvent instanceof XFieldEvent) {
                    return ((XFieldEvent)oldEvent).getNewValue();
                    
                } else
                    return null;
            }
        }
    }
    
    /**
     * Extracts the value a field had at the time just before the given
     * fieldEvent changed it.
     * 
     * @param fieldEvent
     * @param syncLog
     * @return
     */
    private static XValue getOldValueForRemovedValue(XFieldEvent fieldEvent, ISyncLog syncLog) {
        if(fieldEvent == null) {
            return null;
        }
        if(syncLog == null)
            return null;
        
        if(fieldEvent instanceof MemoryReversibleFieldEvent) {
            MemoryReversibleFieldEvent reversibleFieldEvent = (MemoryReversibleFieldEvent)fieldEvent;
            return reversibleFieldEvent.getOldValue();
        } else {
            /*
             * change happened after syncRev, so can be found in current local
             * syncLog
             */
            XEvent oldEvent = syncLog.getEventAt(fieldEvent.getRevisionNumber());
            assert oldEvent.getChangeType() == ChangeType.ADD
                    || oldEvent.getChangeType() == ChangeType.CHANGE
                    || oldEvent.getChangeType() == ChangeType.TRANSACTION;
            if(oldEvent.getChangeType() == ChangeType.TRANSACTION) {
                XTransactionEvent oldTxnEvent = (XTransactionEvent)oldEvent;
                for(XAtomicEvent ae : oldTxnEvent) {
                    if(ae.getTarget().equals(fieldEvent.getTarget())
                            && ae.getChangedEntity().equals(fieldEvent.getChangedEntity())) {
                        assert ae instanceof XFieldEvent;
                        XFieldEvent oldFieldEvent = (XFieldEvent)ae;
                        return oldFieldEvent.getNewValue();
                    }
                }
                throw new AssertionError(
                        "oldEvent (a txn) did not contain a field event for given field");
            } else {
                if(oldEvent instanceof XFieldEvent) {
                    return ((XFieldEvent)oldEvent).getNewValue();
                    
                } else
                    return null;
            }
        }
    }
    
    private static boolean cancelEachOtherOut(ChangeType a, ChangeType b) {
        return
        
        a == ChangeType.ADD && b == ChangeType.REMOVE ||
        
        a == ChangeType.REMOVE && b == ChangeType.ADD;
    }
    
    /**
     * Add the inverse of the given event to the internal state. The resulting
     * events get the sync revision number
     * 
     * @param event to be inversed
     * @param syncLog of this client
     */
    public void addInverseEvent(XEvent event, ISyncLog syncLog) {
        this.serverEventsWereAdded = true;
        
        XAddress changedEntityAddress = event.getChangedEntity();
        ChangeType changeType = event.getChangeType();
        
        if(event instanceof XTransactionEvent) {
            XTransactionEvent transactionEvent = (XTransactionEvent)event;
            for(XAtomicEvent xAtomicEvent : transactionEvent) {
                this.addInverseEvent(xAtomicEvent, syncLog);
            }
        } else {
            XEvent resultingEvent = null;
            if(event instanceof XRepositoryEvent) {
                switch(changeType) {
                case ADD:
                    resultingEvent = MemoryRepositoryEvent.createRemoveEvent(event.getActor(),
                            event.getTarget(), ((XRepositoryEvent)event).getModelId(),
                            RevisionConstants.REVISION_NOT_AVAILABLE, event.inTransaction());
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
                            event.getOldModelRevision(), RevisionConstants.REVISION_NOT_AVAILABLE,
                            event.inTransaction(), event.isImplied());
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
                            RevisionConstants.REVISION_NOT_AVAILABLE, event.inTransaction(),
                            event.isImplied());
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
                resultingEvent = invertFieldEvent(event, syncLog, changeType);
                
            } else {
                throw new RuntimeException("event could not be casted!");
            }
            this.addEvent(resultingEvent, syncLog);
        }
    }
    
    private static XEvent invertFieldEvent(XEvent event, ISyncLog syncLog, ChangeType changeType) {
        XEvent resultingEvent = null;
        XFieldEvent fieldEvent = (XFieldEvent)event;
        
        switch(changeType) {
        case ADD:
            resultingEvent = MemoryFieldEvent.createRemoveEvent(event.getActor(),
                    event.getTarget(), event.getOldModelRevision(), event.getOldObjectRevision(),
                    event.getOldFieldRevision(), event.inTransaction(), event.isImplied());
            break;
        case REMOVE: {
            XValue oldValue = getOldValue(fieldEvent, syncLog);
            if(oldValue == null) {
                throw new RuntimeException("old value could not be restored for fieldChangedEvent "
                        + event.toString());
            }
            resultingEvent = MemoryFieldEvent.createAddEvent(event.getActor(), event.getTarget(),
                    oldValue, event.getOldModelRevision(), event.getOldObjectRevision(),
                    event.getOldFieldRevision(), event.inTransaction());
        }
            break;
        case CHANGE: {
            XValue oldValue = getOldValue(fieldEvent, syncLog);
            // if(oldValue == null) {
            // throw new
            // RuntimeException("old value could not be restored for fieldChangedEvent "
            // + event.toString());
            // }
            assert oldValue != null;
            resultingEvent = MemoryFieldEvent.createChangeEvent(event.getActor(),
                    event.getTarget(), oldValue, event.getOldModelRevision(),
                    event.getOldObjectRevision(), event.getOldFieldRevision(),
                    event.inTransaction());
        }
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
     * Applies the deltas to the given entity without setting revision numbers.
     * Throws runtime exceptions when encountering anomalies
     * 
     * @param syncableState
     */
    public void applyTo(ISyncableState syncableState) {
        if(syncableState instanceof XRevWritableModel) {
            applyTo((XRevWritableModel)syncableState);
        } else if(syncableState instanceof XRevWritableObject) {
            applyTo((XRevWritableObject)syncableState);
        } else {
            throw new RuntimeException("Cannot apply to instanceof of "
                    + syncableState.getClass().getName());
        }
    }
    
    /**
     * Applies the deltas to the given model without setting revision numbers.
     * Throws runtime exceptions when encountering anomalies
     * 
     * @param model
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
                XRevWritableObject object = model.getObject(objectEvent.getObjectId());
                object.setRevisionNumber(objectRevision);
                if(changeType == ChangeType.ADD) {
                    fieldRevision = currentEvent.getOldFieldRevision();
                    object.getField(objectEvent.getFieldId()).setRevisionNumber(fieldRevision);
                }
            } else if(currentEvent instanceof XFieldEvent) {
                objectRevision = currentEvent.getOldObjectRevision();
                fieldRevision = currentEvent.getOldFieldRevision();
                
                XFieldEvent fieldEvent = (XFieldEvent)currentEvent;
                XRevWritableObject object = model.getObject(fieldEvent.getObjectId());
                object.setRevisionNumber(objectRevision);
                object.getField(fieldEvent.getFieldId()).setRevisionNumber(fieldRevision);
            }
        }
        
        log.debug("Done applying eventDelta to " + model.getAddress());
    }
    
    /**
     * Applies the deltas to the given object without setting revision numbers.
     * Throws runtime exceptions when encountering anomalies
     * 
     * @param object
     */
    public void applyTo(XRevWritableObject object) {
        log.debug("Apply EventDelta=\n" + toString() + "\n***");
        
        /* newly created object */
        for(XModelEvent modelEvent : this.modelEvents.values()) {
            if(modelEvent.getChangeType() == ChangeType.ADD) {
                XId objectId = modelEvent.getObjectId();
                if(!object.getId().equals(objectId))
                    throw new IllegalStateException(
                            "EventDelta contains changes for other objects such as '" + objectId
                                    + "'");
                if(object instanceof XExistsRevWritableObject) {
                    XExistsRevWritableObject object2 = (XExistsRevWritableObject)object;
                    if(object2.exists())
                        throw new RuntimeException("object " + objectId + " already existed!");
                    log.debug("Creating object " + objectId);
                    object2.setExists(true);
                }
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
                if(!object.getId().equals(objectId))
                    throw new IllegalStateException(
                            "EventDelta contains changes for other objects such as '" + objectId
                                    + "'");
                XId fieldId = tuple.getKey2();
                XRevWritableField field = object.createField(fieldId);
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
            XId objectId = keyKeyEntryTuple.getKey1();
            if(!object.getId().equals(objectId))
                throw new IllegalStateException(
                        "EventDelta contains changes for other objects such as '" + objectId + "'");
            XWritableField currentField = object.getField(fieldId);
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
                if(!object.getId().equals(objectId))
                    throw new IllegalStateException(
                            "EventDelta contains changes for other objects such as '" + objectId
                                    + "'");
                XId fieldId = tuple.getKey2();
                object.removeField((fieldId));
            }
        }
        
        /* for all events concerning objects to be removed */
        for(XModelEvent modelEvent : this.modelEvents.values()) {
            if(modelEvent.getChangeType() == ChangeType.REMOVE) {
                XId objectId = modelEvent.getObjectId();
                if(!object.getId().equals(objectId))
                    throw new IllegalStateException(
                            "EventDelta contains changes for other objects such as '" + objectId
                                    + "'");
                if(object instanceof XExistsRevWritableObject) {
                    XExistsRevWritableObject object2 = (XExistsRevWritableObject)object;
                    if(!object2.exists())
                        throw new RuntimeException("object " + objectId + " did not exist!");
                    log.debug("Removing object " + objectId);
                    object2.setExists(false);
                }
            }
        }
        
        /* now apply revision numbers of all failed local events */
        for(int i = this.failedLocalEvents.size() - 1; i >= 0; i--) {
            long objectRevision = -999;
            long fieldRevision = -999;
            
            XEvent currentEvent = this.failedLocalEvents.get(i);
            ChangeType changeType = currentEvent.getChangeType();
            
            if(currentEvent instanceof XModelEvent) {
                if(changeType == ChangeType.ADD) {
                    objectRevision = currentEvent.getOldObjectRevision();
                    object.setRevisionNumber(objectRevision);
                }
            } else if(currentEvent instanceof XObjectEvent) {
                objectRevision = currentEvent.getOldObjectRevision();
                XObjectEvent objectEvent = (XObjectEvent)currentEvent;
                object.setRevisionNumber(objectRevision);
                
                if(changeType == ChangeType.ADD) {
                    fieldRevision = currentEvent.getOldFieldRevision();
                    object.getField(objectEvent.getFieldId()).setRevisionNumber(fieldRevision);
                }
            } else if(currentEvent instanceof XFieldEvent) {
                objectRevision = currentEvent.getOldObjectRevision();
                fieldRevision = currentEvent.getOldFieldRevision();
                
                XFieldEvent fieldEvent = (XFieldEvent)currentEvent;
                object.setRevisionNumber(objectRevision);
                object.getField(fieldEvent.getFieldId()).setRevisionNumber(fieldRevision);
            }
        }
        
        log.debug("Done applying eventDelta to " + object.getAddress());
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
