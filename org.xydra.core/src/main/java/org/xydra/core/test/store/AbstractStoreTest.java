package org.xydra.core.test.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;


/**
 * Abstract test class for classes implementing the {@link XydraStore}
 * interface.
 * 
 * 
 * This test assumes that this test alone (no other threads) operates on the
 * {@link XydraStore} that is being tested. Some methods may fail if
 * someone/something else operates on the same {@link XydraStore} at the same
 * time, even though the {@link XydraStore} is working correctly.
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO Test changeState in a seperate class! JUnit does not guarantee a
 * specific execution order of the test methods, so changes made to the
 * XydraStore might mess up the read-methods that suppose that the XydraStore is
 * in the state which is created by the setUp method
 */

public abstract class AbstractStoreTest {
	
	private XydraStore store;
	private XCommandFactory factory;
	
	private XID correctUser, incorrectUser;
	private XAddress[] modelAddresses;
	private XAddress notExistingModel;
	private XAddress[] objectAddresses;
	private XAddress notExistingObject;
	
	private String correctUserPass, incorrectUserPass;
	private long timeout;
	private long bfQuota;
	
	/**
	 * @return an implementation of {@link XydraStore}
	 */
	abstract protected XydraStore getStore();
	
	/**
	 * @return an implementation of {@link XCommandFactory} which works with the
	 *         implementation of {@link XydraStore} return by {
	 *         {@link #getStore()};
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
	
	/*
	 * TODO Maybe turn this into an @BeforeClass Method? (Problem: needs to be
	 * static!)
	 * 
	 * FIXME There might be problems if this is executed before every new test
	 * method execution, since the XydraStore was already manipulated before...
	 * or is this covered by the isForced-Parameter? Look into this!
	 */
	@Before
	public void setUp() {
		this.store = this.getStore();
		this.factory = this.getCommandFactory();
		
		if(this.store == null) {
			throw new RuntimeException(
			        "XydraStore could not be initalized in the setUpClass method!");
		}
		if(this.factory == null) {
			throw new RuntimeException(
			        "XCommandFactory could not be initalized in the setUpClass method!");
		}
		
		setCorrectUser(this.correctUser, this.correctUserPass);
		setIncorrectUser(this.incorrectUser, this.incorrectUserPass);
		
		this.timeout = getCallbackTimeout();
		this.bfQuota = getQuotaForBruteForce();
		
		if(this.bfQuota <= 0) {
			throw new IllegalArgumentException("Quota for Login attempts must be greater than 0!");
		}
		
		// creating some models
		XID modelID1 = XX.toId("TestModel1");
		XID modelID2 = XX.toId("TestModel2");
		XID modelID3 = XX.toId("TestModel3");
		
		XID objectID1 = XX.toId("TestObject1");
		XID objectID2 = XX.toId("TestObject2");
		XID objectID3 = XX.toId("TestObject3");
		
		// get the repository ID of the store
		TestCallback<XID> callback = new TestCallback<XID>();
		this.store.getRepositoryId(this.correctUser, this.correctUserPass, callback);
		waitOnCallback(callback);
		
		if(callback.getEffect() == null) {
			throw new RuntimeException(
			        "getRepositoryID seems to now work correctly, rendering this test useless!");
		}
		XID repoID = callback.getEffect();
		
		XCommand modelCommand1 = this.factory.createAddModelCommand(repoID, modelID1, true);
		XCommand modelCommand2 = this.factory.createAddModelCommand(repoID, modelID2, true);
		XCommand modelCommand3 = this.factory.createAddModelCommand(repoID, modelID3, true);
		
		XCommand objectCommand1 = this.factory.createAddObjectCommand(repoID, modelID1, objectID1,
		        true);
		XCommand objectCommand2 = this.factory.createAddObjectCommand(repoID, modelID1, objectID2,
		        true);
		XCommand objectCommand3 = this.factory.createAddObjectCommand(repoID, modelID1, objectID3,
		        true);
		
		XCommand[] commands = { modelCommand1, modelCommand2, modelCommand3, objectCommand1,
		        objectCommand2, objectCommand3 };
		TestCallback<BatchedResult<Long>[]> commandCallback = new TestCallback<BatchedResult<Long>[]>();
		
		this.store.executeCommands(this.correctUser, this.correctUserPass, commands,
		        commandCallback);
		waitOnCallback(commandCallback);
		
		BatchedResult<Long>[] result = commandCallback.getEffect();
		if(commandCallback.getException() != null) {
			throw new RuntimeException(
			        "ExecuteCommands did not work properly in setUp (threw an Exception), here's its message text: "
			                + commandCallback.getException().getMessage() + '\n'
			                + " and here is its StackTrace: " + '\n'
			                + commandCallback.getException().getStackTrace());
		}
		
		if(result.length <= 0) {
			throw new RuntimeException(
			        "ExecuteCommands did not work properly in setUp (returned no results) - tests can not be run!");
		}
		
		for(int i = 0; i < result.length; i++) {
			if(result[i].getResult() == XCommand.FAILED) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp: command at index " + i
				                + "failed!");
			}
			// TODO is this check necessary?
			if(result[i].getResult() == XCommand.NOCHANGE) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp: command at index " + i
				                + "did not change anything!");
			}
			
			if(result[i].getException() != null) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp (executing command at index "
				                + i + " threw an Exception), here's its message text: "
				                + commandCallback.getException().getMessage() + '\n'
				                + " and here is its StackTrace: " + '\n'
				                + commandCallback.getException().getStackTrace());
			}
		}
		
		XAddress modelAddress1 = XX.toAddress(repoID, modelID1, null, null);
		XAddress modelAddress2 = XX.toAddress(repoID, modelID2, null, null);
		XAddress modelAddress3 = XX.toAddress(repoID, modelID3, null, null);
		
		this.modelAddresses = new XAddress[] { modelAddress1, modelAddress2, modelAddress3 };
		this.notExistingModel = XX.toAddress(repoID, XX.toId("TestModelDoesntExist"), null, null);
		
		XAddress objectAddress1 = XX.toAddress(repoID, modelID1, objectID1, null);
		XAddress objectAddress2 = XX.toAddress(repoID, modelID1, objectID2, null);
		XAddress objectAddress3 = XX.toAddress(repoID, modelID1, objectID3, null);
		this.objectAddresses = new XAddress[] { objectAddress1, objectAddress2, objectAddress3 };
		this.notExistingObject = XX.toAddress(repoID, modelID1, XX.toId("TestObjectDoesntExist"),
		        null);
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
		TestCallback<BatchedResult<XBaseModel>[]> callback;
		
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass,
		        this.modelAddresses, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for addresses of XModels a correct user has
	// access to
	@Test
	public void testGetModelSnapshots() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, this.modelAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertEquals(result.length, this.modelAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.modelAddresses.length; i++) {
			assertNotNull(result[i].getResult());
			assertNull(result[i].getException());
			assertEquals(this.modelAddresses[i], result[i].getResult().getAddress());
		}
	}
	
	// Test if it behaves correctly for addresses of XModels that don't
	// exist
	@Test
	public void testGetModelSnapshotsNotExistingModel() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
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
		XAddress[] tempArray = new XAddress[this.modelAddresses.length + 1];
		System.arraycopy(this.modelAddresses, 0, tempArray, 0, this.modelAddresses.length);
		tempArray[this.modelAddresses.length] = this.notExistingModel;
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertEquals(result.length, tempArray.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.modelAddresses.length; i++) {
			if(i == this.modelAddresses.length) {
				// This index contains an XAddress of a not existing XModel
				assertNull(result[i].getResult());
				assertNotNull(result[i].getException());
				assertTrue(result[i].getException() instanceof RequestException);
			} else {
				assertNotNull(result[i].getResult());
				assertNull(result[i].getException());
				assertEquals(this.modelAddresses[i], result[i].getResult().getAddress());
			}
		}
		// TODO Maybe test more complex mixes?
	}
	
	// Testing the quota exception
	@Test
	public void testGetModelSnapshotsQuotaExcpetion() {
		TestCallback<BatchedResult<XBaseModel>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
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
		
		// first parameter equals null
		try {
			this.store.getModelSnapshots(null, this.correctUserPass, this.modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new TestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, null, this.modelAddresses, callback);
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
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
			        this.modelAddresses, null);
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
		TestCallback<BatchedResult<Long>[]> revisionCallback;
		
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass,
		        this.modelAddresses, revisionCallback);
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
		
		// Get revisions
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, this.modelAddresses,
		        revisionCallback);
		assertTrue(this.waitOnCallback(revisionCallback));
		assertNotNull(revisionCallback.getEffect());
		assertNull(revisionCallback.getException());
		
		// Get Model Snapshots to compare revision numbers
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, this.modelAddresses,
		        snapshotCallback);
		assertTrue(this.waitOnCallback(snapshotCallback));
		assertNotNull(snapshotCallback.getEffect());
		assertNull(snapshotCallback.getException());
		
		BatchedResult<XBaseModel>[] snapshotResult = snapshotCallback.getEffect();
		assertEquals(snapshotResult.length, this.modelAddresses.length);
		
		BatchedResult<Long>[] revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult.length, this.modelAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.modelAddresses.length; i++) {
			// test addresses
			assertEquals(this.modelAddresses[i], snapshotResult[i].getResult().getAddress());
			
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
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
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
		
		XAddress[] tempArray = new XAddress[this.modelAddresses.length + 1];
		System.arraycopy(this.modelAddresses, 0, tempArray, 0, this.modelAddresses.length);
		tempArray[this.modelAddresses.length] = this.notExistingModel;
		
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
		for(int i = 0; i < this.modelAddresses.length; i++) {
			if(i == this.modelAddresses.length) {
				// this index contains an XAddress of a not existing XModel
				assertNull(snapshotResult[i].getResult());
				assertEquals(revisionResult[i].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
				assertNotNull(revisionResult[i].getException());
				assertTrue(revisionResult[i].getException() instanceof RequestException);
			} else {
				assertEquals(this.modelAddresses[i], snapshotResult[i].getResult());
				
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
		XAddress[] tempArray = { this.notExistingModel };
		
		assert this.bfQuota > 0;
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
		
		// first parameter equals null
		try {
			this.store.getModelRevisions(null, this.correctUserPass, this.modelAddresses,
			        revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		revisionCallback = new TestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(this.correctUser, null, this.modelAddresses,
			        revisionCallback);
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
			this.store.getModelRevisions(this.correctUser, this.correctUserPass,
			        this.modelAddresses, null);
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
		TestCallback<BatchedResult<XBaseObject>[]> callback;
		
		// Test if it behaves correctly for wrong account + password
		// combinations
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass,
		        this.objectAddresses, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for addresses of XObjects the user has
	// access to
	@Test
	public void testGetObjectSnapshots() {
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, this.objectAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseObject>[] result = callback.getEffect();
		assertEquals(result.length, this.objectAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.objectAddresses.length; i++) {
			assertNotNull(result[i].getResult());
			assertNull(result[i].getException());
			assertEquals(this.objectAddresses[i], result[i].getResult().getAddress());
		}
	}
	
	// Test if it behaves correctly for addresses of XObjects that don't
	// exist
	@Test
	public void testGetObjectSnapshotsNotExistingObject() {
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingObject };
		
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
		TestCallback<BatchedResult<XBaseObject>[]> callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		XAddress[] tempArray = new XAddress[this.objectAddresses.length + 1];
		System.arraycopy(this.objectAddresses, 0, tempArray, 0, this.objectAddresses.length);
		tempArray[this.objectAddresses.length] = this.notExistingObject;
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseObject>[] result = callback.getEffect();
		assertEquals(result.length, tempArray.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.objectAddresses.length; i++) {
			if(i == this.objectAddresses.length) {
				// this index contains an XAddress of a not existing XObject
				assertNull(result[i].getResult());
				assertNotNull(result[i].getException());
				assertTrue(result[i].getException() instanceof RequestException);
			} else {
				assertNotNull(result[i].getResult());
				assertNull(result[i].getException());
				assertEquals(this.objectAddresses[i], result[i].getResult().getAddress());
			}
		}
		
		// TODO Maybe test more complex mixes?
	}
	
	// Testing the quota exception
	@Test
	public void testGetObjectSnapshotsQuotaException() {
		TestCallback<BatchedResult<XBaseObject>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { this.notExistingObject };
		
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
		
		// first parameter equals null
		try {
			this.store.getObjectSnapshots(null, this.correctUserPass, this.objectAddresses,
			        callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new TestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, null, this.objectAddresses, callback);
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
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
			        this.objectAddresses, null);
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
