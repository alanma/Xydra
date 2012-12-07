package org.xydra.store;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.impl.memory.MemoryPersistence;


public class DelegatingAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
	private XID repositoryId = XX.createUniqueId();
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getRepositoryId() {
		return this.repositoryId;
	}
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = getNewStore(new MemoryPersistence(this.repositoryId));
		}
		
		return this.store;
	}
	
}
