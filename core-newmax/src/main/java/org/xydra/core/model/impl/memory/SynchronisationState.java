package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.change.XChanges;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * Helper class for entities that can execute {@link XTransaction XTransactions}
 * ({@link XObject} and {@link XModel}) implementing most of the logic behind
 * transactions and synchronization.
 * 
 * Manages delayed sending of events.
 * 
 * @author dscharrer
 * @author xamde
 */
class SynchronisationState implements Serializable {
    
    /**
     * The entity being synced.
     */
    private ISyncProvider syncProvider;
    
    /**
     * Used for sending txn events
     */
    private IMemoryModel model;
    
    private IMemoryObject object;
    
    private final IMemoryMOFEntity syncRoot;
    
    /**
     * @param syncProvider
     * @param queue
     * @param model the {@link MemoryModel} to use for sending
     *            {@link XTransactionEvent XTransactionEvents}
     * @param object the {@link MemoryObject} to use for sending
     *            {@link XTransactionEvent XTransactionEvents}
     * @param syncRoot The entity handling the synchronisation
     */
    public SynchronisationState(ISyncProvider syncProvider, MemoryEventQueue queue,
            IMemoryModel model, IMemoryObject object, IMemoryMOFEntity syncRoot) {
        this.syncProvider = syncProvider;
        this.eventQueue = queue;
        this.model = model;
        this.object = object;
        this.syncRoot = syncRoot;
    }
    
    /**
     * Orphans are used during rollback & playback to avoid loosing registered
     * event listeners.
     */
    protected static class Orphans implements Serializable {
        
        private static final long serialVersionUID = -146971665894476381L;
        
        Map<XAddress,IMemoryField> fields = new HashMap<XAddress,IMemoryField>();
        
        Map<XId,IMemoryObject> objects = new HashMap<XId,IMemoryObject>();
        
    }
    
    private static final Logger log = LoggerFactory.getLogger(SynchronisationState.class);
    
    private static final long serialVersionUID = -5649382238597273583L;
    
    /** local state, used to synchronize change access for writes and reads */
    protected final MemoryEventQueue eventQueue;
    
    private void cleanupOrphans() {
        Orphans orphans = this.eventQueue.orphans;
        this.eventQueue.orphans = null;
        
        for(IMemoryObject object : orphans.objects.values()) {
            object.delete();
        }
        
        for(IMemoryField field : orphans.fields.values()) {
            field.delete();
        }
    }
    
    public int countUnappliedLocalChanges() {
        synchronized(this.eventQueue) {
            int count = 0;
            for(XLocalChange lc : this.eventQueue.getLocalChanges()) {
                if(!lc.isApplied()) {
                    count++;
                }
            }
            return count;
        }
    }
    
