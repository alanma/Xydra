package org.xydra.store;

import org.junit.Ignore;
import org.xydra.base.X;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.impl.memory.SecureMemoryStore;


@Ignore
public class DelegatingSecureStoreQuotaExceptionTest extends AbstractSecureStoreQuotaExceptionTest {
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = new SecureMemoryStore();
		}
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
}
