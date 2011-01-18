package org.xydra.store;

import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.test.AbstractAllowAllStoreReadMethodsTest;


public class DelegatingAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
	private XID repositoryID = XX.createUniqueID();
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = getNewStore(new MemoryPersistence(this.repositoryID));
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getRepositoryId() {
		return this.repositoryID;
	}
	
}
