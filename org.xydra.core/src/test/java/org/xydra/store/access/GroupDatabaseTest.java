package org.xydra.store.access;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;


public class GroupDatabaseTest {
	
	private GroupModelWrapper wrapper;
	
	@Before
	public void before() {
		XID actorId = XX.toId("Test");
		XRepository repo = X.createMemoryRepository(actorId);
		this.wrapper = new GroupModelWrapper(repo, XX.toId("ARM"));
	}
	
	static final XID actor1 = XX.toId("actor1");
	static final XID group1 = XX.toId("group1");
	static final XID group2 = XX.toId("group2");
	static final XID group3 = XX.toId("group3");
	static final XID group4 = XX.toId("group4");
	static final XID group5 = XX.toId("group5");
	static final XID group6 = XX.toId("group6");
	
	@Test
	public void testAddAndRemoveOneActor() {
		assertTrue(this.wrapper.getGroupsOf(actor1).isEmpty());
		assertFalse(this.wrapper.getMembersOf(group1).contains(actor1));
		this.wrapper.addToGroup(actor1, group1);
		
		this.wrapper.dump();
		
		assertFalse(this.wrapper.getGroupsOf(actor1).isEmpty());
		assertTrue(this.wrapper.getGroupsOf(actor1).contains(group1));
		assertTrue(this.wrapper.getMembersOf(group1).contains(actor1));
		this.wrapper.removeFromGroup(actor1, group1);
		assertTrue(this.wrapper.getGroupsOf(actor1).isEmpty());
		assertFalse(this.wrapper.getMembersOf(group1).contains(actor1));
	}
	
