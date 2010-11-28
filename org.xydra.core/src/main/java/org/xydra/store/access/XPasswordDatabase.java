package org.xydra.store.access;

import org.xydra.core.model.XID;


/**
 * @author voelkel
 * 
 */
public interface XPasswordDatabase {
	
	/**
	 * @param actorId
	 * @param passwordHash
	 */
	void setPasswordHash(XID actorId, String passwordHash);
	
	/**
	 * @param actorId
	 */
	void removePasswordHash(XID actorId);
	
	/**
	 * @param actorId
	 * @param passwordHash
	 * @return
	 */
	boolean isValidLogin(XID actorId, String passwordHash);
	
}
