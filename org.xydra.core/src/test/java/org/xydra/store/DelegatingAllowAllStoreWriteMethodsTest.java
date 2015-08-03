package org.xydra.store;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.impl.memory.MemoryPersistence;


public class DelegatingAllowAllStoreWriteMethodsTest extends AbstractAllowAllStoreWriteMethodsTest {

	@Override
	protected XCommandFactory getCommandFactory() {
		return BaseRuntime.getCommandFactory();
	}

	@Override
	protected XydraStore createStore() {
		if(this.store == null) {
			// using the standard repo Id
			this.store = getNewStore(new MemoryPersistence(Base.toId("data")));
		}

		return this.store;
	}

}
