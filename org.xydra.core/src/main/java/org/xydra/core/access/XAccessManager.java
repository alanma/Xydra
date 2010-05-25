package org.xydra.core.access;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;


/**
 * A database that stores inheritable access rights.
 * 
 * TODO how are users notified of changes to access rights? keep a history? can
 * this be put into the same history storing XEvents for the model?
 * 
 * TODO how does the ARM query the Group database? is there an internal
 * reference to it (bad for serializing the arm and sending it to the client)
 * -or- do we pass the group db for each call -or- change actor to a class that
 * provides hasGroup()?
 * 
 * @author dscharrer
 */
public interface XAccessManager extends Serializable {
	
	/**
	 * Check if an actor has access rights to a specific resource according to
	 * this rule:
	 * 
	 * If there is a right defined for this (actor,resource,access) combination,
	 * it's value is returned disregarding all other definitions.
	 * 
	 * -else- If there is a positive (allowed = true) right defined for any
	 * group this actor currently belongs to, and this exact resource, access is
	 * granted. Group nesting is disregarded here. Negative rights defined for
	 * groups are ignored.
	 * 
	 * -else- If there is a negative right for the all group (
	 * {@link XA#GROUP_ALL}) then access is denied.
	 * 
	 * -else- Return access for this actor on the parent resource.
	 * 
	 * If there are no rights defined for this actor or his groups on any
	 * resource in hierarchy to the requested resource, null is returned.
	 * 
	 * @param actor The actor trying to get access.
	 * @param resource The resource being accessed.
	 * @param access The type of access being requested. (read, write, ...)
	 * 
	 * @return true if the actor has access to the model, null if access is
	 *         undefined
	 * 
	 */
	Boolean hasAccess(XID actor, XAddress resource, XID access);
	
	/**
	 * Check if an actor has access rights to a whole resource subtree.
	 * 
	 * This is semantically equivalent to checking hasAccess() on every entity
	 * in the subtree but should be much more efficient, at least for sparse
	 * access managers.
	 * 
	 * @param actor The actor trying to get access.
	 * @param rootResource The root resource of the subtree being accessed.
	 * @param access The type of access being requested. (read, write, ...)
	 * 
	 * @return true if the actor has access to the model, null if the access is
	 *         undefined for at least one resource and not defined to false for
	 *         any resource
	 * 
	 */
	Boolean hasAccessToSubtree(XID actor, XAddress rootResource, XID access);
	
	/**
	 * Check if an actor has access rights to any resource in a subtree.
	 * 
	 * This is semantically equivalent to checking hasAccess() on every entity
	 * in the subtree (including the rootResource) but should be much more
	 * efficient, at least for sparse access managers.
	 * 
	 * @param actor The actor trying to get access.
	 * @param rootResource The root resource of the subtree being accessed.
	 * @param access The type of access being requested. (read, write, ...)
	 * 
	 * @return true if the actor has access to the model, null if the access is
	 *         undefined for at least one resource and not defined to true for
	 *         any resource
	 * 
	 */
	Boolean hasAccessToSubresource(XID actor, XAddress rootResource, XID access);
	
	/**
	 * Define access rights to a given resource and it's descendants for an
	 * actor.
	 * 
	 * Access for individual descendants or fields may be overridden by defining
	 * other rights for their address.
	 * 
	 * If actor is a group, access for individual members can be overridden by
	 * defining rights for them.
	 * 
	 * @param actor The actor to set access for.
	 * @param resource The resource to set access for.
	 * @param access The access type to allow or disallow.
	 * @param allowed True if the access is to be allowed or false if access is
	 *            denied.
	 * 
	 */
	void setAccess(XID actor, XAddress resource, XID access, boolean allowed);
	
	/**
	 * Remove all access right definitions to a given resource for an actor.
	 * This reverts the effect of any setAccess() calls previously made for this
	 * exact (actor,resource,access) combination.
	 * 
	 * Access right defined for individual descendants of this resource are not
	 * removed.
	 * 
	 * Any access rights defined for ancestors of this resource still apply and
	 * are not changed.
	 * 
	 * @param actor The actor to reset access for.
	 * @param resource The resource to reset access for.
	 * @param access The access type to reset.
	 * 
	 */
	void resetAccess(XID actor, XAddress resource, XID access);
	
