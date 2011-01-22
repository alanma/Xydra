package org.xydra.core.model.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.TestLogger;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.value.XV;


/**
 * Tests for other implementations should set the appropriate
 * {@link XStateResolver} and {@link XStateFactory} in {@link XSPI} in their
 * {@link BeforeClass} method.
 * 
 * This class in /src/main/java to be available for other implementations in
 * other projects.
 * 
 * @author voelkel
 */
public abstract class AbstractStateTest {
	
	static XID f1 = XX.toId("field1");
	
	static XID m1 = XX.toId("model1");
	
	static XID o1 = XX.toId("object1");
	
	static XID repo1 = XX.toId("repo1");
	static XID REPOID = XX.toId("testrepo");
	static XAddress getRepositoryAddress() {
		return XX.toAddress(REPOID, null, null, null);
	}
	private XRepositoryState repositoryState;
	
	{
		TestLogger.init();
	}
	
	protected boolean canPersist() {
		return true;
	}
	
	@Before
	public void setUp() {
		this.repositoryState = XSPI.getStateStore().createRepositoryState(getRepositoryAddress());
	}
	
	@Test
	public void testAddModelStateNoParent() {
		/*
		 * FIXME how is this different from testAddModelStateWithParent()
		 * XModelState modelState = XSPI.getStateStore().createModelState(
		 * XX.toId("testmodel1"));
		 * this.repositoryState.addModelState(modelState);
		 */
	}
	
	@Test
	public void testAddModelStateWithParent() {
		XID modelId = XX.toId("testmodel1");
		XAddress modelAddr = XX.resolveModel(getRepositoryAddress(), modelId);
		XModelState modelState = XSPI.getStateStore().createModelState(modelAddr);
		this.repositoryState.addModelState(modelState);
	}
	
	/**
	 * Afterwards the parent {@link XObjectState} points to an ID for which no
	 * {@link XFieldState} exists. That is OK for the scope of this test.
	 */
	@Test
	public void testDeleteExistingFieldStateWithParent() {
		
		testSaveFieldStateWithParent();
		XAddress fieldStateAddress = XX.toAddress(null, null, o1, f1);
		if(canPersist()) {
			XFieldState fieldState = XSPI.getStateStore().loadFieldState(fieldStateAddress);
			assertNotNull(fieldState);
			fieldState.delete(null);
			assertNull(XSPI.getStateStore().loadFieldState(fieldStateAddress));
		}
	}
	
	@Test
	@Ignore
	public void testDeleteExistingModelState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testDeleteExistingObjectState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testDeleteExistingRepositoryState() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetID() {
		assertNotNull(this.repositoryState.getID());
		assertEquals("testrepo", this.repositoryState.getID().toString());
	}
	
	@Test
	public void testGetModelState() {
		
		XID modelState1ID = XX.toId("testmodel1");
		XID modelState2ID = XX.toId("testmodel2");
		XAddress modelState1Addr = XX.resolveModel(getRepositoryAddress(), modelState1ID);
		XAddress modelState2Addr = XX.resolveModel(getRepositoryAddress(), modelState2ID);
		
		XModelState modelState1 = XSPI.getStateStore().createModelState(modelState1Addr);
		assertFalse(this.repositoryState.hasModelState(modelState1ID));
		this.repositoryState.addModelState(modelState1);
		assertTrue(this.repositoryState.hasModelState(modelState1ID));
		
		this.repositoryState.save(null);
		modelState1.save(null);
		
		XModelState modelState2 = XSPI.getStateStore().createModelState(modelState2Addr);
		this.repositoryState.addModelState(modelState2);
		assertTrue(this.repositoryState.hasModelState(modelState2ID));
		
		assertEquals(modelState1ID, modelState1.getID());
		
		if(canPersist()) {
			XModelState modelState1again = XSPI.getStateStore().loadModelState(
			        XX.resolveModel(getRepositoryAddress(), modelState1ID));
			assertNotNull(modelState1again);
			assertTrue(modelState1.equals(modelState1again));
			assertEquals(modelState1, modelState1again);
		}
	}
	
	@Test
	public void testHasModelState() {
		XID modelId = XX.toId("testmodel1");
		XAddress modelAddr = XX.resolveModel(getRepositoryAddress(), modelId);
		XModelState modelState1 = XSPI.getStateStore().createModelState(modelAddr);
		assertFalse(this.repositoryState.hasModelState(modelState1.getID()));
		this.repositoryState.addModelState(modelState1);
		assertTrue(this.repositoryState.hasModelState(modelState1.getID()));
	}
	
	@Test
	public void testIsEmpty() {
		// create modelState without parent
		XID modelId = XX.toId("testmodel1");
		XAddress modelAddr = XX.resolveModel(getRepositoryAddress(), modelId);
		XModelState modelState1 = XSPI.getStateStore().createModelState(modelAddr);
		assertTrue(this.repositoryState.isEmpty());
		this.repositoryState.addModelState(modelState1);
		assertFalse(this.repositoryState.isEmpty());
	}
	
