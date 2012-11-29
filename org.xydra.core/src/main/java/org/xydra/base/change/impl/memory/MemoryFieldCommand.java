package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.index.XI;


/**
 * An implementation of {@link XFieldCommand}
 */

public class MemoryFieldCommand extends MemoryAtomicCommand implements XFieldCommand {
    
    private static final long serialVersionUID = -1637754092944391876L;
    
    /**
     * Creates a new {@link XFieldCommand} of the add-type. Will add the given
     * {@link XValue} to the specified {@link XField}, if possible.
     * 
     * @param target The {@link XAddress} of the {@link XField} which value is
     *            to be set.
     * @param fieldRevision The current revision number of the {@link XField}
     *            which {@link XValue} is to be set. The field {@link XID} of
     *            the given address must not be null
     * @param newValue The new {@link XValue} for the specified {@link XField} -
     *            must not be null
     * @return Creates a new {@link XFieldCommand} of the add-type for the
     *         specified target.
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField} or if the given {@link XValue} is
     *             null
     */
    public static XFieldCommand createAddCommand(XAddress target, long fieldRevision,
            XValue newValue) {
        
        if(newValue == null)
            throw new IllegalArgumentException("an ADD command must have a non-null value");
        
        return new MemoryFieldCommand(target, ChangeType.ADD, fieldRevision, newValue);
    }
    
    /**
     * Creates a new {@link XFieldCommand} of the change-type. Will change the
     * current {@link XValue} of the specified {@link XField} to the given
     * {@link XValue}, if possible.
     * 
     * @param target The {@link XAddress} of the {@link XField} which value is
     *            to be set.
     * @param fieldRevision The current revision number of the {@link XField}
     *            which value is to be set. The field {@link XID} of the given
     *            address must not be null
     * @param newValue The new {@link XValue} for the specified {@link XField} -
     *            must not be null
     * @return Creates a new {@link XFieldCommand} of the change-type for the
     *         specified target.
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField}
     * @throws IllegalArgumentException if newValue is null
     */
    public static XFieldCommand createChangeCommand(XAddress target, long fieldRevision,
            XValue newValue) {
        
        if(newValue == null)
            throw new IllegalArgumentException("a CHANGE command must have a non-null value");
        
        return new MemoryFieldCommand(target, ChangeType.CHANGE, fieldRevision, newValue);
    }
    
    /**
     * Creates a new {@link XFieldCommand} of the remove-type. Will remove the
     * current {@link XValue} from the specified {@link XField}, if possible.
     * 
     * @param target The {@link XAddress} of the {@link XField} which value is
     *            to be set.
     * @param fieldRevision The current revision number of the {@link XField}
     *            which {@link XValue} is to be set. The field {@link XID} of
     *            the given address must not be null
     * @return Creates a new {@link XFieldCommand} of the remove-type for the
     *         specified target.
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an {@link XField}
     */
    public static XFieldCommand createRemoveCommand(XAddress target, long fieldRevision) {
        return new MemoryFieldCommand(target, ChangeType.REMOVE, fieldRevision, null);
    }
    
    private final XValue newValue;
    
    private MemoryFieldCommand(XAddress target, ChangeType changeType, long fieldRevision,
            XValue newValue) {
        super(target, changeType, fieldRevision);
        
        if(target.getField() == null)
            throw new IllegalArgumentException(
                    "targets of XFieldCommands must specify a field, was:" + target);
        
        this.newValue = newValue;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(!super.equals(object))
            return false;
        
        if(!(object instanceof XFieldCommand))
            return false;
        XFieldCommand command = (XFieldCommand)object;
        
        if(!XI.equals(this.newValue, command.getValue()))
            return false;
        
        return true;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return getTarget();
    }
    
    @Override
    public XValue getValue() {
        return this.newValue;
    }
    
    @Override
    public int hashCode() {
        
        int result = super.hashCode();
        
        // newValue
        result ^= (this.newValue == null ? 0 : this.newValue.hashCode());
        
        return result;
    }
    
    @Override
    public String toString() {
        String suffix = "";
        if(isForced())
            suffix += "' (forced)";
        else
            suffix += "' safe-r" + getRevisionNumber();
        suffix += " @" + getTarget();
        switch(getChangeType()) {
        case ADD:
            return "\nFieldCommand:  ADD value '" + this.newValue + suffix;
        case REMOVE:
            return "\nFieldCommand:  REMOVE value '" + suffix;
        case CHANGE:
            return "\nFieldCommand:  CHANGE value to '" + this.newValue + suffix;
        default:
            throw new RuntimeException("this field event should have never been created");
        }
    }
}
