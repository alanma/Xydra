package org.xydra.store.access;

import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.core.XX;
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
 * Assuming an average XId has 16 characters in a Java String, with a typical
 * Java overhead of factor 2, we can save only 32K XIds in 1 MB.
 *
 * @author dscharrer
 * @author xamde
 */

public interface XGroupDatabase {

	/**
	 * Built-in administrator-group. All of its members are allowed to
	 * read/write/access every resource.
	 */
	public static XId ADMINISTRATOR_GROUP_ID = XX.toId(NamingUtils.PREFIX_INTERNAL
	        + NamingUtils.NAMESPACE_SEPARATOR + "AdministratorGroup");

	/**
	 * Add an actor to a group.
	 *
	 * The actor will become a direct member of this group.
	 *
	 * @param actorId The {@link XId} of the actor to add to the group
	 * @param groupId The {@link XId} of the group the specified actor will be
	 *            added to
	 */
	@ModificationOperation
	void addToGroup(XId actorId, XId groupId);

	/**
	 * @return returns an set containing the {@link XId XIds} of the defined
	 *         groups. Groups are defined as long as they have at least one
	 *         member. Never null.
	 */
	@ReadOperation
	Set<XId> getGroups();

	/**
	 * Get all groups an actor is part of.
	 *
	 * @param actorOrGroupId The {@link XId} of the actor whose groups are to be
	 *            returned
	 * @return a set with all {@link XId XIds} of the groups the specified actor
	 *         is a member of. Never null.
	 */
	@ReadOperation
	Set<XId> getGroupsOf(XId actorOrGroupId);

	/**
	 * Get all actors that are a member of the given group.
	 *
	 * @param groupId The {@link XId} of the group which members {@link XId
	 *            XIds} are to be returned
	 * @return a set containing all {@link XId XIds} of the members of the
	 *         specified group. Never null.
	 */
	@ReadOperation
	Set<XId> getMembersOf(XId groupId);

	/**
	 * Check if the specified actor is a member of the specific group.
	 *
	 * @param actorId The {@link XId} of the actor whose membership status is to
	 *            be checked
	 * @param groupId The {@link XId} of the group for which the membership
	 *            status of the specified actor is to be checked
	 * @return true, if the specified actor is a member of the specified group
	 */
	@ReadOperation
	boolean hasGroup(XId actorId, XId groupId);

	/**
	 * Remove an actor from a group.
	 *
	 * @param actorId The {@link XId} of the actor for which to revoke group
	 *            membership.
	 * @param groupId The {@link XId} of the group the specified actor will be
	 *            removed from
	 */
	@ModificationOperation
	void removeFromGroup(XId actorId, XId groupId);

	/**
	 * Same as removing all actors from a group but can sometimes be implemented
	 * faster.
	 *
	 * @param groupId
	 */
	@ModificationOperation
	void removeGroup(XId groupId);

}
