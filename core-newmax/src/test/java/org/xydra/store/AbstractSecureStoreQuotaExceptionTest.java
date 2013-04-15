package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.store.access.HashUtils;


/**
 * @author Kaidel
 * 
 */

public abstract class AbstractSecureStoreQuotaExceptionTest extends
        AbstractStoreQuotaExceptionTest {
	
	@Override
	protected XId getIncorrectUser() {
		/*
		 * By definition of createUniqueID this ID is unknown and is therefore
		 * not registered in the accountDb
		 */
		return XX.createUniqueId();
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash("incorrect");
	}
	
	@Override
	protected long getQuotaForBruteForce() {
		return XydraStore.MAX_FAILED_LOGIN_ATTEMPTS;
	}
}
