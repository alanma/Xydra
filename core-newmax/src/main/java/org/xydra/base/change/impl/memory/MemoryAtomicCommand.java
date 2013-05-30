package org.xydra.base.change.impl.memory;

import java.io.Serializable;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.core.change.RevisionConstants;


@RunsInGWT(true)
abstract public class MemoryAtomicCommand implements XAtomicCommand, Serializable {
    
    private static final long serialVersionUID = -4547419646736034654L;
    
    @NeverNull
    private ChangeType changeType;
    
    private long revision;
    
    private XAddress target;
    
    /** For GWT serialisation only - Do not use. */
    public MemoryAtomicCommand() {
    }
    
    protected MemoryAtomicCommand(XAddress target, ChangeType changeType, long revision) {
        
        if(target == null)
            throw new NullPointerException("target must not be null");
        
        if(revision < -1 && revision != XCommand.SAFE_STATE_BOUND && revision != XCommand.FORCED
                && revision != RevisionConstants.REVISION_OF_ENTITY_NOT_SET)
            throw new RuntimeException("invalid revison: " + revision);
        
        this.target = target;
        this.changeType = changeType;
        this.revision = revision;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(object == null)
            return false;
        
        if(!(object instanceof XAtomicCommand))
            return false;
        XAtomicCommand command = (XAtomicCommand)object;
        
        return this.revision == command.getRevisionNumber()
                && this.changeType == command.getChangeType()
                && this.target.equals(command.getTarget());
    }
    
    @Override
    public ChangeType getChangeType() {
        return this.changeType;
    }
    
    public XId getFieldId() {
        return this.target.getField();
    }
    
    /**
     * @return the {@link XId} of the model holding the entity this command will
     *         change (may be null)
     */
    public XId getModelId() {
        return this.target.getModel();
    }
    
    /**
     * @return the {@link XId} of the object holding the entity this command
     *         will change (may be null)
     */
    public XId getObjectId() {
        return this.target.getObject();
    }
    
    /**
     * @return the {@link XId} of the repository holding the entity this command
     *         will change (may be null)
     */
    public XId getRepositoryId() {
        return this.target.getRepository();
    }
    
    @Override
    public long getRevisionNumber() {
        return this.revision;
    }
    
    @Override
    public XAddress getTarget() {
        return this.target;
    }
    
    @Override
    public int hashCode() {
        
        int result = 0;
        
        result ^= this.changeType.hashCode();
        
        // revision
        result ^= this.revision;
        
        // target
        result ^= this.target.hashCode();
        
        return result;
    }
    
    @Override
    public boolean isForced() {
        return this.revision == XCommand.FORCED;
    }
    
    public Intent getIntent() {
        if(this.revision == XCommand.FORCED)
            return Intent.Forced;
        else if(this.revision == XCommand.SAFE_STATE_BOUND)
            return Intent.SafeStateBound;
        else {
            assert this.revision >= -1;
            return Intent.SafeRevBound;
        }
    }
    
    protected void addChangeTypeTarget(StringBuilder sb) {
        sb.append(" ");
        switch(getChangeType()) {
        case ADD:
            sb.append("ADD   ");
            break;
        case REMOVE:
            sb.append("REMOVE");
            break;
        case CHANGE:
            sb.append("CHANGE");
            break;
        case TRANSACTION:
            sb.append("TXN   ");
            break;
        }
        sb.append(" @" + getTarget());
    }
    
    protected void addIntentRev(StringBuilder sb) {
        sb.append(" ");
        switch(this.getIntent()) {
        case Forced:
            sb.append("Forced");
            break;
        case SafeStateBound:
            sb.append("Safe(State)");
            break;
        case SafeRevBound:
            assert this.getRevisionNumber() >= RevisionConstants.NOT_EXISTING;
            sb.append("Safe(" + this.getRevisionNumber() + ")");
            break;
        }
    }
    
}
