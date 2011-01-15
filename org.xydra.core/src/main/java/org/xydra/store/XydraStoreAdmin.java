package org.xydra.store;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.store.base.HashUtils;


/**
 * Administrative functions to manage a local {@link XydraStore}. The methods in
 * this interface should not be exposed over REST or other network interfaces
 * for security reasons.
 * 
 * @author xamde
 */
public interface XydraStoreAdmin {
	
	/**
	 * The XydraAdmin account may do everything, which includes create, read,
	 * update, delete user accounts; create, read, update, delete groups;
	 * create, read, update, delete mappings from users to groups; create, read,
	 * update, delete all Xydra entities (model, object, field, value).
	 */
	public static final XID XYDRA_ADMIN_ID = XX.toId("internal--XydraAdmin");
	
	/**
	 * @return the {@link XydraStore} for which this is the administrative
	 *         interface.
	 */
	XydraStore getXydraStore();
	
	/**
	 * Delete <em>all</em> data in the store. Intended to be used in unit tests.
	 */
	void clear();
	
	/**
	 * For security reasons, the default XydraAdmin password is set to a long,
	 * random string. Use this method to set your password.
	 * 
	 * Note: A servlet-based implementation should take configuration options
	 * set in web.xml and call this method.
	 * 
	 * @param passwordHash which is a password encoded via
	 *            {@link HashUtils#getXydraPasswordHash(String)}
	 */
	void setXydraAdminPasswordHash(String passwordHash);
	
	/**
	 * @return the hash of the XydraAdmin password. It has been constructed via
	 *         {@link HashUtils#getXydraPasswordHash(String)}.
	 */
	String getXydraAdminPasswordHash();
	
}