    private long executeTransactionWorker(XTransaction transaction, XLocalChangeCallback callback) {
        synchronized(this.eventQueue) {
            
            assertThisEntityIsNotRemoved();
            
            assert !this.eventQueue.transactionInProgess : "double transaction detected";
            
            // make sure that the transaction actually refers to this model
            if(!transaction.getTarget().equals(this.syncProvider.getAddress())) {
                if(callback != null) {
                    callback.onFailure();
                }
                return XCommand.FAILED;
            }
            
            ChangedModel model = new ChangedModel(this.syncProvider.getTransactionTarget());
            
            /*
             * Check if the transaction is valid by recording the changes made
             * by the commands in a delta to the current state (removedObjects,
             * addedObjects, removedFields, addedFields, changedValue) and
             * checking the commands against that.
             */
            if(!model.executeCommand(transaction)) {
                if(callback != null) {
                    callback.onFailure();
                }
                return XCommand.FAILED;
            }
            
            // all commands are OK, count the number of changes
            
            long nChanges = model.countCommandsNeeded(2);
            
            if(nChanges == 0) {
                // nothing to change
                if(callback != null) {
                    callback.onSuccess(XCommand.NOCHANGE);
                }
                return XCommand.NOCHANGE;
            }
            
            this.eventQueue.newLocalChange(transaction, callback);
            
            long oldRev = this.syncProvider.getRevisionNumber();
            
            /*
             * only execute as transaction if there actually are multiple
             * changes
             */
            if(nChanges > 1) {
                /*
                 * set "transactionInProgress" to true to stop involuntarily
                 * increasing the affected revision numbers
                 */
                this.eventQueue.transactionInProgess = true;
            }
            
            int since = this.eventQueue.getNextPosition();
            
            // apply changes
            
            for(XId objectId : model.getRemovedObjects()) {
                this.syncProvider.removeObjectInternal(objectId);
            }
            
            for(XReadableObject object : model.getNewObjects()) {
                IMemoryObject newObject = this.syncProvider.createObjectInternal(object.getId());
                for(XId fieldId : object) {
                    XReadableField field = object.getField(fieldId);
                    IMemoryField newField = newObject.createFieldInternal(fieldId);
                    if(!field.isEmpty()) {
                        newField.setValueInternal(field.getValue());
                    }
                }
            }
            
            for(ChangedObject object : model.getChangedObjects()) {
                IMemoryObject oldObject = this.syncProvider.getObject(object.getId());
                
                for(XId fieldId : object.getRemovedFields()) {
                    oldObject.removeFieldInternal(fieldId);
                }
                
                for(XReadableField field : object.getNewFields()) {
                    IMemoryField newField = oldObject.createFieldInternal(field.getId());
                    if(!field.isEmpty()) {
                        newField.setValueInternal(field.getValue());
                    }
                }
                
                for(ChangedField field : object.getChangedFields()) {
                    if(field.isChanged()) {
                        IMemoryField oldField = oldObject.getField(field.getId());
                        oldField.setValueInternal(field.getValue());
                    }
                }
                
            }
            
            // update revision numbers and save state
            
            if(nChanges > 1) {
                
                long newRevision = oldRev + 1;
                
                this.eventQueue.createTransactionEvent(getSessionActor(), this.model, this.object,
                        since);
                
                // new objects
                for(XReadableObject object : model.getNewObjects()) {
                    IMemoryObject newObject = this.syncProvider.getObject(object.getId());
                    assert newObject != null : "should have been created above";
                    for(XId fieldId : object) {
                        IMemoryField newField = newObject.getField(fieldId);
                        assert newField != null : "should have been created above";
                        newField.setRevisionNumber(newRevision);
                    }
                    newObject.setRevisionNumber(newRevision);
                }
                
                // changed objects
                for(ChangedObject object : model.getChangedObjects()) {
                    IMemoryObject oldObject = this.syncProvider.getObject(object.getId());
                    assert oldObject != null : "should have existed already and not been removed";
                    
                    boolean changed = object.getRemovedFields().iterator().hasNext();
                    
                    // new fields in old objects
                    for(XReadableField field : object.getNewFields()) {
                        IMemoryField newField = oldObject.getField(field.getId());
                        assert newField != null : "should have been created above";
                        newField.setRevisionNumber(newRevision);
                        changed = true;
                    }
                    
                    // changed fields
                    for(ChangedField field : object.getChangedFields()) {
                        if(field.isChanged()) {
                            IMemoryField oldField = oldObject.getField(field.getId());
                            assert oldField != null : "should have existed already and not been removed";
                            oldField.setRevisionNumber(newRevision);
                            changed = true;
                        }
                    }
                    
                    if(changed) {
                        oldObject.setRevisionNumber(newRevision);
                    }
                    
                }
                
                this.eventQueue.transactionInProgess = false;
                
                // really increment the model's revision number
                this.syncProvider.incrementRevision();
                
                // dispatch events
                this.eventQueue.sendEvents();
                
            } else {
                assert this.syncProvider.getRevisionNumber() == oldRev + 1 : "there should have been exactly one change";
            }
            
            return oldRev + 1;
        }
    }
    
    private void assertThisEntityIsNotRemoved() throws IllegalStateException {
        if(!this.syncRoot.exists()) {
            throw new IllegalStateException("this entity has been removed");
        }
    }
    
    public XChangeLog getChangeLog() {
        return this.eventQueue.getChangeLog();
    }
    
    public XLocalChange[] getLocalChanges() {
        synchronized(this.eventQueue) {
            List<MemoryLocalChange> mlc = this.eventQueue.getLocalChanges();
            return mlc.toArray(new XLocalChange[mlc.size()]);
        }
    }
    
    public XId getSessionActor() {
        synchronized(this.eventQueue) {
            return this.eventQueue.getActor();
        }
    }
    
