package org.xydra.core.access;

import java.util.Iterator;

import org.xydra.core.model.XID;



/**
 * A central database that stores user groups.
 * 
 * TODO MAX where to persist this information?
 * 
 * @author dscharrer
 */
public interface XGroupDatabase {
	
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
	 * @param actor The actor (or subgroup) to add to the group.
	 * @param group The group to add the actor to.
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
	 * @param actor The actor for which to revoke group membership.
	 * @param group The group to remove the actor from.
	 */
	void removeFromGroup(XID actor, XID group);
	
	/**
	 * Check if an actor belongs to a specific group either directly or through
	 * a subgroup.
	 */
	boolean hasGroup(XID actor, XID group);
	
	/**
	 * Has actor been added directly to the given group?
	 */
	boolean hasDirectGroup(XID actor, XID group);
	
	/**
	 * Get all groups an actor is a direct member of but not their subgroups..
	 * TODO iterator is problematic with concurrent access
	 */
	Iterator<XID> getGroups(XID actor);
	
	/**
	 * Get all direct members (and direct subgroups) of a group. TODO iterator
	 * is problematic with concurrent access
	 */
	Iterator<XID> getMembers(XID group);
	
	/**
	 * Get all groups an actor or subgroup is part of, either directly or
	 * indirectly. TODO iterator is problematic with concurrent access
	 */
	Iterator<XID> getAllGroups(XID actor);
	
	/**
	 * Get all actors that are either a direct member of the given group or an
	 * (indirect) member of a subgroup. TODO iterator is problematic with
	 * concurrent access
	 */
	Iterator<XID> getAllMembers(XID group);
	
	/**
	 * Add a listener for group events. The listener will only receive events
	 * for defined group memberships, not for implied group memberships.
	 */
	void addListener(XGroupListener listener);
	
	/**
	 * Remove a listener for group events.
	 */
	void removeListener(XGroupListener listener);
	
	/**
	 * @return all group IDs for which at least one member is defined.
	 */
	Iterator<XID> getGroups();
	
}
