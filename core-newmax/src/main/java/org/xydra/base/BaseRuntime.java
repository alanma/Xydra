package org.xydra.base;

import org.xydra.annotations.Setting;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.impl.memory.MemoryCommandFactory;
import org.xydra.base.id.MemoryStringIDProvider;
import org.xydra.base.value.XValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.base.value.impl.memory.MemoryValueFactory;


public class BaseRuntime {
    
    private static XCommandFactory commandFactory;
    
    @Setting("repositoryId")
    public static final String DEFAULT_REPOSITORY_ID = "repo";
    
    private static XIdProvider idProvider;
    
    private static XValueFactory valueFactory;
    
    /**
     * Returns the {@link XCommandFactory} instance of the Xydra Instance that
     * is currently being used. An {@link XCommandFactory} provides methods for
     * creating {@link XCommand}s of all types.
     * 
     * @return Returns the {@link XCommandFactory} of the Xydra Instance that is
     *         currently being used.
     */
    public static XCommandFactory getCommandFactory() {
        if(commandFactory == null) {
            commandFactory = new MemoryCommandFactory();
        }
        
        return commandFactory;
    }
    
    /**
     * Returns the {@link XIdProvider} instance of the Xydra Instance that is
     * currently being used. {@link XId}s should only be created using this
     * {@link XIdProvider} instance to ensure, that only unique {@link XId}s are
     * being used.
     * 
     * @return Returns the {@link XIdProvider} instance of the Xydra Instance
     *         that is currently being used.
     */
    public static XIdProvider getIDProvider() {
        if(idProvider == null) {
            idProvider = new MemoryStringIDProvider();
        }
        return idProvider;
    }
    
    /**
     * Returns the {@link XValueFactory} instance of the Xydra Instance that is
     * currently being used. An {@link XValueFactory} provides methods for
     * creating {@link XValue XValues} of all types.
     * 
     * @return Returns the {@link XValueFactory} of the Xydra Instance that is
     *         currently being used.
     */
    public static XValueFactory getValueFactory() {
        if(valueFactory == null) {
            valueFactory = new MemoryValueFactory();
        }
        
        return valueFactory;
    }
    
}
