package org.xydra.store.base;

import org.xydra.base.XID;


/**
 * Internal class that simply stores an actorId and a passwordHash.
 * 
 * @author voelkel
 */
public class Credentials {
	
	private XID actorId;
	private String passwordHash;
	
	public Credentials(XID actorId, String passwordHash) {
		super();
		this.setActorId(actorId);
		this.setPasswordHash(passwordHash);
	}
	
	public XID getActorId() {
		return this.actorId;
	}
	
	public String getPasswordHash() {
		return this.passwordHash;
	}
	
	public void setActorId(XID actorId) {
		this.actorId = actorId;
	}
	
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
