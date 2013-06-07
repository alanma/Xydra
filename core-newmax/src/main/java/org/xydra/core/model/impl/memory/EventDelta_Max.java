package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
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
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.core.util.DumpUtils;
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
 * Implementation idea:
 * 
 * Base case: Reverse all local events in inverse order; then apply all remote
 * events.
 * 
 * Smarter: If the same event has happened locally, do not reverse it and do not
 * apply it later.
 * 
 * Also smarter: Dont use redundant local events
 * 
 * Also smarter: If remote events cancel each other, also don't send them.
 * 
 * Order of events between entities does not matter, we can replay in this
 * order: remove value, remove field, remove object, remove model, add model,
 * add object, add field, add/change value. But we can also use the order of
 * local and remote events.
 * 
 * What is a good data structure to detect and eliminate duplicate events? What
 * uses not much memory, runs in O(n*log(n)), needs few passes over the data?
 * 
 * Given: remoteEvents as an array, localEvents as an iterator.
 * 
 * Data structure 'UniqueEntityEvents': EntityType -> ChangedEntityAddress ->
 * Event
 * 
 * (1) Add all localEvents to a UniqueEntityEvents, getting rid of duplicate
 * events per entity
 * 
 * (2) Add all remoteEvents to another UniqueEntityEvents, getting rid of
 * duplicate events per entity
 * 
 * (3) Create a third UniqueEntityEvents by merging localEvents & remoteEvents
 * on a per-entity-basis
 * 
 * (4) Retrieve merged events in remove-before-add and
 * repo-model-object-field-order.
 * 
 * Analysis: Let R be number of remote and L be number of local events.
 * 
 * Then: (1) O(L), (2) O(R), (3) O(L+R), (4) O(L+R) => 3 L + 3 R
 * 
 * @author xamde
 */
public class EventDelta_Max {
    
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
    
    private int eventCount;
    
    /**
     * Add event to internal state and maintain a redundancy-free state. I.e. if
     * an event gets added that contradicts an existing event, the existing
     * event gets removed. I.e. both events cancel each other out.
     * 
     * @param anyEvent event that comes from the server. Expects that the latest
     *            FieldEvent with ChangeType=CHANGE is the newest
     */
    public void addRemoteEvent(XEvent anyEvent) {
        assert anyEvent != null;
        addRemoteEvent(anyEvent, null);
    }
    
    private void addRemoteEvent(XEvent anyEvent, XChangeLog changeLog) {
        if(anyEvent instanceof XTransactionEvent) {
            XTransactionEvent transactionEvent = (XTransactionEvent)anyEvent;
            for(XAtomicEvent atomicEvent : transactionEvent) {
                this.addAtomicRemoteEvent(atomicEvent, changeLog);
            }
        } else {
            addAtomicRemoteEvent((XAtomicEvent)anyEvent, changeLog);
        }
    }
    
    private void addAtomicRemoteEvent(XAtomicEvent atomicEvent, XChangeLog changeLog) {
        if(atomicEvent instanceof XRepositoryEvent) {
            addRepositoryRemoteEvent((XRepositoryEvent)atomicEvent);
        } else if(atomicEvent instanceof XModelEvent) {
            addModelRemoteEvent((XModelEvent)atomicEvent);
        } else if(atomicEvent instanceof XObjectEvent) {
            addObjectRemoteEvent((XObjectEvent)atomicEvent);
        } else if(atomicEvent instanceof XFieldEvent) {
            addFieldRemoteEvent((XFieldEvent)atomicEvent);
        } else
            throw new AssertionError("unknown event type");
    }
    
    private void addAtomicLocalEvent(XAtomicEvent atomicEvent, XChangeLog changeLog) {
        if(atomicEvent instanceof XRepositoryEvent) {
            addRepositoryLocalEvent((XRepositoryEvent)atomicEvent);
        } else if(atomicEvent instanceof XModelEvent) {
            addModelLocalEvent((XModelEvent)atomicEvent);
        } else if(atomicEvent instanceof XObjectEvent) {
            addObjectLocalEvent((XObjectEvent)atomicEvent);
        } else if(atomicEvent instanceof XFieldEvent) {
            addFieldLocalEvent((XFieldEvent)atomicEvent, changeLog);
        } else
            throw new AssertionError("unknown event type");
    }
    
