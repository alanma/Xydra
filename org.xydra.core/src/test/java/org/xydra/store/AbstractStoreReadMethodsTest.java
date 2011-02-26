package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.log.DefaultLoggerFactorySPI;
import org.xydra.log.LoggerFactory;


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
 * TODO Test changeState in a separate class! JUnit does not guarantee a
 * specific execution order of the test methods, so changes made to the
 * XydraStore might mess up the read-methods that suppose that the XydraStore is
 * in the state which is created by the setUp method
 */

public abstract class AbstractStoreReadMethodsTest extends AbstractStoreTest {
	
	static {
		LoggerFactory.setLoggerFactorySPI(new DefaultLoggerFactorySPI());
	}
	
	private XID correctUser, incorrectUser;
	
	protected String correctUserPass, incorrectUserPass;
	protected XCommandFactory factory;
	protected boolean incorrectActorExists = true;
	protected XAddress[] modelAddresses;
	protected XAddress notExistingModel;
	
	protected XAddress notExistingObject;
	protected XAddress[] objectAddresses;
	protected XydraStore store;
	
	@Before
	public void setUp() {
		this.store = this.getStore();
		this.factory = this.getCommandFactory();
		
		if(this.store == null) {
			throw new RuntimeException("XydraStore could not be initalized in the setUp method!");
		}
		if(this.factory == null) {
			throw new RuntimeException(
			        "XCommandFactory could not be initalized in the setUp method!");
		}
		
		this.correctUser = this.getCorrectUser();
		this.correctUserPass = this.getCorrectUserPasswordHash();
		
		if(this.correctUser == null || this.correctUserPass == null) {
			throw new IllegalArgumentException("correctUser or correctUserPass were null");
		}
		
		this.incorrectUser = this.getIncorrectUser();
		this.incorrectUserPass = this.getIncorrectUserPasswordHash();
		this.incorrectActorExists = (this.incorrectUser != null);
		
		// creating some models
		XID modelId1 = XX.toId("TestModel1");
		XID modelId2 = XX.toId("TestModel2");
		XID modelId3 = XX.toId("TestModel3");
		
		XID objectId1 = XX.toId("TestObject1");
		XID objectId2 = XX.toId("TestObject2");
		XID objectId3 = XX.toId("TestObject3");
		
		/*
		 * FIXME In a secure store you need to give the correctUser the rights
		 * to access these models and objects - this should not be done by this
		 * abstract test, but rather by the implementation. As stated in the
		 * documentation of the "getCorrectUser" method, the test assumes that
		 * the user returned by this method is allowed to execute the following
		 * commands ~Bjoern
		 */

		XID repoID = getRepositoryId();
		
		XCommand modelCommand1 = this.factory.createAddModelCommand(repoID, modelId1, true);
		XCommand modelCommand2 = this.factory.createAddModelCommand(repoID, modelId2, true);
		XCommand modelCommand3 = this.factory.createAddModelCommand(repoID, modelId3, true);
		
		XCommand objectCommand1 = this.factory.createAddObjectCommand(repoID, modelId1, objectId1,
		        true);
		XCommand objectCommand2 = this.factory.createAddObjectCommand(repoID, modelId1, objectId2,
		        true);
		XCommand objectCommand3 = this.factory.createAddObjectCommand(repoID, modelId1, objectId3,
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
			        "ExecuteCommands did not work properly in setUp (threw an Exception), here's its message text: ",
			        commandCallback.getException());
		}
		
		if(result.length <= 0) {
			throw new RuntimeException(
			        "ExecuteCommands did not work properly in setUp (returned no results) - tests can not be run!");
		}
		
