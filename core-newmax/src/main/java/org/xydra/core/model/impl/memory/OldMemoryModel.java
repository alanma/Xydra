package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.XUndoFailedLocalChangeCallback;
import org.xydra.core.model.impl.memory.SynchronisationState.Orphans;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * The core state information is represented in two ways, which must be kept in
 * sync: (a) a {@link XRevWritableModel} representing the current snapshot, and
 * (b) a {@link MemoryEventQueue} which represents the change history. This one
 * is contained in the {@link SynchronizesChangesImpl}.
 * 
 * 
 * Update strategy:
 * 
 * State reads are handled by (a); change operations are checked on (a),
 * executed on (b) and finally materialised again in (a).
 * 
 * @author voelkel
 * @author Kaidel
 */
public class OldMemoryModel extends AbstractMOFEntity implements IMemoryModel, XModel,

IHasXAddress, IHasChangeLog, XSynchronizesChanges, XExecutesCommands,

XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,

Serializable {
    
    private static final long serialVersionUID = -2969189978307340483L;
    
    private static final Logger log = LoggerFactory.getLogger(OldMemoryModel.class);
    
    /**
     * The father-repository of this MemoryModel
     * 
     * @CanBeNull for stand-alone-models without father; if present, it's used
     *            to ensure uniqueness of modelIds
     */
    private final IMemoryRepository father;
    
    /**
     * Current state as a snapshot in augmented form.
     */
    private final transient Map<XId,IMemoryObject> loadedObjects = new HashMap<XId,IMemoryObject>();
    
    /**
     * Represents the current state as a snapshot.
     * 
     * A model with revision numbers is required to let e.g. each object know
     * its current revision number.
     */
    final XRevWritableModel state;
    
    /**
     * Changelog & Co
     */
    SynchronisationState syncState;
    
    @SuppressWarnings("unused")
    private static final void ___implement_XSynchronizesChanges__() {
    }
    
    // implement XSynchronizesChanges
    @Override
    public int countUnappliedLocalChanges() {
        return this.syncState.countUnappliedLocalChanges();
    }
    
    // implement XSynchronizesChanges
    @Override
    public XWritableChangeLog getChangeLog() {
        return this.syncState.getChangeLog();
    }
    
    // implement XSynchronizesChanges
    @Override
    public XLocalChange[] getLocalChanges() {
        return this.syncState.getLocalChanges();
    }
    
    // implement XSynchronizesChanges
    @Override
    public XId getSessionActor() {
        return this.syncState.getSessionActor();
    }
    
    // implement XSynchronizesChanges
    @Override
    public String getSessionPasswordHash() {
        return this.syncState.getSessionPassword();
    }
    
    // implement XSynchronizesChanges
    @Override
    public long getSynchronizedRevision() {
        return this.syncState.getSynchronizedRevision();
    }
    
    // implement XSynchronizesChanges
    @Override
    public void rollback(long revision) {
        this.syncState.rollback(revision);
    }
    
    // implement XSynchronizesChanges
    @Override
    public void setSessionActor(XId actorId, String passwordHash) {
        this.syncState.setSessionActor(actorId, passwordHash);
    }
    
    // implement XSynchronizesChanges
    @Override
    public boolean synchronize(XEvent[] remoteChanges) {
        return this.syncState.synchronize(remoteChanges);
    }
    
    @SuppressWarnings("unused")
    private static final void ___event_listening__() {
    }
    
    @Override
    public boolean addListenerForModelEvents(XModelEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForModelEvents(this, changeListener);
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XModelEvent XModelEvents} happening on this MemoryModel.
     * 
     * @param event The {@link XModelEvent} which will be propagated to the
     *            registered listeners.
     */
    // implement IMemoryModel
    @Override
    public void fireModelEvent(XModelEvent event) {
        this.root.fireModelEvent(this, event);
    }
    
    // implement IMemoryModel
    @Override
    public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForModelEvents(this, changeListener);
        }
    }
    
    // implement IMemoryModel
    @Override
    public void fireObjectEvent(XObjectEvent event) {
        synchronized(this.root) {
            this.root.fireObjectEvent(this, event);
        }
    }
    
    // implement IMemoryModel
    @Override
    public void fireFieldEvent(XFieldEvent event) {
        synchronized(this.root) {
            this.root.fireFieldEvent(this, event);
        }
    }
    
    // implement IMemoryModel
    @Override
    public void fireTransactionEvent(XTransactionEvent event) {
        synchronized(this.root) {
            this.root.fireTransactionEvent(this, event);
        }
    }
    
    @Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForTransactionEvents(this, changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForTransactionEvents(this, changeListener);
        }
    }
    
    @Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForFieldEvents(this, changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForFieldEvents(this, changeListener);
        }
    }
    
    @Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForObjectEvents(this, changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForObjectEvents(this, changeListener);
        }
    }
    
    @SuppressWarnings("unused")
    private static final void ___STUFF__() {
    }
    
    @Override
    public boolean isSynchronized() {
        return this.syncState.isSynchronized();
    }
    
    @Override
    public XType getType() {
        return XType.XMODEL;
    }
    
    @Override
    @ReadOperation
    public boolean equals(Object o) {
        if(!(o instanceof OldMemoryModel)) {
            return false;
        }
        
        OldMemoryModel other = (OldMemoryModel)o;
        synchronized(this.root) {
            // compare revision number, repositoryId & modelId
            return XCompareUtils.equalId(this.father, other.getFather())
                    && XCompareUtils.equalState(this.getState(), other.getState());
        }
    }
    
    /**
     * Checks whether this MemoryModel has a father-{@link XRepository} or not.
     * 
     * @return true, if this MemoryModel has a father-{@link XRepository}, false
     *         otherwise.
     */
    @ReadOperation
    private boolean hasFather() {
        return this.father != null;
    }
    
    @Override
    @ReadOperation
    public int hashCode() {
        synchronized(this.root) {
            int hashCode = getId().hashCode() + (int)getRevisionNumber();
            
            if(this.father != null) {
                hashCode += this.father.getId().hashCode();
            }
            
            return hashCode;
        }
    }
    
    @Override
    @ReadOperation
    public XAddress getAddress() {
        return this.state.getAddress();
    }
    
    @Override
    @ReadOperation
    public boolean isEmpty() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.state.isEmpty();
        }
    }
    
    @Override
    @ReadOperation
    public Iterator<XId> iterator() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.state.iterator();
        }
    }
    
    /**
     * Returns the father-{@link MemoryRepository} of this MemoryModel.
     * 
     * @return The father of this MemoryModel (may be null).
     */
    @Override
    @ReadOperation
    public IMemoryRepository getFather() {
        return this.father;
    }
    
    @Override
    @ReadOperation
    public XId getId() {
        return this.state.getId();
    }
    
    @Override
    @ReadOperation
    public String toString() {
        return this.getId() + " rev[" + this.getRevisionNumber() + "]";
    }
    
    @Override
    @ReadOperation
    public IMemoryObject getObject(@NeverNull XId objectId) {
        synchronized(this.root) {
            assertThisEntityExists();
            
            // lazy loading of MemoryObjects
            IMemoryObject object = this.loadedObjects.get(objectId);
            if(object != null) {
                return object;
            }
            
            XRevWritableObject objectState = this.state.getObject(objectId);
            if(objectState == null) {
                return null;
            }
            
            object = new MemoryObject(this, this.syncState.eventQueue, objectState);
            this.loadedObjects.put(objectId, object);
            
            return object;
        }
    }
    
    /**
     * @return the {@link XId} of the father-{@link XRepository} of this
     *         MemoryModel or null, if this object has none. An object can have
     *         a repositoryId even if it has no father - {@link XRepository}.
     */
    @ReadOperation
    protected XId getRepositoryId() {
        return getAddress().getRepository();
        // TODO was return this.father == null ? null : this.father.getId();
    }
    
    @Override
    @ReadOperation
    public XRevWritableModel createSnapshot() {
        synchronized(this.root) {
            if(exists()) {
                return XCopyUtils.createSnapshot(this.getState());
            } else {
                return null;
            }
        }
    }
    
    @Override
    @ReadOperation
    public boolean hasObject(@NeverNull XId id) {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.state.hasObject(id);
        }
    }
    
    @Override
    @ModificationOperation
    public IMemoryObject createObject(@NeverNull XId objectId) {
        XModelCommand command = MemoryModelCommand.createAddCommand(getAddress(), true, objectId);
        
        // Synchronised so that return is never null if command succeeded
        synchronized(this.root) {
            long result = executeModelCommand(command);
            IMemoryObject object = getObject(objectId);
            XyAssert.xyAssert(result == XCommand.FAILED || object != null);
            return object;
        }
    }
    
    @Override
    @ModificationOperation
    public boolean removeObject(@NeverNull XId objectId) {
        /*
         * no synchronisation necessary here (except that in
         * executeModelCommand())
         */
        XModelCommand command = MemoryModelCommand.createRemoveCommand(getAddress(),
                XCommand.FORCED, objectId);
        long result = executeModelCommand(command);
        XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
        return result != XCommand.NOCHANGE;
    }
    
    // method is responsible for updating the snapshot-like state
    @Override
    public long executeModelCommand(XModelCommand command) {
        return executeModelCommand(command, null);
    }
    
    /**
     * Deletes the state information of this MemoryModel from the currently used
     * persistence layer
     * 
     * Recursively deletes all content
     * 
     * Caller must synchronize access!
     */
    @ModificationOperation
    private void delete() {
        // delete from memory
        for(XId objectId : this) {
            IMemoryObject object = getObject(objectId);
            object.delete();
        }
        // delete from state
        for(XId objectId : this.loadedObjects.keySet()) {
            this.state.removeObject(objectId);
        }
        // clear cache
        this.loadedObjects.clear();
        // mark as deleted
        this.exists = false;
    }
    
    @ModificationOperation
    // implement IMemoryModel
    @Override
    public boolean enqueueModelRemoveEvents(XId actorId) {
        boolean inTrans = false;
        
        for(XId objectId : this) {
            IMemoryObject object = getObject(objectId);
            enqueueObjectRemoveEvents(actorId, object, true, true);
            inTrans = true;
        }
        
        XAddress repoAdrr = getAddress().getParent();
        XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actorId, repoAdrr,
                getId(), getRevisionNumber(), inTrans);
        this.syncState.eventQueue.enqueueRepositoryEvent(getFather(), event);
        return inTrans;
    }
    
    /**
     * Creates all {@link XEvent XEvents} which will represent the removal of
     * the given {@link MemoryObject}. All necessary {@link XObjectEvent
     * XObjectEvents} of the REMOVE-type will and lastly the {@link XModelEvent}
     * representing the actual removal of the {@link MemoryObject} will be
     * created to accurately represent the removal. The created {@link XEvent
     * XEvents} will then be enqueued into the {@link MemoryEventQueue} used by
     * this MemoryModel and then be propagated to the interested listeners.
     * 
     * @param actor The {@link XId} of the actor
     * @param object The {@link MemoryObject} which is to be removed (must not
     *            be null)
     * @param inTrans true, if the removal of this {@link MemoryObject} occurs
     *            during an {@link XTransaction}.
     * @param implied true if this model is also removed in the same transaction
     * @throws IllegalArgumentException if the given {@link MemoryOjbect} equals
     *             null
     */
    @ModificationOperation
    private void enqueueObjectRemoveEvents(XId actor, IMemoryObject object, boolean inTrans,
            boolean implied) {
        if(object == null) {
            throw new IllegalArgumentException("object must not be null");
        }
        
        for(XId fieldId : object) {
            XyAssert.xyAssert(inTrans);
            IMemoryField field = object.getField(fieldId);
            object.enqueueFieldRemoveEvents(actor, field, inTrans, true);
        }
        
        // add event to remove the object
        XModelEvent event = MemoryModelEvent.createRemoveEvent(actor, getAddress(), object.getId(),
                getRevisionNumber(), object.getRevisionNumber(), inTrans, implied);
        this.syncState.eventQueue.enqueueModelEvent(this, event);
    }
    
    @Override
    @ModificationOperation
    public long executeCommand(XCommand command) {
        return executeCommand(command, null);
    }
    
    /**
     * Execute command with given actor and password
     * 
     * @param command
     * @param givenActorId
     * @param givenPasswordHash
     * @param callback
     * @return ...
     */
    @ModificationOperation
    public long executeCommandWithActor(XCommand command, XId givenActorId,
            String givenPasswordHash, XLocalChangeCallback callback) {
        XUndoFailedLocalChangeCallback wrappingCallback = new XUndoFailedLocalChangeCallback(
                command, this, callback);
        if(command instanceof XRepositoryCommand) {
            return executeRepositoryCommandWithActor((XRepositoryCommand)command, givenActorId,
                    givenPasswordHash, callback);
        } else if(command instanceof XTransaction) {
            // TODO give Actor & Pwhash to each individual command
            synchronized(this.root) {
                return this.syncState.executeTransaction((XTransaction)command, wrappingCallback);
            }
        } else if(command instanceof XModelCommand) {
            return executeModelCommandWithActor((XModelCommand)command, givenActorId,
                    givenPasswordHash, wrappingCallback);
        }
        IMemoryObject object = getObject(command.getTarget().getObject());
        if(object == null) {
            return XCommand.FAILED;
        }
        return object.executeCommand(command, wrappingCallback);
    }
    
    @Override
    @ModificationOperation
    public long executeCommand(XCommand command, XLocalChangeCallback callback) {
        return executeCommandWithActor(command, getSessionActor(), getSessionPasswordHash(), callback);
    }
    
    /**
     * Removes all {@link XObject}s of this MemoryModel from the persistence
     * layer and the MemoryModel itself.
     */
    @ModificationOperation
    // implement IMemoryModel
    @Override
    public void removeInternal() {
        // all objects are already loaded for creating events
        for(IMemoryObject object : this.loadedObjects.values()) {
            object.removeInternal();
            this.state.removeObject(object.getId());
        }
        this.state.setRevisionNumber(nextRevisionNumber());
        this.loadedObjects.clear();
        this.exists = true;
    }
    
    /**
     * method is responsible for updating the snapshot-like state
     * 
     * @param command @NeverNull
     * @param callback @CanBeNull
     * @return
     */
    @ModificationOperation
    private long executeModelCommand(XModelCommand command, XLocalChangeCallback callback) {
        assert command != null;
        return executeModelCommandWithActor(command, getSessionActor(), getSessionPasswordHash(),
                callback);
    }
    
    /**
     * method is responsible for updating the snapshot-like state
     * 
     * @param command @NeverNull
     * @param callback @CanBeNull
     * @return
     */
    @ModificationOperation
    private long executeModelCommandWithActor(XModelCommand command, XId actorId,
            String passwordHash, XLocalChangeCallback callback) {
        synchronized(this.root) {
            assertThisEntityExists();
            XyAssert.xyAssert(!this.syncState.eventQueue.transactionInProgess);
            
            if(!getAddress().equals(command.getTarget())) {
                if(callback != null) {
                    callback.onFailure();
                }
                log.warn("Command target (" + command.getTarget() + ")  does not fit this model "
                        + getAddress());
                return XCommand.FAILED;
            }
            
            long oldRev = getRevisionNumber();
            
            switch(command.getChangeType()) {
            case ADD: {
                if(hasObject(command.getObjectId())) {
                    // ID already taken
                    if(command.isForced()) {
                        /*
                         * the forced event only cares about the postcondition -
                         * that there is an object with the given ID, not about
                         * that there was no such object before
                         */
                        if(callback != null) {
                            callback.onSuccess(XCommand.NOCHANGE);
                        }
                        return XCommand.NOCHANGE;
                    } else {
                        if(callback != null) {
                            callback.onFailure();
                        }
                        return XCommand.FAILED;
                    }
                }
                
                this.syncState.eventQueue.newLocalChange(command, callback);
                
                this.createObjectInternal(command.getObjectId());
            }
                break;
            case REMOVE: {
                XObject oldObject = getObject(command.getObjectId());
                
                if(oldObject == null) {
                    // ID not taken
                    if(command.isForced()) {
                        /*
                         * the forced event only cares about the postcondition -
                         * that there is no object with the given ID, not about
                         * that there was such an object before
                         */
                        if(callback != null) {
                            callback.onSuccess(XCommand.NOCHANGE);
                        }
                        return XCommand.NOCHANGE;
                    } else {
                        if(callback != null) {
                            callback.onFailure();
                        }
                        return XCommand.FAILED;
                    }
                }
                
                if(!command.isForced()
                        && oldObject.getRevisionNumber() != command.getRevisionNumber()) {
                    return XCommand.FAILED;
                }
                
                this.syncState.eventQueue.newLocalChange(command, callback);
                this.syncProvider.removeObjectInternal(command.getObjectId());
            }
                break;
            default:
                throw new IllegalArgumentException("Unknown model command type: " + command);
            }
            return oldRev + 1;
        }
    }
    
    /**
     * @param command @NeverNull
     * @param givenActorId
     * @param givenPasswordHash
     * @param callback @CanBeNull
     * @return the resulting revision number or error code
     */
    private long executeRepositoryCommandWithActor(XRepositoryCommand command, XId givenActorId,
            String givenPasswordHash, XLocalChangeCallback callback) {
        assert command != null;
        if(this.father != null && !command.getRepositoryId().equals(this.father.getId())) {
            // given repository-id are not consistent
            if(callback != null) {
                callback.onFailure();
            }
            return XCommand.FAILED;
        }
        
        switch(command.getChangeType()) {
        case ADD: {
            if(this.father != null && this.father.hasModel(getId())) {
                // model exists already with same id
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is a model with the given ID, not about that
                     * there was no such model before
                     */
                    if(callback != null) {
                        callback.onSuccess(XCommand.NOCHANGE);
                    }
                    return XCommand.NOCHANGE;
                } else {
                    if(callback != null) {
                        callback.onFailure();
                    }
                    return XCommand.FAILED;
                }
            }
            assert this.father == null || !this.father.hasModel(getId());
            
            XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(givenActorId,
                    getAddress().getParent(), getId(), this.syncState.getChangeLog()
                            .getCurrentRevisionNumber(), false);
            this.syncState.eventQueue.enqueueRepositoryEvent(getFather(), event);
            // change local rev
            this.state.setRevisionNumber(event.getRevisionNumber());
            this.setExists(true);
            
            // change father
            if(this.father != null) {
                this.father.addModel(this);
            }
            
            return getRevisionNumber();
        }
        case REMOVE: {
            assert this.father == null || this.father.hasModel(getId()) == exists();
            if(!exists()) {
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no model with the given ID, not about that
                     * there was such a model before
                     */
                    if(callback != null) {
                        callback.onSuccess(XCommand.NOCHANGE);
                    }
                    return XCommand.NOCHANGE;
                } else {
                    if(callback != null) {
                        callback.onFailure();
                    }
                    return XCommand.FAILED;
                }
            }
            // model is present, command is removeModel
            
            // check safe conditions
            if(!command.isForced() && getRevisionNumber() != command.getRevisionNumber()) {
                return XCommand.FAILED;
            }
            
            // execute!
            synchronized(this.root) {
                int since = this.syncState.eventQueue.getNextPosition();
                boolean inTrans = enqueueModelRemoveEvents(givenActorId);
                if(inTrans) {
                    this.syncState.eventQueue.createTransactionEvent(givenActorId, this, null,
                            since);
                }
                
                delete();
                
                this.syncState.eventQueue.newLocalChange(command, callback);
                this.syncState.eventQueue.sendEvents();
                this.syncState.eventQueue.setBlockSending(true);
                
                // change local rev
                this.state.setRevisionNumber(this.syncState.getChangeLog()
                        .getCurrentRevisionNumber());
                
                // change father
                if(this.father != null) {
                    this.father.removeModelInternal(getId());
                }
                
                return getRevisionNumber();
            }
        }
        default:
            throw new IllegalArgumentException("unknown command type: " + command);
        }
    }
    
    // FIXME HERE ===========================================
    void fixme_________________here() {
    }
    
    // public void createModelInternal(XId modelId, XCommand command,
    // XLocalChangeCallback callback) {
    //
    // XRepositoryEvent event =
    // MemoryRepositoryEvent.createAddEvent(this.sessionActor,
    // getAddress(), modelId);
    //
    // XRevWritableModel modelState = this.state.createModel(modelId);
    // assert modelState.getRevisionNumber() == 0;
    //
    // XChangeLogState ls = new MemoryChangeLogState(modelState.getAddress());
    // ls.setBaseRevisionNumber(-1);
    //
    // ls.appendEvent(event);
    //
    // MemoryModel model = new MemoryModel(this.sessionActor,
    // this.sessionPasswordHash, this,
    // modelState, ls);
    // XyAssert.xyAssert(model.getRevisionNumber() == 0);
    //
    // // in memory
    // this.loadedModels.put(model.getId(), model);
    //
    // boolean oldLogging = model.eventQueue.setLogging(false);
    // model.eventQueue.enqueueRepositoryEvent(this, event);
    // model.eventQueue.setLogging(oldLogging);
    //
    // XyAssert.xyAssert(model.eventQueue.getLocalChanges().isEmpty());
    // model.eventQueue.newLocalChange(command, callback);
    //
    // model.eventQueue.setSyncRevision(-1);
    //
    // model.eventQueue.sendEvents();
    //
    // }
    //
    // private void XXXexecuteCommand(XCommand command, XLocalChangeCallback
    // callback) {
    // // add or remove model?
    // if(command instanceof XRepositoryCommand) {
    // return executeRepositoryCommand((XRepositoryCommand)command, callback);
    // }
    // // if model currently exists, we just delegate to it
    // MemoryModel model = getModel(command.getTarget().getModel());
    // if(model == null) {
    // return XCommand.FAILED;
    // }
    // synchronized(model.eventQueue) {
    // if(model.isRemoved) {
    // return XCommand.FAILED;
    // }
    // XId modelActor = model.eventQueue.getActor();
    // String modelPsw = model.eventQueue.getPasswordHash();
    // model.eventQueue.setSessionActor(this.sessionActor,
    // this.sessionPasswordHash);
    //
    // long res = model.executeCommand(command, callback);
    // // FIXME model commands executed by listeners will use the
    // // repository actor
    //
    // model.eventQueue.setSessionActor(modelActor, modelPsw);
    // return res;
    // }
    // }
    
    // private synchronized long executeRepositoryCommand(XRepositoryCommand
    // command,
    // XLocalChangeCallback callback) {
    // /*
    // * find out which model should handle it, defer all execution and error
    // * checking there
    // */
    // XId repoId = command.getRepositoryId();
    // XId modelId = command.getModelId();
    // MemoryModel model = this.getModel(modelId);
    // if(model == null) {
    // // might be a create-model
    // model = MemoryModel.createNonExistantModel(this.sessionActor,
    // this.sessionPasswordHash,
    // repoId, modelId);
    // }
    // return model.executeCommandWithActor(command, this.sessionActor,
    // this.sessionPasswordHash,
    // callback);
    // }
    
    // private long removeModelInternal(MemoryModel model, XCommand command,
    // XLocalChangeCallback callback) {
    // synchronized(model.eventQueue) {
    //
    // XId modelId = model.getId();
    //
    // long rev = model.getRevisionNumber() + 1;
    //
    // int since = model.eventQueue.getNextPosition();
    // boolean inTrans = model.enqueueModelRemoveEvents(this.sessionActor);
    // if(inTrans) {
    // model.eventQueue.createTransactionEvent(this.sessionActor, model, null,
    // since);
    // }
    //
    // model.delete();
    // this.state.removeModel(modelId);
    // this.loadedModels.remove(modelId);
    //
    // model.eventQueue.newLocalChange(command, callback);
    //
    // model.eventQueue.sendEvents();
    // model.eventQueue.setBlockSending(true);
    //
    // return rev;
    // }
    // }
    
    void _______constr__________() {
    }
    
    private final boolean canBeSynced;
    
    /**
     * A model that can be synced if it has a father. Internal state is re-used
     * or a new internal state is created if none is given.
     * 
     * @param father @CanBeNull
     * @param actorId @NeverNull
     * @param passwordHash
     * @param modelAddress @NeverNull
     * @param modelState @CanBeNull If present, must have same address as
     *            modelAddress
     * @param changeLogState @CanBeNull
     * @param createModel If no modelState was given and if true, an initial
     *            create-this-model command is added
     */
    private OldMemoryModel(IMemoryRepository father, XId actorId, String passwordHash,
            XAddress modelAddress, XRevWritableModel modelState, XChangeLogState changeLogState,
            boolean createModel) {
        super(null, createModel || modelState != null);
        this.father = father;
        if(father != null) {
            assert father.getAddress().equals(modelAddress.getParent());
        }
        this.canBeSynced = this.father != null;
        assert actorId != null;
        assert modelAddress != null;
        assert modelAddress.getModel() != null;
        assert modelState == null || modelState.getAddress().equals(modelAddress);
        
        XChangeLogState usedChangeLogState;
        if(modelState == null) {
            // create a new model from scratch
            this.state = new SimpleModel(modelAddress);
            assert changeLogState == null;
            usedChangeLogState = new MemoryChangeLogState(modelAddress);
            MemoryChangeLog memoryChangeLog = new MemoryChangeLog(usedChangeLogState);
            long syncRev = -1;
            MemoryEventQueue queue = new MemoryEventQueue(actorId, passwordHash, memoryChangeLog,
                    syncRev);
            this.syncState = new SynchronisationState(this.syncProvider, queue, this, null, this);
            
            if(this.canBeSynced && createModel) {
                XAddress repoAddress = modelAddress.getParent();
                this.syncState.eventQueue.enqueueRepositoryEvent(father, MemoryRepositoryEvent
                        .createAddEvent(actorId, repoAddress, getId(), XCommand.FORCED, false));
            } else {
                usedChangeLogState.setBaseRevisionNumber(0);
            }
        } else {
            // continue the history of an existing model
            this.state = modelState;
            if(changeLogState == null) {
                usedChangeLogState = new MemoryChangeLogState(modelAddress);
                usedChangeLogState.setBaseRevisionNumber(this.state.getRevisionNumber());
            } else {
                usedChangeLogState = changeLogState;
            }
            MemoryChangeLog memoryChangeLog = new MemoryChangeLog(usedChangeLogState);
            long syncRev = this.state.getRevisionNumber();
            MemoryEventQueue queue = new MemoryEventQueue(actorId, passwordHash, memoryChangeLog,
                    syncRev);
            this.syncState = new SynchronisationState(this.syncProvider, queue, this, null, this);
        }
        assert getRevisionNumber() == getChangeLog().getCurrentRevisionNumber() : "rev="
                + getRevisionNumber() + " change.rev=" + getChangeLog().getCurrentRevisionNumber();
    }
    
    // tests
    /**
     * @param actorId
     * @param passwordHash
     * @param modelId
     */
    public OldMemoryModel(XId actorId, String passwordHash, XId modelId) {
        this(null, actorId, passwordHash, XX.resolveModel((XId)null, modelId), null, null, true);
    }
    
    // tests
    public OldMemoryModel(XId actorId, String passwordHash, XAddress modelAddress) {
        this(null, actorId, passwordHash, modelAddress, null, null, true);
    }
    
    /**
     * A model with the given initial state
     * 
     * @param father @CanBeNull
     * @param actorId @NeverNull
     * @param passwordHash
     * @param modelState @NeverNull
     * @param log @CanBeNull
     */
    public OldMemoryModel(MemoryRepository father, XId actorId, String passwordHash,
            XRevWritableModel modelState, XChangeLogState log) {
        this(father, actorId, passwordHash, modelState.getAddress(), modelState, log, true);
    }
    
    /**
     * @param actorId
     * @param passwordHash
     * @param modelState
     * @param log @CanBeNull
     */
    // de-serialisation
    public OldMemoryModel(XId actorId, String passwordHash, XRevWritableModel modelState,
            XChangeLogState log) {
        this(null, actorId, passwordHash, modelState.getAddress(), modelState, log, false);
    }
    
    // copy
    public OldMemoryModel(XId actorId, String passwordHash, XRevWritableModel modelState) {
        this(null, actorId, passwordHash, modelState.getAddress(), modelState, null, false);
    }
    
    /**
     * @param father @CanBeNull
     * @param actorId @NeverNull
     * @param passwordHash
     * @param repoId
     * @param modelId @NeverNull
     * @return a MemoryModel that represents a model that does not exist and
     *         never existed before
     */
    static OldMemoryModel createNonExistantModel(MemoryRepository father, XId actorId,
            String passwordHash, XId repoId, XId modelId) {
        assert actorId != null;
        assert modelId != null;
        
        XAddress modelAddress = XX.resolveModel(father.getId(), modelId);
        OldMemoryModel nonExistingModel = new OldMemoryModel(father, actorId, passwordHash,
                modelAddress, null, null, false);
        return nonExistingModel;
    }
    
    // /**
    // * Creates a new MemoryModel with the given {@link MemoryRepository} as
    // its
    // * father.
    // *
    // * @param actorId TODO
    // * @param father The father-{@link MemoryRepository} for this MemoryModel
    // * @param modelState The initial {@link XModelState} of this MemoryModel.
    // */
    // protected MemoryModel(XId actorId, String passwordHash, MemoryRepository
    // father,
    // XRevWritableModel modelState, XChangeLogState log) {
    // this(actorId, passwordHash, father, modelState, log,
    // modelState.getRevisionNumber(), false);
    // }
    
    // /**
    // * Creates a new MemoryModel with the given {@link MemoryRepository} as
    // its
    // * father, the sync revision can be set and optionally localChanges be
    // * rebuild in order to fully restore the state of a serialized
    // MemoryModel.
    // *
    // * @param actorId TODO
    // * @param passwordHash
    // * @param father The father-{@link MemoryRepository} for this MemoryModel
    // * @param modelState The initial {@link XChangeLogState} of this
    // * MemoryModel.
    // * @param log
    // * @param syncRev the synchronized revision of this MemoryModel
    // * @param loadLocalChanges if true, the
    // * {@link MemoryEventManager#localChanges} will be
    // * initialized/rebuild from the provided {@link XChangeLog} log.
    // */
    // private MemoryModel(XId actorId, String passwordHash, MemoryRepository
    // father,
    // XRevWritableModel modelState, XChangeLogState log, long syncRev,
    // boolean loadLocalChanges) {
    // this(actorId, passwordHash, father, modelState, log, syncRev,
    // loadLocalChanges, null);
    // }
    
    public OldMemoryModel(IMemoryRepository father, XId actorId, String passwordHash,
            XRevWritableModel modelState, XChangeLogState log, long syncRev,
            boolean loadLocalChanges, List<XCommand> localChangesAsCommands) {
        super(null, modelState != null);
        this.syncState = new SynchronisationState(this.syncProvider, new MemoryEventQueue(actorId,
                passwordHash, new MemoryChangeLog(log), syncRev), this, null, this);
        
        // FIXME BUGGY STUFF - LOOK BELOW
        
        // XyAssert.xyAssert(log != null);
        // assert log != null;
        //
        this.state = modelState;
        this.father = father;
        
        this.canBeSynced = this.father != null;
        
        //
        // if(father == null && getAddress().getRepository() != null
        // && this.syncState.eventQueue.getChangeLog() != null
        // &&
        // this.syncState.eventQueue.getChangeLog().getCurrentRevisionNumber()
        // == -1) {
        // XAddress repoAddr = getAddress().getParent();
        //
        // if(!loadLocalChanges) {
        // XCommand createCommand =
        // MemoryRepositoryCommand.createAddCommand(repoAddr, true,
        // getId());
        // this.syncState.eventQueue.newLocalChange(createCommand, null);
        // }
        //
        // XRepositoryEvent createEvent =
        // MemoryRepositoryEvent.createAddEvent(actorId, repoAddr,
        // getId());
        // this.syncState.eventQueue.enqueueRepositoryEvent(null, createEvent);
        // this.syncState.eventQueue.sendEvents();
        // }
        
        initLocalChanges(loadLocalChanges, localChangesAsCommands);
    }
    
    // /**
    // * Creates a new MemoryModel without father-{@link XRepository} but with a
    // * parent repository XId so it can be synchronized.
    // *
    // * @param actorId TODO
    // * @param passwordHash
    // * @param modelAddr The {@link XAddress} for this MemoryModel.
    // */
    // public MemoryModel(XId actorId, String passwordHash, XAddress modelAddr)
    // {
    // this(actorId, passwordHash, new SimpleModel(modelAddr));
    // }
    
    // /**
    // * Creates a new MemoryModel without father-{@link XRepository}.
    // *
    // * @param actorId TODO
    // * @param passwordHash
    // * @param modelId The {@link XId} for this MemoryModel.
    // */
    // public MemoryModel(XId actorId, String passwordHash, XId modelId) {
    // this(actorId, passwordHash, XX.toAddress(null, modelId, null, null));
    // }
    //
    // /**
    // * Creates a new MemoryModel without father-{@link XRepository}.
    // *
    // * @param actorId TODO
    // * @param passwordHash
    // * @param modelState The initial {@link XRevWritableModel} state of this
    // * MemoryModel.
    // */
    // public MemoryModel(XId actorId, String passwordHash, XRevWritableModel
    // modelState) {
    // this(actorId, passwordHash, modelState, createChangeLog(modelState));
    // }
    //
    // /**
    // * Creates a new MemoryModel without father-{@link XRepository}.
    // *
    // * @param actorId TODO
    // * @param passwordHash
    // * @param modelState The initial {@link XRevWritableModel} state of this
    // * MemoryModel.
    // * @param log
    // */
    // public MemoryModel(XId actorId, String passwordHash, XRevWritableModel
    // modelState,
    // XChangeLogState log) {
    // this(actorId, passwordHash, null, modelState, log);
    // }
    
    /**
     * @param loadLocalChanges
     * @param localChangesAsCommands
     */
    @ModificationOperation
    private void initLocalChanges(boolean loadLocalChanges, List<XCommand> localChangesAsCommands) {
        if(loadLocalChanges && localChangesAsCommands != null) {
            
            Iterator<XCommand> localCommands = localChangesAsCommands.iterator();
            
            while(localCommands.hasNext()) {
                XCommand command = localCommands.next();
                
                this.syncState.eventQueue.newLocalChange(command,
                        new XUndoFailedLocalChangeCallback(command, this, null));
            }
            
        }
        XyAssert.xyAssert(this.syncState.eventQueue.getChangeLog() == null
        
        || this.syncState.eventQueue.getChangeLog().getCurrentRevisionNumber()
        
        == getRevisionNumber(),
        
        "eventQueue.changeLog==null?" + (this.syncState.eventQueue.getChangeLog() == null)
                + "; getRevisionNumber()=" + getRevisionNumber()
                + " this.syncState.eventQueue.getChangeLog().getCurrentRevisionNumber()="
                + this.syncState.eventQueue.getChangeLog().getCurrentRevisionNumber());
    }
    
    /**
     * @param modelState
     * @return
     */
    @ModificationOperation
    private static XChangeLogState createChangeLog(XRevWritableModel modelState) {
        XChangeLogState log = new MemoryChangeLogState(modelState.getAddress());
        
        // FIXME MONKEY PATCHED
        // if(modelState.getRevisionNumber() != 0 ||
        // modelState.getAddress().getRepository() == null) {
        // // Bump the log revision.
        // log.setBaseRevisionNumber(modelState.getRevisionNumber() + 1);
        // } else {
        // XyAssert.xyAssert(log.getCurrentRevisionNumber() == -1);
        // }
        log.setBaseRevisionNumber(modelState.getRevisionNumber());
        
        return log;
    }
    
    void ___________rev_nr___() {
    }
    
    /**
     * Internal provider for a {@link SynchronisationState}, delegates all calls
     */
    private ISyncProvider syncProvider = new ISyncProvider() {
        
        @Override
        public XAddress getAddress() {
            return OldMemoryModel.this.getAddress();
        }
        
        @Override
        public void setRevisionNumberIfModel(long modelRevisionNumber) {
            OldMemoryModel.this.state.setRevisionNumber(modelRevisionNumber);
        }
        
        @Override
        @ModificationOperation
        public IMemoryObject createObjectInternal(XId objectId) {
            return OldMemoryModel.this.createObjectInternal(objectId);
        }
        
        @Override
        @ModificationOperation
        public void removeObjectInternal(XId objectId) {
            IMemoryObject object = getObject(objectId);
            XyAssert.xyAssert(object != null);
            assert object != null;
            
            boolean inTrans = OldMemoryModel.this.syncState.eventQueue.transactionInProgess;
            boolean makeTrans = !object.isEmpty();
            int since = OldMemoryModel.this.syncState.eventQueue.getNextPosition();
            enqueueObjectRemoveEvents(OldMemoryModel.this.syncState.eventQueue.getActor(), object,
                    makeTrans || inTrans, false);
            
            // remove the object
            OldMemoryModel.this.loadedObjects.remove(object.getId());
            OldMemoryModel.this.state.removeObject(object.getId());
            
            Orphans orphans = OldMemoryModel.this.syncState.eventQueue.orphans;
            if(orphans != null) {
                object.removeInternal();
            } else {
                object.delete();
            }
            
            /*
             * event propagation and revision number increasing for transactions
             * happens after all events of a transaction were successful
             */
            if(!inTrans) {
                if(makeTrans) {
                    OldMemoryModel.this.syncState.eventQueue.createTransactionEvent(
                            OldMemoryModel.this.syncState.eventQueue.getActor(),
                            OldMemoryModel.this, null, since);
                }
                incrementRevision();
                
                // propagate events
                OldMemoryModel.this.syncState.eventQueue.sendEvents();
            }
        }
        
        @Override
        public void incrementRevision() {
            OldMemoryModel.this.incrementRevision();
        }
        
        @Override
        @ReadOperation
        public XReadableModel getTransactionTarget() {
            return OldMemoryModel.this;
        }
        
        @Override
        public IMemoryObject getObject(XId objectId) {
            return OldMemoryModel.this.getObject(objectId);
        }
        
        @Override
        @ReadOperation
        public long getRevisionNumber() {
            return OldMemoryModel.this.getRevisionNumber();
        }
        
        @Override
        public long executeCommand(XCommand command) {
            return OldMemoryModel.this.executeCommand(command);
        }
        
    };
    
    private IMemoryObject createObjectInternal(XId objectId) {
        XyAssert.xyAssert(getRevisionNumber() >= 0, "modelRev=" + getRevisionNumber());
        XyAssert.xyAssert(!hasObject(objectId));
        
        boolean inTrans = OldMemoryModel.this.syncState.eventQueue.transactionInProgess;
        
        IMemoryObject object = null;
        Orphans orphans = OldMemoryModel.this.syncState.eventQueue.orphans;
        if(orphans != null) {
            object = orphans.objects.remove(objectId);
        }
        if(object == null) {
            // create in modelState
            XRevWritableObject objectState = OldMemoryModel.this.state.createObject(objectId);
            XyAssert.xyAssert(getAddress().contains(objectState.getAddress()));
            object = new MemoryObject(OldMemoryModel.this,
                    OldMemoryModel.this.syncState.eventQueue, objectState);
        } else {
            OldMemoryModel.this.state.addObject(object.getState());
        }
        // XyAssert.xyAssert(object.model.syncProvider == this);
        
        OldMemoryModel.this.loadedObjects.put(object.getId(), object);
        
        // create in modelSyncState
        XModelEvent event = MemoryModelEvent.createAddEvent(
                OldMemoryModel.this.syncState.eventQueue.getActor(), getAddress(), objectId,
                OldMemoryModel.this.syncState.getChangeLog().getCurrentRevisionNumber()
                // FIXME WAS: MemoryModel.this.nextRevisionNumber()
                , inTrans);
        OldMemoryModel.this.syncState.eventQueue.enqueueModelEvent(OldMemoryModel.this, event);
        
        /*
         * event propagation and revision number increasing happens after all
         * events were successful
         */
        if(!inTrans) {
            object.incrementRevision();
            
            // propagate events
            OldMemoryModel.this.syncState.eventQueue.sendEvents();
        }
        
        return object;
    }
    
    @Override
    @ReadOperation
    public long getRevisionNumber() {
        synchronized(this.root) {
            // assert this.state.getRevisionNumber() ==
            // this.syncState.getChangeLog()
            // .getCurrentRevisionNumber() : "stateRev=" +
            // this.state.getRevisionNumber()
            // + " syncStateRev=" +
            // this.syncState.getChangeLog().getCurrentRevisionNumber();
            return this.state.getRevisionNumber();
        }
    }
    
    private long nextRevisionNumber() {
        return OldMemoryModel.this.syncState.getChangeLog().getCurrentRevisionNumber();
    }
    
    // implements IMemoryModel
    @Override
    public XRevWritableModel getState() {
        return this.state;
    }
    
    public void incrementRevision() {
        // FIXME MONKEY
        // XyAssert.xyAssert(!MemoryModel.this.syncState.eventQueue.transactionInProgess);
        long newRevision = nextRevisionNumber();
        OldMemoryModel.this.state.setRevisionNumber(newRevision);
    }
    
    @Override
    public SynchronisationState getSyncState() {
        return this.syncState;
    }
    
    @Override
    public Object getStateLock() {
        return this.state;
    }
    
    @Override
    public LocalChanges getLocalChangesImpl() {
        return this.syncState.getLocalChangesImpl();
    }
    
    // FIXME this.state.setRevisionNumber(this.state.getRevisionNumber() + 1);
}