	@Test
	public void testIterator() {
		XID modelState1ID = XX.toId("testmodel1");
		XID modelState2ID = XX.toId("testmodel2");
		XAddress modelState1Addr = XX.resolveModel(getRepositoryAddress(), modelState1ID);
		XAddress modelState2Addr = XX.resolveModel(getRepositoryAddress(), modelState2ID);
		
		XModelState modelState1 = XSPI.getStateStore().createModelState(modelState1Addr);
		this.repositoryState.addModelState(modelState1);
		XModelState modelState2 = XSPI.getStateStore().createModelState(modelState2Addr);
		this.repositoryState.addModelState(modelState2);
		
		Iterator<XID> it = this.repositoryState.iterator();
		assertTrue(it.hasNext());
		it.next();
		assertTrue("Should contain 2 modelStates", it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}
	
	@Test
	@Ignore
	public void testLoadExistingFieldState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadExistingModelState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadExistingObjectState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadExistingRepositoryState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadNonExistingFieldState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadNonExistingModelState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadNonExistingObjectState() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore
	public void testLoadNonExistingRepositoryState() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testRemoveModelState() {
		XID modelState1ID = XX.toId("testmodel1");
		XID modelState2ID = XX.toId("testmodel2");
		XAddress modelState1Addr = XX.resolveModel(getRepositoryAddress(), modelState1ID);
		XAddress modelState2Addr = XX.resolveModel(getRepositoryAddress(), modelState2ID);
		
		XModelState modelState1 = XSPI.getStateStore().createModelState(modelState1Addr);
		this.repositoryState.addModelState(modelState1);
		XModelState modelState2 = XSPI.getStateStore().createModelState(modelState2Addr);
		this.repositoryState.addModelState(modelState2);
		
		this.repositoryState.removeModelState(modelState1ID);
		this.repositoryState.removeModelState(modelState2ID);
		
		assertTrue(this.repositoryState.isEmpty());
	}
	
	@Test
	public void testSaveAndLoad() {
		
		XID modelId1 = XX.toId("testModel1");
		XAddress modelAddr1 = XX.resolveModel(getRepositoryAddress(), modelId1);
		XModelState modelState1 = XSPI.getStateStore().createModelState(modelAddr1);
		this.repositoryState.addModelState(modelState1);
		assertTrue(this.repositoryState.hasModelState(modelId1));
		
		this.repositoryState.save(null);
		modelState1.save(null);
		
		if(canPersist()) {
			
			this.repositoryState = XSPI.getStateStore().loadRepositoryState(getRepositoryAddress());
			assertTrue("created with exaclty the same ID and never deleted",
			        this.repositoryState.hasModelState(modelId1));
			
			this.repositoryState = XSPI.getStateStore().loadRepositoryState(getRepositoryAddress());
			assertTrue(this.repositoryState.hasModelState(modelId1));
			
		}
	}
	
	/**
	 * Create, save and load {@link XFieldState} /-/-/o1/f1
	 */
	@Test
	public void testSaveFieldStateWithParent() {
		
		XAddress ao1 = XX.toAddress(null, null, o1, null);
		XAddress af1 = XX.resolveField(ao1, f1);
		
		XSPI.getStateStore().createObjectState(ao1);
		XFieldState fieldState = XSPI.getStateStore().createFieldState(af1);
		fieldState.setValue(XV.toValue(3.1415));
		fieldState.save(null);
		
		if(canPersist()) {
			XFieldState loadedFieldState = XSPI.getStateStore().loadFieldState(af1);
			
			assertEquals(fieldState.getAddress(), loadedFieldState.getAddress());
			assertEquals(fieldState.getRevisionNumber(), loadedFieldState.getRevisionNumber());
			assertEquals(fieldState.getValue(), loadedFieldState.getValue());
			
			assertNotNull(XSPI.getStateStore().loadFieldState(af1));
		}
	}
	
	@Test
	public void testSaveModelState() {
		XAddress am1 = XX.resolveModel(getRepositoryAddress(), m1);
		XModelState modelState = XSPI.getStateStore().createModelState(am1);
		this.repositoryState.addModelState(modelState);
		modelState.save(null);
		this.repositoryState.save(null);
		
		if(canPersist()) {
			
			// should not fail now
			XModelState loadedModelState = XSPI.getStateStore().loadModelState(am1);
			
			assertEquals(modelState.getID(), loadedModelState.getID());
			assertEquals(modelState, loadedModelState);
			
			assertEquals(modelState, loadedModelState);
			
		}
		
	}
	
	@Test
	public void testSaveObjectState() {
		XID modelId = XX.toId("testmodel");
		XAddress modelAddr = XX.resolveModel(getRepositoryAddress(), modelId);
		XAddress ao1 = XX.resolveObject(modelAddr, o1);
		
		XObjectState objectState = XSPI.getStateStore().createObjectState(ao1);
		
		XModelState modelState = XSPI.getStateStore().createModelState(modelAddr);
		
		modelState.addObjectState(objectState);
		
		this.repositoryState.addModelState(modelState);
		
		modelState.save(null);
		objectState.save(null);
		this.repositoryState.save(null);
		
		if(canPersist()) {
			XObjectState loadedObjectState = XSPI.getStateStore().loadObjectState(ao1);
			assertEquals(objectState, loadedObjectState);
		}
	}
	
	@Test
	public void testSaveRepositoryState() {
		XRepositoryState repositoryState = XSPI.getStateStore().createRepositoryState(
		        getRepositoryAddress());
		repositoryState.save(null);
		
		if(canPersist()) {
			XRepositoryState loadedRepositoryState = XSPI.getStateStore().loadRepositoryState(
			        getRepositoryAddress());
			assertEquals(repositoryState, loadedRepositoryState);
		}
	}
}
