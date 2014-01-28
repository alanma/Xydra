package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;


public class DelegatingAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
    
    private XId repositoryId = XX.createUniqueId();
    
    @Override
    protected XCommandFactory getCommandFactory() {
        return X.getCommandFactory();
    }
    
    @Override
    protected XId getRepositoryId() {
        return this.repositoryId;
    }
    
    @Override
    protected XydraPersistence createPersistence() {
        return new MemoryPersistence(getRepositoryId());
    }
    
}
