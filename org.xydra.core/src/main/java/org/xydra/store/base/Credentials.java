package org.xydra.store.base;

import org.xydra.core.model.XID;


public class Credentials {
	
	XID actorId;
	String passwordHash;
	
	public Credentials(XID actorId, String passwordHash) {
		super();
		this.actorId = actorId;
		this.passwordHash = passwordHash;
	}
}
