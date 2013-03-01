package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.XI;


/**
 * An implementation of {@link XFieldEvent}.
 * 
 * @author voelkel
 * @author kaidel
 */
public class MemoryFieldEvent extends MemoryAtomicEvent implements XFieldEvent {
    
    private static final long serialVersionUID = -4274165693986851623L;
    
    /**
     * Creates an {@link XFieldEvent} of the add-type (an {@link XValue} was
     * added to the {@link XField} this event refers to)
     * 
     * @param actor The {@link XID} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XID}
     *            of the given address must not be null.
     * @param newValue The added {@link XValue} - must not be null
     * @param objectRevision The revision number of the {@link XObject} holding
     *            the {@link XField} this event refers to.
     * @param fieldRevision The revision number of the {@link XField} this event
     *            refers to
     * @param inTransaction sets whether this event occurred during a
     *            {@link XTransaction} or not
     * @return An {@link XFieldEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given revision number
     *             equals {@link XEvent#RevisionOfEntityNotSet}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XFieldEvent createAddEvent(XID actor, XAddress target, XValue newValue,
            long objectRevision, long fieldRevision, boolean inTransaction) {
        return createAddEvent(actor, target, newValue, RevisionOfEntityNotSet, objectRevision,
                fieldRevision, inTransaction);
    }
    
    public static XFieldEvent createFrom(XFieldEvent fe) {
        MemoryFieldEvent event = new MemoryFieldEvent(fe.getActor(), fe.getTarget(),
                fe.getNewValue(), fe.getChangeType(), fe.getOldModelRevision(),
                fe.getOldObjectRevision(), fe.getOldFieldRevision(), fe.inTransaction(),
                fe.isImplied());
        return event;
    }
    
    /**
     * Creates an {@link XFieldEvent} of the add-type (an {@link XValue} was
     * added to the {@link XField} this event refers to)
     * 
     * @param actor The {@link XID} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XID}
     *            of the given address must not be null.
     * @param newValue The added {@link XValue} - must not be null
     * @param modelRevision The revision number of the {@link XModel} holding
     *            the {@link XObject} holding the {@link XField} this event
     *            refers to.
     * @param objectRevision The revision number of the {@link XObject} holding
     *            the {@link XField} this event refers to.
     * @param fieldRevision The revision number of the {@link XField} this event
     *            refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return An {@link XFieldEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given revision number
     *             equals {@link XEvent#RevisionOfEntityNotSet}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XFieldEvent createAddEvent(XID actor, XAddress target, XValue newValue,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
        if(newValue == null) {
            throw new RuntimeException("newValue must not be null for field ADD events");
        }
        
        return new MemoryFieldEvent(actor, target, newValue, ChangeType.ADD, modelRevision,
                objectRevision, fieldRevision, inTransaction, false);
    }
    
    /**
     * for GWT only
     */
    protected MemoryFieldEvent() {
        super();
    }
    
    /**
     * Creates an {@link XFieldEvent} of the change-type (the {@link XValue} of
     * the {@link XField} this event refers to was changed)
     * 
     * @param actor The {@link XID} of the actor
     * @param target The {@link XAddress} where this event happened - field
     *            {@link XID} of the given address must not be null
     * @param newValue The new {@link XValue} - must not be null
     * @param objectRevision The revision number of the {@link XObject} holding
     *            the {@link XField} this event refers to
     * @param fieldRevision The revision number of the {@link XField} this event
     *            refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XFieldEvent} of the change-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given revision number
     *             equals {@link XEvent#RevisionOfEntityNotSet}
     * @throws IllegalArgumentException if either oldValue or newValue is null
     */
    public static XFieldEvent createChangeEvent(XID actor, XAddress target, XValue newValue,
            long objectRevision, long fieldRevision, boolean inTransaction) {
        return createChangeEvent(actor, target, newValue, RevisionOfEntityNotSet, objectRevision,
                fieldRevision, inTransaction);
    }
    
    /**
     * Creates an {@link XFieldEvent} of the change-type (the {@link XValue} of
     * the {@link XField} this event refers to was changed)
     * 
     * @param actor The {@link XID} of the actor
     * @param target The {@link XAddress} where this event happened - the field
     *            {@link XID} of the given must not be null
     * @param newValue The new {@link XValue} - must not be null
     * @param modelRevision The revision number of the {@link XModel} holding
     *            the {@link XObject} which holds the {@link XField} this event
     *            refers to
     * @param objectRevision The revision number of the {@link XObject} holding
     *            the {@link XField} this event refers to
     * @param fieldRevision The revision number of the {@link XField} this event
     *            refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XFieldEvent} of the change-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given revision number
     *             equals {@link XEvent#RevisionOfEntityNotSet}
     * @throws IllegalArgumentException if either oldValue or newValue is null
     */
    public static XFieldEvent createChangeEvent(XID actor, XAddress target, XValue newValue,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
        if(newValue == null) {
            throw new IllegalArgumentException("newValue must not be null for field CHANGE events");
        }
        
        return new MemoryFieldEvent(actor, target, newValue, ChangeType.CHANGE, modelRevision,
                objectRevision, fieldRevision, inTransaction, false);
    }
    
