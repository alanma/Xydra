package org.xydra.core.test.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

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
	private boolean setUpDone = false, incorrectActorExists = true;
	
	/**
	 * @return an implementation of {@link XydraStore} which is to be tested
	 */
	abstract protected XydraStore getStore();
	
	/**
	 * @return an implementation of {@link XCommandFactory} which works with the
	 *         implementation of {@link XydraStore} returned by {
	 *         {@link #getStore()};
	 */
	abstract protected XCommandFactory getCommandFactory();
	
	/**
	 * Returns the {@link XID} of an account, which is registered on the
	 * {@link XydraStore} returned by {@link #getStore()}.
	 * 
	 * Please note: This methods needs to return the XID of a user who has
	 * access to read everything on the {@link XydraStore} returned by
	 * {@link #getStore()} and at least write-access to the returned store
	 * itself. Otherwise this test will fail (although the implementation which
	 * is to be tested might work correctly).
	 */
	abstract protected XID getCorrectUser();
	
	/**
	 * Returns the correct password hash of the account which {@link XID} is
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
	
	@Before
	public void setUp() {
		
		if(this.setUpDone) {
			return;
			
			/*
			 * This code segment guarantees that the following set-up code will
			 * only be run once. This basically works like an @BeforeClass
			 * method and it not really the most beautiful solution, but
			 * unfortunately we cannot actually use a @BeforeClass method here,
			 * because this is an abstract test and we need to call abstract
			 * methods... but abstract methods cannot be static. There might be
			 * some other kind of workout around this problem, but all I could
			 * think of was/could find on the Internet were even uglier...
			 * ~Bjoern
			 */
		}
		
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
		
		this.correctUser = this.getCorrectUser();
		this.correctUserPass = this.getCorrectUserPasswordHash();
		
		if(this.correctUser == null || this.correctUserPass == null) {
			throw new IllegalArgumentException("correctUser or correctUserPass were null");
		}
		
		this.incorrectUser = this.getIncorrectUser();
		this.incorrectUserPass = this.getIncorrectUserPasswordHash();
		this.incorrectActorExists = (this.incorrectUser != null);
		
		this.timeout = getCallbackTimeout();
		
		if(this.timeout <= 0) {
			throw new IllegalArgumentException("Timeout for callbacks must be greater than 0!");
		}
		
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
		SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
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
		SynchronousTestCallback<BatchedResult<Long>[]> commandCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
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
		
		this.setUpDone = true;
	}
	
	/**
	 * Tests for the checkLogin()-method
	 */
	
	// basic functionality test for checkLogin
	// Testing a login that should succeed
	@Test
	public void testCheckLoginSuccess() {
		SynchronousTestCallback<Boolean> callback = new SynchronousTestCallback<Boolean>();
		
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
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<Boolean> callback = new SynchronousTestCallback<Boolean>();
		
		this.store.checkLogin(this.incorrectUser, this.incorrectUserPass, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), false);
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test for checking if the QuoateException works for checkLogin
	@Test
	public void testCheckLoginQuotaException() {
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<Boolean> callback = null;
		
		assert this.bfQuota > 0;
		// Testing the quota exception
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new SynchronousTestCallback<Boolean>();
			
			this.store.checkLogin(this.incorrectUser, this.incorrectUserPass, callback);
		}
		
		assert callback != null;
		// should now return a QuotaException, since we exceeded the quota
		// for failed login attempts by at least 5
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
		SynchronousTestCallback<Boolean> callback = new SynchronousTestCallback<Boolean>();
		
		try {
			this.store.checkLogin(null, this.correctUserPass, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<Boolean>();
		
		try {
			this.store.checkLogin(this.correctUser, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// both parameters equal null
		callback = new SynchronousTestCallback<Boolean>();
		
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
		if(!this.incorrectActorExists) {
			return;
		}
		
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback;
		
		callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
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
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
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
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseModel>[] result = callback.getEffect();
		assertNull(result[0].getResult());
		assertNull(result[0].getException());
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetModelSnapshotsMixedAddressTypes() {
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
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
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
			
			this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
		}
		
		assert (callback != null);
		// should now return a QuotaException, since we exceeded the quota
		// for failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test if IllegalArgumentException are thrown when null values are passed
	@Test
	public void testGetModelSnapshotsPassingNull() {
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		// first parameter equals null
		try {
			this.store.getModelSnapshots(null, this.correctUserPass, this.modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, null, this.modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(null, null, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		callback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
			        this.modelAddresses, null);
		} catch(IllegalArgumentException iae) {
			// if we reach this, the method didn't work as expected
			fail();
		}
		
		// TODO Test what happens if the XAddress refers to an XObject etc.
	}
	
	/**
	 * Tests for the GetModelRevisions-Method
	 */
	
	// Test if it behaves correctly for wrong account + password
	// combinations
	@Test
	public void testGetModelRevisionsBadAccount() {
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<BatchedResult<Long>[]> revisionCallback;
		
		revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
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
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> snapshotCallback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		SynchronousTestCallback<BatchedResult<Long>[]> revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
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
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<Long>[] result = callback.getEffect();
		assertNotNull(result[0].getResult());
		assertNull(result[0].getException());
		assertEquals(result[0].getResult(), (Long)XydraStore.MODEL_DOES_NOT_EXIST);
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetModelRevisionsMixedAddresses() {
		SynchronousTestCallback<BatchedResult<XBaseModel>[]> snapshotCallback = new SynchronousTestCallback<BatchedResult<XBaseModel>[]>();
		SynchronousTestCallback<BatchedResult<Long>[]> revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
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
				assertNull(revisionResult[i].getException());
			} else {
				assertEquals(this.modelAddresses[i], snapshotResult[i].getResult().getAddress());
				
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
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<BatchedResult<Long>[]> callback = null;
		XAddress[] tempArray = { this.notExistingModel };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
			
			this.store.getModelRevisions(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
		}
		assert callback != null;
		
		// should now return a QuotaException, since we exceeded the quota
		// for failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test IllegalArgumentException
	public void testGetModelRevisionPassingNull() {
		SynchronousTestCallback<BatchedResult<Long>[]> revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		// first parameter equals null
		try {
			this.store.getModelRevisions(null, this.correctUserPass, this.modelAddresses,
			        revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(this.correctUser, null, this.modelAddresses,
			        revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(this.correctUser, this.correctUserPass, null,
			        revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		try {
			this.store.getModelRevisions(null, null, null, revisionCallback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		revisionCallback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
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
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<BatchedResult<XBaseObject>[]> callback;
		
		// Test if it behaves correctly for wrong account + password
		// combinations
		callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
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
		SynchronousTestCallback<BatchedResult<XBaseObject>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
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
		SynchronousTestCallback<BatchedResult<XBaseObject>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingObject };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XBaseObject>[] result = callback.getEffect();
		assertNull(result[0].getResult());
		assertNull(result[0].getException());
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetObjectSnapshotsMixedAddresses() {
		SynchronousTestCallback<BatchedResult<XBaseObject>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
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
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<BatchedResult<XBaseObject>[]> callback = null;
		XAddress[] tempArray = new XAddress[] { this.notExistingObject };
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
			
			this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass, tempArray,
			        callback);
		}
		assert callback != null;
		
		// should now return a QuotaException, since we exceeded the quota
		// for failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test IllegalArgumentException
	@Test
	public void testGetObjectSnapshotsPassingNull() {
		SynchronousTestCallback<BatchedResult<XBaseObject>[]> callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
		// first parameter equals null
		try {
			this.store.getObjectSnapshots(null, this.correctUserPass, this.objectAddresses,
			        callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, null, this.objectAddresses, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(null, null, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		callback = new SynchronousTestCallback<BatchedResult<XBaseObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
			        this.objectAddresses, null);
		} catch(IllegalArgumentException iae) {
			// there's something wrong if we reached this
			fail();
		}
	}
	
	/**
	 * Tests for getModelIDs
	 */
	
	// Test if it behaves correctly for wrong account + password
	// combinations
	@Test
	public void testGetModelIdsBadAccount() {
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<Set<XID>> callback;
		
		// Test if it behaves correctly for wrong account + password
		// combinations
		callback = new SynchronousTestCallback<Set<XID>>();
		
		this.store.getModelIds(this.incorrectUser, this.incorrectUserPass, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	/*
	 * Test if it behaves correctly for a correct account + password combination
	 * 
	 * 
	 * Please note: This is only a rudimentary test of the functionality of
	 * {@link XydraStore#getModelIds()}. Since this method is heavily connected
	 * with account access rights and this test assumes no specific access right
	 * management implementation, every implementation of {@link
	 * AbstractStoreTest} should provide further tests for this method that
	 * actually consider the access right management used by the {@link
	 * XydraStore} implementation they are testing.
	 */
	@Test
	public void testGetModelIds() {
		SynchronousTestCallback<Set<XID>> callback = new SynchronousTestCallback<Set<XID>>();
		
		this.store.getModelIds(this.correctUser, this.correctUserPass, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		Set<XID> result = callback.getEffect();
		
		/*
		 * check if it contains the XIDs of the XModels created by this test
		 * (the result should contain each of these XIDs, since the user with
		 * the account XID this.correctUser has access to everything in the
		 * store)
		 */
		for(int i = 0; i < this.modelAddresses.length; i++) {
			assertTrue(result.contains(this.modelAddresses[i].getModel()));
		}
		
	}
	
	// Testing the quota exception
	@Test
	public void testGetModelIdsQuotaException() {
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<Set<XID>> callback = null;
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new SynchronousTestCallback<Set<XID>>();
			
			this.store.getModelIds(this.incorrectUser, this.incorrectUserPass, callback);
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
	public void testGetModelIdsPassingNull() {
		SynchronousTestCallback<Set<XID>> callback = new SynchronousTestCallback<Set<XID>>();
		
		// first parameter equals null
		try {
			this.store.getModelIds(null, this.correctUserPass, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<Set<XID>>();
		
		try {
			this.store.getModelIds(this.correctUser, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<Set<XID>>();
		
		try {
			this.store.getModelIds(null, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		callback = new SynchronousTestCallback<Set<XID>>();
		
		try {
			this.store.getModelIds(this.correctUser, this.correctUserPass, null);
		} catch(IllegalArgumentException iae) {
			// there's something wrong if we reached this
			fail();
		}
	}
	
	/**
	 * Tests for getRepositoryId
	 */
	
	/*
	 * Please note: getRepositoryIds functionality cannot be tested in an
	 * abstracted way, since {@link XydraStore} does not force any way of how
	 * its repository {@link XID} is to be set. You'll need to write your own,
	 * implementation-specific, tests.
	 */

	// Test if it behaves correctly for wrong account + password
	// combinations
	@Test
	public void testGetRepositoryIdBadAccount() {
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
		
		this.store.getRepositoryId(this.incorrectUser, this.incorrectUserPass, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Testing the quota exception
	@Test
	public void testGetRepositoryIdQuotaException() {
		if(!this.incorrectActorExists) {
			// This test only makes sense if an incorrect actorID - passwordhash
			// combination can be provided
			return;
		}
		
		SynchronousTestCallback<XID> callback = null;
		
		assert this.bfQuota > 0;
		for(long l = 0; l < this.bfQuota + 5; l++) {
			callback = new SynchronousTestCallback<XID>();
			
			this.store.getRepositoryId(this.incorrectUser, this.incorrectUserPass, callback);
		}
		assert callback != null;
		
		// should now return a QuotaException, since we exceeded the quota
		// for
		// failed login attempts by at least 5
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof QuotaException);
	}
	
	// Test IllegalArgumentException
	@Test
	public void testGetRepositoryIdPassingNull() {
		SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
		
		// first parameter equals null
		try {
			this.store.getRepositoryId(null, this.correctUserPass, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<XID>();
		
		try {
			this.store.getRepositoryId(this.correctUser, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<XID>();
		
		try {
			this.store.getRepositoryId(null, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null - should not throw an IllegalArgumentException
		callback = new SynchronousTestCallback<XID>();
		
		try {
			this.store.getRepositoryId(this.correctUser, this.correctUserPass, null);
		} catch(IllegalArgumentException iae) {
			// there's something wrong if we reached this
			fail();
		}
	}
	
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
		int value = callback.waitOnCallback(this.timeout);
		if(value == SynchronousTestCallback.UNKNOWN_ERROR) {
			return false;
		}
		if(value == SynchronousTestCallback.TIMEOUT) {
			return false;
		}
		
		return value == SynchronousTestCallback.SUCCESS;
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
