package org.xydra.store.impl.gae.ng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * The execution context within a transaction. This is where things change while
 * things outside this context remain stable, so that before and after-effect
 * can be calculated.
 * 
 * @author xamde
 */
public class ContextInTxn implements XStateWritableModel {
    
    private static final Logger log = LoggerFactory.getLogger(ContextInTxn.class);
    
    private ChangedModel changedModel;
    
    public XStateWritableObject createObject(XID objectId) {
        return this.changedModel.createObject(objectId);
    }
    
    public XAddress getAddress() {
        return this.changedModel.getAddress();
    }
    
    public XID getId() {
        return this.changedModel.getId();
    }
    
    public boolean hasObject(XID objectId) {
        return this.changedModel.hasObject(objectId);
    }
    
    public XStateWritableObject getObject(XID objectId) {
        return this.changedModel.getObject(objectId);
    }
    
    public boolean isEmpty() {
        return this.changedModel.isEmpty();
    }
    
    public Iterator<XID> iterator() {
        return this.changedModel.iterator();
    }
    
    public XType getType() {
        return this.changedModel.getType();
    }
    
    public boolean removeObject(XID objectId) {
        return this.changedModel.removeObject(objectId);
    }
    
    private boolean modelExists;
    
    public ContextInTxn(@NeverNull ContextBeforeCommand ctxBeforeCmd) {
        XyAssert.xyAssert(ctxBeforeCmd != null);
        assert ctxBeforeCmd != null;
        
        this.changedModel = new ChangedModel(ctxBeforeCmd);
        this.modelExists = ctxBeforeCmd.isModelExists();
        log.trace("At context creation time model '" + this.changedModel.getAddress()
                + "' exists: " + this.modelExists);
    }
    
    /**
     * @return true if there is at least one event caused by changes
     */
    public boolean hasChanges() {
        return this.changedModel.hasChanges();
    }
    
    public void setModelExists(boolean modelExists) {
        this.modelExists = modelExists;
    }
    
    public boolean isModelExists() {
        return this.modelExists;
    }
    
    public @NeverNull
    List<XAtomicEvent> toEvents(XID actorId, ContextBeforeCommand ctxBeforeCommand,
            boolean inTransaction) {
        XyAssert.xyAssert(this.getAddress() != null);
        
        /* Repository commands handled here */
        boolean existedBefore = ctxBeforeCommand.isModelExists();
        boolean existsNow = this.isModelExists();
        if(!existedBefore && existsNow) {
            // model add
            XAddress target = this.getAddress().getParent();
            XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(actorId, target, getId(),
                    ctxBeforeCommand.getRevisionNumber(), inTransaction);
            return Collections.singletonList((XAtomicEvent)event);
        } else if(existedBefore && !existsNow) {
            // model remove - implied events
            List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
            XAddress target = this.getAddress().getParent();
            boolean hadChildren = false;
            for(XID objectId : ctxBeforeCommand) {
                hadChildren = true;
                addImpliedObjectRemoveEventsAndUpdateTos(events, ctxBeforeCommand, actorId,
                        ctxBeforeCommand.getRevisionNumber(), ctxBeforeCommand.getAddress(),
                        ctxBeforeCommand.getObject(objectId));
            }
            XRepositoryEvent repositoryEvent = MemoryRepositoryEvent.createRemoveEvent(actorId,
                    target, getId(), ctxBeforeCommand.getRevisionNumber(), inTransaction
                            || hadChildren);
            events.add(repositoryEvent);
            return events;
        } else if(!existedBefore && !existsNow) {
            // no model at all
            return Collections.EMPTY_LIST;
        } else {
            // model changed
            XyAssert.xyAssert(existedBefore && existsNow);
            
            List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
            boolean inTxn = inTransaction || this.changedModel.countCommandsNeeded(2) > 1;
            DeltaUtils
                    .createEventsForChangedModel(events, actorId, this.changedModel, inTxn, false);
            return events;
        }
    }
    
    /**
     * Remove object and below
     * 
     * @param events
     * @param ctxBeforeCmd
     * @param actorId
     * @param modelRev
     * @param modelAddress
     * @param object
     */
    private static void addImpliedObjectRemoveEventsAndUpdateTos(List<XAtomicEvent> events,
            ContextBeforeCommand ctxBeforeCmd, XID actorId, long modelRev, XAddress modelAddress,
            XReadableObject object) {
        for(XID fieldId : object) {
            addImpliedFieldAndValueRemoveEventsAndUpdateTos(events, actorId, modelRev,
                    object.getRevisionNumber(), object.getField(fieldId));
        }
        XModelEvent modelEvent = MemoryModelEvent.createRemoveEvent(actorId, modelAddress,
                object.getId(), modelRev, object.getRevisionNumber(), true, true);
        events.add(modelEvent);
        // update TOS
        SimpleObject so = new SimpleObject(XX.resolveObject(modelAddress, object.getId()));
        TentativeObjectState tos = new TentativeObjectState(so, false, modelRev);
        ctxBeforeCmd.saveTentativeObjectState(tos);
    }
    
    /**
     * Remove fields and below
     * 
     * @param events
     * @param actorId
     * @param modelRev
     * @param objectRev
     * @param field
     */
    private static void addImpliedFieldAndValueRemoveEventsAndUpdateTos(List<XAtomicEvent> events,
            XID actorId, long modelRev, long objectRev, XReadableField field) {
        if(!field.isEmpty()) {
            XFieldEvent fieldEvent = MemoryFieldEvent.createRemoveEvent(actorId,
                    field.getAddress(), modelRev, objectRev, field.getRevisionNumber(), true, true);
            events.add(fieldEvent);
        }
        XObjectEvent objectEvent = MemoryObjectEvent.createRemoveEvent(actorId, field.getAddress()
                .getParent(), field.getId(), modelRev, objectRev, field.getRevisionNumber(), true,
                true);
        events.add(objectEvent);
    }
    
    public Collection<? extends XReadableObject> getAdded() {
        return this.changedModel.getAdded();
    }
    
    public Collection<XID> getRemoved() {
        return this.changedModel.getRemoved();
    }
    
    public Iterable<ChangedObject> getChanged() {
        return this.changedModel.getChangedObjects();
    }
}
