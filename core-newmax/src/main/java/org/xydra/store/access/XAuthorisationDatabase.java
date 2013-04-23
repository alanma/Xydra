package org.xydra.store.access;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;


/**
 * A database that stores inheritable access rights.
 * 
 * By design, implementations of this interface need a {@link XGroupDatabase}
 * (otherwise they can not correctly compute if a given actor has a certain
 * right).
 * 
 * @author dscharrer
 */
public interface XAuthorisationDatabase {
	
	/**
	 * Get the access value that is defined for this (actor, resource, access)
	 * combination. As opposed to
	 * {@link XAuthorisationManager#hasAccess(XId, XAddress, XId)} this method
	 * only returns the definition for the exact parameters (if there is any)
	 * and does not check for any inherited access rights. This will return a
	 * value not equal to {@link XAccessRightValue#UNDEFINED} exactly if the
	 * access has been set with {@link #setAccess(XId, XAddress, XId, boolean)}
	 * for the exact same parameters and not reset with
	 * {@link #resetAccess(XId, XAddress, XId)}. In this case the value set with
	 * setAccess() is returned.
	 * 
	 * @param actor
	 * @param resource
	 * @param access
	 * 
	 * @return the access right defined for this (actor,resource,access)
	 *         combination or null if there is no such definition
	 * @throws IllegalArgumentException if there is no access right defined for
	 *             this (actor,resource,access) combination.
	 */
	XAccessRightValue getAccessDefinition(XId actor, XAddress resource, XId access)
	        throws IllegalArgumentException;
	
	/**
	 * @return an {@link Set} with all access definitions in this manager. Never
	 *         null.
	 */
	Set<XAccessRightDefinition> getDefinitions();
	
	/**
	 * Checks whether the given access right is defined for the given actor on
	 * the specified resource or not.
	 * 
	 * @param actor The {@link XId} of the actor which's access rights are to be
	 *            checked.
	 * @param resource The {@link XAddress} of the resource on which the access
	 *            rights of the given actor are to be checked
	 * @param access The {@link XId} of the type of access being requested.
	 * 
	 * @return true if an access right for this (actor,resource,access)
	 *         combination has been defined by calling setAccess() and not
	 *         removed by resetAccess()
	 */
	boolean isAccessDefined(XId actor, XAddress resource, XId access);
	
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
	 * @param actor The {@link XId} of the actor to reset access for.
	 * @param resource The {@link XAddress} of the resource which access rights
	 *            of the given actor are to be reset.
	 * @param access The access type to reset.
	 * 
	 */
	void resetAccess(XId actor, XAddress resource, XId access);
	
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
	 * @param actor The {@link XId} of actor to set access for.
	 * @param resource The {@link XAddress} of the resource to set access for
	 *            the given actor.
	 * @param access The {@link XId} of the access type to allow or disallow.
	 * @param allowed True if the access is to be allowed or false if access is
	 *            to be denied.
	 * 
	 */
	void setAccess(XId actor, XAddress resource, XId access, boolean allowed);
	
}