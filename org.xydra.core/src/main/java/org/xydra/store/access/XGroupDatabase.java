package org.xydra.store.access;

import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.NamingUtils;


/**
 * A central database that stores user groups for access right management.
 * 
 * <h2>Scalability</h2>
 * 
 * Retrieving all members of a group is limited by two factors: (a) 1 MB limit
 * for a single data store call on GAE. (b) 1MB Limit for size of XValue object
 * on GAE.
 * 
 * Assuming an average XID has 16 characters in a Java String, with a typical
 * Java overhead of factor 2, we can save only 32K XIDs in 1 MB.
 * 
 * @author dscharrer
 * @author voelkel
 */

public interface XGroupDatabase {
	
	/**
	 * Built-in administrator-group. All of its members are allowed to
	 * read/write/access every resource.
	 */
	public static XID ADMINISTRATOR_GROUP_ID = XX.toId(NamingUtils.PREFIX_INTERNAL
	        + NamingUtils.NAMESPACE_SEPARATOR + "AdministratorGroup");
	
	/**
	 * Add an actor to a group.
	 * 
	 * The actor will become a direct member of this group.
	 * 
	 * @param actorId The {@link XID} of the actor to add to the group
	 * @param groupId The {@link XID} of the group the specified actor will be
	 *            added to
	 */
	@ModificationOperation
	void addToGroup(XID actorId, XID groupId);
	
	/**
	 * @return returns an set containing the {@link XID XIDs} of the defined
	 *         groups. Groups are defined as long as they have at least one
	 *         member. Never null.
	 */
	@ReadOperation
	Set<XID> getGroups();
	
	/**
	 * Get all groups an actor is part of.
	 * 
	 * @param actorOrGroupId The {@link XID} of the actor whose groups are to be
	 *            returned
	 * @return a set with all {@link XID XIDs} of the groups the specified actor
	 *         is a member of. Never null.
	 */
	@ReadOperation
	Set<XID> getGroupsOf(XID actorOrGroupId);
	
	/**
	 * Get all actors that are a member of the given group.
	 * 
	 * @param groupId The {@link XID} of the group which members {@link XID
	 *            XIDs} are to be returned
	 * @return a set containing all {@link XID XIDs} of the members of the
	 *         specified group. Never null.
	 */
	@ReadOperation
	Set<XID> getMembersOf(XID groupId);
	
	/**
	 * Check if the specified actor is a member of the specific group.
	 * 
	 * @param actorId The {@link XID} of the actor whose membership status is to
	 *            be checked
	 * @param groupId The {@link XID} of the group for which the membership
	 *            status of the specified actor is to be checked
	 * @return true, if the specified actor is a member of the specified group
	 */
	@ReadOperation
	boolean hasGroup(XID actorId, XID groupId);
	
	/**
	 * Remove an actor from a group.
	 * 
	 * @param actorId The {@link XID} of the actor for which to revoke group
	 *            membership.
	 * @param groupId The {@link XID} of the group the specified actor will be
	 *            removed from
	 */
	@ModificationOperation
	void removeFromGroup(XID actorId, XID groupId);
	
	/**
	 * Same as removing all actors from a group but can sometimes be implemented
	 * faster.
	 * 
	 * @param groupId
	 */
	@ModificationOperation
	void removeGroup(XID groupId);
	
}
