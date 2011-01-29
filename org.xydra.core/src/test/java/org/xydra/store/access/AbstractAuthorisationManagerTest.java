package org.xydra.store.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


/**
 * Test for implementations of {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractAuthorisationManagerTest {
	
	private XID access;
	
	private XID access2;
	/** member of both groupZero and groupOne */
	private XID actorAlpha;
	/** member of groupZero but not groupOne */
	private XID actorBeta;
	private XAuthorisationManager arm;
	
	private XID groupOne;
	private XID groupZero;
	private XAddress r;
	private XAddress rA;
	
	private XAddress rA0;
	private XAddress rB;
	
	private void assertAllowed(XAccessRightValue a) {
		assertEquals(XAccessRightValue.ALLOWED, a);
	}
	
	private void assertDenied(XAccessRightValue a) {
		assertEquals(XAccessRightValue.DENIED, a);
	}
	
	private void assertUndefined(XAccessRightValue a) {
		assertEquals(XAccessRightValue.UNDEFINED, a);
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
	
	abstract protected XAuthorisationManager getAccessManager(XGroupDatabaseWithListeners groups,
	        XAddress rA0);
	
	@Before
	public void setUp() {
		
		final XGroupDatabaseWithListeners groups = new MemoryGroupDatabase();
		this.arm = new MemoryAuthorisationManager(groups);
		
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
	
	@Test
	public void testAccessOnSubresource() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA, this.access, true);
		
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
	public void testActorOverrideGroupAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.groupZero, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, false);
		
		checkHasNoAccess(this.actorAlpha);
		
		checkHasAllAccess(this.groupZero);
		checkHasAllAccess(this.actorBeta);
		
	}
	
	@Test
	public void testActorRefineActorAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA, this.access, false);
		
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
	public void testActorRefineGroupAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.groupZero, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA, this.access, false);
		this.arm.getAuthorisationDatabase().setAccess(this.actorBeta, this.rA0, this.access, false);
		
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
	public void testChangeActorAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, false);
		
		checkHasNoAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testConflictingGroupAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.groupZero, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.groupOne, this.r, this.access, false);
		
		checkHasAllAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testEmptyDatabase() {
		
		checkHasUndefinedAccess(this.actorAlpha);
		
		assertFalse(this.arm.getAuthorisationDatabase().isAccessDefined(this.actorAlpha, this.r,
		        this.access));
		assertFalse(this.arm.getAuthorisationDatabase().isAccessDefined(this.actorAlpha, this.rA,
		        this.access));
		assertFalse(this.arm.getAuthorisationDatabase().isAccessDefined(this.actorAlpha, this.rA0,
		        this.access));
		
	}
	
	@Test
	public void testGroupNotRefineActorAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.actorBeta, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.groupZero, this.rA, this.access, false);
		this.arm.getAuthorisationDatabase().setAccess(this.groupOne, this.rA0, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		checkHasAllAccess(this.actorBeta);
		
	}
	
	@Test
	public void testNoInherit() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		
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
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testNoInheritGroupOverwrite() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.getAuthorisationDatabase().setAccess(this.groupZero, this.rA, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testNoInheritRefined() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(XA.GROUP_ALL, this.rA, this.access, false);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA0, this.access, true);
		
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
	
	@Test
	public void testRefineAccessOnSubresource() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA, this.access, true);
		this.arm.getAuthorisationDatabase()
		        .setAccess(this.actorAlpha, this.rA0, this.access, false);
		
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
	public void testResetActorAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.rA, this.access, false);
		
		this.arm.getAuthorisationDatabase().resetAccess(this.actorAlpha, this.rA, this.access);
		
		checkHasAllAccess(this.actorAlpha);
		
		this.arm.getAuthorisationDatabase().resetAccess(this.actorAlpha, this.r, this.access);
		
		checkHasUndefinedAccess(this.actorAlpha);
		
	}
	
	@Test
	public void testSetActorAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.actorAlpha, this.r, this.access, true);
		
		assertTrue(this.arm.getAuthorisationDatabase().isAccessDefined(this.actorAlpha, this.r,
		        this.access));
		assertFalse(this.arm.getAuthorisationDatabase().isAccessDefined(this.actorAlpha, this.rA,
		        this.access));
		assertAllowed(this.arm.getAuthorisationDatabase().getAccessDefinition(this.actorAlpha,
		        this.r, this.access));
		
		checkHasAllAccess(this.actorAlpha);
		checkHasUndefinedAccess(this.actorBeta);
		
		assertUndefined(this.arm.hasAccess(this.actorAlpha, this.r, this.access2));
		assertUndefined(this.arm.hasAccessToSubtree(this.actorAlpha, this.r, this.access2));
		assertUndefined(this.arm.hasAccessToSubresource(this.actorAlpha, this.r, this.access2));
		
	}
	
	@Test
	public void testSetGroupAccess() {
		
		this.arm.getAuthorisationDatabase().setAccess(this.groupZero, this.r, this.access, true);
		
		checkHasAllAccess(this.actorAlpha);
		checkHasAllAccess(this.actorBeta);
		checkHasAllAccess(this.groupZero);
		
		checkHasUndefinedAccess(this.groupOne);
		
	}
	
}
