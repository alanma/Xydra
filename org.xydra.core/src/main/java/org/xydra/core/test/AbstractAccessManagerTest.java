package org.xydra.core.test;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;



/**
 * Test for implementations of {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractAccessManagerTest extends TestCase {
	
	private XAccessManager arm;
	
	/** member of both groupZero and groupOne */
	private XID actorAlpha;
	/** member of groupZero but not groupOne */
	private XID actorBeta;
	private XID groupZero;
	private XID groupOne;
	
	private XAddress r;
	private XAddress rA;
	private XAddress rB;
	private XAddress rA0;
	
	private XID access;
	private XID access2;
	
	abstract protected XAccessManager getAccessManager(XGroupDatabase groups, XAddress rA0);
	
	@Override
	@Before
	public void setUp() {
		
		final XGroupDatabase groups = new MemoryGroupDatabase();
		this.arm = new MemoryAccessManager(groups);
		
		final XIDProvider p = X.getIDProvider();
		
		// setup groups
		this.actorAlpha = p.fromString("Alpha");
		this.actorBeta = p.fromString("Beta");
		this.groupZero = p.fromString("Zero");
		this.groupOne = p.fromString("One");
		groups.addToGroup(this.actorAlpha, this.groupZero);
		groups.addToGroup(this.actorAlpha, this.groupOne);
		groups.addToGroup(this.actorBeta, this.groupZero);
		
		this.r = p.fromAddress("/repo/cookie");
		this.rA = p.fromAddress("/repo/cookie/objectA");
		this.rB = p.fromAddress("/repo/cookie/objectB");
		this.rA0 = p.fromAddress("/repo/cookie/objectA/field0");
		
		assert XX.contains(this.r, this.rA);
		assert XX.contains(this.r, this.rA0);
		assert XX.contains(this.r, this.rB);
		assert XX.contains(this.rA, this.rA0);
		
		this.access = p.fromString("foo");
		this.access2 = p.fromString("bar");
		
	}
	
	@Test
	public void testEmptyDatabase() {
		
		assertNull(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertNull(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertNull(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testSetActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		
		assertTrue(this.arm.isAccessDefined(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.getAccessDefinition(this.actorAlpha, this.r, this.access));
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccess(this.actorBeta, this.r, this.access));
		assertNull(this.arm.hasAccess(this.actorBeta, this.rA, this.access));
		assertNull(this.arm.hasAccess(this.actorBeta, this.rA0, this.access));
		
		assertNull(this.arm.hasAccess(this.actorAlpha, this.r, this.access2));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubtree(this.actorBeta, this.r, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.actorBeta, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.actorBeta, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access2));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubresource(this.actorBeta, this.r, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.actorBeta, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.actorBeta, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access2));
		
	}
	
	@Test
	public void testActorRefineActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, false);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rB, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rB, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rB, this.access));
		
	}
	
	@Test
	public void testChangeActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.r, this.access, false);
		
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testResetActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, false);
		
		this.arm.resetAccess(this.actorAlpha, this.rA, this.access);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		this.arm.resetAccess(this.actorAlpha, this.r, this.access);
		
		assertNull(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertNull(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertNull(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testSetGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.groupZero, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.groupZero, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.groupZero, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA0, this.access));
		
		assertNull(this.arm.hasAccess(this.groupOne, this.r, this.access));
		assertNull(this.arm.hasAccess(this.groupOne, this.rA, this.access));
		assertNull(this.arm.hasAccess(this.groupOne, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.groupZero, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.groupZero, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.groupZero, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubtree(this.groupOne, this.r, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.groupOne, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubtree(this.groupOne, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.groupZero, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.groupZero, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.groupZero, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubresource(this.groupOne, this.r, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.groupOne, this.rA, this.access));
		assertNull(this.arm.hasAccessToSubresource(this.groupOne, this.rA0, this.access));
		
	}
	
	@Test
	public void testActorOverrideGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.r, this.access, false);
		
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.groupZero, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.groupZero, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.groupZero, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.groupZero, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.groupZero, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.groupZero, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.groupZero, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.groupZero, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.groupZero, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA0, this.access));
		
	}
	
	@Test
	public void testConflictingGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		this.arm.setAccess(this.groupOne, this.r, this.access, false);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testActorRefineGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, false);
		this.arm.setAccess(this.actorBeta, this.rA0, this.access, false);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorBeta, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorBeta, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorBeta, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorBeta, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorBeta, this.rA0, this.access));
		
	}
	
	@Test
	public void testGroupNotRefineActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorBeta, this.r, this.access, true);
		this.arm.setAccess(this.groupZero, this.rA, this.access, false);
		this.arm.setAccess(this.groupOne, this.rA0, this.access, true);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccess(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorBeta, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorBeta, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorBeta, this.rA0, this.access));
		
	}
	
	@Test
	public void testAccessOnSubresource() {
		
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, true);
		
		assertNull(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertNull(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testRefineAccessOnSubresource() {
		
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA0, this.access, false);
		
		assertNull(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testNoInherit() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testNoInheritActorOverwrite() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, true);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testNoInheritGroupOverwrite() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.setAccess(this.groupZero, this.rA, this.access, true);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testNoInheritRefine() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.setAccess(this.actorAlpha, this.rA0, this.access, true);
		
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertTrue(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	// TODO test more methods
	
}
