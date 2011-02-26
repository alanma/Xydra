package org.xydra.store;

import org.xydra.store.impl.memory.SecureMemoryStore;


public class DelegatingSecureStoreQuotaExceptionTest extends AbstractSecureStoreQuotaExceptionTest {
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = new SecureMemoryStore();
		}
		return this.store;
	}
	
}
