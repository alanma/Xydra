package org.xydra.core.access;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.core.model.XID;


/**
 * A central database that stores user groups for access right management.
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
	 * @param actor The {@link XID} of the actor (or subgroup) to add to the
	 *            group
	 * @param group The {@link XID} of the group the specified actor will be
	 *            added to
	 * 
	 * @throws CycleException if adding the group membership would produce a
	 *             cycle
	 */
	void addToGroup(XID actor, XID group) throws CycleException;
	
	/**
	 * Remove an actor from a group.
	 * 
	 * This will not remove the actor from all subgroups and thus he may retain
	 * membership of this group.
	 * 
	 * @param actor The {@link XID} of the actor for which to revoke group
	 *            membership.
	 * @param group The {@link XID} of the group the specified actor will be
	 *            removed from
	 */
	void removeFromGroup(XID actor, XID group);
	
	/**
	 * Check if the specified actor is a member of the specific group either
	 * directly or through a subgroup.
	 * 
	 * @param actor The {@link XID} of the actor whose membership status is to
	 *            be checked
	 * @param group The {@link XID} of the group for which the membership status
	 *            of the specified actor is to be checked
	 * @return true, if the specified actor is a member of the specified group
	 */
	boolean hasGroup(XID actor, XID group);
	
	/**
	 * Check if the specified actor is a member of the specific group directly
	 * (and not just a member of a subgroup of the specified group)
	 * 
	 * @param actor The {@link XID} of the actor whose membership status is to
	 *            be checked
	 * @param group The {@link XID} of the group for which the membership status
	 *            of the specified actor is to be checked
	 * @return true, if the specified actor is a member of the specified group
	 */
	boolean hasDirectGroup(XID actor, XID group);
	
	/**
	 * Get all groups an actor is a direct member of (will not contain groups
	 * the actor is only a member of by being a member of a subgroup of the
	 * specified group)
	 * 
	 * @param actor The {@link XID} of the actor whose groups are to be returned
	 * @return an iterator over all {@link XID XIDs} of the groups the specified
	 *         actor is a direct member of
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Iterator<XID> getGroups(XID actor);
	
	/**
	 * Get all direct members (and direct subgroups) of a group.
	 * 
	 * @param group The {@link XID} of the group which members and subgroup
	 *            {@link XID XIDs} are to be returned
	 * @return an iterator over all {@link XID XIDs} of the members and
	 *         subgroups of the specified group
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Iterator<XID> getMembers(XID group);
	
	/**
	 * Get all groups an actor or subgroup is part of, either directly or
	 * indirectly.
	 * 
	 * @param actor The {@link XID} of the actor whose groups are to be returned
	 * @return an iterator over all {@link XID XIDs} of the groups the specified
	 *         actor is a member of
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Iterator<XID> getAllGroups(XID actor);
	
	/**
	 * Get all actors that are either a direct member of the given group or an
	 * (indirect) member of a subgroup.
	 * 
	 * @param group The {@link XID} of the group which members and subgroup
	 *            {@link XID XIDs} are to be returned
	 * @return an iterator over all {@link XID XIDs} of the members and
	 *         subgroups of the specified group
	 * 
	 *         TODO iterator is problematic with concurrent access
	 */
	Iterator<XID> getAllMembers(XID group);
	
	/**
	 * Add a listener for {@link XGroupEvent XGroupEvents}. The listener will
	 * only receive events for defined group memberships, not for implied group
	 * memberships.
	 */
	void addListener(XGroupListener listener);
	
	/**
	 * Remove a listener for {@link XGroupEvent}s.
	 */
	void removeListener(XGroupListener listener);
	
	/**
	 * @return returns an iterator over the {@link XID XIDs} of the defined
	 *         groups. Groups are defined as long as they have at least one
	 *         member or subgroup.
	 */
	Iterator<XID> getGroups();
	
}
