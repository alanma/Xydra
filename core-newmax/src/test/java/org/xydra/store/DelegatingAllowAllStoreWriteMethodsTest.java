package org.xydra.store;

import org.xydra.base.change.XCommandFactory;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.store.impl.memory.MemoryPersistence;


public class DelegatingAllowAllStoreWriteMethodsTest extends AbstractAllowAllStoreWriteMethodsTest {
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XydraStore createStore() {
		if(this.store == null) {
			// using the standard repo Id
			this.store = getNewStore(new MemoryPersistence(XX.toId("data")));
		}
		
		return this.store;
	}
	
}
