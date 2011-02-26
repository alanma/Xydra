package org.xydra.store;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.access.HashUtils;


/**
 * @author Kaidel
 * 
 */

public abstract class AbstractSecureStoreQuotaExceptionTest extends
        AbstractStoreQuoateExceptionTest {
	
	@Override
	protected XID getIncorrectUser() {
		/*
		 * By definition of createUniqueID this ID is unknown an is therefore
		 * not registered in the accountDb
		 */
		return XX.createUniqueID();
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
