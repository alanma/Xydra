package org.xydra.core.access;

import java.io.Serializable;
import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.index.query.Pair;
import org.xydra.store.MAXTodo;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessDatabase;
import org.xydra.store.access.XAccessValue;


/**
 * A database that stores inheritable access rights.
 * 
 * TODO how are users notified of changes to access rights? keep a history? can
 * this be put into the same history storing XEvents for the model?
 * 
 * @author dscharrer
 */
@MAXTodo
public interface XAccessManager extends XAccessDatabase, Serializable {
	
	/**
	 * Add a listener for access events.
	 */
	void addListener(XAccessListener listener);
	
	/**
	 * Checks whether the specified actor is allowed to execute the given
	 * {@link XCommand}
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param command The {@link XCommand} which is to be checked
	 * @return return if the actor is allowed to execute the {@link XCommand}
	 */
	boolean canExecute(XID actor, XCommand command);
	
	// TODO Why not have a generic canKnowAboutEntity(XID actor, XAddress
	// entity) method instead?
	/**
	 * @return true if the actor is allowed to know about the existence of the
	 *         specified {@link XField}
	 */
	public boolean canKnowAboutField(XID actor, XAddress objectAddr, XID fieldId);
	
	/**
	 * @return true if the actor is allowed to know about the existence of the
	 *         specified {@link XModel}
	 */
	public boolean canKnowAboutModel(XID actor, XAddress repoAddr, XID modelId);
	
	/**
	 * @return true if the actor is allowed to know about the existence of the
	 *         specified {@link XObject}
	 */
	public boolean canKnowAboutObject(XID actor, XAddress modelAddr, XID objectId);
	
	/**
	 * @return true if the actor is allowed to read from the specified resource
	 */
	boolean canRead(XID actor, XAddress resource);
	
	/**
	 * Checks whether the specified actor is allowed to remove the specified
	 * {@link XField} from the specified {@link XObject}.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param modelAddr The {@link XAddress} of the {@link XObject} for which
	 *            the allowance of the remove-operation is to be checked.
	 * @param objectID The {@link XID} of the {@link XField} the actor would
	 *            like to remove
	 * @return true if the actor is allowed to remove the {@link XField} with
	 *         the given {@link XID} from the given {@link XObject}
	 */
	boolean canRemoveField(XID actor, XAddress objectAddr, XID fieldId);
	
	/**
	 * Checks whether the specified actor is allowed to remove the specified
	 * {@link XModel} from the specified {@link XRepository}.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param repoAddr The {@link XAddress} of the {@link XRepository} for which
	 *            the allowance of the remove-operation is to be checked.
	 * @param modelId The {@link XID} of the {@link XModel} the actor would like
	 *            to remove
	 * @return true if the actor is allowed to remove the {@link XModel} with
	 *         the given {@link XID} from the specified {@link XRepository}
	 */
	boolean canRemoveModel(XID actor, XAddress repoAddr, XID modelId);
	
	/**
	 * Checks whether the specified actor is allowed to remove the specified
	 * {@link XObject} from the specified {@link XModel}.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param modelAddr The {@link XAddress} of the {@link XModel} for which the
	 *            allowance of the remove-operation is to be checked.
	 * @param objectID The {@link XID} of the {@link XObject} the actor would
	 *            like to remove
	 * @return true if the actor is allowed to remove the {@link XObject} with
	 *         the given {@link XID} from the given {@link XModel}
	 */
	boolean canRemoveObject(XID actor, XAddress modelAddr, XID objectId);
	
	/**
	 * @return true if the actor is allowed to write to the specified resource
	 */
	boolean canWrite(XID actor, XAddress resource);
	
	/**
	 * Get all actors that have access to a resource.
	 * 
	 * @param resource The resource being accessed.
	 * @param access The type of access being requested.
	 * @return Returns two sets of actors. An actor is allowed access exactly if
	 *         he or a group he is in is in the first set AND he isn't in the
	 *         second set. Groups in the second set do NOT mean that their
	 *         members don't have access and should be ignored.
	 * 
	 *         FIXME The documentation of the return value is too unclear to be
	 *         implemented.
	 */
	Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access);
	
	/**
	 * Get all types of access an actor has to a resource.
	 * 
	 * @param actor The {@link XID} of the actor of whom the access rights are
	 *            to be returned.
	 * @param resource The {@link XAddress} of the resource of which the access
	 *            rights of the given actor are to be returned.
	 * @return Returns two sets of permissions. Permissions in the first set are
	 *         explicitly allowed while permissions in the second set are
	 *         explicitly denied.
	 */
	Pair<Set<XID>,Set<XID>> getPermissions(XID actor, XAddress resource);
	
	/**
	 * Check if an actor has access rights to a specific resource according to
	 * this rule:
	 * 
	 * If there is a right defined for this (actor,resource,access) combination,
	 * its value is returned disregarding all other definitions.
	 * 
	 * -else- If there is a positive (allowed = true) right defined for any
	 * group this actor currently belongs to, and this exact resource, access is
	 * granted. Negative rights defined for groups are ignored.
	 * 
	 * -else- If there is a negative right for the all group (
	 * {@link XA#GROUP_ALL}) then access is denied.
	 * 
	 * -else- Return access for this actor on the parent resource.
	 * 
	 * If there are no rights defined for this actor or his groups on any
	 * resource in hierarchy to the requested resource, null is returned.
	 * 
	 * @param actor The {@link XID} of the actor trying to get access.
	 * @param resource The {@link XAddress} of the resource on which the access
	 *            rights of the given actor are to be checked.
	 * @param access The {@link XID} of the type of access being requested.
	 *            (read, write, ...)
	 * 
	 * @return true if the actor has access to the model, null if access is
	 *         undefined
	 * 
	 */
	XAccessValue hasAccess(XID actor, XAddress resource, XID access);
	
	/**
	 * Check if an actor has access rights to any resource in a subtree.
	 * 
	 * This is semantically equivalent to checking hasAccess() on every entity
	 * in the subtree (including the rootResource) but should be much more
	 * efficient, at least for sparse access managers.
	 * 
	 * @param actor The {@link XID} of the actor trying to get access.
	 * @param rootResource The {@link XAddress} of the root resource of the
	 *            subtree on which the access rights of the given actor are to
	 *            be checked.
	 * @param access The {@link XID} of the type of access being requested.
	 *            (read, write, ...)
	 * 
	 * @return true if the actor has access to the model, null if the access is
	 *         undefined for at least one resource and not defined to true for
	 *         any resource
	 * 
	 */
	XAccessValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access);
	
	/**
	 * Check if an actor has access rights to a whole resource subtree.
	 * 
	 * This is semantically equivalent to checking hasAccess() on every entity
	 * in the subtree but should be much more efficient, at least for sparse
	 * access managers.
	 * 
	 * @param actor The {@link XID} of the actor trying to get access.
	 * @param rootResource The {@link XAddress} of the root resource of the
	 *            subtree on which the access rights of the given actor are to
	 *            be checked.
	 * @param access The {@link XID} of the type of access being requested.
	 *            (read, write, ...)
	 * 
	 * @return true if the actor has access to the model, null if the access is
	 *         undefined for at least one resource and not defined to false for
	 *         any resource
	 * 
	 */
	XAccessValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access);
	
	/**
	 * Remove a listener for access events.
	 */
	void removeListener(XAccessListener listener);
	
}
