package org.xydra.core.test.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;


/**
 * Abstract test class for classes implementing the {@link XydraStore}
 * interface.
 * 
 * This test assumes that this test alone (no other threads) operates on the
 * {@link XydraStore} that is being tested. Some methods may fail if
 * someone/something else operates on the same {@link XydraStore} at the same
 * time, even though the {@link XydraStore} is working correctly.
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
	 * @return an implementation of {@link XCommandFactory} that can be used
	 *         with the implementation of {@link XydraStore} that is to be
	 *         tested
	 */
	abstract protected XCommandFactory getCommandFactory();
	
	/**
	 * Set an existing actorId with its correct passwordHash in the given
	 * XID/string
	 */
	abstract protected void setCorrectUser(XID actorId, String passwordHash);
	
	/**
	 * Set an existing XID with an incorrect passwordHash in the given
	 * XID/string
	 */
	abstract protected void setIncorrectUser(XID actorId, String passwordHash);
	
	/**
	 * Returns an array of {@link XAddress XAddresses} of {@link XModel XModels}
	 * the actor with the given {@link XID} has at least read access to.
	 * 
	 * @param accountID
	 * @return an array of {@link XAddress XAddresses} of {@link XModel XModels}
	 *         the actor with the given {@link XID} has at least read access to.
	 */
	abstract protected XAddress[] getModelAddresses(XID accountID);
	
	/**
	 * @return the {@link XAddress} of a not existing {@link XModel}
	 */
	abstract protected XAddress getNotExistingModelAddress();
	
	/**
	 * Returns an array of {@link XAddress XAddresses} of {@link XObject
	 * XObjects} the actor with the given {@link XID} has at least read access
	 * to.
	 * 
	 * @param accountID
	 * @return an array of {@link XAddress XAddresses} of {@link XObject
	 *         XObjects} the actor with the given {@link XID} has at least read
	 *         access to.
	 */
	abstract protected XAddress[] getObjectAddresses(XID accountID);
	
	/**
	 * @return the {@link XAddress} of a not existing {@link XObject}
	 */
	abstract protected XAddress getNotExistingObjectAddress();
	
	@Before
	public void setUp() {
		this.store = this.getStore();
		
		setCorrectUser(this.correctUser, this.correctUserPass);
		setIncorrectUser(this.incorrectUser, this.incorrectUserPass);
		this.timeout = getCallbackTimeout();
		this.bfQuota = getQuotaForBruteForce();
		
		if(this.bfQuota <= 0) {
			throw new IllegalArgumentException("Quota for Login attempts must be greater than 0!");
		}
	}
	
	/**
	 * Tests for the checkLogin()-method
	 */
	
	// basic functionality test for checkLogin
	// Testing a login that should succeed
	@Test
	public void testCheckLoginSuccess() {
		TestCallback<Boolean> callback = new TestCallback<Boolean>();
		
		this.store.checkLogin(this.correctUser, this.correctUserPass, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), true);
		assertNull(callback.getException());
		
	}
	
	/*
	 * Testing a login that should fail because of a wrong actorId-passwordHash
	 * combination
	 */
	@Test
	public void testCheckLoginFailure() {
		TestCallback<Boolean> callback = new TestCallback<Boolean>();
		
		this.store.checkLogin(this.incorrectUser, this.incorrectUserPass, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), false);
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test for checking if the QuoateException works for checkLogin
	@Test
	public void testCheckLoginQuotaException() {
		TestCallback<Boolean> callback = null;
		
		assert this.bfQuota > 0;
		// Testing the quota exception
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new TestCallback<Boolean>();
			
			this.store.checkLogin(this.incorrectUser, this.incorrectUserPass, callback);
		}
		
		assert callback != null;
		// should now return a QuotaException, since we exceeded the quota for
		// failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), false);
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test if checkLogin actually throws IllegalArgumentExceptions if null is
	// passed
	@Test
	public void testCheckLoginPassingNull() {
		// check IllegalArgumentException
		// first parameter equals null
		TestCallback<Boolean> callback = new TestCallback<Boolean>();
		
		try {
			this.store.checkLogin(null, this.correctUserPass, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new TestCallback<Boolean>();
		
		try {
			this.store.checkLogin(this.correctUser, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// both parameters equal null
		callback = new TestCallback<Boolean>();
		
		try {
			this.store.checkLogin(null, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback is null - should not throw an IllegalArgumentException
		
		try {
			this.store.checkLogin(this.correctUser, this.correctUserPass, null);
		} catch(IllegalArgumentException iae) {
			// if we reach this, the method didn't work as expected
			fail();
		}
	}
	
	/**
	 * Tests for the getModelSnapshots-method
	 */
	
	// Test if it behaves correctly for wrong account + password
	// combinations
	@Test
	public void testGetModelSnapshotsBadAccount() {
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		TestCallback<BatchedResult<XBaseModel>[]> callback;
		
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass, modelAddresses,
		        callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for addresses of XModels a correct user has
	// access to
	@Test
	public void testGetModelSnapshots() {
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		TestCallback<BatchedResult<XBaseModel>[]> callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertEquals(result.length, modelAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			assertNotNull(result[i].getResult());
			assertNull(result[i].getException());
			assertEquals(modelAddresses[i], result[i].getResult().getAddress());
		}
	}
	
	// Test if it behaves correctly for addresses of XModels that don't
	// exist
	@Test
	public void testGetModelSnapshotsNotExistingModel() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] tempArray = new XAddress[] { this.getNotExistingModelAddress() };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertNull(result[0].getResult());
		assertNotNull(result[0].getException());
		assertTrue(result[0].getException() instanceof RequestException);
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetModelSnapshotsMixedAddressTypes() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		XAddress[] tempArray = new XAddress[modelAddresses.length + 1];
		System.arraycopy(modelAddresses, 0, tempArray, 0, modelAddresses.length);
		tempArray[modelAddresses.length] = this.getNotExistingModelAddress();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertEquals(result.length, tempArray.length);
		
		// check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			if(i == modelAddresses.length) {
				// This index contains an XAddress of a not existing XModel
				assertNull(result[i].getResult());
				assertNotNull(result[i].getException());
				assertTrue(result[i].getException() instanceof RequestException);
			} else {
				assertNotNull(result[i].getResult());
				assertNull(result[i].getException());
				assertEquals(modelAddresses[i], result[i].getResult().getAddress());
			}
		}
		// TODO Maybe test more complex mixes?
	}
	
	// Testing the quota exception
	@Test
	public void testGetModelSnapshotsQuotaExcpetion() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { this.getNotExistingModelAddress() };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new TestCallback<BatchedResult<XBaseModel>[]>();
			
			this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
		}
		
		assert (callback != null);
		// should now return a QuotaException, since we exceeded the quota for
		// failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test if IllegalArgumentException are thrown when null values are passed
	@Test
	public void testGetModelSnapshotsPassingNull() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		
		// first parameter equals null
		try {
			this.store.getModelSnapshots(null, this.correctUserPass, modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, null, modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(null, null, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses,
			        null);
		} catch(IllegalArgumentException iae) {
			// if we reach this, the method didn't work as expected
			fail();
		}
		
		// TODO How to test for other exception types like ConnectionException,
		// TimeoutException etc.?
		
		// TODO Test what happens if the XAddress refers to an XObject etc.
	}
	
	/**
	 * Tests for the GetModelRevisions-Method
	 */
	
	// Test if it behaves correctly for wrong account + password
	// combinations
	@Test
	public void testGetModelRevisionsBadAccount() {
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		TestCallback<BatchedResult<Long>[]> revisionCallback;
		
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass, modelAddresses,
		        revisionCallback);
		assertFalse(this.waitOnCallback(revisionCallback));
		assertNull(revisionCallback.getEffect());
		assertNotNull(revisionCallback.getException());
		assertTrue(revisionCallback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for addresses of XModels the user has
	// access to
	@Test
	public void testGetModelRevisions() {
		TestCallback<BatchedResult<XBaseModel>[]> snapshotCallback = new TestCallback<BatchedResult<XBaseModel>[]>();
		TestCallback<BatchedResult<Long>[]> revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		
		// Get revisions
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, modelAddresses,
		        revisionCallback);
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		// Get Model Snapshots to compare revision numbers
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, modelAddresses,
		        snapshotCallback);
		assertTrue(this.waitOnCallback(snapshotCallback));
		assertNotNull(snapshotCallback.getEffect());
		assertNull(snapshotCallback.getException());
		
		BatchedResult<XBaseModel>[] snapshotResult = snapshotCallback.getEffect();
		assertEquals(snapshotResult.length, modelAddresses.length);
		
		BatchedResult<Long>[] revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult.length, modelAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			// test addresses
			assertEquals(modelAddresses[i], snapshotResult[i].getResult().getAddress());
			
			// compare revision numbers
			assertNotNull(revisionResult[i].getResult());
			assertNull(revisionResult[i].getException());
			assertEquals((Long)snapshotResult[i].getResult().getRevisionNumber(),
			        revisionResult[i].getResult());
		}
	}
	
	// Test if it behaves correctly for addresses of XModels that don't
	// exist
	@Test
	public void testGetModelRevisionsNotExistingModel() {
		TestCallback<BatchedResult<Long>[]> revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		XAddress[] tempArray = new XAddress[] { this.getNotExistingModelAddress() };
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray,
		        revisionCallback);
		
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		BatchedResult<Long>[] revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult[0].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
		assertNotNull(revisionResult[0].getException());
		assertTrue(revisionResult[0].getException() instanceof RequestException);
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetModelRevisionsMixedAddresses() {
		TestCallback<BatchedResult<XBaseModel>[]> snapshotCallback = new TestCallback<BatchedResult<XBaseModel>[]>();
		TestCallback<BatchedResult<Long>[]> revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		XAddress[] tempArray = new XAddress[modelAddresses.length + 1];
		System.arraycopy(modelAddresses, 0, tempArray, 0, modelAddresses.length);
		tempArray[modelAddresses.length] = this.getNotExistingModelAddress();
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray,
		        revisionCallback);
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray,
		        snapshotCallback);
		assertTrue(this.waitOnCallback(snapshotCallback));
		assertNotNull(snapshotCallback.getEffect());
		assertNull(snapshotCallback.getException());
		
		BatchedResult<XBaseModel>[] snapshotResult = snapshotCallback.getEffect();
		assertEquals(snapshotResult.length, tempArray.length);
		
		BatchedResult<Long>[] revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult.length, tempArray.length);
		
		// check order of returned snapshots
		for(int i = 0; i < modelAddresses.length; i++) {
			if(i == modelAddresses.length) {
				// this index contains an XAddress of a not existing XModel
				assertNull(snapshotResult[i].getResult());
				assertEquals(revisionResult[i].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
				assertNotNull(revisionResult[i].getException());
				assertTrue(revisionResult[i].getException() instanceof RequestException);
			} else {
				assertEquals(modelAddresses[i], snapshotResult[i].getResult());
				
				assertNotNull(revisionResult[i].getResult());
				assertNull(revisionResult[i].getException());
				assertEquals((Long)snapshotResult[i].getResult().getRevisionNumber(),
				        revisionResult[i].getResult());
			}
		}
		
		// TODO Maybe test more complex mixes?
	}
	
	// Testing the quota exception
	public void testGetModelRevisionsQuotaException() {
		TestCallback<BatchedResult<Long>[]> callback = null;
		XAddress[] tempArray = { this.getNotExistingObjectAddress() };
		
		assert bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new TestCallback<BatchedResult<Long>[]>();
			
			this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
		}
		assert callback != null;
		
		// should now return a QuotaException, since we exceeded the quota for
		// failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test IllegalArgumentException
	public void testGetModelRevisionPassingNull() {
		TestCallback<BatchedResult<Long>[]> revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		XAddress[] modelAddresses = this.getModelAddresses(this.correctUser);
		
		// first parameter equals null
		try {
			this.store.getModelRevisions(null, this.correctUserPass, modelAddresses,
			        revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(this.correctUser, null, modelAddresses, revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(this.correctUser, this.correctUserPass, null,
			        revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(null, null, null, revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(this.correctUser, this.correctUserPass, modelAddresses,
			        null);
		} catch(IllegalArgumentException iae) {
			// if we reach this, the method didn't work as expected
			fail();
		}
	}
	
	/**
	 * Tests for the getObjectSnapshot-Method
	 */
	
	// Test if it behaves correctly for wrong account + password
	// combinations
	@Test
	public void testGetObjectSnapshotsBadAccount() {
		XAddress[] objectAddresses = this.getObjectAddresses(this.correctUser);
		TestCallback<BatchedResult<XBaseObject>[]> callback;
		
		// Test if it behaves correctly for wrong account + password
		// combinations
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass, objectAddresses,
		        callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for addresses of XObjects the user has
	// access to
	@Test
	public void testGetObjectSnapshots() {
		XAddress[] objectAddresses = this.getObjectAddresses(this.correctUser);
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, objectAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseObject>[] result = callback.getEffect();
		assertEquals(result.length, objectAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < objectAddresses.length; i++) {
			assertNotNull(result[i].getResult());
			assertNull(result[i].getException());
			assertEquals(objectAddresses[i], result[i].getResult().getAddress());
		}
	}
	
	// Test if it behaves correctly for addresses of XObjects that don't
	// exist
	@Test
	public void testGetObjectSnapshotsNotExistingObject() {
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		XAddress[] tempArray = new XAddress[] { this.getNotExistingObjectAddress() };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseObject>[] result = callback.getEffect();
		assertNull(result[0].getResult());
		assertNotNull(result[0].getException());
		assertTrue(result[0].getException() instanceof RequestException);
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetObjectSnapshotsMixedAddresses() {
		XAddress[] objectAddresses = this.getObjectAddresses(this.correctUser);
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		XAddress[] tempArray = new XAddress[objectAddresses.length + 1];
		System.arraycopy(objectAddresses, 0, tempArray, 0, objectAddresses.length);
		tempArray[objectAddresses.length] = this.getNotExistingObjectAddress();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseObject>[] result = callback.getEffect();
		assertEquals(result.length, tempArray.length);
		
		// check order of returned snapshots
		for(int i = 0; i < objectAddresses.length; i++) {
			if(i == objectAddresses.length) {
				// this index contains an XAddress of a not existing XObject
				assertNull(result[i].getResult());
				assertNotNull(result[i].getException());
				assertTrue(result[i].getException() instanceof RequestException);
			} else {
				assertNotNull(result[i].getResult());
				assertNull(result[i].getException());
				assertEquals(objectAddresses[i], result[i].getResult().getAddress());
			}
		}
		
		// TODO Maybe test more complex mixes?
	}
	
	// Testing the quota exception
	@Test
	public void testGetObjectSnapshotsQuotaException() {
		TestCallback<BatchedResult<XBaseObject>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { this.getNotExistingObjectAddress() };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new TestCallback<BatchedResult<XBaseObject>[]>();
			
			this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
		}
		assert callback != null;
		
		// should now return a QuotaException, since we exceeded the quota for
		// failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test IllegalArgumentException
	@Test
	public void testGetObjectSnapshotsPassingNull() {
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		XAddress[] objectAddresses = this.getObjectAddresses(this.correctUser);
		
		// first parameter equals null
		try {
			this.store.getObjectSnapshots(null, this.correctUserPass, objectAddresses, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, null, objectAddresses, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(null, null, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass, objectAddresses,
			        null);
		} catch(IllegalArgumentException iae) {
			// there's something wrong if we reached this
			fail();
		}
	}
	
	/*
	 * TODO From here on this tests assumes that every user has access on the
	 * general store and can actually create XModels directly on the store.
	 * Update the first tests to also use this (instead of using an abstract
	 * method, that is now unneccesary)
	 */

	/**
	 * Method for checking whether a callback succeeded or not. Waits until the
	 * operation/method the callback was passed to is finished or aborted by an
	 * error.
	 * 
	 * @param callback
	 * @return True, if the method which the callback was passed to succeeded,
	 *         false if it failed or some kind of error occurred
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
	
	/**
	 * Return value sets the amount of time tests shall wait on callbacks.
	 * Implementations of this abstract test may override this to use a custom
	 * value.
	 */
	protected long getCallbackTimeout() {
		return 1000;
	}
	
	/**
	 * Return value sets the amount of allowed incorrect login tries before a
	 * QuotaException is thrown.
	 * 
	 * Implementations of this abstract test need to override this to return the
	 * specific quota of the XydraStore implementation which is to be tested.
	 */
	abstract protected long getQuotaForBruteForce();
}