    public String getSessionPassword() {
        synchronized(this.eventQueue) {
            return this.eventQueue.getPasswordHash();
        }
    }
    
    public long getSynchronizedRevision() {
        synchronized(this.eventQueue) {
            return this.eventQueue.getSyncRevision();
        }
    }
    
    public boolean isSynchronized() {
        return (this.syncProvider.getRevisionNumber() <= getSynchronizedRevision());
    }
    
    private long replayCommand(XCommand command) {
        
        XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
        
        if(command instanceof XRepositoryCommand) {
            if(this.syncProvider.getAddress().getAddressedType() != XType.XMODEL) {
                return XCommand.FAILED;
            }
            
            XRepositoryCommand rc = (XRepositoryCommand)command;
            
            if(!rc.getRepositoryId().equals(this.syncProvider.getAddress().getRepository())) {
                // given given repository-id are not consistent
                return XCommand.FAILED;
            }
            
            if(!rc.getModelId().equals(this.model.getId())) {
                return XCommand.FAILED;
            }
            
            if(command.getChangeType() == ChangeType.ADD) {
                if(this.syncRoot.exists()) {
                    // ID already taken
                    if(rc.isForced()) {
                        /*
                         * the forced event only cares about the postcondition -
                         * that there is a model with the given ID, not about
                         * that there was no such model before
                         */
                        return XCommand.NOCHANGE;
                    }
                    return XCommand.FAILED;
                }
                
                this.syncRoot.setExists(false);
                
                XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(getSessionActor(),
                        this.syncProvider.getAddress().getParent(), this.model.getId(),
                        this.syncProvider.getRevisionNumber(), false);
                this.eventQueue.enqueueRepositoryEvent(this.model.getFather(), event);
                
                this.syncProvider.incrementRevision();
            }
            
            else if(command.getChangeType() == ChangeType.REMOVE) {
                if(!this.syncRoot.exists()) {
                    // ID not taken
                    if(rc.isForced()) {
                        /*
                         * the forced event only cares about the postcondition -
                         * that there is no model with the given ID, not about
                         * that there was such a model before
                         */
                        return XCommand.NOCHANGE;
                    }
                    return XCommand.FAILED;
                }
                if(!rc.isForced()
                        && this.syncProvider.getRevisionNumber() != rc.getRevisionNumber()) {
                    return XCommand.FAILED;
                }
                
                int since = this.eventQueue.getNextPosition();
                boolean inTrans = this.model.enqueueModelRemoveEvents(getSessionActor());
                if(inTrans) {
                    this.eventQueue.createTransactionEvent(getSessionActor(), this.model, null,
                            since);
                }
                
                this.model.removeInternal();
                
            } else {
                throw new AssertionError("unknown command type: " + rc);
            }
            
            XyAssert.xyAssert(this.model.getRevisionNumber() == this.syncProvider
                    .getRevisionNumber());
            return this.syncProvider.getRevisionNumber();
        }
        
        // TODO allow replaying XModelCommands on a model-less object
        
        return this.syncProvider.executeCommand(command);
    }
    
    private boolean replayEvent(XEvent event) {
        
        XyAssert.xyAssert(!event.inTransaction(), "event %s should not be in a txn", event);
        
        while(getChangeLog().getCurrentRevisionNumber() < event.getOldModelRevision()) {
            this.eventQueue.logNullEvent();
        }
        
        long oldModelRev = this.model == null ? -1 : this.model.getRevisionNumber();
        XyAssert.xyAssert(oldModelRev <= event.getOldModelRevision());
        this.syncProvider.setRevisionNumberIfModel(event.getOldModelRevision());
        // TODO adjust object and field revisions? this is needed for
        // synchronizing parent-less
        // objects and/or if there are missing events (access rights?)
        
        XCommand replayCommand = XChanges.createReplayCommand(event);
        /*
         * The remote changes should be applied as the actor specified in the
         * event
         */
        XId oldActor = getSessionActor();
        /* switch actor to the one specified in this event */
        setSessionActor(event.getActor(), "NOTSET");
        long result = replayCommand(replayCommand);
        /* Switch back actor */
        setSessionActor(oldActor, "NOTSET");
        
        if(result < 0) {
            this.syncProvider.setRevisionNumberIfModel(oldModelRev);
            return false;
        }
        XEvent newEvent = getChangeLog().getEventAt(result);
        XyAssert.xyAssert(event.equals(newEvent), "should be equal", event, newEvent);
        assert this.model == null ? this.object.getRevisionNumber() == event.getRevisionNumber()
                : this.model.getRevisionNumber() == event.getRevisionNumber();
        XyAssert.xyAssert(this.syncProvider.getRevisionNumber() == event.getRevisionNumber());
        
        XyAssert.xyAssert(event.getChangedEntity().getObject() == null
                || this.syncProvider.getObject(event.getChangedEntity().getObject()) == null
                || this.syncProvider.getObject(event.getChangedEntity().getObject())
                        .getRevisionNumber() == event.getRevisionNumber());
        
        return true;
    }
    