    private void addObjectLocalEvent(XObjectEvent objectEvent) {
        XId objectId = objectEvent.getTarget().getObject();
        XId fieldId = objectEvent.getChangedEntity().getField();
        Iterator<KeyKeyEntryTuple<XId,XId,XObjectEvent>> indexedEventIt = this.objectEvents
                .tupleIterator(new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
                        fieldId), new Wildcard<XObjectEvent>());
        if(indexedEventIt.hasNext()) {
            KeyKeyEntryTuple<XId,XId,XObjectEvent> indexedEventTuple = indexedEventIt.next();
            XObjectEvent indexedEvent = indexedEventTuple.getEntry();
            
            XObjectEvent mergedEvent = mergeWithLocalRMOEvent(indexedEvent, objectEvent);
            if(mergedEvent != indexedEvent) {
                this.objectEvents.deIndex(objectId, fieldId, indexedEvent);
                if(mergedEvent != null) {
                    this.objectEvents.index(objectId, fieldId, mergedEvent);
                } else {
                    this.eventCount--;
                }
            }
            // event count unchanged
        } else {
            this.eventCount++;
            this.objectEvents.index(objectId, fieldId, objectEvent);
        }
    }
    
    private void addModelLocalEvent(XModelEvent modelEvent) {
        assert modelEvent != null;
        XModelEvent indexedEvent = this.modelEvents.get(modelEvent.getObjectId());
        if(indexedEvent == null) {
            this.eventCount++;
            this.modelEvents.put(modelEvent.getObjectId(), modelEvent);
        } else {
            XModelEvent mergedEvent = mergeWithLocalRMOEvent(indexedEvent, modelEvent);
            if(mergedEvent != indexedEvent) {
                this.modelEvents.remove(modelEvent.getObjectId());
                if(mergedEvent != null) {
                    this.modelEvents.put(modelEvent.getObjectId(), mergedEvent);
                } else {
                    this.eventCount--;
                }
            }
            // event count unchanged
        }
    }
    
    private void addRepositoryLocalEvent(XRepositoryEvent repositoryEvent) {
        XRepositoryEvent indexedEvent = this.repoEvent;
        if(indexedEvent == null) {
            this.eventCount++;
            this.repoEvent = repositoryEvent;
        } else {
            XRepositoryEvent mergedEvent = mergeWithLocalRMOEvent(indexedEvent, repositoryEvent);
            if(mergedEvent != indexedEvent) {
                this.repoEvent = mergedEvent;
            }
            // event count unchanged
        }
    }
    
    /**
     * Merge with another repository/model/object event
     * 
     * @param a
     * @param local
     * @return the merged event or null, if they cancel each other out
     */
    private static <T extends XEvent> T mergeWithLocalRMOEvent(T a, T local) {
        assert a.getChangedEntity().equals(local.getChangedEntity());
        assert a.getTarget().getAddressedType() != XType.XFIELD;
        
        switch(a.getChangeType()) {
        case ADD:
            switch(local.getChangeType()) {
            case ADD:
                // same event
                return null;
            case REMOVE:
                return a;
            default:
                throw new AssertionError();
            }
        case REMOVE:
            switch(local.getChangeType()) {
            case ADD:
                return a;
            case REMOVE:
                // same event
                return null;
            default:
                throw new AssertionError();
            }
        default:
            throw new AssertionError();
        }
    }
    
    private void addFieldLocalEvent(XFieldEvent fieldEvent, XChangeLog changeLog) {
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
                XFieldEvent mergedEvent = mergeWithLocalFieldEvent(indexedEvent, fieldEvent,
                        changeLog);
                // newer event must be used
                this.fieldEvents.deIndex(objectId, fieldId, indexedEvent);
                if(mergedEvent != null) {
                    this.fieldEvents.index(objectId, fieldId, mergedEvent);
                } else {
                    this.eventCount--;
                }
            }
        } else {
            this.eventCount++;
            this.fieldEvents.index(objectId, fieldId, fieldEvent);
        }
    }
    
    private static XFieldEvent mergeWithLocalFieldEvent(XFieldEvent a, XFieldEvent local,
            XChangeLog changeLog) {
        assert a.getTarget().equals(local.getTarget());
        assert a.getChangedEntity().equals(local.getChangedEntity());
        
        switch(a.getChangeType()) {
        case ADD:
            switch(local.getChangeType()) {
            case ADD:
                // compare values
                if(a.getNewValue().equals(local.getNewValue())) {
                    // same event
                    return null;
                } else {
                    // merge: server-ADD-A & local-ADD-B => server-CHANGE-B-to-A
                    return MemoryFieldEvent.createChangeEvent(a.getActor(), a.getTarget(),
                            a.getNewValue(), a.getOldModelRevision(), a.getOldObjectRevision(),
                            a.getOldFieldRevision(), a.inTransaction());
                }
            case REMOVE:
                // server-ADD & local-REMOVE
                return a;
            case CHANGE:
                // server-ADD & local-CHANGE => merge to CHANGE
                return MemoryFieldEvent.createChangeEvent(a.getActor(), a.getTarget(),
                        a.getNewValue(), a.getOldModelRevision(), a.getOldObjectRevision(),
                        a.getOldFieldRevision(), a.inTransaction());
            default:
                throw new AssertionError();
            }
        case REMOVE:
            switch(local.getChangeType()) {
            case ADD:
                return a;
            case REMOVE:
                // no matter what the old values were, theyre gone now
                return null;
            case CHANGE:
                // server-REMOVE & local-CHANGE => remains remove
                return a;
            default:
                throw new AssertionError();
            }
        case CHANGE: {
            XValue aOldValue = getOldValue(a, changeLog);
            switch(local.getChangeType()) {
            case ADD:
                // remote-CHANGE & local-ADD
                // FIXME
                throw new AssertionError("can this happen?");
            case REMOVE:
                // remote-CHANGE & local-REMOVE
                // FIXME
                throw new AssertionError("can this happen?");
            case CHANGE:
                if(a.getNewValue().equals(local.getNewValue())) {
                    // same event
                    return null;
                } else {
                    return a;
                }
            default:
                throw new AssertionError();
            }
        }
        default:
            throw new AssertionError();
        }
    }
    
    private void addFieldRemoteEvent(XFieldEvent fieldEvent) {
        XId objectId = fieldEvent.getTarget().getObject();
        XId fieldId = fieldEvent.getTarget().getField();
        Iterator<KeyKeyEntryTuple<XId,XId,XFieldEvent>> indexedEventIt = this.fieldEvents
                .tupleIterator(new EqualsConstraint<XId>(objectId), new EqualsConstraint<XId>(
                        fieldId), new Wildcard<XFieldEvent>());
        if(indexedEventIt.hasNext()) {
            // update indexed event
            KeyKeyEntryTuple<XId,XId,XFieldEvent> indexedEventTuple = indexedEventIt.next();
            XFieldEvent indexedEvent = indexedEventTuple.getEntry();
            XFieldEvent mergedEvent = mergeWithRemoteFieldEvent(indexedEvent, fieldEvent);
            // newer event must be used
            this.fieldEvents.deIndex(objectId, fieldId, indexedEvent);
            if(mergedEvent != null) {
                this.fieldEvents.index(objectId, fieldId, mergedEvent);
            }
        } else {
            // just index it
            this.eventCount++;
            this.fieldEvents.index(objectId, fieldId, fieldEvent);
        }
    }
    
    private static XFieldEvent mergeWithRemoteFieldEvent(XFieldEvent a, XFieldEvent b) {
        switch(a.getChangeType()) {
        case ADD:
            switch(b.getChangeType()) {
            case ADD:
                return b;
            case CHANGE:
                return MemoryFieldEvent.createAddEvent(b.getActor(), b.getTarget(),
                        b.getNewValue(), b.getOldModelRevision(), b.getOldObjectRevision(),
                        b.getOldFieldRevision(), b.inTransaction());
            case REMOVE:
                return null;
            default:
            case TRANSACTION:
                throw new AssertionError();
            }
        case CHANGE:
            switch(b.getChangeType()) {
            case ADD:
                return MemoryFieldEvent.createChangeEvent(b.getActor(), b.getTarget(),
                        b.getNewValue(), b.getOldModelRevision(), b.getOldObjectRevision(),
                        b.getOldFieldRevision(), b.inTransaction());
            case CHANGE:
                return b;
            case REMOVE:
                return b;
            default:
            case TRANSACTION:
                throw new AssertionError();
            }
        case REMOVE:
            switch(b.getChangeType()) {
            case ADD:
                return null;
            case CHANGE:
                throw new AssertionError();
            case REMOVE:
                return b;
            default:
            case TRANSACTION:
                throw new AssertionError();
            }
        default:
        case TRANSACTION:
            throw new AssertionError();
        }
    }
    
    private void addObjectRemoteEvent(XObjectEvent objectEvent) {
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
        }
    }
    
    private void addModelRemoteEvent(XModelEvent modelEvent) {
        XModelEvent indexedEvent = this.modelEvents.get(modelEvent.getObjectId());
        if(indexedEvent == null) {
            this.eventCount++;
            this.modelEvents.put(modelEvent.getObjectId(), modelEvent);
        } else if(cancelEachOtherOut(modelEvent, indexedEvent)) {
            this.eventCount--;
            this.modelEvents.remove(modelEvent.getObjectId());
        } else {
            assert equalEffect(modelEvent, indexedEvent);
        }
    }
    
    private void addRepositoryRemoteEvent(XRepositoryEvent repositoryEvent) {
        XRepositoryEvent indexedEvent = this.repoEvent;
        if(indexedEvent == null) {
            this.eventCount++;
            this.repoEvent = repositoryEvent;
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
        assert changeLog != null;
        XEvent oldEvent = changeLog.getEventAt(fieldEvent.getOldFieldRevision());
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
    
    public void addLocalEvent(XEvent anyLocalEvent, XChangeLog changeLog) {
        assert anyLocalEvent != null;
        if(anyLocalEvent instanceof XTransactionEvent) {
            XTransactionEvent transactionEvent = (XTransactionEvent)anyLocalEvent;
            for(XAtomicEvent atomicEvent : transactionEvent) {
                this.addAtomicLocalEvent(atomicEvent, changeLog);
            }
        } else {
            addAtomicLocalEvent((XAtomicEvent)anyLocalEvent, changeLog);
        }
    }
    
    /**
     * Add the inverse of the given event to the internal state. The resulting
     * events get the sync revision number
     * 
     * @param event to be inversed
     * @param changeLog of this client
     * @return the inverse XEvent, for debugging purposes
     */
    public XEvent OLD_addLocalEvent(XEvent event, XChangeLog changeLog) {
        XAddress changedEntityAddress = event.getChangedEntity();
        ChangeType changeType = event.getChangeType();
        long syncRevision = 1000;
        if(event instanceof XTransactionEvent) {
            XTransactionEvent transactionEvent = (XTransactionEvent)event;
            for(XAtomicEvent xAtomicEvent : transactionEvent) {
                this.addLocalEvent(xAtomicEvent, changeLog);
            }
            return null;
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
                    long adaptedObjectRevisionToReachRightRevision = event.getOldObjectRevision() - 1;
                    resultingEvent = MemoryModelEvent.createInternalAddEvent(event.getActor(),
                            event.getTarget(), changedEntityAddress.getObject(),
                            adaptedObjectRevisionToReachRightRevision,
                            adaptedObjectRevisionToReachRightRevision, event.inTransaction());
                    
                    break;
                default:
                    break;
                }
                
            } else if(event instanceof XObjectEvent) {
                switch(changeType) {
                case ADD:
                    resultingEvent = MemoryObjectEvent.createRemoveEvent(event.getActor(),
                            event.getTarget(), changedEntityAddress.getField(),
                            event.getOldObjectRevision(), syncRevision, event.inTransaction(),
                            event.isImplied());
                    break;
                case REMOVE:
                    /*
                     * this is necessary because elsewise we couldn't restore
                     * the right revision to this formerly locally deleted
                     * entity
                     */
                    long adaptedObjectRevisionToReachRightRevision = event.getOldFieldRevision() - 1;
                    
                    resultingEvent = MemoryObjectEvent.createInternalAddEvent(event.getActor(),
                            event.getTarget(), changedEntityAddress.getField(),
                            adaptedObjectRevisionToReachRightRevision,
                            adaptedObjectRevisionToReachRightRevision, event.inTransaction());
                    break;
                default:
                    break;
                }
                
            } else if(event instanceof XFieldEvent) {
                XFieldEvent fieldEvent = (XFieldEvent)event;
                XValue oldValue = getOldValue(fieldEvent, changeLog);
                
                switch(changeType) {
                case ADD:
                    resultingEvent = MemoryFieldEvent.createRemoveEvent(event.getActor(),
                            event.getTarget(), event.getOldModelRevision(),
                            event.getOldObjectRevision(), event.getOldFieldRevision(),
                            event.inTransaction(), event.isImplied());
                    break;
                case REMOVE:
                    if(oldValue == null) {
                        throw new RuntimeException(
                                "old value could not be restored for fieldChangedEvent "
                                        + event.toString());
                    }
                    resultingEvent = MemoryFieldEvent.createAddEvent(event.getActor(),
                            event.getTarget(), oldValue, event.getOldModelRevision(),
                            event.getOldObjectRevision(), event.getOldFieldRevision(),
                            event.inTransaction());
                    break;
                case CHANGE:
                    if(oldValue == null) {
                        throw new RuntimeException(
                                "old value could not be restored for fieldChangedEvent "
                                        + event.toString());
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
            this.addRemoteEvent(resultingEvent, changeLog);
            return resultingEvent;
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
        
        // FIXME KILL
        log.debug("   Model=\n" + DumpUtils.toStringBuffer(model) + "\n***");
        
        /* for all newly created objects */
        for(XModelEvent modelEvent : this.modelEvents.values()) {
            assert modelEvent != null;
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
            assert currentEvent != null;
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
                // TODO revs?
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
