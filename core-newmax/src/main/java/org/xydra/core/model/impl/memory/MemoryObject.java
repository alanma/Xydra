package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
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
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
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
    
    /**
     * delegates change events to local cache only, state change is handled by
     * {@link Executor} already
     */
    private final XRMOFChangeListener changeListener = new XRMOFChangeListener() {
        
        @Override
        public void onChangeEvent(XFieldEvent event) {
        }
        
        @Override
        public void onChangeEvent(XModelEvent event) {
        }
        
        @Override
        public void onChangeEvent(XObjectEvent event) {
            if(event.getChangeType() == ChangeType.REMOVE) {
                MemoryObject.this.loadedFields.remove(event.getFieldId());
            }
        }
        
        @Override
        public void onChangeEvent(XRepositoryEvent event) {
        }
    };
    
    /** The father-model of this MemoryObject */
    @CanBeNull
    private final IMemoryModel father;
    
    private final Map<XId,IMemoryField> loadedFields = new HashMap<XId,IMemoryField>();
    
    /** The snapshot-like runtime state */
    private final XRevWritableObject objectState;
    
    /**
     * Wrap an existing objectState.
     * 
     * @param father @NeverNull
     * @param objectState @NeverNull
     */
    public MemoryObject(IMemoryModel father, XRevWritableObject objectState) {
        super(father.getRoot());
        assert objectState != null;
        assert objectState.getAddress().getRepository() != null;
        assert objectState.getAddress().getModel() != null;
        
        this.father = father;
        this.objectState = objectState;
        this.objectState.setExists(true);
    }
    
    /**
     * @param root @CanBeNull if father is defined
     * @param father @CanBeNull if root is defined
     * @param actorId
     * @param passwordHash
     * @param objectAddress
     * @param objectState @CanBeNull
     * @param log
     * @param createObject Can only be true if state & log are null. If true, an
     *            initial create-this-object-event is added.
     */
    private MemoryObject(Root root, IMemoryModel father, XId actorId, String passwordHash,
            XAddress objectAddress, XRevWritableObject objectState, XChangeLogState log,
            boolean createObject) {
        super(father == null ? root : father.getRoot());
        assert father != null || root != null;
        
        if(objectState == null) {
            XRevWritableObject newObjectState = new SimpleObject(objectAddress);
            if(father != null) {
                // TODO good idea?
                newObjectState.setRevisionNumber(father.getRevisionNumber());
            } else {
                // TODO really?
                newObjectState.setRevisionNumber(XCommand.NONEXISTANT);
            }
            this.objectState = newObjectState;
        } else {
            this.objectState = objectState;
        }
        assert this.objectState != null;
        this.objectState.setExists(createObject || objectState != null);
        assert this.objectState.getAddress().getRepository() != null;
        assert this.objectState.getAddress().getModel() != null;
        assert this.objectState.getAddress().equals(objectAddress);
        
        if(createObject) {
            if(father != null) {
                XModelCommand createObjectCommand = MemoryModelCommand.createAddCommand(
                        father.getAddress(), true, getId());
                Executor.executeModelCommand(actorId, createObjectCommand, father.getState(), root,
                        null);
            }
            setExists(true);
        }
        
        this.father = father;
    }
    
    /**
     * Create a new stand-alone object (without father), which can be synced IFF
     * objectAddress is a full address and has an initial create-object command
     * in its changeLog
     * 
     * @param actorId
     * @param passwordHash
     * @param objectAddress @NeverNull
     */
    public MemoryObject(XId actorId, String passwordHash, XAddress objectAddress) {
        this(Root.createWithActor(objectAddress, actorId), null, actorId, passwordHash,
                objectAddress, null, null, true);
        
        assert objectAddress.getRepository() != null;
        assert objectAddress.getModel() != null;
        
        assert objectAddress != null;
    }
    
    /**
     * Create a new stand-alone object (without father), which can be synced IFF
     * objectAddress is a full address and has an initial create-object command
     * in its changeLog
     * 
     * @param actorId
     * @param passwordHash
     * @param objectId @NeverNull
     */
    public MemoryObject(XId actorId, String passwordHash, XId objectId) {
        this(actorId, passwordHash, XX.toAddress(XId.DEFAULT, XId.DEFAULT, objectId, null));
        
        assert objectId != null;
    }
    
    /**
     * Create a stand-alone object (without father) wrapping an existing state.
     * Can be synced IFF objectAddress is a full address and has an initial
     * create-object command in its changeLog.
     * 
     * Used for de-serialisation.
     * 
     * @param actorId
     * @param passwordHash
     * @param objectState @NeverNull
     * @param log @CanBeNull
     */
    public MemoryObject(XId actorId, String passwordHash, XRevWritableObject objectState,
            XChangeLogState log) {
        this(Root.createWithActor(objectState.getAddress(), actorId), null, actorId, passwordHash,
                objectState.getAddress(), objectState, log, true);
        
        assert objectState != null;
        assert objectState.getAddress().getRepository() != null;
        assert objectState.getAddress().getModel() != null;
    }
    
    @Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(getRoot()) {
            return getRoot().addListenerForFieldEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(getRoot()) {
            return getRoot().addListenerForObjectEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(getRoot()) {
            return getRoot().addListenerForTransactionEvents(getAddress(), changeListener);
        }
    }
    
    public int countUnappliedLocalChanges() {
        return this.getRoot().countUnappliedLocalChanges();
    }
    
    @Override
    public IMemoryField createField(XId fieldId) {
        XObjectCommand command = MemoryObjectCommand.createAddCommand(getAddress(), true, fieldId);
        
        // synchronize so that return is never null if command succeeded
        synchronized(getRoot()) {
            long result = executeObjectCommand(command);
            IMemoryField field = getField(fieldId);
            XyAssert.xyAssert(result == XCommand.FAILED || field != null, "result=" + result
                    + " field=" + field);
            return field;
        }
    }
    
    @Override
    public XRevWritableObject createSnapshot() {
        synchronized(getRoot()) {
            if(exists()) {
                return XCopyUtils.createSnapshot(this);
            } else {
                return null;
            }
        }
    }
    
    @ReadOperation
    @Override
    public boolean equals(Object object) {
        synchronized(getRoot()) {
            return super.equals(object);
        }
    }
    
    @Override
    public long executeCommand(XCommand command) {
        if(command instanceof XTransaction) {
            synchronized(getRoot()) {
                long result = Executor.executeObjectTransaction(getRoot().getSessionActor(),
                        (XTransaction)command, getState(), getRoot(), this.changeListener);
                return result;
            }
        }
        if(command instanceof XObjectCommand) {
            return executeObjectCommand((XObjectCommand)command);
        }
        if(command instanceof XFieldCommand) {
            IMemoryField field = getField(command.getTarget().getField());
            if(field != null) {
                return field.executeFieldCommand((XFieldCommand)command);
            } else {
                return XCommand.FAILED;
            }
        }
        throw new IllegalArgumentException("Unknown command type: " + command);
    }
    
    @Override
    public long executeObjectCommand(XObjectCommand command) {
        synchronized(getRoot()) {
            assertThisEntityExists();
            
            XRevWritableModel modelState = null;
            if(this.father != null) {
                modelState = this.father.getState();
            }
            
            return Executor.executeObjectCommand(getRoot().getSessionActor(), command, modelState,
                    this.objectState, getRoot(), this.changeListener);
        }
    }
    
    // implement IMemoryObject
    @Override
    public void fireFieldEvent(XFieldEvent event) {
        synchronized(getRoot()) {
            getRoot().fireFieldEvent(getAddress(), event);
        }
    }
    
    // implement IMemoryObject
    @Override
    public void fireObjectEvent(XObjectEvent event) {
        synchronized(getRoot()) {
            getRoot().fireObjectEvent(getAddress(), event);
        }
    }
    
    // implement IMemoryObject
    @Override
    public void fireTransactionEvent(XTransactionEvent event) {
        synchronized(getRoot()) {
            getRoot().fireTransactionEvent(getAddress(), event);
        }
    }
    
    @Override
    public XAddress getAddress() {
        synchronized(getRoot()) {
            return this.objectState.getAddress();
        }
    }
    
    public XWritableChangeLog getChangeLog() {
        return this.getRoot().getWritableChangeLog();
    }
    
    @Override
    public IMemoryModel getFather() {
        return this.father;
    }
    
    @Override
    public IMemoryField getField(XId fieldId) {
        synchronized(getRoot()) {
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
        synchronized(getRoot()) {
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
    
    @Override
    public long getRevisionNumber() {
        synchronized(getRoot()) {
            return exists() ? this.objectState.getRevisionNumber() : XCommand.NONEXISTANT;
        }
    }
    
    public XId getSessionActor() {
        return this.getRoot().getSessionActor();
    }
    
    public String getSessionPasswordHash() {
        return this.getRoot().getSessionPasswordHash();
    }
    
    // implement IMemoryObject
    @Override
    public XRevWritableObject getState() {
        return this.objectState;
    }
    
    public long getSynchronizedRevision() {
        return getRoot().getSynchronizedRevision();
    }
    
    @Override
    public XType getType() {
        return XType.XOBJECT;
    }
    
    @Override
    public boolean hasField(XId id) {
        synchronized(getRoot()) {
            assertThisEntityExists();
            return this.objectState.hasField(id);
        }
    }
    
    @ReadOperation
    @Override
    public int hashCode() {
        synchronized(getRoot()) {
            return super.hashCode();
        }
    }
    
    @Override
    public boolean isEmpty() {
        synchronized(getRoot()) {
            assertThisEntityExists();
            return this.objectState.isEmpty();
        }
    }
    
    public boolean isSynchronized() {
        return getRevisionNumber() <= getSynchronizedRevision();
    }
    
    @Override
    public Iterator<XId> iterator() {
        synchronized(getRoot()) {
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
    
    @Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(getRoot()) {
            return getRoot().removeListenerForFieldEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(getRoot()) {
            return getRoot().removeListenerForObjectEvents(getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(getRoot()) {
            return getRoot().removeListenerForTransactionEvents(getAddress(), changeListener);
        }
    }
    
    public void setSessionActor(XId actorId, String passwordHash) {
        getRoot().setSessionActor(actorId);
        getRoot().setSessionPasswordHash(passwordHash);
    }
    
    @ReadOperation
    @Override
    public String toString() {
        return this.getId() + " rev[" + this.getRevisionNumber() + "]" + " "
                + this.objectState.toString();
    }
    
    @Override
    public boolean exists() {
        return this.objectState.exists();
    }
    
    @Override
    public void setExists(boolean entityExists) {
        this.objectState.setExists(entityExists);
    }
    
}
