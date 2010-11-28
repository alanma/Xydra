package org.xydra.store.access;

import java.io.Serializable;
import java.util.Set;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;


/**
 * A database that stores inheritable access rights.
 * 
 * @author dscharrer
 */
public interface XAccessDatabase extends Serializable {
	
	/**
	 * Define access rights to a given resource and its descendants for an
	 * actor.
	 * 
	 * Access for individual descendants or fields may be overridden by defining
	 * other rights for their address.
	 * 
	 * If actor is a group, access for individual members can be overridden by
	 * defining rights for them.
	 * 
	 * @param actor The {@link XID} of actor to set access for.
	 * @param resource The {@link XAddress} of the resource to set access for
	 *            the given actor.
	 * @param access The {@link XID} of the access type to allow or disallow.
	 * @param allowed True if the access is to be allowed or false if access is
	 *            to be denied.
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
	 * @param actor The {@link XID} of the actor to reset access for.
	 * @param resource The {@link XAddress} of the resource which access rights
	 *            of the given actor are to be reset.
	 * @param access The access type to reset.
	 * 
	 */
	void resetAccess(XID actor, XAddress resource, XID access);
	
	/**
	 * Checks whether the given access right is defined for the given actor on
	 * the specified resource or not.
	 * 
	 * @param actor The {@link XID} of the actor which's access rights are to be
	 *            checked.
	 * @param resource The {@link XAddress} of the resource on which the access
	 *            rights of the given actor are to be checked
	 * @param access The {@link XID} of the type of access being requested.
	 * 
	 * @return true if an access right for this (actor,resource,access)
	 *         combination has been defined by calling setAccess() and not
	 *         removed by resetAccess()
	 */
	boolean isAccessDefined(XID actor, XAddress resource, XID access);
	
	/**
	 * Get the access value that is defined for this (actor, resource, access)
	 * combination. As opposed to {@link #hasAccess(XID, XAddress, XID)} this
	 * method only returns the definition for the exact parameters (if there is
	 * any) and does not check for any inherited access rights. This will return
	 * a value not equal to {@link XAccessValue#UNDEFINED} exactly if the access
	 * has been set with {@link #setAccess(XID, XAddress, XID, boolean)} for the
	 * exact same parameters and not reset with
	 * {@link #resetAccess(XID, XAddress, XID)}. In this case the value set with
	 * setAccess() is returned.
	 * 
	 * @return the access right defined for this (actor,resource,access)
	 *         combination or null if there is no such definition
	 * @throws IllegalArgumentException if there is no access right defined for
	 *             this (actor,resource,access) combination.
	 */
	XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException;
	
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
	 * @return an {@link Set} with all access definitions in this manager. Never
	 *         null.
	 */
	Set<XAccessDefinition> getDefinitions();
	
}
