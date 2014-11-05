package org.xydra.base.change.impl.memory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;


/**
 * Implementation of the {@XTransaction} interface.
 * 
 * @author kaidel
 * @author dscharrer
 */
@RunsInGWT(true)
public class MemoryTransaction implements XTransaction {
    
    private static final long serialVersionUID = 2638954432914484202L;
    
    /**
     * @param target
     * @param commands
     * @return a new transaction with the specified target and commands. Changes
     *         to the passed {@link List} will not affect the transaction.
     */
    public static XTransaction createTransaction(XAddress target, List<XAtomicCommand> commands) {
        XAtomicCommand[] commandsCopy = new XAtomicCommand[commands.size()];
        commandsCopy = commands.toArray(commandsCopy);
        return new MemoryTransaction(target, commandsCopy);
    }
    
    /**
     * @param target
     * @param commands
     * @return a new transaction with the specified target and commands. Changes
     *         to the passed array will not affect the transaction.
     */
    public static XTransaction createTransaction(XAddress target, XAtomicCommand[] commands) {
        // create a copy so the array can't be modified from the outside
        XAtomicCommand[] commandsCopy = new XAtomicCommand[commands.length];
        System.arraycopy(commands, 0, commandsCopy, 0, commands.length);
        return new MemoryTransaction(target, commandsCopy);
    }
    
    private XAtomicCommand[] commands;
    
    /** The XAddress of the model or object this transaction applies to */
    // Impl. note: made non-final for GWT serialisation
    private XAddress target;
    
    /** WARN: For GWT only. Do not use. */
    public MemoryTransaction() {
    }
    
    private MemoryTransaction(XAddress target, XAtomicCommand[] commands) {
        
        if(commands.length == 0) {
            throw new RuntimeException("the command list must not be empty");
        }
        
        if((target.getModel() == null && target.getObject() == null) || target.getField() != null) {
            throw new RuntimeException("target must be a model or object, was:" + target);
        }
        
        for(int i = 0; i < commands.length; ++i) {
            
            if(
            
            (!target.equalsOrContains(commands[i].getTarget()))
            
            // allow repo commands if they concern this model
                    && !(target.getAddressedType() == XType.XMODEL
                            && commands[i] instanceof XRepositoryCommand && ((XRepositoryCommand)commands[i])
                            .getModelId().equals(target.getModel()))
            
            ) {
                throw new IllegalArgumentException("command #" + i + " " + commands[i]
                        + " is not contained in " + target);
            }
            
            // if(!(commands[i] instanceof XModelCommand || commands[i]
            // instanceof XObjectCommand || commands[i] instanceof
            // XFieldCommand)) {
            // throw new IllegalArgumentException("command #" + i + " " +
            // commands[i]
            // + " is not an XModelCommand, XObjectCommand or XFieldCommand.");
            // }
        }
        
        this.commands = commands;
        this.target = target;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(object == null)
            return false;
        
        if(!(object instanceof XTransaction))
            return false;
        XTransaction trans = (XTransaction)object;
        
        if(size() != trans.size())
            return false;
        
        if(!this.target.equals(trans.getTarget()))
            return false;
        
        for(int i = 0; i < size(); ++i) {
            
            if(!this.commands[i].equals(trans.getCommand(i)))
                return false;
            
        }
        
        return true;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return this.target;
    }
    
    @Override
    public ChangeType getChangeType() {
        return ChangeType.TRANSACTION;
    }
    
    @Override
    public XAtomicCommand getCommand(int index) {
        return this.commands[index];
    }
    
    @Override
    public XAddress getTarget() {
        return this.target;
    }
    
    @Override
    public int hashCode() {
        
        int result = 0;
        
        result ^= Arrays.hashCode(this.commands);
        
        // target
        result ^= this.target.hashCode();
        
        return result;
    }
    
    @Override
    public Iterator<XAtomicCommand> iterator() {
        return Arrays.asList(this.commands).iterator();
    }
    
    @Override
    public int size() {
        return this.commands.length;
    }
    
    @Override
    public String toString() {
        return "Transaction @" + this.target + ": \n" + Arrays.toString(this.commands);
    }
    
}
