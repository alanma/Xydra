package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.model.XRepository;


/**
 * An abstract test capsuling the methods that are the same for read- and
 * write-tests
 * 
 * All methods need to be implemented as described in their comments. Not
 * adhering to this guidelines might result in failing tests, even though the
 * code which is to be tested might be correct.
 * 
 * @author Kaidel
 * 
 */

public abstract class AbstractStoreTest {
	
	{
		LoggerTestHelper.init();
	}
	
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
	 * Returns the {@link XId} of an account, which is registered on the
	 * {@link XydraStore} returned by {@link #getStore()}.
	 * 
	 * Please note: This methods needs to return the XId of a user who has
	 * access to read everything on the {@link XydraStore} returned by
	 * {@link #getStore()} and at least write-access to the returned store
	 * itself. Otherwise this test will fail (although the implementation which
	 * is to be tested might work correctly).
	 */
	abstract protected XId getCorrectUser();
	
	/**
	 * Returns the correct password hash of the account which {@link XId} is
	 * returned by {{@link #getCorrectUser()}.
	 * 
	 * Please note: This method needs to return the correct password hash,
	 * otherwise all tests will fail, even though the implementation might work
	 * correctly.
	 * 
	 * @return
	 */
	abstract protected String getCorrectUserPasswordHash();
	
	/**
	 * Returns the {@link XId} of any account which is registered on the
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
	 * Please note: If you return an {@link XId}, you need to make sure that the
	 * String returned by {@link #getIncorrectUserPasswordHash()} is not the
	 * correct password hash for this user. Otherwise some test will fail, even
	 * though the implementation might work correctly.
	 * 
	 * @returns the {@link XId} of a registered account or null, if no incorrect
	 *          user combination could be provided (for example if your
	 *          XydraStore implementation doesn't care about access rights at
	 *          all)
	 * 
	 */
	abstract protected XId getIncorrectUser();
	
	/**
	 * Returns a password hash which is not the correct password hash for the
	 * account which {@link XId} is returned by {@link #getIncorrectUser()}.
	 * 
	 * Should return null, if the implementation of {@link XydraStore} which is
	 * returned by {@link #getStore()} cannot provide such a password hash (for
	 * example if the implementation does not implement any access right
	 * management)
	 * 
	 * Please note: If you return a password hash}, you need to make sure that
	 * it is not the correct password hash for the account which {@link XId} is
	 * returned by {@link #getIncorrectUser()}. Otherwise some test will fail,
	 * even though the implementation might work correctly.
	 * 
	 * @return an incorrect password hash for the account which {@link XId} is
	 *         returned by {@link #getIncorrectUser()} or null if it's not
	 *         possible to provide such a hash
	 */
	abstract protected String getIncorrectUserPasswordHash();
	
	/**
	 * Returns the {@link XId} of the {@link XRepository} used by the
	 * {@link XydraStore} returned by {@link #getStore()}.
	 * 
	 * @return the {@link XId} of the {@link XRepository} used by the
	 *         {@link XydraStore} returned by {@link #getStore()}
	 */
	abstract protected XId getRepositoryId();
	
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
	protected boolean waitOnCallback(SynchronousCallbackWithOneResult<?> callback) {
		int value = callback.waitOnCallback(this.getCallbackTimeout());
		if(value == SynchronousCallbackWithOneResult.UNKNOWN_ERROR) {
			throw new RuntimeException("Unknown Error occurred");
		}
		if(value == SynchronousCallbackWithOneResult.TIMEOUT) {
			throw new RuntimeException("Timeout occurred");
		}
		if(callback.failure == callback.success) {
			throw new RuntimeException(
			        "Either both onSuccess and onFailure or neither of these two methods were called");
		}
		
		return value == SynchronousCallbackWithOneResult.SUCCESS;
	}
	
}
