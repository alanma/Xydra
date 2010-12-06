package org.xydra.core.test.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.model.XID;
import org.xydra.store.AutorisationException;
import org.xydra.store.XydraStore;

/**
 * Abstract test class for classes implementing the {@link XydraStore} interface.
 * @author Kaidel
 *
 */

public abstract class AbstractStoreTest {
	
	private XydraStore store;
	private XID correctUser, incorrectUser;
	private String correctUserPass, incorrectUserPass;
	private long timeout;
	private long bfQuota;
	
	/**
	 * @return an implementation of {@link XydraStore}
	 */
	abstract protected XydraStore getStore(); 
	
	/**
	 * Stores an accountname with its correct passwordhash in the given XID/string
	 */
	abstract protected void getCorrectUser(XID account, String passwordhash);
	
	/**
	 * Stores an accountname with an incorrect passwordhash in the given XID/string
	 */
	abstract protected void getIncorrectUser(XID account, String passwordhash);
	
	/**
	 * @param timeout sets the amount of time tests shall wait on callbacks
	 */
	abstract protected void getCallbackTimeout(long timeout);
	
	/**
	 * @param quota sets the amount of allowed incorrect login tries before a QuotaException is thrown
	 */
	abstract protected void getQuotaForBruteForce(long quota);
	
	@Before
	public void setUp()  {
		this.store = this.getStore();
		
		getCorrectUser(this.correctUser, this.correctUserPass);
		getIncorrectUser(this.incorrectUser, this.incorrectUserPass);
		getCallbackTimeout(this.timeout);
		getQuotaForBruteForce(this.bfQuota);
	}
	
	@Test
	public void testCheckLogin() {
		TestCallback<Boolean> callback = new TestCallback<Boolean>();
		
		//Testing a login that should succeed
		this.store.checkLogin(correctUser, correctUserPass, callback);
		
		assertTrue(this.waitOnCallbackSuccess(callback));
		assertEquals(callback.getEffect(), true);
		assertEquals(callback.getException(), null);
		
		//Testing a login that should fail because of a wrong accountname-passwordhash combination
		callback = new TestCallback<Boolean>();
		
		this.store.checkLogin(incorrectUser, incorrectUserPass, callback);
		
		assertTrue(this.waitOnCallbackFailure(callback));
		assertEquals(callback.getEffect(), false);
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AutorisationException);
		
		//TODO How to test for other exception types like ConnectionException, TimeoutException etc.?
		
	}
	
	
	private boolean waitOnCallbackSuccess(TestCallback<?> callback) {
		int value = callback.waitOnCallback(this.timeout);
		if(value == TestCallback.UNKNOWN_ERROR) {
			return false;
		}
		if(value == TestCallback.TIMEOUT) {
			return false;
		}
		
		return value == TestCallback.SUCCESS;
	}
	
	private boolean waitOnCallbackFailure(TestCallback<?> callback) {
		int value = callback.waitOnCallback(this.timeout);
		if(value == TestCallback.UNKNOWN_ERROR) {
			return false;
		}
		if(value == TestCallback.TIMEOUT) {
			return false;
		}
		
		return value == TestCallback.FAILURE;
	}

}
