package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryEvent;


/**
 * An implementation of {@link XRepositoryEvent}
 * 
 * @author Kaidel
 * 
 */
@RunsInGWT(true)
public class MemoryRepositoryEvent extends MemoryAtomicEvent implements XRepositoryEvent {
    
    private static final long serialVersionUID = 4709068915672914712L;
    
    /**
     * Creates a new {@link XRepositoryEvent} of the add-type (an model
     * was added to the repository this event refers to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the repository which the
     *            model was added to - repository {@link XId} must not
     *            be null
     * @param modelId The {@link XId} of the added model - must not be
     *            null
     * @return An {@link XRepositoryEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an repository or if the given modelId is
     *             null
     */
    public static XRepositoryEvent createAddEvent(XId actor, XAddress target, XId modelId) {
        return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.ADD,
                RevisionOfEntityNotSet, false, false);
    }
    
    public static XRepositoryEvent createFrom(XRepositoryEvent re) {
        MemoryRepositoryEvent event = new MemoryRepositoryEvent(re.getActor(), re.getTarget(),
                re.getModelId(), re.getChangeType(), re.getOldModelRevision(), re.inTransaction(),
                re.isImplied());
        return event;
    }
    
    /**
     * Creates a new {@link XRepositoryEvent} of the add-type (an model
     * was added to the repository this event refers to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the repository which the
     *            model was added to - repository {@link XId} must not
     *            be null
     * @param modelId The {@link XId} of the added model - must not be
     *            null
     * @param modelRev
     * @param inTrans
     * @return An {@link XRepositoryEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an repository or if the given modelId is
     *             null
     */
    public static XRepositoryEvent createAddEvent(XId actor, XAddress target, XId modelId,
            long modelRev, boolean inTrans) {
        return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.ADD, modelRev, inTrans,
                false);
    }
    
    /**
     * Creates a new {@link XRepositoryEvent} of the remove-type (an
     * model was removed from the repository this event refers
     * to)
     * 
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the repository which the
     *            model was removed from - repository {@link XId} must
     *            not be null
     * @param modelId The {@link XId} of the removed model - must not
     *            be null
     * @param modelRevison of the remove event
     * @param inTrans if in transaction
     * @return An {@link XRepositoryEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an repository, if the given modelId is null
     *             or if the given modelRevision equals
     *             {@link XEvent#RevisionOfEntityNotSet}
     */
    public static XRepositoryEvent createRemoveEvent(XId actor, XAddress target, XId modelId,
            long modelRevison, boolean inTrans) {
        if(modelRevison < 0) {
            throw new IllegalArgumentException(
                    "model revision must be set for repository REMOVE events");
        }
        
        return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.REMOVE, modelRevison,
                inTrans, false);
    }
    
    // The XId of the model that was added/deleted
    private XId modelId;
    
    // the model revision before this event happened
    private long modelRevision;
    
    // private constructor, use the createEvent methods for instantiating a
    // MemoryRepositoryEvent
    private MemoryRepositoryEvent(XId actor, XAddress target, XId modelId, ChangeType changeType,
            long modelRevision, boolean inTrans, boolean implied) {
        super(target, changeType, actor, inTrans, implied);
        
        if(target.getRepository() == null || target.getModel() != null) {
            throw new IllegalArgumentException("target must refer to a repository, was: " + target);
        }
        
        if(modelId == null) {
            throw new IllegalArgumentException("model Id must be set for repository events");
        }
        
        if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
            throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
        }
        
        this.modelId = modelId;
        this.modelRevision = modelRevision;
    }
    
    /**
     * GWT only
     */
    protected MemoryRepositoryEvent() {
        
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(!super.equals(object)) {
            return false;
        }
        
        if(!(object instanceof XRepositoryEvent)) {
            return false;
        }
        XRepositoryEvent event = (XRepositoryEvent)object;
        
        if(!this.modelId.equals(event.getModelId())) {
            return false;
        }
        
        if(this.modelRevision != event.getOldModelRevision()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return Base.resolveModel(getTarget(), getModelId());
    }
    
    @Override
    public XId getModelId() {
        return this.modelId;
    }
    
    @Override
    public long getOldModelRevision() {
        return this.modelRevision;
    }
    
    @Override
    public int hashCode() {
        
        int result = super.hashCode();
        
        // newValue
        result ^= this.modelId.hashCode();
        
        // old revisions
        result += this.modelRevision;
        
        return result;
    }
    
    @Override
    public String toString() {
        String str = "RepositoryEvent by actor:" + getActor() + " " + getChangeType() + " modelId:"
                + this.modelId;
        if(this.modelRevision >= 0) {
            str += " r" + rev2str(this.modelRevision);
        }
        str += " @" + getTarget();
        if(inTransaction()) {
            str += " [inTxn]";
        }
        return str;
    }
    
}
