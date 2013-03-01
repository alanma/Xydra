package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.delegate.XydraPersistence;


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
	 * Every user can do everything, so it doesn't matter what we return here.
	 * We use 'DirkCanDoAll' because that is easier in the debugger.
	 */
	@Override
	protected XId getCorrectUser() {
		return XX.toId("DirkCanDoAll");
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
	protected XId getIncorrectUser() {
		return null;
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return null;
	}
	
	/**
	 * @param xydraPersistence
	 * @return a new instance of AllowAllStore initialized with the given
	 *         {@link DelegatingAllowAllStore}. This makes it possible to reuse
	 *         this test with different instantiations.
	 */
	public DelegatingAllowAllStore getNewStore(XydraPersistence xydraPersistence) {
		return new DelegatingAllowAllStore(xydraPersistence);
	}
}
