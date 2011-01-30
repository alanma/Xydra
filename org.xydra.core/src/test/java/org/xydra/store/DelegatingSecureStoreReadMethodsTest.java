package org.xydra.store;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.impl.memory.SecureMemoryStore;


/**
 * TODO testing for quote exceptions is very slow, because the implementation
 * lets the caller waiting for a long time to throttle the hacking attempt - all
 * tests testing the quota stuff should go into another class so that this class
 * can also be used for performance tests.
 * 
 * @author xamde
 * 
 */
public class DelegatingSecureStoreReadMethodsTest extends AbstractSecureStoreReadMethodsTest {
	
	@Override
	protected XID getRepositoryId() {
		return XX.toId("data");
		// repositoryId as set in the standard constructor of {@link
		// MemoryStore}
	}
	
	@Override
	protected XydraStore getStore() {
		if(this.store == null) {
			this.store = new SecureMemoryStore();
		}
		return this.store;
	}
	
}
