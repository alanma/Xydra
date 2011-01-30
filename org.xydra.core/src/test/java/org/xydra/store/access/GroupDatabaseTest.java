package org.xydra.store.access;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


// TODO make abstract and test with other impls, too
public class GroupDatabaseTest {
	
	static final XID actor1 = XX.toId("actor1");
	
	static final XID group1 = XX.toId("group1");
	
	static final XID group2 = XX.toId("group2");
	
	static final XID group3 = XX.toId("group3");
	static final XID group4 = XX.toId("group4");
	static final XID group5 = XX.toId("group5");
	static final XID group6 = XX.toId("group6");
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private MemoryGroupDatabase groupDb;
	
	@Before
	public void before() {
		this.groupDb = new MemoryGroupDatabase();
	}
	
	public void deprecatedTestAddingTransitiveGroups() {
		assertFalse(this.groupDb.getGroups().contains(group1));
		this.groupDb.addToGroup(actor1, group1);
		assertTrue(this.groupDb.getGroups().contains(group1));
		this.groupDb.addToGroup(actor1, group2);
		assertTrue(this.groupDb.getGroups().contains(group2));
		this.groupDb.addToGroup(group2, group3);
		assertTrue(this.groupDb.getGroups().contains(group3));
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group5));
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group6));
		this.groupDb.addToGroup(group4, group5);
		assertFalse("its not a group yet because it has no members", this.groupDb.getGroups()
		        .contains(group4));
		assertTrue(this.groupDb.getGroups().contains(group5));
		assertFalse(this.groupDb.getMembersOf(group6).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group6).contains(group4));
		assertTrue(this.groupDb.getGroupsOf(group4).contains(group5));
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group6));
		this.groupDb.addToGroup(group5, group6);
		assertTrue(this.groupDb.getGroups().contains(group6));
		
		GroupUtils.dump(this.groupDb);
		GroupUtils.dumpGroupId(this.groupDb, group4);
		
		assertTrue(this.groupDb.getMembersOf(group6).contains(group5));
		assertTrue(this.groupDb.getMembersOf(group6).contains(group4));
		assertTrue(this.groupDb.getGroupsOf(group4).contains(group5));
		assertTrue(this.groupDb.getGroupsOf(group4).contains(group6));
		
		/*
		 * <pre> A1 -memberOf-> G1, G2 G2 -memberOf-> G3 G4 -memberOf-> G5
		 * -memberOf-> G6
		 * 
		 * Now add G3 -memberOf-> G4 </pre>
		 */

		assertTrue(this.groupDb.getMembersOf(group3).contains(actor1));
		assertFalse(this.groupDb.getMembersOf(group4).contains(actor1));
		assertFalse(this.groupDb.getMembersOf(group5).contains(actor1));
		assertFalse(this.groupDb.getMembersOf(group6).contains(actor1));
		
		this.groupDb.addToGroup(group3, group4);
		
		assertTrue(this.groupDb.getMembersOf(group3).contains(actor1));
		assertTrue(this.groupDb.getMembersOf(group4).contains(actor1));
		assertTrue(this.groupDb.getMembersOf(group5).contains(actor1));
		assertTrue(this.groupDb.getMembersOf(group6).contains(actor1));
		
		this.groupDb.removeFromGroup(group3, group4);
		
		assertTrue(this.groupDb.getMembersOf(group3).contains(actor1));
		assertFalse(this.groupDb.getMembersOf(group4).contains(actor1));
		assertFalse(this.groupDb.getMembersOf(group5).contains(actor1));
		assertFalse(this.groupDb.getMembersOf(group6).contains(actor1));
	}
	
	public void deprecatedTestAddingTransitiveGroups1() {
		this.groupDb.addToGroup(group1, group2);
		this.groupDb.addToGroup(group2, group3);
		this.groupDb.addToGroup(group4, group5);
		this.groupDb.addToGroup(group5, group6);
		this.groupDb.addToGroup(group3, group4);
		
		/* transitive super-groups */

		assertFalse(this.groupDb.getGroupsOf(group1).contains(group1));
		assertTrue(this.groupDb.getGroupsOf(group1).contains(group2));
		assertTrue(this.groupDb.getGroupsOf(group1).contains(group3));
		assertTrue(this.groupDb.getGroupsOf(group1).contains(group4));
		assertTrue(this.groupDb.getGroupsOf(group1).contains(group5));
		assertTrue(this.groupDb.getGroupsOf(group1).contains(group6));
		
		assertFalse(this.groupDb.getGroupsOf(group2).contains(group1));
		assertFalse(this.groupDb.getGroupsOf(group2).contains(group2));
		assertTrue(this.groupDb.getGroupsOf(group2).contains(group3));
		assertTrue(this.groupDb.getGroupsOf(group2).contains(group4));
		assertTrue(this.groupDb.getGroupsOf(group2).contains(group5));
		assertTrue(this.groupDb.getGroupsOf(group2).contains(group6));
		
		assertFalse(this.groupDb.getGroupsOf(group3).contains(group1));
		assertFalse(this.groupDb.getGroupsOf(group3).contains(group2));
		assertFalse(this.groupDb.getGroupsOf(group3).contains(group3));
		assertTrue(this.groupDb.getGroupsOf(group3).contains(group4));
		assertTrue(this.groupDb.getGroupsOf(group3).contains(group5));
		assertTrue(this.groupDb.getGroupsOf(group3).contains(group6));
		
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group1));
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group2));
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group3));
		assertFalse(this.groupDb.getGroupsOf(group4).contains(group4));
		assertTrue(this.groupDb.getGroupsOf(group4).contains(group5));
		assertTrue(this.groupDb.getGroupsOf(group4).contains(group6));
		
		assertFalse(this.groupDb.getGroupsOf(group5).contains(group1));
		assertFalse(this.groupDb.getGroupsOf(group5).contains(group2));
		assertFalse(this.groupDb.getGroupsOf(group5).contains(group3));
		assertFalse(this.groupDb.getGroupsOf(group5).contains(group4));
		assertFalse(this.groupDb.getGroupsOf(group5).contains(group5));
		assertTrue(this.groupDb.getGroupsOf(group5).contains(group6));
		
		assertFalse(this.groupDb.getGroupsOf(group6).contains(group1));
		assertFalse(this.groupDb.getGroupsOf(group6).contains(group2));
		assertFalse(this.groupDb.getGroupsOf(group6).contains(group3));
		assertFalse(this.groupDb.getGroupsOf(group6).contains(group4));
		assertFalse(this.groupDb.getGroupsOf(group6).contains(group5));
		assertFalse(this.groupDb.getGroupsOf(group6).contains(group6));
		
		/* transitive sub-groups */

		assertFalse(this.groupDb.getMembersOf(group1).contains(group1));
		assertFalse(this.groupDb.getMembersOf(group1).contains(group2));
		assertFalse(this.groupDb.getMembersOf(group1).contains(group3));
		assertFalse(this.groupDb.getMembersOf(group1).contains(group4));
		assertFalse(this.groupDb.getMembersOf(group1).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group1).contains(group6));
		
		assertTrue(this.groupDb.getMembersOf(group2).contains(group1));
		assertFalse(this.groupDb.getMembersOf(group2).contains(group2));
		assertFalse(this.groupDb.getMembersOf(group2).contains(group3));
		assertFalse(this.groupDb.getMembersOf(group2).contains(group4));
		assertFalse(this.groupDb.getMembersOf(group2).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group2).contains(group6));
		
		assertTrue(this.groupDb.getMembersOf(group3).contains(group1));
		assertTrue(this.groupDb.getMembersOf(group3).contains(group2));
		assertFalse(this.groupDb.getMembersOf(group3).contains(group3));
		assertFalse(this.groupDb.getMembersOf(group3).contains(group4));
		assertFalse(this.groupDb.getMembersOf(group3).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group3).contains(group6));
		
		assertTrue(this.groupDb.getMembersOf(group4).contains(group1));
		assertTrue(this.groupDb.getMembersOf(group4).contains(group2));
		assertTrue(this.groupDb.getMembersOf(group4).contains(group3));
		assertFalse(this.groupDb.getMembersOf(group4).contains(group4));
		assertFalse(this.groupDb.getMembersOf(group4).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group4).contains(group6));
		
		assertTrue(this.groupDb.getMembersOf(group5).contains(group1));
		assertTrue(this.groupDb.getMembersOf(group5).contains(group2));
		assertTrue(this.groupDb.getMembersOf(group5).contains(group3));
		assertTrue(this.groupDb.getMembersOf(group5).contains(group4));
		assertFalse(this.groupDb.getMembersOf(group5).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group5).contains(group6));
		
		assertTrue(this.groupDb.getMembersOf(group6).contains(group1));
		assertTrue(this.groupDb.getMembersOf(group6).contains(group2));
		assertTrue(this.groupDb.getMembersOf(group6).contains(group3));
		assertTrue(this.groupDb.getMembersOf(group6).contains(group4));
		assertTrue(this.groupDb.getMembersOf(group6).contains(group5));
		assertFalse(this.groupDb.getMembersOf(group6).contains(group6));
		
	}
	
	@Test
	public void testAddAndRemoveOneActor() {
		assertTrue(this.groupDb.getGroupsOf(actor1).isEmpty());
		assertFalse(this.groupDb.getMembersOf(group1).contains(actor1));
		this.groupDb.addToGroup(actor1, group1);
		
		GroupUtils.dump(this.groupDb);
		
		assertFalse(this.groupDb.getGroupsOf(actor1).isEmpty());
		assertTrue(this.groupDb.getGroupsOf(actor1).contains(group1));
		assertTrue(this.groupDb.getMembersOf(group1).contains(actor1));
		this.groupDb.removeFromGroup(actor1, group1);
		assertTrue(this.groupDb.getGroupsOf(actor1).isEmpty());
		assertFalse(this.groupDb.getMembersOf(group1).contains(actor1));
	}
	
	/**
	 * === group1 subGroupOf group2 group1 isTransitiveMemberOf group2 . group2
	 * hasTransitiveMember group1 . === group2 subGroupOf group3 group2
	 * isTransitiveMemberOf group3 . group3 hasTransitiveMember group2 . ::
	 * group2 hasSubGroup group1 group1 isTransitiveMemberOf group3 . group3
	 * hasTransitiveMember group1 . === group3 subGroupOf group4 group3
	 * isTransitiveMemberOf group4 . group4 hasTransitiveMember group3 . ::
	 * group3 hasSubGroup group1 group1 isTransitiveMemberOf group4 . group4
	 * hasTransitiveMember group1 . :: group3 hasSubGroup group2 group2
	 * isTransitiveMemberOf group4 . group4 hasTransitiveMember group2 . ===
	 * group4 subGroupOf group5 group4 isTransitiveMemberOf group5 . group5
	 * hasTransitiveMember group4 . :: group4 hasSubGroup group3 group3
	 * isTransitiveMemberOf group5 . group5 hasTransitiveMember group3 . ::
	 * group4 hasSubGroup group1 group1 isTransitiveMemberOf group5 . group5
	 * hasTransitiveMember group1 . :: group4 hasSubGroup group2 group2
	 * isTransitiveMemberOf group5 . group5 hasTransitiveMember group2 . ===
	 * group5 subGroupOf group6 group5 isTransitiveMemberOf group6 . group6
	 * hasTransitiveMember group5 . :: group5 hasSubGroup group3 group3
	 * isTransitiveMemberOf group6 . group6 hasTransitiveMember group3 . ::
	 * group5 hasSubGroup group4 group4 isTransitiveMemberOf group6 . group6
	 * hasTransitiveMember group4 . :: group5 hasSubGroup group1 group1
	 * isTransitiveMemberOf group6 . group6 hasTransitiveMember group1 . ::
	 * group5 hasSubGroup group2 group2 isTransitiveMemberOf group6 . group6
	 * hasTransitiveMember group2 .
	 */
	
}
