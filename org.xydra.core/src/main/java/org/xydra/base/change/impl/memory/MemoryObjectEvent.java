package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransaction;


/**
 * An implementation of {@link XObjectEvent}
 * 
 * @author Kaidel
 * 
 */
@RunsInGWT(true)
public class MemoryObjectEvent extends MemoryAtomicEvent implements XObjectEvent {
    
    private static final long serialVersionUID = 6129548600082005223L;
    
    /**
     * Creates a new {@link XObjectEvent} of the add-type (an field was
     * added to the object this event refers to)
     * 
     * @param actorId The {@link XId} of the actor
     * @param target The {@link XAddress} of the object to which the
     *            field was added - object {@link XId} must not be null
     * @param fieldId The {@link XId} of the added field - must not be
     *            null
     * @param objectRevision the revision number of the object this
     *            event refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XObjectEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an object, if the given fieldId is null or
     *             if the given objectRevision equals
     *             {@link XEvent#RevisionOfEntityNotSet}.
     */
    public static XObjectEvent createAddEvent(XId actorId, XAddress target, XId fieldId,
            long objectRevision, boolean inTransaction) {
        return createAddEvent(actorId, target, fieldId, RevisionOfEntityNotSet, objectRevision,
                inTransaction);
    }
    
    public static XObjectEvent createFrom(XObjectEvent oe) {
        MemoryObjectEvent event = new MemoryObjectEvent(oe.getActor(), oe.getTarget(),
                oe.getFieldId(), oe.getChangeType(), oe.getOldModelRevision(),
                oe.getOldObjectRevision(), oe.getOldFieldRevision(), oe.inTransaction(),
                oe.isImplied());
        return event;
    }
    
    /**
     * Creates a new {@link XObjectEvent} of the add-type (an field was
     * added to the object this event refers to)
     * 
     * @param actorId The {@link XId} of the actor
     * @param target The {@link XAddress} of the object to which the
     *            fieldwas added - object {@link XId} must not be null
     * @param fieldId The {@link XId} of the added field - must not be
     *            null
     * @param modelRevision the revision number of the model holding
     *            the object this event refers to
     * @param objectRevision the revision number of the object this
     *            event refers to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return an {@link XObjectEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an object, if the given fieldId is null or
     *             if the given objectRevision equals
     *             {@link XEvent#RevisionOfEntityNotSet}.
     */
    public static XObjectEvent createAddEvent(XId actorId, XAddress target, XId fieldId,
            long modelRevision, long objectRevision, boolean inTransaction) {
        
        return new MemoryObjectEvent(actorId, target, fieldId, ChangeType.ADD, modelRevision,
                objectRevision, RevisionOfEntityNotSet, inTransaction, false);
    }
    
    /**
     * GWT only
     */
    protected MemoryObjectEvent() {
        
    }
    
    /**
     * Creates a new {@link XObjectEvent} of the remove-type (an field
     * was removed from the object this event refers to)
     * 
     * @param actorId The {@link XId} of the actor
     * @param target The {@link XAddress} of the object from which the
     *            field was removed - object {@link XId} must not be
     *            null
     * @param fieldId The {@link XId} of the removed field - must not
     *            be null
     * @param objectRevision the revision number of the object this
     *            event refers to
     * @param fieldRevision the revision number of the field which was
     *            removed
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing a field whose
     *            containing object is also removed in the same transaction
     * @return an {@link XObjectEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an object, if the given fieldId is null or
     *             if the given objectRevision equals
     *             {@link XEvent#RevisionOfEntityNotSet}.
     */
    public static XObjectEvent createRemoveEvent(XId actorId, XAddress target, XId fieldId,
            long objectRevision, long fieldRevision, boolean inTransaction, boolean implied) {
        return createRemoveEvent(actorId, target, fieldId, RevisionOfEntityNotSet, objectRevision,
                fieldRevision, inTransaction, implied);
        
    }
    
    /**
     * Returns an {@link XObjectEvent} of the remove-type (an field was
     * removed from the object this event refers to)
     * 
     * @param actorId The {@link XId} of the actor
     * @param target The {@link XAddress} of the object from which the
     *            field was removed - object {@link XId} must not be
     *            null
     * @param fieldId The {@link XId} of the removed field - must not
     *            be null
     * @param modelRevision the revision number of the model holding
     *            the object this event refers to
     * @param objectRevision the revision number of the object this
     *            event refers to
     * @param fieldRevision the revision number of the field which was
     *            removed
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing a field whose
     *            containing object is also removed in the same transaction
     * @return an {@link XObjectEvent} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an object, if the given fieldId is null or
     *             if the given objectRevision or fieldRevision equals
     *             {@link XEvent#RevisionOfEntityNotSet}.
     */
    public static XObjectEvent createRemoveEvent(XId actorId, XAddress target, XId fieldId,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
            boolean implied) {
        if(fieldRevision < 0) {
            throw new IllegalArgumentException(
                    "field revision must be set for object REMOVE events");
        }
        
        return new MemoryObjectEvent(actorId, target, fieldId, ChangeType.REMOVE, modelRevision,
                objectRevision, fieldRevision, inTransaction, implied);
    }
    
    // The XId of field that was created/deleted
    private XId fieldId;
    
    // the revision numbers before the event happened
    private long fieldRevision, objectRevision, modelRevision;
    
    // private constructor, use the createEvent methods for instantiating a
    // MemObjectEvent
    private MemoryObjectEvent(XId actor, XAddress target, XId fieldId, ChangeType changeType,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
            boolean implied) {
        super(target, changeType, actor, inTransaction, implied);
        
        if(target.getObject() == null || target.getField() != null) {
            throw new IllegalArgumentException("target must refer to an object, was: " + target);
        }
        
        if(fieldId == null) {
            throw new IllegalArgumentException("field Id must be set for object events");
        }
        
        if(objectRevision < 0 && objectRevision != RevisionNotAvailable) {
            throw new IllegalArgumentException("object revision must be set for object events");
        }
        
        if(fieldRevision < 0 && fieldRevision != RevisionOfEntityNotSet) {
            throw new IllegalArgumentException("invalid fieldRevision: " + fieldRevision);
        }
        
        if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
            throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
        }
        
        this.fieldId = fieldId;
        this.modelRevision = modelRevision;
        this.objectRevision = objectRevision;
        this.fieldRevision = fieldRevision;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(!super.equals(object)) {
            return false;
        }
        
        if(!(object instanceof XObjectEvent)) {
            return false;
        }
        XObjectEvent event = (XObjectEvent)object;
        
        if(!this.fieldId.equals(event.getFieldId())) {
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
        return Base.resolveField(getTarget(), getFieldId());
    }
    
    @Override
    public XId getFieldId() {
        return this.fieldId;
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
        result ^= this.fieldId.hashCode();
        
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
        String str = "ObjectEvent by actor: '" + getActor() + "' " + getChangeType() + " '"
                + this.fieldId + "'";
        if(this.fieldRevision >= 0)
            str += " r" + rev2str(this.fieldRevision);
        str += " @" + getTarget();
        str += " r" + rev2str(this.modelRevision) + "/" + rev2str(this.objectRevision);
        if(isImplied()) {
            str += " [implied]";
        }
        if(inTransaction()) {
            str += " [inTxn]";
        }
        return str;
    }
    
}
