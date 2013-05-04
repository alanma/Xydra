package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.value.XValue;


/**
 * An implementation of {@link XFieldEvent}.
 * 
 * @author voelkel
 * @author kaidel
 */
@RunsInGWT(true)
public class MemoryReversibleFieldEvent extends MemoryFieldEvent implements XReversibleFieldEvent {
    
    /** For GWT only */
    protected MemoryReversibleFieldEvent() {
    }
    
    private static final long serialVersionUID = -2461624822886642985L;
    
    /**
     * Creates an {@link XReversibleFieldEvent} of the add-type (an
     * {@link XValue} was added to the field this event refers to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XId}
     *            of the given address must not be null.
     * @param newValue The added {@link XValue} - must not be null
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to.
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during a
     *            {@link XTransaction} or not
     * @return An {@link XReversibleFieldEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XReversibleFieldEvent createAddEvent(XId actor, XAddress target, XValue newValue,
            long objectRevision, long fieldRevision, boolean inTransaction) {
        return createAddEvent(actor, target, newValue, REVISION_OF_ENTITY_NOT_SET, objectRevision,
                fieldRevision, inTransaction);
    }
    
    /**
     * Creates an {@link XReversibleFieldEvent} of the add-type (an
     * {@link XValue} was added to the field this event refers to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XId}
     *            of the given address must not be null.
     * @param newValue The added {@link XValue} - must not be null
     * @param modelRevision The revision number of the model holding the object
     *            holding the field this event refers to.
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to.
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return An {@link XReversibleFieldEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XReversibleFieldEvent createAddEvent(XId actor, XAddress target, XValue newValue,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
        if(newValue == null) {
            throw new RuntimeException("newValue must not be null for field ADD events");
        }
        
        return new MemoryReversibleFieldEvent(actor, target, null, newValue, ChangeType.ADD,
                modelRevision, objectRevision, fieldRevision, inTransaction, false);
    }
    
    /**
     * Creates an {@link XReversibleFieldEvent} of the change-type (the
     * {@link XValue} of the field this event refers to was changed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} where this event happened - field
     *            {@link XId} of the given address must not be null
     * @param oldValue The old {@link XValue} - must not be null
     * @param newValue The new {@link XValue} - must not be null
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XReversibleFieldEvent} of the change-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if either oldValue or newValue is null
     */
    public static XReversibleFieldEvent createChangeEvent(XId actor, XAddress target,
            XValue oldValue, XValue newValue, long objectRevision, long fieldRevision,
            boolean inTransaction) {
        return createChangeEvent(actor, target, oldValue, newValue, REVISION_OF_ENTITY_NOT_SET,
                objectRevision, fieldRevision, inTransaction);
    }
    
    /**
     * Creates an {@link XReversibleFieldEvent} of the change-type (the
     * {@link XValue} of the field this event refers to was changed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} where this event happened - the field
     *            {@link XId} of the given must not be null
     * @param oldValue The old {@link XValue} - must not be null
     * @param newValue The new {@link XValue} - must not be null
     * @param modelRevision The revision number of the model holding the object
     *            which holds the field this event refers to
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XReversibleFieldEvent} of the change-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if either oldValue or newValue is null
     */
    public static XReversibleFieldEvent createChangeEvent(XId actor, XAddress target,
            XValue oldValue, XValue newValue, long modelRevision, long objectRevision,
            long fieldRevision, boolean inTransaction) {
        if(oldValue == null || newValue == null) {
            throw new IllegalArgumentException(
                    "oldValue and newValue must not be null for ´reversible field CHANGE events");
        }
        
        return new MemoryReversibleFieldEvent(actor, target, oldValue, newValue, ChangeType.CHANGE,
                modelRevision, objectRevision, fieldRevision, inTransaction, false);
    }
    
    /**
     * Creates an {@link XReversibleFieldEvent} of the remove-type (the
     * {@link XValue} of the field this event refers to was removed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XId}
     *            of the given address must not be null.
     * @param oldValue The removed {@link XValue} - must not be null
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to.
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing the value of a
     *            field whose containing object is also removed in the same
     *            transaction
     * @return An {@link XReversibleFieldEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if oldValue is null
     */
    public static XReversibleFieldEvent createRemoveEvent(XId actor, XAddress target,
            XValue oldValue, long objectRevision, long fieldRevision, boolean inTransaction,
            boolean implied) {
        return createRemoveEvent(actor, target, oldValue, REVISION_OF_ENTITY_NOT_SET, objectRevision,
                fieldRevision, inTransaction, implied);
    }
    
    /**
     * Creates an {@link XReversibleFieldEvent} of the remove-type (the
     * {@link XValue} of the fields this event refers to was removed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the target - field Id must not be
     *            null.
     * @param oldValue The added {@link XValue} - must not be null
     * @param modelRevision The revision number of the model holding the object
     *            holding the field this event refers to.
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to.
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing the value of a
     *            field whose containing object is also removed in the same
     *            transaction
     * @return An {@link XReversibleFieldEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if oldValue is null
     */
    public static XReversibleFieldEvent createRemoveEvent(XId actor, XAddress target,
            XValue oldValue, long modelRevision, long objectRevision, long fieldRevision,
            boolean inTransaction, boolean implied) {
        if(oldValue == null) {
            throw new IllegalArgumentException("oldValue must not be null for field REMOVE events");
        }
        
        return new MemoryReversibleFieldEvent(actor, target, oldValue, null, ChangeType.REMOVE,
                modelRevision, objectRevision, fieldRevision, inTransaction, implied);
    }
    
    // the old value, before the event happened (null for "add" events)
    private XValue oldValue;
    
    // private constructor, use the createEvent-methods for instantiating a
    // MemoryFieldEvent.
    private MemoryReversibleFieldEvent(XId actor, XAddress target, XValue oldValue,
            XValue newValue, ChangeType changeType, long modelRevision, long objectRevision,
            long fieldRevision, boolean inTransaction, boolean implied) {
        super(actor, target, newValue, changeType, modelRevision, objectRevision, fieldRevision,
                inTransaction, implied);
        
        this.oldValue = oldValue;
    }
    
    @Override
    public XValue getOldValue() {
        return this.oldValue;
    }
    
    @Override
    public String toString() {
        String prefix = "FieldEvent by " + getActor() + ": ";
        String suffix = " @" + getTarget() + " r" + rev2str(getOldModelRevision()) + "/"
                + rev2str(getOldObjectRevision()) + "/" + rev2str(getOldFieldRevision());
        switch(getChangeType()) {
        case ADD:
            return prefix + "ADD " + getNewValue() + suffix;
        case REMOVE:
            return prefix + "REMOVE " + this.oldValue + suffix + (isImplied() ? " [implied]" : "");
        case CHANGE:
            return prefix + "CHANGE " + this.oldValue + " to " + getNewValue() + suffix;
        default:
            throw new RuntimeException("this field event should have never been created");
        }
    }
    
}