    public void rollback(long revision) {
        
        if(revision < -1) {
            throw new IllegalArgumentException("invalid revision number: " + revision);
        }
        
        synchronized(this.eventQueue) {
            
            XChangeLog log = getChangeLog();
            long currentRev = log.getCurrentRevisionNumber();
            if(revision == currentRev) {
                return;
            }
            
            boolean oldBlock = this.eventQueue.setBlockSending(true);
            int pos = this.eventQueue.getNextPosition();
            
            // stop the change log to prevent the rollback events from being
            // logged
            boolean oldLogging = this.eventQueue.setLogging(false);
            
            boolean hasOrphans = (this.eventQueue.orphans != null);
            if(!hasOrphans) {
                this.eventQueue.orphans = new Orphans();
            }
            
            try {
                
                // rollback each event individually
                for(long i = currentRev; i > revision; i--) {
                    XEvent event = log.getEventAt(i);
                    if(event == null) {
                        continue;
                    }
                    if(event instanceof XAtomicEvent) {
                        rollbackEvent((XAtomicEvent)event);
                    } else {
                        XyAssert.xyAssert(event instanceof XTransactionEvent);
                        XTransactionEvent trans = (XTransactionEvent)event;
                        for(int j = trans.size() - 1; j >= 0; j--) {
                            XAtomicEvent atomicEvent = trans.getEvent(j);
                            rollbackEvent(atomicEvent);
                        }
                    }
                    
                }
                
                // reset the change log
                this.eventQueue.truncateLog(revision);
                
                if(!hasOrphans) {
                    // Clean unneeded events.
                    this.eventQueue.cleanEvents(pos);
                    
                    cleanupOrphans();
                }
                
            } finally {
                
                if(!hasOrphans) {
                    this.eventQueue.setBlockSending(oldBlock);
                    this.eventQueue.sendEvents();
                }
                
            }
            
            this.eventQueue.setLogging(oldLogging);
            
        }
        
    }
    