	public void deprecatedTestAddingTransitiveGroups1() {
		this.wrapper.addToGroup(group1, group2);
		this.wrapper.addToGroup(group2, group3);
		this.wrapper.addToGroup(group4, group5);
		this.wrapper.addToGroup(group5, group6);
		this.wrapper.addToGroup(group3, group4);
		
		/* transitive super-groups */

		assertFalse(this.wrapper.getGroupsOf(group1).contains(group1));
		assertTrue(this.wrapper.getGroupsOf(group1).contains(group2));
		assertTrue(this.wrapper.getGroupsOf(group1).contains(group3));
		assertTrue(this.wrapper.getGroupsOf(group1).contains(group4));
		assertTrue(this.wrapper.getGroupsOf(group1).contains(group5));
		assertTrue(this.wrapper.getGroupsOf(group1).contains(group6));
		
		assertFalse(this.wrapper.getGroupsOf(group2).contains(group1));
		assertFalse(this.wrapper.getGroupsOf(group2).contains(group2));
		assertTrue(this.wrapper.getGroupsOf(group2).contains(group3));
		assertTrue(this.wrapper.getGroupsOf(group2).contains(group4));
		assertTrue(this.wrapper.getGroupsOf(group2).contains(group5));
		assertTrue(this.wrapper.getGroupsOf(group2).contains(group6));
		
		assertFalse(this.wrapper.getGroupsOf(group3).contains(group1));
		assertFalse(this.wrapper.getGroupsOf(group3).contains(group2));
		assertFalse(this.wrapper.getGroupsOf(group3).contains(group3));
		assertTrue(this.wrapper.getGroupsOf(group3).contains(group4));
		assertTrue(this.wrapper.getGroupsOf(group3).contains(group5));
		assertTrue(this.wrapper.getGroupsOf(group3).contains(group6));
		
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group1));
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group2));
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group3));
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group4));
		assertTrue(this.wrapper.getGroupsOf(group4).contains(group5));
		assertTrue(this.wrapper.getGroupsOf(group4).contains(group6));
		
		assertFalse(this.wrapper.getGroupsOf(group5).contains(group1));
		assertFalse(this.wrapper.getGroupsOf(group5).contains(group2));
		assertFalse(this.wrapper.getGroupsOf(group5).contains(group3));
		assertFalse(this.wrapper.getGroupsOf(group5).contains(group4));
		assertFalse(this.wrapper.getGroupsOf(group5).contains(group5));
		assertTrue(this.wrapper.getGroupsOf(group5).contains(group6));
		
		assertFalse(this.wrapper.getGroupsOf(group6).contains(group1));
		assertFalse(this.wrapper.getGroupsOf(group6).contains(group2));
		assertFalse(this.wrapper.getGroupsOf(group6).contains(group3));
		assertFalse(this.wrapper.getGroupsOf(group6).contains(group4));
		assertFalse(this.wrapper.getGroupsOf(group6).contains(group5));
		assertFalse(this.wrapper.getGroupsOf(group6).contains(group6));
		
		/* transitive sub-groups */

		assertFalse(this.wrapper.getMembersOf(group1).contains(group1));
		assertFalse(this.wrapper.getMembersOf(group1).contains(group2));
		assertFalse(this.wrapper.getMembersOf(group1).contains(group3));
		assertFalse(this.wrapper.getMembersOf(group1).contains(group4));
		assertFalse(this.wrapper.getMembersOf(group1).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group1).contains(group6));
		
		assertTrue(this.wrapper.getMembersOf(group2).contains(group1));
		assertFalse(this.wrapper.getMembersOf(group2).contains(group2));
		assertFalse(this.wrapper.getMembersOf(group2).contains(group3));
		assertFalse(this.wrapper.getMembersOf(group2).contains(group4));
		assertFalse(this.wrapper.getMembersOf(group2).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group2).contains(group6));
		
		assertTrue(this.wrapper.getMembersOf(group3).contains(group1));
		assertTrue(this.wrapper.getMembersOf(group3).contains(group2));
		assertFalse(this.wrapper.getMembersOf(group3).contains(group3));
		assertFalse(this.wrapper.getMembersOf(group3).contains(group4));
		assertFalse(this.wrapper.getMembersOf(group3).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group3).contains(group6));
		
		assertTrue(this.wrapper.getMembersOf(group4).contains(group1));
		assertTrue(this.wrapper.getMembersOf(group4).contains(group2));
		assertTrue(this.wrapper.getMembersOf(group4).contains(group3));
		assertFalse(this.wrapper.getMembersOf(group4).contains(group4));
		assertFalse(this.wrapper.getMembersOf(group4).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group4).contains(group6));
		
		assertTrue(this.wrapper.getMembersOf(group5).contains(group1));
		assertTrue(this.wrapper.getMembersOf(group5).contains(group2));
		assertTrue(this.wrapper.getMembersOf(group5).contains(group3));
		assertTrue(this.wrapper.getMembersOf(group5).contains(group4));
		assertFalse(this.wrapper.getMembersOf(group5).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group5).contains(group6));
		
		assertTrue(this.wrapper.getMembersOf(group6).contains(group1));
		assertTrue(this.wrapper.getMembersOf(group6).contains(group2));
		assertTrue(this.wrapper.getMembersOf(group6).contains(group3));
		assertTrue(this.wrapper.getMembersOf(group6).contains(group4));
		assertTrue(this.wrapper.getMembersOf(group6).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group6).contains(group6));
		
	}
	
	public void deprecatedTestAddingTransitiveGroups() {
		assertFalse(this.wrapper.getGroups().contains(group1));
		this.wrapper.addToGroup(actor1, group1);
		assertTrue(this.wrapper.getGroups().contains(group1));
		this.wrapper.addToGroup(actor1, group2);
		assertTrue(this.wrapper.getGroups().contains(group2));
		this.wrapper.addToGroup(group2, group3);
		assertTrue(this.wrapper.getGroups().contains(group3));
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group5));
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group6));
		this.wrapper.addToGroup(group4, group5);
		assertFalse("its not a group yet because it has no members", this.wrapper.getGroups()
		        .contains(group4));
		assertTrue(this.wrapper.getGroups().contains(group5));
		assertFalse(this.wrapper.getMembersOf(group6).contains(group5));
		assertFalse(this.wrapper.getMembersOf(group6).contains(group4));
		assertTrue(this.wrapper.getGroupsOf(group4).contains(group5));
		assertFalse(this.wrapper.getGroupsOf(group4).contains(group6));
		this.wrapper.addToGroup(group5, group6);
		assertTrue(this.wrapper.getGroups().contains(group6));
		
		this.wrapper.dump();
		this.wrapper.dumpGroupId(group4);
		
		assertTrue(this.wrapper.getMembersOf(group6).contains(group5));
		assertTrue(this.wrapper.getMembersOf(group6).contains(group4));
		assertTrue(this.wrapper.getGroupsOf(group4).contains(group5));
		assertTrue(this.wrapper.getGroupsOf(group4).contains(group6));
		
		/*
		 * <pre> A1 -memberOf-> G1, G2 G2 -memberOf-> G3 G4 -memberOf-> G5
		 * -memberOf-> G6
		 * 
		 * Now add G3 -memberOf-> G4 </pre>
		 */

		assertTrue(this.wrapper.getMembersOf(group3).contains(actor1));
		assertFalse(this.wrapper.getMembersOf(group4).contains(actor1));
		assertFalse(this.wrapper.getMembersOf(group5).contains(actor1));
		assertFalse(this.wrapper.getMembersOf(group6).contains(actor1));
		
		this.wrapper.addToGroup(group3, group4);
		
		assertTrue(this.wrapper.getMembersOf(group3).contains(actor1));
		assertTrue(this.wrapper.getMembersOf(group4).contains(actor1));
		assertTrue(this.wrapper.getMembersOf(group5).contains(actor1));
		assertTrue(this.wrapper.getMembersOf(group6).contains(actor1));
		
		this.wrapper.removeFromGroup(group3, group4);
		
		assertTrue(this.wrapper.getMembersOf(group3).contains(actor1));
		assertFalse(this.wrapper.getMembersOf(group4).contains(actor1));
		assertFalse(this.wrapper.getMembersOf(group5).contains(actor1));
		assertFalse(this.wrapper.getMembersOf(group6).contains(actor1));
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
