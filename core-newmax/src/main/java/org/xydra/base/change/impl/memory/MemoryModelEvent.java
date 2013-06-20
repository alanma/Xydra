package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.core.change.RevisionConstants;


/**
 * An implementation of {@link XModelEvent}.
 * 
 * @author Kaidel
 */
@RunsInGWT(true)
public class MemoryModelEvent extends MemoryAtomicEvent implements XModelEvent {
    
    private static final long serialVersionUID = -598246000186155639L;
    
    /**
     * Creates a new {@link XModelEvent} of the add-type (an object was added to
     * the model this event refers to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the model this event refers to -
     *            model {@link XId} must not be null
     * @param objectId The {@link XId} of the added object - must not be null
     * @param modelRevision the revision number of the model this event refers
     *            to
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return An {@link XModelEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an model, if objectId is null or if the given
     *             revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     */
    public static XModelEvent createAddEvent(XId actor, XAddress target, XId objectId,
            long modelRevision, boolean inTransaction) {
        return new MemoryModelEvent(actor, target, objectId, ChangeType.ADD, modelRevision,
                REVISION_OF_ENTITY_NOT_SET, inTransaction, false);
    }
    
    public static XModelEvent createFrom(XModelEvent me) {
        MemoryModelEvent event = new MemoryModelEvent(me.getActor(), me.getTarget(),
                me.getObjectId(), me.getChangeType(), me.getOldModelRevision(),
                me.getOldObjectRevision(), me.inTransaction(), me.isImplied());
        return event;
    }
    
    /**
     * Creates a new {@link XModelEvent} of the remove-type (an object was
     * removed from the model this event refers to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the model this event refers to -
     *            model {@link XId} must not be null
     * @param objectId The {@link XId} of the removed object
     * @param modelRevision the revision number of the model this event refers
     *            to
     * @param objectRevision the revision number of the object which was removed
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @param implied sets whether this event describes removing an object whose
     *            containing model is also removed in the same transaction
     * @return An XModelEvent of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an model, if objectId is null or if one of the given
     *             revision numbers equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     */
    public static XModelEvent createRemoveEvent(XId actor, XAddress target, XId objectId,
            long modelRevision, long objectRevision, boolean inTransaction, boolean implied) {
        if(objectRevision < 0 && objectRevision != XEvent.REVISION_NOT_AVAILABLE) {
            throw new IllegalArgumentException(
                    "object revision must be set for model REMOVE events");
        }
        
        return new MemoryModelEvent(actor, target, objectId, ChangeType.REMOVE, modelRevision,
                objectRevision, inTransaction, implied);
    }
    
    // the revision numbers before the event happened
    private long modelRevision, objectRevision;
    
    // The XId of the object that was created/deleted
    private XId objectId;
    
    // private constructor, use the createEvent for instantiating MemModelEvents
    private MemoryModelEvent(XId actor, XAddress target, XId objectId, ChangeType changeType,
            long modelRevision, long objectRevision, boolean inTransaction, boolean implied) {
        super(target, changeType, actor, inTransaction, implied);
        
        if(target.getModel() == null || target.getObject() != null || target.getField() != null) {
            throw new IllegalArgumentException("target must refer to a model, was: " + target);
        }
        
        if(objectId == null) {
            throw new IllegalArgumentException("object Id must be set for model events, is null");
        }
        if(modelRevision < RevisionConstants.NOT_EXISTING) {
            throw new IllegalArgumentException("modelRevision (" + modelRevision
                    + ") must be set for model events");
        }
        
        if(objectRevision < RevisionConstants.NOT_EXISTING
                && objectRevision != REVISION_OF_ENTITY_NOT_SET
                && objectRevision != REVISION_NOT_AVAILABLE) {
            throw new IllegalArgumentException("invalid objectRevision: " + objectRevision);
        }
        
        this.objectId = objectId;
        this.objectRevision = objectRevision;
        this.modelRevision = modelRevision;
    }
    
    /**
     * GWT only
     */
    protected MemoryModelEvent() {
        
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(!super.equals(object)) {
            return false;
        }
        
        if(!(object instanceof XModelEvent)) {
            return false;
        }
        XModelEvent event = (XModelEvent)object;
        
        if(!this.objectId.equals(event.getObjectId())) {
            return false;
        }
        
        if(this.modelRevision != event.getOldModelRevision()) {
            return false;
        }
        
        if(this.objectRevision != event.getOldObjectRevision()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return Base.resolveObject(getTarget(), getObjectId());
    }
    
    @Override
    public XId getObjectId() {
        return this.objectId;
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
        result ^= this.objectId.hashCode();
        
        // old revisions
        result += this.modelRevision;
        result += this.objectRevision;
        
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
        sb.append("     ModelEvent");
        
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
        sb.append(" *" + this.objectId + "*");
        sb.append("                 (actor:'" + getActor() + "')");
        return sb.toString();
    }
    
    /**
     * Creates a new {@link XModelEvent} of the add-type (an object was added to
     * the model this event refers to), which exists to negate an already
     * performed event
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the model this event refers to -
     *            model {@link XId} must not be null
     * @param objectId The {@link XId} of the added object - must not be null
     * @param modelRevision the revision number of the model this event refers
     *            to
     * @param objectRevision the revision number that should be given to the
     *            newly created object
     * @param inTransaction sets whether this event occurred during an
     *            {@link XTransaction} or not
     * @return An {@link XModelEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an model, if objectId is null or if the given
     *             revision number equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     */
    public static XEvent createInternalAddEvent(XId actor, XAddress target, XId objectId,
            long modelRevision, long objectRevision, boolean inTransaction) {
        return new MemoryModelEvent(actor, target, objectId, ChangeType.ADD, modelRevision,
                objectRevision, inTransaction, false);
    }
}