		for(int i = 0; i < result.length; i++) {
			if(result[i].getResult() == XCommand.FAILED) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp: command at index " + i
				                + " failed!");
			}
			// TODO is this check necessary?
			// TODO this fails with the GaeStore which cannot be reset
			if(result[i].getResult() == XCommand.NOCHANGE) {
				throw new RuntimeException(
				        "ExecuteCommands did not work properly in setUp: command at index " + i
				                + " did not change anything!");
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
		
		XAddress modelAddress1 = XX.toAddress(repoID, modelId1, null, null);
		XAddress modelAddress2 = XX.toAddress(repoID, modelId2, null, null);
		XAddress modelAddress3 = XX.toAddress(repoID, modelId3, null, null);
		
		this.modelAddresses = new XAddress[] { modelAddress1, modelAddress2, modelAddress3 };
		this.notExistingModel = XX.toAddress(repoID, XX.toId("TestModelDoesntExist"), null, null);
		
		XAddress objectAddress1 = XX.toAddress(repoID, modelId1, objectId1, null);
		XAddress objectAddress2 = XX.toAddress(repoID, modelId1, objectId2, null);
		XAddress objectAddress3 = XX.toAddress(repoID, modelId1, objectId3, null);
		this.objectAddresses = new XAddress[] { objectAddress1, objectAddress2, objectAddress3 };
		this.notExistingObject = XX.toAddress(repoID, modelId1, XX.toId("TestObjectDoesntExist"),
		        null);
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
		
		assertTrue(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), false);
		assertNull(callback.getException());
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
		
		// callback is null
		try {
			this.store.checkLogin(this.correctUser, this.correctUserPass, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<Boolean>();
		
		try {
			this.store.checkLogin(null, null, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
	}
	
	/**
	 * Tests for the checkLogin()-method
	 */
	
	// basic functionality test for checkLogin
	// Testing a login that should succeed
	@Test
	public void testCheckLoginSuccess() {
		SynchronousTestCallback<Boolean> callback = new SynchronousTestCallback<Boolean>();
		assert this.store != null;
		this.store.checkLogin(this.correctUser, this.correctUserPass, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertEquals(callback.getEffect(), true);
		assertNull(callback.getException());
		
	}
	
	/*
	 * Test if it behaves correctly for a correct account + password combination
	 * 
	 * 
	 * Please note: This is only a rudimentary test of the functionality of
	 * {@link XydraStore#getModelIds()}. Since this method is heavily connected
	 * with account access rights and this test assumes no specific access right
	 * management implementation, every implementation of {@link
	 * AbstractStoreReadMethodsTest} should provide further tests for this
	 * method that actually consider the access right management used by the
	 * {@link XydraStore} implementation they are testing.
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
			assertTrue(result + " should contain " + this.modelAddresses[i].getModel(),
			        result.contains(this.modelAddresses[i].getModel()));
		}
		
	}
	
	/**
	 * Tests for getModelIds
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
		
		// callback equals null
		try {
			this.store.getModelIds(this.correctUser, this.correctUserPass, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// everything equals null
		try {
			this.store.getModelIds(null, null, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
	}
	
	// Test IllegalArgumentException
	@Test
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
		try {
			this.store.getModelRevisions(this.correctUser, this.correctUserPass,
			        this.modelAddresses, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// everything equals null
		try {
			this.store.getModelRevisions(null, null, null, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
	}
	
	// Test if it behaves correctly for addresses of XModels the user has
	// access to
	@Test
	public void testGetModelRevisions() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> snapshotCallback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
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
		
		BatchedResult<XReadableModel>[] snapshotResult = snapshotCallback.getEffect();
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
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetModelRevisionsMixedAddresses() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> snapshotCallback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
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
		
		BatchedResult<XReadableModel>[] snapshotResult = snapshotCallback.getEffect();
		assertEquals(snapshotResult.length, tempArray.length);
		
		BatchedResult<Long>[] revisionResult = revisionCallback.getEffect();
		assertEquals(revisionResult.length, tempArray.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.modelAddresses.length; i++) {
			if(i == this.modelAddresses.length) {
				// this index contains an XAddress of a not existing XModel
				assertNull(snapshotResult[i].getResult());
				assertEquals(revisionResult[i].getResult(), (Long)XCommand.FAILED);
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
		assertEquals(result[0].getResult(), (Long)XCommand.FAILED);
	}
	
	// Test if it behaves correctly for addresses that do not address an XModel
	@Test
	public void testGetModelRevisionsWrongAddress() {
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		
		this.store.getModelRevisions(this.correctUser, this.correctUserPass, this.objectAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<Long>[] result = callback.getEffect();
		
		for(int i = 0; i < result.length; i++) {
			// assertNotNull(result[i].getResult());
			assertNotNull(result[i].getException());
			assertTrue(result[i].getException() instanceof RequestException);
		}
	}
	
	// Test if it behaves correctly for addresses of XModels a correct user has
	// access to
	@Test
	public void testGetModelSnapshots() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, this.modelAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableModel>[] result = callback.getEffect();
		assertEquals(result.length, this.modelAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.modelAddresses.length; i++) {
			assertNotNull(result[i].getResult());
			assertNull(result[i].getException());
			assertEquals(this.modelAddresses[i], result[i].getResult().getAddress());
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
		
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback;
		
		callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		this.store.getModelSnapshots(this.incorrectUser, this.incorrectUserPass,
		        this.modelAddresses, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetModelSnapshotsMixedAddressTypes() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		XAddress[] tempArray = new XAddress[this.modelAddresses.length + 1];
		System.arraycopy(this.modelAddresses, 0, tempArray, 0, this.modelAddresses.length);
		tempArray[this.modelAddresses.length] = this.notExistingModel;
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableModel>[] result = callback.getEffect();
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
		// TODO Maybe test more complex mixes? (use objectadresses too?)
	}
	
	// Test if it behaves correctly for addresses of XModels that don't
	// exist
	@Test
	public void testGetModelSnapshotsNotExistingModel() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingModel };
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableModel>[] result = callback.getEffect();
		assertNull(result[0].getResult());
		assertNull(result[0].getException());
	}
	
	// Test if IllegalArgumentException are thrown when null values are passed
	@Test
	public void testGetModelSnapshotsPassingNull() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		// first parameter equals null
		try {
			this.store.getModelSnapshots(null, this.correctUserPass, this.modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, null, this.modelAddresses, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		try {
			this.store.getModelSnapshots(null, null, null, callback);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null
		try {
			this.store.getModelSnapshots(this.correctUser, this.correctUserPass,
			        this.modelAddresses, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// everything equals null
		try {
			this.store.getModelSnapshots(null, null, null, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
	}
	
	// Test if it behaves correctly for addresses that do not address an XModel
	@Test
	public void testGetModelSnapshotsWrongAddress() {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		
		this.store.getModelSnapshots(this.correctUser, this.correctUserPass, this.objectAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableModel>[] result = callback.getEffect();
		for(int i = 0; i < result.length; i++) {
			assertNull(result[i].getResult());
			assertNotNull(result[i].getException());
			assertTrue(result[i].getException() instanceof RequestException);
		}
	}
	
	// Test if it behaves correctly for addresses of XObjects the user has
	// access to
	@Test
	public void testGetObjectSnapshots() {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, this.objectAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableObject>[] result = callback.getEffect();
		assertEquals(result.length, this.objectAddresses.length);
		
		// check order of returned snapshots
		for(int i = 0; i < this.objectAddresses.length; i++) {
			assertNotNull(result[i].getResult());
			assertNull(result[i].getException());
			assertEquals(this.objectAddresses[i], result[i].getResult().getAddress());
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
		
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback;
		
		// Test if it behaves correctly for wrong account + password
		// combinations
		callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		this.store.getObjectSnapshots(this.incorrectUser, this.incorrectUserPass,
		        this.objectAddresses, callback);
		
		assertFalse(this.waitOnCallback(callback));
		assertNull(callback.getEffect());
		assertNotNull(callback.getException());
		assertTrue(callback.getException() instanceof AuthorisationException);
	}
	
	// Test if it behaves correctly for mixes of the cases above
	@Test
	public void testGetObjectSnapshotsMixedAddresses() {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] tempArray = new XAddress[this.objectAddresses.length + 1];
		System.arraycopy(this.objectAddresses, 0, tempArray, 0, this.objectAddresses.length);
		tempArray[this.objectAddresses.length] = this.notExistingObject;
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableObject>[] result = callback.getEffect();
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
	
	// Test if it behaves correctly for addresses of XObjects that don't
	// exist
	@Test
	public void testGetObjectSnapshotsNotExistingObject() {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		XAddress[] tempArray = new XAddress[] { this.notExistingObject };
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, tempArray, callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableObject>[] result = callback.getEffect();
		assertNull(result[0].getResult());
		assertNull(result[0].getException());
	}
	
	// Test IllegalArgumentException
	@Test
	public void testGetObjectSnapshotsPassingNull() {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		// first parameter equals null
		try {
			this.store.getObjectSnapshots(null, this.correctUserPass, this.objectAddresses,
			        callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// second parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, null, this.objectAddresses, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// third parameter equals null
		callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		try {
			this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// all parameters equal null
		callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		try {
			this.store.getObjectSnapshots(null, null, null, callback);
			// there's something wrong if we reached this
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// callback equals null
		try {
			this.store.getObjectSnapshots(this.correctUser, this.correctUserPass,
			        this.objectAddresses, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// everything equals null
		try {
			this.store.getObjectSnapshots(null, null, null, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
	}
	
	// Test if it behaves correctly for addresses that do not address an XObject
	@Test
	public void testGetObjectSnapshotsWrongAddress() {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		
		this.store.getObjectSnapshots(this.correctUser, this.correctUserPass, this.modelAddresses,
		        callback);
		
		assertTrue(this.waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertNull(callback.getException());
		
		BatchedResult<XReadableObject>[] result = callback.getEffect();
		for(int i = 0; i < result.length; i++) {
			assertNull(result[i].getResult());
			assertNotNull(result[i].getException());
			assertTrue(result[i].getException() instanceof RequestException);
		}
	}
	
	/**
	 * Tests for getRepositoryId
	 */
	
	/*
	 * Test if it behaves correctly for a correct account + password combination
	 */
	@Test
	public void testGetRepositoryId() {
		XID correctUser = this.getCorrectUser();
		String correctUserPass = this.getCorrectUserPasswordHash();
		
		SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
		
		this.store.getRepositoryId(correctUser, correctUserPass, callback);
		
		assertTrue(waitOnCallback(callback));
		assertNotNull(callback.getEffect());
		assertEquals(callback.getEffect(), this.getRepositoryId());
		assertNull(callback.getException());
	}
	
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
		
		// callback equals null
		try {
			this.store.getRepositoryId(this.correctUser, this.correctUserPass, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
		
		// everything equals null
		try {
			this.store.getRepositoryId(null, null, null);
			// if we reach this, the method didn't work as expected
			fail();
		} catch(IllegalArgumentException iae) {
		}
	}
}
