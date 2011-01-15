package org.xydra.store.test;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.store.impl.delegating.DelegatingAllowAllStore;
import org.xydra.store.impl.delegating.XydraAsyncBatchPersistence;


/**
 * This is an abstract class capsuling behaviour that is the same for all
 * instantiations of {@link DelegatingAllowAllStore}.
 * 
 * 
 * @author Kaidel
 * 
 */

public abstract class AbstractAllowAllStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	/**
	 * Returns a new instance of AllowAllStore initialized with the given
	 * {@link XydraAsyncBatchPersistence}. This makes it possible to reuse this
	 * test with different instantiations.
	 */
	public DelegatingAllowAllStore getNewStore(XydraAsyncBatchPersistence base) {
		return new DelegatingAllowAllStore(base);
	}
	
	/**
	 * QuotaException cannot be tested here (no access rights mean no
	 * QuotaException), so we can return what we want...
	 */
	@Override
	protected long getQuotaForBruteForce() {
		return 1;
	}
	
	/**
	 * Every user can do everything, so it doesn't matter what we return here
	 */
	@Override
	protected XID getCorrectUser() {
		return XX.createUniqueID();
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return "Test";
	}
	
	/**
	 * Incorrect users do not exist, so we'll need to return null and do nothing
	 * else
	 */
	@Override
	protected XID getIncorrectUser() {
		return null;
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return null;
	}
}