	/**
	 * @return true if an right for this (actor,resource,access) combination has
	 *         been defined by calling setAccess() and not removed by
	 *         resetAccess()
	 */
	boolean isAccessDefined(XID actor, XAddress resource, XID access);
	
	/**
	 * @return the right defined for this (actor,resource,access) combination or
	 *         null if there is no such definition
	 * @throws IllegalArgumentException if there is no right defined for this
	 *             (actor,resource,access) combination.
	 */
	Boolean getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException;
	
	/**
	 * Get all types of access an actor has to a resource.
	 * 
	 * @param actor The actor trying to get access.
	 * @param resource The resource being accessed.
	 * @return All access types for which hasPermission(actor,resource,access)
	 *         returns true.
	 */
	Set<XID> getPermissions(XID actor, XAddress resource);
	
	/**
	 * Get all actors that have access to a resource.
	 * 
	 * @param resource The resource being accessed.
	 * @param access The type of access being requested.
	 * @return Returns two sets of actors. An actor is allowed access exactly if
	 *         he or a group he is in is in the first set AND he isn't in the
	 *         second set. Groups in the second set do NOT mean that their
	 *         members don't have access and should be ignored.
	 */
	Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access);
	
	/**
	 * @return an {@link Iterator} over all access definitions in this manager.
	 *         TODO iterator is problematic with concurrent access
	 */
	Iterator<XAccessDefinition> getDefinitions();
	
	/**
	 * Add a listener for access events.
	 */
	void addListener(XAccessListener listener);
	
	/**
	 * Remove a listener for access events.
	 */
	void removeListener(XAccessListener listener);
	
	/**
	 * @return return if the actor is allowed to execute the {@link XCommand}
	 */
	boolean canExecute(XID actor, XCommand command);
	
	/**
	 * @return return if the actor is allowed to to execute the
	 *         {@link XAtomicCommand}
	 */
	boolean canExecute(XID actor, XAtomicCommand command);
	
	/**
	 * @return return if the actor is allowed to execute the
	 *         {@link XFieldCommand}
	 */
	boolean canExecute(XID actor, XFieldCommand command);
	
	/**
	 * @return return if the actor is allowed to execute the
	 *         {@link XObjectCommand}
	 */
	boolean canExecute(XID actor, XObjectCommand command);
	
	/**
	 * @return return if the actor is allowed to execute the
	 *         {@link XModelCommand}
	 */
	boolean canExecute(XID actor, XModelCommand command);
	
	/**
	 * @return return if the actor is allowed to execute the
	 *         {@link XRepositoryCommand}
	 */
	boolean canExecute(XID actor, XRepositoryCommand command);
	
	/**
	 * @return return if the actor is allowed to execute the
	 *         {@link XTransaction}
	 */
	boolean canExecute(XID actor, XTransaction trans);
	
	/**
	 * @return true if the actor is allowed to remove the model with the given
	 *         id from the given repository
	 */
	boolean canRemoveModel(XID actor, XAddress repoAddr, XID modelId);
	
	/**
	 * @return true if the actor is allowed to remove the object with the given
	 *         id from the given object
	 */
	boolean canRemoveObject(XID actor, XAddress modelAddr, XID objectId);
	
	/**
	 * @return true if the actor is allowed to remove the field with the given
	 *         id from the given object
	 */
	boolean canRemoveField(XID actor, XAddress objectAddr, XID fieldId);
	
	/**
	 * @return true if the actor is allowed to read from the given resource
	 */
	boolean canRead(XID actor, XAddress resource);
	
	/**
	 * @return true if the actor is allowed to write to the given resource
	 */
	boolean canWrite(XID actor, XAddress resource);
	
	/**
	 * @return true if the actor is allowed to know about the existence of the
	 *         given field
	 */
	public boolean canKnowAboutField(XID actor, XAddress objectAddr, XID fieldId);
	
	/**
	 * @return true if the actor is allowed to know about the existence of the
	 *         given object
	 */
	public boolean canKnowAboutObject(XID actor, XAddress modelAddr, XID objectId);
	
	/**
	 * @return true if the actor is allowed to know about the existence of the
	 *         given model
	 */
	public boolean canKnowAboutModel(XID actor, XAddress repoAddr, XID modelId);
	
}
