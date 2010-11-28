package org.xydra.store.base;

import org.xydra.core.model.XID;


/**
 * Internal class that simply stores an actorId and a passwordHash.
 * 
 * @author voelkel
 */
public class Credentials {
	
	XID actorId;
	String passwordHash;
	
	public Credentials(XID actorId, String passwordHash) {
		super();
		this.actorId = actorId;
		this.passwordHash = passwordHash;
	}
}
