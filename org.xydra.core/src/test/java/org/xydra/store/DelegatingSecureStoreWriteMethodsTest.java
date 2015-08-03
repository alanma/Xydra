package org.xydra.store;

import org.xydra.store.impl.memory.SecureMemoryStore;


public class DelegatingSecureStoreWriteMethodsTest extends AbstractSecureStoreWriteMethodsTest {

	@Override
	protected XydraStore createStore() {
		if(this.store == null) {
			this.store = new SecureMemoryStore();
		}
		return this.store;
	}

}
