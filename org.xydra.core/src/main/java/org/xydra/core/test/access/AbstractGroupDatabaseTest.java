package org.xydra.core.test.access;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.access.XA;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;



/**
 * Test for implementations of {@link XGroupDatabase}.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractGroupDatabaseTest extends TestCase {
	
	private XGroupDatabase groups;
	
	XID actorAlpha;
	XID actorBeta;
	XID groupZero;
	XID groupOne;
	
	protected abstract XGroupDatabase getGroupDB();
	
	@Override
	@Before
	public void setUp() {
		this.groups = getGroupDB();
		XIDProvider p = X.getIDProvider();
		this.actorAlpha = p.fromString("Alpha");
		this.actorBeta = p.fromString("Beta");
		this.groupZero = p.fromString("Zero");
		this.groupOne = p.fromString("One");
	}
	
	@Test
	public void testEmptyDatabase() {
		
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupZero));
		
	}
	
	@Test
	public void testAddToGroup() {
		
		this.groups.addToGroup(this.actorAlpha, this.groupZero);
		
		assertTrue(this.groups.hasGroup(this.actorAlpha, this.groupZero));
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupOne));
		assertFalse(this.groups.hasGroup(this.actorBeta, this.groupZero));
		
	}
	
	@Test
	public void testRemoveFromGroup() {
		
		this.groups.addToGroup(this.actorAlpha, this.groupZero);
		this.groups.removeFromGroup(this.actorAlpha, this.groupZero);
		
		assertFalse(this.groups.hasGroup(this.actorAlpha, this.groupZero));
		
	}
	
	@Test
	public void testNestedGroups() {
		
		this.groups.addToGroup(this.actorAlpha, this.groupZero);
		this.groups.addToGroup(this.groupZero, this.groupOne);
		
		assertTrue(this.groups.hasGroup(this.actorAlpha, this.groupOne));
		assertFalse(this.groups.hasDirectGroup(this.actorAlpha, this.groupOne));
		assertTrue(this.groups.hasDirectGroup(this.actorAlpha, this.groupZero));
		assertTrue(this.groups.hasDirectGroup(this.groupZero, this.groupOne));
		
	}
	
	@Test
	public void testAllGroup() {
		assertTrue(this.groups.hasGroup(null, XA.GROUP_ALL));
		assertTrue(this.groups.hasGroup(this.actorAlpha, XA.GROUP_ALL));
		assertTrue(this.groups.hasGroup(this.groupOne, XA.GROUP_ALL));
		assertTrue(this.groups.hasGroup(XA.GROUP_ALL, XA.GROUP_ALL));
		assertFalse(this.groups.hasGroup(XA.GROUP_ALL, this.actorAlpha));
	}
	
}
