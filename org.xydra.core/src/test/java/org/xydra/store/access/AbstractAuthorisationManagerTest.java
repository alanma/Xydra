package org.xydra.store.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


/**
 * Test for implementations of {@link XAuthorisationManager}.
 *
 * @author dscharrer
 *
 */
abstract public class AbstractAuthorisationManagerTest {

	private XId access;

	private XId access2;
	/** member of both groupZero and groupOne */
	private XId actorAlpha;
	/** member of groupZero but not groupOne */
	private XId actorBeta;
	private XAuthorisationManager arm;

	private XId groupOne;
	private XId groupZero;
	private XAddress r;
	private XAddress rA;

	private XAddress rA0;
	private XAddress rB;

	private static void assertAllowed(final XAccessRightValue a) {
		assertEquals(XAccessRightValue.ALLOWED, a);
	}

	private static void assertDenied(final XAccessRightValue a) {
		assertEquals(XAccessRightValue.DENIED, a);
	}

	private static void assertUndefined(final XAccessRightValue a) {
		assertEquals(XAccessRightValue.UNDEFINED, a);
	}

	private void checkHasAllAccess(final XId actor) {

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

	private void checkHasNoAccess(final XId actor) {

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

	private void checkHasUndefinedAccess(final XId actor) {

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
		this.actorAlpha = Base.toId("Alpha");
		this.actorBeta = Base.toId("Beta");
		this.groupZero = Base.toId("Zero");
		this.groupOne = Base.toId("One");
		groups.addToGroup(this.actorAlpha, this.groupZero);
		groups.addToGroup(this.actorAlpha, this.groupOne);
		groups.addToGroup(this.actorBeta, this.groupZero);

		this.r = Base.toAddress("/repo/cookie");
		this.rA = Base.toAddress("/repo/cookie/objectA");
		this.rB = Base.toAddress("/repo/cookie/objectB");
		this.rA0 = Base.toAddress("/repo/cookie/objectA/field0");

		XyAssert.xyAssert(this.r.contains(this.rA));
		XyAssert.xyAssert(this.r.contains(this.rA0));
		XyAssert.xyAssert(this.r.contains(this.rB));
		XyAssert.xyAssert(this.rA.contains(this.rA0));

		this.access = Base.toId("foo");
		this.access2 = Base.toId("bar");

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
