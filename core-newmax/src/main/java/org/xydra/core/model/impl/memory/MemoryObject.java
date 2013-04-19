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
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRMOFChangeListener;
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
public class MemoryObject extends AbstractMOFEntity implements IMemoryObject, XObject,
        IHasXAddress, IHasChangeLog, XSynchronizesChanges, XExecutesCommands, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents, Serializable {
    
    private static final long serialVersionUID = -808702139986657842L;
    
    /** The father-model of this MemoryObject */
    @CanBeNull
    private final IMemoryModel father;
    
    /** The snapshot-like runtime state */
    private final XRevWritableObject objectState;
    
    private final Map<XId,IMemoryField> loadedFields = new HashMap<XId,IMemoryField>();
    
    // nice for sync algos
    public int countUnappliedLocalChanges() {
        return this.getRoot().countUnappliedLocalChanges();
    }
    
    public XWritableChangeLog getChangeLog() {
        return this.getRoot().getWritableChangeLog();
    }
    
    public XId getSessionActor() {
        return this.getRoot().getSessionActor();
    }
    
    public String getSessionPasswordHash() {
        return this.getRoot().getSessionPasswordHash();
    }
    
    public long getSynchronizedRevision() {
        // FIXME ... manage this also for objects with a parent
        return this.syncState.getSynchronizedRevision();
    }
    
    public boolean isSynchronized() {
        return this.syncState.isSynchronized();
    }
    
    /**
     * Check if this entity may be synchronised. Throws an exception, if not.
     * 
     * @throws IllegalStateException if entity can not be synchronised
     */
    private void assertCanBeSynced() throws IllegalStateException {
        if(MemoryObject.this.father != null) {
            throw new IllegalStateException(
                    "an object that is part of a model cannot be rolled abck / synchronized individualy");
        }
    }
    
    public void setSessionActor(XId actorId, String passwordHash) {
        getRoot().setSessionActor(actorId);
        getRoot().setSessionPasswordHash(passwordHash);
    }
    
    public boolean synchronize(XEvent[] remoteChanges) {
        assertCanBeSynced();
        return this.syncState.synchronize(remoteChanges);
    }
    
    private final boolean canBeSynced;
    
    /**
     * Create a new object within a model. Can be synchronized.
     * 
     * @param father
     * @param queue
     * @param objectState
     */
    public MemoryObject(IMemoryModel father, MemoryEventQueue queue, XRevWritableObject objectState) {
        this(father.getRoot(), father, queue.getActor(), queue.getPasswordHash(), objectState
                .getAddress(), objectState, queue.getChangeLog().getChangeLogState(), false);
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
    public MemoryObject(XId actorId, String passwordHash, XRevWritableObject state,
            XChangeLogState log) {
        this(Root.createWithActor(state.getAddress(), actorId), null, actorId, passwordHash, state
                .getAddress(), state, log, true);
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
    public MemoryObject(XId actorId, String passwordHash, XAddress objectAddress) {
        this(Root.createWithActor(objectAddress, actorId), null, actorId, passwordHash,
                objectAddress, null, null, true);
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
    public MemoryObject(XId actorId, String passwordHash, XId objectId) {
        this(Root.createWithActor(XX.toAddress(null, null, objectId, null), actorId), null,
                actorId, passwordHash, XX.toAddress(null, null, objectId, null), null, null, true);
    }
    
    /**
     * @param root @CanBeNull if father is defined
     * @param father @CanBeNull if root is defined
     * @param actorId
     * @param passwordHash
     * @param objectAddress
     * @param state
     * @param log
     * @param createObject Can only be true if state & log are null. If true, an
     *            initial create-this-object-event is added.
     */
    private MemoryObject(Root root, IMemoryModel father, XId actorId, String passwordHash,
            XAddress objectAddress, XRevWritableObject state, XChangeLogState log,
            boolean createObject) {
        super(father == null ? root : father.getRoot(), createObject || state != null);
        assert father != null || root != null;
        this.father = father;
        assert state == null || state.getAddress().equals(objectAddress);
        this.canBeSynced = objectAddress.getRepository() != null
                && objectAddress.getModel() != null;
        if(state == null) {
            // create new object
            this.objectState = new SimpleObject(objectAddress);
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
            this.objectState = state;
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
    @Deprecated
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
    
    @Deprecated
    private long executeObjectCommand(XObjectCommand command, XLocalChangeCallback callback) {
        synchronized(this.root) {
            assertThisEntityExists();
            
            XRevWritableModel modelState = null;
            if(this.father != null) {
                modelState = this.father.getState();
            }
            
            return Executor.executeObjectCommand(getRoot().getSessionActor(), command, modelState,
                    this.objectState, getRoot(), new XRMOFChangeListener() {
                        
                        @Override
                        public void onChangeEvent(XRepositoryEvent event) {
                        }
                        
                        @Override
                        public void onChangeEvent(XModelEvent event) {
                        }
                        
                        @Override
                        public void onChangeEvent(XObjectEvent event) {
                        }
                        
                        @Override
                        public void onChangeEvent(XFieldEvent event) {
                            // remove field internally
                            XId fieldId = event.getFieldId();
                            MemoryObject.this.loadedFields.remove(fieldId);
                            MemoryObject.this.objectState.removeField(fieldId);
                        }
                    });
            
        }
    }
    
    @Override
    public XAddress getAddress() {
        synchronized(this.root) {
            return this.objectState.getAddress();
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
            
            XRevWritableField fieldState = this.objectState.getField(fieldId);
            if(fieldState == null) {
                return null;
            }
            
            field = new MemoryField(this, fieldState);
            this.loadedFields.put(fieldId, field);
            
            return field;
        }
    }
    
    @Override
    public XId getId() {
        synchronized(this.root) {
            return this.objectState.getId();
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
    @Deprecated
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
    @Deprecated
    private XId getRepositoryId() {
        return this.father == null ? null : this.father.getAddress().getRepository();
    }
    
    @Override
    public long getRevisionNumber() {
        synchronized(this.root) {
            return this.objectState.getRevisionNumber();
        }
    }
    
    // implement IMemoryObject
    @Override
    public XRevWritableObject getState() {
        return this.objectState;
    }
    
    @Override
    public boolean hasField(XId id) {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.objectState.hasField(id);
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
    @Deprecated
    private boolean hasObject(XId objectId) {
        return getId().equals(objectId);
    }
    
    @Override
    public boolean isEmpty() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.objectState.isEmpty();
        }
    }
    
    @Override
    public Iterator<XId> iterator() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.objectState.iterator();
        }
    }
    
    @Override
    public boolean removeField(XId fieldId) {
        
        /*
         * no synchronization necessary here (except that in
         * executeObjectCommand())
         */
        
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
    @Deprecated
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
        this.objectState.removeField(field.getId());
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
    @Deprecated
    public void removeInternal() {
        // all fields are already loaded for creating events
        
        for(IMemoryField field : this.loadedFields.values()) {
            field.getState().setValue(null);
            XyAssert.xyAssert(!this.syncState.eventQueue.orphans.fields.containsKey(field
                    .getAddress()));
            this.syncState.eventQueue.orphans.fields.put(field.getAddress(), field);
            this.objectState.removeField(field.getId());
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
                + this.objectState.toString();
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
    @Deprecated
    public SynchronisationState getSyncState() {
        return this.syncState;
    }
    
    @Override
    @Deprecated
    public Object getStateLock() {
        return this.objectState;
    }
    
    @Override
    @Deprecated
    public LocalChanges getLocalChangesImpl() {
        return this.syncState.getLocalChangesImpl();
    }
    
    @Deprecated
    public SynchronisationState syncState;
    
    @Deprecated
    public XLocalChange[] getLocalChanges() {
        return this.syncState.getLocalChanges();
    }
    
    @Deprecated
    public void rollback(long revision) {
        assertCanBeSynced();
        this.syncState.rollback(revision);
    }
    
    @Deprecated
    ISyncProvider syncProvider = new ISyncProvider() {
        
        @Override
        public XAddress getAddress() {
            return MemoryObject.this.getAddress();
        }
        
        @Override
        public void removeObjectInternal(XId objectId) {
            throw new AssertionError("object transactions cannot remove objects");
        }
        
        @Override
        public void incrementRevision() {
            MemoryObject.this.incrementRevision();
        }
        
        @Override
        public XReadableModel getTransactionTarget() {
            if(MemoryObject.this.father != null) {
                return MemoryObject.this.father;
            } else {
                return new ReadableModelWithOneObject(MemoryObject.this);
            }
        }
        
        @Override
        public MemoryObject getObject(@NeverNull XId objectId) {
            if(getId().equals(objectId)) {
                return MemoryObject.this;
            }
            return null;
        }
        
        @Override
        public long getRevisionNumber() {
            if(MemoryObject.this.father != null)
                return MemoryObject.this.father.getRevisionNumber();
            else
                return getRevisionNumber();
        }
        
        @Override
        public long executeCommand(XCommand command) {
            return MemoryObject.this.executeCommand(command);
        }
        
        @Override
        public MemoryObject createObjectInternal(XId objectId) {
            throw new AssertionError("object transactions cannot create objects");
        }
        
        @Override
        public void setRevisionNumberIfModel(long modelRevisionNumber) {
            // not a model, so nothing to do here
        }
        
    };
    
    // implement IMemoryObject
    @Override
    @Deprecated
    public void setRevisionNumber(long newRevision) {
        MemoryObject.this.objectState.setRevisionNumber(newRevision);
    }
    
    // implement IMemoryObject
    @Override
    @Deprecated
    public void incrementRevision() {
        XyAssert.xyAssert(!MemoryObject.this.syncState.eventQueue.transactionInProgess);
        if(MemoryObject.this.father != null) {
            // this increments the revisionNumber of the father and sets
            // this revNr to the revNr of the father
            MemoryObject.this.father.incrementRevision();
            setRevisionNumber(MemoryObject.this.father.getRevisionNumber());
        } else {
            XChangeLog log = MemoryObject.this.syncState.eventQueue.getChangeLog();
            if(log != null) {
                XyAssert.xyAssert(log.getCurrentRevisionNumber() > getRevisionNumber());
                setRevisionNumber(log.getCurrentRevisionNumber());
            } else {
                setRevisionNumber(getRevisionNumber() + 1);
            }
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
    @Deprecated
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
            XRevWritableField fieldState = this.objectState.createField(fieldId);
            XyAssert.xyAssert(getAddress().contains(fieldState.getAddress()));
            field = new MemoryField(this, this.syncState.eventQueue, fieldState);
        } else {
            this.objectState.addField(field.getState());
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
    @Deprecated
    public void delete() {
        for(XId fieldId : this) {
            IMemoryField field = getField(fieldId);
            field.delete();
        }
        for(XId fieldId : this.loadedFields.keySet()) {
            this.objectState.removeField(fieldId);
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
    @Deprecated
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
    
}