    /**
     * Rolls back the changes represented by the given {@link XAtomicEvent} and
     * will restore the states of the affected entity and its parents to the
     * time-point before the {@link XCommand} which is responsible for this
     * {@link XAtomicEvent} was executed.
     * 
     * @param event The {@link XAtomicEvent} which represented changes will be
     *            rolled back
     */
    private void rollbackEvent(XAtomicEvent event) {
        
        XyAssert.xyAssert(this.model == null
                || event.getRevisionNumber() == this.model.getRevisionNumber()
                || (event.inTransaction() && event.getOldModelRevision() == this.model
                        .getRevisionNumber()));
        
        if(event instanceof XRepositoryEvent) {
            XyAssert.xyAssert(event.getTarget().equals(this.syncProvider.getAddress().getParent()));
            XyAssert.xyAssert(event.getChangedEntity().getModel()
                    .equals(this.syncProvider.getAddress().getModel()));
            if(event.getChangeType() == ChangeType.REMOVE) {
                XyAssert.xyAssert(!this.syncRoot.exists());
                this.syncRoot.setExists(true);
            } else {
                XyAssert.xyAssert(event.getChangeType() == ChangeType.ADD);
                XyAssert.xyAssert(this.syncRoot.exists());
                XyAssert.xyAssert(this.model.isEmpty());
                this.syncRoot.setExists(false);
            }
            
        } else if(event instanceof XModelEvent) {
            // TODO allow applying XModelEvents on a model-less object
            XyAssert.xyAssert(event.getTarget().equals(this.syncProvider.getAddress()));
            XId objectId = ((XModelEvent)event).getObjectId();
            if(event.getChangeType() == ChangeType.REMOVE) {
                XyAssert.xyAssert(!this.model.hasObject(objectId));
                IMemoryObject object = this.syncProvider.createObjectInternal(objectId);
                XyAssert.xyAssert(event.getOldObjectRevision() >= 0);
                object.setRevisionNumber(event.getOldObjectRevision());
            } else {
                XyAssert.xyAssert(event.getChangeType() == ChangeType.ADD);
                XyAssert.xyAssert(this.model.hasObject(objectId));
                XyAssert.xyAssert(event.getRevisionNumber() == this.model.getObject(objectId)
                        .getRevisionNumber()
                        || (event.inTransaction() && this.model.getObject(objectId)
                                .getRevisionNumber() == XCommand.NEW));
                this.syncProvider.removeObjectInternal(objectId);
            }
            
        } else {
            XyAssert.xyAssert(event instanceof XObjectEvent || event instanceof XFieldEvent);
            IMemoryObject object = this.syncProvider.getObject(event.getTarget().getObject());
            XyAssert.xyAssert(object != null);
            assert object != null;
            XyAssert.xyAssert(event.getRevisionNumber() == object.getRevisionNumber()
                    || (event.inTransaction() && event.getOldObjectRevision() == object
                            .getRevisionNumber()));
            
            if(event instanceof XObjectEvent) {
                XyAssert.xyAssert(event.getTarget().equals(object.getAddress()));
                XId fieldId = ((XObjectEvent)event).getFieldId();
                if(event.getChangeType() == ChangeType.REMOVE) {
                    XyAssert.xyAssert(!object.hasField(fieldId));
                    IMemoryField field = object.createFieldInternal(fieldId);
                    XyAssert.xyAssert(event.getOldFieldRevision() >= 0);
                    field.setRevisionNumber(event.getOldFieldRevision());
                } else {
                    XyAssert.xyAssert(event.getChangeType() == ChangeType.ADD);
                    XyAssert.xyAssert(object.hasField(fieldId));
                    XyAssert.xyAssert(event.getRevisionNumber() == object.getField(fieldId)
                            .getRevisionNumber()
                            || (event.inTransaction() && object.getField(fieldId)
                                    .getRevisionNumber() == XCommand.NEW));
                    object.removeFieldInternal(fieldId);
                }
                
            } else {
                XyAssert.xyAssert(event instanceof XReversibleFieldEvent);
                IMemoryField field = object.getField(((XReversibleFieldEvent)event).getFieldId());
                XyAssert.xyAssert(field != null);
                assert field != null;
                XyAssert.xyAssert(event.getRevisionNumber() == field.getRevisionNumber()
                        || (event.inTransaction() && event.getOldFieldRevision() == field
                                .getRevisionNumber()));
                XyAssert.xyAssert(XI.equals(field.getValue(),
                        ((XReversibleFieldEvent)event).getNewValue()));
                field.setValueInternal(((XReversibleFieldEvent)event).getOldValue());
                XyAssert.xyAssert(event.getOldFieldRevision() >= 0);
                field.setRevisionNumber(event.getOldFieldRevision());
            }
            
            XyAssert.xyAssert(event.getOldObjectRevision() >= 0);
            object.setRevisionNumber(event.getOldObjectRevision());
        }
        
        this.syncProvider.setRevisionNumberIfModel(event.getOldModelRevision());
    }
    
    public void setSessionActor(XId actorId, String passwordHash) {
        synchronized(this.eventQueue) {
            this.eventQueue.setSessionActor(actorId, passwordHash);
        }
    }
    
