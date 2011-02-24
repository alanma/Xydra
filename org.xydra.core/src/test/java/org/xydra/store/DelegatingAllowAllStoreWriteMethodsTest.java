package org.xydra.store;

import org.xydra.base.X;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.impl.memory.MemoryPersistence;


public class DelegatingAllowAllStoreWriteMethodsTest extends AbstractAllowAllStoreWriteMethodsTest {
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			// using the standard repo Id
			this.store = getNewStore(new MemoryPersistence(XX.toId("data")));
		}
		
		return this.store;
	}
	
}
