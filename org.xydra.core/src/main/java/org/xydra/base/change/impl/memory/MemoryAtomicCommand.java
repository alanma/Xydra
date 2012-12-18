package org.xydra.base.change.impl.memory;

import java.io.Serializable;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


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
        
        if(revision < 0 && revision != XCommand.SAFE && revision != XCommand.FORCED)
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
    
    public XID getFieldId() {
        return this.target.getField();
    }
    
    /**
     * @return the {@link XID} of the {@link XModel} holding the entity this
     *         command will change (may be null)
     */
    public XID getModelId() {
        return this.target.getModel();
    }
    
    /**
     * @return the {@link XID} of the {@link XObject} holding the entity this
     *         command will change (may be null)
     */
    public XID getObjectId() {
        return this.target.getObject();
    }
    
    /**
     * @return the {@link XID} of the {@link XRepository} holding the entity
     *         this command will change (may be null)
     */
    public XID getRepositoryId() {
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
    
}
