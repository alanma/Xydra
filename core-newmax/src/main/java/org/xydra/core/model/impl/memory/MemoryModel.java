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
public class MemoryModel extends AbstractMOFEntity implements IMemoryModel, XModel,

IHasXAddress, IHasChangeLog, XSynchronizesChanges, XExecutesCommands,

XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,

Serializable {
    
    private static final long serialVersionUID = -2969189978307340483L;
    
    private static final Logger log = LoggerFactory.getLogger(MemoryModel.class);
    
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
    @Deprecated
    SynchronisationState syncState;
    
    // implement XSynchronizesChanges
    @Override
    public int countUnappliedLocalChanges() {
        return this.getRoot().countUnappliedLocalChanges();
    }
    
    // implement XSynchronizesChanges
    @Override
    public XWritableChangeLog getChangeLog() {
        return this.getRoot().getWritableChangeLog();
    }
    
    // implement XSynchronizesChanges
    @Override
    @Deprecated
    public XLocalChange[] getLocalChanges() {
        return this.syncState.getLocalChanges();
    }
    
    // implement XSynchronizesChanges
    @Override
    public XId getSessionActor() {
        return getRoot().getSessionActor();
    }
    
    // implement XSynchronizesChanges
    @Override
    public String getSessionPasswordHash() {
        return getRoot().getSessionPasswordHash();
    }
    
    // implement XSynchronizesChanges
    @Override
    public long getSynchronizedRevision() {
        // FIXME !!!!!
        return this.syncState.getSynchronizedRevision();
    }
    
    // implement XSynchronizesChanges
    @Override
    @Deprecated
    public void rollback(long revision) {
        this.syncState.rollback(revision);
    }
    
    // implement XSynchronizesChanges
    @Override
    public void setSessionActor(XId actorId, String passwordHash) {
        getRoot().setSessionActor(actorId);
        getRoot().setSessionPasswordHash(passwordHash);
    }
    
    // implement XSynchronizesChanges
    @Override
    @Deprecated
    public boolean synchronize(XEvent[] remoteChanges) {
        return this.syncState.synchronize(remoteChanges);
    }
    
    @Override
    public boolean addListenerForModelEvents(XModelEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForModelEvents(getAddress(), changeListener);
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
        this.root.fireModelEvent(getAddress(), event);
    }
    
    // implement IMemoryModel
    @Override
    public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForModelEvents(getAddress(), changeListener);
        }
    }
    
    // implement IMemoryModel
    @Override
    public void fireObjectEvent(XObjectEvent event) {
        synchronized(this.root) {
            this.root.fireObjectEvent(getAddress(), event);
        }
    }
    
    // implement IMemoryModel
    @Override
    public void fireFieldEvent(XFieldEvent event) {
        synchronized(this.root) {
            this.root.fireFieldEvent(getAddress(), event);
        }
    }
    
    // implement IMemoryModel
    @Override
    public void fireTransactionEvent(XTransactionEvent event) {
        synchronized(this.root) {
            this.root.fireTransactionEvent(getAddress(), event);
        }
    }
    
    @Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForTransactionEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForTransactionEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForFieldEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForFieldEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(this.root) {
            return this.root.addListenerForObjectEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(this.root) {
            return this.root.removeListenerForObjectEvents(getAddress(), changeListener);
        }
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
        if(!(o instanceof MemoryModel)) {
            return false;
        }
        
        MemoryModel other = (MemoryModel)o;
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public long executeCommand(XCommand command, XLocalChangeCallback callback) {
        return executeCommandWithActor(command, getSessionActor(), getSessionPasswordHash(),
                callback);
    }
    
    /**
     * Removes all {@link XObject}s of this MemoryModel from the persistence
     * layer and the MemoryModel itself.
     */
    @ModificationOperation
    // implement IMemoryModel
    @Override
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    private MemoryModel(Root root, IMemoryRepository father, XId actorId, String passwordHash,
            XAddress modelAddress, XRevWritableModel modelState, XChangeLogState changeLogState,
            boolean createModel) {
        super(root, createModel || modelState != null);
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
    public MemoryModel(XId actorId, String passwordHash, XId modelId) {
        this(Root.createWithActor(XX.resolveModel((XId)null, modelId), actorId), null, actorId,
                passwordHash, XX.resolveModel((XId)null, modelId), null, null, true);
    }
    
    // tests
    public MemoryModel(XId actorId, String passwordHash, XAddress modelAddress) {
        this(Root.createWithActor(modelAddress, actorId), null, actorId, passwordHash,
                modelAddress, null, null, true);
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
    public MemoryModel(IMemoryRepository father, XId actorId, String passwordHash,
            XRevWritableModel modelState, XChangeLogState log) {
        this(Root.createWithActor(modelState.getAddress(), actorId), father, actorId, passwordHash,
                modelState.getAddress(), modelState, log, true);
    }
    
    /**
     * @param actorId
     * @param passwordHash
     * @param modelState
     * @param log @CanBeNull
     */
    // de-serialisation
    public MemoryModel(XId actorId, String passwordHash, XRevWritableModel modelState,
            XChangeLogState log) {
        this(Root.createWithActor(modelState.getAddress(), actorId), null, actorId, passwordHash,
                modelState.getAddress(), modelState, log, false);
    }
    
    // copy
    public MemoryModel(XId actorId, String passwordHash, XRevWritableModel modelState) {
        this(Root.createWithActor(modelState.getAddress(), actorId), null, actorId, passwordHash,
                modelState.getAddress(), modelState, null, false);
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
    static MemoryModel createNonExistantModel(IMemoryRepository father, XId actorId,
            String passwordHash, XId repoId, XId modelId) {
        assert actorId != null;
        assert modelId != null;
        
        XAddress modelAddress = XX.resolveModel(father.getId(), modelId);
        MemoryModel nonExistingModel = new MemoryModel(Root.createWithActor(modelAddress, actorId),
                father, actorId, passwordHash, modelAddress, null, null, false);
        return nonExistingModel;
    }
    
    public MemoryModel(IMemoryRepository father, XId actorId, String passwordHash,
            XRevWritableModel modelState, XChangeLogState log, long syncRev,
            boolean loadLocalChanges, List<XCommand> localChangesAsCommands) {
        super(Root.createWithActor(modelState.getAddress(), actorId), modelState != null);
        this.syncState = new SynchronisationState(this.syncProvider, new MemoryEventQueue(actorId,
                passwordHash, new MemoryChangeLog(log), syncRev), this, null, this);
        
        // FIXME BUGGY STUFF - LOOK BELOW
        
        // XyAssert.xyAssert(log != null);
        // assert log != null;
        //
        this.state = modelState;
        this.father = father;
        
        this.canBeSynced = this.father != null;
        
        initLocalChanges(loadLocalChanges, localChangesAsCommands);
    }
    
    /**
     * @param loadLocalChanges
     * @param localChangesAsCommands
     */
    @ModificationOperation
    @Deprecated
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
    @Deprecated
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
    
    /**
     * Internal provider for a {@link SynchronisationState}, delegates all calls
     */
    @Deprecated
    private ISyncProvider syncProvider = new ISyncProvider() {
        
        @Override
        public XAddress getAddress() {
            return MemoryModel.this.getAddress();
        }
        
        @Override
        public void setRevisionNumberIfModel(long modelRevisionNumber) {
            MemoryModel.this.state.setRevisionNumber(modelRevisionNumber);
        }
        
        @Override
        @ModificationOperation
        public IMemoryObject createObjectInternal(XId objectId) {
            return MemoryModel.this.createObjectInternal(objectId);
        }
        
        @Override
        @ModificationOperation
        public void removeObjectInternal(XId objectId) {
            IMemoryObject object = getObject(objectId);
            XyAssert.xyAssert(object != null);
            assert object != null;
            
            boolean inTrans = MemoryModel.this.syncState.eventQueue.transactionInProgess;
            boolean makeTrans = !object.isEmpty();
            int since = MemoryModel.this.syncState.eventQueue.getNextPosition();
            enqueueObjectRemoveEvents(MemoryModel.this.syncState.eventQueue.getActor(), object,
                    makeTrans || inTrans, false);
            
            // remove the object
            MemoryModel.this.loadedObjects.remove(object.getId());
            MemoryModel.this.state.removeObject(object.getId());
            
            Orphans orphans = MemoryModel.this.syncState.eventQueue.orphans;
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
                    MemoryModel.this.syncState.eventQueue.createTransactionEvent(
                            MemoryModel.this.syncState.eventQueue.getActor(), MemoryModel.this,
                            null, since);
                }
                incrementRevision();
                
                // propagate events
                MemoryModel.this.syncState.eventQueue.sendEvents();
            }
        }
        
        @Override
        public void incrementRevision() {
            MemoryModel.this.incrementRevision();
        }
        
        @Override
        @ReadOperation
        public XReadableModel getTransactionTarget() {
            return MemoryModel.this;
        }
        
        @Override
        public IMemoryObject getObject(XId objectId) {
            return MemoryModel.this.getObject(objectId);
        }
        
        @Override
        @ReadOperation
        public long getRevisionNumber() {
            return MemoryModel.this.getRevisionNumber();
        }
        
        @Override
        public long executeCommand(XCommand command) {
            return MemoryModel.this.executeCommand(command);
        }
        
    };
    
    @Deprecated
    private IMemoryObject createObjectInternal(XId objectId) {
        XyAssert.xyAssert(getRevisionNumber() >= 0, "modelRev=" + getRevisionNumber());
        XyAssert.xyAssert(!hasObject(objectId));
        
        boolean inTrans = MemoryModel.this.syncState.eventQueue.transactionInProgess;
        
        IMemoryObject object = null;
        Orphans orphans = MemoryModel.this.syncState.eventQueue.orphans;
        if(orphans != null) {
            object = orphans.objects.remove(objectId);
        }
        if(object == null) {
            // create in modelState
            XRevWritableObject objectState = MemoryModel.this.state.createObject(objectId);
            XyAssert.xyAssert(getAddress().contains(objectState.getAddress()));
            object = new MemoryObject(MemoryModel.this, MemoryModel.this.syncState.eventQueue,
                    objectState);
        } else {
            MemoryModel.this.state.addObject(object.getState());
        }
        // XyAssert.xyAssert(object.model.syncProvider == this);
        
        MemoryModel.this.loadedObjects.put(object.getId(), object);
        
        // create in modelSyncState
        XModelEvent event = MemoryModelEvent.createAddEvent(
                MemoryModel.this.syncState.eventQueue.getActor(), getAddress(), objectId,
                MemoryModel.this.syncState.getChangeLog().getCurrentRevisionNumber()
                // FIXME WAS: MemoryModel.this.nextRevisionNumber()
                , inTrans);
        MemoryModel.this.syncState.eventQueue.enqueueModelEvent(MemoryModel.this, event);
        
        /*
         * event propagation and revision number increasing happens after all
         * events were successful
         */
        if(!inTrans) {
            object.incrementRevision();
            
            // propagate events
            MemoryModel.this.syncState.eventQueue.sendEvents();
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
    
    @Deprecated
    private long nextRevisionNumber() {
        return MemoryModel.this.syncState.getChangeLog().getCurrentRevisionNumber();
    }
    
    // implements IMemoryModel
    @Override
    public XRevWritableModel getState() {
        return this.state;
    }
    
    @Deprecated
    public void incrementRevision() {
        // FIXME MONKEY
        // XyAssert.xyAssert(!MemoryModel.this.syncState.eventQueue.transactionInProgess);
        long newRevision = nextRevisionNumber();
        MemoryModel.this.state.setRevisionNumber(newRevision);
    }
    
    @Override
    @Deprecated
    public SynchronisationState getSyncState() {
        return this.syncState;
    }
    
    @Override
    @Deprecated
    public Object getStateLock() {
        return this.state;
    }
    
    @Override
    @Deprecated
    public LocalChanges getLocalChangesImpl() {
        return this.syncState.getLocalChangesImpl();
    }
    
    // FIXME this.state.setRevisionNumber(this.state.getRevisionNumber() + 1);
}
