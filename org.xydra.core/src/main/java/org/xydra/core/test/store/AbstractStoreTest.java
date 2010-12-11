package org.xydra.core.test.store;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.store.AutorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.QuotaException;
import org.xydra.store.XydraStore;

/**
 * Abstract test class for classes implementing the {@link XydraStore} interface.
 * 
 * This test assumes that this test alone operates on the {@link XydraStore} that is being tested. Some methods may 
 * fail if someone/something else operates on the same {@link XydraStore} at the same time, even though the 
 * {@link XydraStore} is working correctly.
 * 
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
	 * Stores a correct/existing accountname with its correct passwordhash in the given XID/string
	 */
	abstract protected void getCorrectUser(XID account, String passwordhash);
	
	/**
	 * Stores a correct/existing accountname with an incorrect passwordhash in the given XID/string
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
	
	/**
	 * Returns an array of {@link XAddress XAddresses} of {@link XXModel XModels} the actor with the given
	 * {@link XID} has at least read access to.
	 * @param accountID 
	 * @return an array of {@link XAddress XAddresses} of {@link XXModel XModels} the actor with the given
	 * {@link XID} has at least read access to.
	 */
	abstract protected XAddress[] getModelAddresses(XID accountID);
	
	/**
	 * Returns the {@link XAddress} of an {@link XModel} the given actor has no access to (not even read access!)
	 * @param accountID
	 * @return Returns the {@link XAddress} of an {@link XModel} the given actor has no access to 
	 * 			(not even read access!)
	 */
	abstract protected XAddress getModelAddressWithoutAccess(XID accountID);
	
	/**
	 * @return the {@link XAddress} of a not existing {@link XModel}
	 */
	abstract protected XAddress getNotExistingModelAddress();
	
	@Before
	public void setUp()  {
		this.store = this.getStore();
		
		getCorrectUser(this.correctUser, this.correctUserPass);
		getIncorrectUser(this.incorrectUser, this.incorrectUserPass);
		getCallbackTimeout(this.timeout);
		getQuotaForBruteForce(this.bfQuota);
	}
	
	/**
	 * Test for the checkLogin()-method
	 */
	@Test
	public void testCheckLogin() {
		TestCallback<Boolean> callback = new TestCallback<Boolean>();
		
		//Testing a login that should succeed
		this.store.checkLogin(correctUser, correctUserPass, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), true);
		assertNull(callback.getException());
		
		//Testing a login that should fail because of a wrong accountname-passwordhash combination
		callback = new TestCallback<Boolean>();
		
		this.store.checkLogin(incorrectUser, incorrectUserPass, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), false);
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AutorisationException);
		
		//Testing the quota exception
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new TestCallback<Boolean>();
			
			this.store.checkLogin(incorrectUser, incorrectUserPass, callback);
		}
		
		//should now return a QuotaException, since we exceeded the quota for failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), false);
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
		
		//TODO How to test for other exception types like ConnectionException, TimeoutException etc.?
		
	}
	
	/**
	 * Test for the getModelSnapshots-method
	 */
	@Test
	public void testGetModelSnapshots() {
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		XAddress noAccess = this.getModelAddressWithoutAccess(this.correctUser);
		XAddress doesntExist = this.getNotExistingModelAddress();
		TestCallback<BatchedResult<XBaseModel>[]> callback;
		
		//Test if it behaves correctly for wrong account + password combinations
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass, modelAddresses, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AutorisationException);
		
		//Test if it behaves correctly for addresses of XModels the user has access to
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertEquals(result.length, modelAddresses.length);
		
		//check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			assertEquals(modelAddresses[i], result[i].getResult().getAddress());
		}
		
		//Test if it behaves correctly for addresses of XModels the user has no access to
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] tempArray = new XAddress[]{noAccess};
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		result = callback.getEffect();
		assertNull(result[0].getResult());
		
		//Test if it behaves correctly for addresses of XModels that don't exist
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		tempArray = new XAddress[]{doesntExist};
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		result = callback.getEffect();
		assertNull(result[0].getResult());
		
		//Test if it behaves correctly for mixes of the cases above
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		tempArray = new XAddress[modelAddresses.length+2];
		System.arraycopy(modelAddresses, 0, tempArray, 0, modelAddresses.length);
		tempArray[modelAddresses.length] = doesntExist;
		tempArray[modelAddresses.length+1] = noAccess;
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		result = callback.getEffect();
		assertEquals(result.length, tempArray.length);
		
		//check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			if(i == modelAddresses.length) {
				assertNull(result[i].getResult());
			}
			else if(i == modelAddresses.length + 1) {
				assertNull(result[i].getResult());
			}
			else {
				assertEquals(modelAddresses[i], result[i].getResult().getAddress());
			}		
		}
		
		//TODO Maybe test more complex mixes?
		
		//Testing the quota exception
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new TestCallback<BatchedResult<XBaseModel>[]>();
			tempArray = new XAddress[]{doesntExist}; //use small array to speed up the test
			
			this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray, callback);
		}
		
		//should now return a QuotaException, since we exceeded the quota for failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
		
		//TODO How to test for other exception types like ConnectionException, TimeoutException etc.?
	}
	
	/**
	 * Tests for the GetModelRevisions-Method
	 */	
	@Test
	public void testGetModelRevisions() {
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		XAddress noAccess = this.getModelAddressWithoutAccess(this.correctUser);
		XAddress doesntExist = this.getNotExistingModelAddress();
		TestCallback<BatchedResult<XBaseModel>[]> snapshotCallback;
		TestCallback<BatchedResult<Long>[]> revisionCallback;
		
		//Test if it behaves correctly for wrong account + password combinations
		snapshotCallback = new TestCallback<BatchedResult<XBaseModel>[]>();
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass, modelAddresses, revisionCallback);
		assertFalse(this.waitOnCallback(revisionCallback));
		assertNull(revisionCallback.getEffect());
		assertNotNull(revisionCallback.getException());
		assertTrue(revisionCallback.getException() instanceof AutorisationException);
		
		//Test if it behaves correctly for addresses of XModels the user has access to
		snapshotCallback = new TestCallback<BatchedResult<XBaseModel>[]>();
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		//Get revisions
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, modelAddresses, revisionCallback);
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		//Get Model Snapshots to compare revision numbers
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses, snapshotCallback);
		assertTrue(this.waitOnCallback(snapshotCallback));
		assertNotNull(snapshotCallback.getEffect());
		assertNull(snapshotCallback.getException());
		
		BatchedResult<XBaseModel>[] snapshotResult = snapshotCallback.getEffect();
		assertEquals(snapshotResult.length, modelAddresses.length);
		
		BatchedResult<Long>[] revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult.length, modelAddresses.length);
		
		//check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			//test addresses
			assertEquals(modelAddresses[i], snapshotResult[i].getResult().getAddress());
			
			//compare revision numbers
			assertEquals((Long)snapshotResult[i].getResult().getRevisionNumber(),
						revisionResult[i].getResult());			
		}
		
		//Test if it behaves correctly for addresses of XModels the user has no access to
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		XAddress[] tempArray = new XAddress[]{noAccess};
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray, revisionCallback);
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult[0].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
		
		//Test if it behaves correctly for addresses of XModels that don't exist
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		tempArray = new XAddress[]{doesntExist};
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray, revisionCallback);
		
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult[0].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
		
		//Test if it behaves correctly for mixes of the cases above
		snapshotCallback = new TestCallback<BatchedResult<XBaseModel>[]>();
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		tempArray = new XAddress[modelAddresses.length+2];
		System.arraycopy(modelAddresses, 0, tempArray, 0, modelAddresses.length);
		tempArray[modelAddresses.length] = doesntExist;
		tempArray[modelAddresses.length+1] = noAccess;
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray, revisionCallback);
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, snapshotCallback);
		assertTrue(this.waitOnCallback(snapshotCallback));
		assertNotNull(snapshotCallback.getEffect());
		assertNull(snapshotCallback.getException());
		
		snapshotResult = snapshotCallback.getEffect();
		assertEquals(snapshotResult.length, tempArray.length);
		
		revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult.length, tempArray.length);
		
		//check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			if(i == modelAddresses.length) {
				assertNull(snapshotResult[i].getResult());
				assertEquals(revisionResult[i].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
			}
			else if(i == modelAddresses.length + 1) {
				assertNull(snapshotResult[i].getResult());
				assertEquals(revisionResult[i].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
			}
			else {
				assertEquals(modelAddresses[i], snapshotResult[i].getResult());
				
				assertEquals((Long)snapshotResult[i].getResult().getRevisionNumber(),
						revisionResult[i].getResult());
			}		
		}
		
		//TODO Maybe test more complex mixes?
		
		//Testing the quota exception
		for(long l = 0; l < this.bfQuota + 5; l++) {
			revisionCallback = new TestCallback<BatchedResult<Long>[]>();
			tempArray = new XAddress[]{doesntExist}; //use small array to speed up the test
			
			this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass, tempArray, revisionCallback);
		}
		
		//should now return a QuotaException, since we exceeded the quota for failed login attempts by at least 5
		assertFalse(this.waitOnCallback(revisionCallback));
		assertNull(revisionCallback.getEffect());
		assertNotNull(revisionCallback.getException());
		assertTrue(revisionCallback.getException() instanceof QuotaException);
		
		//TODO How to test for other exception types like ConnectionException, TimeoutException etc.?
		
		
		/**
		 * TODO add tests that actually test whether changes to a model affect this method, i.e. its new revision
		 * number is returned. Problem: It isn't really clear who has access to which models/who can actually
		 * create models etc. in this abstract case, so I don't really know how to test this right now ~Bjoern
		 */
	}
	
	/**
	 * Method for checking whether a callback succeeded or not. Waits until the operation/method the callback was
	 * passed to is finished or aborted by an error.
	 * @param callback 
	 * @return True, if the method which the callback was passed to succeeded, false if it failed or some kind
	 * 			of error occurred
	 */
	private boolean waitOnCallback(TestCallback<?> callback) {
		int value = callback.waitOnCallback(this.timeout);
		if(value == TestCallback.UNKNOWN_ERROR) {
			return false;
		}
		if(value == TestCallback.TIMEOUT) {
			return false;
		}
		
		return value == TestCallback.SUCCESS;
	}
}
