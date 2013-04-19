package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.delta.ReadableModelWithOneObject;
import org.xydra.core.model.impl.memory.SynchronisationState.Orphans;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XObject}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class OldMemoryObject extends AbstractMOFEntity implements IMemoryObject, XObject,
        IHasXAddress, IHasChangeLog, XSynchronizesChanges, XExecutesCommands, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents, Serializable {
    
    private static final long serialVersionUID = -808702139986657842L;
    
    /** The father-model of this MemoryObject */
    @CanBeNull
    private final IMemoryModel father;
    
    /** The snapshot-like runtime state */
    private final XRevWritableObject state;
    
    private final Map<XId,IMemoryField> loadedFields = new HashMap<XId,IMemoryField>();
    
    public SynchronisationState syncState;
    
    public int countUnappliedLocalChanges() {
        return this.syncState.countUnappliedLocalChanges();
    }
    
    public XWritableChangeLog getChangeLog() {
        return this.syncState.getChangeLog();
    }
    
    public XLocalChange[] getLocalChanges() {
        return this.syncState.getLocalChanges();
    }
    
    public XId getSessionActor() {
        return this.syncState.getSessionActor();
    }
    
    public String getSessionPasswordHash() {
        return this.syncState.getSessionPassword();
    }
    
    public long getSynchronizedRevision() {
        return this.syncState.getSynchronizedRevision();
    }
    
    public boolean isSynchronized() {
        return this.syncState.isSynchronized();
    }
    
    public void rollback(long revision) {
        assertCanBeSynced();
        this.syncState.rollback(revision);
    }
    
    /**
     * Check if this entity may be synchronised. Throws an exception, if not.
     * 
     * @throws IllegalStateException if entity can not be synchronised
     */
    private void assertCanBeSynced() throws IllegalStateException {
        if(OldMemoryObject.this.father != null) {
            throw new IllegalStateException(
                    "an object that is part of a model cannot be rolled abck / synchronized individualy");
        }
    }
    
    public void setSessionActor(XId actorId, String passwordHash) {
        this.syncState.setSessionActor(actorId, passwordHash);
    }
    
    public boolean synchronize(XEvent[] remoteChanges) {
        assertCanBeSynced();
        return this.syncState.synchronize(remoteChanges);
    }
    
    ISyncProvider syncProvider = new ISyncProvider() {
        
        @Override
        public XAddress getAddress() {
            return OldMemoryObject.this.getAddress();
        }
        
        // /**
        // * Sets the revision number of this MemoryObject
        // *
        // * @param newRevision the new revision number
        // */
        // public void setRevisionNumber(long newRevision) {
        // MemoryObject.this.setRevisionNumber(newRevision);
        // }
        
        @Override
        public void removeObjectInternal(XId objectId) {
            throw new AssertionError("object transactions cannot remove objects");
        }
        
        @Override
        public void incrementRevision() {
            OldMemoryObject.this.incrementRevision();
        }
        
        @Override
        public XReadableModel getTransactionTarget() {
            if(OldMemoryObject.this.father != null) {
                return OldMemoryObject.this.father;
            } else {
                return new ReadableModelWithOneObject(OldMemoryObject.this);
            }
        }
        
        @Override
        public OldMemoryObject getObject(@NeverNull XId objectId) {
            if(getId().equals(objectId)) {
                return OldMemoryObject.this;
            }
            return null;
        }
        
        @Override
        public long getRevisionNumber() {
            if(OldMemoryObject.this.father != null)
                return OldMemoryObject.this.father.getRevisionNumber();
            else
                return getRevisionNumber();
        }
        
        @Override
        public long executeCommand(XCommand command) {
            return OldMemoryObject.this.executeCommand(command);
        }
        
        @Override
        public OldMemoryObject createObjectInternal(XId objectId) {
            throw new AssertionError("object transactions cannot create objects");
        }
        
        @Override
        public void setRevisionNumberIfModel(long modelRevisionNumber) {
            // not a model, so nothing to do here
        }
        
    };
    
    private final boolean canBeSynced;
    
    /**
     * Create a new object within a model. Can be synchronized.
     * 
     * @param father
     * @param queue
     * @param objectState
     */
    public OldMemoryObject(IMemoryModel father, MemoryEventQueue queue,
            XRevWritableObject objectState) {
        this(father, queue.getActor(), queue.getPasswordHash(), objectState.getAddress(),
                objectState, queue.getChangeLog().getChangeLogState(), false);
    }
    
    /**
     * Create a stand-alone object (without father) wrapping an existing state.
     * Can be synced IFF objectAddress is a full address and has an initial
     * create-object command in its changeLog
     * 
     * @param actorId
     * @param passwordHash
     * @param state
     * @param log @CanBeNull
     */
    public OldMemoryObject(XId actorId, String passwordHash, XRevWritableObject state,
            XChangeLogState log) {
        this(null, actorId, passwordHash, state.getAddress(), state, log, true);
    }
    
    /**
     * Create a new stand-alone object (without father), which can be synced IFF
     * objectAddress is a full address and has an initial create-object command
     * in its changeLog
     * 
     * @param actorId
     * @param passwordHash
     * @param objectAddress
     */
    public OldMemoryObject(XId actorId, String passwordHash, XAddress objectAddress) {
        this(null, actorId, passwordHash, objectAddress, null, null, true);
    }
    
    /**
     * Create a new stand-alone object (without father), which can be synced IFF
     * objectAddress is a full address and has an initial create-object command
     * in its changeLog
     * 
     * @param actorId
     * @param passwordHash
     * @param objectId
     */
    public OldMemoryObject(XId actorId, String passwordHash, XId objectId) {
        this(null, actorId, passwordHash, XX.resolveObject((XId)null, null, objectId), null, null,
                true);
    }
    
    /**
     * @param father @CanBeNull
     * @param actorId
     * @param passwordHash
     * @param objectAddress
     * @param state
     * @param log
     * @param createObject Can only be true if state & log are null. If true, an
     *            initial create-this-object-event is added.
     */
    private OldMemoryObject(IMemoryModel father, XId actorId, String passwordHash,
            XAddress objectAddress, XRevWritableObject state, XChangeLogState log,
            boolean createObject) {
        super(null, createObject || state != null);
        this.father = father;
        assert state == null || state.getAddress().equals(objectAddress);
        this.canBeSynced = objectAddress.getRepository() != null
                && objectAddress.getModel() != null;
        if(state == null) {
            // create new object
            this.state = new SimpleObject(objectAddress);
            assert log == null;
            XChangeLogState changeLogState = new MemoryChangeLogState(objectAddress);
            MemoryChangeLog changeLog = new MemoryChangeLog(changeLogState);
            
            if(createObject) {
                if(this.canBeSynced) {
                    XModelEvent createObjectEvent = MemoryModelEvent.createAddEvent(actorId,
                            XX.resolveModel(objectAddress), objectAddress.getObject(),
                            XCommand.FORCED, false);
                    changeLog.appendEvent(createObjectEvent);
                } else {
                    changeLogState.setBaseRevisionNumber(0);
                }
                assert changeLog.getCurrentRevisionNumber() == 0;
            }
            MemoryEventQueue queue = new MemoryEventQueue(actorId, passwordHash, changeLog, -1);
            this.syncState = new SynchronisationState(this.syncProvider, queue, this.getFather(),
                    this, this);
        } else {
            // re-use existing state
            this.state = state;
            XChangeLogState changeLogState;
            if(log == null) {
                changeLogState = new MemoryChangeLogState(objectAddress);
            } else {
                changeLogState = log;
            }
            MemoryChangeLog changeLog = new MemoryChangeLog(changeLogState);
            MemoryEventQueue queue = new MemoryEventQueue(actorId, passwordHash, changeLog,
                    changeLog.getBaseRevisionNumber());
            this.syncState = new SynchronisationState(this.syncProvider, queue, this.getFather(),
                    this, this);
        }
    }
    
    // implement IMemoryField
    @Override
    public void setRevisionNumber(long newRevision) {
        OldMemoryObject.this.state.setRevisionNumber(newRevision);
    }
    
    // implement IMemoryObject
    @Override
    public void incrementRevision() {
        XyAssert.xyAssert(!OldMemoryObject.this.syncState.eventQueue.transactionInProgess);
        if(OldMemoryObject.this.father != null) {
            // this increments the revisionNumber of the father and sets
            // this revNr to the revNr of the father
            OldMemoryObject.this.father.incrementRevision();
            setRevisionNumber(OldMemoryObject.this.father.getRevisionNumber());
        } else {
            XChangeLog log = OldMemoryObject.this.syncState.eventQueue.getChangeLog();
            if(log != null) {
                XyAssert.xyAssert(log.getCurrentRevisionNumber() > getRevisionNumber());
                setRevisionNumber(log.getCurrentRevisionNumber());
            } else {
                setRevisionNumber(getRevisionNumber() + 1);
            }
        }
    }
    
    // /**
    // * Creates a new MemoryObject with the given {@link MemoryModel} as its
    // * father.
    // *
    // * @param father The father-{@link MemoryModel} of this MemoryObject.
    // * @param eventQueue The {@link MemoryEventManager} which will be used by
    // * this MemoryObject.
    // * @param objectState A {@link XRevWritableObject} representing the
    // initial
    // * state of this object. The {@link XObject} will continue using
    // * this state object, so it must not be modified directly after
    // * wrapping it in an {@link XObject}.
    // */
    // protected MemoryObject(MemoryModel father, MemoryEventManager eventQueue,
    // XRevWritableObject objectState) {
    // // TODO super(eventQueue);
    // XyAssert.xyAssert(eventQueue != null);
    // assert eventQueue != null;
    //
    // if(objectState == null) {
    // throw new IllegalArgumentException("objectState may not be null");
    // }
    // this.state = objectState;
    //
    // if(father == null && objectState.getAddress().getModel() != null) {
    // throw new
    // IllegalArgumentException("must load object through containing model");
    // }
    // this.father = father;
    // }
    //
    // /**
    // * Creates a new MemoryObject without a father-{@link XModel}.
    // *
    // * @param actorId The actor to be used in events generated by this object.
    // * @param passwordHash
    // * @param objectId The {@link XId} for this MemoryObject
    // */
    // // 2013-4: only used in tests
    // public MemoryObject(XId actorId, String passwordHash, XId objectId) {
    // this(actorId, passwordHash, new SimpleObject(XX.toAddress(null, null,
    // objectId, null)));
    // }
    //
    // /**
    // * Creates a new MemoryObject without a father-{@link XModel}.
    // *
    // * @param actorId The actor to be used in events generated by this object.
    // * @param passwordHash
    // * @param objectState A {@link XRevWritableObject} representing the
    // initial
    // * state of this object. The {@link XObject} will continue using
    // * this state object, so it must not be modified directly after
    // * wrapping it in an {@link XObject}.
    // */
    // public MemoryObject(XId actorId, String passwordHash, XRevWritableObject
    // objectState) {
    // this(actorId, passwordHash, objectState, createChangeLog(objectState));
    // }
    // /**
    // * Creates a new MemoryObject without a father-{@link XModel}.
    // *
    // * @param actorId The actor to be used in events generated by this object.
    // * @param passwordHash
    // * @param objectState A {@link XRevWritableObject} representing the
    // initial
    // * state of this object. The {@link XObject} will continue using
    // * this state object, so it must not be modified directly after
    // * wrapping it in an {@link XObject}.
    // * @param log
    // */
    // public MemoryObject(XId actorId, String passwordHash, XRevWritableObject
    // objectState,
    // XChangeLogState log) {
    // this(null, createEventQueue(actorId, passwordHash, objectState, log),
    // objectState);
    // }
    
    // private static XChangeLogState createChangeLog(XRevWritableObject
    // objectState) {
    // XChangeLogState log = new MemoryChangeLogState(objectState.getAddress());
    //
    // // FIXME MONKEY PATCHED
    // // log.setBaseRevisionNumber(objectState.getRevisionNumber() + 1);
    // log.setBaseRevisionNumber(objectState.getRevisionNumber());
    //
    // return log;
    // }
    
    // private static MemoryEventManager createEventQueue(XId actorId, String
    // passwordHash,
    // XRevWritableObject objectState, XChangeLogState logState) {
    // if(logState.getCurrentRevisionNumber() !=
    // objectState.getRevisionNumber()) {
    // throw new
    // IllegalArgumentException("object state and log revision mismatch");
    // }
    // MemoryChangeLog log = new MemoryChangeLog(logState);
    // return new MemoryEventManager(actorId, passwordHash, log,
    // objectState.getRevisionNumber());
    // }
    
    @Override
    public IMemoryField createField(XId fieldId) {
        
        XObjectCommand command = MemoryObjectCommand.createAddCommand(getAddress(), true, fieldId);
        
        // synchronize so that return is never null if command succeeded
        synchronized(this.root) {
            long result = executeObjectCommand(command);
            IMemoryField field = getField(fieldId);
            XyAssert.xyAssert(result == XCommand.FAILED || field != null);
            return field;
        }
    }
    
    /**
     * Create a new field, increase revision (if not in a transaction) and
     * enqueue the corresponding event.
     * 
     * The caller is responsible for handling synchronization, for checking that
     * this object has not been removed and for checking that the field doesn't
     * already exist.
     */
    // implement IMemoryField
    @Override
    public IMemoryField createFieldInternal(XId fieldId) {
        
        XyAssert.xyAssert(!hasField(fieldId));
        
        boolean inTrans = this.syncState.eventQueue.transactionInProgess;
        
        IMemoryField field = null;
        Orphans orphans = this.syncState.eventQueue.orphans;
        if(orphans != null) {
            XAddress fieldAddr = XX.resolveField(getAddress(), fieldId);
            field = orphans.fields.remove(fieldAddr);
        }
        if(field == null) {
            XRevWritableField fieldState = this.state.createField(fieldId);
            XyAssert.xyAssert(getAddress().contains(fieldState.getAddress()));
            field = new OldMemoryField(this, this.syncState.eventQueue, fieldState);
        } else {
            this.state.addField(field.getState());
        }
        
        XyAssert.xyAssert(field.getObject() == this);
        
        this.loadedFields.put(field.getId(), field);
        long eventRev = this.syncState.getChangeLog().getCurrentRevisionNumber();
        XObjectEvent event = MemoryObjectEvent.createAddEvent(this.syncState.eventQueue.getActor(),
                getAddress(), field.getId(), eventRev, eventRev, inTrans);
        
        this.syncState.eventQueue.enqueueObjectEvent(this, event);
        
        /*
         * event propagation and revision number increasing happens after all
         * events were successful
         */
        if(!inTrans) {
            
            field.incrementRevision();
            
            // propagate events
            this.syncState.eventQueue.sendEvents();
            
        }
        
        return field;
    }
    
    /**
     * Deletes the state information of this MemoryObject from the currently
     * used persistence layer
     */
    // implement IMemoryField
    @Override
    public void delete() {
        for(XId fieldId : this) {
            IMemoryField field = getField(fieldId);
            field.delete();
        }
        for(XId fieldId : this.loadedFields.keySet()) {
            this.state.removeField(fieldId);
        }
        this.loadedFields.clear();
        this.exists = true;
    }
    
    /**
     * Builds a transaction that first removes the value of the given field and
     * then the given field itself.
     * 
     * @param actor The actor for this transaction
     * @param field The field which should be removed by the transaction
     * @param inTrans true, if the removal of this {@link MemoryField} occurs
     *            during an {@link XTransaction}.
     * @param implied true if this object is also removed in the same
     *            transaction
     */
    public void enqueueFieldRemoveEvents(XId actor, IMemoryField field, boolean inTrans,
            boolean implied) {
        
        if(field == null) {
            throw new NullPointerException("field must not be null");
        }
        
        long modelRev = getModelRevisionNumber();
        
        if(field.getValue() != null) {
            XyAssert.xyAssert(inTrans);
            XReversibleFieldEvent event = MemoryReversibleFieldEvent.createRemoveEvent(actor,
                    field.getAddress(), field.getValue(), modelRev, getRevisionNumber(),
                    field.getRevisionNumber(), inTrans, true);
            this.syncState.eventQueue.enqueueFieldEvent(field, event);
        }
        
        XObjectEvent event = MemoryObjectEvent.createRemoveEvent(actor, getAddress(),
                field.getId(), modelRev, getRevisionNumber(), field.getRevisionNumber(), inTrans,
                implied);
        this.syncState.eventQueue.enqueueObjectEvent(this, event);
        
    }
    
    @ReadOperation
    @Override
    public boolean equals(Object object) {
        synchronized(this.root) {
            return super.equals(object);
        }
    }
    
    @Override
    public long executeCommand(XCommand command) {
        return executeCommand(command, null);
    }
    
    @Override
    public long executeCommand(XCommand command, XLocalChangeCallback callback) {
        if(command instanceof XTransaction) {
            synchronized(this.root) {
                return this.syncState.executeTransaction((XTransaction)command, callback);
            }
        }
        if(command instanceof XObjectCommand) {
            return executeObjectCommand((XObjectCommand)command, callback);
        }
        if(command instanceof XFieldCommand) {
            IMemoryField field = getField(command.getTarget().getField());
            if(field != null) {
                return field.executeFieldCommand((XFieldCommand)command, callback);
            } else {
                return XCommand.FAILED;
            }
        }
        throw new IllegalArgumentException("Unknown command type: " + command);
    }
    
    @Override
    public long executeObjectCommand(XObjectCommand command) {
        return executeObjectCommand(command, null);
    }
    
    private long executeObjectCommand(XObjectCommand command, XLocalChangeCallback callback) {
        synchronized(this.root) {
            assertThisEntityExists();
            
            XyAssert.xyAssert(!this.syncState.eventQueue.transactionInProgess);
            
            if(!getAddress().equals(command.getTarget())) {
                if(callback != null) {
                    callback.onFailure();
                }
                return XCommand.FAILED;
            }
            
            long oldRev = getRevisionNumber();
            
            if(command.getChangeType() == ChangeType.ADD) {
                if(hasField(command.getFieldId())) {
                    // ID already taken
                    if(command.isForced()) {
                        /*
                         * the forced event only cares about the postcondition -
                         * that there is a field with the given ID, not about
                         * that there was no such field before
                         */
                        if(callback != null) {
                            callback.onSuccess(XCommand.NOCHANGE);
                        }
                        return XCommand.NOCHANGE;
                    }
                    if(callback != null) {
                        callback.onFailure();
                    }
                    return XCommand.FAILED;
                }
                
                this.syncState.eventQueue.newLocalChange(command, callback);
                
                createFieldInternal(command.getFieldId());
                
            } else if(command.getChangeType() == ChangeType.REMOVE) {
                XField oldField = getField(command.getFieldId());
                
                if(oldField == null) {
                    // ID not taken
                    if(command.isForced()) {
                        /*
                         * the forced event only cares about the postcondition -
                         * that there is no field with the given ID, not about
                         * that there was such a field before
                         */
                        if(callback != null) {
                            callback.onSuccess(XCommand.NOCHANGE);
                        }
                        return XCommand.NOCHANGE;
                    }
                    if(callback != null) {
                        callback.onFailure();
                    }
                    return XCommand.FAILED;
                }
                
                if(!command.isForced()
                        && oldField.getRevisionNumber() != command.getRevisionNumber()) {
                    if(callback != null) {
                        callback.onFailure();
                    }
                    return XCommand.FAILED;
                }
                
                this.syncState.eventQueue.newLocalChange(command, callback);
                
                removeFieldInternal(command.getFieldId());
                
            } else {
                throw new IllegalArgumentException("Unknown object command type: " + command);
            }
            
            return oldRev + 1;
        }
    }
    
    @Override
    public XAddress getAddress() {
        synchronized(this.root) {
            return this.state.getAddress();
        }
    }
    
    @Override
    public IMemoryField getField(XId fieldId) {
        synchronized(this.root) {
            assertThisEntityExists();
            
            IMemoryField field = this.loadedFields.get(fieldId);
            if(field != null) {
                return field;
            }
            
            XRevWritableField fieldState = this.state.getField(fieldId);
            if(fieldState == null) {
                return null;
            }
            
            field = new OldMemoryField(this, this.syncState.eventQueue, fieldState);
            this.loadedFields.put(fieldId, field);
            
            return field;
        }
    }
    
    @Override
    public XId getId() {
        synchronized(this.root) {
            return this.state.getId();
        }
    }
    
    /**
     * @return the {@link XId} of the father-{@link XModel} of this MemoryObject
     *         or null, if this object has no father.
     */
    @SuppressWarnings("unused")
    private XId getModelId() {
        return this.father == null ? null : this.father.getId();
    }
    
    /**
     * @return the current revision number of the father-{@link MemoryModel} of
     *         this MemoryObject or {@link XEvent#RevisionOfEntityNotSet} if
     *         this MemoryObject has no father.
     */
    // implement IMemoryObject
    public long getModelRevisionNumber() {
        if(this.father != null)
            return this.father.getRevisionNumber();
        else
            return XEvent.RevisionOfEntityNotSet;
    }
    
    /**
     * @return the {@link XId} of the father-{@link XRepository} of this
     *         MemoryObject or null, if this object has no father.
     */
    @SuppressWarnings("unused")
    private XId getRepositoryId() {
        return this.father == null ? null : this.father.getAddress().getRepository();
    }
    
    @Override
    public long getRevisionNumber() {
        synchronized(this.root) {
            return this.state.getRevisionNumber();
        }
    }
    
    // implement IMemoryObject
    @Override
    public XRevWritableObject getState() {
        return this.state;
    }
    
    @Override
    public boolean hasField(XId id) {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.loadedFields.containsKey(id) || this.state.hasField(id);
        }
    }
    
    @Override
    public IMemoryModel getFather() {
        return this.father;
    }
    
    @ReadOperation
    @Override
    public int hashCode() {
        synchronized(this.root) {
            return super.hashCode();
        }
    }
    
    @SuppressWarnings("unused")
    private boolean hasObject(XId objectId) {
        return getId().equals(objectId);
    }
    
    @Override
    public boolean isEmpty() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.state.isEmpty();
        }
    }
    
    @Override
    public Iterator<XId> iterator() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.state.iterator();
        }
    }
    
    @Override
    public boolean removeField(XId fieldId) {
        
        // no synchronization necessary here (except that in
        // executeObjectCommand())
        
        XObjectCommand command = MemoryObjectCommand.createRemoveCommand(getAddress(),
                XCommand.FORCED, fieldId);
        
        long result = executeObjectCommand(command);
        XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
        return result != XCommand.NOCHANGE;
    }
    
    /**
     * Remove an existing field, increase revision (if not in a transaction) and
     * enqueue the corresponding event(s).
     * 
     * The caller is responsible for handling synchronization, for checking that
     * this object has not been removed and for checking that the field actually
     * exists.
     */
    // implement IMemoryField
    @Override
    public void removeFieldInternal(XId fieldId) {
        
        XyAssert.xyAssert(hasField(fieldId));
        
        IMemoryField field = getField(fieldId);
        assert field != null : "checked by caller";
        
        boolean inTrans = this.syncState.eventQueue.transactionInProgess;
        
        boolean makeTrans = !field.isEmpty();
        int since = this.syncState.eventQueue.getNextPosition();
        enqueueFieldRemoveEvents(this.syncState.eventQueue.getActor(), field, makeTrans || inTrans,
                false);
        
        // actually remove the field
        this.state.removeField(field.getId());
        this.loadedFields.remove(field.getId());
        
        Orphans orphans = this.syncState.eventQueue.orphans;
        if(orphans != null) {
            XyAssert.xyAssert(!orphans.fields.containsKey(field.getAddress()));
            field.getState().setValue(null);
            orphans.fields.put(field.getAddress(), field);
        } else {
            field.delete();
        }
        
        // event propagation and revision number increasing happens after
        // all events were successful
        if(!inTrans) {
            
            if(makeTrans) {
                this.syncState.eventQueue.createTransactionEvent(
                        this.syncState.eventQueue.getActor(), this.father, this, since);
            }
            
            incrementRevision();
            
            // propagate events
            this.syncState.eventQueue.sendEvents();
            
        }
        
    }
    
    /**
     * Removes all {@link XField XFields} of this MemoryObject from the
     * persistence layer and the MemoryObject itself.
     */
    // implement IMemoryObject
    @Override
    public void removeInternal() {
        // all fields are already loaded for creating events
        
        for(IMemoryField field : this.loadedFields.values()) {
            field.getState().setValue(null);
            XyAssert.xyAssert(!this.syncState.eventQueue.orphans.fields.containsKey(field
                    .getAddress()));
            this.syncState.eventQueue.orphans.fields.put(field.getAddress(), field);
            this.state.removeField(field.getId());
        }
        
        this.loadedFields.clear();
        
        XyAssert.xyAssert(this.syncState.eventQueue.orphans != null);
        XyAssert.xyAssert(!this.syncState.eventQueue.orphans.objects.containsKey(getId()));
        this.syncState.eventQueue.orphans.objects.put(getId(), this);
    }
    
    @ReadOperation
    @Override
    public String toString() {
        return this.getId() + " rev[" + this.getRevisionNumber() + "]" + " "
                + this.state.toString();
    }
    
    @Override
    public XRevWritableObject createSnapshot() {
        synchronized(this.root) {
            if(exists()) {
                return XCopyUtils.createSnapshot(this);
            } else {
                return null;
            }
        }
    }
    
    @Override
    public XType getType() {
        return XType.XOBJECT;
    }
    
    // implement IMemoryObject
    @Override
    public void fireObjectEvent(XObjectEvent event) {
        synchronized(this.root) {
            this.root.fireObjectEvent(getAddress(), event);
        }
    }
    
    // implement IMemoryObject
    @Override
    public void fireFieldEvent(XFieldEvent event) {
        synchronized(this.root) {
            this.root.fireFieldEvent(getAddress(), event);
        }
    }
    
    // implement IMemoryObject
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
    
    // implement IMemoryObject
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
}
