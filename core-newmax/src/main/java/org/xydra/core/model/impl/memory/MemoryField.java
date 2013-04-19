package org.xydra.core.model.impl.memory;

import java.io.Serializable;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XField}.
 * 
 * @author voelkel
 * @author Kaidel
 */
public class MemoryField extends AbstractMOFEntity implements XField, IMemoryField, Serializable,
        Synchronizable {
    
    private static final long serialVersionUID = -4390811955475742528L;
    
    /** the father-object which is holding this field (may be null) */
    @CanBeNull
    // TODO deprecate?
    private final IMemoryObject father;
    
    /** The internal runtime state, like a snapshot */
    private final XRevWritableField fieldState;
    
    /**
     * Create a stand-alone field that exists
     * 
     * This instance cannot be added directly to another object. However, the
     * internal state can be added to another object.
     * 
     * @param actorId The actor to be used in events generated by this field.
     * @param fieldState A {@link XRevWritableField} representing the initial
     *            state of this field. The {@link XField} will continue using
     *            this state object, so it must not be modified directly after
     *            wrapping it in an {@link XField} @NeverNull
     */
    public MemoryField(XId actorId, XRevWritableField fieldState) {
        super(Root.createWithActor(fieldState.getAddress(), actorId), true);
        this.fieldState = fieldState;
        this.father = null;
    }
    
    /**
     * Creates a new MemoryField with a father-{@link XObject}.
     * 
     * @param father The father-{@link XObject} of this MemoryField @NeverNull
     * @param fieldState A {@link XRevWritableField} representing the initial
     *            state of this field. The {@link XField} will continue using
     *            this state object, so it must not be modified directly after
     *            wrapping it in an {@link XField} @NeverNull
     */
    public MemoryField(IMemoryObject father, XRevWritableField fieldState) {
        super(father.getRoot(), true);
        if(fieldState == null) {
            throw new IllegalArgumentException("fieldState may not be null");
        }
        if(fieldState.getAddress().getObject() != null) {
            throw new IllegalArgumentException("must load field through containing object");
        }
        this.fieldState = fieldState;
        this.father = father;
    }
    
    /**
     * Create a stand-alone field that exists
     * 
     * This instance cannot be added directly to another object. However, the
     * internal state can be added to another object.
     * 
     * @param actorId The actor to be used in events generated by this field.
     * @param fieldId The {@link XId} for this MemoryField.
     */
    public MemoryField(XId actorId, XId fieldId) {
        this(actorId, new SimpleField(XX.toAddress(null, null, null, fieldId)));
    }
    
    /**
     * Adds the given {@link XFieldEventListener} to this MemoryField, if
     * possible.
     * 
     * @param changeListener The {@link XFieldEventListener} which is to be
     *            added
     * @return false, if the given {@link XFieldEventListener} was already
     *         registered on this MemoryField, true otherwise
     */
    @Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
        return this.root.addListenerForFieldEvents(getAddress(), changeListener);
    }
    
    @Override
    synchronized public XRevWritableField createSnapshot() {
        if(!exists()) {
            return null;
        }
        return XCopyUtils.createSnapshot(this);
    }
    
    @Override
    @ReadOperation
    public boolean equals(Object object) {
        synchronized(this.root) {
            return super.equals(object);
        }
    }
    
    @Override
    public long executeFieldCommand(XFieldCommand command) {
        synchronized(this.root) {
            assertThisEntityExists();
            
            XRevWritableObject objectState = null;
            XRevWritableModel modelState = null;
            if(this.father != null) {
                objectState = this.father.getState();
                if(this.father.getFather() != null) {
                    modelState = this.father.getFather().getState();
                }
            }
            
            return Executor.executeFieldCommand(getRoot().getSessionActor(), command, modelState,
                    objectState, this.fieldState, this.getRoot(), null);
        }
    }
    
    @Override
    public XAddress getAddress() {
        synchronized(this.root) {
            return this.fieldState.getAddress();
        }
    }
    
    @Override
    public IMemoryObject getFather() {
        return this.father;
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    @Override
    @ReadOperation
    public XId getId() {
        synchronized(this.root) {
            return this.fieldState.getId();
        }
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    @Override
    @ReadOperation
    public long getRevisionNumber() {
        synchronized(this.root) {
            return this.fieldState.getRevisionNumber();
        }
    }
    
    @Override
    public XId getSessionActor() {
        synchronized(this.root) {
            return this.root.getSessionActor();
        }
    }
    
    @Override
    public XType getType() {
        return XType.XFIELD;
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    @Override
    @ReadOperation
    public XValue getValue() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.fieldState.getValue();
        }
    }
    
    @Override
    public int hashCode() {
        synchronized(this.root) {
            return super.hashCode();
        }
    }
    
    @Override
    public boolean isEmpty() {
        synchronized(this.root) {
            assertThisEntityExists();
            return this.getValue() == null;
        }
    }
    
    @Override
    public boolean isSynchronized() {
        assert getFather() != null : "A standalone MemoryField can't be synchronized.";
        
        /*
         * If the field's revNo is smaller than its father's syncRevNo, the
         * field is synchronized.
         */
        return (getRevisionNumber() <= getFather().getSynchronizedRevision());
    }
    
    /**
     * Removes the given {@link XFieldEventListener} from this MemoryField.
     * 
     * @param changeListener The {@link XFieldEventListener} which is to be
     *            removed
     * @return true, if the given {@link XFieldEventListener} was registered on
     *         this MemoryField, false otherwise
     */
    
    @Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
        return this.root.removeListenerForFieldEvents(getAddress(), changeListener);
    }
    
    @Override
    public void setSessionActor(XId actorId) {
        this.getRoot().setSessionActor(actorId);
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    @Override
    @ModificationOperation
    public boolean setValue(XValue newValue) {
        /*
         * no synchronization necessary here (except that in
         * executeFieldCommand())
         */
        XFieldCommand command;
        if(newValue == null) {
            command = MemoryFieldCommand.createRemoveCommand(getAddress(), XCommand.FORCED);
        } else {
            command = MemoryFieldCommand.createAddCommand(getAddress(), XCommand.FORCED, newValue);
        }
        
        long result = executeFieldCommand(command);
        XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
        return result != XCommand.NOCHANGE;
    }
    
    @Override
    public String toString() {
        return this.getId() + " rev[" + this.getRevisionNumber() + "]";
    }
    
    @Override
    public Root getRoot() {
        return this.root;
    }
    
    @Override
    public Object getStateLock() {
        return this.fieldState;
    }
    
    // ------------- DEP
    
    // implement IMemoryField
    @Deprecated
    @Override
    public void setRevisionNumber(long newRevision) {
        this.fieldState.setRevisionNumber(newRevision);
        throw new RuntimeException("OUTDATED");
    }
    
    // implement IMemoryField
    @Override
    @Deprecated
    public void incrementRevision() {
        XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
        if(this.father != null) {
            // this increments the revisionNumber of the father and sets
            // this revNr to the
            // revNr of the father
            this.father.incrementRevision();
            setRevisionNumber(this.father.getRevisionNumber());
        } else {
            setRevisionNumber(getRevisionNumber() + 1);
        }
        throw new RuntimeException("OUTDATED");
    }
    
    /**
     * Set the new value, increase revision (if not in a transaction) and
     * enqueue the corresponding event.
     * 
     * The caller is responsible for handling synchronization, for checking that
     * this field has not been removed, for checking that the newValue is
     * actually different from the current value.
     */
    // implement IMemoryField
    @Override
    @Deprecated
    public void setValueInternal(XValue newValue) {
        
        XValue oldValue = getValue();
        
        XyAssert.xyAssert(!XI.equals(oldValue, newValue));
        
        boolean inTrans = this.eventQueue.transactionInProgess;
        
        this.fieldState.setValue(newValue);
        
        // check for field event type
        long modelRev = getModelRevisionNumber();
        long objectRev = getObjectRevisionNumber();
        long fieldRev = getRevisionNumber();
        XId actorId = this.eventQueue.getActor();
        XReversibleFieldEvent event = null;
        if((oldValue == null)) {
            XyAssert.xyAssert(newValue != null);
            assert newValue != null;
            event = MemoryReversibleFieldEvent.createAddEvent(actorId, getAddress(), newValue,
                    modelRev, objectRev, fieldRev, inTrans);
        } else {
            if(newValue == null) {
                // implies remove
                event = MemoryReversibleFieldEvent.createRemoveEvent(actorId, getAddress(),
                        oldValue, modelRev + 1, objectRev + 1, fieldRev + 1, inTrans, false);
            } else {
                XyAssert.xyAssert(!newValue.equals(oldValue));
                // implies change
                event = MemoryReversibleFieldEvent.createChangeEvent(actorId, getAddress(),
                        oldValue, newValue, modelRev, objectRev, fieldRev, inTrans);
            }
        }
        
        XyAssert.xyAssert(event != null);
        assert event != null;
        
        this.eventQueue.enqueueFieldEvent(this, event);
        
        // event propagation and revision number increasing happens after
        // all events were successful
        if(!inTrans) {
            
            // increment revision number
            incrementRevision();
            
            // dispatch events
            this.eventQueue.sendEvents();
            
        }
        throw new RuntimeException("OUTDATED");
    }
    
    /**
     * The {@link MemoryEventQueue} used by this MemoryField.
     * 
     * If this MemoryField is created by an {@link MemoryObject}, the event
     * queue used by the {@link MemoryObject} will be used.
     */
    // integrate in root?
    @Deprecated
    private MemoryEventQueue eventQueue;
    
    /**
     * Creates a new MemoryField with or without a father-{@link XObject}.
     * 
     * @param father The father-{@link XObject} of this MemoryField @NeverNull
     * @param eventQueue the {@link MemoryEventQueue} this MemoryField will use; @NeverNull
     * @param fieldState A {@link XRevWritableField} representing the initial
     *            state of this field. The {@link XField} will continue using
     *            this state object, so it must not be modified directly after
     *            wrapping it in an {@link XField} @NeverNull
     */
    @Deprecated
    protected MemoryField(IMemoryObject father, MemoryEventQueue eventQueue,
            XRevWritableField fieldState) {
        super(father.getRoot(), true);
        if(eventQueue == null) {
            throw new IllegalArgumentException("eventQueue may not be null");
        }
        if(fieldState == null) {
            throw new IllegalArgumentException("objectState may not be null");
        }
        
        this.eventQueue = eventQueue;
        this.fieldState = fieldState;
        this.father = father;
        throw new RuntimeException("OUTDATED");
    }
    
    /**
     * Cleans up a field that is being removed.
     */
    // implement IMemoryField
    @Override
    @Deprecated
    public void delete() {
        this.fieldState.setValue(null);
        this.exists = false;
        throw new RuntimeException("OUTDATED");
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XFieldEvent XFieldEvents} happening on this MemoryField.
     * 
     * @param event The {@link XFieldEvent} which will be propagated to the
     *            registered listeners.
     */
    
    // implement IMemoryField
    @Override
    @Deprecated
    public void fireFieldEvent(XFieldEvent event) {
        this.root.fireFieldEvent(getAddress(), event);
        throw new RuntimeException("OUTDATED");
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    // implement IMemoryField
    @Override
    @Deprecated
    public long executeFieldCommand(XFieldCommand command, XLocalChangeCallback callback) {
        synchronized(this.root) {
            assertThisEntityExists();
            return Executor.executeFieldCommand(this.getSessionActor(), command, this.father
                    .getFather().getState(), this.father.getState(), this.getState(), this
                    .getRoot(), null);
        }
    }
    
    @Deprecated
    private long getObjectRevisionNumber() {
        if(this.father != null)
            return this.father.getRevisionNumber();
        else
            return XEvent.RevisionOfEntityNotSet;
    }
    
    @Deprecated
    private long getModelRevisionNumber() {
        if(this.father != null)
            return this.father.getModelRevisionNumber();
        else
            return XEvent.RevisionOfEntityNotSet;
    }
    
    /**
     * Returns the father-{@link XObject} (the {@link XObject} containing this
     * MemoryField) of this MemoryField.
     * 
     * @return The father-{@link XObject} of this MemoryField - may be null if
     *         this MemoryField has no father-{@link XObject}
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    @ReadOperation
    // implement IMemoryField
    @Override
    @Deprecated
    public IMemoryObject getObject() {
        return this.father;
    }
    
    /**
     * @return the {@link XRevWritableField} object representing the current
     *         state of this MemoryField
     */
    // implement IMemoryField
    @Override
    @Deprecated
    public XRevWritableField getState() {
        return this.fieldState;
    }
}
