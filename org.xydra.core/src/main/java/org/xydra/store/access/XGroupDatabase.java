package org.xydra.store.access;

import java.io.Serializable;
import java.util.Set;

import org.xydra.core.model.XID;


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
 */
public interface XGroupDatabase extends Serializable {
	
	public class CycleException extends IllegalArgumentException {
		
		private static final long serialVersionUID = -1021218459315159856L;
		
	}
	
	/**
	 * Add an actor to a group.
	 * 
	 * The actor will become a direct member of this group and an indirect
	 * member of all the groups this group belongs to (and all groups they
	 * belong to).
	 * 
	 * While groups can be added to groups, cycles are not allowed.
	 * 
	 * @param actorOrGroupId The {@link XID} of the actor (or subgroup) to add to the
	 *            group
	 * @param groupId The {@link XID} of the group the specified actor will be
	 *            added to
	 * 
	 * @throws CycleException if adding the group membership would produce a
	 *             cycle
	 */
	void addToGroup(XID actorOrGroupId, XID groupId) throws CycleException;
	
	/**
	 * Remove an actor from a group.
	 * 
	 * This will not remove the actor from all subgroups and thus he may retain
	 * membership of this group.
	 * 
	 * @param actorId The {@link XID} of the actor for which to revoke group
	 *            membership.
	 * @param groupId The {@link XID} of the group the specified actor will be
	 *            removed from
	 */
	void removeFromGroup(XID actorId, XID groupId);
	
	/**
	 * Check if the specified actor is a member of the specific group either
	 * directly or through a subgroup.
	 * 
	 * @param actorId The {@link XID} of the actor whose membership status is to
	 *            be checked
	 * @param groupId The {@link XID} of the group for which the membership status
	 *            of the specified actor is to be checked
	 * @return true, if the specified actor is a member of the specified group
	 */
	boolean hasGroup(XID actorId, XID groupId);
	
	/**
	 * Check if the specified actor is a member of the specific group directly
	 * (and not just a member of a subgroup of the specified group)
	 * 
	 * @param actorId The {@link XID} of the actor whose membership status is to
	 *            be checked
	 * @param groupId The {@link XID} of the group for which the membership status
	 *            of the specified actor is to be checked
	 * @return true, if the specified actor is a member of the specified group
	 */
	boolean hasDirectGroup(XID actorId, XID groupId);
	
	/**
	 * Get all groups an actor is a direct member of (will not contain groups
	 * the actor is only a member of by being a member of a subgroup of the
	 * specified group)
	 * 
	 * @param actorId The {@link XID} of the actor whose groups are to be returned
	 * @return an iterator over all {@link XID XIDs} of the groups the specified
	 *         actor is a direct member of
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Set<XID> getDirectGroups(XID actorId);
	
	/**
	 * Get all direct members (and direct subgroups) of a group.
	 * 
	 * @param groupId The {@link XID} of the group which members and subgroup
	 *            {@link XID XIDs} are to be returned
	 * @return an iterator over all {@link XID XIDs} of the members and
	 *         subgroups of the specified group
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Set<XID> getDirectMembers(XID groupId);
	
	/**
	 * Get all groups an actor or subgroup is part of, either directly or
	 * indirectly.
	 * 
	 * @param actorOrGroupId The {@link XID} of the actor whose groups are to be returned
	 * @return an iterator over all {@link XID XIDs} of the groups the specified
	 *         actor is a member of
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Set<XID> getAllGroups(XID actorOrGroupId);
	
	/**
	 * Get all actors that are either a direct member of the given group or an
	 * (indirect) member of a subgroup.
	 * 
	 * @param groupId The {@link XID} of the group which members and subgroup
	 *            {@link XID XIDs} are to be returned
	 * @return an iterator over all {@link XID XIDs} of the members and
	 *         subgroups of the specified group
	 * 
	 *         TODO iterator is problematic with concurrent access
	 * 
	 *         TODO can return null?
	 */
	Set<XID> getAllMembers(XID groupId);
	
	/**
	 * @return returns an iterator over the {@link XID XIDs} of the defined
	 *         groups. Groups are defined as long as they have at least one
	 *         member or subgroup.
	 */
	Set<XID> getDirectGroups();
	
}
