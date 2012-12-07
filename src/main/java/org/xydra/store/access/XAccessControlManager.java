package org.xydra.store.access;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.model.XModel;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.XModelArm;


/**
 * Provides authorisation for a complete store/repository. Individual access
 * rights for models are provided via one {@link XModelArm} for each
 * {@link XModel}.
 * 
 * <h2>Security Warning</h2>
 * 
 * Users of this interface must make sure that the internal admin account is not
 * allowed to perform <em>any</em> operation from the outside.
 * 
 * @author xamde
 */
public interface XAccessControlManager {
	
	/**
	 * XID of <em>internal</em> admin account -- see security warning in
	 * interface comment
	 */
	public static final XID INTERNAL_ADMIN_ACCOUNT = XX.toId(NamingUtils.PREFIX_INTERNAL
	        + NamingUtils.NAMESPACE_SEPARATOR + "InternalXydraAdmin");
	
	/**
	 * @return the internally used {@link XAuthenticationDatabase} or null if
	 *         there is no internal database used.
	 */
	XAuthenticationDatabase getAuthenticationDatabase();
	
	/**
	 * @return the internally used {@link XAuthorisationManager}. Never null.
	 */
	XAuthorisationManager getAuthorisationManager();
	
	/**
	 * Should initialise internal data in the underlying persistence layer (if
	 * any, e.g. set up initial right management models). This method should be
	 * called after a {@link XydraStoreAdmin#clear()}.
	 */
	void init();
	
	/**
	 * Uses the internal {@link XAuthenticationDatabase} to calculate if the
	 * given credentials are authenticated.
	 * 
	 * @param actorId
	 * @param passwordHash
	 * @return true if the (actorId/passwordHash) combination is a valid
	 *         account.
	 */
	boolean isAuthenticated(XID actorId, String passwordHash);
	
}
