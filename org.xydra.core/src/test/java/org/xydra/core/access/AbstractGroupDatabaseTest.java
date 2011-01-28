package org.xydra.core.access;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.access.XA;
import org.xydra.store.access.XGroupDatabaseWithListeners;


/**
 * Test for implementations of {@link XGroupDatabaseWithListeners}.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractGroupDatabaseTest {
	
	XID actorAlpha;
	
	XID actorBeta;
	
	XID groupOne;
	private XGroupDatabaseWithListeners groups;
	XID groupZero;
	{
		LoggerTestHelper.init();
	}
	
	protected abstract XGroupDatabaseWithListeners getGroupDB();
	
	@Before
	public void setUp() {
		this.groups = getGroupDB();
		this.actorAlpha = XX.toId("Alpha");
		this.actorBeta = XX.toId("Beta");
		this.groupZero = XX.toId("Zero");
		this.groupOne = XX.toId("One");
	}
	
	@Test
	public void testAddToGroup() {
		
		this.groups.addToGroup(this.actorAlpha, this.groupZero);
		
		assertTrue(this.groups.hasGroup(this.actorAlpha, this.groupZero));
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupOne));
		assertFalse(this.groups.hasGroup(this.actorBeta, this.groupZero));
		
	}
	
	@Test
	public void testAllGroup() {
		assertTrue(this.groups.hasGroup(null, XA.GROUP_ALL));
		assertTrue(this.groups.hasGroup(this.actorAlpha, XA.GROUP_ALL));
		assertTrue(this.groups.hasGroup(this.groupOne, XA.GROUP_ALL));
		assertTrue(this.groups.hasGroup(XA.GROUP_ALL, XA.GROUP_ALL));
		assertFalse(this.groups.hasGroup(XA.GROUP_ALL, this.actorAlpha));
	}
	
	@Test
	public void testEmptyDatabase() {
		
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupZero));
		
	}
	
	@Test
	public void testNestedGroups() {
		this.groups.addToGroup(this.actorAlpha, this.groupZero);
		
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupOne));
		assertTrue(this.groups.hasGroup(this.actorAlpha, this.groupZero));
	}
	
	@Test
	public void testRemoveFromGroup() {
		
		this.groups.addToGroup(this.actorAlpha, this.groupZero);
		this.groups.removeFromGroup(this.actorAlpha, this.groupZero);
		
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupZero));
		
	}
	
}
