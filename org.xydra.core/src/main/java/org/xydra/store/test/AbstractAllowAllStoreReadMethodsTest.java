package org.xydra.store.test;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.store.impl.memory.AllowAllStore;
import org.xydra.store.impl.memory.XydraNoAccessRightsStore;


/**
 * This is an abstract class capsuling behaviour that is the same for all
 * instantiations of {@link AllowAllStore}.
 * 
 * 
 * @author Kaidel
 * 
 */

public abstract class AbstractAllowAllStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	/**
	 * Returns a new instance of AllowAllStore initialized with the given
	 * {@link XydraNoAccessRightsStore}. This makes it possible to reuse this
	 * test with different instantiations.
	 */
	public AllowAllStore getNewStore(XydraNoAccessRightsStore base) {
		return new AllowAllStore(base);
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
