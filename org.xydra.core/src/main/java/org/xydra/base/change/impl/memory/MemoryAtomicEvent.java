package org.xydra.base.change.impl.memory;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


@RunsInGWT(true)
abstract public class MemoryAtomicEvent implements XEvent {
    
    private static final long serialVersionUID = 4051446642240477244L;
    
    // The XId of the actor of this event.
    private XId actor;
    
    // The ChangeType
    private @NeverNull
    ChangeType changeType;
    
    // is this remove event implied by another event in the same transaction
    private boolean implied;
    
    // was this event part of a transaction or not?
    private boolean inTransaction;
    
    private XAddress target;
    
    protected MemoryAtomicEvent(XAddress target, ChangeType changeType, XId actor, boolean inTrans,
            boolean implied) {
        
        if(target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        
        XyAssert.xyAssert(
                !implied
                        || (inTrans && changeType == ChangeType.REMOVE && target.getParent() != null),
                "If an event is implied, it must be in a txn and... = assert !implied or inTrans&REM&hasParent. Reality: implied = %s, inTrans = %s, changeType = %s, target = %s, target.parent = %s",
                implied, inTrans, changeType, target, target.getParent());
        XyAssert.xyAssert(changeType != ChangeType.TRANSACTION);
        
        this.target = target;
        this.changeType = changeType;
        this.actor = actor;
        this.inTransaction = inTrans;
        this.implied = implied;
    }
    
    /**
     * GWT only
     */
    protected MemoryAtomicEvent() {
        
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(object == null) {
            return false;
        }
        
        if(!(object instanceof XAtomicEvent)) {
            return false;
        }
        XAtomicEvent event = (XAtomicEvent)object;
        
        if(this.inTransaction != event.inTransaction()) {
            return false;
        }
        
        if(this.implied != event.isImplied()) {
            return false;
        }
        
        return XI.equals(this.actor, event.getActor()) && this.changeType == event.getChangeType()
                && this.target.equals(event.getTarget());
    }
    
    @Override
    public XId getActor() {
        return this.actor;
    }
    
    @Override
    public ChangeType getChangeType() {
        return this.changeType;
    }
    
    /**
     * @return the {@link XId} of the {@link XField} holding the entity this
     *         event refers to (may be null)
     */
    public XId getFieldId() {
        return this.target.getField();
    }
    
    /**
     * @return the {@link XId} of the {@link XModel} holding the entity this
     *         event refers to (may be null)
     */
    public XId getModelId() {
        return this.target.getModel();
    }
    
    /**
     * @return the {@link XId} of the {@link XObject} holding the entity this
     *         event refers to (may be null)
     */
    public XId getObjectId() {
        return this.target.getObject();
    }
    
    @Override
    public long getOldFieldRevision() {
        return XEvent.RevisionOfEntityNotSet;
    }
    
    @Override
    public long getOldModelRevision() {
        return XEvent.RevisionOfEntityNotSet;
    }
    
    @Override
    public long getOldObjectRevision() {
        return XEvent.RevisionOfEntityNotSet;
    }
    
    /**
     * @return the {@link XId} of the {@link XRepository} holding the entity
     *         this event refers to (may be null)
     */
    public XId getRepositoryId() {
        return this.target.getRepository();
    }
    
    @Override
    public long getRevisionNumber() {
        
        long rev = getOldModelRevision();
        if(rev >= 0) {
            return rev + 1;
        }
        
        rev = getOldObjectRevision();
        if(rev >= 0) {
            return rev + 1;
        }
        
        rev = getOldFieldRevision();
        if(rev >= 0) {
            return rev + 1;
        }
        
        return 0;
    }
    
    @Override
    public XAddress getTarget() {
        return this.target;
    }
    
    @Override
    public int hashCode() {
        
        int result = 0;
        
        result ^= this.changeType.hashCode();
        
        // actor
        result ^= (this.actor != null) ? this.actor.hashCode() : 0;
        
        // target
        result ^= this.target.hashCode();
        
        return result;
    }
    
    @Override
    public boolean inTransaction() {
        return this.inTransaction;
    }
    
    @Override
    public boolean isImplied() {
        return this.implied;
    }
    
    protected String rev2str(long rev) {
        if(rev == XEvent.RevisionOfEntityNotSet) {
            return "-";
        } else if(rev == XEvent.RevisionNotAvailable) {
            return "?";
        } else {
            XyAssert.xyAssert(rev >= 0);
            return Long.toString(rev);
        }
    }
    
}
