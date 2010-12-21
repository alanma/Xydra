package org.xydra.core.test.store;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommandFactory;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.memory.MemoryNoAccessRightsNoBatchNoAsyncStore;
import org.xydra.store.impl.memory.SynchronousNoAccessRightsStore;


public class MemoryAllowAllStoreTest extends AbstractAllowAllStoreTest {
	private XydraStore store;
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = getNewStore(new SynchronousNoAccessRightsStore(
			        new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.createUniqueID())));
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
}