    /**
     * Creates an {@link XFieldEvent} of the remove-type (the {@link XValue} of
     * the {@link XField XFields} this event refers to was removed)
     * 
     * @param actor The {@link XID} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XID}
     *            of the given address must not be null.
     * @param objectRevision The revision number of the {@link XObject} holding
     *            the {@link XField} this event refers to.
     * @param fieldRevision The revision number of the {@link XField} this event
     *            refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing the value of a
     *            field whose containing object is also removed in the same
     *            transaction
     * @return An {@link XFieldEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given revision number
     *             equals {@link XEvent#RevisionOfEntityNotSet}
     * @throws IllegalArgumentException if oldValue is null
     */
    public static XFieldEvent createRemoveEvent(XID actor, XAddress target, long objectRevision,
            long fieldRevision, boolean inTransaction, boolean implied) {
        return createRemoveEvent(actor, target, RevisionOfEntityNotSet, objectRevision,
                fieldRevision, inTransaction, implied);
    }
    
    /**
     * Creates an {@link XFieldEvent} of the remove-type (the {@link XValue} of
     * the {@link XField XFields} this event refers to was removed)
     * 
     * @param actor The {@link XID} of the actor
     * @param target The {@link XAddress} of the target - field Id must not be
     *            null.
     * @param modelRevision The revision number of the {@link XModel} holding
     *            the {@link XObject} holding the {@link XField} this event
     *            refers to.
     * @param objectRevision The revision number of the {@link XObject} holding
     *            the {@link XField} this event refers to.
     * @param fieldRevision The revision number of the {@link XField} this event
     *            refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing the value of a
     *            field whose containing object is also removed in the same
     *            transaction
     * @return An {@link XFieldEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given revision number
     *             equals {@link XEvent#RevisionOfEntityNotSet}
     * @throws IllegalArgumentException if oldValue is null
     */
    public static XFieldEvent createRemoveEvent(XID actor, XAddress target, long modelRevision,
            long objectRevision, long fieldRevision, boolean inTransaction, boolean implied) {
        return new MemoryFieldEvent(actor, target, null, ChangeType.REMOVE, modelRevision,
                objectRevision, fieldRevision, inTransaction, implied);
    }
    
    // the revision numbers before the event happened
    private long modelRevision, objectRevision, fieldRevision;
    
    // the new value, after the event happened (null for "delete" events)
    private XValue newValue;
    
    // private constructor, use the createEvent-methods for instantiating a
    // MemoryFieldEvent.
    protected MemoryFieldEvent(XID actor, XAddress target, XValue newValue, ChangeType changeType,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
            boolean implied) {
        super(target, changeType, actor, inTransaction, implied);
        
        if(target.getField() == null || fieldRevision < 0) {
            throw new IllegalArgumentException("field Id and revision must be set for field events");
        }
        
        if(objectRevision < 0 && objectRevision != RevisionOfEntityNotSet
                && objectRevision != RevisionNotAvailable) {
            throw new IllegalArgumentException("invalid objectRevision: " + objectRevision);
        }
        
        if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
            throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
        }
        
        this.newValue = newValue;
        
        this.modelRevision = modelRevision;
        this.objectRevision = objectRevision;
        this.fieldRevision = fieldRevision;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(!super.equals(object)) {
            return false;
        }
        
        if(!(object instanceof XFieldEvent)) {
            return false;
        }
        XFieldEvent event = (XFieldEvent)object;
        
        if(!XI.equals(this.newValue, event.getNewValue())) {
            return false;
        }
        
        if(this.modelRevision != event.getOldModelRevision()) {
            return false;
        }
        
        long otherObjectRev = event.getOldObjectRevision();
        if(this.objectRevision != otherObjectRev) {
            if((this.objectRevision != XEvent.RevisionNotAvailable && otherObjectRev != XEvent.RevisionNotAvailable)) {
                return false;
            }
        }
        
        if(this.fieldRevision != event.getOldFieldRevision()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return getTarget();
    }
    
    @Override
    public XValue getNewValue() {
        return this.newValue;
    }
    
    @Override
    public long getOldFieldRevision() {
        return this.fieldRevision;
    }
    
    @Override
    public long getOldModelRevision() {
        return this.modelRevision;
    }
    
    @Override
    public long getOldObjectRevision() {
        return this.objectRevision;
    }
    
    @Override
    public int hashCode() {
        
        int result = super.hashCode();
        
        // newValue
        result ^= (this.newValue == null ? 0 : this.newValue.hashCode());
        
        // old revisions
        result += this.modelRevision;
        if(this.objectRevision != XEvent.RevisionOfEntityNotSet) {
            result += 0x3472089;
        }
        result += this.fieldRevision;
        
        return result;
    }
    
    @Override
    public String toString() {
        String prefix = "MFieldEvent by actor: '" + getActor() + "' ";
        String suffix = " @" + getTarget() + " r" + rev2str(this.modelRevision) + "/"
                + rev2str(this.objectRevision) + "/" + rev2str(this.fieldRevision);
        if(inTransaction()) {
            suffix += " [inTxn]";
        }
        switch(getChangeType()) {
        case ADD:
            return prefix + "ADD value:" + this.newValue + suffix;
        case REMOVE:
            return prefix + "REMOVE value " + suffix + (isImplied() ? " [implied]" : "");
        case CHANGE:
            return prefix + "CHANGE " + " to value:" + this.newValue + suffix;
        default:
            throw new RuntimeException("this field event should have never been created");
        }
    }
    
}
