package org.xydra.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.index.query.Pair;
import org.xydra.log.DefaultLoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public abstract class AbstractStoreQuoateExceptionTest {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractStoreReadMethodsTest.class);
	
	static {
		LoggerFactory.setLoggerFactorySPI(new DefaultLoggerFactorySPI());
	}
	
	protected long bfQuota;
	private XID incorrectUser;
	
	protected String incorrectUserPass;
	
	protected XydraStore store;
	
	/**
	 * Return value sets the amount of time tests shall wait on callbacks (in
	 * milliseconds). Implementations of this abstract test may override this to
	 * use a custom value. Returned value must be greater than zero.
	 */
	protected long getCallbackTimeout() {
		return 1000;
	}
	
	/**
	 * @return an implementation of {@link XCommandFactory} which works with the
	 *         implementation of {@link XydraStore} returned by {
	 *         {@link #getStore()};
	 */
	abstract protected XCommandFactory getCommandFactory();
	
	/**
	 * Returns the {@link XID} of any account which is registered on the
	 * {@link XydraStore} returned by {@link #getStore()}. This method works
	 * together with {@link #getIncorrectUserPasswordHash()}. These two methods
	 * need to return a account-passwordhash combination which is incorrect,
	 * i.e. the password hash is not correct for this account.
	 * 
	 * Return null, if the implementation of {@link XydraStore} which is
	 * returned by {@link #getStore()} cannot provide such a combination (for
	 * example if the implementation does not implement any access right
	 * management)
	 * 
	 * Please note: If you return an {@link XID}, you need to make sure that the
	 * String returned by {@link #getIncorrectUserPasswordHash()} is not the
	 * correct password hash for this user. Otherwise some test will fail, even
	 * though the implementation might work correctly.
	 * 
	 * @returns the {@link XID} of a registered account or null, if no incorrect
	 *          user combination could be provided (for example if your
	 *          XydraStore implementation doesn't care about access rights at
	 *          all)
	 * 
	 */
	abstract protected XID getIncorrectUser();
	
	/**
	 * Returns a password hash which is not the correct password hash for the
	 * account which {@link XID} is returned by {@link #getIncorrectUser()}.
	 * 
	 * Should return null, if the implementation of {@link XydraStore} which is
	 * returned by {@link #getStore()} cannot provide such a password hash (for
	 * example if the implementation does not implement any access right
	 * management)
	 * 
	 * Please note: If you return a password hash}, you need to make sure that
	 * it is not the correct password hash for the account which {@link XID} is
	 * returned by {@link #getIncorrectUser()}. Otherwise some test will fail,
	 * even though the implementation might work correctly.
	 * 
	 * @return an incorrect password hash for the account which {@link XID} is
	 *         returned by {@link #getIncorrectUser()} or null if it's not
	 *         possible to provide such a hash
	 */
	abstract protected String getIncorrectUserPasswordHash();
	
	/**
	 * Return value sets the amount of allowed incorrect login tries before a
	 * QuotaException is thrown.
	 * 
	 * Implementations of this abstract test need to override this to return the
	 * specific quota of the XydraStore implementation which is to be tested.
	 * Return a value less than zero if your implementation does not support
	 * QuotaExceptions or such a value cannot be provided.
	 */
	abstract protected long getQuotaForBruteForce();
	
	/**
	 * @return an implementation of {@link XydraStore} which is to be tested
	 */
	abstract protected XydraStore getStore();
	
	/**
	 * Method for checking whether a callback succeeded or not. Waits until the
	 * operation/method the callback was passed to is finished or aborted by an
	 * error.
	 * 
	 * @param callback
	 * @return True, if the method which the callback was passed to succeeded,
	 *         false if it failed or some kind of error occurred
	 */
	protected boolean waitOnCallback(SynchronousTestCallback<?> callback) {
		int value = callback.waitOnCallback(this.getCallbackTimeout());
		if(value == SynchronousTestCallback.UNKNOWN_ERROR) {
			throw new RuntimeException("Unknown Error occurred");
		}
		if(value == SynchronousTestCallback.TIMEOUT) {
			throw new RuntimeException("Timeout occurred");
		}
		if(callback.failure == callback.success) {
			throw new RuntimeException(
			        "Either both onSuccess and onFailure or neither of these two methods were called");
		}
		
		return value == SynchronousTestCallback.SUCCESS;
	}
	
	@Before
	public void setUp() {
		this.store = this.getStore();
		
		if(this.store == null) {
			throw new RuntimeException("XydraStore could not be initalized in the setUp method!");
		}
		
		this.incorrectUser = this.getIncorrectUser();
		this.incorrectUserPass = this.getIncorrectUserPasswordHash();
		
		this.bfQuota = getQuotaForBruteForce();
	}
	
	// Test for checking if the QuoateException works for checkLogin
	@Test
	public void testCheckLoginQuotaException() {
		SynchronousTestCallback<Boolean> callback = null;
		
		assert this.bfQuota > 0;
		// Testing the quota exception
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<Boolean>();
			
			this.store.checkLogin(this.incorrectUser, this.incorrectUserPass, callback);
			
			boolean wait = waitOnCallback(callback);
			
			if(l <= this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertTrue(wait);
				assertNotNull(callback.getEffect());
				assertNull(callback.getException());
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				assertFalse("should now return a QuotaException, since we exceeded the quota; l = "
				        + l, wait);
				assertNull(callback.getEffect());
				assertNotNull(callback.getException());
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
		
	}
	
	// Testing the quota exception for getModelIds
	@Test
	public void testGetModelIdsQuotaException() {
		SynchronousTestCallback<Set<XID>> callback = null;
		
		assert this.bfQuota > 0;
		boolean foundQuotaException = false;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<Set<XID>>();
			
			log.info("logging in with wrong credentials " + l + " ...");
			this.store.getModelIds(this.incorrectUser, this.incorrectUserPass, callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l < this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
				foundQuotaException = true;
			}
		}
		assertTrue("We got a QuotaException", foundQuotaException);
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
	}
	
	// Testing the quota exception for getModelRevisions
	@Test
	public void testGetModelRevisionsQuotaException() {
		SynchronousTestCallback<BatchedResult<Long>[]> callback = null;
		XAddress[] tempArray = { XX.toAddress(XX.createUniqueID(), XX.createUniqueID(), null, null) };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
			
			this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l <= this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
	}
	
	// Testing the quota exception for getModelSnapshots
	@Test
	public void testGetModelSnapshotsQuotaExcpetion() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { XX.toAddress(XX.createUniqueID(),
		        XX.createUniqueID(), null, null) };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
			
			this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l <= this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
	}
	
	// Testing the quota exception for getObjectSnapshots
	@Test
	public void testGetObjectSnapshotsQuotaException() {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { XX.toAddress(XX.createUniqueID(),
		        XX.createUniqueID(), XX.createUniqueID(), null) };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
			
			this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l <= this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
		
	}
	
	// Testing the quota exception for getRepositoryId
	@Test
	public void testGetRepositoryIdQuotaException() {
		SynchronousTestCallback<XID> callback = null;
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<XID>();
			
			this.store.getRepositoryId(this.incorrectUser, this.incorrectUserPass, callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l < this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
	}
	
	// Testing the quota exception for executeCommands
	@Test
	public void testExecuteCommandsQuotaException() {
		SynchronousTestCallback<BatchedResult<Long>[]> callback = null;
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
			XCommand[] commands = new XCommand[] { this.getCommandFactory().createAddModelCommand(
			        XX.toId("data"), XX.createUniqueID(), true) };
			
			this.store.executeCommands(this.incorrectUser, this.incorrectUserPass, commands,
			        callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l < this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
	}
	
	// Testing the quota exception for executeCommandsAndGetEvents
	@Test
	public void testExecuteCommandsAndGetEventsQuotaException() {
		SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = null;
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 1; l++) {
			callback = new SynchronousTestCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();
			XCommand[] commands = new XCommand[] { this.getCommandFactory().createAddModelCommand(
			        XX.toId("data"), XX.createUniqueID(), true) };
			GetEventsRequest[] requests = new GetEventsRequest[1];
			
			this.store.executeCommandsAndGetEvents(this.incorrectUser, this.incorrectUserPass,
			        commands, requests, callback);
			
			assertFalse(waitOnCallback(callback));
			assertNull(callback.getEffect());
			assertNotNull(callback.getException());
			
			if(l < this.bfQuota) {
				// QuotaException shouldn't be thrown yet.
				assertFalse(callback.getException() instanceof QuotaException);
			} else {
				// should now return a QuotaException, since we exceeded the
				// quota
				assertTrue(callback.getException() instanceof QuotaException);
			}
		}
		
		// TODO also check if QuotaException keeps being thrown after the quota
		// was exceeded by one
	}
}
