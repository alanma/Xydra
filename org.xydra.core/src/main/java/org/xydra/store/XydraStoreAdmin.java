package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;


/**
 * Administrative functions to manage a specific XydraStore instance (or an
 * implementation delegate of it). The methods in this interface should not be
 * exposed over REST or other network interfaces for security reasons.
 * 
 * @author xamde
 * 
 */
public interface XydraStoreAdmin {
	
	/**
	 * The XydraAdmin account may do everything, which includes create, read,
	 * update, delete user accounts; create, read, update, delete groups;
	 * create, read, update, delete mappings from users to groups; create, read,
	 * update, delete all Xydra entities (model, object, field, value).
	 * 
	 * For security reasons, the default XydraAdmin password is set to a long,
	 * random string. Use {@link #getAccessControlManager()} to obtain an
	 * {@link XAccessControlManager} and then call
	 * {@link XAccessControlManager#getAuthenticationDatabase()} to get the
	 * {@link XAuthenticationDatabase} on which can then call
	 * {@link XAuthenticationDatabase#setPasswordHash(XId, String)} with acotrId
	 * = XYDRA_ADMIN_ID to set the administrator password. You can also retrieve
	 * the current administrator password via
	 * {@link XAuthenticationDatabase#getPasswordHash(XId)}.
	 * 
	 * Note: A servlet-based implementation should take configuration options
	 * set in web.xml and set the administrator password from it.
	 */
	public static final XId XYDRA_ADMIN_ID = XX.toId("internal--XydraAdmin");
	
	/**
	 * Delete <em>all</em> data in the store. Intended to be used in unit tests.
	 */
	void clear();
	
	/**
	 * @return the used {@link XAccessControlManager}. This method returns null
	 *         if the {@link XydraStore} does not support this (e.g. an
	 *         allow-all store).
	 */
	XAccessControlManager getAccessControlManager();
	
	/**
	 * @return the XId of the repository being managed.
	 */
	XId getRepositoryId();
	
}