    /**
     * @param remoteChanges
     * @return ...
     */
    public boolean synchronize(XEvent[] remoteChanges) {
        
        boolean success = true;
        boolean removedChanged;
        
        synchronized(this.eventQueue) {
            
            XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
            
            boolean oldRemoved = !this.syncRoot.exists();
            
            List<MemoryLocalChange> localChanges = this.eventQueue.getLocalChanges();
            
            log.info("sync: merging " + remoteChanges.length + " remote and " + localChanges.size()
                    + " local changes, local rev is " + this.syncProvider.getRevisionNumber()
                    + " (synced to " + getSynchronizedRevision() + ")");
            
            long[] results = null;
            
            boolean oldBlock = this.eventQueue.setBlockSending(true);
            
            XyAssert.xyAssert(this.eventQueue.orphans == null);
            this.eventQueue.orphans = new Orphans();
            
            int pos = this.eventQueue.getNextPosition();
            
            long syncRev = getSynchronizedRevision();
            
            // Roll back to the old revision and save removed entities.
            rollback(syncRev);
            
            // Apply the remote changes.
            for(XEvent remoteChange : remoteChanges) {
                log.info("sync: merging remote event " + remoteChange);
                if(remoteChange == null) {
                    this.eventQueue.logNullEvent();
                    continue;
                }
                if(!replayEvent(remoteChange)) {
                    success = false;
                    break;
                }
            }
            
            long remoteRev = this.syncProvider.getRevisionNumber();
            this.eventQueue.setSyncRevision(remoteRev);
            
            // Remove local changes that have been applied remotely.
            for(int i = localChanges.size() - 1; i >= 0; i--) {
                MemoryLocalChange lc = localChanges.get(i);
                if(lc.isApplied() && lc.getRemoteRevision() <= remoteRev) {
                    XyAssert.xyAssert(lc.getRemoteRevision() >= 0);
                    localChanges.remove(i);
                }
            }
            
            results = new long[localChanges.size()];
            
            // Re-apply the local changes.
            for(int i = 0; i < localChanges.size(); i++) {
                
                // FIXME use the actorId from the local change
                MemoryLocalChange lc = localChanges.get(i);
                
                lc.updateCommand(syncRev, remoteRev);
                
                /*
                 * Applying all local commands as the current actor, ignoring
                 * what actor was used when they were originally executed.
                 */
                results[i] = replayCommand(lc.getCommand());
                
            }
            
            // Clean unneeded events.
            this.eventQueue.cleanEvents(pos);
            
            cleanupOrphans();
            
            this.eventQueue.setBlockSending(oldBlock);
            
            // invoke callbacks for failed / nochange commands
            for(int i = 0; i < results.length; i++) {
                MemoryLocalChange change = localChanges.get(i);
                if(results[i] == XCommand.FAILED) {
                    log.info("sync: client command conflicted: " + change.getCommand());
                    if(change.getCallback() != null) {
                        change.getCallback().onFailure();
                    }
                } else if(results[i] == XCommand.NOCHANGE) {
                    log.info("sync: client command redundant: " + change.getCommand());
                    if(change.getCallback() != null) {
                        change.getCallback().onSuccess(results[i]);
                    }
                }
            }
            
            // remove failed / nochange commands
            // IMPROVE this is O(nLocalChanges^2) worst case
            for(int i = results.length - 1; i >= 0; i--) {
                if(results[i] < 0) {
                    localChanges.remove(i);
                }
            }
            
            IMemoryModel model = this.model;
            removedChanged = (oldRemoved !=
            
            !this.syncRoot.exists() && model != null && model.getFather() != null);
            if(!removedChanged) {
                this.eventQueue.sendEvents();
                this.eventQueue.sendSyncEvents();
            }
            
            log.info("sync: merged changes, new local rev is "
                    + this.syncProvider.getRevisionNumber() + " (synced to "
                    + getSynchronizedRevision() + ")");
        }
        
        // This needs to be outside of the synchronized block to prevent
        // deadlocks between repository and model locking.
        if(removedChanged) {
            this.model.getFather().updateRemoved(this.model);
        }
        
        return success;
    }
    
    @ModificationOperation
    protected long executeTransaction(XTransaction transaction, XLocalChangeCallback callback) {
        // TODO assertThisEntityIsNotRemoved();
        if(transaction.getTarget().getObject() != null) {
            /*
             * try to get the object the given transaction actually refers to
             */
            IMemoryObject object = this.syncProvider.getObject(transaction.getTarget().getObject());
            
            if(object == null) {
                // object does not exist -> transaction fails
                if(callback != null) {
                    callback.onFailure();
                }
                return XCommand.FAILED;
            } else {
                // let the object handle the transaction execution
                /*
                 * TODO using the actor set on the object instead of the one set
                 * on the model (on which the user called the
                 * #executeTransaction() method) - this is counter-intuitive for
                 * an API user
                 */
                return object.getSyncState().executeTransactionWorker(transaction, callback);
            }
        }
        return executeTransactionWorker(transaction, callback);
    }
}
