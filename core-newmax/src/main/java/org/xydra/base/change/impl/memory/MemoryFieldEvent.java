package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.value.XValue;
import org.xydra.core.change.RevisionConstants;
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
     * added to the field this event refers to)
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
     * @return An {@link XFieldEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XFieldEvent createAddEvent(XId actor, XAddress target, XValue newValue,
            long objectRevision, long fieldRevision, boolean inTransaction) {
        return createAddEvent(actor, target, newValue, REVISION_OF_ENTITY_NOT_SET, objectRevision,
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
     * added to the field this event refers to)
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
     * @return An {@link XFieldEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XFieldEvent createAddEvent(XId actor, XAddress target, XValue newValue,
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
     * the field this event refers to was changed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} where this event happened - field
     *            {@link XId} of the given address must not be null
     * @param newValue The new {@link XValue} - must not be null
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XFieldEvent} of the change-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if either oldValue or newValue is null
     */
    public static XFieldEvent createChangeEvent(XId actor, XAddress target, XValue newValue,
            long objectRevision, long fieldRevision, boolean inTransaction) {
        return createChangeEvent(actor, target, newValue, REVISION_OF_ENTITY_NOT_SET,
                objectRevision, fieldRevision, inTransaction);
    }
    
    /**
     * Creates an {@link XFieldEvent} of the change-type (the {@link XValue} of
     * the field this event refers to was changed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} where this event happened - the field
     *            {@link XId} of the given must not be null
     * @param newValue The new {@link XValue} - must not be null
     * @param modelRevision The revision number of the model holding the object
     *            which holds the field this event refers to
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XFieldEvent} of the change-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if either oldValue or newValue is null
     */
    public static XFieldEvent createChangeEvent(XId actor, XAddress target, XValue newValue,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
        if(newValue == null) {
            throw new IllegalArgumentException("newValue must not be null for field CHANGE events");
        }
        
        return new MemoryFieldEvent(actor, target, newValue, ChangeType.CHANGE, modelRevision,
                objectRevision, fieldRevision, inTransaction, false);
    }
    
    /**
     * Creates an {@link XFieldEvent} of the remove-type (the {@link XValue} of
     * the fields this event refers to was removed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the target - the field {@link XId}
     *            of the given address must not be null.
     * @param objectRevision The revision number of the object holding the field
     *            this event refers to.
     * @param fieldRevision The revision number of the field this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing the value of a
     *            field whose containing object is also removed in the same
     *            transaction
     * @return An {@link XFieldEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if oldValue is null
     */
    public static XFieldEvent createRemoveEvent(XId actor, XAddress target, long objectRevision,
            long fieldRevision, boolean inTransaction, boolean implied) {
        return createRemoveEvent(actor, target, REVISION_OF_ENTITY_NOT_SET, objectRevision,
                fieldRevision, inTransaction, implied);
    }
    
    /**
     * Creates an {@link XFieldEvent} of the remove-type (the {@link XValue} of
     * the fields this event refers to was removed)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the target - field Id must not be
     *            null.
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
     * @return An {@link XFieldEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     * @throws IllegalArgumentException if oldValue is null
     */
    public static XFieldEvent createRemoveEvent(XId actor, XAddress target, long modelRevision,
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
    protected MemoryFieldEvent(XId actor, XAddress target, XValue newValue, ChangeType changeType,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
            boolean implied) {
        super(target, changeType, actor, inTransaction, implied);
        
        if(target.getField() == null || fieldRevision < -1
                && fieldRevision != REVISION_OF_ENTITY_NOT_SET) {
            throw new IllegalArgumentException("fieldId ('" + target.getField()
                    + "') and revision (" + fieldRevision + ") must be set for field events");
        }
        /* objectRev can only be: defined, notSet, or notAvailable */
        if(objectRevision < -1 && objectRevision != REVISION_OF_ENTITY_NOT_SET
                && objectRevision != REVISION_NOT_AVAILABLE) {
            throw new IllegalArgumentException("invalid objectRevision: " + objectRevision);
        }
        /* modelRev can only be: define or notSet */
        if(modelRevision < -1 && modelRevision != REVISION_OF_ENTITY_NOT_SET) {
            throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
        }
        /* modelRev >= objectRev */
        if(modelRevision >= -1 && modelRevision < objectRevision) {
            throw new IllegalArgumentException("model revision(" + modelRevision
                    + ") cannot be smaller than object revision(" + objectRevision + ")");
        }
        /* modelRev defined && objectRev notSet && fieldRev defined => error */
        if(modelRevision >= -1 && objectRevision == REVISION_OF_ENTITY_NOT_SET
                && fieldRevision >= -1) {
            throw new IllegalArgumentException("An even cannot define a modelRev (" + modelRevision
                    + "), no object rev, but a fieldRev (" + fieldRevision + ")");
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
            if((this.objectRevision != XEvent.REVISION_NOT_AVAILABLE && otherObjectRev != XEvent.REVISION_NOT_AVAILABLE)) {
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
        if(this.objectRevision != XEvent.REVISION_OF_ENTITY_NOT_SET) {
            result += 0x3472089;
        }
        result += this.fieldRevision;
        
        return result;
    }
    
    /**
     * Format: {MOF}Event
     * 
     * r{mRev}/{oRev}/{fRev}
     * 
     * {'ADD'|'REMOVE'}
     * 
     * '[' {'+'|'-'} 'inTxn]' '[' {'+'|'-'} 'implied]'
     * 
     * @{target *{id/value}, where xRef = '-' for
     *          {@link RevisionConstants#REVISION_OF_ENTITY_NOT_SET} and '?' for
     *          {@link RevisionConstants#REVISION_NOT_AVAILABLE}.
     * 
     *          by actor: '{actorId}'
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("     FieldEvent");
        
        sb.append(" rev:");
        sb.append(rev2str(this.getRevisionNumber()));
        sb.append(" old:");
        sb.append(rev2str(this.getOldModelRevision()));
        sb.append("/");
        sb.append(rev2str(this.getOldObjectRevision()));
        sb.append("/");
        sb.append(rev2str(this.getOldFieldRevision()));
        
        addChangeTypeAndFlags(sb);
        sb.append(" @" + getTarget());
        XValue newValue = this.getNewValue();
        sb.append(" ->" + (newValue == null ? " X " : "*" + newValue + "*"));
        sb.append("                 (actor:'" + getActor() + "')");
        return sb.toString();
    }
    
}
