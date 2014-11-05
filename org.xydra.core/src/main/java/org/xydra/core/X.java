package org.xydra.core;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;


/**
 * A utility class that provides helpful methods concerning the set-up of Xydra.
 * 
 * @author xamde
 * @author kaidel
 * 
 */
public class X extends BaseRuntime {
    
    /**
     * Creates an repository implementation that lives in-memory with default no
     * security.
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
     * Creates an repository implementation that lives in-memory.
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
    
}
