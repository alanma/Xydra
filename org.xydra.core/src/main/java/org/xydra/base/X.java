package org.xydra.base;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.impl.memory.MemoryCommandFactory;
import org.xydra.base.value.XValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.base.value.impl.memory.MemoryValueFactory;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.MemoryStringIDProvider;


/**
 * A utility class that provides helpful methods concerning the set-up of Xydra.
 * 
 * @author voelkel
 * @author kaidel
 * 
 */
public class X {
    
    private static XCommandFactory commandFactory;
    
    public static final String DEFAULT_REPOSITORY_ID = "repo";
    
    private static XIdProvider idProvider;
    
    private static XValueFactory valueFactory;
    
    /**
     * Creates an {@link XRepository} implementation that lives in-memory with
     * default no security.
     * 
     * @param actorId of the new memory repository
     * 
     * @return the new repository with ID = {@link X}#DEFAULT_REPOSITORY_ID.
     */
    public static XRepository createMemoryRepository(XId actorId) {
        XId repoId = getIDProvider().fromString(DEFAULT_REPOSITORY_ID);
        return createMemoryRepository(actorId, null, repoId);
    }
    
    /**
     * Creates an {@link XRepository} implementation that lives in-memory.
     * 
     * @param actorId of the new memory repository
     * @param passwordHash use null for none
     * @param repoId XId of new memory repository
     * 
     * @return the new repository
     */
    public static XRepository createMemoryRepository(XId actorId, String passwordHash, XId repoId) {
        return new MemoryRepository(actorId, passwordHash, repoId);
    }
    
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
     * IMPROVE Maybe we should think about a way to persist the XIdProvider in
     * the future to really ensure unique random Ids
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
