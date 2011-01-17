package org.xydra.core.test.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabaseWithListeners;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessValue;


/**
 * Test for implementations of {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractAccessManagerTest {
	
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
	
	abstract protected XAccessManager getAccessManager(
	        XGroupDatabaseWithListeners groups, XAddress rA0);
	
	@Before
	public void setUp() {
		
		final XGroupDatabaseWithListeners groups = new MemoryGroupDatabase();
		this.arm = new MemoryAccessManager(groups);
		
		// setup groups
		this.actorAlpha = XX.toId("Alpha");
		this.actorBeta = XX.toId("Beta");
		this.groupZero = XX.toId("Zero");
		this.groupOne = XX.toId("One");
		groups.addToGroup(this.actorAlpha, this.groupZero);
		groups.addToGroup(this.actorAlpha, this.groupOne);
		groups.addToGroup(this.actorBeta, this.groupZero);
		
		this.r = XX.toAddress("/repo/cookie");
		this.rA = XX.toAddress("/repo/cookie/objectA");
		this.rB = XX.toAddress("/repo/cookie/objectB");
		this.rA0 = XX.toAddress("/repo/cookie/objectA/field0");
		
		assert this.r.contains(this.rA);
		assert this.r.contains(this.rA0);
		assert this.r.contains(this.rB);
		assert this.rA.contains(this.rA0);
		
		this.access = XX.toId("foo");
		this.access2 = XX.toId("bar");
		
	}
	
	private void assertAllowed(XAccessValue a) {
		assertEquals(XAccessValue.ALLOWED, a);
	}
	
	private void assertDenied(XAccessValue a) {
		assertEquals(XAccessValue.DENIED, a);
	}
	
	private void assertUndefined(XAccessValue a) {
		assertEquals(XAccessValue.UNDEFINED, a);
	}
	
	@Test
	public void testEmptyDatabase() {
		
		checkHasUndefinedAccess(this.actorAlpha);
		
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.rA, this.access));
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testSetActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		
		assertTrue(this.arm.isAccessDefined(this.actorAlpha, this.r, this.access));
		assertFalse(this.arm.isAccessDefined(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.getAccessDefinition(this.actorAlpha, this.r, this.access));
		
		checkHasAllAccess(this.actorAlpha);
		checkHasUndefinedAccess(this.actorBeta);
		
		assertUndefined(this.arm.hasAccess(this.actorAlpha, this.r, this.access2));
		assertUndefined(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access2));
		assertUndefined(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access2));
		
	}
	
	@Test
	public void testActorRefineActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, false);
		
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.rB, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubtree(this.actorAlpha, this.rB, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.rB, this.access));
		
	}
	
	@Test
	public void testChangeActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.r, this.access, false);
		
		checkHasNoAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testResetActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, false);
		
		this.arm.resetAccess(this.actorAlpha, this.rA, this.access);
		
		checkHasAllAccess(this.actorAlpha);
		
		this.arm.resetAccess(this.actorAlpha, this.r, this.access);
		
		checkHasUndefinedAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testSetGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		checkHasAllAccess(this.actorBeta);
		checkHasAllAccess(this.groupZero);
		
		checkHasUndefinedAccess(this.groupOne);
		
	}
	
	@Test
	public void testActorOverrideGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.r, this.access, false);
		
		checkHasNoAccess(this.actorAlpha);
		
		checkHasAllAccess(this.groupZero);
		checkHasAllAccess(this.actorBeta);
		
	}
	
	@Test
	public void testConflictingGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		this.arm.setAccess(this.groupOne, this.r, this.access, false);
		
		checkHasAllAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testActorRefineGroupAccess() {
		
		this.arm.setAccess(this.groupZero, this.r, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, false);
		this.arm.setAccess(this.actorBeta, this.rA0, this.access, false);
		
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccess(this.actorBeta, this.r, this.access));
		assertAllowed(this.arm.hasAccess(this.actorBeta, this.rA, this.access));
		assertDenied(this.arm.hasAccess(this.actorBeta, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(this.actorBeta, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorBeta, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorBeta, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorBeta, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(this.actorBeta, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorBeta, this.rA0, this.access));
		
	}
	
	@Test
	public void testGroupNotRefineActorAccess() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(this.actorBeta, this.r, this.access, true);
		this.arm.setAccess(this.groupZero, this.rA, this.access, false);
		this.arm.setAccess(this.groupOne, this.rA0, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		checkHasAllAccess(this.actorBeta);
		
	}
	
	@Test
	public void testAccessOnSubresource() {
		
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, true);
		
		assertUndefined(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertUndefined(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testRefineAccessOnSubresource() {
		
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, true);
		this.arm.setAccess(this.actorAlpha, this.rA0, this.access, false);
		
		assertUndefined(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testNoInherit() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	@Test
	public void testNoInheritActorOverwrite() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.setAccess(this.actorAlpha, this.rA, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testNoInheritGroupOverwrite() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.setAccess(this.groupZero, this.rA, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testNoInheritRefined() {
		
		this.arm.setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.setAccess(this.actorAlpha, this.rA0, this.access, true);
		
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccess(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.hasAccess(this.actorAlpha, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.hasAccessToSubtree(this.actorAlpha, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(this.actorAlpha, this.rA0, this.access));
		
	}
	
	private void checkHasAllAccess(XID actor) {
		
		assertAllowed(this.arm.hasAccess(actor, this.r, this.access));
		assertAllowed(this.arm.hasAccess(actor, this.rA, this.access));
		assertAllowed(this.arm.hasAccess(actor, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubtree(actor, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubtree(actor, this.rA, this.access));
		assertAllowed(this.arm.hasAccessToSubtree(actor, this.rA0, this.access));
		
		assertAllowed(this.arm.hasAccessToSubresource(actor, this.r, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(actor, this.rA, this.access));
		assertAllowed(this.arm.hasAccessToSubresource(actor, this.rA0, this.access));
		
	}
	
	private void checkHasNoAccess(XID actor) {
		
		assertDenied(this.arm.hasAccess(actor, this.r, this.access));
		assertDenied(this.arm.hasAccess(actor, this.rA, this.access));
		assertDenied(this.arm.hasAccess(actor, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubtree(actor, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubtree(actor, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubtree(actor, this.rA0, this.access));
		
		assertDenied(this.arm.hasAccessToSubresource(actor, this.r, this.access));
		assertDenied(this.arm.hasAccessToSubresource(actor, this.rA, this.access));
		assertDenied(this.arm.hasAccessToSubresource(actor, this.rA0, this.access));
		
	}
	
	private void checkHasUndefinedAccess(XID actor) {
		
		assertUndefined(this.arm.hasAccess(actor, this.r, this.access));
		assertUndefined(this.arm.hasAccess(actor, this.rA, this.access));
		assertUndefined(this.arm.hasAccess(actor, this.rA0, this.access));
		
		assertUndefined(this.arm.hasAccessToSubtree(actor, this.r, this.access));
		assertUndefined(this.arm.hasAccessToSubtree(actor, this.rA, this.access));
		assertUndefined(this.arm.hasAccessToSubtree(actor, this.rA0, this.access));
		
		assertUndefined(this.arm.hasAccessToSubresource(actor, this.r, this.access));
		assertUndefined(this.arm.hasAccessToSubresource(actor, this.rA, this.access));
		assertUndefined(this.arm.hasAccessToSubresource(actor, this.rA0, this.access));
		
	}
	
}
