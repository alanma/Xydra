package org.xydra.store.impl.gae.changes;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.impl.memory.MemoryAtomicEvent;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.value.XValue;
import org.xydra.index.XI;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;


/**
 * An implementation of {@link XFieldEvent} that can load the newValue
 * asynchronously from the GAE datastore without blocking before
 * {@link #getNewValue()} is called.
 * 
 * @author voelkel
 * @author kaidel
 */
@RunsInGWT(false)
class GaeFieldEvent extends MemoryAtomicEvent implements XFieldEvent {
    
    private static final long serialVersionUID = -4274165693986851623L;
    
    // the revision numbers before the event happened
    private long modelRevision, objectRevision, fieldRevision;
    
    /*
     * the new value, after the event happened (never null, newValue.get()
     * returns null for "delete" events)
     */
    private AsyncValue newValue;
    
    protected GaeFieldEvent(XId actor, XAddress target, AsyncValue newValue, ChangeType changeType,
            long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
            boolean implied) {
        super(target, changeType, actor, inTransaction, implied);
        
        assert target.getField() != null && fieldRevision >= -1;
        assert objectRevision >= -1 || objectRevision == REVISION_OF_ENTITY_NOT_SET
                || objectRevision == REVISION_NOT_AVAILABLE;
        assert modelRevision >= -1 || modelRevision == REVISION_OF_ENTITY_NOT_SET;
        
        assert newValue != null;
        this.newValue = newValue;
        
        this.modelRevision = modelRevision;
        this.objectRevision = objectRevision;
        this.fieldRevision = fieldRevision;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return getTarget();
    }
    
    @Override
    public XValue getNewValue() {
        return this.newValue.get();
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
    
    // Overloads of standard Java equals(), hashCode() and toString() methods.
    // These need to match other XFieldEvent implementations.
    
    @Override
    public boolean equals(Object object) {
        
        if(!super.equals(object)) {
            return false;
        }
        
        if(!(object instanceof XFieldEvent)) {
            return false;
        }
        XFieldEvent event = (XFieldEvent)object;
        
        if(!XI.equals(getNewValue(), event.getNewValue())) {
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
    public int hashCode() {
        
        int result = super.hashCode();
        
        // newValue
        result ^= (getNewValue() == null ? 0 : getNewValue().hashCode());
        
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
        sb.append("  GaeFieldEvent");
        
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
        sb.append(" ->*" + this.getNewValue() + "*");
        sb.append("                 (actor:'" + getActor() + "')");
        return sb.toString();
    }
    
}
